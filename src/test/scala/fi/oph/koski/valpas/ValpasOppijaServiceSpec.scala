package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.valpas.db.ValpasDatabaseFixtureLoader
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat, ValpasOpiskeluoikeusExampleData}
import fi.oph.koski.valpas.opiskeluoikeusrepository.{HakeutumisvalvontaTieto, MockValpasRajapäivätService}
import fi.oph.koski.valpas.valpasrepository._
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.BeforeAndAfterEach
import java.time.LocalDate.{of => date}
import java.time.LocalDateTime

class ValpasOppijaServiceSpec extends ValpasOppijaServiceTestBase with BeforeAndAfterEach {
  override protected def beforeEach() {
    super.beforeEach()
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
  }

  override protected def afterEach(): Unit = {
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
    super.afterEach()
  }

  // Jyväskylän normaalikoulusta löytyvät näytettävät hakeutumisvelvolliset aakkosjärjestyksessä, tutkittaessa ennen syksyn rajapäivää
  private val hakeutumisvelvolliset = List(
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
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  // Jyväskylän normaalikoulusta löytyvät näytettävät hakeutumisvelvolliset aakkosjärjestyksessä, tutkittaessa syksyn rajapäivän jälkeen
  private val hakeutumisvelvollisetRajapäivänJälkeen = List(
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
  private val suorittamisvalvottavatAmis = List(
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
      ValpasMockOppijat.amisEronnutUusiKelpaamatonOpiskeluoikeusNivelvaiheessa,
      List(
        ExpectedData(
          ValpasOpiskeluoikeusExampleData.kymppiluokkaAlkaaSyys2021,
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
    ),
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

  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
    val (expectedOppija, expectedData) = hakeutumisvelvolliset(1)
    val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(expectedOppija.oid)(defaultSession).toOption.get

    validateOppijaLaajatTiedot(result.oppija, expectedOppija, expectedData)
  }

  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksillan palauttaman oppijan valintatilat ovat oikein" in {
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

  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla palauttaa oppijan tiedot, vaikka oid ei olisikaan master oid" in {
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

  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla palauttaa oppijan tiedot, vaikka hakukoostekysely epäonnistuisi" in {
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

  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla palauttaa oppijan tiedot, vaikka kysely tehtäisiin oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
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

  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla palauttaa oppijan tiedot, vaikka kysely tehtäisiin master-oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
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

  "getHakeutumisvalvottavatOppijatSuppeatTiedot palauttaa yhden oppilaitoksen oppijat oikein tarkasteltaessa ennen syksyn rajapäivää" in {
    val oppijat = oppijaSuppeatTiedotService.getHakeutumisvalvottavatOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(defaultSession).toOption.get.map(_.oppija)
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

  "getHakeutumisvalvottavatOppijatSuppeatTiedot palauttaa yhden oppilaitoksen oppijat oikein käyttäjälle, jolla globaalit oikeudet, tarkasteltaessa ennen syksyn rajapäivää" in {
    val oppijat = oppijaSuppeatTiedotService.getHakeutumisvalvottavatOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(session(ValpasMockUsers.valpasOphHakeutuminenPääkäyttäjä))
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

  "getHakeutumisvalvottavatOppijatSuppeatTiedot palauttaa yhden oppilaitoksen oppijat oikein tarkasteltaessa syksyn rajapäivän jälkeen" in {
    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 10, 1))

    val oppijat = oppijaSuppeatTiedotService.getHakeutumisvalvottavatOppijatSuppeatTiedot(oppilaitos, HakeutumisvalvontaTieto.Perusopetus)(defaultSession).toOption.get.map(_.oppija)
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

  "getSuorittamisvalvottavatOppijatSuppeatTiedot palauttaa yhden oppilaitoksen oppijat oikein tarkasteltaessa syksyn alussa" in {
    val oppijat = oppijaSuppeatTiedotService.getSuorittamisvalvottavatOppijatSuppeatTiedot(amisOppilaitos)((session(ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu))).toOption.get.map(_.oppija)
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

  "kuntailmoitukset: getOppija palauttaa kuntailmoituksettoman oppijan ilman kuntailmoituksia" in {
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa.oid)(defaultSession)
      .toOption.get

    oppija.kuntailmoitukset should equal(Seq.empty)
  }

  "kuntailmoitukset: getOppija palauttaa oppijasta tehdyn kuntailmoituksen kaikki tiedot ilmoituksen tekijälle" in {
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.valmistunutYsiluokkalainenJollaIlmoitus.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)
    val expectedIlmoitukset = Seq(expectedIlmoitus.withAktiivinen(true))

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: getOppija palauttaa oppijasta tehdyn kuntailmoituksen kaikki tiedot ilmoituksen kohdekunnalle" in {
    // Tässä testissä pitää toistaiseksi temppuilla oppijalla, jolla on monta opiskeluoikeutta, koska pelkällä kuntakäyttäjällä ei vielä ole oikeuksia
    // oppijan tietoihin. Oppijalla on siis ilmoitus Jyväskylä normaalikoulusta Pyhtäälle, ja lisäksi oppija opiskelee Aapajoen peruskoulussa.
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus.oid)(session(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu))
      .toOption.get

    val expectedIlmoitus = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)
    val expectedIlmoitukset = Seq(expectedIlmoitus.withAktiivinen(true))

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: getOppija palauttaa oppijasta tehdystä kuntailmoituksesta vain perustiedot muulle kuin tekijälle tai kunnalle" in {
    // Tässä testissä pitää toistaiseksi temppuilla oppijalla, jolla on monta opiskeluoikeutta, koska pelkällä kuntakäyttäjällä ei vielä ole oikeuksia
    // oppijan tietoihin. Oppijalla on siis ilmoitus Jyväskylän normaalikoulusta Pyhtäälle, ja lisäksi oppija opiskelee Aapajoen peruskoulussa.
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus.oid)(session(ValpasMockUsers.valpasHelsinkiJaAapajoenPeruskoulu))
      .toOption.get

    val expectedIlmoitusKaikkiTiedot = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)
    val expectedIlmoitus: ValpasKuntailmoitusLaajatTiedot = karsiPerustietoihin(expectedIlmoitusKaikkiTiedot)

