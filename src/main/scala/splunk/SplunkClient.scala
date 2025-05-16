package com.ab
package splunk

import akka.actor.ActorSystem
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal

import java.io.FileInputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{ SSLContext, TrustManagerFactory}
import scala.concurrent.Future
import scala.util.{Failure, Success}

object SplunkClient {

  implicit val system: ActorSystem = ActorSystem("actorSystemDemo")
  implicit val executionContext = system.dispatcher

  def createHttpsContext(): HttpsConnectionContext = {
    val truststoreFile = "/Users/arbhard2/truststore.jks"
    val password = "changeit".toCharArray

    val trustStore = KeyStore.getInstance("PKCS12")
    trustStore.load(new FileInputStream(truststoreFile), password)

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    tmf.init(trustStore)

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, tmf.getTrustManagers, new SecureRandom())

    ConnectionContext.httpsClient(sslContext)
  }

  def sendEventToSplunk(jsonEvent: String): Future[HttpResponse] = {
    val splunkHECUrl = "https://xyz.splunkcloud.com:8088/services/collector/event"
    val splunkToken = "7daa59ef-7fe2-47ed-ad3a-69c84bfa7788"
    val entity = HttpEntity(ContentTypes.`application/json`, jsonEvent)
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = splunkHECUrl,
      entity = entity
    ).withHeaders(RawHeader("Authorization", s"Splunk $splunkToken"))
    Http().singleRequest(request, createHttpsContext())
  }

  def main(args: Array[String]): Unit = {
    val jsonEvent =
      """
        |{
        |    "event": "hello world"
        |}
        |""".stripMargin

    val responseFuture = sendEventToSplunk(jsonEvent)

    responseFuture.onComplete {
      case Success(response) =>
        Unmarshal(response.entity).to[String].onComplete {
          case Success(body) => println(s"Response from Splunk: $body")
          case Failure(error) => println(s"Failed to unmarshal response: $error")
        }
      case Failure(exception) =>
        println(s"Failed to send event to Splunk: $exception")
    }
  }
}