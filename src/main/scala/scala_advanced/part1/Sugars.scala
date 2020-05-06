package scala_advanced.part1

import scala.util.Try

object Sugars extends App {
  // syntax sugar #1: single argument method
  def singleArgMethod(arg: Int) = s"$arg little ducks..."

  val description = singleArgMethod {
    42
  }
  println(description)

  var aTryInstance = Try {
    throw new RuntimeException("from try")
  }

  private val list: List[Int] = List(1, 2, 3).map{x =>
    println(x)
    x + 1
  }
  print (list)

  // syntax sugar #2: single abstract method - override can use lambda, like java's default interface method

  trait Action {
    def act(x: Int):Int
  }

  private val anActionInstance: Action = new Action {
    override def act(x: Int): Int = x + 1
  }

  val aFunkyInstance: Action = (x: Int) => x + 1

  println(s"${anActionInstance.act(10)} is ${aFunkyInstance.act(10)}")


  // example: thread
  val aThread: Thread = new Thread(new Runnable {
    override def run(): Unit = {
      println(s"in thread: ${Thread.currentThread().getName}")
    }
  })

  val anotherThread: Thread = new Thread(() => println(s"in another thread: ${Thread.currentThread().getName}"))

  aThread.start()
  anotherThread.start()
  println(s"This is in thread: ${Thread.currentThread().getName}")

  abstract class AbstractType {
    def implemented: Int = 43
    def f(x: Int):Int
  }

  val anAbstractInstance: AbstractType = (x: Int) => 45
  println(s"${anAbstractInstance.implemented} vs ${anAbstractInstance.f(0)}")

  // syntax sugar #3: :: and #:: methods are special
  val prependedList = 2 :: List(3, 4) // List(3,4).::(2)

  // scala spec: last char of the operator decides the associativity of method ":" -> right associated

  val anotherPrependedList = 1 :: 2 :: 3 :: List(4, 5)
  val yetAnotherPrependedList = List(4, 5).::(3).::(2).::(1)
  println(s"$anotherPrependedList vs $yetAnotherPrependedList")

}
