package ru.pavkin.ihavemoney.readback

import ru.pavkin.ihavemoney.domain.fortune.{Currency, FortuneId}

import scala.concurrent.{ExecutionContext, Future}

trait MoneyViewRepository {

  def findAll(id: FortuneId)(implicit ec: ExecutionContext): Future[Map[Currency, BigDecimal]]
  def find(id: FortuneId, currency: Currency)(implicit ec: ExecutionContext): Future[Option[BigDecimal]]
  def updateById(id: FortuneId, currency: Currency, newAmount: BigDecimal)(implicit ec: ExecutionContext): Future[Unit]
  def insert(id: FortuneId, currency: Currency, amount: BigDecimal)(implicit ec: ExecutionContext): Future[Unit]
}
