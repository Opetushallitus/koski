package fi.oph.koski.valpas.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.fixture.DatabaseFixtureCreator
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema._
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat

class ValpasDatabaseFixtureCreator(application: KoskiApplication) extends DatabaseFixtureCreator(application, "opiskeluoikeus_valpas_fixture", "opiskeluoikeushistoria_valpas_fixture") {
  protected def oppijat = ValpasMockOppijat.defaultOppijat

  protected lazy val validatedOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = {
    defaultOpiskeluOikeudet.zipWithIndex.map { case ((henkilö, oikeus), index) =>
      timed(s"Validating fixture ${index}", 500) {
        validator.validateAsJson(Oppija(henkilö.toHenkilötiedotJaOid, List(oikeus))) match {
          case Right(oppija) => (henkilö, oppija.tallennettavatOpiskeluoikeudet.head)
          case Left(status) => throw new RuntimeException(
            s"Fixture insert failed for ${henkilö.etunimet} ${henkilö.sukunimi} with data ${JsonSerializer.write(oikeus)}: ${status}"
          )
        }
      }
    }
  }

  protected lazy val invalidOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = List()

  private def defaultOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = List(
    (ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021, ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus),
    (ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004, ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus),
    (ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia, ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein1),
    (ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia, ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2),
    (ValpasMockOppijat.lukioOpiskelija, ValpasExampleData.lukionOpiskeluoikeus),
    (ValpasMockOppijat.kasiluokkaKeskenKeväällä2021, ValpasExampleData.kasiluokkaKeskenKeväällä2021Opiskeluoikeus),
    (ValpasMockOppijat.kotiopetusMeneilläänOppija, ValpasExampleData.kotiopetusMeneilläänOpiskeluoikeus),
    (ValpasMockOppijat.kotiopetusMenneisyydessäOppija, ValpasExampleData.kotiopetusMenneisyydessäOpiskeluoikeus),
    (ValpasMockOppijat.eronnutOppija, ValpasExampleData.eronnutOpiskeluoikeusTarkastelupäivääEnnen),
    (ValpasMockOppijat.eronnutOppijaTarkastelupäivänä, ValpasExampleData.eronnutOpiskeluoikeusTarkastelupäivänä),
    (ValpasMockOppijat.eronnutOppijaTarkastelupäivänJälkeen, ValpasExampleData.eronnutOpiskeluoikeusTarkastelupäivänJälkeen),
    (ValpasMockOppijat.valmistunutYsiluokkalainen, ValpasExampleData.valmistunutYsiluokkalainen),
    (ValpasMockOppijat.luokalleJäänytYsiluokkalainen, ValpasExampleData.luokallejäänytYsiluokkalainen),
    (ValpasMockOppijat.luokallejäänytYsiluokkalainenJollaUusiYsiluokka, ValpasExampleData.luokallejäänytYsiluokkalainenJollaUusiYsiluokka),
    (ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKoulua, ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen),
    (ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKoulua, ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen),
    (ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta,  ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen2),
    (ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta, ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen2),
    (ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut, ValpasExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus),
    (ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut, ValpasExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus),
    (ValpasMockOppijat.lukionAloittanut, ValpasExampleData.valmistunutYsiluokkalainen),
    (ValpasMockOppijat.lukionAloittanut, ValpasExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä),
    (ValpasMockOppijat.lukionLokakuussaAloittanut, ValpasExampleData.valmistunutYsiluokkalainen),
    (ValpasMockOppijat.lukionLokakuussaAloittanut, ValpasExampleData.lukionOpiskeluoikeusAlkaa2021Lokakuussa),
    (ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster, ValpasExampleData.valmistunutYsiluokkalainen),
    (ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen, ValpasExampleData.lukionOpiskeluoikeus),
    (ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas, ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu),
    (ValpasMockOppijat.aapajoenPeruskoulustaValmistunut, ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu),
    (ValpasMockOppijat.ennenLainRajapäivääPeruskoulustaValmistunut, ValpasExampleData.ennenLainRajapäivääToisestaKoulustaValmistunutYsiluokkalainen),
    (ValpasMockOppijat.ennenLainRajapäivääPeruskoulustaValmistunut, ValpasExampleData.valmistunutYsiluokkalainen),
    (ValpasMockOppijat.yli2kkAiemminPeruskoulustaValmistunut, ValpasExampleData.yli2kkAiemminPeruskoulustaValmistunut),
    (ValpasMockOppijat.oppivelvollinenAloittanutJaEronnutTarkastelupäivänJälkeen, ValpasExampleData.oppivelvollinenAloittanutJaEronnutTarkastelupäivänJälkeenOpiskeluoikeus),
    (ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa, ValpasExampleData.valmistunutYsiluokkalainen),
    (ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa, ValpasExampleData.kesäYsiluokkaKesken), // Tämä on vähän huono esimerkki, mutta varmistelee sitä, että homma toimii myös sitten, kun aletaan tukea nivelvaihetta, jossa nämä tapaukset voivat olla yleisempiä
    (ValpasMockOppijat.turvakieltoOppija, ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus),
    (ValpasMockOppijat.hakukohteidenHakuEpäonnistuu, ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus),
  )
}
