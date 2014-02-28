package com.zero.server

object MainStart extends App {
	import scala.collection.mutable.{ HashMap }
	val STORE = HashMap[Int, String]()
	val BASE_TIME = System.currentTimeMillis()
	var HOST = "127.0.0.1"
	var HOST_PORT = 8888

	val configuration = loadConfiguration(args)
	def loadConfiguration(args: Array[String]) = {
		if (args.length > 0 && args.length <= 2) {
			HOST = args(0)
			HOST_PORT = args(1).toInt
		} else {
			println("App arguments error")
			System.exit(1)
		}
	}

	println("This is ScalaWebServer running on port " + HOST_PORT);
	new SelectingRunnable().run
}