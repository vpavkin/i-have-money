package ru.pavkin.ihavemoney.domain.errors

import ru.pavkin.ihavemoney.domain.fortune.Currency

sealed trait DomainError extends Throwable
case object NegativeWorth extends DomainError
case class BalanceIsNotEnough(amount: BigDecimal, currency: Currency) extends DomainError
case object UnsupportedCommand extends DomainError
