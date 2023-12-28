package com.ab
package pubsub

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import org.zeromq.{SocketType, ZContext, ZMQ}

import scala.concurrent.{ExecutionContextExecutor, Future}

object ZmqToWebhook {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("ZmqToWebhookAS")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val context = new ZContext()
    val subscriber = context.createSocket(SocketType.SUB)
    subscriber.connect("tcp://127.0.0.1:5555")
    subscriber.subscribe("".getBytes(ZMQ.CHARSET))

    while (true) {
      val msg = subscriber.recvStr(0)
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = "http://localhost:8080/webhook",
        entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, msg)
      ))
      responseFuture.onComplete(response => println(s"Response: $response"))
    }
  }
}
