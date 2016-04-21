package ru.pavkin.ihavemoney.frontend.styles

import scalacss.Defaults._
import scala.language.postfixOps

object Global extends StyleSheet.Standalone {

  import dsl._

  "body" - (
    paddingTop(80 px)
    )

  ".form-group" -(
    &("button") - (
      marginRight(15 px)
      ),
    &(".form-control") - (
      marginBottom(10 px)
      )
    )

}
