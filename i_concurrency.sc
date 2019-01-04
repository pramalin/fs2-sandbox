import fs2.Stream
import cats.effect.IO

import cats.effect.ContextShift

object i_concurrency {

  /*
	Concurrency

	FS2 comes with lots of concurrent operations. The merge function runs two streams concurrently,
	combining their outputs. It halts when both inputs have halted:
	*/

  //  Stream(1, 2, 3).merge(Stream.eval(IO { Thread.sleep(200); 4 })).compile.toVector.unsafeRunSync()

  /*
	Oops, we need a cats.effect.ContextShift[IO] in implicit scope. Let’s add that:
	*/

  Stream(1, 2, 3).merge(Stream.eval(IO { Thread.sleep(200); 4 })).compile.toVector.unsafeRunSync()

  /*
	The merge function supports concurrency. FS2 has a number of other useful concurrency functions like concurrently
	(runs another stream concurrently and discards its output), interrupt (halts if the left branch produces false),
	either (like merge but returns an Either), mergeHaltBoth (halts if either branch halts), and others.

	The function parJoin runs multiple streams concurrently. The signature is:
	*/

  // note Concurrent[F] bound
  import cats.effect.Concurrent
  //  def parJoin[F[_]: Concurrent, O](maxOpen: Int)(outer: Stream[F, Stream[F, O]]): Stream[F, O]

  /*
	It flattens the nested stream, letting up to maxOpen inner streams run at a time.

	The Concurrent bound on F is required anywhere concurrency is used in the library. As mentioned earlier, users
	can bring their own effect types provided they also supply an Concurrent instance in implicit scope.

	In addition, there are a number of other concurrency primitives—asynchronous queues, signals, and semaphores.
	See the Concurrency Primitives section for more examples. We’ll make use of some of these in the next section
	when discussing how to talk to the external world.

	Exercises
	Without looking at the implementations, try implementing mergeHaltBoth:
	*/
  type Pipe2[F[_], -I, -I2, +O] = (Stream[F, I], Stream[F, I2]) => Stream[F, O]

  /** Like `merge`, but halts as soon as _either_ branch halts. */
  def mergeHaltBoth[F[_]: Concurrent, O]: Pipe2[F, O, O, O] = (s1, s2) => ???

}