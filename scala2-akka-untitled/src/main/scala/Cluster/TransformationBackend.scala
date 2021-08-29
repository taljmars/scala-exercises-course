package com.talma
package Cluster

import akka.actor.{Actor, ActorSystem, Props, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}
import com.typesafe.config.ConfigFactory

class TransformationBackend extends Actor {

    val cluster = Cluster(context.system)

    // subscribe to cluster changes, MemberUp
    // re-subscribe when restart
    override def preStart(): Unit = {
      println("TransformationBackend >>>> Pre Start actions")
      cluster.subscribe(self, classOf[MemberUp])
    }
    override def postStop(): Unit = {
      println("TransformationBackend >>>> Post Stop actions")
      cluster.unsubscribe(self)
    }

    def receive = {
      case TransformationJob(text) => sender() ! TransformationResult(text.toUpperCase)
      case state: CurrentClusterState =>
        state.members.filter(_.status == MemberStatus.Up).foreach(register)
      case MemberUp(m) => {
        println("TransformationBackend >>>> Member in cluster is up '%s'".format(m))
        register(m)
      }
    }

    def register(member: Member): Unit = {
      println("TransformationBackend >>>> Register '%s'".format(member))
      if (member.hasRole("frontend"))
        context.actorSelection(RootActorPath(member.address) / "user" / "frontend") !
          BackendRegistration
    }

}

object TransformationBackend extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString("akka.remote.artery.canonical.port=%s".format(port))
    .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
    .withFallback(ConfigFactory.load())
  val system = ActorSystem("ClusterSystem", config)
  system.actorOf(Props[TransformationBackend], name = "backend")
}
