package scala_beginner.part1_basics

import scala.annotation.tailrec

object Recursion extends App {
  def factorial(n: Int) : Int = {
    if (n == 1) 1
    else  {
      println(s"Computing factorial of $n, first we need to compute factorial of ${n-1}")
      val result = n * factorial(n - 1)
      println(s"Computed factorial of $n")
      result
    }
  }

  println(factorial(5))


  def anotherFactorial(n : BigInt) : BigInt = {
    @tailrec
    def factorialHelper(n: BigInt, accumulator: BigInt) : BigInt = {
      if (n == 1) accumulator
      else factorialHelper(n - 1, n * accumulator) //TAIL RECURSION = use recursive as last THE expression
    }
    factorialHelper(n, 1)
  }

  println(anotherFactorial(5000))

  // WHEN you need loop, use tail recursion
  /*
     1. Concatenate a string with n times
   */
  def greeting(name: String, n : Int): String = {
    @tailrec
    def greetingHelper(name: String, n: Int, accumulator: String ) : String = {
      if (n == 0) accumulator
      else greetingHelper(name, n - 1, accumulator + name)
    }

    greetingHelper(name, n, "")
  }

  println(greeting("hello", 10))

  /*
     2. isPrimitive
   */
  def isPrimitive(n: Int): Boolean = {
    @tailrec
    def isPrimitiveHelper(n: Int, t: Int, accumulator: Boolean) : Boolean = {
      if (t == 1) accumulator
      else isPrimitiveHelper(n, t - 1, n % t != 0 && accumulator)
    }
    isPrimitiveHelper(n, n / 2, true)
  }

  println(isPrimitive(1009))
  /*
     3. Fibonacci
   */
  def fibonacci(n : Int) : Int = {
    def fibonacciHelper(n : Int, accumulator1: Int, accumulator2: Int): Int = {
      if (n <= 2) accumulator1
      else fibonacciHelper(n - 1, accumulator1 + accumulator2, accumulator1 )
    }
    fibonacciHelper(n, 1, 1)
  }

  println(fibonacci(8))
}
