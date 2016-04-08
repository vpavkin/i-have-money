package ru.pavkin.ihavemoney.writeback

import akka.cluster.sharding.ShardRegion
import ru.pavkin.ihavemoney.domain.CommandEnvelope

class CommandMessageExtractor(shardsNumber: Int) extends ShardRegion.HashCodeMessageExtractor(shardsNumber) {
  def entityId(message: Any): String = message match {
    case CommandEnvelope(id, _) ⇒ id
    case _ ⇒ null
  }
}
