import fs2.Stream

object guide_a_build_streams {

  /*
	  Overview
		The FS2 library has two major capabilities:

		The ability to build arbitrarily complex streams, possibly with embedded effects.
		The ability to transform one or more streams using a small but powerful set of operations
		We’ll consider each of these in this guide.

		Building streams
		A Stream[F,O] (formerly Process) represents a discrete stream of O values which may request evaluation of F effects. We’ll call F the effect type and O the output type. Let’s look at some examples:
	*/

  val s0 = Stream.empty                           //> s0  : fs2.Stream[fs2.Pure,fs2.INothing] = Stream(..)
  val s1 = Stream.emit(1)                         //> s1  : fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)
  val s1a = Stream(1, 2, 3) // variadic           //> s1a  : fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)
  val s1b = Stream.emits(List(1, 2, 3)) // accepts any Seq
                                                  //> s1b  : fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)

  /*
		The s1 stream has the type Stream[Pure,Int]. Its output type is of course Int, and its effect type is Pure, which means it does not require evaluation of any effects to produce its output. Streams that don’t use any effects are called pure streams. You can convert a pure stream to a List or Vector using:
	*/
  s1.toList                                       //> res0: List[Int] = List(1)
  s1.toVector                                     //> res1: Vector[Int] = Vector(1)

  /*
    Streams have lots of handy list-like functions. Here's a very small sample:
  */

  (Stream(1,2,3) ++ Stream(4,5)).toList           //> res2: List[Int] = List(1, 2, 3, 4, 5)

  Stream(1,2,3).map(_ + 1).toList                 //> res3: List[Int] = List(2, 3, 4)

  Stream(1,2,3).filter(_ % 2 != 0).toList         //> res4: List[Int] = List(1, 3)

  Stream(1,2,3).fold(0)(_ + _).toList             //> res5: List[Int] = List(6)

  Stream(None,Some(2),Some(3)).collect { case Some(i) => i }.toList
                                                  //> res6: List[Int] = List(2, 3)

  Stream.range(0,5).intersperse(42).toList        //> res7: List[Int] = List(0, 42, 1, 42, 2, 42, 3, 42, 4)

  Stream(1,2,3).flatMap(i => Stream(i,i)).toList  //> res8: List[Int] = List(1, 1, 2, 2, 3, 3)

  Stream(1,2,3).repeat.take(9).toList             //> res9: List[Int] = List(1, 2, 3, 1, 2, 3, 1, 2, 3)

//  Stream(1,2,3).repeatN(2).toList

  /*
     Of these, only flatMap is primitive, the rest are built using combinations of various
      other primitives. We'll take a look at how that works shortly.
	*/

}