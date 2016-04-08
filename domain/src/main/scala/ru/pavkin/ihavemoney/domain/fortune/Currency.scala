package ru.pavkin.ihavemoney.domain.fortune

import cats.Eq
import ru.pavkin.ihavemoney.domain._

sealed trait Currency {
  self =>
  def code: String = self.toString
}

object Currency {

  case object USD extends Currency
  case object RUR extends Currency
  case object EUR extends Currency

  def unsafeFromCode(code: String): Currency = fromCode(code).getOrElse(unexpected)
  def fromCode(code: String): Option[Currency] = all.find(_.code == code)

  lazy val all: Set[Currency] = Set(USD, RUR, EUR)

  implicit val eq: Eq[Currency] = new Eq[Currency] {
    def eqv(x: Currency, y: Currency): Boolean = x == y
  }
}

