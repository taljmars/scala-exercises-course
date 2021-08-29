package com.talma
package Actors

import akka.actor.{Actor, ActorSystem, Props, Status}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}


class MyActor extends Actor {

  override def receive: Receive = {
    case "Hi" =>
      sender() ! "Bye"
      context.stop(self)
    case x =>
      Future.failed(new Exception("Unknown Input: %s".format(x))) pipeTo sender()
      sender() !  Status.Failure(new Exception("bla"))
      context.stop(self)
  }

}


object ActorsPlayground {

  import akka.dispatch.ExecutionContexts.global


  def main(args: Array[String]): Unit = {
    println("Welcome to Actors Playground")

    implicit val timeout = Timeout(1.seconds)
    implicit val ec = global


    val context = ActorSystem("talma-Actors")
    val actor = context.actorOf(Props(classOf[MyActor]), name = "MyActor")

    val ftr = actor ? "Hi"

    ftr.onComplete{
      case Failure(a) => println(a)
      case Success(a) => println(a)
      case unexpected: Any =>
        println(s"Unexpected: $unexpected")
    }

    Await.result(ftr, 3.seconds)
    context.terminate()
    println("Exit")
  }

}
