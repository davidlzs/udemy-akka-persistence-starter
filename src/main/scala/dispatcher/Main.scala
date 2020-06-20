package dispatcher

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory

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

  var root = Behaviors.setup[Nothing](ctx => {
    val print = Behaviors.receive[Integer] { (ctx, msg) => ctx.log.info(msg.toString)
      Behaviors.same
    }

    val blocking = Behaviors.receive[Integer] {(ctx, msg) => {
      ctx.log.info(s"Before $msg")
      Thread.sleep(5000)
      ctx.log.info(s"After $msg")
      Behaviors.same
    }}

    for (i <- 1 to 10000) {
      ctx.spawn(print, s"print-$i") ! i
      ctx.spawn(blocking, s"blocking-$i", DispatcherSelector.fromConfig("my-dispatcher-for-blocking")) ! i
    }
    Behaviors.empty
  })
  ActorSystem[Nothing](root, name = "Main", config)

}
