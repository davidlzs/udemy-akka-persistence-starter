package maximizing_throughput_and_scalability

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object SingleStreamMapAsyncParallelismExample extends App {
  implicit val system = ActorSystem("singleStreamSystem")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  // Simulate a CPU - intensive workload that takes ~ 10 milliseconds
  def spin(value: Int): Int = {
    val start = System.currentTimeMillis()
    while((System.currentTimeMillis() - start) < 10) {}
    value
  }

  val start = System.currentTimeMillis();
  Source(1 to 1000)
    // mapAsync executes a Future and emit the completed results to the downstream
    .mapAsync(2)(x => Future(spin(x)))
    .mapAsync(2)(x => Future(spin(x)))
    .runWith(Sink.ignore)
    .onComplete {
      case Success(done) => println(s"${done} in milliseconds:  ${System.currentTimeMillis() - start}")
      case Failure(exception) => println(exception)
    }
}
