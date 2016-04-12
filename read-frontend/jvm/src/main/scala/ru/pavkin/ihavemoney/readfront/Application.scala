package ru.pavkin.ihavemoney.readfront

import java.util.UUID

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.pattern.AskTimeoutException
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.CirceSupport
import akka.http.scaladsl.model.StatusCodes._

import scala.concurrent.duration._
import ru.pavkin.ihavemoney.domain.fortune.FortuneId
import ru.pavkin.ihavemoney.domain.query.{MoneyBalance, QueryFailed, QueryId}

object Application extends App with CirceSupport {

  import protocol._

  implicit val system = ActorSystem("IHaveMoneyReadFront")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(30.seconds)

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val readBack = new ReadBackClient(system, config.getString("read-backend.interface"))

  val routes = {
    logRequestResult("i-have-money-read-frontend") {
      pathPrefix("money" / Segment) { fortuneId ⇒
        get {
          complete {
            val queryId = QueryId(UUID.randomUUID.toString)
            readBack.query(MoneyBalance(queryId, FortuneId(fortuneId)))
              .recover {
                case timeout: AskTimeoutException ⇒
                  RequestTimeout → QueryFailed(queryId, s"Query $queryId timed out")
              }
              .map(kv ⇒ kv._1 → conversions.toFrontendFormat(kv._2))
          }
        }
      }
    }
  }

  Http().bindAndHandle(routes, config.getString("app.host"), config.getInt("app.http-port"))
}
