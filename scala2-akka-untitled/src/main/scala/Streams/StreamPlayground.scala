package com.talma
package Streams

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths
import akka.stream._
import akka.stream.scaladsl._
object StreamPlayground extends App{

  implicit val system: ActorSystem = ActorSystem("StreamDemo")
  implicit val ec = system.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 50)
  val factorials = source.scan(BigInt(1))((acc, num) => acc * num)
  val odds = source.filter(_ % 2 == 1)
  val pairs = source.filterNot(_ % 2 == 1)


//  val new_source = source.buffer(10, OverflowStrategy.dropHead).map(_).runWith(Sink.foreach(println))
//  val new_source.onComplete(a => println(a))


  val flow = source.map(x => "" + x)
//  flow.runForeach(println(_))

//  val factftr = source.reduce((a,b) => a+b).runForeach(println(_))



  val k =  factorials
    .zipWith(Source(1 to 5))((num, idx) => s"$idx! = $num")
    .throttle(1, 1.second)
    .runForeach(println(_))


  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String].map(s => ByteString(s + "\n")).toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  val b = source.map(_.toString).runWith(lineSink("factorials.txt"))


  /// Termination
  k.onComplete{_ => system.terminate()}

}
