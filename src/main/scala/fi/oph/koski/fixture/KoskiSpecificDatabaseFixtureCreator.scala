package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenExampleData.{ammatillinenTutkintoSuoritus, ammatillisetTutkinnonOsat, k3, puuteollisuudenPerustutkinnonSuoritus, puuteollisuudenPerustutkinto, stadinAmmattiopisto, stadinToimipiste, tietoJaViestintäTekniikanPerustutkinnonSuoritus, tutkinnonOsanSuoritus}
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusMitätöity, suomenKieli}
import fi.oph.koski.documentation.ExamplesEsiopetus.{ostopalveluOpiskeluoikeus, peruskoulusuoritus, päiväkotisuoritus}
import fi.oph.koski.documentation.ExamplesPerusopetus.ysinOpiskeluoikeusKesken
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
        )
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
            osasuoritukset = Some(List(
              AmmatillinenExampleData
                .tutkinnonOsanSuoritus(
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
      ))
    )
  }

  protected def defaultOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = {
    List(
      (KoskiSpecificMockOppijat.eero, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (KoskiSpecificMockOppijat.eero, PerusopetuksenOpiskeluoikeusTestData.mitätöityOpiskeluoikeus),
      (KoskiSpecificMockOppijat.eerola, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (KoskiSpecificMockOppijat.teija, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (KoskiSpecificMockOppijat.syntymäajallinen, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (KoskiSpecificMockOppijat.markkanen, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia).copy(ostettu = true)),
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
      (KoskiSpecificMockOppijat.aikuisOpiskelija, ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen),
      (KoskiSpecificMockOppijat.aikuisAineOpiskelijaMuuKuinVos, AikuistenPerusopetusOppijaMaaratRaporttiFixtures.aineOpiskelijaMuuKuinVos),
      (KoskiSpecificMockOppijat.aikuisOpiskelijaMuuKuinVos, AikuistenPerusopetusOppijaMaaratRaporttiFixtures.oppimääränSuorittajaMuuKuinVos),
      (KoskiSpecificMockOppijat.aikuisOpiskelijaVieraskielinen, AikuistenPerusopetusOppijaMaaratRaporttiFixtures.oppimääränSuorittaja),
      (KoskiSpecificMockOppijat.aikuisOpiskelijaVieraskielinenMuuKuinVos, AikuistenPerusopetusOppijaMaaratRaporttiFixtures.oppimääränSuorittajaMuuKuinVos),
      (KoskiSpecificMockOppijat.aikuisOpiskelijaMuuRahoitus, ExamplesAikuistenPerusopetus.oppiaineenOppimääräOpiskeluoikeusMuuRahoitus),
      (KoskiSpecificMockOppijat.kymppiluokkalainen, ExamplesPerusopetuksenLisaopetus.lisäopetuksenPäättötodistus.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.lukiolainen, PerusopetusExampleData.päättötodistusOpiskeluoikeus(luokka = "B")),
      (KoskiSpecificMockOppijat.lukiolainen, ExamplesLukio.päättötodistus()),
      (KoskiSpecificMockOppijat.lukiolainen, AmmatillinenOpiskeluoikeusTestData.mitätöityOpiskeluoikeus),
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
      (KoskiSpecificMockOppijat.ammatillisenOsittainenRapsa,AmmatillinenPerustutkintoExample.osittainenPerustutkintoOpiskeluoikeus.copy(suoritukset = List(AmmatillinenExampleData.ammatillisenTutkinnonOsittainenAutoalanSuoritus))),
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
      (KoskiSpecificMockOppijat.erkkiEiperusteissa, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 334117, diaariNumero = "22/011/2004")),
      (KoskiSpecificMockOppijat.internationalschool, ExamplesInternationalSchool.opiskeluoikeus),
      (KoskiSpecificMockOppijat.valviraaKiinnostavaTutkinto, AmmatillinenExampleData.sosiaaliJaTerveysalaOpiskeluoikeus()),
      (KoskiSpecificMockOppijat.valviraaKiinnostavaTutkintoKesken, AmmatillinenExampleData.sosiaaliJaTerveysalaOpiskeluoikeusKesken()),
      (KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia, ExamplesEsiopetus.esioppilas.tallennettavatOpiskeluoikeudet.head),
      (KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia, PerusopetusExampleData.päättötodistusOpiskeluoikeus()),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_nuortenOppimaara, LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.lukionOppimaaraNuorten),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_aikuistenOppimaara, LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.lukionOppimaaraAikuisten),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_aineopiskelija, LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.lukionAineopiskelija),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_dia, LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.dia),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_ib, LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.ib),
      (KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_international, LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.international),
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
      (KoskiSpecificMockOppijat.luvaOpiskelijamaaratRaportti_nuortenOppimaara, LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.nuortenOppimaaraLuva),
      (KoskiSpecificMockOppijat.luvaOpiskelijamaaratRaportti_aikuistenOppimaara, LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.aikuistenOppimaaraLuva),
      (KoskiSpecificMockOppijat.paallekkaisiOpiskeluoikeuksia, PaallekkaisetOpiskeluoikeudetFixtures.ensimmainenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.paallekkaisiOpiskeluoikeuksia, PaallekkaisetOpiskeluoikeudetFixtures.keskimmainenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.paallekkaisiOpiskeluoikeuksia, PaallekkaisetOpiskeluoikeudetFixtures.viimeinenOpiskeluoikeus),
      (KoskiSpecificMockOppijat.vapaaSivistystyöOppivelvollinen, VapaaSivistystyöExample.opiskeluoikeusKOPS),
      (KoskiSpecificMockOppijat.vapaaSivistystyöMaahanmuuttajienKotoutus, VapaaSivistystyöExample.opiskeluoikeusKOTO),
      (KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen),
      (KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKotoutus, VapaaSivistystyöExample.opiskeluoikeusLukutaito),
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
        AmmatillinenPerustutkintoExample.perustutkintoOpiskeluoikeusValmisOrganisaatiohistorialla()
      ),
      (KoskiSpecificMockOppijat.opiskeleeAmmatillisessaErityisoppilaitoksessa,
        // Erityisoppilaitos esiintyy vain suorituksen toimipisteessä
        AmmatillinenPerustutkintoExample.perustutkintoOpiskeluoikeusValmisOrganisaatiohistorialla(
          koulutustoimija = helsinki,
          oppilaitos = AmmatillinenExampleData.stadinAmmattiopisto,
          toimipiste = AmmatillinenExampleData.kiipulanAmmattiopisto,
          organisaatioHistorianOppilaitos = AmmatillinenExampleData.stadinAmmattiopisto,
          vahvistuksenOrganisaatio = AmmatillinenExampleData.stadinAmmattiopisto
        )
      ),
      (KoskiSpecificMockOppijat.opiskeleeAmmatillisessaErityisoppilaitoksessa,
        // Erityisoppilaitos esiintyy vain suorituksen vahvistuksen organisaationa
        AmmatillinenPerustutkintoExample.perustutkintoOpiskeluoikeusValmisOrganisaatiohistorialla(
          koulutustoimija = helsinki,
          oppilaitos = AmmatillinenExampleData.stadinAmmattiopisto,
          toimipiste = AmmatillinenExampleData.stadinToimipiste,
          organisaatioHistorianOppilaitos = AmmatillinenExampleData.stadinAmmattiopisto,
          vahvistuksenOrganisaatio = AmmatillinenExampleData.kiipulanAmmattiopisto
        )
      ),
      (KoskiSpecificMockOppijat.tuva, ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusValmistunut),
      (KoskiSpecificMockOppijat.tuvaPerus, ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusEiValmistunut),
      (KoskiSpecificMockOppijat.poistettuOpiskeluoikeus, VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen),
      (KoskiSpecificMockOppijat.nuortenPerusopetuksenOppimääräErityinenTutkinto, ExamplesPerusopetus.useampiNuortenPerusopetuksenOppiaineenOppimääränSuoritusSamassaOppiaineessaEriLuokkaAsteella),
      (KoskiSpecificMockOppijat.vstKoto2022Aloittaja, ExamplesVapaaSivistystyöKotoutuskoulutus2022.Opiskeluoikeus.juuriAloittanut),
      (KoskiSpecificMockOppijat.vstKoto2022Kesken, ExamplesVapaaSivistystyöKotoutuskoulutus2022.Opiskeluoikeus.keskeneräinen),
      (KoskiSpecificMockOppijat.vstKoto2022Suorittanut, ExamplesVapaaSivistystyöKotoutuskoulutus2022.Opiskeluoikeus.suoritettu),
      (KoskiSpecificMockOppijat.ajoneuvoalanOpiskelija, AmmatillinenReforminMukainenPerustutkinto2022Example.opiskeluoikeus),
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
    val poistoResult = application.suostumuksenPeruutusService.peruutaSuostumus(opiskeluoikeusOid)(user)
    poistoResult.isOk
  }
}

