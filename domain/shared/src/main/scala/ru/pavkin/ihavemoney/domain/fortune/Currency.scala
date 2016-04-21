package ru.pavkin.ihavemoney.domain.fortune

import cats.Eq
import ru.pavkin.ihavemoney.domain._
import ru.pavkin.utils.enum._

sealed trait Currency {
  self ⇒
  def code: String = self.toString
}

object Currency {

  case object USD extends Currency
  case object RUR extends Currency
  case object EUR extends Currency

  val values: Set[Currency] = Values

  def isCurrency(code: String) = fromCode(code).isDefined
  def unsafeFromCode(code: String): Currency = fromCode(code).getOrElse(unexpected)
  def fromCode(code: String): Option[Currency] = values.find(_.code == code)

  def unapply(arg: Currency): Option[String] = Some(arg.code)

  implicit val eq: Eq[Currency] = new Eq[Currency] {
    def eqv(x: Currency, y: Currency): Boolean = x == y
  }
}

