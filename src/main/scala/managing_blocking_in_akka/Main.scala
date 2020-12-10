package managing_blocking_in_akka

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Youtube video Managing Blocking in Akka
  * https://www.youtube.com/watch?v=xBVKJUzYD_Q
  *
  * Creating dedicated blocking dispatcher for the blocking actors
  */
object Main extends App {
  val root = Behaviors.setup[Nothing] { context =>
    val print = Behaviors.receive[Integer] { (ctx, msg) =>
      ctx.log.info(msg.toString)
      Behaviors.same
    }

    val blocking = Behaviors.receive[Integer] { (ctx, msg) =>
      ctx.log.info(s"Before $msg")
      Thread.sleep(5000)
      ctx.log.info(s"After $msg")
      Behaviors.same
    }

    for (i <- 1 to 50) {
      context.spawn(print, s"print-$i") ! i
      context.spawn(blocking, s"blocking-$i",
        DispatcherSelector.fromConfig("my-dispatcher-for-blocking")
      ) ! i

    }
    Behaviors.empty
  }

  private val config: Config = ConfigFactory.load("blocking/blocking_dispatcher.conf")
  ActorSystem[Nothing](root, "Main", config)
}
