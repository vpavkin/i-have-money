package ru.pavkin.ihavemoney.domain

import io.funcqrs.DomainCommand

case class CommandEnvelope(aggregateId: String, command: DomainCommand)
