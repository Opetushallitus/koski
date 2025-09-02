package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenExampleData.{ammatillinenTutkintoSuoritus, ammatillisetTutkinnonOsat, k3, pakollinenTutkinnonOsanSuoritus, puuteollisuudenPerustutkinnonSuoritus, puuteollisuudenPerustutkinto, stadinAmmattiopisto, stadinToimipiste, tietoJaViestintäTekniikanPerustutkinnonSuoritus}
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, opiskeluoikeusMitätöity, opiskeluoikeusValmistunut, suomenKieli}
import fi.oph.koski.documentation.ExamplesEsiopetus.{ostopalveluOpiskeluoikeus, peruskoulusuoritus, päiväkotisuoritus}
import fi.oph.koski.documentation.ExamplesPerusopetus.ysinOpiskeluoikeusKesken
import fi.oph.koski.documentation.VapaaSivistystyöExampleData.varsinaisSuomenKansanopisto
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{helsinki, oppilaitos}
import fi.oph.koski.documentation.{ExamplesEsiopetus, _}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.{jyväskylänNormaalikoulu, päiväkotiMajakka}
import fi.oph.koski.schema._

import java.net.InetAddress
import java.time.LocalDate
import java.time.LocalDate.{of => date}

class KoskiSpecificDatabaseFixtureCreator(application: KoskiApplication) extends DatabaseFixtureCreator(application, "opiskeluoikeus_fixture", "opiskeluoikeushistoria_fixture") {
  protected def oppijat = KoskiSpecificMockOppijat.defaultOppijat
  protected def kuntahistoriat = KoskiSpecificMockOppijat.defaultKuntahistoriat
  protected def turvakieltoKuntahistoriat = KoskiSpecificMockOppijat.defaultTurvakieltoKuntahistoriat

  protected lazy val invalidOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = {
    val validOpiskeluoikeus: AmmatillinenOpiskeluoikeus = updateFieldsAndValidateOpiskeluoikeus(AmmatillinenExampleData.opiskeluoikeus(tutkinto = tietoJaViestintäTekniikanPerustutkinnonSuoritus(stadinToimipiste)))
    val opiskeluoikeusJostaTunnisteenKoodiarvoPoistettu = validOpiskeluoikeus.copy(
      suoritukset = validOpiskeluoikeus.suoritukset.map{
        case tutkinnonSuoritus: AmmatillisenTutkinnonSuoritus => tutkinnonSuoritus.copy(koulutusmoduuli = tutkinnonSuoritus.koulutusmoduuli.copy(
          tutkinnonSuoritus.koulutusmoduuli.tunniste.copy(koodiarvo = "123456")
        ))
        case _ => throw new InternalError("Tällä ammatillisella opiskeluoikeudella ei pitäisi olla muita kuin ammatillisen tutkinnon suoritukssia")
      }
    )

    val validRakenteessaMontaKoulutuskoodiaOpiskeluoikeus: AmmatillinenOpiskeluoikeus = updateFieldsAndValidateOpiskeluoikeus(AmmatillinenExampleData.puuteollisuusOpiskeluoikeusKesken())
    val rakenteessaMontaKoulutuskoodiaOpiskeluoikeusJostaTunnisteenKoodiarvoPoistettu = validRakenteessaMontaKoulutuskoodiaOpiskeluoikeus.copy(
      suoritukset = validRakenteessaMontaKoulutuskoodiaOpiskeluoikeus.suoritukset.map{
        case tutkinnonSuoritus: AmmatillisenTutkinnonSuoritus => tutkinnonSuoritus.copy(koulutusmoduuli = tutkinnonSuoritus.koulutusmoduuli.copy(
          tutkinnonSuoritus.koulutusmoduuli.tunniste.copy(koodiarvo = "12345")
        ))
        case _ => throw new InternalError("Tällä ammatillisella opiskeluoikeudella ei pitäisi olla muita kuin ammatillisen tutkinnon suoritukssia")
      }
    )

    val validESHOpiskeluoikeus = updateFieldsAndValidateOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus)

