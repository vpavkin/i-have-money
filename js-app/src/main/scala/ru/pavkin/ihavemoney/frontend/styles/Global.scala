package ru.pavkin.ihavemoney.frontend.styles

import scalacss.Defaults._
import scala.language.postfixOps

object Global extends StyleSheet.Standalone {

  import dsl._

  "body" - (
    paddingTop(70 px)
    )
}
