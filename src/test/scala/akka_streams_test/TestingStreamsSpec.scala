package akka_streams_test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TestingStreamsSpec extends TestKit(ActorSystem("TestingAkkaStreams"))
  with WordSpecLike
  with BeforeAndAfterAll {
  implicit val materializer = ActorMaterializer()

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A simple stream" should {
    "satisfy basic assertions" in {
      // describe our test
      val simpleSource = Source(1 to 10)
      val simpleSink = Sink.fold[Int, Int](0)(_ + _)

      val sumFuture = simpleSource.toMat(simpleSink)(Keep.right).run()

      val sum = Await.result(sumFuture, 2 seconds)
      assert(sum == 55)
    }

    "integrate with test actors via materialized values" in {
      import akka.pattern.pipe
      import system.dispatcher
      val simpleSource = Source(1 to 10)
      val simpleSink = Sink.fold[Int, Int](0)(_ + _)

      val probe = TestProbe()

      simpleSource.toMat(simpleSink)(Keep.right).run().pipeTo(probe.ref)
      probe.expectMsg(55)
    }

    "integrate with a test-actor-based sink" in {
      val simpleSource = Source(1 to 5)
      val flow = Flow[Int].scan[Int](0)(_ + _) // 0, 1, 3, 6, 10, 15
      val streamUnderTest = simpleSource.via(flow)

      val probe = TestProbe()
      val probeSink = Sink.actorRef(probe.ref, "completion message")

      streamUnderTest.to(probeSink).run()

      probe.expectMsgAllOf(0, 1, 3, 6, 10, 15)
    }

    "integration with Streams TestKit Sink" in {
      val sourceUnderTest = Source(1 to 5).map(_ * 2)

      val testSink = TestSink.probe[Int]

      val materializedTestValue: TestSubscriber.Probe[Int] = sourceUnderTest.runWith(testSink)
      materializedTestValue
        .request(5)
        .expectNext(2, 4, 6, 8, 10)
        .expectComplete()
    }

    "integrate with Streams TestKit Source" in {
      import system.dispatcher
      val sinkUnderTest = Sink.foreach[Int] {
        case 13 => throw new RuntimeException("bad luck")
        case _ =>
      }

      val testSource = TestSource.probe[Int]

      val materialized = testSource.toMat(sinkUnderTest)(Keep.both).run()
      val (testPublisher, resultFuture) = materialized
      testPublisher
        .sendNext(1)
        .sendNext(5)
        .sendNext(13)
        .sendComplete()
      resultFuture.onComplete {
        case Success(_) => fail("test sink should have thrown an exception on 13") // tricky, this does not fail the test
        case Failure(exception) => system.log.error(exception, "got exception")
      }
    }

    "test flows with a test source and a test sink" in {
      val flowUnderTest = Flow[Int].map(_ * 2)

      val testSource = TestSource.probe[Int]
      val testSink = TestSink.probe[Int]

      val materialized = testSource.via(flowUnderTest).toMat(testSink)(Keep.both).run()
      val (testPublisher, testSubscriber) = materialized

      testPublisher
        .sendNext(1)
        .sendNext(42)
        .sendNext(99)
        .sendComplete()

      testSubscriber
        .request(3)
        .expectNext(2, 84, 198)
        .expectComplete()
    }
  }
}
