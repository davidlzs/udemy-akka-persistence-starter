package part4_fault_tolerance

import akka.actor
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, ActorSystem, PoisonPill, Props}

import scala.io.StdIn

object StartingStopingActors extends App {
  val system = ActorSystem("StoppingAndStartingActors")

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging {
    override def receive: Receive = withChildren(Map())

    import Parent._
    def withChildren(children: Map[String, ActorRef]) : Receive = {
      case StartChild(name) =>
        log.info(s"Starting child with name: $name")
        context.become(withChildren(children + (name -> context.actorOf(actor.Props[Child], name))))
      case StopChild(name) =>
        log.warning(s"Stopping child with name: $name")
        val childOption = children.get(name)
//        childOption.foreach(e => e ! PoisonPill) // poison pill, immediate killed
        childOption.foreach(e => context.stop(e))  // context.stop, async kill
      case Stop =>
        log.info("Stopping myself")
        context.stop(self)
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString + " : " + self)
    }
  }

  import Parent._

  val parent = system.actorOf(Props[Parent], "parent")
  parent ! StartChild("child1")
  val child1: ActorSelection = system.actorSelection("/user/parent/child1")
  child1 ! "Hello you are child 1"


  parent ! StopChild("child1")
//  for (_ <- 1 to 50) child1 ! "are you still alive?"

  parent ! StartChild("child2")

  val child2: ActorSelection = system.actorSelection("/user/parent/child2")
  child2 ! "Hi child2"
  parent ! Stop

  StdIn.readLine()
  system.terminate()
}
