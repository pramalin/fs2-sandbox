//package declarative.control.flow

//// object a_io_demo {
  println("Welcome to the Scala worksheet")

  /*
		#FS2

		Declarative control flow with Streams


		#CATS-EFFECT
			IO[A]

		Produces one value, fails or never terminates

			- Referentially transparent (pure)
			- Compositional
			- Many algebras (monad,...)

		#DATA PROCESSING
*/

  // processing an input list and creating output list
  def process(in: List[String]): List[String] =
    in
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(_.toUpperCase())
      .take(100)

  val inLines = List("//comment", "Line 1", "Line 2")
  val out = process(inLines)

  // assuming file read
  import cats.effect.IO
  def p: IO[List[String]] = IO(readListFromFile("file-name")).map(process)
  def readListFromFile(f: String) = inLines

  // problem with this is reading the entire file into memory
  // typically needs imperative code to read file in chunks

  /*
				FS2 STREAMS

			Stream[F[_], A]

			- emits 0...n values of type A, where n can be ∞
			- While requesting effects in F
			- F is normally IO

	STREAMING IO!
	*/

  /*
	def fahrenheitToCelsius(f: Double): Double =
	  (f - 32.0) * (5.0/9.0)

	def converter: Stream[IO, Unit] =
	  io.file.readAll[IO](Paths.get("testdata/fahrenheit.txt"), 4096)
	    .through(text.utf8Decode)
	    .through(text.lines)
	    .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
	    .map(line => fahrenheitToCelsius(line.toDouble).toString)
	    .intersperse("\n")
	    .through(text.utf8Encode)
	    .through(io.file.writeAll(Paths.get("testdata/celsius.txt")))

			**guide/ConverterApp.scala**
	*/

  /*
				#CONTROL FLOW

	FS2 STREAMS
	*/
  import fs2.Stream
  val s1 = Stream.emit(1, 2, 3)

  val s2 = Stream.eval { IO(println("hello")) }

  val s3 = Stream.repeatEval { IO(println("hello")) }

  s1.toList

  val action: IO[Unit] = s2.compile.drain
  action.unsafeRunSync

  /*
	#LIST WITH SUPERPOWERS: ++
*/
  List.range(1, 100).take(3) ++ List(21, 22)
  def put(s: String) = IO(println(s))

  val s4 = Stream.repeatEval(put("hello")).take(3) ++ Stream.eval(put("world"))

  s4.compile.drain.unsafeRunSync()

  /*
		#LIST WITH SUPERPOWERS: FLATMAP
	*/
  List(1, 2, 3).flatMap(x => List(x, x))
  def putInt(i: Int) = IO(println(i))

  val s5 = Stream(1, 2, 3).flatMap(x => Stream.repeatEval(putInt(x)).take(2))

  s5.compile.drain.unsafeRunSync()
  /*
	#LIST WITH SUPERPOWERS: ZIP
*/

  import scala.concurrent.duration._, cats.effect.{ IO, Timer }

  implicit val timer: Timer[IO] = IO.timer(scala.concurrent.ExecutionContext.Implicits.global)

  List(1, 2, 3).zip(List("a", "b", "c"))

  def printRange = Stream.range(1, 5).evalMap(putInt)

  def seconds = Stream.awakeEvery[IO](1.second)

  val z = seconds.zip(printRange)
  z.take(3).compile.toVector.unsafeRunSync

  z.take(6).compile.toVector.unsafeRunSync
