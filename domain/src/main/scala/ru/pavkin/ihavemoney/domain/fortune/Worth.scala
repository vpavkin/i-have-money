package ru.pavkin.ihavemoney.domain.fortune

import cats.Eq
import cats.data.Xor
import ru.pavkin.ihavemoney.domain.errors.NegativeWorth

case class Worth[C <: Currency](amount: BigDecimal, currency: C) {
  def +(other: Worth[C]): Worth[C] = copy(amount = amount + other.amount)
  def *(by: BigDecimal): Worth[C] = copy(amount = amount * by)
  def -(other: Worth[C]): Xor[NegativeWorth.type, Worth[C]] = if (amount < other.amount)
    Xor.left(NegativeWorth)
  else
    Xor.right(copy(amount - other.amount))

  override def toString: String = f"$amount%1.2f ${currency.code}"
}

object Worth {
  implicit def eq[C <: Currency]: Eq[Worth[C]] = new Eq[Worth[C]] {
    def eqv(x: Worth[C], y: Worth[C]): Boolean = x.amount.equals(y.amount)
  }
  implicit def ord[C <: Currency]: Ordering[Worth[C]] = Ordering.by(_.amount)

  def unsafeFrom(amount: BigDecimal, currency: String) = Worth(amount, Currency.unsafeFromCode(currency))
}
