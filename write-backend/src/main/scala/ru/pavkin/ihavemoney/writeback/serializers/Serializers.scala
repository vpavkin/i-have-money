package ru.pavkin.ihavemoney.writeback.serializers

import ru.pavkin.ihavemoney.proto.events.{PBFortuneIncreased, PBFortuneSpent}
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol._
import implicits._
import implicits.identity._
import ru.pavkin.ihavemoney.proto.commands.{PBReceiveIncome, PBSpend}

class ReceiveIncomeSerializer extends ProtobufSerializer[ReceiveIncome, PBReceiveIncome](100)
class SpendSerializer extends ProtobufSerializer[Spend, PBSpend](101)

class PBFortuneIncreasedSerializer extends ProtobufSerializer[PBFortuneIncreased, PBFortuneIncreased](200)
class PBFortuneSpentSerializer extends ProtobufSerializer[PBFortuneSpent, PBFortuneSpent](201)
