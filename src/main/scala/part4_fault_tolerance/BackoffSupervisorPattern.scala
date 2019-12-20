package part4_fault_tolerance

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{Backoff, BackoffSupervisor}
import scala.concurrent.duration._

import scala.io.Source

object BackoffSupervisorPattern extends App {
  case object ReadFile
  class FileBasedPersistentActor extends Actor with ActorLogging {
    var dataSource: Source = null

    override def preStart(): Unit = log.info("Persistent actor starting")

    override def postStop(): Unit = log.warning("Persistent actor stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.warning("Persistent actor retarting")

    override def receive: Receive = {
      case ReadFile =>
        if (dataSource == null)
          dataSource = Source.fromFile(new File("src/main/resources/testfiles/important_data.txt"))
        log.info("I've just read some important data " + dataSource.getLines().toList)
    }
  }

  val system = ActorSystem("backoffsuppervisor")

//  val simpleActor: ActorRef = system.actorOf(Props[FileBasedPersistentActor], "simpleActor")
//  simpleActor ! ReadFile

  val simpleSupervisorProps = BackoffSupervisor.props(
      Backoff.onFailure(
        Props[FileBasedPersistentActor],
  "simpleBackoffActor",
  3 seconds, //3s, 6s, 12s
  30 seconds,
  0.2
    )
  )

  val simpleSupervisor: ActorRef = system.actorOf(simpleSupervisorProps, "simpleSupervisor")
  simpleSupervisor ! ReadFile
}
