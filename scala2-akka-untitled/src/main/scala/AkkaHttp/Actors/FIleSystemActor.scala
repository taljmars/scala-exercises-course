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

    case DOWNLOAD_COMPLETED(name: String, codedimage: String) =>

      val mySender = sender()
      val file = Paths.get(name)
      val text = Source.single(codedimage)

      val result: Future[IOResult] = text.map(t => ByteString(t)).runWith(FileIO.toPath(file))
      result.map(_ => mySender ! WRITING_COMPLETED(name))
  }
}
