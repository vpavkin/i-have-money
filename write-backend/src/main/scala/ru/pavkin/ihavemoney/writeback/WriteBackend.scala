package ru.pavkin.ihavemoney.writeback

import akka.actor.{ActorSystem, Props}
import akka.cluster.client.ClusterClientReceptionist
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.funcqrs.akka.EventsSourceProvider
import io.funcqrs.akka.backend.AkkaBackend
import io.funcqrs.backend.Query
import io.funcqrs.config.api._
import ru.pavkin.ihavemoney.domain.fortune._

import scala.concurrent.duration._

object WriteBackend extends App {

  println("Starting IHaveMoney write backend...")

  val config = ConfigFactory.load()
  val system: ActorSystem = ActorSystem(config.getString("app.system"))

  val backend = new AkkaBackend {
    val actorSystem: ActorSystem = system
    def sourceProvider(query: Query): EventsSourceProvider = null
  }.configure {
    aggregate[Fortune](Fortune.behavior)
  }

  implicit val timeout: Timeout = new Timeout(30.seconds)

  val fortuneRegion = ClusterSharding(system).start(
    typeName = "FortuneShard",
    entityProps = Props(new FortuneOffice(backend)),
    settings = ClusterShardingSettings(system),
    messageExtractor = new CommandMessageExtractor(config.getInt("app.number-of-nodes"))
  )

  val interface = system.actorOf(Props(new InterfaceActor(fortuneRegion)), "interface")
  ClusterClientReceptionist(system).registerService(interface)
}
