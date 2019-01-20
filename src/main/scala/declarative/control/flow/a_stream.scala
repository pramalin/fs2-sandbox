package declarative.control.flow

import cats.effect.IO
import fs2._

object a_stream extends App {
  val s1 = Stream.emit(1, 2, 3)

  val s2 = Stream.eval { IO(println("hello")) }

//  val s3 = Stream.repeatEval { IO(println("hello")) }

  println(s1.toList) // no IO 
  
  val action2: IO[Unit] = s2.compile.drain
  action2.unsafeRunSync
}