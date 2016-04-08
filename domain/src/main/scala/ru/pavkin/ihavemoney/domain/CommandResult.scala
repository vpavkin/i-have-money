package ru.pavkin.ihavemoney.domain

import io.funcqrs.CommandId
import ru.pavkin.ihavemoney.domain.errors.DomainError

sealed trait CommandResult

case class CommandSuccess(id: CommandId) extends CommandResult
case object UnknownCommandMessage extends CommandResult
case class InvalidCommand(id: CommandId, reason: DomainError) extends CommandResult
case class UnexpectedFailure(id: CommandId, reason: Throwable) extends CommandResult
