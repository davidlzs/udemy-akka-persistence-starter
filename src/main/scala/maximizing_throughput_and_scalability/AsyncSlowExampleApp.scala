package maximizing_throughput_and_scalability

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.util.{Failure, Success}

case class One(value: Int)
case class Two(value: Int)
case class Three(value: Int)


object AsyncSlowExampleApp extends App {
  implicit val system = ActorSystem("testSystem")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val start = System.currentTimeMillis()

  Source(1 to 1000000)
    .async
    .map(x => One(x))
    .async
    .map(x => Two(x.value))
    .async
    .map(x => Three(x.value))
    .runWith(Sink.ignore)
    .onComplete {
      case Success(done) => println(s"${done} in ${System.currentTimeMillis() - start} milliseconds")
      case Failure(exception) => println(exception)
    }
}
