package scala_beginner.part1_basics

object Expressions extends App {

  val x = 1 + 2
  println(x)

  println(3 + 4 + 6)

  // + - * / & | ^ >> << >>> (right shift with zero extension)

  println ( 1 == x)
  // == != > >= < <=

  println(!(1 == x))
  // ! && ||

  var counter = 2
  counter += 1

  // Instruction(DO) vs Expression(VALUE)

  // if expression

  val aCondition = true

  val conditionedValue = if (aCondition) 6 else 10
  println(conditionedValue)


  // Avoid loops in scala
  var i = 0
  while (i < 10) {
    println(i)
    i += 1
  }

  // everything is an expression

  val aWeiredValue = (i = 3) //Unit === void
  println(aWeiredValue)

  // side effects: println, while, reassigning

  // code blocks

  val codeBlock = {
    val x = 2
    val y = 3
    if (x > y) "Hello" else "Bye"
  }

  // 1. difference between "hello world" : String and println("hello world") : Unit
  // 2. what are the values for
  val someValue = {
    2 > 3
  } // false

  val someOtherValue = {
    if (someValue) 239 else 456
    42
  } // 42

}
