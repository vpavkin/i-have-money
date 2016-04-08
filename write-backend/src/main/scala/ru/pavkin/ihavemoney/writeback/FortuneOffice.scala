package ru.pavkin.ihavemoney.writeback

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import io.funcqrs.akka.backend.AkkaBackend
import ru.pavkin.ihavemoney.domain.errors.DomainError
import ru.pavkin.ihavemoney.domain._
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol.FortuneCommand
import ru.pavkin.ihavemoney.domain.fortune.{Fortune, FortuneId}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class FortuneOffice(backend: AkkaBackend)(implicit val timeout: Timeout) extends Actor with ActorLogging {

  implicit val dispatcher: ExecutionContext = context.system.dispatcher

  def receive: Receive = {
    case CommandEnvelope(id, command) ⇒
      val origin = sender
      command match {
        case c: FortuneCommand ⇒
          val aggregate = backend.aggregateRef[Fortune](FortuneId(id))
          (aggregate ? c).onComplete {
            case Success(_) ⇒ origin ! CommandSuccess(c.id)
            case Failure(e: DomainError) ⇒ origin ! InvalidCommand(c.id, e)
            case Failure(e) ⇒ origin ! UnexpectedFailure(c.id, e)
          }
        case other ⇒ sender ! UnknownCommandMessage
      }
  }
}
