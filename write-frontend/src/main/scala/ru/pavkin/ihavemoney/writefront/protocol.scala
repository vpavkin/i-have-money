package ru.pavkin.ihavemoney.writefront

object protocol {
  case class ReceiveIncomeRequest(amount: BigDecimal,
                                  currency: String,
                                  category: String,
                                  comment: Option[String] = None)
  case class SpendRequest(amount: BigDecimal,
                          currency: String,
                          category: String,
                          comment: Option[String] = None)

  case class RequestResult(commandId: String, success: Boolean, error: Option[String] = None)
}
