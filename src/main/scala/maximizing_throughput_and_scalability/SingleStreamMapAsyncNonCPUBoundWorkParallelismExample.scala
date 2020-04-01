package maximizing_throughput_and_scalability

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success}
import scala.concurrent.duration._

object SingleStreamMapAsyncNonCPUBoundWorkParallelismExample extends App {
  implicit val system = ActorSystem("singleStreamSystem")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  val random = new Random()

  def nonBlocking(value: Int) : Future[Int] = {
    val promise = Promise[Int]

    // Simulate a non-blocking network call to another service
    val t = FiniteDuration(random.nextInt(101), MILLISECONDS)
    system.scheduler.scheduleOnce(t) {
      promise.success(value)
    }

    promise.future
  }

  val start = System.currentTimeMillis();
  Source(1 to 1000)
    // mapAsync executes a Future and emit the completed results to the downstream
    .mapAsync(1000)(x => nonBlocking(x))
    .mapAsync(1000)(x => nonBlocking(x))
    .runWith(Sink.ignore)
    .onComplete {
      case Success(done) => println(s"${done} in milliseconds:  ${System.currentTimeMillis() - start}")
      case Failure(exception) => println(exception)
    }
}
