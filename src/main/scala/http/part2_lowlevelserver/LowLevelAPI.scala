package http.part2_lowlevelserver

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object LowLevelAPI extends App {
  implicit val system = ActorSystem("LowLevelSeverAPI")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val serverSource = Http().bind("localhost", 8080)
  val connectionSink = Sink.foreach[IncomingConnection] { connection =>
    println(s"Accept incoming connection from ${connection.remoteAddress}")
  }

  private val serverBindingFuture: Future[Http.ServerBinding] = serverSource.to(connectionSink).run()

  serverBindingFuture.onComplete {
    case Success(binding) =>
      println("Server binding successful.")
      binding.terminate(2 seconds)
    case Failure(ex) => println(s"Server binging failed: $ex")
  }

  /*
  Method 1: synchronously
   */

  val httpRequestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
      HttpResponse(
        StatusCodes.OK, // 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<p>Akka HTTP rocks</p>
            |</body>
            |</html>
            |""".stripMargin
        )
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<p>Oops, not found</p>
            |</body>
            |</html>
            |""".stripMargin
        )
      )
  }

  private val httpSyncConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
    connection.handleWithSyncHandler(httpRequestHandler)
  }

  //Http().bind("localhost", 8090).runWith(httpSyncConnectionHandler)
  //shorthand version:
  //  Http().bindAndHandleSync(httpRequestHandler, "localhost", 8090)

  /*
   Method 2 Serve back HTTP response Async
   */

  val httpAsyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), _, _, _) =>
      Future(HttpResponse(
        StatusCodes.OK, // 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<p>Akka HTTP Asnyc rocks</p>
            |</body>
            |</html>
            |""".stripMargin
        )))
    case request: HttpRequest =>
      request.discardEntityBytes()
      Future(HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<p>Oops, not found async</p>
            |</body>
            |</html>
            |""".stripMargin
        )))
  }

  private val httpAsyncConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
    connection.handleWithAsyncHandler(httpAsyncRequestHandler)
  }
  Http().bind("localhost", 8090).runWith(httpAsyncConnectionHandler)
}
