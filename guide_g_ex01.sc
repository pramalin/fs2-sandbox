object guide_g_ex01 {
  import fs2.Stream
  import cats.effect.IO

  /*
	Exercises
		Implement repeat, which repeats a stream indefinitely, drain, which strips all output from a stream, eval_,
	 	which runs an effect and ignores its output, and attempt, which catches any errors produced by a stream:
	*/

  Stream(1, 0).repeat.take(6).toList              //> res0: List[Int] = List(1, 0, 1, 0, 1, 0)
  Stream(1, 2, 3).drain.toList                    //> res1: List[fs2.INothing] = List()

  Stream.eval_(IO(println("!!"))).compile.toVector.unsafeRunSync()
                                                  //> !!
                                                  //| res2: Vector[fs2.INothing] = Vector()

  (Stream(1, 2) ++ (throw new Exception("nooo!!!"))).attempt.toList
                                                  //> res3: List[Either[Throwable,Int]] = List(Right(1), Right(2), Left(java.lang.
                                                  //| Exception: nooo!!!))
}