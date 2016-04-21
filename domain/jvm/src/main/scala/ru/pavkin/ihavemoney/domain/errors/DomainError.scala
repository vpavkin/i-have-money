package ru.pavkin.ihavemoney.domain.errors

import ru.pavkin.ihavemoney.domain.fortune.Currency

sealed trait DomainError extends Throwable {
  def message: String
  override def getMessage: String = message
}
case object NegativeWorth extends DomainError {
  def message = "Asset can't have negative worth"
}
case class BalanceIsNotEnough(amount: BigDecimal, currency: Currency) extends DomainError {
  def message = s"Your balance ($amount ${currency.code}) is not enough for this operation"
}
case object UnsupportedCommand extends DomainError{
  def message = s"Command is not supported"
}
