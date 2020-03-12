package remoting

import akka.actor.{Actor, ActorLogging, DeadLetter}

class DeadLetterListener extends Actor with ActorLogging {
  override def receive: Receive = {
    case d: DeadLetter => log.info(s"Dead letter received: $d from ${sender()}")
  }
}