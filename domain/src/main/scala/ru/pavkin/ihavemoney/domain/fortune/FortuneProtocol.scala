package ru.pavkin.ihavemoney.domain.fortune

import java.time.OffsetDateTime
import java.util.UUID

import io.funcqrs._

case class FortuneId(value: String) extends AggregateId
object FortuneId {
  def fromString(aggregateId: String): FortuneId = FortuneId(aggregateId)
  def generate(): FortuneId = FortuneId(UUID.randomUUID().toString)
}

object FortuneProtocol extends ProtocolLike {

  case class ExpenseCategory(name: String)
  case class IncomeCategory(name: String)

  /*-------------------Commands---------------------*/
  sealed trait FortuneCommand extends ProtocolCommand

  sealed trait FortuneLifecycleCommand extends FortuneCommand
  case class Spend(amount: BigDecimal,
                   currency: String,
                   category: ExpenseCategory,
                   comment: Option[String] = None) extends FortuneLifecycleCommand
  case class ReceiveIncome(amount: BigDecimal,
                           currency: String,
                           category: IncomeCategory,
                           comment: Option[String] = None) extends FortuneLifecycleCommand

  /*-------------------Events---------------------*/
  sealed trait FortuneEvent extends ProtocolEvent with MetadataFacet[FortuneMetadata]

  case class FortuneIncreased(amount: BigDecimal,
                              currency: String,
                              category: IncomeCategory,
                              metadata: FortuneMetadata,
                              comment: Option[String] = None) extends FortuneEvent
  case class FortuneSpent(amount: BigDecimal,
                          currency: String,
                          category: ExpenseCategory,
                          metadata: FortuneMetadata,
                          comment: Option[String] = None) extends FortuneEvent

  /*-------------------Metadata---------------------*/
  case class FortuneMetadata(aggregateId: FortuneId,
                             commandId: CommandId,
                             eventId: EventId = EventId(),
                             date: OffsetDateTime = OffsetDateTime.now(),
                             tags: Set[Tag] = Set()) extends Metadata with JavaTime {
    type Id = FortuneId
  }
}
