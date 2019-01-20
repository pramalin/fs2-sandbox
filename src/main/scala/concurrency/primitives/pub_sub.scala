package concurrency.primitives
/**
  * Single Publisher / Multiple Subscribers application implemented on top of
  * [[fs2.async.mutable.Topic]] and [[fs2.async.mutable.Signal]].
  *
  * The program ends after 15 seconds when the signal interrupts the publishing of more events
  * given that the final streaming merge halts on the end of its left stream (the publisher).
  *
  * - Subscriber #1 should receive 15 events + the initial empty event
  * - Subscriber #2 should receive 10 events
  * - Subscriber #3 should receive 5 events
  * */
import scala.concurrent.duration._

import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}
import cats.syntax.all._
import fs2.{Sink, Stream}
import fs2.concurrent.{SignallingRef, Topic}

sealed trait Event
case class Text(value: String) extends Event
case object Quit extends Event

class EventService[F[_]](eventsTopic: Topic[F, Event],
                         interrupter: SignallingRef[F, Boolean])(implicit F: Concurrent[F], timer: Timer[F]) {

  // Publishing 15 text events, then single Quit event, and still publishing text events
  def startPublisher: Stream[F, Unit] = {
    val textEvents = eventsTopic.publish(
      Stream.awakeEvery[F](1.second)
        .zipRight(Stream(Text(System.currentTimeMillis().toString)).repeat)
    )
    val quitEvent = Stream.eval(eventsTopic.publish1(Quit))

    (textEvents.take(15) ++ quitEvent ++ textEvents).interruptWhen(interrupter)
  }

  // Creating 3 subscribers in a different period of time and join them to run concurrently
  def startSubscribers: Stream[F, Unit] = {
    val s1: Stream[F, Event] = eventsTopic.subscribe(10)
    val s2: Stream[F, Event] = Stream.sleep_[F](5.seconds) ++ eventsTopic.subscribe(10)
    val s3: Stream[F, Event] = Stream.sleep_[F](10.seconds) ++ eventsTopic.subscribe(10)

    // When Quit message received - terminate the program
    def sink(subscriberNumber: Int): Sink[F, Event] =
      _.evalMap {
        case e @ Text(_) => F.delay(println(s"Subscriber #$subscriberNumber processing event: $e"))
        case Quit => interrupter.set(true)
      }

    Stream(s1.to(sink(1)), s2.to(sink(2)), s3.to(sink(3))).parJoin(3)
  }
}

object PubSub extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream = for {
      topic     <- Stream.eval(Topic[IO, Event](Text("")))
      signal    <- Stream.eval(SignallingRef[IO, Boolean](false))
      service   = new EventService[IO](topic, signal)
      _         <- Stream(
        service.startPublisher.concurrently(service.startSubscribers)
      ).parJoin(2).drain
    } yield ()
    stream.compile.drain.as(ExitCode.Success)
  }
}