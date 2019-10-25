package http.part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object HighLevelIntro extends App {
  implicit val system = ActorSystem("HighLevelIntro")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  // directives
  import akka.http.scaladsl.server.Directives._

  val simpleRoute: Route =
    path("home") {
      complete(StatusCodes.OK)
    }

  val getRoute =
    path("home") {
      get {
        complete(StatusCodes.OK)
      }
    }

  val chainedRoute =
    path("api") {
      get {
        complete(StatusCodes.OK)
      } ~ // chaining, needed, be careful
      post {
        complete(StatusCodes.Forbidden)
      }
    } ~
   path ("home") {
     complete(
       HttpEntity(ContentTypes.`text/html(UTF-8)`,
       """
         |<html>
         |<body>
         |<p>Hi from akka HTTP
         |</body>
         |</html>
         |""".stripMargin)
     )
   } // routing tree

  Http().bindAndHandle(chainedRoute, "localhost", 8080)
}
