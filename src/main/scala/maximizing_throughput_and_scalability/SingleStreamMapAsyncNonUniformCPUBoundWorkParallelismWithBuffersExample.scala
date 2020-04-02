package maximizing_throughput_and_scalability

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

/**
  * insert buffer stage to improve performance for NON-UNIFORMED work load
  * option 1: use async
  * option2: use buffer
  *
  * WARNING: for UNIFORMED work load, insert buffer does not improve performance, it only consume resources (memory)
  */
object SingleStreamMapAsyncNonUniformCPUBoundWorkParallelismWithBuffersExample extends App {
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
    .mapAsync(4)(randomSpin)
    // Option1: Use async to add async boundary, will add default buffer (size:16) to keep the downstream saturated
//    .async
    // Option2: add buffer stage
    .buffer(16, OverflowStrategy.backpressure)
    .mapAsync(4)(randomSpin)
//    .async
    .buffer(16, OverflowStrategy.backpressure)
    .mapAsync(4)(randomSpin)
//    .async
    .buffer(16, OverflowStrategy.backpressure)
    .mapAsync(4)(randomSpin)
//    .async
    .buffer(16, OverflowStrategy.backpressure)
    .runWith(Sink.ignore)
    .onComplete {
      case Success(done) => println(s"${done} in milliseconds:  ${System.currentTimeMillis() - start}")
      case Failure(exception) => println(exception)
    }
}
