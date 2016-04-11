package ru.pavkin.ihavemoney.readback.interface

import ru.pavkin.ihavemoney.domain.fortune.{Currency, FortuneId}

sealed trait QueryResult

case class MoneyBalanceQueryResult(id: FortuneId, balance: Map[Currency, BigDecimal]) extends QueryResult

case class QueryFailed(id: QueryId, reason: String) extends QueryResult
