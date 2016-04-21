package ru.pavkin.ihavemoney.frontend

import io.circe._
import io.circe.syntax._
import cats.data.{Xor, XorT}
import cats.syntax.xor._
import japgolly.scalajs.react.extra.router.BaseUrl
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, AjaxException}
import ru.pavkin.ihavemoney.domain.fortune.Currency
import ru.pavkin.ihavemoney.protocol.readfront._
import ru.pavkin.ihavemoney.protocol.writefront._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

object api {

  val readFrontBaseUrl = BaseUrl.fromWindowOrigin_/
  val writeFrontBaseUrl = BaseUrl(
    dom.window.asInstanceOf[js.Dynamic].writefrontURL.asInstanceOf[String]
  )

  object routes {
    def addIncome(fortuneId: String) = writeFrontBaseUrl / "fortune" / fortuneId / "income"
    def addExpense(fortuneId: String) = writeFrontBaseUrl / "fortune" / fortuneId / "spend"
  }

  def addIncome(id: String,
                amount: BigDecimal,
                currency: Currency,
                category: String,
                comment: Option[String])
               (implicit ec: ExecutionContext): Future[String Xor Unit] =
    postJSON(routes.addIncome(id).value,
      ReceiveIncomeRequest(amount, currency, category, comment).asJson.toString())
      .map(_.map(_ ⇒ ()))

  def addExpense(id: String,
                 amount: BigDecimal,
                 currency: Currency,
                 category: String,
                 comment: Option[String])
                (implicit ec: ExecutionContext): Future[String Xor Unit] =
    postJSON(routes.addExpense(id).value,
      ReceiveIncomeRequest(amount, currency, category, comment).asJson.toString())
      .map(_.map(_ ⇒ ()))

  private def recover(f: Future[String Xor String])(implicit ec: ExecutionContext) = f.recover {
    case AjaxException(xhr) => (xhr.status match {
      case 404 => "NotFound"
      case 400 => s"BadRequest: ${xhr.responseText}"
      case 503 => "ServiceUnavailable"
      case i => s"Other Error: $i"
    }).left
  }

  private def get(url: String)(implicit ec: ExecutionContext): Future[String Xor String] = recover {
    Ajax.get(url).map(xhr =>
      if (xhr.status == 200) {
        xhr.responseText.right
      } else
        s"Other Error: ${xhr.status}".left
    )
  }

  private def postJSON(url: String, body: String)(implicit ec: ExecutionContext): Future[String Xor String] = recover {
    Ajax.post(
      url,
      data = body,
      headers = Map("Content-Type" -> "application/json")
    ).map(xhr =>
      if (xhr.status == 200) {
        xhr.responseText.right
      } else
        s"Other Error: ${xhr.status}".left
    )
  }
}
