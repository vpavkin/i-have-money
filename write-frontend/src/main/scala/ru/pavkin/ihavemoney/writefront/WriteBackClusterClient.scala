package ru.pavkin.ihavemoney.writefront

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.pattern.ask
import akka.util.Timeout
import io.funcqrs.DomainCommand
import ru.pavkin.ihavemoney.domain.CommandEnvelope
import ru.pavkin.ihavemoney.proto.results.{CommandSuccess, InvalidCommand, UnexpectedFailure, UnknownCommand}
import ru.pavkin.ihavemoney.writefront.protocol.RequestResult

import scala.concurrent.{ExecutionContext, Future}

class WriteBackClusterClient(system: ActorSystem) {

  val writeBackendClient: ActorRef = system.actorOf(
    ClusterClient.props(ClusterClientSettings(system)),
    "writeBackendClient"
  )

  def sendCommand(aggregateId: String, command: DomainCommand)
                 (implicit ec: ExecutionContext, timeout: Timeout): Future[(StatusCode, RequestResult)] =
    (writeBackendClient ? ClusterClient.Send("/user/interface", CommandEnvelope(aggregateId, command), localAffinity = true))
      .map {
        case CommandSuccess(id) ⇒
          OK → RequestResult(id, success = true)
        case UnknownCommand(c) ⇒
          InternalServerError → RequestResult("unassigned", success = false, Some(s"Unknown command $c"))
        case InvalidCommand(id, reason) ⇒
          BadRequest → RequestResult(id, success = false, Some(reason))
        case UnexpectedFailure(id, reason) ⇒
          InternalServerError → RequestResult(id, success = false, Some(reason))
      }

}
