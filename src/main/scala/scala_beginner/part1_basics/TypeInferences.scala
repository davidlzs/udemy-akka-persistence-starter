package scala_beginner.part1_basics

object TypeInferences extends App {
  val name = "David"

  val x = 1
  val y = 1 + "item"

  def succ(x: Int): Int = x  + 1

  def factorial(n: Int) : Int = {
    if (n == 1) 1
    else n * factorial(n - 1)
  }

}
