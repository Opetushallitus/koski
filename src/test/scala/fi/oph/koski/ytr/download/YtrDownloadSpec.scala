package fi.oph.koski.ytr.download

import fi.oph.koski.KoskiHttpSpec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class YtrDownloadSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with YtrDownloadTestMethods {

  val birthdateStart = LocalDate.of(1980, 3, 1)
  val birthdateEnd = LocalDate.of(1980, 3, 31)

  "YTR download" in {
    redownloadYtrData(birthdateStart, birthdateEnd)
    Thread.sleep(3000)
  }
}
