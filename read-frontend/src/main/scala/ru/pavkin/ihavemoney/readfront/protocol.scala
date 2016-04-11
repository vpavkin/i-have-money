package ru.pavkin.ihavemoney.readfront

import java.util.UUID

import io.circe.{Encoder, Json}
import io.circe.syntax._
import ru.pavkin.ihavemoney.domain.fortune.Currency

object protocol {

  implicit val moneyBalanceEncoder: Encoder[Map[Currency, BigDecimal]] = Encoder.instance(
    m ⇒ Json.obj(m.map(kv ⇒ kv._1.code → kv._2.asJson).toSeq: _*)
  )
}
