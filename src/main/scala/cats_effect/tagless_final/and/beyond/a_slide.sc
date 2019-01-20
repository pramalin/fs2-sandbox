/*
    Referential Transparency
 */

val expr1 = 123
(expr1, expr1)
(123, 123)

// memoizied
// not referentialy transparent
val expr2 = println("Hey!")
(expr2, expr2)

(println("Hey!"), println("Hey!"))


// wrapping in Future

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val expr3 = Future(println("Hey"))
(expr3, expr3)

Future(println("Hey!"), println("Hey!"))


// IO Monad
import cats.effect.IO

val expr4 = IO(println("Hey!"))
val expr5 = (expr4, expr4)

val expr6 = (IO(println("Hey!")), IO(println("Hey!")))

//IO(expr5).compile.unsafeRunSync



// tagless final encoding
