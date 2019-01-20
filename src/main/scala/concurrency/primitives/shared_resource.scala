package concurrency.primitives

import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}
import cats.effect.concurrent.Semaphore
import cats.syntax.functor._
import fs2.Stream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * It demonstrates one of the possible uses of [[fs2.async.mutable.Semaphore]]
  *
  * Three processes are trying to access a shared resource at the same time but only one at
  * a time will be granted access and the next process have to wait until the resource gets
  * available again (availability is one as indicated by the semaphore counter).
  *
  * R1, R2 & R3 will request access of the precious resource concurrently so this could be
  * one possible outcome:
  *
  * R1 >> Availability: 1
  * R2 >> Availability: 1
  * R2 >> Started | Availability: 0
  * R3 >> Availability: 0
  * --------------------------------
  * R1 >> Started | Availability: 0
  * R2 >> Done | Availability: 0
  * --------------------------------
  * R3 >> Started | Availability: 0
  * R1 >> Done | Availability: 0
  * --------------------------------
  * R3 >> Done | Availability: 1
  *
  * This means when R1 and R2 requested the availability it was one and R2 was faster in
  * getting access to the resource so it started processing. R3 was the slowest and saw
  * that there was no availability from the beginning.
  *
  * Once R2 was done R1 started processing immediately showing no availability.
  *
  * Once R1 was done R3 started processing immediately showing no availability.
  *
  * Finally, R3 was done showing an availability of one once again.
  * */
class PreciousResource[F[_]: Concurrent: Timer](name: String, s: Semaphore[F]) {

  def use: Stream[F, Unit] = {
    for {
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Availability: $a")))
      _ <- Stream.eval(s.acquire)
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Started | Availability: $a")))
      _ <- Stream.sleep(3.seconds)
      _ <- Stream.eval(s.release)
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Done | Availability: $a")))
    } yield ()
  }
}

object shared_resource extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream = for {
      s   <- Stream.eval(Semaphore[IO](1))
      r1  = new PreciousResource[IO]("R1", s)
      r2  = new PreciousResource[IO]("R2", s)
      r3  = new PreciousResource[IO]("R3", s)
      _   <- Stream(r1.use, r2.use, r3.use).parJoin(3).drain
    } yield ()
    stream.compile.drain.as(ExitCode.Success)
  }
}