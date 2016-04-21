package ru.pavkin.ihavemoney.protocol

import io.circe._
import io.circe.generic.semiauto._
import ru.pavkin.ihavemoney.domain.fortune.Currency

object writefront extends SharedProtocol {

  sealed trait WriteFrontRequest
  case class ReceiveIncomeRequest(amount: BigDecimal,
                                  currency: Currency,
                                  category: String,
                                  comment: Option[String] = None) extends WriteFrontRequest
  case class SpendRequest(amount: BigDecimal,
                          currency: Currency,
                          category: String,
                          comment: Option[String] = None) extends WriteFrontRequest

  case class RequestResult(commandId: String, success: Boolean, error: Option[String] = None)


  implicit val riEncoder: Encoder[ReceiveIncomeRequest] = deriveEncoder[ReceiveIncomeRequest]
  implicit val riDecoder: Decoder[ReceiveIncomeRequest] = deriveDecoder[ReceiveIncomeRequest]

  implicit val sEncoder: Encoder[SpendRequest] = deriveEncoder[SpendRequest]
  implicit val sDecoder: Decoder[SpendRequest] = deriveDecoder[SpendRequest]

  implicit val reqEncoder: Encoder[WriteFrontRequest] = deriveEncoder[WriteFrontRequest]
  implicit val reqDecoder: Decoder[WriteFrontRequest] = deriveDecoder[WriteFrontRequest]

  implicit val resEncoder: Encoder[RequestResult] = deriveEncoder[RequestResult]
  implicit val resDecoder: Decoder[RequestResult] = deriveDecoder[RequestResult]

}
