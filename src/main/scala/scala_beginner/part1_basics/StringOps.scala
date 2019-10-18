package scala_beginner.part1_basics

object StringOps extends App {
  val str: String = "Hello, I am learning Scala"

  println(str.charAt(2))
  println(str.substring(7, 11))
  println(str.split(" ").toList)
  println(str.startsWith("Hello"))
  println(str.replace("l", "1"))
  println(str.toLowerCase())
  println(str.toUpperCase())
  println(str.length)

  val aNumberString = "25"
  private val number: Int = aNumberString.toInt
  println(number)
  println('a' +: aNumberString)
  println(aNumberString.reverse)
  println(str.take(2))

  //Scala specific - String interpolators
  // s-interpolators
  val name = "david"
  val age = 12
  private val greetings = s"Hello, my name is $name, I am $age years old, I will be turning ${age + 1} years old"

  println(greetings)

  // f- interpolators
  val speed = 1.2f
  private val anotherGreeting = f"$name can eat $speed%2.2f burgers per minute"
  println(anotherGreeting)

  // raw - interpolateors
  private val rawInterpolator = raw"this is a new line \n a new line "

  val escaped = "this is a new line \n a new line"
  println(rawInterpolator)
  println(escaped)
}
