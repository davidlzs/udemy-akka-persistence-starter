package scala_beginner.part1_basics

object CBVvsCBN extends App {
  def calledByValue(x : Long) : Unit = {
    println("by value: " + x)
    println("by value: " + x)
  }

  def calledByName(x: => Long) : Unit = {
    println("by name: " + x)
    println("by name: " + x)
  }

  calledByValue(System.nanoTime())
  calledByName(System.nanoTime())


  def infinite(): Int = 1 + infinite()
  def printFirst(x: Int, y: => Int) = println(x)

//  printFirst(infinite(), 34 )
  printFirst(34, infinite()) // called by name will be evaluated lazily, the infinite() is never evaluated

}
