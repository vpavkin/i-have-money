package ru.pavkin.ihavemoney.serialization

import java.time.OffsetDateTime

import com.trueaccord.scalapb.GeneratedMessageCompanion
import ru.pavkin.ihavemoney.domain.CommandEnvelope
import ru.pavkin.ihavemoney.domain.fortune.{Currency, FortuneId}
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol._
import ru.pavkin.ihavemoney.proto.commands.{PBCommandEnvelope, PBReceiveIncome, PBSpend}
import ru.pavkin.ihavemoney.proto.events.{PBFortuneIncreased, PBFortuneSpent, PBMetadata}
import ru.pavkin.utils.option._
import ProtobufSuite.syntax._
import ru.pavkin.ihavemoney.proto.commands.PBCommandEnvelope.Command.{Command1, Command2, Empty}

object implicits {
  def deserializeFortuneMetadata(m: PBMetadata): FortuneMetadata =
    MetadataSerialization.deserialize[FortuneMetadata, FortuneId](FortuneMetadata, FortuneId(_), OffsetDateTime.parse)(m)

  implicit val fortuneIncreasedSuite: ProtobufSuite[FortuneIncreased, PBFortuneIncreased] =
    new ProtobufSuite[FortuneIncreased, PBFortuneIncreased] {
      def encode(m: FortuneIncreased): PBFortuneIncreased = PBFortuneIncreased(
        m.amount.toString,
        m.currency.code,
        m.category.name,
        Some(MetadataSerialization.serialize(m.metadata)),
        m.comment.getOrElse(""))
      def decode(p: PBFortuneIncreased): FortuneIncreased = FortuneIncreased(
        BigDecimal(p.amount),
        Currency.unsafeFromCode(p.currency),
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
        m.currency.code,
        m.category.name,
        Some(MetadataSerialization.serialize(m.metadata)),
        m.comment.getOrElse(""))
      def decode(p: PBFortuneSpent): FortuneSpent = FortuneSpent(
        BigDecimal(p.amount),
        Currency.unsafeFromCode(p.currency),
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
        m.currency.code,
        m.category.name,
        m.comment.getOrElse("")
      )

      def decode(p: PBReceiveIncome): ReceiveIncome = ReceiveIncome(
        BigDecimal(p.amount),
        Currency.unsafeFromCode(p.currency),
        IncomeCategory(p.category),
        notEmpty(p.comment)
      )
      def companion = PBReceiveIncome
    }

  implicit val spendSuite: ProtobufSuite[Spend, PBSpend] =
    new ProtobufSuite[Spend, PBSpend] {
      def encode(m: Spend): PBSpend = PBSpend(
        m.amount.toString,
        m.currency.code,
        m.category.name,
        m.comment.getOrElse("")
      )

      def decode(p: PBSpend): Spend = Spend(
        BigDecimal(p.amount),
        Currency.unsafeFromCode(p.currency),
        ExpenseCategory(p.category),
        notEmpty(p.comment)
      )
      def companion = PBSpend
    }

  implicit val commandEnvelopeSuite: ProtobufSuite[CommandEnvelope, PBCommandEnvelope] =
    new ProtobufSuite[CommandEnvelope, PBCommandEnvelope] {
      def encode(m: CommandEnvelope): PBCommandEnvelope = PBCommandEnvelope(
        m.aggregateId,
        m.command match {
          case f: FortuneCommand ⇒ f match {
            case s: ReceiveIncome ⇒ PBCommandEnvelope.Command.Command1(s.encode)
            case s: Spend ⇒ PBCommandEnvelope.Command.Command2(s.encode)
          }
          case other ⇒ throw new Exception(s"Unknown domain command ${other.getClass.getName}")
        }
      )
      def decode(p: PBCommandEnvelope): CommandEnvelope = CommandEnvelope(
        p.aggregateId,
        p.command match {
          case Empty => throw new Exception(s"Received empty command envelope")
          case Command1(value) => value.decode
          case Command2(value) => value.decode
        }
      )
      def companion: GeneratedMessageCompanion[PBCommandEnvelope] = PBCommandEnvelope
    }
}
