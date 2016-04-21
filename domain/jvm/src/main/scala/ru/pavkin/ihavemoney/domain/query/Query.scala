package ru.pavkin.ihavemoney.domain.query

import ru.pavkin.ihavemoney.domain.fortune.FortuneId

case class QueryId(value: String)

sealed trait Query {
  val id: QueryId
}

case class MoneyBalance(id: QueryId, fortuneId: FortuneId) extends Query
