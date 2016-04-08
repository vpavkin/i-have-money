package ru.pavkin.ihavemoney.domain.fortune

import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol.FortuneEvent

case class FortuneView(balance: Map[Currency, BigDecimal],
                       log: List[FortuneEvent],
                       id: FortuneId) {

  override def toString: String =
    s"""
       |FortuneView
       | balances: ${balance.map { case (c, a) => s"${f"$a%1.2f"} ${c.code}" }.mkString(", ")}
       | id: $id
      """.stripMargin
}
