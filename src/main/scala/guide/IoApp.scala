package guide
import fs2.Stream

object IoApp extends App {
  val s = Stream.range(1,10).scan(0)(_ + _).toList 
  println(s"s: $s")
}