package ru.pavkin.ihavemoney.readback

import akka.actor.Actor
import ru.pavkin.ihavemoney.domain.query._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class InterfaceActor(moneyRepo: MoneyViewRepository) extends Actor {
  implicit val dispatcher: ExecutionContext = context.system.dispatcher

  def receive: Receive = {
    case q: Query ⇒
      val origin = sender
      val queryFuture: Future[QueryResult] = q match {
        case MoneyBalance(_, fortuneId) =>
          moneyRepo.findAll(fortuneId).map {
            case m if m.isEmpty ⇒ EntityNotFound(q.id, s"Fortune $fortuneId not found")
            case m ⇒ MoneyBalanceQueryResult(fortuneId, m)
          }
      }
      queryFuture.onComplete {
        case Success(r) ⇒ origin ! r
        case Failure(ex) ⇒ origin ! QueryFailed(q.id, ex.getMessage)
      }
  }
}