object AmmatillinenOpiskeluoikeusTestData {
  def opiskeluoikeus(oppilaitosId: String, koulutusKoodi: Int = 351301, diaariNumero: String = "39/011/2014", versio: Option[Int] = Some(11)): AmmatillinenOpiskeluoikeus = {
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
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2019, 5, 30), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)))),
      lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
        hojks = None,
        erityinenTuki = Some(List(Aikajakso(date(2019, 5, 30), None))),
        vaikeastiVammainen = Some(List(Aikajakso(date(2019, 5, 30), None))),
        vankilaopetuksessa = Some(List(Aikajakso(date(2019, 5, 30), None)))
      )),
    )
  }

  def päättynytOpiskeluoikeus(oppilaitosId: String, koulutusKoodi: Int = 351301, diaariNumero: String = "39/011/2014"): AmmatillinenOpiskeluoikeus = {
    val oppilaitos: Oppilaitos = Oppilaitos(oppilaitosId, None, None)
    val koulutusKoodiViite = Koodistokoodiviite(koulutusKoodi.toString, None, "koulutus", None)

    AmmatillinenOpiskeluoikeus(
      oppilaitos = Some(oppilaitos),
      suoritukset = List(AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(koulutusKoodiViite, Some(diaariNumero)),
        toimipiste = oppilaitos,
        suorituskieli = suomenKieli,
        suoritustapa = AmmatillinenExampleData.suoritustapaOps,
        vahvistus = Some(HenkilövahvistusValinnaisellaPaikkakunnalla(
          päivä = LocalDate.of(2019, 5, 15),
          myöntäjäOrganisaatio = oppilaitos,
          myöntäjäHenkilöt = List(Organisaatiohenkilö("Reksi Rehtori", LocalizedString.finnish("rehtori"), oppilaitos)
        ))),
        osasuoritukset = Some(List(tutkinnonOsanSuoritus("101050", "Yritystoiminnan suunnittelu", ammatillisetTutkinnonOsat, k3, 40)))
      )),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2020, 5, 30), ExampleData.opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen)))),
      lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
        hojks = None,
        erityinenTuki = Some(List(Aikajakso(date(2019, 5, 30), None))),
        vaikeastiVammainen = Some(List(Aikajakso(date(2019, 5, 30), None))),
        vankilaopetuksessa = Some(List(Aikajakso(date(2019, 5, 30), None)))
      )),
    )
  }

  lazy val lähdejärjestelmällinenOpiskeluoikeus: AmmatillinenOpiskeluoikeus =
    opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto).copy(lähdejärjestelmänId = Some(AmmatillinenExampleData.winnovaLähdejärjestelmäId("l-050504")))

  lazy val mitätöityOpiskeluoikeus: AmmatillinenOpiskeluoikeus = {
    val baseOo = opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)
    baseOo.copy(
      tila = baseOo.tila.copy(
        opiskeluoikeusjaksot = baseOo.tila.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.now, opiskeluoikeusMitätöity)
      )
    )
  }}

object PerusopetuksenOpiskeluoikeusTestData {
  lazy val lähdejärjestelmällinenOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    PerusopetusExampleData.päättötodistusOpiskeluoikeus(oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, None, None)).copy(
      lähdejärjestelmänId = Some(AmmatillinenExampleData.primusLähdejärjestelmäId("l-0303032"))
    )

  lazy val mitätöityOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    ysinOpiskeluoikeusKesken.copy(tila =
      ysinOpiskeluoikeusKesken.tila.copy(opiskeluoikeusjaksot =
        ysinOpiskeluoikeusKesken.tila.opiskeluoikeusjaksot :+ NuortenPerusopetuksenOpiskeluoikeusjakso(alku = LocalDate.now, opiskeluoikeusMitätöity)
      )
    )
}
