package parts_event_source

import java.util.Date

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

import scala.collection.immutable

object PersistentActors extends App {

  /*
    Scenario Accountant keep track of invoices
   */

  // COMMAND
  case class Invoice(recipient: String, date: Date, amount: Int)
  case class InvoiceBulk(invoices: List[Invoice])
  // EVENT
  case class InvoiceRecorded(id: Int, recipient: String, date: Date, amount: Int)

  class Accountant extends PersistentActor with ActorLogging {

    var latestInvoiceId = 0
    var totalAmount = 0

    override def persistenceId: String = "simple-accountant"

    /*
      the normal receive method
     */
    override def receiveCommand: Receive = {
      case Invoice(recipient, date, amount) =>
        log.info(s"Received invoice for amount: $amount")
        // SAFE to access mutable state in persistent actor
        persist(InvoiceRecorded(latestInvoiceId, recipient, date, amount))
          /* time gap: all messages are stashed */
        { e =>
          latestInvoiceId += 1
          totalAmount += amount
          // Correctly identify the sender of COMMAND
          sender() ! "PersistenceACK"
          log.info(s"Persisted $e as ${e.id}, for total amount $totalAmount")
        }
      case InvoiceBulk(invoices) =>
        val invoiceIds = latestInvoiceId  to (latestInvoiceId + invoices.size)
        val events = invoices.zip(invoiceIds).map {pair =>
          val id = pair._2
          val invoice = pair._1

          InvoiceRecorded(id, invoice.recipient, invoice.date, invoice.amount)
        }
        persistAll(events) { e =>
          latestInvoiceId += 1
          totalAmount += e.amount
          log.info(s"Persisted single $e as ${e.id}, for total amount $totalAmount")
        }

      case "print" =>
        log.info(s"Latest invoice id $latestInvoiceId")
    }

    /*
      handler that will be called when recover
     */
    override def receiveRecover: Receive = {
      case InvoiceRecorded(id, _, _, amount) =>
        latestInvoiceId = id
        totalAmount += amount
        log.info(s"Recovered #$id for invoice amount: $amount total amount: $totalAmount")
    }

    /*
      This method is called if persistent failed
      The actor will be STOPPED

      Best practice: start the actor in a while
      Use backoff supervisor
     */
    override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
      log.error(s"Fail to persist $event because of $cause")
      super.onPersistFailure(cause, event, seqNr)
    }

    /*
      Called if JOURNAL fails to persist the event
      The actor is RESUMED
     */
    override protected def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
      log.error(s"Persist reject for $event because of $cause")
      super.onPersistRejected(cause, event, seqNr)
    }
  }

  val system = ActorSystem("PersistentActors")

  val accountant = system.actorOf(Props[Accountant])

  /*for (i <- 1 to 10) {
    accountant ! Invoice("The Sofa company", new Date,  i * 1000)
  }*/

  val newInvoices: immutable.Seq[Invoice] = for (i <- 1 to 10) yield Invoice("The awesome chairs", new Date, i * 2000)

  accountant ! InvoiceBulk(newInvoices.toList)


  accountant ! "print"

  /*
    Persistence Failures
   */

  /*
    Persist multiple events

    persistAll
   */

  /*
    NEVER EVER CALL persist or persistAll IN FUTURES
   */
}
