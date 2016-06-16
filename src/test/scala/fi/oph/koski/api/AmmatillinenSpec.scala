package fi.oph.koski.api

import fi.oph.koski.oppija.MockOppijat
import org.scalatest.FreeSpec

class AmmatillinenSpec extends FreeSpec with TodistusTestMethods{
  "Ammatillisen perustutkinnon päättötodistus" in {
    todistus(MockOppijat.ammattilainen.oid, "ammatillinenkoulutus") should equal("""""")
  }
}
