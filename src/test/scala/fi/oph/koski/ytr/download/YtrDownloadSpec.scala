package fi.oph.koski.ytr.download

import fi.oph.koski.KoskiHttpSpec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class YtrDownloadSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with YtrDownloadTestMethods {

  val birthmonthStart = "1980-03"
  val birthmonthEnd = "1980-04"

  "YTR download" in {
    redownloadYtrData(birthmonthStart, birthmonthEnd)
  }
}
