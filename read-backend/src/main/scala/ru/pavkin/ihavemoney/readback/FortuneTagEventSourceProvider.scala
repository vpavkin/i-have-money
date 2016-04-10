package ru.pavkin.ihavemoney.readback

import akka.NotUsed
import akka.actor.{ActorContext, Props}
import akka.persistence.query.EventEnvelope
import akka.stream.scaladsl.Source
import io.funcqrs.Tag
import io.funcqrs.akka.EventsSourceProvider
import ru.pavkin.ihavemoney.proto.events.{PBFortuneIncreased, PBFortuneSpent}
import ru.pavkin.ihavemoney.serialization.ProtobufSuite.syntax._
import ru.pavkin.ihavemoney.serialization.implicits._

class FortuneTagEventSourceProvider(tag: Tag) extends EventsSourceProvider {

  def source(offset: Long)(implicit context: ActorContext): Source[EventEnvelope, NotUsed] =
    Source.actorPublisher[EventEnvelope](Props(new JournalPuller(tag.value, offset)))
      .mapMaterializedValue(_ ⇒ NotUsed)
      .map {
        case e: EventEnvelope ⇒ e.event match {
          case p: PBFortuneIncreased ⇒ e.copy(event = p.decode)
          case p: PBFortuneSpent ⇒ e.copy(event = p.decode)
          case p ⇒ e
        }
      }
}
