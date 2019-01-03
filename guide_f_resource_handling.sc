object guide_f_resource_handling {
  import fs2.Stream
  import cats.effect.IO

  /*
		Resource acquisition
		If you have to acquire a resource and want to guarantee that some cleanup action is run if the resource
		is acquired, use the bracket function:
	*/
  val count = new java.util.concurrent.atomic.AtomicLong(0)
                                                  //> count  : java.util.concurrent.atomic.AtomicLong = 0

  val acquire = IO { println("incremented: " + count.incrementAndGet); () }
                                                  //> acquire  : cats.effect.IO[Unit] = IO$1641808846
  val release = IO { println("decremented: " + count.decrementAndGet); () }
                                                  //> release  : cats.effect.IO[Unit] = IO$750044075

	val err = Stream.raiseError[IO](new Exception("oh noes!"))
                                                  //> err  : fs2.Stream[cats.effect.IO,fs2.INothing] = Stream(..)

  Stream.bracket(acquire)(_ => release).flatMap(_ => Stream(1, 2, 3) ++ err).compile.drain.unsafeRunSync()
                                                  //> incremented: 1
                                                  //| decremented: 0
                                                  //| java.lang.Exception: oh noes!
                                                  //| 	at guide_f_resource_handling$.$anonfun$main$1(guide_f_resource_handling.
                                                  //| scala:15)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$anonfun$$ex
                                                  //| ecute$1(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:76)
                                                  //| 	at guide_f_resource_handling$.main(guide_f_resource_handling.scala:3)
                                                  //| 	at guide_f_resource_handling.main(guide_f_resource_handling.scala)

  count.get

  // The inner stream fails, but notice the release action is still run:

  
  /*
	 No matter how you transform an FS2 Stream or where any errors occur, the library guarantees that if the resource
	 is acquired via a bracket, the release action associated with that bracket will be run.
	 Here’s the signature of bracket:
	 
			def bracket[F[_], R](acquire: F[R])(release: R => F[Unit]): Stream[F, R]
	
		FS2 guarantees once and only once semantics for resource cleanup actions introduced by the Stream.bracket
		function.
	*/

}