package part4_practice

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

object PersistenceQueryDemo extends App {
  val system = ActorSystem("PersistenceQueryDemo", ConfigFactory.load().getConfig("persistenceQuery"))

  // read journal
  val readJournal = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  // give me all persistenceIds
  val persistenceIds = readJournal.currentPersistenceIds()
  implicit val materializer = ActorMaterializer()(system)
/*  persistenceIds.runForeach(persistenceId =>
    println(s"Found persistence ID: $persistenceId")
  )*/


  class SimplePersistenceActor extends PersistentActor with ActorLogging {
    var counter = 0;
    override def receiveRecover: Receive = {
      case e => log.info(s"Recovered: $e")
    }
    override def receiveCommand: Receive = {
      case m => persist(s"$m : $counter" ) { e =>
        log.info(s"Persisted: $e")
        counter += 1
      }
    }

    override def persistenceId: String = "persistence-query-id-14"
  }

  val simpleActor = system.actorOf(Props[SimplePersistenceActor], "simplePersistentActor")
  val message  = "hello simple persistence actor - after 5 seconds"
  import system.dispatcher
  system.scheduler.schedule(5 seconds, 5 seconds ) {
    simpleActor ! message
  }

  // events by persistent ID
  val events = readJournal.currentEventsByPersistenceId("persistence-query-id-14", 0, Long.MaxValue)
  events.runForeach { event =>
    println(s"Read event: $event")
  }
}
