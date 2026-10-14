package fi.oph.koski.valpas.hakeutumisvalvonta

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.opiskeluoikeusrepository.{HakeutumisvalvontaTieto, MockValpasRajapäivätService}
import fi.oph.koski.valpas.oppija.ValpasOppijaTestData.{hakeutumisvelvolliset, hakeutumisvelvollisetRajapäivänJälkeen}
import fi.oph.koski.valpas.oppija.ValpasOppijaTestBase
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import fi.oph.koski.valpas.kuntailmoitus.ValpasKuntailmoitusApiServletSpec
import fi.oph.koski.valpas.valpasrepository.ValpasKuntailmoitusLaajatTiedot

import java.time.LocalDate.{of => date}

class ValpasHakeutumisvalvontaServiceSpec extends ValpasOppijaTestBase {
  private val hakeutumisvalvontaService = new ValpasHakeutumisvalvontaService(KoskiApplicationForTests)

  private def nivelvaiheenOppijaOidit: Seq[String] =
    hakeutumisvalvontaService
      .getOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Nivelvaihe)(defaultSession).toOption.get
      .map(_.oppija.henkilö.oid)

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

    "palauttaa yhden oppilaitoksen nivelvaiheen TUVA-oppijan oikein tarkasteltaessa ennen syksyn rajapäivää" in {
      val oppijat = hakeutumisvalvontaService
        .getOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Nivelvaihe)(defaultSession).toOption.get
        .map(_.oppija)
        .sortBy(o => (o.henkilö.sukunimi, o.henkilö.etunimet))

      oppijat.map(_.henkilö.oid) should contain(ValpasMockOppijat.valmistunutTuvalainen.oid)

    }

    "palauttaa kevään valmistumisjaksolla nivelvaiheesta eronneen oppijan syksyn rajapäivään asti" in {
      val oid = ValpasMockOppijat.alkukesästäEronneeksiKatsottuNivelvaiheenOpiskelija.oid
      nivelvaiheenOppijaOidit should contain(oid)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 9, 30))
      nivelvaiheenOppijaOidit should contain(oid)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 10, 1))
      nivelvaiheenOppijaOidit should not contain(oid)
    }

    "palauttaa kevään valmistumisjakson ulkopuolella nivelvaiheesta eronneen oppijan 2 kk ajan" in {
      val oid = ValpasMockOppijat.nivelvaiheestaKesänJälkeenEronneeksiKatsottu.oid
      nivelvaiheenOppijaOidit should contain(oid)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 10, 15))
      nivelvaiheenOppijaOidit should contain(oid)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 10, 16))
      nivelvaiheenOppijaOidit should not contain(oid)
    }

    "ei palauta oppijaa, jolle on tehty kuntailmoitus" in {
      val ilmoitus = ValpasKuntailmoitusApiServletSpec.teeMinimiKuntailmoitusInput(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
      post("/valpas/api/kuntailmoitus", body = ilmoitus, headers = authHeaders() ++ jsonContent) {
        verifyResponseStatusOk()
      }
      val oppijat = hakeutumisvalvontaService.getOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(defaultSession)
      oppijat.toOption.get.length shouldBe (hakeutumisvelvolliset.length - 1)
    }

    "palauttaa taas oppijan, jonka opiskeluoikeuteen liittyy mitätöity kuntailmoitus" in {
      val ilmoitus = ValpasKuntailmoitusApiServletSpec.teeMinimiKuntailmoitusInput(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
      val id = post("/valpas/api/kuntailmoitus", body = ilmoitus, headers = authHeaders() ++ jsonContent) {
        verifyResponseStatusOk()
        JsonSerializer.parse[ValpasKuntailmoitusLaajatTiedot](response.body).id.get
      }
      delete(s"/valpas/api/kuntailmoitus/${id}", headers = authHeaders()) {
        verifyResponseStatus(204)
      }
      val oppijat = hakeutumisvalvontaService.getOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(defaultSession)
      oppijat.toOption.get.length shouldBe (hakeutumisvelvolliset.length)
    }
  }
}