    val expectedIlmoitukset = Seq(expectedIlmoitus.withAktiivinen(true))

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: palauttaa kaikki master- ja slave-oideille tehdyt ilmoitukset pyydettäessä master-oidilla" in {
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitukset = Seq(
      täydennäAikaleimallaJaOrganisaatiotiedoilla(karsiPerustietoihin(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta)).withAktiivinen(true),
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla).withAktiivinen(false)
    )

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: palauttaa kaikki master- ja slave-oideille tehdyt ilmoitukset pyydettäessä slave-oidilla" in {
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusKolmas.oid)(session(ValpasMockUsers.valpasHelsinkiJaAapajoenPeruskoulu))
      .toOption.get

    val expectedIlmoitukset = Seq(
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta).withAktiivinen(true),
      täydennäAikaleimallaJaOrganisaatiotiedoilla(karsiPerustietoihin(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)).withAktiivinen(false),
    )

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: aktiivinen jos on ilmoituksen tekemisen jälkeen vasta tulevaisuudessa alkava ov-suorittamiseen kelpaava opiskeluoikeus" in {
    val ilmoituksenTekopäivä = date(2021, 8, 1)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus =
      ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla.withOppijaOid(ValpasMockOppijat.lukionAloittanut.oid)
    kuntailmoitusRepository.create(ilmoitus, Seq.empty)

    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus =
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay)
        .withAktiivinen(true)

    validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
  }

  "kuntailmoitukset: passiivinen jos on ilmoituksen tekemisen jälkeen alkanut ov-suorittamiseen kelpaava opiskeluoikeus ja on kulunut 2 kk tai alle" in {
    val ilmoituksenTekopäivä = date(2021, 7, 15)
    val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus = ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla.withOppijaOid(ValpasMockOppijat.lukionAloittanut.oid)
    kuntailmoitusRepository.create(ilmoitus, Seq.empty)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus =
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay).withAktiivinen(false)

    validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
  }

  "kuntailmoitukset: ei-aktiivinen jos on ilmoituksen tekemisen jälkeen alkanut ov-suorittamiseen kelpaava opiskeluoikeus ja on kulunut yli 2 kk" in {
    val ilmoituksenTekopäivä = date(2021, 7, 15)
    val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina).plusDays(1)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus =
      ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla.withOppijaOid(
        ValpasMockOppijat.lukionAloittanut.oid
      )
    kuntailmoitusRepository.create(ilmoitus, Seq.empty)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus =
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay).withAktiivinen(false)

    validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
  }

  "kuntailmoitukset: aktiivinen, vaikka on yli 2 kk ilmoituksesta, mutta ei ole voimassaolevaa opiskeluoikeutta" in {
    val ilmoituksenTekopäivä = date(2021, 6, 10)
    val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina).plusDays(10)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus =
      ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta.withOppijaOid(ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid)
    kuntailmoitusRepository.create(ilmoitus, Seq.empty)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid)(session(ValpasMockUsers.valpasAapajoenKoulu))
      .toOption.get

    val expectedIlmoitus = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta, ilmoituksenTekopäivä.atStartOfDay)
    val expectedIlmoitukset = Seq(expectedIlmoitus.withAktiivinen(true))

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: aktiivinen, vaikka yli 2 kk ilmoituksesta, jos on ilmoituksen tekemisen jälkeen alkanut ov-suorittamiseen kelpaamaton opiskeluoikeus" in {
    val ilmoituksenTekopäivä = date(2021, 6, 10)
    val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina).plusDays(10)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus =
      ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla.withOppijaOid(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)
    kuntailmoitusRepository.create(ilmoitus, Seq.empty)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus =
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay).withAktiivinen(true)

    validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
  }

  "kuntailmoitukset: palautetaan ilmoitukset aikajärjestyksessä ja vain uusin on aktiivinen" in {
    val ilmoituksenTekopäivät = (1 to 3).map(date(2021, 8, _))
    val tarkastelupäivä = date(2021, 8, 30)

    ilmoituksenTekopäivät.map(
      tekopäivä => {
        rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tekopäivä)
        val ilmoitus =
          oppijanPuhelinnumerolla(
            tekopäivä.toString, // Tehdään varmuuden vuoksi ilmoituksista erilaisia myös muuten kuin aikaleiman osalta
            ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla
          ).withOppijaOid(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)
        kuntailmoitusRepository.create(ilmoitus, Seq.empty)
      }
    )

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitukset = Seq(
      täydennäAikaleimallaJaOrganisaatiotiedoilla(oppijanPuhelinnumerolla("2021-08-03", ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla), date(2021, 8, 3).atStartOfDay)
        .withAktiivinen(true),
      täydennäAikaleimallaJaOrganisaatiotiedoilla(oppijanPuhelinnumerolla("2021-08-02", ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla), date(2021, 8, 2).atStartOfDay)
        .withAktiivinen(false),
      täydennäAikaleimallaJaOrganisaatiotiedoilla(oppijanPuhelinnumerolla("2021-08-01", ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla), date(2021, 8, 1).atStartOfDay)
        .withAktiivinen(false)
    )

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "Peruskoulun hakeutumisen valvoja saa haettua oman oppilaitoksen oppijan tiedot" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu
    ) shouldBe true
  }

  "Peruskoulun hakeutumisen valvoja saa haettua 17 vuotta tänä vuonna täyttävän oman oppilaitoksen oppijan tiedot rajapäivään asti" in {
    rajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(
        rajapäivätService.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
      )

    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.turvakieltoOppija,
      ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu
    ) shouldBe true
  }

  "Peruskoulun hakeutumisen valvoja saa haettua 17 vuotta tänä vuonna täyttävän oman oppilaitoksen oppijan tiedot rajapäivän jälkeen" in {
    rajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(
        rajapäivätService.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä.plusDays(1)
      )

    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.turvakieltoOppija,
      ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu
    ) shouldBe true
  }

  "Peruskoulun hakeutumisen valvoja saa haettua 18 vuotta tänä vuonna täyttävän oman oppilaitoksen oppijan tiedot" in {
    val päivä2022 = date(2022, 1, 15)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(päivä2022)

    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.turvakieltoOppija,
      ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu
    ) shouldBe true
  }
  "Peruskoulun hakeutumisen valvoja ei saa haettua toisen oppilaitoksen oppijan tietoja" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      ValpasMockUsers.valpasHelsinkiPeruskoulu
    ) shouldBe false
  }

  "Käyttäjä, jolla hakeutumisen valvontaoikeudet koulutustoimijatasolla, näkee oppilaitoksen oppijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      ValpasMockUsers.valpasJklYliopisto
    ) shouldBe true
  }

  "Käyttäjä, jolla globaalit oikeudet, näkee oppijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
      ValpasMockUsers.valpasOphPääkäyttäjä
    ) shouldBe true
  }

  "Käyttäjä, jolla maksuttomuusoikeudet, näkee peruskoulusta valmistuneen oppijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
      ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä
    ) shouldBe true
  }

  "Käyttäjä, jolla kunnan oikeudet, näkee peruskoulusta valmistuneen oppijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
      ValpasMockUsers.valpasHelsinki
    ) shouldBe true
  }

  "Käyttäjä, jolla globaalit oikeudet, ei näe liian vanhaa oppijaa" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004,
      ValpasMockUsers.valpasOphPääkäyttäjä
    ) shouldBe false
  }

  "Käyttäjä, jolla globaalit oikeudet, ei näe oppijaa, joka on valmistunut peruskoulusta ennen lain rajapäivää" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.ennenLainRajapäivääPeruskoulustaValmistunut,
      ValpasMockUsers.valpasOphPääkäyttäjä
    ) shouldBe false
  }

  "Käyttäjä, jolla OPPILAITOS_HAKEUTUMINEN globaalit oikeudet, ei näe oppijaa, joka on valmistunut peruskoulusta yli 2 kk aiemmin" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.yli2kkAiemminPeruskoulustaValmistunut,
      ValpasMockUsers.valpasOphHakeutuminenPääkäyttäjä
    ) shouldBe false
  }

  "Käyttäjä, jolla vain globaalit OPPILAITOS_HAKEUTUMINEN oikeudet, ei näe lukio-oppijaa" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.lukioOpiskelija,
      ValpasMockUsers.valpasOphHakeutuminenPääkäyttäjä
    ) shouldBe false
  }

  "Käyttäjä, jolla globaalit oikeudet näkee lukio-oppijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.lukioOpiskelija,
      ValpasMockUsers.valpasOphPääkäyttäjä
    ) shouldBe true
  }

  "Käyttäjä, jolla maksuttomuusoikeudet näkee lukio-oppijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.lukioOpiskelija,
      ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä
    ) shouldBe true
  }

  "Käyttäjä, jolla kunnan oikeudet näkee lukio-oppijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.lukioOpiskelija,
      ValpasMockUsers.valpasHelsinki
    ) shouldBe true
  }

  "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet näkee lukio-oppijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.lukioOpiskelija,
      ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
    ) shouldBe true
  }

  "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet näkee lukio-oppijan vielä valmistumisen jälkeenkin, koska YO-tutkinto oletetaan olevan suorittamatta" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.lukiostaValmistunutOpiskelija,
      ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
    ) shouldBe true
  }

  "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet näkee ammattiopiskelijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.ammattikouluOpiskelija,
      ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu
    ) shouldBe true
  }

  "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet ei näe ammattiopiskelijaa valmistumisen jälkeen" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.ammattikoulustaValmistunutOpiskelija,
      ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu
    ) shouldBe false
  }


  "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet näkee nivelvaiheen opiskelijan" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.kulosaarenYsiluokkalainenJaJyväskylänNivelvaiheinen,
      ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
    ) shouldBe true
  }

  "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet ei näe nivelvaiheen opiskelijaa valmistumisen jälkeen" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.nivelvaiheestaValmistunutOpiskelija,
      ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
    ) shouldBe false
  }

  "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet ammattikouluun ei näe kaksoistutkinnon opiskelijaa valmistumisen jälkeen." in {
    // Näkyy ainoastaan lukiolle, päätetty niin.
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.kaksoistutkinnostaValmistunutOpiskelija,
      ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu
    ) shouldBe false
  }

  "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet lukioon näkee yhteistutkinnon opiskelijan vielä valmistumisen jälkeenkin, koska YO-tutkinto oletetaan olevan suorittamatta" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.kaksoistutkinnostaValmistunutOpiskelija,
      ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
    ) shouldBe true
  }

  "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet ei näe peruskoulun oppijaa" in {
    canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
    ) shouldBe false
  }

  "Kuntailmoitukset" - {
    "Kuntailmoitusten hakeminen kunnalle: palauttaa oikeat oppijat, case #1" in {
      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 8, 30))

      validateKunnanIlmoitetutOppijat(
        organisaatioOid = MockOrganisaatiot.helsinginKaupunki,
        user = ValpasMockUsers.valpasHelsinki
      )(Seq(
        ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia,
        ValpasMockOppijat.oppivelvollisuusKeskeytettyEiOpiskele
      ))
    }

    "Kuntailmoitusten hakeminen kunnalle: palauttaa oikeat oppijat, case #2" in {
      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 8, 30))

      validateKunnanIlmoitetutOppijat(
        organisaatioOid = MockOrganisaatiot.pyhtäänKunta,
        user = ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu
      )(Seq(
        ValpasMockOppijat.turvakieltoOppija,
        ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia,
        ValpasMockOppijat.lukionAloittanutJollaVanhaIlmoitus,
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster,
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2,
        ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus,
        ValpasMockOppijat.kasiinAstiToisessaKoulussaOllutJollaIlmoitus,
        ValpasMockOppijat.valmistunutYsiluokkalainenJollaIlmoitus,
        ValpasMockOppijat.ilmoituksenLisätiedotPoistettu,
        ValpasMockOppijat.oppivelvollisuusKeskeytettyEiOpiskele
      ))
    }
  }

  "Oppijalle, jonka kuntailmoituksista on poistettu lisätiedot, palautuu kuntailmoitukset vajailla tiedoilla" in {
    val oppija = ValpasMockOppijat.ilmoituksenLisätiedotPoistettu
    val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(oppija.oid)(defaultSession)

    result.map(_.kuntailmoitukset.map(_.tekijä)) shouldBe Right(Seq(
      ValpasKuntailmoituksenTekijäLaajatTiedot(
        organisaatio = OidOrganisaatio(MockOrganisaatiot.jyväskylänNormaalikoulu),
        henkilö = Some(ValpasKuntailmoituksenTekijäHenkilö(
          oid = Some(ValpasMockUsers.valpasJklNormaalikoulu.oid),
          etunimet = None,
          sukunimi = None,
          kutsumanimi = None,
          email = None,
          puhelinnumero = None,
        )),
      ))
    )

    result.map(_.kuntailmoitukset.map(_.oppijanYhteystiedot)) shouldBe Right(Seq(None))
  }

  "Oppivelvollisuutta ei pysty keskeyttämään ilman kunnan valvontaoikeuksia" in {
    val oppija = ValpasMockOppijat.valmistunutYsiluokkalainen
    val tekijäOrganisaatioOid = MockOrganisaatiot.jyväskylänNormaalikoulu
    val alku = rajapäivätService.tarkastelupäivä

    val result = oppijaLaajatTiedotService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
      oppijaOid = oppija.oid,
      alku = alku,
      loppu = None,
      tekijäOrganisaatioOid = tekijäOrganisaatioOid,
    ))(defaultSession)

    result.left.map(_.statusCode) shouldBe Left(403)
  }

  "Oppivelvollisuutta ei pysty keskeyttämään organisaation nimissä, jos siihen ei ole oikeuksia" in {
    val oppija = ValpasMockOppijat.valmistunutYsiluokkalainen
    val tekijäOrganisaatioOid = MockOrganisaatiot.jyväskylänNormaalikoulu
    val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
    val alku = rajapäivätService.tarkastelupäivä

    val result = oppijaLaajatTiedotService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
      oppijaOid = oppija.oid,
      alku = alku,
      loppu = None,
      tekijäOrganisaatioOid = tekijäOrganisaatioOid,
    ))(kuntaSession)

    result.left.map(_.statusCode) shouldBe Left(403)
  }

  "Oppivelvollisuuden pystyy keskeyttämään toistaiseksi kunnan valvontaoikeuksilla" in {
    val oppija = ValpasMockOppijat.valmistunutYsiluokkalainen
    val tekijäOrganisaatioOid = MockOrganisaatiot.helsinginKaupunki
    val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
    val alku = rajapäivätService.tarkastelupäivä

    val keskeytykset = oppijaLaajatTiedotService
      .getOppijaLaajatTiedotHakuJaYhteystiedoilla(oppija.oid)(kuntaSession)
      .map(_.oppivelvollisuudenKeskeytykset)

    keskeytykset shouldBe Right(Seq.empty)

    val result = oppijaLaajatTiedotService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
      oppijaOid = oppija.oid,
      alku = alku,
      loppu = None,
      tekijäOrganisaatioOid = tekijäOrganisaatioOid,
    ))(kuntaSession)

    val expectedKeskeytys = ValpasOppivelvollisuudenKeskeytys(
      id = result.toOption.get.id,
      tekijäOrganisaatioOid = tekijäOrganisaatioOid,
      alku = alku,
      loppu = None,
      voimassa = true,
      tulevaisuudessa = false,
    )

    result shouldBe Right(expectedKeskeytys)

    val keskeytykset2 = oppijaLaajatTiedotService
      .getOppijaLaajatTiedotHakuJaYhteystiedoilla(oppija.oid)(kuntaSession)
      .map(_.oppivelvollisuudenKeskeytykset)

    keskeytykset2 shouldBe Right(List(expectedKeskeytys))
  }

  "Oppivelvollisuuden pystyy keskeyttämään määräaikaisesti kunnan valvontaoikeuksilla" in {
    val oppija = ValpasMockOppijat.valmistunutYsiluokkalainen
    val tekijäOrganisaatioOid = MockOrganisaatiot.helsinginKaupunki
    val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
    val alku = rajapäivätService.tarkastelupäivä
    val loppu = alku.plusMonths(3)

    val result = oppijaLaajatTiedotService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
      oppijaOid = oppija.oid,
      alku = alku,
      loppu = Some(loppu),
      tekijäOrganisaatioOid = tekijäOrganisaatioOid,
    ))(kuntaSession)

    val expectedKeskeytys = ValpasOppivelvollisuudenKeskeytys(
      id = result.toOption.get.id,
      tekijäOrganisaatioOid = tekijäOrganisaatioOid,
      alku = alku,
      loppu = Some(loppu),
      voimassa = true,
      tulevaisuudessa = false,
    )

    result shouldBe Right(expectedKeskeytys)

    val keskeytykset = oppijaLaajatTiedotService
      .getOppijaLaajatTiedotHakuJaYhteystiedoilla(oppija.oid)(kuntaSession)
      .map(_.oppivelvollisuudenKeskeytykset)

    keskeytykset shouldBe Right(List(expectedKeskeytys))
  }

  "Oppivelvollisuutta ei voi keskeyttää ellei oppija ole ovl-lain alainen" in {
    val oppija = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004
    val tekijäOrganisaatioOid = MockOrganisaatiot.helsinginKaupunki
    val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
    val alku = rajapäivätService.tarkastelupäivä

    val result = oppijaLaajatTiedotService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
      oppijaOid = oppija.oid,
      alku = alku,
      loppu = None,
      tekijäOrganisaatioOid = tekijäOrganisaatioOid,
    ))(kuntaSession)

    result.left.map(_.statusCode) shouldBe Left(403)
  }
}
