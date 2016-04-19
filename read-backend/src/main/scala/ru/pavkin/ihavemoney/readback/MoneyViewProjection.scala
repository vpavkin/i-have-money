package ru.pavkin.ihavemoney.readback

import io.funcqrs.{HandleEvent, Projection}
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol._
import ru.pavkin.ihavemoney.domain.fortune.{Currency, FortuneId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MoneyViewProjection(repo: MoneyViewRepository) extends Projection {

  private def adjustFortune(id: FortuneId, currency: Currency, op: Option[BigDecimal] ⇒ BigDecimal): Future[Unit] = {
    repo.find(id, currency).flatMap {
      case Some(amount) ⇒ repo.updateById(id, currency, op(Some(amount)))
      case None ⇒ repo.insert(id, currency, op(None))
    }
  }

  def handleEvent: HandleEvent = {

    case e: FortuneIncreased =>
      adjustFortune(e.aggregateId, e.currency, _.getOrElse(BigDecimal(0.0)) + e.amount)
    case e: FortuneSpent =>
      adjustFortune(e.aggregateId, e.currency, _.getOrElse(BigDecimal(0.0)) - e.amount)
  }
}
