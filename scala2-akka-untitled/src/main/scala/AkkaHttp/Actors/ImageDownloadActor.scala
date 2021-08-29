package com.talma
package AkkaHttp.Actors

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImageDownloadActor extends Actor {

  def imgName(content: String): String = {
    val lst = content.split('/')
    lst(lst.length-1)
  }

  implicit val system = context.system

  override def receive: Receive = {

    case DOWNLOAD(url) =>
        val originalSender = sender()
        Http(context.system).singleRequest(Get(url)).map {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            val imageStringFuture: Future[String] = entity.dataBytes.runFold[ByteString](ByteString(""))(_ ++ _).map {
              _.utf8String
            }

            imageStringFuture
              .map(codedImage => DOWNLOAD_COMPLETED(imgName(url), codedImage))
              .map(downloadMessage => originalSender ! downloadMessage)
        }
  }

}
