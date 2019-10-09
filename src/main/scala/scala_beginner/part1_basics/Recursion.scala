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
    def concatenateTailrec(name: String, n: Int, accumulator: String ) : String = {
      if (n == 0) accumulator
      else concatenateTailrec(name, n - 1, accumulator + name)
    }

    concatenateTailrec(name, n, "")
  }

  println(greeting("hello", 10))

  /*
     2. isPrimitive
   */
  def isPrimitive(n: Int): Boolean = {
    @tailrec
    def isPrimeTailrec(t: Int, isStillPrime: Boolean) : Boolean = {
      if (!isStillPrime) false
      else if (t == 1) true
      else isPrimeTailrec(t - 1, n % t != 0 && isStillPrime)
    }
    isPrimeTailrec(n / 2, true)
  }

  println(isPrimitive(1009))
  /*
     3. Fibonacci
   */
  def fibonacci(n : Int) : Int = {
    @tailrec
    def fibonacciTailrec(i : Int, last: Int, nextLast: Int): Int = {
      if (i <= 2) last
      else fibonacciTailrec(i - 1, last + nextLast, last )
    }
    fibonacciTailrec(n, 1, 1)
  }

  println(fibonacci(8))

  def fibonacci2(n: Int) : Int = {
    def fibonacci2Tailrec(i : Int, last: Int, nextLast: Int) : Int = {
      if (i >= n) last
      else fibonacci2Tailrec(i + 1, last + nextLast, last)
    }

    if (n <= 2) 1
    else fibonacci2Tailrec(2, 1, 1)
  }

  println(fibonacci2(1))
  println(fibonacci2(2))
  println(fibonacci2(8))
}
