package ru.pavkin.ihavemoney.domain

case class CommandEnvelope(entityId: String, command: AnyRef)
