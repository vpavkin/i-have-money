package ru.pavkin.ihavemoney.serialization.adapters

import akka.persistence.journal.{EventAdapter, EventSeq}
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol._
import ru.pavkin.ihavemoney.proto.events._
import ru.pavkin.ihavemoney.serialization.ProtobufSuite.syntax._
import ru.pavkin.ihavemoney.serialization.implicits._

class FortuneEventAdapter extends EventAdapter with DomainEventTagAdapter {
  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = event match {
    case m: FortuneIncreased ⇒ tag(m.encode, m.metadata)
    case m: FortuneSpent ⇒ tag(m.encode, m.metadata)
    case _ ⇒ event
  }

  override def fromJournal(event: Any, manifest: String): EventSeq = event match {
    case p: PBFortuneIncreased ⇒ EventSeq.single(p.decode)
    case p: PBFortuneSpent ⇒ EventSeq.single(p.decode)
    case _ ⇒ EventSeq.single(event)
  }
}
