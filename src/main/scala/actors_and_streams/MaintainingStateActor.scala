package actors_and_streams

import actors_and_streams.Total.Increment
import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.duration._

object Total {

  case class Increment(value: Long)

}

class Total extends Actor {
  var total: Long = 0

  override def receive: Receive = {
    case Increment(value) =>
      total = total + value
  }
}


object Messages {

  def parse(messages: Seq[String]): Measurements = {
    Measurements(
      messages.map { message =>
        implicit val measurementMessageFormat = Json.format[MeasurementMessage]
        implicit val measurementContainerMessageFormat = Json.format[MeasurementContainerMessage]
        println(message)
        Json.parse(message).as[MeasurementContainerMessage]
      }.flatMap(msg => {
        val id = msg.id
        val timestamp = msg.timestamp
        val m = msg.measurements
        Seq(
          Measurement(id, timestamp, "power", m.power),
          Measurement(id, timestamp, "rotor_speed", m.rotor_speed),
          Measurement(id, timestamp, "wind_speed", m.wind_speed)
        )
      })
    )
  }

  def ack(message: String): Message = {
    TextMessage(message)
  }
}


case class Measurements(measurements: Seq[Measurement]) {
  var sum: Long = 100
}

case class Measurement(id: String, timestamp: Long, signal: String, value: Float)

case class MeasurementMessage(power: Float, rotor_speed: Float, wind_speed: Float)

case class MeasurementContainerMessage(id: String, timestamp: Long, measurements: MeasurementMessage)


object MaintainingStateActor extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val total = system.actorOf(Props[Total], "total")

  val measurementsWebSocket =
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) =>
          Future.successful(text)
        case TextMessage.Streamed(textStream) =>
          textStream.runFold("")(_ + _)
            .flatMap(Future.successful)
      }
      .mapAsync(1)(identity)
      .groupedWithin(1000, 1 second)
      .map(messages => (messages.last, Messages.parse(messages)))
      .map {
        case (lastMessage, measurements) =>
          total ! Increment(measurements.sum)
          lastMessage
      }
      .map(Messages.ack)

  val route =
    path("measurements" / JavaUUID) { id =>
      get {
        println(s"Receiving WindTurbineData form: $id")
        handleWebSocketMessages(measurementsWebSocket)
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

}



