package akka_streams_error_handling

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.{ActorAttributes, ActorMaterializer, Attributes, Supervision}
import akka.stream.scaladsl.{Sink, Source}

import scala.util.{Failure, Success}


object SimpleErrorHandling extends App {
  implicit val system = ActorSystem("error_handling_system")
  implicit val materializer = ActorMaterializer()
  implicit val defaultDispatcher = system.dispatcher

  val logger = Logging.getLogger(system, this)
  val decider: Supervision.Decider = {
    case e: Exception =>
      logger.error("error: {}", e)
      Supervision.Resume
    case _                      => Supervision.Stop
  }

  val PlanB = Source(List("four", "five", "six"))

  Source(-5 to 5)
    .map( n =>
      if (n != 0) n
      else throw new RuntimeException("n is zero")
    )
//    .map(1 / _  ) //throwing ArithmeticException: / by zero
//    .map(n =>
//      if (n == 4) throw new RuntimeException("Boom!")
//      else n
//    )
//    .divertTo(Sink.ignore, n => n == 4 )
//    .log("result")

    // Recover
//    .recover {
//      case e: ArithmeticException => " error: " + e.getMessage
//    }
    // Recover with retries
//    .recoverWithRetries(attempts = 1 )
//    .log("error logging")
//    .withAttributes(Attributes
//      .logLevels(onElement = Logging.DebugLevel, onFinish = Logging.InfoLevel, onFailure = Logging.WarningLevel))
    .withAttributes(ActorAttributes.supervisionStrategy(decider))
//    .runWith(Sink.ignore)
    .runForeach(println)
    .onComplete {
        case Success(_) =>
          println("Done")
        case Failure(e) =>
          println(s"Failed with $e")
    }

}
