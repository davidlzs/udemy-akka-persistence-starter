package maximizing_throughput_and_scalability.error_handling

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.stream.ActorMaterializer

import scala.io.StdIn
import scala.util.Random

object HttpServer extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val random = Random

  def body(value: Int): String = s"""{"value":$value}"""

  val Id = """[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}""".r

  val route =
    path(Id) { id =>
      get {
        if (random.nextInt % 2 > 10) {
          complete(StatusCodes.InternalServerError)
        }
        else {
          complete(
            StatusCodes.OK,
            HttpEntity(ContentTypes.`application/json`,
              body(random.nextInt(101)))
          )
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
