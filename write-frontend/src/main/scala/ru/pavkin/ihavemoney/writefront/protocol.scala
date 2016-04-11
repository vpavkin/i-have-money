package ru.pavkin.ihavemoney.writefront

import cats.data.Xor
import io.circe.Decoder
import ru.pavkin.ihavemoney.domain.fortune.Currency

object protocol {

  implicit val decodeCurrency: Decoder[Currency] =
    Decoder.decodeString.emap(s ⇒ Currency.fromCode(s) match {
      case Some(c) ⇒ Xor.Right(c)
      case None ⇒ Xor.Left(s"$s is not a valid currency")
    })

  case class ReceiveIncomeRequest(amount: BigDecimal,
                                  currency: Currency,
                                  category: String,
                                  comment: Option[String] = None)
  case class SpendRequest(amount: BigDecimal,
                          currency: Currency,
                          category: String,
                          comment: Option[String] = None)

  case class RequestResult(commandId: String, success: Boolean, error: Option[String] = None)
}
