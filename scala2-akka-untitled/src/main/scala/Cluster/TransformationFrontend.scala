package com.talma
package Cluster

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.cluster.Cluster
import akka.pattern.ask
import akka.util.Timeout
import com.talma.Cluster.TransformationBackend.args
import com.typesafe.config.ConfigFactory

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.DurationInt

class TransformationFrontend extends Actor {

  var backends = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  def receive = {
    case job: TransformationJob if backends.isEmpty => {
      println("TransformationFrontend >>>> Job (no BE) '%s'".format(job))
      sender() ! JobFailed("Service unavailable, try again later", job)
    }

    case job: TransformationJob => {
      println("TransformationFrontend >>>> Job (with BE) '%s'".format(job))
      jobCounter += 1
      backends(jobCounter % backends.size).forward(job)
    }

    case BackendRegistration if !backends.contains(sender()) => {
      println("TransformationFrontend >>>> Register")
      context.watch(sender())
      backends = backends :+ sender()
    }

    case Terminated(a) => {
      println("TransformationFrontend >>>> Terminated '%s'".format(a))
      backends = backends.filterNot(_ == a)
    }
  }

}

object TransformationFrontend extends App {

  implicit val timeout = Timeout(5.seconds)

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString("akka.remote.artery.canonical.port=%s".format(port))
    .withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]"))
    .withFallback(ConfigFactory.load())
  val system = ActorSystem("ClusterSystem", config)
  val frontend = system.actorOf(Props[TransformationFrontend], name = "frontend")

  frontend ? TransformationJob("First Packet")

  val counter = new AtomicInteger
  import system.dispatcher
  system.scheduler.scheduleAtFixedRate(2.seconds, 2.seconds) (() => (frontend ? TransformationJob("hello-" + counter.incrementAndGet())).onComplete{println(_)})

}
