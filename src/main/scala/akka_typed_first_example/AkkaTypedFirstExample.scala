package akka_typed_first_example

import akka.actor.typed.ActorSystem

object AkkaTypedFirstExample extends App {

  val system: ActorSystem[HelloWorldMain.SayHello] =
    ActorSystem(HelloWorldMain(), "hello")

  system ! HelloWorldMain.SayHello("World")
  system ! HelloWorldMain.SayHello("Akka")

}
