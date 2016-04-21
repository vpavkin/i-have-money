package ru.pavkin.ihavemoney.domain.query

import ru.pavkin.ihavemoney.domain.fortune.{Currency, FortuneId}

sealed trait QueryResult

case class MoneyBalanceQueryResult(id: FortuneId, balance: Map[Currency, BigDecimal]) extends QueryResult

case class EntityNotFound(id: QueryId, error: String) extends QueryResult
case class QueryFailed(id: QueryId, error: String) extends QueryResult
