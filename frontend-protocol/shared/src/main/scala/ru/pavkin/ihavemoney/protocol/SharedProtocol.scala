package ru.pavkin.ihavemoney.protocol

import cats.data.Xor
import io.circe.{Decoder, Encoder, Json}
import ru.pavkin.ihavemoney.domain.fortune.Currency

trait SharedProtocol {

  implicit val decodeCurrency: Decoder[Currency] =
    Decoder.decodeString.emap(s ⇒ Currency.fromCode(s) match {
      case Some(c) ⇒ Xor.Right(c)
      case None ⇒ Xor.Left(s"$s is not a valid currency")
    })

  implicit val encodeCurrency: Encoder[Currency] =
    Encoder.instance(c ⇒ Json.string(c.code))

}
