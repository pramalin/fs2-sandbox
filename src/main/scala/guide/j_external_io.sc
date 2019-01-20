object j_external_io {
  import fs2.Stream
  import cats.effect.IO
  /*
		Talking to the external world
	
		When talking to the external world, there are a few different situations you might encounter:
		
		1. Functions which execute side effects synchronously.
			These are the easiest to deal with.
		2. Functions which execute effects asynchronously, and invoke a callback once when completed.
		 	Example: fetching 4MB from a file on disk might be a function that accepts
		 	a callback to be invoked when the bytes are available.
		3. Functions which execute effects asynchronously, and invoke a callback one or more times as results become available.
			Example: a database API which asynchronously streams results of a query as they become available.
	
		We’ll consider each of these in turn.
	*/

	//
	//	1. Synchronous effects
	//		These are easy to deal with. Just wrap these effects in a Stream.eval:
  def destroyUniverse(): Unit = { println("BOOOOM!!!"); } // stub implementation
                                                  //> destroyUniverse: ()Unit

  val s = Stream.eval_(IO { destroyUniverse() }) ++ Stream("...moving on")
                                                  //> s  : fs2.Stream[[x]cats.effect.IO[x],String] = Stream(..)

  s.compile.toVector.unsafeRunSync()              //> BOOOOM!!!
                                                  //| res0: Vector[String] = Vector(...moving on)

  /*
		The way you bring synchronous effects into your effect type may differ. Sync.delay can be used for this generally,
		without committing to a particular effect:
	*/

  import cats.effect.Sync

  val T = Sync[IO]                                //> T  : cats.effect.Sync[cats.effect.IO] = cats.effect.IOLowPriorityInstances$
                                                  //| IOEffect@351d00c0

  val s2 = Stream.eval_(T.delay { destroyUniverse() }) ++ Stream("...moving on")
                                                  //> s2  : fs2.Stream[[x]cats.effect.IO[x],String] = Stream(..)

  s2.compile.toVector.unsafeRunSync()             //> BOOOOM!!!
                                                  //| res1: Vector[String] = Vector(...moving on)

  /*
		When using this approach, be sure the expression you pass to delay doesn’t throw exceptions.
	*/
	
	//
	//	2. Asynchronous effects (callbacks invoked once)
	//		Very often, you’ll be dealing with an API like this:
	
	trait Connection {
	  def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit
	
	  // or perhaps
	  def readBytesE(onComplete: Either[Throwable,Array[Byte]] => Unit): Unit =
	    readBytes(bs => onComplete(Right(bs)), e => onComplete(Left(e)))
	
	  override def toString = "<connection>"
	}
	
	/*
	// defined trait Connection
	That is, we provide a Connection with two callbacks (or a single callback that accepts an Either), and at some point
	later, the callback will be invoked once. The cats.effect.Async trait provides a handy function in these situations:
	
	trait Async[F[_]] extends MonadError[F, Throwable] {
	  ...
	  /**
	   Create an `F[A]` from an asynchronous computation, which takes the form
	   of a function with which we can register a callback. This can be used
	   to translate from a callback-based API to a straightforward monadic
	   version.
	   */
	  def async[A](register: (Either[Throwable,A] => Unit) => Unit): F[A]
	}
	
	Here’s a complete example:
	*/

  val c = new Connection {
    def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit = {
      Thread.sleep(200)
      onSuccess(Array(0, 1, 2))
    }
  }                                               //> c  : j_external_io.Connection = <connection>

  val bytes = cats.effect.Async[IO].async[Array[Byte]] { (cb: Either[Throwable, Array[Byte]] => Unit) =>
    c.readBytesE(cb)
  }                                               //> bytes  : cats.effect.IO[Array[Byte]] = IO$1278677872

  Stream.eval(bytes).map(_.toList).compile.toVector.unsafeRunSync()
                                                  //> res2: Vector[List[Byte]] = Vector(List(0, 1, 2))

  /*
		Be sure to check out the fs2.io package which has nice FS2 bindings to Java NIO libraries, using exactly this approach.
	*/

	//
	// 3. Asynchronous effects (callbacks invoked multiple times)
	//
	/*
	The nice thing about callback-y APIs that invoke their callbacks once is that throttling/back-pressure can be handled
	within FS2 itself. If you don’t want more values, just don’t read them, and they won’t be produced! But sometimes you’ll
	be dealing with a callback-y API which invokes callbacks you provide it more than once. Perhaps it’s a streaming API of
	some sort and it invokes your callback whenever new data is available. In these cases, you can use an asynchronous queue
	to broker between the nice stream processing world of FS2 and the external API, and use whatever ad hoc mechanism that
	API provides for throttling of the producer.
	
	Note: Some of these APIs don’t provide any means of throttling the producer, in which case you either have accept
	possibly unbounded memory usage (if the producer and consumer operate at very different rates), or use blocking
	concurrency primitives like fs2.concurrent.Queue.bounded or the primitives in java.util.concurrent.
	
	Let’s look at a complete example:
	*/

  import fs2._
  import fs2.concurrent._
  import cats.effect.{ ConcurrentEffect, ContextShift, IO }

  type Row = List[String]
  // defined type alias Row

  trait CSVHandle {
    def withRows(cb: Either[Throwable, Row] => Unit): Unit
  }
  // defined trait CSVHandle

  def rows[F[_]](h: CSVHandle)(implicit F: ConcurrentEffect[F], cs: ContextShift[F]): Stream[F, Row] =
    for {
      q <- Stream.eval(Queue.unbounded[F, Either[Throwable, Row]])
      _ <- Stream.eval { F.delay(h.withRows(e => F.runAsync(q.enqueue1(e))(_ => IO.unit).unsafeRunSync)) }
      row <- q.dequeue.rethrow
    } yield row                                   //> rows: [F[_]](h: j_external_io.CSVHandle)(implicit F: cats.effect.Concurrent
                                                  //| Effect[F], implicit cs: cats.effect.ContextShift[F])fs2.Stream[F,j_external
                                                  //| _io.Row]

  /*
		See Queue for more useful methods. Most concurrent queues in FS2 support tracking their size, which is handy for
		implementing size-based throttling of the producer.
	*/

}