package ru.pavkin.ihavemoney.readback

import akka.actor.Actor
import akka.util.Timeout
import ru.pavkin.ihavemoney.readback.interface._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class InterfaceActor(moneyRepo: MoneyRepo) extends Actor {
  implicit val dispatcher: ExecutionContext = context.system.dispatcher

  def receive: Receive = {
    case q: Query ⇒
      val origin = sender
      val queryFuture: Future[QueryResult] = q match {
        case MoneyBalance(_, fortuneId) =>
          moneyRepo.findAll(fortuneId).map {
            case m if m.isEmpty ⇒ QueryFailed(q.id, s"Fortune $fortuneId not found")
            case m ⇒ MoneyBalanceQueryResult(fortuneId, m)
          }
      }
      queryFuture.onComplete {
        case Success(r) ⇒ origin ! r
        case Failure(ex) ⇒ origin ! QueryFailed(q.id, ex.getMessage)
      }
  }
}
