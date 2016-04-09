package ru.pavkin.ihavemoney.serialization

import ru.pavkin.ihavemoney.domain.CommandEnvelope
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol._
import ru.pavkin.ihavemoney.proto.commands.{PBCommandEnvelope, PBReceiveIncome, PBSpend}
import ru.pavkin.ihavemoney.serialization.implicits._

class CommandEnvelopeSerializer extends ProtobufSerializer[CommandEnvelope, PBCommandEnvelope](100)
class ReceiveIncomeSerializer extends ProtobufSerializer[ReceiveIncome, PBReceiveIncome](101)
class SpendSerializer extends ProtobufSerializer[Spend, PBSpend](102)
