// object e_error_handling {
  import fs2.Stream
  import cats.effect.IO

  /*
		Error handling
		A stream can raise errors, either explicitly, using Stream.raiseError, or implicitly via an exception
		in pure code or inside an effect passed to eval:
	*/

  val err = Stream.raiseError[IO](new Exception("oh noes!"))
                                                  

  val err2 = Stream(1, 2, 3) ++ (throw new Exception("!@#$"))
                                                  

  val err3 = Stream.eval(IO(throw new Exception("error in effect!!!")))
                                                  

	//
  // All these fail when running:
  //
  try err.compile.toList.unsafeRunSync catch { case e: Exception => println(e) }
                                                  
                                                  
  try err2.toList catch { case e: Exception => println(e) }
                                                  
                                                  
  try err3.compile.drain.unsafeRunSync() catch { case e: Exception => println(e) }
                                                  

	//
  // The handleErrorWith method lets us catch any of these errors:
	//
  err.handleErrorWith { e => Stream.emit(e.getMessage) }.compile.toList.unsafeRunSync()
                                                  
  
  /*
	Note: Don’t use handleErrorWith for doing resource cleanup; use bracket as discussed in the next section.
	 Also see this section of the appendix for more details.
	*/

//}