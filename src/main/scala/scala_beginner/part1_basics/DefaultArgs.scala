package scala_beginner.part1_basics

object DefaultArgs extends App {
  def trFact(n: Int,  acc: Int = 1) : Int = {
    if (n <= 1) acc
    else trFact(n - 1, acc * n)
  }

  val startTime = System.nanoTime()
  println(trFact(5))
  val endTime = System.nanoTime()

  println("elapsed: " + (endTime - startTime))

  def savePicture(format: String= "jpg", width: Int = 1920, height: Int = 1080) : Unit =  println("saving picture")
  savePicture( "png", 800, 600)

  /*
    1. pass leading argument
   */
  savePicture("bmp")

  /*
    2. named parameter
   */
  savePicture(width = 800)

  savePicture(height = 600, width = 1000, format = "bmp")
}
