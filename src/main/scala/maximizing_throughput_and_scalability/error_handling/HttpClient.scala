package maximizing_throughput_and_scalability.error_handling

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{RestartSource, Sink, Source}
import spray.json._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class Id(value: String)

object HttpClient extends App with DefaultJsonProtocol with SprayJsonSupport {

  /* val decider : Decider = {
     case DatabaseBusyException => Supervision.Resume
     case _ => Supervision.Stop
   }
  */
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer() //ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit def responseFormat = jsonFormat1(Response.apply)

  val ids = List(
    Id("bc6b456b-277a-4555-9248-c180762d981a"),
    Id("7676230e-f6e7-482e-8554-a53857a98ac7"),
    Id("ca182877-8c2a-4e88-8b1c-84d0e1da64c9"),
    Id("1d00f3af-200f-4606-bfa9-1201ecc6eb90"),
    Id("35e1f019-2ca7-434d-bbe9-e7f4cb4356ba"),
    Id("7bd746c7-333c-4358-97ae-89e6828adec3"),
    Id("1e26b82d-a922-4ce2-9129-b38011e0cd44"),
    Id("7db79f5c-a2dd-433d-807a-1e8af17031f9"),
    Id("ddf5e981-88f0-4dc3-99b2-e6abbd05309b"),
    Id("007597b8-1abd-484c-adf1-a0ea09837d1e")
  )

  val aggregate = RestartSource.withBackoff(
    minBackoff = 10 milliseconds,
    maxBackoff = 30 seconds,
    randomFactor = 0.2
  ) {
    () => {
      println(s"Retrying: ${System.currentTimeMillis()}")
      Source(ids)
        .mapAsync(parallelism = 4) { id =>
          Http().singleRequest(HttpRequest(uri = s"http://localhost:8080/${id.value}"))
        }
        .mapAsync(parallelism = 4) {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            Unmarshal(entity).to[Response]
          case HttpResponse(StatusCodes.InternalServerError, _, _, _) =>
            throw DatabaseBusyException
          case HttpResponse(statusCode, _, _, _) =>
            println(statusCode)
            throw DatabaseUnexpectedException(statusCode)
        }
    }
  }
  .map(_.value)
  //    .withAttributes(ActorAttributes.supervisionStrategy(decider))
  .runWith(Sink.fold(0)(_ + _))

  aggregate.onComplete {
    case Success(sum) => println(s"Sum : $sum")
    case Failure(error) => sys.error(s"Failure : $error")
  }
}

object DatabaseBusyException extends Exception

case class DatabaseUnexpectedException(statusCode: StatusCode) extends Exception

case class Response(value: Int)


