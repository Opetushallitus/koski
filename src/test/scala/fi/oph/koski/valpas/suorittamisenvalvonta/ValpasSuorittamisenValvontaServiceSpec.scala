package fi.oph.koski.valpas.suorittamisenvalvonta

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.valpas.oppija.ValpasOppijaTestData.suorittamisvalvottavatAmis
import fi.oph.koski.valpas.oppija.ValpasOppijaTestBase
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

class ValpasSuorittamisenValvontaServiceSpec extends ValpasOppijaTestBase {
  private val suorittamisenValvontaService = new ValpasSuorittamisenValvontaService(KoskiApplicationForTests)

  "getOppijatSuppeatTiedot palauttaa yhden oppilaitoksen oppijat oikein tarkasteltaessa syksyn alussa" in {
    val oppijat = suorittamisenValvontaService.getOppijatSuppeatTiedot(amisOppilaitos)((session(ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu))).toOption.get.map(_.oppija)
      .sortBy(o => (o.henkilö.sukunimi, o.henkilö.etunimet))

    oppijat.map(_.henkilö.oid) shouldBe suorittamisvalvottavatAmis.map(_._1.oid)

    (oppijat zip suorittamisvalvottavatAmis).foreach { actualAndExpected =>
      val (oppija, (expectedOppija, expectedData)) = actualAndExpected
      validateOppijaSuppeatTiedot(
        oppija,
        expectedOppija,
        expectedData)
    }
  }
}
