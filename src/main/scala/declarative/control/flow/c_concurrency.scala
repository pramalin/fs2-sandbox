package declarative.control.flow


import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Timer
import fs2.Stream
import fs2.concurrent.SignallingRef
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

object c_concurrency extends App {
  implicit val timer: Timer[IO] = IO.timer(scala.concurrent.ExecutionContext.Implicits.global)
  implicit val ioContextShift: ContextShift[IO] = 
    IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

    
	def stopAfter[A](f: FiniteDuration): Stream[IO, A] => Stream[IO, A] =
	  in => {
	   def close(s: SignallingRef[IO, Boolean]): Stream[IO, Unit] =
	     Stream.sleep_[IO](f) ++ Stream.eval(s.set(true))
	
	   Stream.eval(SignallingRef[IO, Boolean](false)).flatMap { end =>
	    in.interruptWhen(end).concurrently(close(end))
	  }
	}
	
	val prog = Stream
	 .repeatEval(IO(println("hello")))
	 .through(stopAfter(2.seconds))

  prog.compile.drain.unsafeRunSync
  
}