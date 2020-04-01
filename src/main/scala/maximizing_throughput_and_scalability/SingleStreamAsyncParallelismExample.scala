package maximizing_throughput_and_scalability

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.util.{Failure, Success}

object SingleStreamAsyncParallelismExample extends App {
  implicit val system = ActorSystem("singleStreamSystem")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  // Simulate a CPU - intensive workload that takes ~ 10 milliseconds
  def spin(value: Int): Int = {
    val start = System.currentTimeMillis()
    while((System.currentTimeMillis() - start) < 10) {}
    value
  }

  Source(1 to 1000)
    .map(spin)
    // Add this async to break the stream asynchronous boundary
    .async
    .map(spin)
    .runWith(Sink.ignore)
    .onComplete {
      case Success(done) => println(done)
      case Failure(exception) => println(exception)
    }
}
