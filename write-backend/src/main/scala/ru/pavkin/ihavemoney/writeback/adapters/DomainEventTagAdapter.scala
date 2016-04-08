package ru.pavkin.ihavemoney.writeback.adapters

import akka.persistence.journal.Tagged
import io.funcqrs.Metadata

trait DomainEventTagAdapter {

  def tag(event: Any, metadata: Metadata): Any =
    if (metadata.tags.nonEmpty)
      Tagged(event, metadata.tags.map(_.value))
    else event
}
