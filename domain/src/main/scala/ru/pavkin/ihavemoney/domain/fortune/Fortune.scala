package ru.pavkin.ihavemoney.domain.fortune

import io.funcqrs._
import io.funcqrs.behavior._
import ru.pavkin.ihavemoney.domain.errors.BalanceIsNotEnough

case class Fortune(id: FortuneId, balances: Map[Currency, BigDecimal]) extends AggregateLike {
  type Id = FortuneId
  type Protocol = FortuneProtocol.type

  def increase[C <: Currency](worth: Worth[C]): Fortune =
    copy(balances = balances + (worth.currency -> (amount(worth.currency) + worth.amount)))

  def decrease[C <: Currency](by: Worth[C]): Fortune =
    copy(balances = balances + (by.currency -> (amount(by.currency) - by.amount)))

  def worth[C <: Currency](currency: C): Worth[C] = Worth(amount(currency), currency)
  def amount[C <: Currency](currency: C): BigDecimal = balances.getOrElse(currency, BigDecimal(0.0))

  import FortuneProtocol._

  def metadata(cmd: FortuneCommand): FortuneMetadata =
    Fortune.metadata(id, cmd)

def cantHaveNegativeBalance = action[Fortune]
    .rejectCommand {
      case cmd: Spend if this.amount(cmd.currency) < cmd.amount =>
        BalanceIsNotEnough(this.amount(cmd.currency), cmd.currency)
    }

  def increaseFortune = action[Fortune]
    .handleCommand {
      cmd: ReceiveIncome => Fortune.handleReceiveIncome(id, cmd)
    }
    .handleEvent {
      evt: FortuneIncreased => this.increase(Worth(evt.amount, evt.currency))
    }

  def decreaseFortune = action[Fortune]
    .handleCommand {
      cmd: Spend => FortuneSpent(
        cmd.amount,
        cmd.currency,
        cmd.category,
        metadata(cmd),
        cmd.comment)
    }
    .handleEvent {
      evt: FortuneSpent => this.decrease(Worth(evt.amount, evt.currency))
    }
}

object Fortune {

  import FortuneProtocol._

  val tag = Tags.aggregateTag("fortune")

  def metadata(fortuneId: FortuneId, cmd: FortuneCommand) = {
    FortuneMetadata(fortuneId, cmd.id, tags = Set(tag))
  }

  def handleReceiveIncome(id: FortuneId, cmd: ReceiveIncome): FortuneIncreased = FortuneIncreased(
    cmd.amount,
    cmd.currency,
    cmd.category,
    metadata(id, cmd),
    cmd.comment)

  def createFortune(fortuneId: FortuneId) =
    actions[Fortune]
      .handleCommand {
        cmd: ReceiveIncome => Fortune.handleReceiveIncome(fortuneId, cmd)
      }
      .handleEvent {
        evt: FortuneIncreased => Fortune(id = fortuneId, balances = Map(evt.currency -> evt.amount))
      }

  def behavior(fortuneId: FortuneId): Behavior[Fortune] = {

    case Uninitialized(id) => createFortune(id)

    case Initialized(fortune) =>
      fortune.cantHaveNegativeBalance ++
        fortune.increaseFortune ++
        fortune.decreaseFortune
  }
}
