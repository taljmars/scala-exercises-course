package com.talma
package AkkaHttp.Actors

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.util.ByteString
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HtmlParsingActor extends Actor {

  def imgSrc(content: String) :List[String]= {
    val src = """(?s)<img\s[^>]*?src\s*=\s*['\"]([^'\"]*?)['\"][^>]*?>""".r
    src.findAllMatchIn(content).map(_.group(1)).toList
  }

  implicit val system = context.system

  override def receive: Receive = {

    case PARSE(url) =>
      val originalSender = sender()
      Http(system).singleRequest(Get(url)).map {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          val contentFuture: Future[String] = entity.dataBytes.runFold[ByteString](ByteString(""))(_ ++ _).map {
            _.utf8String
          }
          contentFuture.map(imgSrc(_)).map(originalSender ! PARSE_COMPLETED(_))
      }
  }
}
