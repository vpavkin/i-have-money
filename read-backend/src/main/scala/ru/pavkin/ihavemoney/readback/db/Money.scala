package ru.pavkin.ihavemoney.readback.db

import ru.pavkin.ihavemoney.domain.fortune.{Currency, FortuneId}
import slick.driver.PostgresDriver.api._
import slick.jdbc.GetResult
import slick.lifted.{Rep, TableQuery, Tag}

case class MoneyRow(fortuneId: FortuneId, currency: Currency, amount: BigDecimal)

class Money(tableTag: Tag) extends Table[MoneyRow](tableTag, "money") {
  def * = (fortuneId, currency, amount) <>(
    (t: (String, String, BigDecimal)) ⇒
      MoneyRow(FortuneId(t._1), Currency.unsafeFromCode(t._2), t._3),
    (m: MoneyRow) ⇒ Some((m.fortuneId.value, m.currency.code, m.amount))
    )

  val fortuneId: Rep[String] = column[String]("fortune_id")
  val currency: Rep[String] = column[String]("currency")
  val amount: Rep[BigDecimal] = column[BigDecimal]("amount")

  def pk = primaryKey("money_pkey", (fortuneId, currency))
}

object Money {

  implicit def GetResultMoneyRow(implicit
                                 e0: GetResult[String],
                                 e1: GetResult[BigDecimal]): GetResult[MoneyRow] = GetResult {
    prs => import prs._
      MoneyRow(FortuneId(<<[String]), Currency.unsafeFromCode(<<[String]), <<[BigDecimal])
  }

  lazy val table = new TableQuery(tag ⇒ new Money(tag))
}
