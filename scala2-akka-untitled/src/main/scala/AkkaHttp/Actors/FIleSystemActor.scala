package com.talma
package AkkaHttp.Actors

import akka.actor.Actor
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString

import java.nio.file.Paths
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FIleSystemActor extends Actor {

  implicit val system = context.system

  override def receive: Receive = {

    case DOWNLOAD_COMPLETED(name: String, codedImage: String) =>
      val originalSender = sender()
      val filePath = Paths.get(name)
      val contentSource = Source.single(codedImage)

      val res: Future[IOResult] = contentSource.map(t => ByteString(t)).runWith(FileIO.toPath(filePath))
      res.map(_ => originalSender ! WRITING_COMPLETED(name))
  }
}