    val hkiTallentaja = MockUsers.helsinkiTallentaja.toKoskiSpecificSession(application.käyttöoikeusRepository)
    List(
      (KoskiSpecificMockOppijat.organisaatioHistoria, validOpiskeluoikeus.copy(organisaatiohistoria = Some(AmmatillinenExampleData.opiskeluoikeudenOrganisaatioHistoria))),
      (
        KoskiSpecificMockOppijat.organisaatioHistoriallinen,
        updateFieldsAndValidateOpiskeluoikeus(PerusopetusOppijaMaaratRaporttiFixtures.eriOppilaitoksessa).copy(
          organisaatiohistoria = PerusopetusOppijaMaaratRaporttiFixtures.organisaatiohistoria
        )
      ),
      (
        KoskiSpecificMockOppijat.organisaatioHistoriallinen,
        updateFieldsAndValidateOpiskeluoikeus(PerusopetusOppijaMaaratRaporttiFixtures.eriOppilaitoksessaLisäopetus).copy(
          organisaatiohistoria = PerusopetusOppijaMaaratRaporttiFixtures.organisaatiohistoria
        )
      ),
      (KoskiSpecificMockOppijat.tunnisteenKoodiarvoPoistettu, opiskeluoikeusJostaTunnisteenKoodiarvoPoistettu),
      (KoskiSpecificMockOppijat.eskari, updateFieldsAndValidateOpiskeluoikeus(ostopalveluOpiskeluoikeus, hkiTallentaja)),
      (KoskiSpecificMockOppijat.eskari, updateFieldsAndValidateOpiskeluoikeus(ostopalveluOpiskeluoikeus.copy(
        suoritukset = List(päiväkotisuoritus(oppilaitos(päiväkotiMajakka))),
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          ostopalveluOpiskeluoikeus.tila.opiskeluoikeusjaksot.map(j => j.copy(alku = j.alku.minusDays(1)))
        )
      ), hkiTallentaja)),
      (KoskiSpecificMockOppijat.eskari, updateFieldsAndValidateOpiskeluoikeus(ostopalveluOpiskeluoikeus.copy(
        suoritukset = List(peruskoulusuoritus(oppilaitos(jyväskylänNormaalikoulu)).copy(vahvistus = None)),
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          ostopalveluOpiskeluoikeus.tila.opiskeluoikeusjaksot.map(j => j.copy(alku = date(2022, 6, 7)))
        ),
      ), hkiTallentaja)),
      (KoskiSpecificMockOppijat.rikkinäinenOpiskeluoikeus, MaksuttomuusRaporttiFixtures.opiskeluoikeusAmmatillinenMaksuttomuuttaPidennetty.copy(
        tyyppi = OpiskeluoikeudenTyyppi.ammatillinenkoulutus.copy(
          // Normaalisti validaattori täyttää nimen, nyt esitäytetään se itse. Jos tätä ei tehdä, esim. Koski
          // Pulssi menee sekaisin eikä tunnista opiskeluoikeutta ammatilliseksi opiskeluoikeudeksi.
          nimi = Some(LocalizedString.finnish("Ammatillinen koulutus"))
        )
      )),
      (KoskiSpecificMockOppijat.montaKoulutuskoodiaAmis, rakenteessaMontaKoulutuskoodiaOpiskeluoikeusJostaTunnisteenKoodiarvoPoistettu),
      // Lisätään opiskeluoikeus validoitumattomien joukossa, jotta organisaatiohistoria saadaan tallennettua
      (KoskiSpecificMockOppijat.opiskeleeAmmatillisessaErityisoppilaitoksessaOrganisaatioHistoriallinen,
        // Erityisoppilaitos esiintyy vain organisaatiohistoriassa
        AmmatillinenPerustutkintoExample.perustutkintoOpiskeluoikeusValmisOrganisaatiohistorialla(
          koulutustoimija = helsinki,
          oppilaitos = AmmatillinenExampleData.stadinAmmattiopisto,
          toimipiste = AmmatillinenExampleData.stadinToimipiste,
          organisaatioHistorianOppilaitos = AmmatillinenExampleData.kiipulanAmmattiopisto,
          vahvistuksenOrganisaatio = AmmatillinenExampleData.stadinAmmattiopisto
        ).copy(
          tyyppi = OpiskeluoikeudenTyyppi.ammatillinenkoulutus.copy(
            // Normaalisti validaattori täyttää nimen, nyt esitäytetään se itse. Jos tätä ei tehdä, esim. Koski
            // Pulssi menee sekaisin eikä tunnista opiskeluoikeutta ammatilliseksi opiskeluoikeudeksi.
            nimi = Some(LocalizedString.finnish("Ammatillinen koulutus"))
          ),
        ).withLisääPuuttuvaMaksuttomuustieto
      ),
      (KoskiSpecificMockOppijat.kelaRikkinäinenOpiskeluoikeus, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis().copy(
          tyyppi = OpiskeluoikeudenTyyppi.ammatillinenkoulutus.copy(
            // Normaalisti validaattori täyttää nimen, nyt esitäytetään se itse. Jos tätä ei tehdä, esim. Koski
            // Pulssi menee sekaisin eikä tunnista opiskeluoikeutta ammatilliseksi opiskeluoikeudeksi.
            nimi = Some(LocalizedString.finnish("Ammatillinen koulutus"))
          ),
        lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
          majoitus = Some(List(Aikajakso(alku = LocalDate.of(2022, 1, 1), loppu = Some(LocalDate.of(2020, 12, 31))))),
          hojks = None
        )),
        suoritukset = List(
          AmmatillinenExampleData.ympäristöalanPerustutkintoValmis().copy(
            suoritustapa = Koodistokoodiviite("rikkinäinenKoodi", "ammatillisentutkinnonsuoritustapa"),
            vahvistus = None,
            keskiarvo = None,
            osasuoritukset = Some(List(
              AmmatillinenExampleData
                .pakollinenTutkinnonOsanSuoritus(
                  "100431",
                  "Kestävällä tavalla toimiminen",
                  AmmatillinenExampleData.ammatillisetTutkinnonOsat,
                  AmmatillinenExampleData.k3,
                  40
                )
                .copy(arviointi = Some(List(AmmatillinenExampleData
                  .arviointi(AmmatillinenExampleData.k3)
                  .copy(päivä = date(2015, 1, 1)))),
                  lisätiedot = Some(List(
                    AmmatillinenExampleData.lisätietoMuutosArviointiasteikossa,
                    AmmatillinenExampleData.lisätietoOsaamistavoitteet
                  ))
                )
            ))
          )
        )
      )),
    )
  }

  protected def defaultOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = {
    List(
      (KoskiSpecificMockOppijat.eero, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, versio = Some(11))),
      (KoskiSpecificMockOppijat.eero, PerusopetuksenOpiskeluoikeusTestData.mitätöitäväOpiskeluoikeus),
      (KoskiSpecificMockOppijat.eerola, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, versio = Some(11))),
      (KoskiSpecificMockOppijat.teija, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, versio = Some(11))),
      (KoskiSpecificMockOppijat.syntymäajallinen, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, versio = Some(11))),
      (KoskiSpecificMockOppijat.markkanen, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia, versio = Some(11)).copy(ostettu = true)),
      (KoskiSpecificMockOppijat.eskari, ExamplesEsiopetus.esioppilas.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.eskariAikaisillaLisätiedoilla, ExamplesEsiopetus.esioppilasAikaisillaLisätiedoilla.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.ysiluokkalainen, ysinOpiskeluoikeusKesken),
      (KoskiSpecificMockOppijat.hetuton, ysinOpiskeluoikeusKesken),
      (KoskiSpecificMockOppijat.monessaKoulussaOllut, ysinOpiskeluoikeusKesken),
      (KoskiSpecificMockOppijat.monessaKoulussaOllut, ExamplesPerusopetus.seiskaTuplattuOpiskeluoikeus),
      (KoskiSpecificMockOppijat.koululainen, ExamplesEsiopetus.opiskeluoikeusHelsingissä),
      (KoskiSpecificMockOppijat.koululainen, PerusopetusExampleData.päättötodistusOpiskeluoikeus()),
      (KoskiSpecificMockOppijat.suoritusTuplana, PerusopetusExampleData.suoritusTuplana()),
      (KoskiSpecificMockOppijat.koululainen, ExamplesPerusopetukseenValmistavaOpetus.perusopetukseenValmistavaOpiskeluoikeus),
      (KoskiSpecificMockOppijat.luokallejäänyt, PerusopetusExampleData.päättötodistusLuokanTuplauksellaOpiskeluoikeus()),
      (KoskiSpecificMockOppijat.vuosiluokkalainen, PerusopetusExampleData.vuosiluokanOpiskeluoikeus()),
      (KoskiSpecificMockOppijat.toimintaAlueittainOpiskelija, ExamplesPerusopetus.toimintaAlueittainOpiskelija.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.oppiaineenKorottaja, ExamplesAikuistenPerusopetus.oppiaineenOppimääräOpiskeluoikeus),
      (KoskiSpecificMockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa, ExamplesAikuistenPerusopetus.montaOppiaineenOppimääränSuoritustaOpiskeluoikeus),
      (KoskiSpecificMockOppijat.aikuisOpiskelija, ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineenValmistunutVanhanOppivelvollisuuslainAikana),
      (KoskiSpecificMockOppijat.aikuisAineOpiskelijaMuuKuinVos, AikuistenPerusopetusOppijaMaaratRaporttiFixtures.aineOpiskelijaMuuKuinVos),
      (KoskiSpecificMockOppijat.aikuisOpiskelijaMuuKuinVos, AikuistenPerusopetusOppijaMaaratRaporttiFixtures.oppimääränSuorittajaMuuKuinVos),
      (KoskiSpecificMockOppijat.aikuisOpiskelijaVieraskielinen, AikuistenPerusopetusOppijaMaaratRaporttiFixtures.oppimääränSuorittaja),
      (KoskiSpecificMockOppijat.aikuisOpiskelijaVieraskielinenMuuKuinVos, AikuistenPerusopetusOppijaMaaratRaporttiFixtures.oppimääränSuorittajaMuuKuinVos),
      (KoskiSpecificMockOppijat.aikuisOpiskelijaMuuRahoitus, ExamplesAikuistenPerusopetus.oppiaineenOppimääräOpiskeluoikeusMuuRahoitus),
      (KoskiSpecificMockOppijat.kymppiluokkalainen, ExamplesPerusopetuksenLisaopetus.lisäopetuksenPäättötodistus.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.lukiolainen, PerusopetusExampleData.päättötodistusOpiskeluoikeus(luokka = "B")),
      (KoskiSpecificMockOppijat.lukiolainen, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.lukiolainen, AmmatillinenOpiskeluoikeusTestData.mitätöitäväOpiskeluoikeus),
      (KoskiSpecificMockOppijat.lukioKesken, ExamplesLukio.lukioKesken),
      (KoskiSpecificMockOppijat.lukionAineopiskelija, ExamplesLukio.aineopiskelija),
      (KoskiSpecificMockOppijat.lukionAineopiskelijaAktiivinen, ExamplesLukio.aineOpiskelijaAktiivinen),
      (KoskiSpecificMockOppijat.lukionEiTiedossaAineopiskelija, ExamplesLukio.aineOpiskelijaEiTiedossaOppiaineella),
      (KoskiSpecificMockOppijat.uusiLukio, ExamplesLukio2019.opiskeluoikeus),
      (KoskiSpecificMockOppijat.uusiLukionAineopiskelija, ExamplesLukio2019.oppiaineenOppimääräOpiskeluoikeus),
      (KoskiSpecificMockOppijat.luva, ExamplesLukioonValmistavaKoulutus.luvaTodistus.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.luva2019, ExamplesLukioonValmistavaKoulutus.luvaTodistus2019.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.ammattilainen, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis()),
      (KoskiSpecificMockOppijat.tutkinnonOsaaPienempiKokonaisuus, TutkinnonOsaaPienempiKokonaisuusExample.opiskeluoikeus),
      (KoskiSpecificMockOppijat.muuAmmatillinen, MuunAmmatillisenKoulutuksenExample.muuAmmatillinenKoulutusOpiskeluoikeus),
      (KoskiSpecificMockOppijat.muuAmmatillinenKokonaisuuksilla, MuunAmmatillisenKoulutuksenExample.muuAmmatillinenKoulutusKokonaisuuksillaOpiskeluoikeus),
      (KoskiSpecificMockOppijat.ammatilliseenTetäväänValmistavaMuuAmmatillinen, MuunAmmatillisenKoulutuksenExample.ammatilliseenTehtäväänValmistavaKoulutusOpiskeluoikeus),
      (KoskiSpecificMockOppijat.amis, AmmatillinenExampleData.perustutkintoOpiskeluoikeusKesken()),
      (KoskiSpecificMockOppijat.valma, ExamplesValma.valmaTodistus.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.telma, ExamplesTelma.telmaTodistus.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.ylioppilasLukiolainen, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.erikoisammattitutkinto, AmmattitutkintoExample.opiskeluoikeus),
      (KoskiSpecificMockOppijat.reformitutkinto, ReforminMukainenErikoisammattitutkintoExample.opiskeluoikeus),
      (KoskiSpecificMockOppijat.osittainenammattitutkinto, AmmatillinenPerustutkintoExample.osittainenPerustutkintoOpiskeluoikeus),
      (KoskiSpecificMockOppijat.osittainenAmmattitutkintoUseastaTutkinnostaValmis, AmmatillinenOsittainenUseistaTutkinnoista.osittainenPerustutkintoOpiskeluoikeus),
      (KoskiSpecificMockOppijat.osittainenAmmattitutkintoUseastaTutkinnostaKesken, AmmatillinenOsittainenUseistaTutkinnoista.keskeneräinenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.ammatillisenOsittainenRapsa,AmmatillinenOsittainenReformi.opiskeluoikeusRapsa),
      (KoskiSpecificMockOppijat.paikallinenTunnustettu, AmmatillinenPerustutkintoExample.tunnustettuPaikallinenTutkinnonOsaOpiskeluoikeus),
      (KoskiSpecificMockOppijat.tiedonsiirto, AmmatillinenOpiskeluoikeusTestData.lähdejärjestelmällinenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.perusopetuksenTiedonsiirto, PerusopetuksenOpiskeluoikeusTestData.lähdejärjestelmällinenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.omattiedot, PerusopetusExampleData.päättötodistusOpiskeluoikeus(luokka = "D")),
      (KoskiSpecificMockOppijat.omattiedot, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.omattiedotSlave.henkilö, AmmatillinenOldExamples.uusi.tallennettavatOpiskeluoikeudet(0)),
      (KoskiSpecificMockOppijat.ibFinal, ExamplesIB.opiskeluoikeus),
      (KoskiSpecificMockOppijat.ibPredicted, ExamplesIB.opiskeluoikeusPredictedGrades),
      (KoskiSpecificMockOppijat.ibPreIB2019, ExamplesIB.opiskeluoikeusPreIB2019),
      (KoskiSpecificMockOppijat.dia, ExamplesDIA.opiskeluoikeus),
      (KoskiSpecificMockOppijat.master, ExamplesPerusopetus.päättötodistus.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.slave.henkilö, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.turvakielto, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.erkkiEiperusteissa, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 334117, diaariNumero = "22/011/2004", versio = Some(11))),
      (KoskiSpecificMockOppijat.internationalschool, ExamplesInternationalSchool.opiskeluoikeus),
      (KoskiSpecificMockOppijat.europeanSchoolOfHelsinki, ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus),
      (KoskiSpecificMockOppijat.valviraaKiinnostavaTutkinto, AmmatillinenExampleData.sosiaaliJaTerveysalaOpiskeluoikeus()),
      (KoskiSpecificMockOppijat.valviraaKiinnostavaTutkintoKesken, AmmatillinenExampleData.sosiaaliJaTerveysalaOpiskeluoikeusKesken()),
      (KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia, ExamplesEsiopetus.esioppilas.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia, PerusopetusExampleData.päättötodistusOpiskeluoikeus()),
      (KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia, ExamplesPerusopetus.aineopiskelija.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalESHOpiskelijamaaratRaportti_nuortenOppimaara, LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.lukionOppimaaraNuorten),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalESHOpiskelijamaaratRaportti_aikuistenOppimaara, LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.lukionOppimaaraAikuisten),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalESHOpiskelijamaaratRaportti_aineopiskelija, LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.lukionAineopiskelija),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalESHOpiskelijamaaratRaportti_dia, LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.dia),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalESHOpiskelijamaaratRaportti_ib, LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.ib),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalESHOpiskelijamaaratRaportti_international, LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.international),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalESHOpiskelijamaaratRaportti_esh, LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.esh),
      (KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_tavallinen, PerusopetusOppijaMaaratRaporttiFixtures.tavallinen),
      (KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_erikois, PerusopetusOppijaMaaratRaporttiFixtures.erikois),
      (KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_virheellisestiSiirretty, PerusopetusOppijaMaaratRaporttiFixtures.virheellisestiSiirrettyVaikeastiVammainen),
      (KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_virheellisestiSiirrettyVieraskielinen, PerusopetusOppijaMaaratRaporttiFixtures.virheellisestiSiirrettyVammainen),
      (KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_tavallinen, PerusopetusOppijaMaaratRaporttiFixtures.tavallinenLisäopetus),
      (KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_erikois, PerusopetusOppijaMaaratRaporttiFixtures.erikoisLisäopetus),
      (KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_virheellisestiSiirretty, PerusopetusOppijaMaaratRaporttiFixtures.virheellisestiSiirrettyVaikeastiVammainenLisäopetus),
      (KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_virheellisestiSiirrettyVieraskielinen, PerusopetusOppijaMaaratRaporttiFixtures.virheellisestiSiirrettyVammainenLisäopetus),
      (KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_kotiopetus, PerusopetusOppijaMaaratRaporttiFixtures.kotiopetus),
      (KoskiSpecificMockOppijat.lukioKurssikertymaRaportti_oppimaara, LukioKurssikertymaRaporttiFixtures.oppimaara),
      (KoskiSpecificMockOppijat.lukioKurssikertymaRaportti_aineopiskelija_eronnut, LukioKurssikertymaRaporttiFixtures.aineopiskelijaEronnut),
      (KoskiSpecificMockOppijat.lukioKurssikertymaRaportti_aineopiskelija_valmistunut, LukioKurssikertymaRaporttiFixtures.aineopiskelijaValmistunut),
      (KoskiSpecificMockOppijat.luvaOpiskelijamaaratRaportti_nuortenOppimaara, LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.nuortenOppimaaraLuva),
      (KoskiSpecificMockOppijat.luvaOpiskelijamaaratRaportti_aikuistenOppimaara, LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.aikuistenOppimaaraLuva),
      (KoskiSpecificMockOppijat.paallekkaisiOpiskeluoikeuksia, PaallekkaisetOpiskeluoikeudetFixtures.ensimmainenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.paallekkaisiOpiskeluoikeuksia, PaallekkaisetOpiskeluoikeudetFixtures.keskimmainenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.paallekkaisiOpiskeluoikeuksia, PaallekkaisetOpiskeluoikeudetFixtures.viimeinenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.vapaaSivistystyöOppivelvollinen, VapaaSivistystyöExample.opiskeluoikeusKOPS),
      (KoskiSpecificMockOppijat.vapaaSivistystyöMaahanmuuttajienKotoutus, VapaaSivistystyöExample.opiskeluoikeusKOTO),
      (KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen),
      (KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKoulutus, VapaaSivistystyöExample.opiskeluoikeusLukutaito),
      (KoskiSpecificMockOppijat.vapaaSivistystyöOsaamismerkki, VapaaSivistystyöExample.opiskeluoikeusOsaamismerkki()),
      (KoskiSpecificMockOppijat.maksuttomuuttaPidennetty1, MaksuttomuusRaporttiFixtures.opiskeluoikeusAmmatillinenMaksuttomuuttaPidennetty),
      (KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2, MaksuttomuusRaporttiFixtures.opiskeluoikeusAmmatillinenMaksuttomuuttaPidennetty),
      (KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2, MaksuttomuusRaporttiFixtures.opiskeluoikeusLukioMaksuttomuuttaPidennetty),
      (KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021, MaksuttomuusRaporttiFixtures.peruskouluSuoritettu2021),
      (KoskiSpecificMockOppijat.vuonna2004SyntynytMuttaPeruskouluValmisEnnen2021, MaksuttomuusRaporttiFixtures.peruskouluSuoritettu2020),
      (KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021, MaksuttomuusRaporttiFixtures.peruskouluSuoritettu2021),
      (KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021EiKotikuntaaSuomessa, MaksuttomuusRaporttiFixtures.peruskouluSuoritettu2021),
      (KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021KotikuntaAhvenanmaalla, MaksuttomuusRaporttiFixtures.peruskouluSuoritettu2021),
      (KoskiSpecificMockOppijat.vuonna2004SyntynytMuttaEronnutPeruskoulustaEnnen2021, MaksuttomuusRaporttiFixtures.peruskouluEronnut2020),
      (KoskiSpecificMockOppijat.opiskeleeAmmatillisessaErityisoppilaitoksessa,
        // Normaali tapaus: opiskeluoikeuden oppilaitos on erityisoppilaitos
        AmmatillinenPerustutkintoExample.perustutkintoOpiskeluoikeusValmisOrganisaatiohistorialla().withLisääPuuttuvaMaksuttomuustieto
      ),
      (KoskiSpecificMockOppijat.opiskeleeAmmatillisessaErityisoppilaitoksessa2,
        // Erityisoppilaitos esiintyy vain suorituksen toimipisteessä
        AmmatillinenPerustutkintoExample.perustutkintoOpiskeluoikeusValmisOrganisaatiohistorialla(
          koulutustoimija = helsinki,
          oppilaitos = AmmatillinenExampleData.stadinAmmattiopisto,
          toimipiste = AmmatillinenExampleData.kiipulanAmmattiopisto,
          organisaatioHistorianOppilaitos = AmmatillinenExampleData.stadinAmmattiopisto,
          vahvistuksenOrganisaatio = AmmatillinenExampleData.stadinAmmattiopisto
        ).withLisääPuuttuvaMaksuttomuustieto
      ),
      (KoskiSpecificMockOppijat.opiskeleeAmmatillisessaErityisoppilaitoksessa,
        // Erityisoppilaitos esiintyy vain suorituksen vahvistuksen organisaationa
        AmmatillinenPerustutkintoExample.perustutkintoOpiskeluoikeusValmisOrganisaatiohistorialla(
          koulutustoimija = helsinki,
          oppilaitos = AmmatillinenExampleData.stadinAmmattiopisto,
          toimipiste = AmmatillinenExampleData.stadinToimipiste,
          organisaatioHistorianOppilaitos = AmmatillinenExampleData.stadinAmmattiopisto,
          vahvistuksenOrganisaatio = AmmatillinenExampleData.kiipulanAmmattiopisto
        ).withLisääPuuttuvaMaksuttomuustieto
      ),
      (KoskiSpecificMockOppijat.tuva, ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusValmistunut),
      (KoskiSpecificMockOppijat.tuvaPerus, ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusEiValmistunut),
      (KoskiSpecificMockOppijat.poistettuOpiskeluoikeus, VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen),
      (KoskiSpecificMockOppijat.nuortenPerusopetuksenOppimääräErityinenTutkinto, ExamplesPerusopetus.useampiNuortenPerusopetuksenOppiaineenOppimääränSuoritusSamassaOppiaineessaEriLuokkaAsteella),
      (KoskiSpecificMockOppijat.vstKoto2022Aloittaja, ExamplesVapaaSivistystyöKotoutuskoulutus2022.Opiskeluoikeus.juuriAloittanut),
      (KoskiSpecificMockOppijat.vstKoto2022Kesken, ExamplesVapaaSivistystyöKotoutuskoulutus2022.Opiskeluoikeus.keskeneräinen),
      (KoskiSpecificMockOppijat.vstKoto2022Suorittanut, ExamplesVapaaSivistystyöKotoutuskoulutus2022.Opiskeluoikeus.suoritettu),
      (KoskiSpecificMockOppijat.ajoneuvoalanOpiskelija, AmmatillinenReforminMukainenPerustutkinto2022Example.opiskeluoikeus),
      (KoskiSpecificMockOppijat.  jotpaAmmattikoululainen, AmmatillinenReforminMukainenPerustutkinto2022Example.opiskeluoikeusJotpa),
      (KoskiSpecificMockOppijat.vstJotpaKeskenOppija, ExamplesVapaaSivistystyöJotpa.Opiskeluoikeus.keskeneräinen),
      (KoskiSpecificMockOppijat.jotpaMuuKuinSäännelty, ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken),
      (KoskiSpecificMockOppijat.taiteenPerusopetusAloitettu, ExamplesTaiteenPerusopetus.Opiskeluoikeus.aloitettuYleinenOppimäärä),
      (KoskiSpecificMockOppijat.taiteenPerusopetusValmis, ExamplesTaiteenPerusopetus.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä),
      (KoskiSpecificMockOppijat.taiteenPerusopetusValmis, VapaaSivistystyöExample.opiskeluoikeusKOPS),
      (KoskiSpecificMockOppijat.taiteenPerusopetusHankintakoulutus, ExamplesTaiteenPerusopetus.Opiskeluoikeus.hankintakoulutuksenaHyväksytystiSuoritettuLaajaOppimäärä),
      (KoskiSpecificMockOppijat.amiksenKorottaja, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis()),
      (KoskiSpecificMockOppijat.ylioppilasLukiolainenMaksamatonSuoritus, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.ylioppilasLukiolainenVanhaSuoritus, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.ylioppilasLukiolainenTimeouttaava, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.ylioppilasLukiolainenRikki, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.amisKoulutusvienti, AmmatillinenExampleData.sosiaaliJaTerveysalaKoulutusvientiOpiskeluoikeus()),
      (KoskiSpecificMockOppijat.vanhanMallinenIBOppija, ExamplesIB.kaikkiArviointityypitArvioinnissaSisältäväVanhanmallinenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.ammattilainenVahvistettuTulevaisuudessa, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis(valmistumispäivä = LocalDate.now.plusDays(2))),
      (KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmisMontaOsaamisalaaMukanaPäivämäärätön(
        oppilaitos = AmmatillinenExampleData.kiipulanAmmattiopisto,
        toimipiste = AmmatillinenExampleData.kiipulanAmmattiopistoNokianToimipaikka)
      ),
      (KoskiSpecificMockOppijat.slaveAmmattilainen.henkilö, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmisMontaOsaamisalaa()),
      (KoskiSpecificMockOppijat.ammatilliseenTetäväänValmistavaMuuAmmatillinenVahvistettu, MuunAmmatillisenKoulutuksenExample.ammatilliseenTehtäväänValmistavaKoulutusOpiskeluoikeusVahvistettu),
      (KoskiSpecificMockOppijat.jotpaMuuKuinSäänneltySuoritettu, ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.suoritettu),
      (KoskiSpecificMockOppijat.lukioVajaaSuoritus, ExamplesLukio2019.aktiivinenVähänOpiskeltuOppiaineenOppimääräOpiskeluoikeus),
      (KoskiSpecificMockOppijat.pelkkäESH, ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus),
      (KoskiSpecificMockOppijat.tiedonsiirto, ExamplesVapaaSivistystyöJotpa.Opiskeluoikeus.keskeneräinenLähdejärjestelmästä),
      (KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia, ExamplesEsiopetus.esioppilas.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia, PerusopetusExampleData.päättötodistusOpiskeluoikeus()),
      (KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia, ExamplesPerusopetus.aineopiskelija.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia, VapaaSivistystyöExample.opiskeluoikeusOsaamismerkki()),
      (KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia, VapaaSivistystyöExample.opiskeluoikeusOsaamismerkki(koodiarvo = "1002")),
      (KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia, VapaaSivistystyöExample.opiskeluoikeusLukutaito),
      (KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia, PerusopetusExampleData.päättötodistusOpiskeluoikeus(oppilaitos = VapaaSivistystyöExampleData.varsinaisSuomenKansanopisto, toimipiste = VapaaSivistystyöExampleData.varsinaisSuomenKansanopisto)),
      (KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia, ExamplesTaiteenPerusopetus.Opiskeluoikeus.aloitettuYleinenOppimäärä),
      (KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.varsinaisSuomenKansanopisto)),
      (KoskiSpecificMockOppijat.suomeenTäysiikäisenäMuuttanut, AmmatillinenReforminMukainenPerustutkinto2022Example.opiskeluoikeusJotpa),
      (KoskiSpecificMockOppijat.suomeenAlaikäisenäMuuttanut, AmmatillinenReforminMukainenPerustutkinto2022Example.opiskeluoikeusJotpa),
      (KoskiSpecificMockOppijat.ulkomaillaHetkenAsunut, AmmatillinenReforminMukainenPerustutkinto2022Example.opiskeluoikeusJotpa),
      (KoskiSpecificMockOppijat.suomeenAhvenanmaaltaTäysiikäisenäMuuttanut, AmmatillinenReforminMukainenPerustutkinto2022Example.opiskeluoikeusJotpa),
      (KoskiSpecificMockOppijat.vapaaSivistystyöMaksuttomuus, VapaaSivistystyöExample.opiskeluoikeusKOPSMaksuttomuus),
      (KoskiSpecificMockOppijat.vuonna2003SyntynytPeruskouluValmis2021, MaksuttomuusRaporttiFixtures.peruskouluSuoritettu2021),
      (KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021MuuttanutSuomeenTäysiIkäisenä, MaksuttomuusRaporttiFixtures.peruskouluSuoritettu2021),
      (KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021EiKotikuntahistoriaa, MaksuttomuusRaporttiFixtures.peruskouluSuoritettu2021),
      (KoskiSpecificMockOppijat.vainMitätöityjäOpiskeluoikeuksia, PerusopetuksenOpiskeluoikeusTestData.mitätöitäväOpiskeluoikeus),
      (KoskiSpecificMockOppijat.kielitutkinnonSuorittaja, ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2011, 1, 3), "FI", "kt")),
      (KoskiSpecificMockOppijat.kielitutkinnonSuorittaja, ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2023, 11, 17), "SV", "pt")),
      (KoskiSpecificMockOppijat.kielitutkinnonSuorittaja, ExamplesKielitutkinto.ValtionhallinnonKielitutkinnot.Opiskeluoikeus.valmis(LocalDate.of(2020, 9, 10), "FI", List("kirjallinen", "suullinen"), "hyvajatyydyttava")),
      (KoskiSpecificMockOppijat.kielitutkinnonSuorittaja, ExamplesKielitutkinto.ValtionhallinnonKielitutkinnot.Opiskeluoikeus.valmis(LocalDate.of(2022, 8, 12), "FI", List("kirjallinen"), "erinomainen")),
      (KoskiSpecificMockOppijat.kielitutkinnonSuorittaja, ExamplesKielitutkinto.ValtionhallinnonKielitutkinnot.Opiskeluoikeus.keskeneräinen(LocalDate.of(2022, 8, 12), "SV", List("kirjallinen"), "hyvajatyydyttava")),
      (KoskiSpecificMockOppijat.keskeneräinenIbTutkinto, ExamplesIB.keskeneräinenIbTutkintoOpiskeluoikeus(LocalDate.of(2025, 7, 30))),
      (KoskiSpecificMockOppijat.esikoululainen2025, ExamplesEsiopetus.opiskeluoikeus2025),
    )
  }

  protected def secondBatchOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = {
    List(
      (KoskiSpecificMockOppijat.europeanSchoolOfHelsinki, ExamplesEB.opiskeluoikeus), // Ei validoidu ilman, että ESH-opiskeluoikeus on jo aikaisemmin tietokannassa
    )
  }

  protected def thirdBatchPäivitettävätOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = {
    List(
      (KoskiSpecificMockOppijat.eero, PerusopetuksenOpiskeluoikeusTestData.mitätöityOpiskeluoikeus), // Mitätöintiä ei voi tehdä ensimmäisellä kirjoituksella
      (KoskiSpecificMockOppijat.lukiolainen, AmmatillinenOpiskeluoikeusTestData.mitätöityOpiskeluoikeus), // Mitätöintiä ei voi tehdä ensimmäisellä kirjoituksella
      (KoskiSpecificMockOppijat.vainMitätöityjäOpiskeluoikeuksia, PerusopetuksenOpiskeluoikeusTestData.mitätöityOpiskeluoikeus), // Mitätöintiä ei voi tehdä ensimmäisellä kirjoituksella
    )
  }

  override def resetFixtures: Unit = {
    super.resetFixtures
    peruutaSuostumusOpiskeluoikeudelta()
  }

  private def peruutaSuostumusOpiskeluoikeudelta(): Unit = {
    val oppijaOid = KoskiSpecificMockOppijat.poistettuOpiskeluoikeus.oid
    peruutaSuostumusOpiskeluoikeudelta(
      oppijaOid = oppijaOid,
      opiskeluoikeusOid = application.oppijaFacade.findOppija(oppijaOid)(KoskiSpecificSession.systemUser).right.get.map(_.opiskeluoikeudet.head.oid.get)._value
    )
  }

  def peruutaSuostumusOpiskeluoikeudelta(oppijaOid: String, opiskeluoikeusOid: String): Boolean = {
    val oppija = oppijat.find(_.henkilö.oid == oppijaOid).get.henkilö
    val user = new KoskiSpecificSession(AuthenticationUser(oppija.oid, oppija.etunimet, oppija.etunimet, None), "fi", InetAddress.getLocalHost, "", Set())
    val poistoResult = application.suostumuksenPeruutusService.peruutaSuostumus(opiskeluoikeusOid, None)(user)
    poistoResult.isOk
  }
}

