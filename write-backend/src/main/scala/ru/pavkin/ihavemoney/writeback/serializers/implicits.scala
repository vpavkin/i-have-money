package ru.pavkin.ihavemoney.writeback.serializers

import java.time.OffsetDateTime

import ru.pavkin.ihavemoney.domain.fortune.FortuneId
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol._
import ru.pavkin.ihavemoney.proto.commands.{PBReceiveIncome, PBSpend}
import ru.pavkin.ihavemoney.proto.events.{PBFortuneIncreased, PBFortuneSpent, PBMetadata}
import ru.pavkin.utils.option._

object implicits {
  def deserializeFortuneMetadata(m: PBMetadata): FortuneMetadata =
    MetadataSerialization.deserialize[FortuneMetadata, FortuneId](FortuneMetadata, FortuneId(_), OffsetDateTime.parse)(m)

  implicit val fortuneIncreasedSuite: ProtobufSuite[FortuneIncreased, PBFortuneIncreased] =
    new ProtobufSuite[FortuneIncreased, PBFortuneIncreased] {
      def encode(m: FortuneIncreased): PBFortuneIncreased = PBFortuneIncreased(
        m.amount.toString,
        m.currency,
        m.category.name,
        Some(MetadataSerialization.serialize(m.metadata)),
        m.comment.getOrElse(""))
      def decode(p: PBFortuneIncreased): FortuneIncreased = FortuneIncreased(
        BigDecimal(p.amount),
        p.currency,
        IncomeCategory(p.category),
        deserializeFortuneMetadata(p.metadata.get),
        notEmpty(p.comment)
      )
      def companion = PBFortuneIncreased
    }

  implicit val fortuneSpentSuite: ProtobufSuite[FortuneSpent, PBFortuneSpent] =
    new ProtobufSuite[FortuneSpent, PBFortuneSpent] {
      def encode(m: FortuneSpent): PBFortuneSpent = PBFortuneSpent(
        m.amount.toString,
        m.currency,
        m.category.name,
        Some(MetadataSerialization.serialize(m.metadata)),
        m.comment.getOrElse(""))
      def decode(p: PBFortuneSpent): FortuneSpent = FortuneSpent(
        BigDecimal(p.amount),
        p.currency,
        ExpenseCategory(p.category),
        deserializeFortuneMetadata(p.metadata.get),
        notEmpty(p.comment)
      )
      def companion = PBFortuneSpent
    }

  implicit val receiveIncomeSuite: ProtobufSuite[ReceiveIncome, PBReceiveIncome] =
    new ProtobufSuite[ReceiveIncome, PBReceiveIncome] {
      def encode(m: ReceiveIncome): PBReceiveIncome = PBReceiveIncome(
        m.amount.toString,
        m.currency,
        m.category.name,
        m.comment.getOrElse("")
      )

      def decode(p: PBReceiveIncome): ReceiveIncome = ReceiveIncome(
        BigDecimal(p.amount),
        p.currency,
        IncomeCategory(p.category),
        notEmpty(p.comment)
      )
      def companion = PBReceiveIncome
    }

  implicit val spendSuite: ProtobufSuite[Spend, PBSpend] =
    new ProtobufSuite[Spend, PBSpend] {
      def encode(m: Spend): PBSpend = PBSpend(
        m.amount.toString,
        m.currency,
        m.category.name,
        m.comment.getOrElse("")
      )

      def decode(p: PBSpend): Spend = Spend(
        BigDecimal(p.amount),
        p.currency,
        ExpenseCategory(p.category),
        notEmpty(p.comment)
      )
      def companion = PBSpend
    }

  object identity {
    implicit val fortuneIncreasedPBSuite: ProtobufSuite[PBFortuneIncreased, PBFortuneIncreased] =
      ProtobufSuite.identity(PBFortuneIncreased)

    implicit val fortuneSpentPBSuite: ProtobufSuite[PBFortuneSpent, PBFortuneSpent] =
      ProtobufSuite.identity(PBFortuneSpent)
  }
}
