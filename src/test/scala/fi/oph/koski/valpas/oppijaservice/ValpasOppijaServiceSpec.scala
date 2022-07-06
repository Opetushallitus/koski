package fi.oph.koski.valpas.oppijaservice

import ValpasOppijaServiceSpecTestData.{hakeutumisvelvolliset, hakeutumisvelvollisetRajapäivänJälkeen, suorittamisvalvottavatAmis}
import fi.oph.koski.valpas.opiskeluoikeusfixture.{ValpasMockOppijat, ValpasOpiskeluoikeusExampleData}
import fi.oph.koski.valpas.opiskeluoikeusrepository.{HakeutumisvalvontaTieto, MockValpasRajapäivätService}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

import java.time.LocalDate.{of => date}

class ValpasOppijaServiceSpec extends ValpasOppijaServiceTestBase {
  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla Koskesta ja Valppaasta löytyvällä oppijalla" - {

    "palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
      val (expectedOppija, expectedData) = hakeutumisvelvolliset(1)
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(expectedOppija.oid)(defaultSession).toOption.get

      validateOppijaLaajatTiedot(result.oppija, expectedOppija, expectedData)
    }

    "palautetun oppijan valintatilat ovat oikein" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)(defaultSession).toOption.get

      val valintatilat = result.hakutilanteet.map(_.hakutoiveet.flatMap(_.valintatila.map(_.koodiarvo)))

      valintatilat shouldBe List(
        List(
          "hylatty",
          "hyvaksytty",
          "peruuntunut",
          "peruuntunut",
          "peruuntunut",
        ),
      )
    }

    "palauttaa oppijan tiedot, vaikka oid ei olisikaan master oid" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid)(defaultSession)
      validateOppijaLaajatTiedot(
        result.toOption.get.oppija,
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
        Set(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid),
        List(
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus,
            onHakeutumisValvottavaOpiskeluoikeus = false,
            onHakeutumisvalvovaOppilaitos = false,
            onSuorittamisvalvovaOppilaitos = true,
            perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
          ),
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
            onHakeutumisValvottavaOpiskeluoikeus = true,
            onHakeutumisvalvovaOppilaitos = true,
            onSuorittamisvalvovaOppilaitos = false,
            Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
          ),
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu,
            onHakeutumisValvottavaOpiskeluoikeus = true,
            onHakeutumisvalvovaOppilaitos = true,
            onSuorittamisvalvovaOppilaitos = false,
            Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
          )
        )
      )
    }

    "palauttaa oppijan tiedot, vaikka hakukoostekysely epäonnistuisi" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.hakukohteidenHakuEpäonnistuu.oid)(defaultSession).toOption.get
      result.hakutilanneError.get should equal("Hakukoosteita ei juuri nyt saada haettua suoritusrekisteristä. Yritä myöhemmin uudelleen.")
      validateOppijaLaajatTiedot(
        result.oppija,
        ValpasMockOppijat.hakukohteidenHakuEpäonnistuu,
        List(ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )),
      )
    }

    "palauttaa oppijan tiedot, vaikka kysely tehtäisiin oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid)(defaultSession)
      validateOppijaLaajatTiedot(
        result.toOption.get.oppija,
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
        Set(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid),
        List(
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus,
            onHakeutumisValvottavaOpiskeluoikeus = false,
            onHakeutumisvalvovaOppilaitos = false,
            onSuorittamisvalvovaOppilaitos = true,
            perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
          ),
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
            onHakeutumisValvottavaOpiskeluoikeus = true,
            onHakeutumisvalvovaOppilaitos = true,
            onSuorittamisvalvovaOppilaitos = false,
            Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
          ),
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu,
            onHakeutumisValvottavaOpiskeluoikeus = true,
            onHakeutumisvalvovaOppilaitos = true,
            onSuorittamisvalvovaOppilaitos = false,
            Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
          )
        )
      )
    }

    "palauttaa oppijan tiedot, vaikka kysely tehtäisiin master-oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid)(session(ValpasMockUsers.valpasAapajoenKoulu))
      validateOppijaLaajatTiedot(
        result.toOption.get.oppija,
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
        Set(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid),
        List(
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus,
            onHakeutumisValvottavaOpiskeluoikeus = false,
            onHakeutumisvalvovaOppilaitos = false,
            onSuorittamisvalvovaOppilaitos = true,
            perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
          ),
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
            onHakeutumisValvottavaOpiskeluoikeus = true,
            onHakeutumisvalvovaOppilaitos = true,
            onSuorittamisvalvovaOppilaitos = false,
            Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
          ),
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu,
            onHakeutumisValvottavaOpiskeluoikeus = true,
            onHakeutumisvalvovaOppilaitos = true,
            onSuorittamisvalvovaOppilaitos = false,
            Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
          )
        )
      )
    }
  }

  "getHakeutumisvalvottavatOppijatSuppeatTiedot" - {
    "palauttaa yhden oppilaitoksen oppijat oikein tarkasteltaessa ennen syksyn rajapäivää" in {
      val oppijat = oppijalistatService.getHakeutumisvalvottavatOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(defaultSession).toOption.get.map(_.oppija)
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
      val oppijat = oppijalistatService.getHakeutumisvalvottavatOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(session(ValpasMockUsers.valpasOphHakeutuminenPääkäyttäjä))
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

      val oppijat = oppijalistatService.getHakeutumisvalvottavatOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(defaultSession).toOption.get.map(_.oppija)
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

  "getSuorittamisvalvottavatOppijatSuppeatTiedot palauttaa yhden oppilaitoksen oppijat oikein tarkasteltaessa syksyn alussa" in {
    val oppijat = oppijalistatService.getSuorittamisvalvottavatOppijatSuppeatTiedot(amisOppilaitos)((session(ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu))).toOption.get.map(_.oppija)
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
