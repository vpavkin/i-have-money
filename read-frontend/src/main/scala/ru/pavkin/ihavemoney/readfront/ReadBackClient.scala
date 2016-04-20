package ru.pavkin.ihavemoney.readfront

import akka.actor.{ActorSelection, ActorSystem}
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.pattern.ask
import akka.util.Timeout
import ru.pavkin.ihavemoney.domain.query.{EntityNotFound, Query, QueryFailed, QueryResult}

import scala.concurrent.{ExecutionContext, Future}

class ReadBackClient(system: ActorSystem, interfaceAddress: String) {

  val writeBackendClient: ActorSelection = system.actorSelection(interfaceAddress)

  def query(query: Query)
           (implicit ec: ExecutionContext, timeout: Timeout): Future[(StatusCode, QueryResult)] =
    (writeBackendClient ? query).mapTo[QueryResult]
      .map {
        case e: EntityNotFound ⇒
          NotFound → e
        case e: QueryFailed ⇒
          InternalServerError → e
        case q ⇒
          OK → q
      }

}
