package ru.pavkin.ihavemoney.writefront

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.CirceSupport
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol.{ExpenseCategory, IncomeCategory, ReceiveIncome, Spend}
import ru.pavkin.ihavemoney.protocol.writefront._
import ch.megard.akka.http.cors.{CorsDirectives, CorsSettings}
import akka.http.scaladsl.model.StatusCodes._
import scala.concurrent.duration._

object WriteFrontend extends App with CirceSupport with CorsDirectives {

  implicit val system = ActorSystem("IHaveMoneyWriteFront")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(30.seconds)

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val writeBack = new WriteBackClusterClient(system)

  val routes: Route =
    cors(CorsSettings.defaultSettings.copy(allowCredentials = false)) {
      logRequestResult("i-have-money-write-frontend") {
        pathPrefix("fortune" / Segment) { fortuneId ⇒
          (path("income") & post & entity(as[ReceiveIncomeRequest])) { req ⇒
            complete {
              writeBack.sendCommand(fortuneId, ReceiveIncome(
                req.amount,
                req.currency,
                IncomeCategory(req.category),
                req.comment
              ))
            }
          } ~ (path("spend") & post & entity(as[SpendRequest])) { req ⇒
            complete {
              writeBack.sendCommand(fortuneId, Spend(
                req.amount,
                req.currency,
                ExpenseCategory(req.category),
                req.comment
              ))
            }
          }
        } ~ complete(NotFound)
      }
    }

  Http().bindAndHandle(routes, config.getString("app.host"), config.getInt("app.http-port"))
}
