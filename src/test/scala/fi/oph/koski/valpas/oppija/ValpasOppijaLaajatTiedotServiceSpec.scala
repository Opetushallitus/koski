package fi.oph.koski.valpas.oppija

import fi.oph.koski.valpas.opiskeluoikeusfixture.{ValpasMockOppijat, ValpasOpiskeluoikeusExampleData}
import fi.oph.koski.valpas.oppija.ValpasOppijaTestData.hakeutumisvelvolliset
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

class ValpasOppijaLaajatTiedotServiceSpec extends ValpasOppijaTestBase {
  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla Koskesta ja Valppaasta löytyvällä oppijalla" - {

    "palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
      val (expectedOppija, expectedData) = hakeutumisvelvolliset(1)
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(expectedOppija.oid)(defaultSession).toOption.get

      validateOppijaLaajatTiedot(result.oppija, expectedOppija, expectedData)
    }

    "palauttaa annetun oppijanumeron mukaisen oppijan, jolla on YO-opiskeluoikeuksia, ilman ylioppilastutkintoja" in {
      val oid = ValpasMockOppijat.oppijaJollaMuitaOpiskeluoikeuksia.oid
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(oid)(session(ValpasMockUsers.valpasAapajoenKoulu))

      result.isRight should be(true)
      result.toOption.get.oppija.opiskeluoikeudet should have length(1)
      result.toOption.get.oppija.opiskeluoikeudet(0).tyyppi.koodiarvo should be("perusopetus")
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

    "palauttaa turvakiellon alaisen oppijan tiedot ilman kotikuntaa" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.turvakieltoOppija.oid)(session(ValpasMockUsers.valpasMonta))
      result.right.get.oppija.henkilö.kotikunta shouldBe None
    }
  }
}
