package com.ab
package pubsub

import org.zeromq.{SocketType, ZContext, ZMQ}

object ZmqPublisher {
  def main(args: Array[String]): Unit = {
    val context = new ZContext()
    val publisher = context.createSocket(SocketType.PUB)
    publisher.bind("tcp://127.0.0.1:5555")

    var msgId = 0
    while (true) {
      val message = s"Message ${msgId}"
      publisher.send(message.getBytes(ZMQ.CHARSET), 0)
      println(s"Published: $message")
      msgId += 1
      Thread.sleep(5000)
    }
  }
}
