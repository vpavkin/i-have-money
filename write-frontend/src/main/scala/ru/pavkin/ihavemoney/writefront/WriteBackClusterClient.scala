package ru.pavkin.ihavemoney.writefront

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import ru.pavkin.ihavemoney.domain.{UnexpectedFailure, _}
import ru.pavkin.ihavemoney.writefront.protocol.RequestResult
import akka.pattern.ask
import akka.util.Timeout
import io.funcqrs.CommandId
import ru.pavkin.ihavemoney.domain.errors.DomainError

import scala.concurrent.{ExecutionContext, Future}

class WriteBackClusterClient(system: ActorSystem) {

  val writeBackendClient: ActorRef = system.actorOf(
    ClusterClient.props(ClusterClientSettings(system)),
    "writeBackendClient"
  )

  def sendCommand(aggregateId: String, command: AnyRef)
                 (implicit ec: ExecutionContext, timeout: Timeout): Future[(StatusCode, RequestResult)] =
    (writeBackendClient ? ClusterClient.Send("/user/interface", CommandEnvelope(aggregateId, command), localAffinity = true))
      .mapTo[CommandResult]
      .map {
        case CommandSuccess(id) ⇒
          OK → RequestResult(id.value.toString, success = true)
        case UnknownCommandMessage ⇒
          InternalServerError → RequestResult("unassigned", success = false, Some("Unknown command message"))
        case InvalidCommand(id: CommandId, reason: DomainError) ⇒
          BadRequest → RequestResult(id.value.toString, success = false, Some(reason.toString))
        case UnexpectedFailure(id: CommandId, reason: Throwable) ⇒
          InternalServerError → RequestResult(id.value.toString, success = false, Some(reason.toString))
      }

}
