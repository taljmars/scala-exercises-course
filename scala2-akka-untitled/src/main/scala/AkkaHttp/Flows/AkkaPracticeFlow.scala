package com.talma
package AkkaHttp.Flows

import AkkaHttp.Flows.Utils.{getFutureOfLink, imgName, imgSrc}

import akka.NotUsed
import akka.actor.ClassicActorSystemProvider
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.HttpResponse
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.util.ByteString

import java.nio.file.Paths
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}


/*
Exercise: Code the following Scala program :
Get “www.salt.security” HTML page
Get all images from HTML page in a.
save all images in the directory "/tmp"
Optional tips:
Use 3 actors:
HtmlParsingActor - download html page + parsing image urls
ImageDownloadActor - download image
FIleSystemActor - write image to filesystem.
If the system runs from a single process why are 3 actors good?
Use Pattern matching.
Use Future to print output to Main.
Notice that actor messages are immutable, why is it important?
*/

object Utils {

  def imgSrc(content: String) :List[String]= {
    val src = """(?s)<img\s[^>]*?src\s*=\s*['\"]([^'\"]*?)['\"][^>]*?>""".r
    src.findAllMatchIn(content).map(_.group(1)).toList
  }

  def imgName(content: String): String = {
    val lst = content.split('/')
    lst(lst.length-1)
  }

  def getFutureOfLink(url: String)(implicit classicActorSystemProvider: ClassicActorSystemProvider): Future[Source[ByteString, Any]] = {
    val saltHtml = Get(url)
    val ftr: Future[HttpResponse] = Http().singleRequest(saltHtml)
    ftr.map[Source[ByteString, Any]](_.entity.dataBytes)
  }

}

object AkkaPractice extends App {

  println("Staring Akka Exercise")

  implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext = system.executionContext

  def handleAddress(url: String) = {

    val x = getFutureOfLink(url)
    x.map{ in =>

      val decoder: Flow[ByteString, String, NotUsed] = Flow[ByteString].map(_.utf8String)
      val parser: Flow[String, Set[String], NotUsed] = Flow[String].fold(Set[String]())((set, value) => if (imgSrc(value).isEmpty) set else set ++ imgSrc(value))

      def downloader(path: String) = getFutureOfLink(path).map {decoder.to(filewriter(imgName(path))).runWith(_)}
      def filewriter(filename: String): Sink[String, Future[IOResult]] = Flow[String].map(s => ByteString(s + "\n")).toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

      val imagedispatcher = Sink.foreach[Set[String]]{_.foreach(downloader(_))}

      decoder via parser to imagedispatcher runWith in
    }
  }

  Await.result(handleAddress("https://salt.security/"), 10.seconds)
  println("End")

}
