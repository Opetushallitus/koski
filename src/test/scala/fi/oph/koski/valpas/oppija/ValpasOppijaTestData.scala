package fi.oph.koski.valpas.oppija

import fi.oph.koski.valpas.opiskeluoikeusfixture.{ValpasMockOppijat, ValpasOpiskeluoikeusExampleData}

import java.time.LocalDate

object ValpasOppijaTestData {
  // Jyväskylän normaalikoulusta löytyvät näytettävät hakeutumisvelvolliset aakkosjärjestyksessä, tutkittaessa ennen syksyn rajapäivää
  val hakeutumisvelvolliset = List(
    (
      ValpasMockOppijat.amisValmistunutEronnutValmasta,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluValmistunutOpiskeluoikeus(LocalDate.of(2022, 1, 10), LocalDate.of(2023, 4, 28)),
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassatulevaisuudessa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluValmaOpiskeluoikeusEronnut,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassatulevaisuudessa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaaJaLoppuu2021Syksyllä(),
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.esiopetusValmistunutOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          muuOpetusTiedot = Some(ExpectedDataMuuOpetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein1,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.valmistunutYsiluokkalainen,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
      ))
    ),
    (
      ValpasMockOppijat.valmistunutYsiluokkalainenJollaIlmoitus,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
      ))
    ),
    (
      ValpasMockOppijat.kotiopetusMenneisyydessäOppija,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.kotiopetusMenneisyydessäOpiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainen,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainen,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.luokallejäänytYsiluokkalainenJollaUusiYsiluokka,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainenJollaUusiYsiluokka,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        )
      )
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllutJollaIlmoitus,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanut,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä(),
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.lukionAineopinnotAloittanut,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.lukionLokakuussaAloittanut,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaa2021Lokakuussa(),
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassatulevaisuudessa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.turvakieltoOppija,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kesäYsiluokkaKesken,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.eronnutOppijaTarkastelupäivänJälkeen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.eronnutOpiskeluoikeusTarkastelupäivänJälkeen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.hakukohteidenHakuEpäonnistuu,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanutJollaVanhaIlmoitus,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä(),
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä(),
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.eronnutKeväänValmistumisJaksolla17VuottaTäyttäväKasiluokkalainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.eronnutOpiskeluoikeusEiYsiluokkaaKeväänJaksolla,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.eronnutElokuussa17VuottaTäyttäväKasiluokkalainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.eronnutOpiskeluoikeusEiYsiluokkaaElokuussa,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.valmistunutYsiluokkalainenVsop,
      List(
        ExpectedData(
          opiskeluoikeus = ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenVsop,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut", vuosiluokkiinSitomatonOpetus = true)),
        )
      )
    ),
    (
      ValpasMockOppijat.ysiluokkaKeskenVsop,
      List(
        ExpectedData(
          opiskeluoikeus = ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenVsop,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna", vuosiluokkiinSitomatonOpetus = true)),
        )
      )
    ),
    (
      ValpasMockOppijat.valmistunutKasiluokkalainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutKasiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2,
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
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.ilmoituksenLisätiedotPoistettu,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.oppivelvollisuusKeskeytetty,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.oppivelvollisuusKeskeytettyToistaiseksi,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.oppivelvollinenJollaHetu,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kulosaarelainenYsiluokkalainenOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
      )
    ),
    (
      ValpasMockOppijat.hetuton,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.peruskoulustaValmistunutIlman9Luokkaa,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.valmistunutIlmanYsiluokkaa,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
      )),
    ),
    (
      ValpasMockOppijat.lukioVanhallaOpsilla,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.lukionVanhanOpsinOpiskeluoikeusAlkaa2021Keväällä(),
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.turvakieltoOppijaTyhjälläKotikunnalla,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.oppivelvollisuusKeskeytettyEiOpiskele,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
      )),
    ),
    (
      ValpasMockOppijat.preIbAloitettu,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ibOpiskeluoikeusPreIbSuoritus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021Puuttuva7LuokanAlkamispäivä,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021OpiskeluoikeusPuuttuva7LuokanAlkamispäivä,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.perusopetukseenValmistautuva17VuottaTäyttävä,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.perusopetukseenValmistavanOpetuksenOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          muuOpetusTiedot = Some(ExpectedDataMuuOpetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.perusopetukseenValmistavastaValmistunut17Vuotias,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.perusopetukseenValmistavanOpetuksenOpiskeluoikeusValmistunut,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          muuOpetusTiedot = Some(ExpectedDataMuuOpetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.perusopetukseenValmistavastaEronnut17Vuotias,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.perusopetukseenValmistavanOpetuksenOpiskeluoikeusEronnut,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          muuOpetusTiedot = Some(ExpectedDataMuuOpetusTiedot("eronnut", "eronnut")),
        )
      )
    ),
    (
      ValpasMockOppijat.keväänUlkopuolellaValmistunut17v,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.keväänUlkopuolellaValmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.keväänUlkopuolellaEronnut17v,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.keväänUlkopuolellaEronnutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        )
      )
    ),
    (
      ValpasMockOppijat.läsnä17VuottaTäyttäväKasiluokkalainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kasiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.keskeyttänyt17VuottaTäyttäväKasiluokkalainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kasiluokkaKeskeytetty2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "valiaikaisestikeskeytynyt")),
        )
      )
    ),
    (
      ValpasMockOppijat.valmistunutYsiluokkalainenJollaIlmoitusJaUusiOpiskeluoikeus,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmaRessussa,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  // Jyväskylän normaalikoulusta löytyvät näytettävät hakeutumisvelvolliset aakkosjärjestyksessä, tutkittaessa syksyn rajapäivän jälkeen
  val hakeutumisvelvollisetRajapäivänJälkeen = List(
    (
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.esiopetusValmistunutOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          muuOpetusTiedot = Some(ExpectedDataMuuOpetusTiedot("valmistunut", "valmistunut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.läsnä17VuottaTäyttäväKasiluokkalainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kasiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.keskeyttänyt17VuottaTäyttäväKasiluokkalainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kasiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "valiaikaisestikeskeytynyt")),
        )
      )
    ),
    (
      ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein1,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.kotiopetusMenneisyydessäOppija,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.kotiopetusMenneisyydessäOpiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainen,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainen,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.luokallejäänytYsiluokkalainenJollaUusiYsiluokka,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainenJollaUusiYsiluokka,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen2,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen2,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        )
      )
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        )
      )
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllutJollaIlmoitus,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        )
      )
    ),
    (
      ValpasMockOppijat.turvakieltoOppija,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kesäYsiluokkaKesken,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.keväänUlkopuolellaValmistunut17v,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.keväänUlkopuolellaValmistunutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
    (
      ValpasMockOppijat.keväänUlkopuolellaEronnut17v,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.keväänUlkopuolellaEronnutYsiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        )
      )
    ),
    (
      ValpasMockOppijat.eronnutOppijaTarkastelupäivänJälkeen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.eronnutOpiskeluoikeusTarkastelupäivänJälkeen,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.hakukohteidenHakuEpäonnistuu,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.oppivelvollinenAloittanutJaEronnutTarkastelupäivänJälkeen,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenAloittanutJaEronnutTarkastelupäivänJälkeenOpiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.eronnutElokuussa17VuottaTäyttäväKasiluokkalainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.eronnutOpiskeluoikeusEiYsiluokkaaElokuussa,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("eronnut", "eronnut")),
        ),
      )
    ),
    (
      ValpasMockOppijat.ysiluokkaKeskenVsop,
      List(
        ExpectedData(opiskeluoikeus = ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenVsop,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot(
            tarkastelupäivänTila = "voimassa",
            tarkastelupäivänKoskiTila = "lasna",
            vuosiluokkiinSitomatonOpetus = true
          )),
        )
      )
    ),
    (
      ValpasMockOppijat.ilmoituksenLisätiedotPoistettu,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.oppivelvollisuusKeskeytetty,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.oppivelvollisuusKeskeytettyToistaiseksi,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.oppivelvollinenJollaHetu,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kulosaarelainenYsiluokkalainenOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
      )
    ),
    (
      ValpasMockOppijat.hetuton,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.turvakieltoOppijaTyhjälläKotikunnalla,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
      )),
    ),
    (
      ValpasMockOppijat.peruskoulustaLokakuussaValmistunutIlman9Luokkaa,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.valmistunutLokakuussaIlmanYsiluokkaa,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = false,
        perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
      )),
    ),
    (
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021Puuttuva7LuokanAlkamispäivä,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021OpiskeluoikeusPuuttuva7LuokanAlkamispäivä,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.perusopetukseenValmistautuva17VuottaTäyttävä,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.perusopetukseenValmistavanOpetuksenOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          muuOpetusTiedot = Some(ExpectedDataMuuOpetusTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.perusopetukseenValmistavastaEronnut17Vuotias,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.perusopetukseenValmistavanOpetuksenOpiskeluoikeusEronnut,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = false,
          muuOpetusTiedot = Some(ExpectedDataMuuOpetusTiedot("eronnut", "eronnut")),
        )
      )
    ),
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  // Stadin ammattiopistosta löytyvät suorittamisvalvottavat oppijat 5.9.2021
  val suorittamisvalvottavatAmis = List(
    (
      ValpasMockOppijat.ammattikouluOpiskelija,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.ammattikouluOpiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = false,
        onHakeutumisvalvovaOppilaitos = false,
        onSuorittamisvalvovaOppilaitos = true,
        perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.ammattikouluOpiskelijaValma,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.ammattikouluValmaOpiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = true,
        perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.ammattikouluOpiskelijaTelma,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.ammattikouluTelmaOpiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = true,
        onHakeutumisvalvovaOppilaitos = true,
        onSuorittamisvalvovaOppilaitos = true,
        perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.amisEronnutEiUuttaOpiskeluoikeutta,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluEronnutOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("eronnut", "eronnut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenSaksalainenKouluVäliaikaisestiKeskeytynytToukokuussa,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      ),
    ),
    (
      ValpasMockOppijat.amisEronnutUusiOpiskeluoikeusTulevaisuudessaKeskeyttänyt,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluAlkaaOmniaLoka2021,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassatulevaisuudessa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluEronnutOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("eronnut", "eronnut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenSaksalainenKoulu,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      ),
    ),
    (
      ValpasMockOppijat.amisEronnutUusiOpiskeluoikeusPeruskoulussaKeskeyttänytTulevaisuudessa,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.alkaaYsiluokkalainenSaksalainenKouluSyys2021,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluEronnutOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("eronnut", "eronnut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenSaksalainenKoulu,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      ),
    ),
    (
      ValpasMockOppijat.ammattikouluOpiskelijaMontaOpiskeluoikeutta,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluValmaOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.amisAmmatillinenJaNäyttötutkintoonValmistava,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.amisAmmatillinenJaNäyttötutkintoonValmistavaOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
      ),
    ),
    (
      ValpasMockOppijat.amisLomalla,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluLomallaOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "loma")),
        ),
      )
    ),
    (
      ValpasMockOppijat.kaksiToisenAsteenOpiskelua,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmaRessussa,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluValmaOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = true,
          onHakeutumisvalvovaOppilaitos = true,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        )
      )
    ),
    (
      ValpasMockOppijat.maksuttomuuttaPidennetty,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluMaksuttomuuttaPidennetty,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
      )
    ),
    (
      ValpasMockOppijat.amisEronnutUusiKelpaamatonOpiskeluoikeusPerusopetukseenValmistavassa,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.perusopetukseenValmistavanOpetuksenOpiskeluoikeusAlkaaSyys2021,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          muuOpetusTiedot = Some(ExpectedDataMuuOpetusTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluEronnutOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("eronnut", "eronnut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenSaksalainenKoulu,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutKymppiluokkalainen,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("valmistunut", "valmistunut")),
        ),
      ),
    ),
    (
      ValpasMockOppijat.amisEronnutTuvalainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.tuvaOpiskeluoikeusKesken,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenSaksalainenKouluVäliaikaisestiKeskeytynytToukokuussa,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      ),
    ),
    (
      ValpasMockOppijat.amisEronnutUusiOpiskeluoikeusAlkanutJaPäättynytEroonKeskellä,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluEronnutOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("eronnut", "eronnut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluAlkaaJaEroaaOmnia(
            ValpasOpiskeluoikeusExampleData.ammattikouluEronnutOpiskeluoikeus.päättymispäivä.get.minusMonths(6),
            ValpasOpiskeluoikeusExampleData.ammattikouluEronnutOpiskeluoikeus.päättymispäivä.get.minusMonths(3)
          ),
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("eronnut", "eronnut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenSaksalainenKoulu,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      ),
    ),
    (
      ValpasMockOppijat.valmistunutAmiksenOsittainen,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluValmistunutOsittainenOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("valmistunut", "valmistunut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenSaksalainenKoulu,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        ),
      ),
    ),
    (
      ValpasMockOppijat.lukionAineOpinnotJaAmmatillisia,
      List(ExpectedData(
        ValpasOpiskeluoikeusExampleData.ammattikouluOpiskeluoikeus,
        onHakeutumisValvottavaOpiskeluoikeus = false,
        onHakeutumisvalvovaOppilaitos = false,
        onSuorittamisvalvovaOppilaitos = true,
        perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
      ))
    ),
    (
      ValpasMockOppijat.oppijaJollaAmisJaValmistunutYO,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.ammattikouluEronnutOpiskeluoikeus,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = true,
          perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("eronnut", "eronnut")),
        ),
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu,
          onHakeutumisValvottavaOpiskeluoikeus = false,
          onHakeutumisvalvovaOppilaitos = false,
          onSuorittamisvalvovaOppilaitos = false,
          perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
        )
      )
    ),
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  // Varsinais-Suomen Kansanopiston suorittamisvalvottavat oppijat 5.9.2021
  val suorittamisvalvottavatNivelvaihe = List(
      (
        ValpasMockOppijat.amisEronnutUusiKelpaamatonOpiskeluoikeusNivelvaiheessa2,
        List(
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.vstAlkaaSyys2021,
            onHakeutumisValvottavaOpiskeluoikeus = false,
            onHakeutumisvalvovaOppilaitos = false,
            onSuorittamisvalvovaOppilaitos = false,
            perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("voimassa", "lasna")),
          ),
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.ammattikouluEronnutOpiskeluoikeus,
            onHakeutumisValvottavaOpiskeluoikeus = false,
            onHakeutumisvalvovaOppilaitos = false,
            onSuorittamisvalvovaOppilaitos = true,
            perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("eronnut", "eronnut")),
          ),
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenSaksalainenKoulu,
            onHakeutumisValvottavaOpiskeluoikeus = false,
            onHakeutumisvalvovaOppilaitos = false,
            onSuorittamisvalvovaOppilaitos = false,
            perusopetusTiedot = Some(ExpectedDataPerusopetusTiedot("valmistunut", "valmistunut")),
          ),
          ExpectedData(
            ValpasOpiskeluoikeusExampleData.valmistunutKymppiluokkalainen,
            onHakeutumisValvottavaOpiskeluoikeus = false,
            onHakeutumisvalvovaOppilaitos = false,
            onSuorittamisvalvovaOppilaitos = false,
            perusopetuksenJälkeinenTiedot = Some(ExpectedDataPerusopetuksenJälkeinenTiedot("valmistunut", "valmistunut")),
          ),
        ),
      )
    )
}
