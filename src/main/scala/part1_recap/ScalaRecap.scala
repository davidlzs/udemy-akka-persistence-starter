package part1_recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object ScalaRecap extends App {
  val aCondition : Boolean = false
  def myFucntion(x: Int) = {
    if (x > 4) 42 else 65
  }

  println(myFucntion(0))

  // OO
  class Animal
  trait Carnivore {
    def eat(a: Animal):Unit
  }

  object Carnivore

  // generics
  abstract class MyList[+A]

  // method notations
  1 + 2 // infix notation
  1.+(2)

  // FP
  val anIncrementer: Int => Int = (x : Int) => x + 1

  println(anIncrementer.apply(10))

  println(List(1,2,3).map(anIncrementer)) // HOF - higher order function: filter, map, flatMap

  //for-comprehension

  //Monads: Option, Try

  // Pattern match
  val unknown = 2
  val order = unknown match {
    case 1 => "First"
    case 2 => "Second"
    case _ => "Unknown"
  }

  try {
    throw new RuntimeException
  } catch {
    case e: Exception => println("Caught one exception")
  }

  // Scala Advanced Features

  // multithreading
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    // long computation running on t
    42
  }

  // map, flatMap, filter, recover and recoverWith
  future.onComplete {
    case Success(value) => println(s"I found some meaning $value")
    case Failure(exception) => println(s"I found exception $exception")
  }

  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case _ => 999
  }

  println(partialFunction.apply(1))

  // type alias
  type AkkaReceive = PartialFunction[Any, Unit]
  def receive : AkkaReceive = {
    case 1 => println("received one")
    case _ => println("confusion...")
  }

  receive("Hello")

  // Implicits

  def setTimeout(f: () => Unit) (implicit timeout: Int) = f()

  implicit val timeout1 = 5

  setTimeout(() => println(s"long running may be timeout"))

  // conversions

  // 1) implicit method

  case class Person(name: String) {
    def greet: String = s"Say hi to: $name"
  }

  implicit def fromStringToPerson(name: String) = Person(name)

  println("David".greet)

  println(fromStringToPerson("Susie").greet)

  // 2) implicit class
  implicit class Dog(name: String) {
    def bark = println(s"$name bark!")
  }

  "Donald".bark
  new Dog("Lassie").bark

  // implicit organization
  // local scope
  implicit val numberOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  println(List(1,2,3).sorted)

  // import scope - refer to the future executorcontext

  // companion object of the types involved in the call

  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) > 0)
  }

  println(List(Person("David"), Person("Susie")).sorted) //(Person.personOrdering)
}
