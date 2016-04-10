package ru.pavkin.ihavemoney.readback

import ru.pavkin.ihavemoney.domain.fortune.{Currency, FortuneId}
import ru.pavkin.ihavemoney.readback.db.{Money, MoneyRow}
import slick.driver.PostgresDriver.api.{Database, _}

import scala.concurrent.{ExecutionContext, Future}

class MoneyRepo(db: Database) {

  private def findQuery(id: FortuneId, currency: Currency): Query[Money, MoneyRow, Seq] =
    Money.table
      .filter(money ⇒ money.fortuneId === id.value && money.currency === currency.code)

  def findAll(id: FortuneId)(implicit ec: ExecutionContext): Future[Map[Currency, BigDecimal]] = db.run {
    Money.table.filter(_.fortuneId === id.value).result
  }.map(_.map(a ⇒ a.currency → a.amount).toMap)

  def find(id: FortuneId, currency: Currency)(implicit ec: ExecutionContext): Future[Option[BigDecimal]] = db.run {
    findQuery(id, currency)
      .take(1)
      .map(_.amount)
      .result
  }.map(_.headOption)

  def updateById(id: FortuneId, currency: Currency, newAmount: BigDecimal)(implicit ec: ExecutionContext): Future[Unit] = db.run {
    findQuery(id, currency).map(_.amount).update(newAmount)
  }.map(_ ⇒ ())

  def insert(id: FortuneId, currency: Currency, amount: BigDecimal)(implicit ec: ExecutionContext): Future[Unit] = db.run {
    Money.table += MoneyRow(id, currency, amount)
  }.map(_ ⇒ ())

}
