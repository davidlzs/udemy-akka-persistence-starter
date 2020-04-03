package streams_part2_primer

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MaterializingStreams extends App {
  implicit val system = ActorSystem("materializingSystem")
  implicit val materializer = ActorMaterializer()
  import scala.concurrent.ExecutionContext.Implicits.global

  val simpleGraph: RunnableGraph[NotUsed] = Source(1 to 10).to(Sink.foreach(println))

  //  val simpleMaterializedValue: NotUsed = simpleGraph.run()

  val source = Source(1 to 10)
  val sink: Sink[Int, Future[Int]] = Sink.reduce[Int]((a, b) => a + b)
  //  val someFuture: Future[Int] = source.runWith(sink) // use runWith for the sink Materialized value
//  someFuture.onComplete {
//    case Success(value) => println(value)
//    case Failure(exception) => println(exception)
//  }

  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x+ 1)
  val simpleSink = Sink.foreach(println)
  val graph: RunnableGraph[Future[Done]] = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)

/*
  graph.run().onComplete {
    case Success(done) => println(s"$done")
    case Failure(exception) => println(s"$exception")
  }
*/

  // sugars
/*
  val sum: Future[Int] = Source(1 to 10).runReduce[Int](_ + _) //Source(1 to 10).runWith(Sink.reduce[Int]((a, b) => a + b))
  sum.onComplete{
    case Success(value) => println(s"sum is : $value")
    case Failure(exception) => println(s"failed to sum: $exception")
  }
*/

  // backward
  Sink.foreach(println).runWith(Source.single(42))

  // both ways
  Flow[Int].map(x => x * 2).runWith(simpleSource, simpleSink)

  /**
    * - return the last element of a source (Sink.last)
    * - compute the total word count out a stream of sentences
    *   - map, fold, reduce
    */

  val elementSource = Source(1 to 10)
  val lastSink = Sink.last[Int]
  //option 1:
//   elementSource.toMat(lastSink)(Keep.right).run()
  //option 2:
  elementSource.runWith(lastSink)
    .onComplete{
    case Success(value) => println(s"last value is: $value")
    case Failure(exception) => println(s"failed to get last element: $exception")
  }

  val sentenceSource = Source(List("the first sentence", "another a sentence", "yet another sentence"))
  val toWordCount = Flow[String].map(x => x.split(" ").length)
  val countSink = Sink.reduce[Int]((a, b) => a + b)

  //option 1
  sentenceSource.viaMat(toWordCount)(Keep.right).toMat(countSink)(Keep.right)
    .run()
    .onComplete{
      case Success(value) => println(s"total words is is: $value")
      case Failure(exception) => println(s"failed to get total words: $exception")
    }

}

