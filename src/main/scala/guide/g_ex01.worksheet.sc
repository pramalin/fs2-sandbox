// object g_ex01 {
  import fs2.Stream
  import cats.effect.IO

  /*
	Exercises
		Implement repeat, which repeats a stream indefinitely, drain, which strips all output from a stream, eval_,
	 	which runs an effect and ignores its output, and attempt, which catches any errors produced by a stream:
	*/

  Stream(1, 0).repeat.take(6).toList              
  Stream(1, 2, 3).drain.toList                    

  Stream.eval_(IO(println("!!"))).compile.toVector.unsafeRunSync()
                                                  
                                                  

  (Stream(1, 2) ++ (throw new Exception("nooo!!!"))).attempt.toList
                                                  
                                                  
//}