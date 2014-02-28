package com.zero.server.actors

import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.Date
import java.util.StringTokenizer

import scala.util.Random

import com.zero.server.MainStart
import com.zero.server.MainStart.BASE_TIME
import com.zero.server.MainStart.STORE
import com.zero.util.ServerUtil

import akka.actor.Actor

class SocketWorker extends Actor {

	val rng = new Random()
	var readBuffer: ByteBuffer = ByteBuffer.allocate(512)

	val longUrl = """/urlshortener/url\?longUrl=(.*)""".r
	val shortUrl = """/urlshortener/url\?shortUrl=.*/([\w\d]+)""".r

	def receive = {
		case IN(channel) =>
			var ret: Int = 0
			var recBytes: Int = 0;
			try {
				while ({ ret = channel.read(readBuffer); ret > 0 }) {
					recBytes += ret
				}
			} catch {
				case e: Exception =>
					println("Read data exception")
			} finally {
				readBuffer.flip();
			}
			if (recBytes > 0) {
				var msg = Charset.forName("UTF-8").decode(readBuffer).toString()
				readBuffer.clear()
				val tokens = new StringTokenizer(msg.split("\n")(0))

				tokens.nextToken match {
					case method @ ("GET" | "get") =>
						var path = tokens.nextToken
						path = URLDecoder.decode(path, "utf-8")
						path match {
							case url @ ("/") =>
								channel.write(ByteBuffer.wrap(respondWithError(Status(200, "OK"))))
							case url @ longUrl(a) =>
								val key: Int = (System.currentTimeMillis() - BASE_TIME).toInt + rng.nextInt(10000)
								val str = ServerUtil.dehydrate(key)
								STORE += (key -> a)
								channel.write(ByteBuffer.wrap(respondWithJson(Status(200, "OK"), "application/json", new JsonMsg("shorten", str, a))))
							case url @ shortUrl(a) =>
								val long = STORE.get(ServerUtil.saturate(a)).get
								channel.write(ByteBuffer.wrap(respondWithJson(Status(200, "OK"), "application/json", new JsonMsg("expand", a, long))))
							case _ =>
								channel.write(ByteBuffer.wrap(respondWithError(Status(200, "OK"))))
						}

					case method =>
						channel.write(ByteBuffer.wrap(respondWithError(Status(200, "OK"))))
				}
			}
			channel.close()
	}
	def respondWithError(status: Status): Array[Byte] = {
		val content = s"""{"error": true,"code": 404,"message": "Not found"}"""

		val header = s"""
      |HTTP/1.1 ${status.code} ${status.text}
      |Server: Scala HTTP Server 1.0
      |Date: ${new Date()}
      |Content-type: application/json;charset=UTF-8
      |Content-length: ${content.length}
      |
      |${content}
    """.trim.stripMargin
		header.getBytes("UTF-8")
	}

	def respondWithJson(status: Status, contentType: String = "application/json", body: JsonMsg): Array[Byte] = {
		val content = s"""
					|{"kind": "${body.kind}","shortUrl": "http://127.0.0.1:8888/${body.short}","longUrl": "${body.long}"}
			""".trim().stripMargin

		val header = s"""
      |HTTP/1.1 ${status.code} ${status.text}
      |Server: Scala HTTP Server 1.0
      |Date: ${new Date()}
      |Content-type: ${contentType};charset=UTF-8
      |Content-length: ${content.length}
      |
      |${content}
    """.trim.stripMargin
		header.getBytes("UTF-8")
	}
}