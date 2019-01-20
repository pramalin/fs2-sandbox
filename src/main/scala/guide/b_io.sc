object b_io {
  import fs2.Stream
  import cats.effect.IO

  val eff = Stream.eval(IO { println("BEING RUN!!"); 1 + 1 })
                                                  //> eff  : fs2.Stream[cats.effect.IO,Int] = Stream(..)

	  /*
	 IO is an effect type we’ll see a lot in these examples. Creating an IO has no side effects,
	 and Stream.eval doesn’t do anything at the time of creation, it’s just a description of
	 what needs to happen when the stream is eventually interpreted. Notice the type of eff is
	 now Stream[IO,Int].
	
	 The eval function works for any effect type, not just IO. FS2 does not care what effect type
	 you use for your streams. You may use IO for effects or bring your own, just by implementing
	 a few interfaces for your effect type (e.g., cats.MonadError[?, Throwable], cats.effect.Sync,
	 cats.effect.Async, cats.effect.Concurrent, and cats.effect.Effect).
	
	 Here’s the signature of eval:
	
			def eval[F[_],A](f: F[A]): Stream[F,A]
	
		eval produces a stream that evaluates the given effect, then emits the result
		(notice that F is unconstrained). Any Stream formed using eval is called ‘effectful’ and
		can’t be run using toList or toVector. If we try we’ll get a compile error:
	
	scala> eff.toList
	<console>:16: error: value toList is not a member of fs2.Stream[cats.effect.IO,Int]
	       eff.toList
	           ^
	
	 Here’s a complete example of running an effectful stream. We’ll explain this in a minute:
	*/

  eff.compile.toVector.unsafeRunSync()            //> BEING RUN!!
                                                  //| res0: Vector[Int] = Vector(2)

  /*
  The first .compile.toVector is one of several methods available to ‘compile’
  the stream to a single effect:
	*/

  val eff1 = Stream.eval(IO { println("TASK BEING RUN!!"); 1 + 1 })
                                                  //> eff1  : fs2.Stream[cats.effect.IO,Int] = Stream(..)

  val ra = eff1.compile.toVector // gather all output into a Vector
                                                  //> ra  : cats.effect.IO[Vector[Int]] = <function1>
  val rb = eff1.compile.drain // purely for effects
                                                  //> rb  : cats.effect.IO[Unit] = <function1>
  val rc = eff1.compile.fold(0)(_ + _) // run and accumulate some result
                                                  //> rc  : cats.effect.IO[Int] = <function1>
  /*
		Notice these all return a IO of some sort, but this process of compilation doesn’t actually
		perform any of the effects (nothing gets printed).

		If we want to run these for their effects ‘at the end of the universe’, we can use one of the
		unsafe* methods on IO (if you are bringing your own effect type, how you run your effects may
		of course differ):
	*/

  ra.unsafeRunSync()                              //> TASK BEING RUN!!
                                                  //| res1: Vector[Int] = Vector(2)
  rb.unsafeRunSync()                              //> TASK BEING RUN!!
  rc.unsafeRunSync()                              //> TASK BEING RUN!!
                                                  //| res2: Int = 2
  rc.unsafeRunSync()                              //> TASK BEING RUN!!
                                                  //| res3: Int = 2

  /*
   Here we finally see the tasks being executed. As is shown with rc, rerunning a task executes the
   entire computation again; nothing is cached for you automatically.

   Note: The various run* functions aren’t specialized to IO and work for any F[_] with an
   implicit Sync[F] — FS2 needs to know how to catch errors that occur during evaluation of
   F effects, how to suspend computations.
  */
 
}