package ru.pavkin.ihavemoney.writeback

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import io.funcqrs.akka.backend.AkkaBackend
import ru.pavkin.ihavemoney.domain.errors.DomainError
import ru.pavkin.ihavemoney.domain._
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol.FortuneCommand
import ru.pavkin.ihavemoney.domain.fortune.{Fortune, FortuneId}
import ru.pavkin.ihavemoney.proto.results.{CommandSuccess, InvalidCommand, UnexpectedFailure, UnknownCommand}

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
            case Success(_) ⇒ origin ! CommandSuccess(c.id.value.toString)
            case Failure(e: DomainError) ⇒ origin ! InvalidCommand(c.id.value.toString, e.message)
            case Failure(e) ⇒ origin ! UnexpectedFailure(c.id.value.toString, e.getMessage)
          }
        case other ⇒ sender ! UnknownCommand(other.getClass.getName)
      }
  }
}
