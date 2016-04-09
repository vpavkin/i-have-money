package ru.pavkin.ihavemoney.serialization

import java.util.UUID

import io.funcqrs._
import ru.pavkin.ihavemoney.proto.events.PBMetadata

object MetadataSerialization {
  def serialize(m: Metadata with JavaTime): PBMetadata = PBMetadata(
    m.aggregateId.value,
    m.commandId.value.toString,
    m.eventId.value.toString,
    m.date.toString,
    m.tags.map(_.value).toSeq
  )

  def deserialize[M <: Metadata, Id](constructor: (Id, CommandId, EventId, M#DateTime, Set[Tag]) ⇒ M,
                                     idConstructor: String ⇒ Id,
                                     dateConstructor: String ⇒ M#DateTime)
                                    (m: PBMetadata): M = constructor(
    idConstructor(m.aggregateId),
    CommandId(UUID.fromString(m.commandId)),
    EventId(UUID.fromString(m.eventId)),
    dateConstructor(m.timestamp),
    m.tags.toSet.map(Tags.aggregateTag)
  )
}
