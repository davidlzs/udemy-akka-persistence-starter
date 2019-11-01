package http.part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, PathMatchers, Route}
import akka.stream.ActorMaterializer

import scala.util.matching.Regex

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

  val matcher: PathMatcher1[Option[Int]] =
    "foo" / "bar" / "X" ~ IntNumber.? / ("edit" | "create")
  println(matcher(Path("foo/bar/X42/edit or foo/bar/X/create")))

  println(PathMatchers.DoubleNumber(Path("123")))

  println(PathMatcher("[a-z]{2}".r)(Path("ca")))

  // type Route = RequestContext => Future[RouteResult]

  // Regex
  val date = """(\d\d\d\d)-(\d\d)-(\d\d)""".r
  println("2004-01-20" match {
    case date(year, month, day) => s"$year was a good year for PLs."
  })

  val event = """gateway\.(.+)\.device\.(.+)\.(.+)""".r
  println("gateway.1234.device.abc.lock" match {
    case event(gatewayId, deviceId, source) => s"$gatewayId, $deviceId, $source"
  })

  Http().bindAndHandle(chainedRoute, "localhost", 8080)

}
