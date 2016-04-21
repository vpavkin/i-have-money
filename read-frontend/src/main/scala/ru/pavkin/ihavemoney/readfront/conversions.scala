package ru.pavkin.ihavemoney.readfront

import ru.pavkin.ihavemoney.domain.query.{EntityNotFound, MoneyBalanceQueryResult, QueryFailed, QueryResult}
import ru.pavkin.ihavemoney.protocol.readfront._

object conversions {

  def toFrontendFormat(qr: QueryResult): FrontendQueryResult = qr match {
    case MoneyBalanceQueryResult(id, balance) =>
      FrontendMoneyBalance(id.value, balance.map(kv ⇒ kv._1.code → kv._2))
    case EntityNotFound(id, error) =>
      FrontendEntityNotFound(id.value, error)
    case QueryFailed(id, error) =>
      FrontendQueryFailed(id.value, error)
  }
}
