package com.talma
package AkkaHttp.Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.routing.SmallestMailboxPool

object AkkaPracticeActors extends App{

  implicit val system = ActorSystem("SingleRequest")

  val orch = system.actorOf(Props(classOf[Orchestrator]), "orchestrator")
  val parser = system.actorOf(Props(classOf[HtmlParsingActor]), "html-parser")
  val writersPool: ActorRef = system.actorOf(SmallestMailboxPool(3).props(Props(classOf[FIleSystemActor])), "files-writers")


  class Orchestrator extends Actor {

    var totalImages = 0

    override def receive: Receive = {

      case req: PARSE =>
        parser ! req

      case PARSE_COMPLETED(imagesUrlList) =>
        totalImages = imagesUrlList.length
        println(s"Downloading $totalImages Images...")

        //Closing actor
        context.stop(parser)
        imagesUrlList.foreach(
          path => {
            val downloader = system.actorOf(Props(classOf[ImageDownloadActor]))
            downloader ! DOWNLOAD(path)
          }
        )

      case DOWNLOAD_COMPLETED(url, data) =>
        // Closing Downloader actor
        context.stop(sender())
        writersPool ! DOWNLOAD_COMPLETED(url, data)

      case WRITING_COMPLETED(name) =>
        totalImages = totalImages - 1
        if (totalImages == 0) {
          println("Download Completed")
          context.stop(writersPool)
          context.stop(self)
          context.system.terminate()
        }

      case Terminated(diedActor) =>
        println(s"Oh No!! $diedActor")

      case unknownError: Any =>
        println(s"Unknown: $unknownError")
    }
  }

  println("Staring Akka Exercise")
  orch ! PARSE("https://salt.security/")
}
