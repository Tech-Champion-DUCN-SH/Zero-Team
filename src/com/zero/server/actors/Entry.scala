package com.zero.server.actors

import java.nio.channels.SocketChannel

case class Status(code: Int, text: String)
case class IN(channel: SocketChannel)
case class JsonMsg(kind: String, short: String, long: String)
case class errorMsg()

class Entry {

}