package part1_recap

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, PoisonPill, Props, Stash, SupervisorStrategy}
import akka.util.Timeout

import scala.concurrent.{Await, Future}

object AkkaRecap extends App {

  class SimpleActor extends Actor with ActorLogging with Stash {
    override def receive: Receive = {

      case "change handler now" => {
        unstashAll()
        context.become(anotherHandler)
      }
      case message => stash()
      case "change" => context.become(anotherHandler)
      case message => log.info(s"Received message $message")
    }

    def anotherHandler:Receive = {
      case "create child" =>
        val childActor = context.actorOf(Props[SimpleActor], "mychild")
        childActor ! "to child"
        childActor ! "change handler now"
      case "asking question" => sender() ! "given answer"
      case message => log.info(s"I am in another handler to handle the message $message $self")
    }

    override def preStart(): Unit = log.info(s"I am starting $self")

    override def postStop(): Unit = log.info(s"I was killed $self")

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: RuntimeException => Restart
      case _ => Stop

    }
  }

  // actor encapsulation
  // #1 create actor via system.acgtorOf
  val system = ActorSystem("AkkaRecap")
  val simpleActor: ActorRef = system.actorOf(Props[SimpleActor], "simpleActor")
  // #2 sending message
  simpleActor ! "Bob" // async message to the actor
  /*
    - messages are sending async
    - many actors share a few dozen threads
    - each message a handled in single thread illusion
    - no need for lock
   */

  // change actor behaviour - become
  // stashing
  simpleActor ! "First message stashed"
  simpleActor ! "Seoncd message stashed"
  simpleActor ! "change handler now"

  // spawning actors
  simpleActor ! "create child"

  // guardians /system, /user, /=root guardian
  // actor lifecycle: started, stopped, suspended, resumed, restarted

  // stopping - context.stop
//  simpleActor ! PoisonPill

  // logging

  // supervision

  // configure akka infrastructure: dispatchers, routers, mailboxes

  // schedulers
  import scala.concurrent.duration._
  import system.dispatcher
  system.scheduler.scheduleOnce(2 seconds) {
    simpleActor ! "delayed message"
  }

  // Akka patterns including FSM and ask pattern
  import akka.pattern.ask
  implicit val timeout = Timeout(3 seconds)
  private val future: Future[Any] = simpleActor ? "asking question"

  // pipe pattern
  private val anotherSimpleActor: ActorRef = system.actorOf(Props[SimpleActor], "anotherSimpleActor")
  import akka.pattern.pipe
  future.mapTo[String].pipeTo(anotherSimpleActor)
  anotherSimpleActor ! "change handler now"

}