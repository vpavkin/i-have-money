package ru.pavkin.ihavemoney.readback

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import io.funcqrs.akka.EventsSourceProvider
import io.funcqrs.akka.backend.AkkaBackend
import io.funcqrs.backend.{Query, QueryByTag}
import io.funcqrs.config.api._
import ru.pavkin.ihavemoney.domain.fortune._
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

object Application extends App {

  println("Starting IHaveMoney read backend...")

  val config = ConfigFactory.load()
  val system: ActorSystem = ActorSystem(config.getString("app.system"))

  val database: PostgresDriver.Backend#Database = Database.forConfig("read-db")
  val moneyRepo = new DatabaseMoneyViewRepository(database)

  val backend = new AkkaBackend {
    val actorSystem: ActorSystem = system
    def sourceProvider(query: Query): EventsSourceProvider = {
      query match {
        case QueryByTag(Fortune.tag) => new FortuneTagEventSourceProvider(Fortune.tag)
      }
    }
  }.configure {
    projection(
      query = QueryByTag(Fortune.tag),
      projection = new MoneyViewProjection(moneyRepo),
      name = "MoneyViewProjection"
    ).withBackendOffsetPersistence()
  }

  val interface = system.actorOf(Props(new InterfaceActor(moneyRepo)), "interface")
}
