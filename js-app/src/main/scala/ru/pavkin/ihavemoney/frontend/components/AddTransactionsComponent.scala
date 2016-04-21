package ru.pavkin.ihavemoney.frontend.components

import cats.data.Xor
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom.raw.HTMLInputElement
import ru.pavkin.ihavemoney.domain.fortune.Currency
import ru.pavkin.ihavemoney.frontend.api
import ru.pavkin.utils.option._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.Try

object AddTransactionsComponent {

  case class State(fortuneId: String,
                   currency: String,
                   amount: String,
                   category: String,
                   comment: String)
  class Backend($: BackendScope[Unit, State]) {

    val fortuneIdInput = Ref[HTMLInputElement]("fortuneIdInput")
    val amountInput = Ref[HTMLInputElement]("amountInput")
    val currencyInput = Ref[HTMLInputElement]("currencyInput")
    val categoryInput = Ref[HTMLInputElement]("categoryInput")
    val commentInput = Ref[HTMLInputElement]("commentInput")

    def onTextChange(change: (State, String) ⇒ State)(e: ReactEventI) = {
      val newValue = e.target.value
      $.modState(change(_, newValue))
    }

    def onFormSubmit(e: ReactEventI) = e.preventDefaultCB

    def onIncomeSubmit(state: State)(e: ReactEventI) = e.preventDefaultCB >> Callback {
      if (!isValid(state))
        Callback.alert("Invalid data").runNow()
      else
        api.addIncome(
          state.fortuneId,
          BigDecimal(state.amount),
          Currency.unsafeFromCode(state.currency),
          state.category,
          notEmpty(state.comment)
        ).map {
          case Xor.Left(error) ⇒ Callback.alert(s"Error: $error").runNow()
          case _ ⇒ Callback.alert(s"Success")
        }
    }

    def isValid(s: State) =
      s.fortuneId.nonEmpty &&
        Try(BigDecimal(s.amount)).isSuccess &&
        Currency.isCurrency(s.currency) &&
        s.category.nonEmpty

    def render(state: State) = {
      val valid = isValid(state)
      form(
        className := "form-horizontal",
        onSubmit ==> onFormSubmit,
        div(className := "form-group",
          label(htmlFor := "fortuneIdInput", className := "col-sm-2 control-label", "Fortune ID"),
          div(className := "col-sm-10",
            input(
              ref := fortuneIdInput,
              required := true,
              tpe := "text",
              className := "form-control",
              id := "fortuneIdInput",
              placeholder := "Fortune Id",
              value := state.fortuneId,
              onChange ==> onTextChange((s, v) ⇒ s.copy(fortuneId = v))
            )
          )
        ),
        div(className := "form-group",
          label(htmlFor := "amountInput", className := "col-sm-2 control-label", "Amount"),
          div(className := "col-sm-8", input(
            ref := amountInput,
            required := true,
            tpe := "number",
            min := 0.0,
            step := 0.01,
            className := "form-control",
            id := "amountInput",
            placeholder := "Amount",
            value := state.amount,
            onChange ==> onTextChange((s, v) ⇒ s.copy(amount = v))
          )),
          div(className := "col-sm-2", select(
            ref := currencyInput,
            required := true,
            className := "form-control",
            id := "currencyInput",
            value := state.currency,
            onChange ==> onTextChange((s, v) ⇒ s.copy(currency = v)),
            List("USD", "EUR", "RUR").map(option(_))
          ))
        ),
        div(className := "form-group",
          label(htmlFor := "categoryInput", className := "col-sm-2 control-label", "Category"),
          div(className := "col-sm-10",
            input(
              ref := categoryInput,
              required := true,
              tpe := "text",
              className := "form-control",
              id := "categoryInput",
              placeholder := "Category",
              value := state.category,
              onChange ==> onTextChange((s, v) ⇒ s.copy(category = v))
            )
          )
        ),
        div(className := "form-group",
          label(htmlFor := "commentInput", className := "col-sm-2 control-label", "Comment"),
          div(className := "col-sm-10",
            input(
              ref := commentInput,
              tpe := "text",
              className := "form-control",
              id := "commentInput",
              placeholder := "Comment",
              value := state.comment,
              onChange ==> onTextChange((s, v) ⇒ s.copy(comment = v))
            )
          )
        ),
        div(className := "form-group",
          div(className := "col-sm-offset-2 col-sm-10",
            button(tpe := "submit", className := "btn btn-success", disabled := (!valid),
              onClick ==> onIncomeSubmit(state), "Income"),
            button(tpe := "submit", className := "btn btn-danger", disabled := (!valid),
              //              onClick ==> onExpenseSubmit,
              "Expense")
          )
        )
      )
    }
  }
  val component = ReactComponentB[Unit]("AddTransactionsComponent")
    .initialState(State("MyFortune", "USD", "1000", "Salary", ""))
    .renderBackend[Backend]
    .build
}
