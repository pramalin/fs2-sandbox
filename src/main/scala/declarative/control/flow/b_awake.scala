package declarative.control.flow

import fs2._
import scala.concurrent.duration._, cats.effect.{IO, Timer }

object b_awake extends App {
  implicit val timer: Timer[IO] = IO.timer(scala.concurrent.ExecutionContext.Implicits.global)

  List(1, 2, 3).zip(List("a", "b", "c"))

  def putInt(i: Int) = IO(println(i))

  def printRange = Stream.range(1, 10).evalMap(putInt)

  def seconds = Stream.awakeEvery[IO](1.second)

  val z = seconds.zip(printRange)
  z.take(3).compile.toVector.unsafeRunSync

  z.take(11).compile.toVector.unsafeRunSync

}
