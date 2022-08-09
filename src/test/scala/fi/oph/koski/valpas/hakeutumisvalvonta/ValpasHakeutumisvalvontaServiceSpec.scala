package fi.oph.koski.valpas.hakeutumisvalvonta

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.valpas.opiskeluoikeusrepository.{HakeutumisvalvontaTieto, MockValpasRajapäivätService}
import fi.oph.koski.valpas.oppija.ValpasOppijaTestData.{hakeutumisvelvolliset, hakeutumisvelvollisetRajapäivänJälkeen}
import fi.oph.koski.valpas.oppija.ValpasOppijaTestBase
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

import java.time.LocalDate.{of => date}

class ValpasHakeutumisvalvontaServiceSpec extends ValpasOppijaTestBase {
  private val hakeutumisvalvontaService = new ValpasHakeutumisvalvontaService(KoskiApplicationForTests)

  "getHakeutumisvalvottavatOppijatSuppeatTiedot" - {
    "palauttaa yhden oppilaitoksen oppijat oikein tarkasteltaessa ennen syksyn rajapäivää" in {
      val oppijat = hakeutumisvalvontaService.getOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(defaultSession).toOption.get.map(_.oppija)
        .sortBy(o => (o.henkilö.sukunimi, o.henkilö.etunimet))

      oppijat.map(_.henkilö.oid) shouldBe hakeutumisvelvolliset.map(_._1.oid)

      (oppijat zip hakeutumisvelvolliset).foreach { actualAndExpected =>
        val (oppija, (expectedOppija, expectedData)) = actualAndExpected
        validateOppijaSuppeatTiedot(
          oppija,
          expectedOppija,
          expectedData)
      }
    }

    "palauttaa yhden oppilaitoksen oppijat oikein käyttäjälle, jolla globaalit oikeudet, tarkasteltaessa ennen syksyn rajapäivää" in {
      val oppijat = hakeutumisvalvontaService.getOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(session(ValpasMockUsers.valpasOphHakeutuminenPääkäyttäjä))
        .toOption.get.map(_.oppija)
        .sortBy(o => (o.henkilö.sukunimi, o.henkilö.etunimet))
      oppijat.map(_.henkilö.oid) shouldBe hakeutumisvelvolliset.map(_._1.oid)

      (oppijat zip hakeutumisvelvolliset).foreach { actualAndExpected =>
        val (oppija, (expectedOppija, expectedData)) = actualAndExpected
        validateOppijaSuppeatTiedot(
          oppija,
          expectedOppija,
          expectedData)
      }
    }

    "palauttaa yhden oppilaitoksen oppijat oikein tarkasteltaessa syksyn rajapäivän jälkeen" in {
      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 10, 1))

      val oppijat = hakeutumisvalvontaService.getOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(defaultSession).toOption.get.map(_.oppija)
        .sortBy(o => (o.henkilö.sukunimi, o.henkilö.etunimet))

      oppijat.map(_.henkilö.oid) shouldBe hakeutumisvelvollisetRajapäivänJälkeen.map(_._1.oid)

      (oppijat zip hakeutumisvelvollisetRajapäivänJälkeen).foreach { actualAndExpected =>
        val (oppija, (expectedOppija, expectedData)) = actualAndExpected
        validateOppijaSuppeatTiedot(
          oppija,
          expectedOppija,
          expectedData)
      }
    }
  }
}
