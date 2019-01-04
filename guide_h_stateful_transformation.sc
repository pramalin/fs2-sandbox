object guide_h_stateful_transformation {

  /*
		Statefully transforming streams

		We often wish to statefully transform one or more streams in some way, possibly evaluating effects as we do so.
		As a running example, consider taking just the first 5 elements of a s: Stream[IO,Int]. To produce a Stream[IO,Int]
		which takes just the first 5 elements of s, we need to repeatedly await (or pull) values from s, keeping track
		of the number of values seen so far and stopping as soon as we hit 5 elements. In more complex scenarios, we may want
		to evaluate additional effects as we pull from one or more streams.

		Let’s look at an implementation of take using the scanChunksOpt combinator:
	*/

  import fs2._

  def tk[F[_], O](n: Long): Pipe[F, O, O] =
    in => in.scanChunksOpt(n) { n =>
      if (n <= 0) None
      else Some(c => c.size match {
        case m if m < n => (n - m, c)
        case m          => (0, c.take(n.toInt))
      })
    }                                             //> tk: [F[_], O](n: Long)fs2.Pipe[F,O,O]

  Stream(1, 2, 3, 4).through(tk(2)).toList        //> res0: List[Int] = List(1, 2)

  /*
	// Let’s take this line by line.

				in => in.scanChunksOpt(n) { n =>

		Here we create an anonymous function from Stream[F,O] to Stream[F,O] and we call scanChunksOpt passing an initial
		state of n and a function which we define on subsequent lines. The function takes the current state as an argument,
		which we purposefully give the name n, shadowing the n defined in the signature of tk, to make sure we can’t
		accidentally reference it.

				if (n <= 0) None

		If the current state value is 0 (or less), we’re done so we return None. This indicates to scanChunksOpt that the stream
	 	should terminate.

				else Some(c => c.size match {
				  case m if m <= n => (c, n - m)
				  case m => (c.take(n), 0)
				})

		Otherwise, we return a function which processes the next chunk in the stream. The function first checks the size
		of the chunk. If it is less than the number of elements to take, it returns the chunk unmodified, causing it to be
		output downstream, along with the number of remaining elements to take from subsequent chunks (n - m). If instead,
		the chunks size is greater than the number of elements left to take, n elements are taken from the chunk and output,
		along with an indication that there are no more elements to take.

		Sometimes, scanChunksOpt isn’t powerful enough to express the stream transformation. Regardless of how complex the job,
		the fs2.Pull type can usually express it.

		The Pull[+F[_],+O,+R] type represents a program that may pull values from one or more streams, write output of type O,
		and return a result of type R. It forms a monad in R and comes equipped with lots of other useful operations. See the
		Pull class for the full set of operations on Pull.

		Let’s look at an implementation of take using Pull:
	*/

  import fs2._

  def tk1[F[_], O](n: Long): Pipe[F, O, O] = {
    def go(s: Stream[F, O], n: Long): Pull[F, O, Unit] = {
      s.pull.uncons.flatMap {
        case Some((hd, tl)) =>
          hd.size match {
            case m if m <= n => Pull.output(hd) >> go(tl, n - m)
            case m           => Pull.output(hd.take(n.toInt)) >> Pull.done
          }
        case None => Pull.done
      }
    }
    in => go(in, n).stream
  }                                               //> tk1: [F[_], O](n: Long)fs2.Pipe[F,O,O]

  Stream(1, 2, 3, 4).through(tk(2)).toList        //> res1: List[Int] = List(1, 2)

  /*
		Taking this line by line:

				def go(s: Stream[F,O], n: Long): Pull[F,O,Unit] = {

		We implement this with a recursive function that returns a Pull. On each invocation, we provide a Stream[F,O] and
		the number of elements remaining to take n.

				s.pull.uncons.flatMap {

		Calling s.pull gives us a variety of methods which convert the stream to a Pull. We use uncons to pull the next chunk
		from the stream, giving us a Pull[F,Nothing,Option[(Chunk[O],Stream[F,O])]]. We then flatMap in to that pull to access
		the option.

				case Some((hd,tl)) =>
				  hd.size match {
				    case m if m <= n => Pull.output(hd) >> go(tl, n - m)
				    case m => Pull.output(hd.take(n)) >> Pull.done
				  }
				  
		If we receive a Some, we destructure the tuple as hd: Chunk[O] and tl: Stream[F,O]. We then check the size of the head
		chunk, similar to the logic we used in the scanChunksOpt version. If the chunk size is less than or equal to the
		remaining elements to take, the chunk is output via Pull.output and we then recurse on the tail by calling go, passing
		the remaining elements to take. Otherwise we output the first n elements of the head and indicate we are done pulling.

					in => go(in,n).stream
					
		Finally, we create an anonymous function from Stream[F,O] to Stream[F,O] and call go with the initial n value. We’re
		returned a Pull[F,O,Unit], which we convert back to a Stream[F,O] via the .stream method.
	*/

  val s2 = Stream(1, 2, 3, 4).through(tk1(2))     //> s2  : fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)

  s2.toList                                       //> res2: List[Int] = List(1, 2)

  /*
		FS2 takes care to guarantee that any resources allocated by the Pull are released when the stream completes. Note again
		that nothing happens when we call .stream on a Pull, it is merely converting back to the Stream API.

		There are lots of useful transformation functions in Stream built using the Pull type.

	Exercises
	Try implementing takeWhile, intersperse, and scan:
	*/
	
	Stream.range(0,100).takeWhile(_ < 7).toList
                                                  //> res3: List[Int] = List(0, 1, 2, 3, 4, 5, 6)
	
	Stream("Alice","Bob","Carol").intersperse("|").toList
                                                  //> res4: List[String] = List(Alice, |, Bob, |, Carol)
	
//	Stream.range(1,10).scan(0)(_ + _).toList
	 // running sum
		
}