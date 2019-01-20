object c_ops {
  import fs2.Stream
  import cats.effect.IO

  /*
	Basic stream operations
	Streams have a small but powerful set of operations, some of which we’ve seen already.
 	The key operations are ++, map, flatMap, handleErrorWith, and bracket:
	*/

  val appendEx1 = Stream(1, 2, 3) ++ Stream.emit(42)
                                                  //> appendEx1  : fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)
  val appendEx2 = Stream(1, 2, 3) ++ Stream.eval(IO.pure(4))
                                                  //> appendEx2  : fs2.Stream[cats.effect.IO,Int] = Stream(..)

  appendEx1.toVector                              //> res0: Vector[Int] = Vector(1, 2, 3, 42)
  appendEx2.compile.toVector.unsafeRunSync()      //> res1: Vector[Int] = Vector(1, 2, 3, 4)
  appendEx1.map(_ + 1).toList                     //> res2: List[Int] = List(2, 3, 4, 43)

	/*
		The flatMap operation is the same idea as lists - it maps, then concatenates:
	*/
 appendEx1.flatMap(i => Stream.emits(List(i,i))).toList
                                                  //> res3: List[Int] = List(1, 1, 2, 2, 3, 3, 42, 42)

	/*
		Regardless of how a Stream is built up, each operation takes constant time. So s ++ s2 takes constant time,
		regardless of whether s is Stream.emit(1) or it’s a huge stream with millions of elements and lots of
		embedded effects. Likewise with s.flatMap(f) and handleErrorWith, which we’ll see in a minute.
		The runtime of these operations do not depend on the structure of s.
	*/

}