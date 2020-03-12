// object k_reactive_streams {
  /*
		Reactive streams
		
		The reactive streams initiative is complicated, mutable and unsafe - it is not something that is desired for
		use over fs2. But there are times when we need use fs2 in conjunction with a different streaming library, and
		this is where reactive streams shines.
		
		Any reactive streams system can interoperate with any other reactive streams system by exposing an
		org.reactivestreams.Publisher or an org.reactivestreams.Subscriber.
		
		The reactive-streams library provides instances of reactive streams compliant publishers and subscribers to ease
		interoperability with other streaming libraries.
		
		Usage
		You may require the following imports:
	*/
  import fs2._
  import fs2.interop.reactivestreams._
  import cats.effect.{ ContextShift, IO }
  import scala.concurrent.ExecutionContext

  // A ContextShift instance is necessary when working with IO
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
                                                  
                                                  

  // To convert a Stream into a downstream unicast org.reactivestreams.Publisher:
  val stream = Stream(1, 2, 3).covary[IO]         

  stream.toUnicastPublisher                       
                                                  

  // To convert an upstream org.reactivestreams.Publisher into a Stream:
  val publisher: StreamUnicastPublisher[IO, Int] = Stream(1, 2, 3).covary[IO].toUnicastPublisher
                                                  
                                                  

  publisher.toStream[IO]                          

  // A unicast publisher must have a single subscriber only.

//}