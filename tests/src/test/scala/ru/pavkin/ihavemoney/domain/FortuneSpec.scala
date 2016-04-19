package ru.pavkin.ihavemoney.domain

import ru.pavkin.ihavemoney.domain.fortune.{Fortune, Worth}

class FortuneSpec extends IHaveMoneySpec {

  test("Fortune.increase increases the amount of money") {
    forAll { (f: Fortune, w: Worth) ⇒
      f.increase(w).amount(w.currency) shouldBe (f.amount(w.currency) + w.amount)
    }
  }

  test("Fortune.decrease unconditionally decreases the amount of money") {
    forAll { (f: Fortune, w: Worth) ⇒
      f.decrease(w).amount(w.currency) shouldBe (f.amount(w.currency) - w.amount)
    }
  }


}
