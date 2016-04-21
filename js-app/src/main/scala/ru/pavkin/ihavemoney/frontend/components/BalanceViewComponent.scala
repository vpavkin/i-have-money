package ru.pavkin.ihavemoney.frontend.components

import cats.data.Xor
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom.raw.HTMLInputElement
import ru.pavkin.ihavemoney.frontend.api
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object BalanceViewComponent {

  case class State(fortuneId: String,
                   balances: Map[String, BigDecimal])

  class Backend($: BackendScope[Unit, State]) {

    val fortuneIdInput = Ref[HTMLInputElement]("fortuneIdInput")

    def onTextChange(change: (State, String) ⇒ State)(e: ReactEventI) = {
      val newValue = e.target.value
      $.modState(change(_, newValue))
    }

    def onFormSubmit(e: ReactEventI) = e.preventDefaultCB

    def loadBalances(fortuneId: String) = Callback {
      println(s"Refresh balance for $fortuneId")
      if (fortuneId.isEmpty)
        Callback.alert("Empty id").runNow()
      else
        api.getBalances(
          fortuneId
        ).map {
          case Xor.Left(error) ⇒ Callback.alert(s"Error: $error").runNow()
          case Xor.Right(balances) ⇒
            $.modState(_.copy(balances = balances)).runNow()
        }
    }

    def render(state: State) = {
      div(
        form(
          className := "form-horizontal",
          onSubmit ==> onFormSubmit,
          div(className := "form-group",
            label(htmlFor := "fortuneIdInput", className := "col-sm-2 control-label", "Fortune ID"),
            div(className := "col-sm-8",
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
            ),
            div(className := "col-sm-2",
              button(tpe := "submit", className := "btn btn-success", disabled := state.fortuneId.isEmpty,
                onClick --> loadBalances(state.fortuneId), "Refresh")
            )
          )
        ),
        if (state.balances.nonEmpty)
          div(
            table(className := "table table-striped table-hover table-condensed",
              thead(tr(th("Currency"), th("Amount"))),
              tbody(
                state.balances.map {
                  case (currency, amount) ⇒ tr(td(currency), td(amount.toString))
                }
              )
            )
          )
        else
          div()
      )
    }

    def init = $.state.flatMap(s ⇒ loadBalances(s.fortuneId))
  }

  val component = ReactComponentB[Unit]("AddTransactionsComponent")
    .initialState(State("MyFortune", Map.empty))
    .renderBackend[Backend]
    .componentDidMount(_.backend.init)
    .build
}
