package streams_part2_primer

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}

import scala.concurrent.Future

object FirstPrinciples extends App {
  implicit val system = ActorSystem("firstprinciples")
  implicit val materializer = ActorMaterializer()

  // source
  val source = Source[Int](1 to 10)
  // sink
  val sink = Sink.foreach[Int](println)

  val graph: RunnableGraph[NotUsed] = source.to(sink)
//  graph.run()

  // flows
  val flow = Flow[Int].map(x => x + 1)
  val sourceWithFlow: Source[Int, NotUsed] = source.via(flow)
  val flowWithSink = flow.to(sink)

//  sourceWithFlow.to(sink).run()
//  source.to(flowWithSink).run()
//  source.via(flow).to(sink).run()

  //nulls are not allowed, use option instead
  val illegalSource = Source.single[Option[String]](Option.empty[String])
//  illegalSource.to(Sink.foreach(println)).run()

  //various kinds of source
  val finiteSource = Source.single(10)
  val anotherFiniteSource = Source(List(1, 2, 3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(Stream.from(1)) // do not confuse a Akka stream with a collection stream
//  infiniteSource.runForeach(println)

  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.fromFuture(Future(42))

  // sinks
  val theMostBoringSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int]
  val foldSink = Sink.fold[Int, Int](0)( _ + _) // (a, b) => a + b

  //flows
  val mapFlow = Flow[Int].map(x => x * 2)
  val takeFlow = Flow[Int].take(4)
  val dropFlow = Flow[Int].drop(5)

  // do not have flatmap
  // source -> flow -> flow ... ->flow -> sink
  val doubleFlowGraph = source.via(mapFlow).via(takeFlow).to(sink)
  doubleFlowGraph.run()

  // syntactic sugar
  val mapSource = Source(1 to 10).map(x => x * 2) // Source(1 to 10).via(Flow[Int].map(x => x *2)
//  mapSource.runForeach(println) // mapSource.to(Sink.foreach[Int](println)).run()

  // operators = components
  /**
    * Exercise, take the names of persons, you take the first 2 name with length > 5 characters
    */

  val nameSource = Source(List("David", "Abcded", "Dudesss", "Kit"))
//  nameSource.filter(n => n.length > 5).take(2).runForeach(println)
  val longNameFlow = Flow[String].filter(n => n.length > 5)
  val limitFlow = Flow[String].take(2)
  nameSource.via(longNameFlow).via(limitFlow).to(Sink.foreach(println)).run()
}
