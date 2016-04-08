package ru.pavkin.ihavemoney.writeback

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext

class InterfaceActor(shardRegion: ActorRef)(implicit val timeout: Timeout) extends Actor {
  implicit val dispatcher: ExecutionContext = context.system.dispatcher

  def receive: Receive = {
    case a ⇒
      val origin = sender
      (shardRegion ? a).foreach(origin ! _)
  }
}
