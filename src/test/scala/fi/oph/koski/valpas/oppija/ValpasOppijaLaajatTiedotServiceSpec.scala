package fi.oph.koski.valpas.oppija

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.localization.Locale
import fi.oph.koski.valpas.opiskeluoikeusfixture.{ValpasMockOppijat, ValpasOpiskeluoikeusExampleData}
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasOpiskeluoikeusDatabaseService, ValpasOppijaRow}
import fi.oph.koski.valpas.oppija.ValpasOppijaTestData.hakeutumisvelvolliset
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

class ValpasOppijaLaajatTiedotServiceSpec extends ValpasOppijaTestBase {
  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla Koskesta ja Valppaasta löytyvällä oppijalla" - {

    "palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
      val (expectedOppija, expectedData) = hakeutumisvelvolliset(1)
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(expectedOppija.oid)(defaultSession).toOption.get

      validateOppijaLaajatTiedot(result.oppija, expectedOppija, expectedData)
    }

    "ei palauta ylioppilasta, koska YO-tutkinto lopettaa oppivelvollisuuden" in {
      val oid = ValpasMockOppijat.oppijaJollaYOOpiskeluoikeus.oid
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(oid)(session(ValpasMockUsers.valpasAapajoenKoulu))

      result.left.map(_.statusCode) should be(Left(403))
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
            ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus(),
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
      result.hakutilanneError.get should equal("Hakukoosteita ei juuri nyt saada haettua. Yritä myöhemmin uudelleen.")
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
            ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus(),
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
            ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus(),
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

    "palauttaa turvakiellon alaisen oppijan tiedot ilman kotikuntaa" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.turvakieltoOppija.oid)(session(ValpasMockUsers.valpasMonta))
      result.toOption.get.oppija.henkilö.kotikunta shouldBe None
    }

    "ei palauta menehtyneen oppijan tietoja" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.menehtynytOppija.oid)(session(ValpasMockUsers.valpasMonta))
      result.left.map(_.statusCode) should be(Left(403))
    }
  }

  "getOppijat eriin jaettuna" - {
    "palauttaa samat oppijat kuin yhdellä kyselyllä" in {
      val eräkoko = 25
      val oppijaOids = ValpasMockOppijat.defaultOppijat.map(_.henkilö.oid)

      val yhdelläKyselyllä = haeOppijat(KoskiApplicationForTests.valpasOpiskeluoikeusDatabaseService, oppijaOids)
      val erissä = haeOppijat(new ValpasOpiskeluoikeusDatabaseService(KoskiApplicationForTests, eräkoko), oppijaOids)

      yhdelläKyselyllä.size should be > eräkoko
      erissä should equal(yhdelläKyselyllä)
    }

    "palauttaa oppijat sukunimen ja etunimien mukaisessa aakkosjärjestyksessä erien yli" in {
      val eräkoko = 25
      val oppijaOids = ValpasMockOppijat.defaultOppijat.map(_.henkilö.oid)

      val nimet = haeOppijat(new ValpasOpiskeluoikeusDatabaseService(KoskiApplicationForTests, eräkoko), oppijaOids)
        .map(o => (o.sukunimi, o.etunimet))

      nimet.size should be > eräkoko
      nimet should equal(nimet.sorted(Ordering.Tuple2(Locale.finnishAlphabeticalOrdering, Locale.finnishAlphabeticalOrdering)))
    }

    "palauttaa oppijan vain kerran, vaikka oppijan oidit päätyvät eri eriin" in {
      val master = ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster
      val oppijaOids = Seq(
        master,
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen,
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas,
      ).map(_.oid)

      haeOppijat(new ValpasOpiskeluoikeusDatabaseService(KoskiApplicationForTests, 1), oppijaOids)
        .map(_.oppijaOid) should equal(Seq(master.oid))
    }
  }

  private def haeOppijat(service: ValpasOpiskeluoikeusDatabaseService, oppijaOids: Seq[String]): Seq[ValpasOppijaRow] =
    service.getOppijat(oppijaOids, rajaaOVKelpoisiinOpiskeluoikeuksiin = false, haeMyösOppivelvollisuudestaVapautetut = true)
}
