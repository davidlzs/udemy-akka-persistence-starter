package http.part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

object ChunkedGzipUpload {
  def main(args: Array[String]) {

    val config =
      ConfigFactory.parseString(
        """
          |akka.loglevel = debug
          |akka.http.server.log-unencrypted-network-bytes = 1000
          |akka.http.client.log-unencrypted-network-bytes = 1000
        """.stripMargin
      ).withFallback(ConfigFactory.defaultApplication())

    implicit val system = ActorSystem("my-system", config)
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      post {
        decodeRequest {
          entity(as[String]) { text ⇒
            println(s"Server got: $text")
            complete(s"Got: $text")
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9999)

    clientRequest()

    bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
  }

  private def clientRequest() = {

    val config =
      ConfigFactory.parseString(
        """
          |akka.loglevel = debug
          |akka.http.server.log-unencrypted-network-bytes = 1000
          |akka.http.client.log-unencrypted-network-bytes = 1000
        """.stripMargin
      ).withFallback(ConfigFactory.defaultApplication())

    implicit val system = ActorSystem("my-system", config)
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val testDataSource = Source("This is " :: "my" :: " teststring" :: Nil map (ByteString(_, "utf8")))
    val uncompressedRequest =
      HttpRequest(
        HttpMethods.POST,
        uri = "http://localhost:9999",
        entity = HttpEntity.Chunked.fromData(ContentTypes.`text/plain(UTF-8)`, testDataSource)
      )
    val compressedRequest = Gzip.encodeMessage(uncompressedRequest)

    val responseFut = Http(system).singleRequest(compressedRequest)
    val response = Await.result(responseFut, 10.seconds)
    println("Got response")
    println(response)
    system.terminate()
  }
}
