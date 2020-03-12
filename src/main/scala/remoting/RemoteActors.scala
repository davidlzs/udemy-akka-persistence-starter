package remoting

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorSystem, DeadLetter, Identify, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.util.{Failure, Success}
object RemoteActors extends App {
  val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("remoting/remoteActors.conf"))
  val localSimpleActor = localSystem.actorOf(Props[SimpleActor], "localSimpleActor")
  localSimpleActor ! "hello, local actor!"

  // send a message to remote actor
  // method 1 actor selection
  val remoteActorSelection = localSystem.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
  remoteActorSelection ! "hello from local JVM"

  // method 2 resolve actor selection to an actor ref
  import localSystem.dispatcher
  implicit val timeout = Timeout(3 seconds)
  val remoteActorRefFuture = remoteActorSelection.resolveOne()
  remoteActorRefFuture.onComplete {
    case Success(actorRef) => actorRef ! "hello from local JVM via actorRef"
    case Failure(exception) => println(s"I failed to resolve the actorRef becaus: $exception")
  }

  // method 3 actor identification via message
  /*
    - actor resolver will ask for an actor selection from the local actor system
    - actor resolver will send an Identity(42) to the actor selection
    - the remote actor will AUTOMATICALLY respond with ActorIdentity(42, actorRef)
    - the actor resolver is free to use the remote actorRef
   */
  class ActorResolver extends Actor with ActorLogging {
    override def preStart(): Unit = {
      val selection = context.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
      selection ! Identify(42)
    }

    override def receive: Receive = {
      case ActorIdentity(42, Some(actorRef)) =>
        actorRef ! "Thank you for identifying yourself!"
    }
  }

  localSystem.actorOf(Props[ActorResolver], "localActorResolver")
}

object RemoteActors_Remote extends App {
  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("remoting/remoteActors.conf").getConfig("remoteSystem"))
  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor], "remoteSimpleActor")
  val deadLetterListener = remoteSystem.actorOf(Props[DeadLetterListener], "remoteSystemDeadLetterListener")
  remoteSystem.eventStream.subscribe(deadLetterListener, classOf[DeadLetter])
  remoteSimpleActor ! "hello, remote actor!"
}