object AmmatillinenOpiskeluoikeusTestData {
  def opiskeluoikeus(oppilaitosId: String, koulutusKoodi: Int = 351301, diaariNumero: String = "39/011/2014", versio: Option[Int] = None, alkamispäivä: LocalDate = date(2019, 5, 30)): AmmatillinenOpiskeluoikeus = {
    val oppilaitos: Oppilaitos = Oppilaitos(oppilaitosId, None, None)
    val koulutusKoodiViite = Koodistokoodiviite(koulutusKoodi.toString, None, "koulutus", versio)

    AmmatillinenOpiskeluoikeus(
      oppilaitos = Some(oppilaitos),
      suoritukset = List(AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(koulutusKoodiViite, Some(diaariNumero)),
        toimipiste = oppilaitos,
        suorituskieli = suomenKieli,
        suoritustapa = AmmatillinenExampleData.suoritustapaOps
      )),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)))),
      lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
        hojks = None,
        erityinenTuki = Some(List(Aikajakso(alkamispäivä, None))),
        vaikeastiVammainen = Some(List(Aikajakso(alkamispäivä, None))),
        vankilaopetuksessa = Some(List(Aikajakso(alkamispäivä, None)))
      )),
    )
  }

  def päättynytOpiskeluoikeus(oppilaitosId: String, koulutusKoodi: Int = 351301, diaariNumero: String = "39/011/2014", alkamispäivä: LocalDate = date(2019, 5, 30), päättymispäivä: LocalDate = date(2020, 5, 30)): AmmatillinenOpiskeluoikeus = {
    val oppilaitos: Oppilaitos = Oppilaitos(oppilaitosId, None, None)
    val koulutusKoodiViite = Koodistokoodiviite(koulutusKoodi.toString, None, "koulutus", None)

    AmmatillinenOpiskeluoikeus(
      oppilaitos = Some(oppilaitos),
      suoritukset = List(AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(koulutusKoodiViite, Some(diaariNumero)),
        toimipiste = oppilaitos,
        suorituskieli = suomenKieli,
        suoritustapa = AmmatillinenExampleData.suoritustapaOps,
        keskiarvo = Some(4.0),
        vahvistus = Some(HenkilövahvistusValinnaisellaPaikkakunnalla(
          päivä = alkamispäivä,
          myöntäjäOrganisaatio = oppilaitos,
          myöntäjäHenkilöt = List(Organisaatiohenkilö("Reksi Rehtori", LocalizedString.finnish("rehtori"), oppilaitos)
        ))),
        osasuoritukset = Some(List(pakollinenTutkinnonOsanSuoritus("101050", "Yritystoiminnan suunnittelu", ammatillisetTutkinnonOsat, k3, 40, alkamispäivä)))
      )),
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
        AmmatillinenOpiskeluoikeusjakso(päättymispäivä, ExampleData.opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
      )),
      lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
        hojks = None,
        erityinenTuki = Some(List(Aikajakso(alkamispäivä, None))),
        vaikeastiVammainen = Some(List(Aikajakso(alkamispäivä, None))),
        vankilaopetuksessa = Some(List(Aikajakso(alkamispäivä, None)))
      )),
    )
  }

  def katsotaanEronneeksiOpiskeluoikeus(oppilaitosId: String, koulutusKoodi: Int = 351301, diaariNumero: String = "39/011/2014", alkamispäivä: LocalDate = date(2019, 5, 30), päättymispäivä: LocalDate = date(2020, 5, 30)): AmmatillinenOpiskeluoikeus = {
    val oppilaitos: Oppilaitos = Oppilaitos(oppilaitosId, None, None)
    val koulutusKoodiViite = Koodistokoodiviite(koulutusKoodi.toString, None, "koulutus", None)

    AmmatillinenOpiskeluoikeus(
      oppilaitos = Some(oppilaitos),
      suoritukset = List(AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(koulutusKoodiViite, Some(diaariNumero)),
        toimipiste = oppilaitos,
        suorituskieli = suomenKieli,
        suoritustapa = AmmatillinenExampleData.suoritustapaOps,
        keskiarvo = Some(4.0),
        osasuoritukset = Some(List(pakollinenTutkinnonOsanSuoritus("101050", "Yritystoiminnan suunnittelu", ammatillisetTutkinnonOsat, k3, 40, alkamispäivä)))
      )),
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
        AmmatillinenOpiskeluoikeusjakso(päättymispäivä, ExampleData.opiskeluoikeusKatsotaanEronneeksi, Some(ExampleData.valtionosuusRahoitteinen))
      )),
      lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
        hojks = None,
        erityinenTuki = Some(List(Aikajakso(alkamispäivä, None))),
        vaikeastiVammainen = Some(List(Aikajakso(alkamispäivä, None))),
        vankilaopetuksessa = Some(List(Aikajakso(alkamispäivä, None)))
      )),
    )
  }

  lazy val lähdejärjestelmällinenOpiskeluoikeus: AmmatillinenOpiskeluoikeus =
    opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, versio = Some(11)).copy(lähdejärjestelmänId = Some(AmmatillinenExampleData.winnovaLähdejärjestelmäId("l-050504")))

  lazy val mitätöitäväOpiskeluoikeus: AmmatillinenOpiskeluoikeus =
    opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, versio = Some(11))
      .copy(lähdejärjestelmänId = Some(AmmatillinenExampleData.winnovaLähdejärjestelmäId("l-010203")))

  lazy val mitätöityOpiskeluoikeus: AmmatillinenOpiskeluoikeus = {
    mitätöitäväOpiskeluoikeus.copy(
      tila = mitätöitäväOpiskeluoikeus.tila.copy(
        opiskeluoikeusjaksot = mitätöitäväOpiskeluoikeus.tila.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.now, opiskeluoikeusMitätöity)
      )
    )
  }
}

object PerusopetuksenOpiskeluoikeusTestData {
  lazy val lähdejärjestelmällinenOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    PerusopetusExampleData.päättötodistusOpiskeluoikeus(oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, None, None)).copy(
      lähdejärjestelmänId = Some(AmmatillinenExampleData.primusLähdejärjestelmäId("l-0303032"))
    )

  lazy val mitätöitäväOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    ysinOpiskeluoikeusKesken.copy(
      lähdejärjestelmänId = Some(AmmatillinenExampleData.primusLähdejärjestelmäId("l-010101"))
    )

  lazy val mitätöityOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    mitätöitäväOpiskeluoikeus.copy(tila =
      mitätöitäväOpiskeluoikeus.tila.copy(opiskeluoikeusjaksot =
        mitätöitäväOpiskeluoikeus.tila.opiskeluoikeusjaksot :+ NuortenPerusopetuksenOpiskeluoikeusjakso(alku = LocalDate.now, opiskeluoikeusMitätöity)
      )
    )
}
