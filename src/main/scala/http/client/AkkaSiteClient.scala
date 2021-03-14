package http.client

import akka.actor.{ActorSystem, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.remote.WireFormats.FiniteDuration
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object AkkaSiteClient extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val responseFuture: Future[Terminated] =
    Http()
      .singleRequest(HttpRequest(uri = "https://integration-api-iot.yardienergy.com/iot-api/monitoring"))
      .flatMap(res => res.entity.toStrict(5.seconds))
      .andThen {
        case Success(entity) => entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
          //            println(body.utf8String)
          print(body.utf8String)
        }
        case Failure(e) => println("something went wrong: " + e)
      }
      .flatMap(_ => Http().shutdownAllConnectionPools())
      .flatMap(_ => system.terminate())

  Await.ready(responseFuture, 60.seconds)
}
