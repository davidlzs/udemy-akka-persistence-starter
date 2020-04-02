package maximizing_throughput_and_scalability

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success}

object SingleStreamMapAsyncUnorderedNonUniformCPUBoundWorkParallelismExample extends App {
  implicit val system = ActorSystem("singleStreamSystem")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  val random = Random

  // Simulate a non-uniform CPU - intensive workload that takes ~ 10 milliseconds
  def randomSpin(value: Int): Future[Int] = Future {
    val max = random.nextInt(101)
    val start = System.currentTimeMillis()
    while((System.currentTimeMillis() - start) < max) {}
    value
  }

  val start = System.currentTimeMillis();
  Source(1 to 1000)
    // mapAsync executes a Future and emit the completed results to the downstream
    .mapAsyncUnordered(8)(randomSpin)
    .runWith(Sink.ignore)
    .onComplete {
      case Success(done) => println(s"${done} in milliseconds:  ${System.currentTimeMillis() - start}")
      case Failure(exception) => println(exception)
    }
}
