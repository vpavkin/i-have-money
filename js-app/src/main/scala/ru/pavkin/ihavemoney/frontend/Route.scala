package ru.pavkin.ihavemoney.frontend

sealed trait Route

object Route {
  case object AddTransactions extends Route
  case object BalanceView extends Route
}
