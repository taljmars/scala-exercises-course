package com.talma
package Futures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object FuturesPlayground extends App {

  println("Welcome to Future Playground")

  val firstOccurrence: Future[Int] = Future {
    val randomNum = (Math.random() * 100).toInt
    val listRan = 1 to randomNum
    val v = listRan.foldLeft(0)((acc, num) => acc + num)
    v
  }

  firstOccurrence onComplete {
    case Success(idx) => println("The keyword first appears at position: " + idx)
    case Failure(t) => println("Could not process file: " + t.getMessage)
    case x => println(x)
  }

  println(firstOccurrence.value)

  println("Exit")

}
