package ru.pavkin.ihavemoney.domain

import io.funcqrs.CommandException
import io.funcqrs.backend.QueryByTag
import io.funcqrs.config.Api._
import io.funcqrs.test.InMemoryTestSupport
import io.funcqrs.test.backend.InMemoryBackend
import org.scalatest.concurrent.ScalaFutures
import ru.pavkin.ihavemoney.domain.errors.BalanceIsNotEnough
import ru.pavkin.ihavemoney.domain.fortune.FortuneProtocol._
import ru.pavkin.ihavemoney.domain.fortune.{Currency, Fortune, FortuneId}
import ru.pavkin.ihavemoney.readback.{MoneyViewProjection, MoneyViewRepository}

import scala.concurrent.ExecutionContext.Implicits.global

class FortuneProtocolSpec extends IHaveMoneySpec with ScalaFutures {

  class FortuneInMemoryTest extends InMemoryTestSupport {

    val repo = new InMemoryMoneyViewRepository
    val id = FortuneId.generate()

    def configure(backend: InMemoryBackend): Unit =
      backend.configure {
        aggregate[Fortune](Fortune.behavior)
      }
        .configure {
          projection(
            query = QueryByTag(Fortune.tag),
            projection = new MoneyViewProjection(repo),
            name = "MoneyViewProjection"
          )
        }

    def ref(id: FortuneId) = aggregateRef[Fortune](id)
  }


  test("Increase fortune") {


    new FortuneInMemoryTest {
      val fortune = ref(id)

      fortune ! ReceiveIncome(BigDecimal(123.12), Currency.USD, IncomeCategory("salary"))
      fortune ! ReceiveIncome(BigDecimal(20), Currency.EUR, IncomeCategory("salary"))
      fortune ! ReceiveIncome(BigDecimal(30.5), Currency.EUR, IncomeCategory("salary"))

      expectEvent { case FortuneIncreased(amount, Currency.USD, _, _, None) if amount.toDouble == 123.12 => () }
      expectEvent { case FortuneIncreased(amount, Currency.EUR, _, _, None) if amount.toDouble == 20.0 => () }
      expectEvent { case FortuneIncreased(amount, Currency.EUR, _, _, None) if amount.toDouble == 30.5 => () }

      val view = repo.findAll(id).futureValue
      view(Currency.USD) shouldBe BigDecimal(123.12)
      view(Currency.EUR) shouldBe BigDecimal(50.5)
    }
  }

  test("Increase and decrease fortune") {

    new FortuneInMemoryTest {
      val fortune = ref(id)

      fortune ! ReceiveIncome(BigDecimal(123.12), Currency.USD, IncomeCategory("salary"))
      fortune ! Spend(BigDecimal(20), Currency.USD, ExpenseCategory("food"))

      expectEvent { case FortuneIncreased(amount, Currency.USD, _, _, None) if amount.toDouble == 123.12 => () }
      expectEvent { case FortuneSpent(amount, Currency.USD, _, _, None) if amount.toDouble == 20.0 => () }

      val view = repo.findAll(id).futureValue
      view(Currency.USD) shouldBe BigDecimal(103.12)
    }
  }

  test("Spending of not initialized fortune produces an error") {

    new FortuneInMemoryTest {
      val fortune = ref(id)
      intercept[CommandException] {
        fortune ? Spend(BigDecimal(20), Currency.USD, ExpenseCategory("food"))
      }.getMessage should startWith("Invalid command Spend")
    }
  }

  test("Spending more than is available is not allowed") {
    new FortuneInMemoryTest {
      val fortune = ref(id)

      fortune ? ReceiveIncome(BigDecimal(10), Currency.USD, IncomeCategory("salary"))

      intercept[BalanceIsNotEnough] {
        fortune ? Spend(BigDecimal(20), Currency.USD, ExpenseCategory("food"))
      }.getMessage shouldBe "Your balance (10 USD) is not enough for this operation"
    }
  }

}
