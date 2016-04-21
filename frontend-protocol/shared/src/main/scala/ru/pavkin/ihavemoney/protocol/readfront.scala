package ru.pavkin.ihavemoney.protocol

import io.circe._
import io.circe.generic.semiauto._

object readfront extends SharedProtocol {
  sealed trait FrontendQueryResult
  case class FrontendMoneyBalance(fortuneId: String, balances: Map[String, BigDecimal]) extends FrontendQueryResult
  case class FrontendQueryFailed(id: String, error: String) extends FrontendQueryResult
  case class FrontendEntityNotFound(entityId: String, error: String) extends FrontendQueryResult

  implicit val fqEncoder: Encoder[FrontendQueryResult] = deriveEncoder[FrontendQueryResult]
  implicit val fqDecoder: Decoder[FrontendQueryResult] = deriveDecoder[FrontendQueryResult]
}
