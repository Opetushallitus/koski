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

  val birthmonthStart = "1980-03"
  val birthmonthEnd = "1981-10"

  val modifiedSince = LocalDate.of(2023, 1, 1)

  "YTR download" in {
    redownloadYtrData(birthmonthStart, birthmonthEnd)
  }

  "YTR download modified since" in {
    redownloadYtrData(modifiedSince, force = false)
  }
}
