package com.zero.server

import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider
import com.zero.server.actors.IN
import com.zero.server.actors.SocketWorker
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import akka.routing.RandomRouter

class SelectingRunnable() {
	import MainStart.HOST
	import MainStart.HOST_PORT
	val selector: Selector = initSelector(8888)

	def run() = {
		val system = ActorSystem("Zero")
		val prosessors = Runtime.getRuntime().availableProcessors() * 4
		val props = Props[SocketWorker].withRouter(RandomRouter(prosessors))

		val worker = system.actorOf(props)
		while (true) {
			selector.select()

			val selectedKeysItr = selector.selectedKeys().iterator()

			while (selectedKeysItr.hasNext()) {
				val key = selectedKeysItr.next().asInstanceOf[SelectionKey]
				selectedKeysItr.remove

				if (key.isValid) {
					if (key.isAcceptable)
						accept(key)
					else if (key.isReadable) {
						worker ! IN(key.channel().asInstanceOf[SocketChannel])
						key.cancel
					} 
				}
			}
		}
	}

	def initSelector(port: Int): Selector = {
		val socketSelector = SelectorProvider.provider().openSelector()
		val serverChannel = ServerSocketChannel.open
		val serverAddress = new InetSocketAddress(port)

		serverChannel.configureBlocking(false)
		serverChannel.socket().bind(serverAddress)
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT)

		return socketSelector
	}

	def accept(key: SelectionKey) = {
		val serverSocketChannel = key.channel().asInstanceOf[ServerSocketChannel]
		val socketChannel = serverSocketChannel.accept

		socketChannel.configureBlocking(false)
		socketChannel.register(selector, SelectionKey.OP_READ)
	}
}

