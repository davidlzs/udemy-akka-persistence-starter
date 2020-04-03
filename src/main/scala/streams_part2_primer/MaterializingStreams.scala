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

  graph.run().onComplete {
    case Success(done) => println(s"$done")
    case Failure(exception) => println(s"$exception")
  }
}

