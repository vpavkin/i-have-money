package ru.pavkin.ihavemoney.readfront

import io.circe._
import io.circe.generic.semiauto._

object protocol {
  sealed trait FrontendQueryResult
  case class FrontendMoneyBalance(fortuneId: String, balances: Map[String, BigDecimal]) extends FrontendQueryResult
  case class FrontendQueryFailed(id: String, error: String) extends FrontendQueryResult
  case class FrontendEntityNotFound(entityId: String, error: String) extends FrontendQueryResult

  implicit val encoder: Encoder[FrontendQueryResult] = deriveEncoder[FrontendQueryResult]
}
