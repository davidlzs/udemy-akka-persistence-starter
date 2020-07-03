package part4_fault_tolerance

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Supervision.{Resume, Stop}
import akka.stream.scaladsl.{RestartSource, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer}

import scala.concurrent.duration._
import scala.util.Random

object FaultTolerance extends App {
  implicit val system = ActorSystem("FaultTolerance")
  implicit val materializer = ActorMaterializer()


  // 1. logging
  val faultSource = Source(1 to 10).map(e => if (e == 6) throw new RuntimeException else e) // Termintae the stream
  faultSource.log("trackingElements").to(Sink.ignore)
  //    .run()
  // 2. gracefully terminating a stream
  faultSource.recover {
    case _: RuntimeException => Int.MinValue
  }.log("gracefulSource").to(Sink.ignore)
  //    .run()
  // 3. recover with another stream
  faultSource.recoverWithRetries(3, {
    case _: RuntimeException => Source(90 to 99)
  }).log("recoverWithReties")
    .to(Sink.ignore)
  //    .run()

  // 4. backoff supervision
  val restartSource = RestartSource.onFailuresWithBackoff(
    1 second,
    30 seconds,
    0.2)(() => {
    val randomNumber = new Random().nextInt(20)
    Source(1 to 10).map(e => if (e == randomNumber) throw new RuntimeException else e)
  })

  restartSource
    .log("restartBackoff")
    .to(Sink.ignore)
  //    .run()

  // 5. supervision strategy
  val numbers = Source(1 to 20).map(n => if (n == 13) throw new RuntimeException("bad luck") else n)
  val supervisedNumbers: Source[Int, NotUsed] = numbers.withAttributes(ActorAttributes.supervisionStrategy {
    /*
      Resume = skip the faulty element
      Stop = stop the stream
      Restart = resume + clears internal state
     */
    case _: RuntimeException => Resume
    case _ => Stop
  })

  supervisedNumbers
    .log("supervisionStrategy")
    .to(Sink.ignore)
    .run()
}