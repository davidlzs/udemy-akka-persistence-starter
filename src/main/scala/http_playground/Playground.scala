package http_playground

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.io.StdIn

object Playground extends App {
  implicit val system = ActorSystem("AkkaHTTPPlayground")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val simpleRoute =
    pathEndOrSingleSlash {
      complete(HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        """
          |<html>
          |<p>Hello Akka HTTP</p>
          |</html>
          |""".stripMargin
      ))
    }

  val futureBinding: Future[Http.ServerBinding] = Http().bindAndHandle(simpleRoute, "localhost", 8080)

  StdIn.readLine()

  futureBinding
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}
