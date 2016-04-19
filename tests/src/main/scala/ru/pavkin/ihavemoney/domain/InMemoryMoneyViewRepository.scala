package ru.pavkin.ihavemoney.domain

import ru.pavkin.ihavemoney.domain.fortune.{Currency, FortuneId}
import ru.pavkin.ihavemoney.readback.MoneyViewRepository

import scala.concurrent.{ExecutionContext, Future}

class InMemoryMoneyViewRepository extends MoneyViewRepository {

  private var repo: Map[FortuneId, Map[Currency, BigDecimal]] = Map.empty

  def findAll(id: FortuneId)(implicit ec: ExecutionContext): Future[Map[Currency, BigDecimal]] = Future.successful {
    repo.getOrElse(id, Map.empty)
  }

  def find(id: FortuneId, currency: Currency)(implicit ec: ExecutionContext): Future[Option[BigDecimal]] = Future.successful {
    repo.get(id).flatMap(_.get(currency))
  }

  def updateById(id: FortuneId, currency: Currency, newAmount: BigDecimal)(implicit ec: ExecutionContext): Future[Unit] = Future.successful {
    repo = repo.updated(id, repo.getOrElse(id, Map.empty).updated(currency, newAmount))
  }

  def insert(id: FortuneId, currency: Currency, amount: BigDecimal)(implicit ec: ExecutionContext): Future[Unit] =
    updateById(id, currency, amount)
}
