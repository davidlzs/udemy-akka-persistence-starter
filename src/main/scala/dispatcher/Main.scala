package dispatcher

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, DispatcherSelector}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * You can run the Main and use jconsole or jvisulavm to watch the threads
  */
object Main extends App {
  var config = ConfigFactory.parseString(
    """
      |my-dispatcher-for-blocking {
      |  type = Dispatcher
      |  executor = "thread-pool-executor"
      |  thread-pool-executor {
      |    fixed-pool-size = 16
      |  }
      |  throughput = 1
      |}
      |""".stripMargin).withFallback(ConfigFactory.load())

  var root = Behaviors.setup[Nothing](context => {
    val print = Behaviors.receive[Integer] { (context, msg) => context.log.info(msg.toString)
      Behaviors.same
    }

    val blocking = Behaviors.receive[Integer] {(context, msg) => {
      context.log.info(s"Before $msg")
      // case 1: blocking the default dispatcher threads by using the default dispatcher for blocking operations.
      // solution: spawn the blocking actor with a dedicated dispatcher: my-dispatcher-for-blocking
//      Thread.sleep(5000)

      // case 2: blocking (slow) future also can take all the default dispatcher's threads
      // solution: use dedicated dispatcher: my-dispatcher-for-blocking for blocking future

      //      implicit val executionContext: ExecutionContext = context.executionContext
      implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup(DispatcherSelector.fromConfig("my-dispatcher-for-blocking"))
      Future {
        Thread.sleep(5000) //block for 5 seconds
        context.log.info(s"Blocking future finished")
      }

      context.log.info(s"After $msg")
      Behaviors.same
    }}

    var actor:ActorRef[Integer] = null
    for (i <- 1 to 100) {
      context.spawn(print, s"print-$i") ! i
      actor = context.spawn(blocking, s"blocking-$i"
//        , DispatcherSelector.fromConfig("my-dispatcher-for-blocking")
      )
      actor ! i
    }

    actor !  200

    Behaviors.empty
  })
  ActorSystem[Nothing](root, name = "Main", config)

}
