object guide_e_error_handling {
  import fs2.Stream
  import cats.effect.IO

  /*
		Error handling
		A stream can raise errors, either explicitly, using Stream.raiseError, or implicitly via an exception
		in pure code or inside an effect passed to eval:
	*/

  val err = Stream.raiseError[IO](new Exception("oh noes!"))
                                                  //> err  : fs2.Stream[cats.effect.IO,fs2.INothing] = Stream(..)

  val err2 = Stream(1, 2, 3) ++ (throw new Exception("!@#$"))
                                                  //> err2  : fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)

  val err3 = Stream.eval(IO(throw new Exception("error in effect!!!")))
                                                  //> err3  : fs2.Stream[cats.effect.IO,Nothing] = Stream(..)

	//
  // All these fail when running:
  //
  try err.compile.toList.unsafeRunSync catch { case e: Exception => println(e) }
                                                  //> java.lang.Exception: oh noes!
                                                  //| res0: Any = ()
  try err2.toList catch { case e: Exception => println(e) }
                                                  //> java.lang.Exception: !@#$
                                                  //| res1: Any = ()
  try err3.compile.drain.unsafeRunSync() catch { case e: Exception => println(e) }
                                                  //> java.lang.Exception: error in effect!!!

	//
  // The handleErrorWith method lets us catch any of these errors:
	//
  err.handleErrorWith { e => Stream.emit(e.getMessage) }.compile.toList.unsafeRunSync()
                                                  //> res2: List[String] = List(oh noes!)
  
  /*
	Note: Don’t use handleErrorWith for doing resource cleanup; use bracket as discussed in the next section.
	 Also see this section of the appendix for more details.
	*/

}