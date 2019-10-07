package scala_beginner.part1_basics

object Functions extends App {
  def aFunction(a : String, b: Int) : String = {
    a + " " + b
  }

  println(aFunction("Hello", 6))

  def aParameterlessFunction() : Int = 42

  println(aParameterlessFunction())
  println(aParameterlessFunction)

  def aRepeatedFunction(aString: String, times: Int): String = {
    if (times == 1) aString
    else aString + aRepeatedFunction(aString, times - 1) // recursive
  }

  println(aRepeatedFunction("Hello", 5))

  // WHEN needing loops using recursive

  def aFunctionWithSideEffects(a : String) : Unit = println(a)

  aFunctionWithSideEffects("hello you")

  def aBigFunction(n : Int) : Int = {
    def aSmallerFunction(a: Int, b: Int) : Int = a + b
    aSmallerFunction(n, n-1)
  }

  /*
    1. A greeting function name, age
   */
  def greeting(name: String, age: Int) : String = s"Hi, my name is $name and I am $age years old"
  println(greeting("John", 10))

  /*
    2. Factorial (n: Int): Int
   */
  def factorial(n : Int) : Int = {
    if (n == 1) 1
    else n * factorial(n - 1)
  }

  println(factorial(5))
  /*
    3. Fibonacii f(1) = 1, f(2) = 1, f(n) = f(n-1) + f(n-2)
   */
  def fibonacii(n : Int) : Int = {
    if (n == 1 || n == 2) 1
    else fibonacii(n-1) + fibonacii(n-2)
  }
  println(fibonacii(8))
  /*
    4. Test a number is prime or not
   */
  def isPrime(n : Int) : Boolean = {
    def isPrimeUntil(t: Int): Boolean = {
      if (t <= 1) true
      else n % t != 0 && isPrimeUntil(t - 1)
    }

    isPrimeUntil(n / 2)
  }

  println(isPrime(2003))
}
