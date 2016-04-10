package ru.pavkin.ihavemoney.readback

import akka.actor.ActorLogging
import akka.persistence.jdbc.query.journal.scaladsl.JdbcReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.scaladsl.Sink
import ru.pavkin.ihavemoney.readback.JournalPuller.Pull

import scala.concurrent.Future
import scala.concurrent.duration._

object JournalPuller {
  private case object Pull
}

class JournalPuller(tag: String,
                    initialOffset: Long,
                    pullFrequency: FiniteDuration = 1.second,
                    maxBatchSize: Int = 1000) extends ActorPublisher[EventEnvelope] with ActorLogging {

  implicit val dispatcher = context.system.dispatcher
  implicit val materializer = ActorMaterializer()

  private lazy val journal = PersistenceQuery(context.system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

  private var buffer = Vector.empty[EventEnvelope]
  private var nextOffset: Long = initialOffset

  override def preStart(): Unit = scheduleNextPull

  def receive: Receive = {
    case _: Request ⇒
      deliverIfRequested()

    case Pull ⇒
      pull.onComplete { _ ⇒
        scheduleNextPull
        deliverIfRequested()
      }

    case Cancel ⇒ context.stop(self)
  }

  def pull: Future[Unit] =
    journal.currentEventsByTag(tag, nextOffset)
      .take(maxBatchSize.toLong)
      .grouped(maxBatchSize)
      .runWith(Sink.foreach[Seq[EventEnvelope]] { seq ⇒
        log.info(s"Appending ${seq.length} events to internal buffer")
        buffer ++= seq
        nextOffset = seq.last.offset
      })
      .map(_ ⇒ ())

  def scheduleNextPull = context.system.scheduler.scheduleOnce(pullFrequency, self, Pull)

  def deliverIfRequested(): Unit =
    if (buffer.nonEmpty && totalDemand > 0) {
      if (buffer.size == 1) {
        // optimize for this common case
        onNext(buffer.head)
        log.info("Pushing 1 event to consumer")
        buffer = Vector.empty
      } else if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buffer.splitAt(totalDemand.toInt)
        buffer = keep
        log.info(s"Pushing ${use.size} events to consumer")
        use foreach onNext
      } else {
        log.info(s"Pushing ${buffer.size} events to consumer")
        buffer foreach onNext
        buffer = Vector.empty
      }
    }
}
