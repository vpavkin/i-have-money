package ru.pavkin.ihavemoney.frontend.components

import ru.pavkin.ihavemoney.frontend.Route
import ru.pavkin.ihavemoney.frontend.Route._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.all._

object Nav {

  val component = ReactComponentB[RouterCtl[Route]]("Menu")
    .render_P { ctl =>
      def routeLink(name: String, target: Route) =
        li(a(ctl setOnClick target, name))

      nav(className := "navbar navbar-default navbar-fixed-top",
        div(className := "container",
          div(className := "navbar-header",
            button(tpe := "button", className := "navbar-toggle collapsed",
              "data-toggle".reactAttr := "collapse",
              "data-target".reactAttr := "#navbar",
              "aria-expanded".reactAttr := "false",
              "aria-controls".reactAttr := "navbar",
              span(className := "sr-only", "Toggle navigation"),
              span(className := "icon-bar"),
              span(className := "icon-bar"),
              span(className := "icon-bar")
            ),
            a(className := "navbar-brand", href := "#", "I Have Money")
          ),
          div(id := "navbar", className := "navbar-collapse collapse",
            "aria-expanded".reactAttr := "false",
            height := "1px",
            ul(className := "nav navbar-nav",
              routeLink("Add Transactions", AddTransactions),
              routeLink("Balance View", BalanceView)
            )
          )
        )
      )
    }
    .configure(Reusability.shouldComponentUpdate)
    .build
}
