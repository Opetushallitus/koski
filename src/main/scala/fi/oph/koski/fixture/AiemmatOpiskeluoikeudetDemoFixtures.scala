package fi.oph.koski.fixture

import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut, suomenKieli, vahvistusPaikkakunnalla, valtionosuusRahoitteinen}
import fi.oph.koski.documentation.ExamplesLukio2019.lukionOppimäärä2019
import fi.oph.koski.documentation.Lukio2019ExampleData.{lukionÄidinkieli, moduulinSuoritusOppiaineissa, numeerinenArviointi, oppiaineenSuoritus, äidinkielenModuuliOppiaineissa}
import fi.oph.koski.documentation.LukioExampleData.nuortenOpetussuunnitelma
import fi.oph.koski.documentation.{ExamplesEsiopetus, PerusopetusExampleData}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

// Demoaineisto aiemmatOpiskeluoikeudet-massaluovutuskyselylle: Helsingin kaupungin nykyisiä opiskelijoita Stadin ammattiopistossa
// ja Kulosaaren ala-asteella, joiden aiemmat opinnot ovat Torniossa (ei muiden testien laskemissa organisaatioissa) tai Omniassa.
// Kysely hyväksyy vain alle 18-vuotiaat ja kyselyvuonna 18 täyttävät oppijat, joten päivämäärät lasketaan kuluvasta vuodesta.
object AiemmatOpiskeluoikeudetDemoFixtures {
  private val vuosi = LocalDate.now().getYear
  // Kyselyvuonna 18 täyttävät: perusopetus 7–16-vuotiaana, toinen aste kaksi vuotta sitten alkaen
  private val perusopetuksenAlku = date(vuosi - 11, 8, 15)
  private val perusopetuksenLoppu = date(vuosi - 2, 6, 1)
  private val toisenAsteenAlku = date(vuosi - 2, 8, 1)
  private val stadinOpintojenAlku = date(vuosi - 1, 8, 1)
  // 14-vuotias peruskoululainen: esiopetus ja luokat 1–6 Torniossa, 7. luokka Kulosaaressa viime syksystä
  private val esiopetuksenAlku = date(vuosi - 8, 8, 13)
  private val esiopetuksenLoppu = date(vuosi - 7, 5, 29)
  private val alakoulunAlku = date(vuosi - 7, 8, 13)
  private val alakoulunLoppu = date(vuosi - 1, 6, 1)
  private val kulosaarenAlku = date(vuosi - 1, 8, 15)

  private val tornio = Koodistokoodiviite("851", "kunta")
  private val raumonKoulu = Oppilaitos(MockOrganisaatiot.raumonKoulu, None, None)
  private val arpelanPäiväkoti = Oppilaitos(MockOrganisaatiot.arpelanPäiväkoti, None, None)
  private val tornionYhteislyseonLukio = Oppilaitos(MockOrganisaatiot.tornionYhteislyseonLukio, None, None)
  private val kulosaarenAlaAste = Oppilaitos(MockOrganisaatiot.kulosaarenAlaAste, None, None)

  def opiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = List(
    (KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus, esiopetusArkaluontoisillaLisätiedoilla),
    (KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus, alakouluArkaluontoisillaLisätiedoilla),
    (KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus, kulosaarenYläkoulu),

    (KoskiSpecificMockOppijat.aiemmatLukio, perusopetus()),
    (KoskiSpecificMockOppijat.aiemmatLukio, lukio(alku = toisenAsteenAlku, eronnut = Some(date(vuosi - 1, 12, 20)))),
    (KoskiSpecificMockOppijat.aiemmatLukio, stadinAmmatillinen(alku = date(vuosi, 1, 8))),

    (KoskiSpecificMockOppijat.aiemmatAmmatillinen, perusopetus()),
    (KoskiSpecificMockOppijat.aiemmatAmmatillinen, AmmatillinenOpiskeluoikeusTestData.katsotaanEronneeksiOpiskeluoikeus(MockOrganisaatiot.omnia, alkamispäivä = toisenAsteenAlku, päättymispäivä = date(vuosi - 1, 5, 1))),
    (KoskiSpecificMockOppijat.aiemmatAmmatillinen, stadinAmmatillinen()),

    (KoskiSpecificMockOppijat.aiemmatOmaOppilaitos, perusopetus()),
    (KoskiSpecificMockOppijat.aiemmatOmaOppilaitos, stadinPäättynytAmmatillinen),
    (KoskiSpecificMockOppijat.aiemmatOmaOppilaitos, stadinAmmatillinen()),

    (KoskiSpecificMockOppijat.aiemmatMyöhemminAlkanut, perusopetus()),
    (KoskiSpecificMockOppijat.aiemmatMyöhemminAlkanut, stadinAmmatillinen(alku = toisenAsteenAlku)),
    (KoskiSpecificMockOppijat.aiemmatMyöhemminAlkanut, lukio(alku = stadinOpintojenAlku, eronnut = None)),

    (KoskiSpecificMockOppijat.aiemmatEiAiempia, stadinAmmatillinen(alku = toisenAsteenAlku)),
  )

  private def stadinAmmatillinen(alku: LocalDate = stadinOpintojenAlku): AmmatillinenOpiskeluoikeus =
    AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, alkamispäivä = alku)

  // Eri koulutus kuin nykyisessä opiskeluoikeudessa, jotta duplikaattivalidaatio hyväksyy saman oppilaitoksen toisen rivin
  private def stadinPäättynytAmmatillinen: AmmatillinenOpiskeluoikeus = {
    val oo = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 351741, diaariNumero = "OPH-2455-2017", alkamispäivä = toisenAsteenAlku)
    oo.copy(tila = AmmatillinenOpiskeluoikeudenTila(oo.tila.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(date(vuosi - 1, 5, 31), opiskeluoikeusEronnut)))
  }

  private def esiopetusArkaluontoisillaLisätiedoilla: EsiopetuksenOpiskeluoikeus = EsiopetuksenOpiskeluoikeus(
    oppilaitos = Some(arpelanPäiväkoti),
    suoritukset = List(ExamplesEsiopetus.päiväkotisuoritus(arpelanPäiväkoti).copy(vahvistus = vahvistusPaikkakunnalla(esiopetuksenLoppu, arpelanPäiväkoti, tornio))),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
      NuortenPerusopetuksenOpiskeluoikeusjakso(esiopetuksenAlku, opiskeluoikeusLäsnä),
      NuortenPerusopetuksenOpiskeluoikeusjakso(esiopetuksenLoppu, opiskeluoikeusValmistunut),
    )),
    lisätiedot = Some(EsiopetuksenOpiskeluoikeudenLisätiedot(
      pidennettyOppivelvollisuus = Some(Aikajakso(esiopetuksenAlku, Some(esiopetuksenLoppu))),
      vammainen = Some(List(Aikajakso(esiopetuksenAlku, Some(esiopetuksenAlku.withMonth(12).withDayOfMonth(31))))),
      vaikeastiVammainen = Some(List(Aikajakso(esiopetuksenLoppu.withDayOfYear(1), Some(esiopetuksenLoppu)))),
      kuljetusetu = Some(Aikajakso(esiopetuksenAlku, Some(esiopetuksenLoppu))),
      sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(esiopetuksenAlku.withMonth(9).withDayOfMonth(1), Some(esiopetuksenLoppu)))),
      erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
        alku = Some(esiopetuksenAlku),
        loppu = Some(esiopetuksenLoppu),
        erityisryhmässä = Some(false),
        tukimuodot = None,
      ))),
    )),
  )

  // Luokat 1–6 Torniossa, 6. luokka vahvistettu, eronnut koulun vaihdon vuoksi
  private def alakouluArkaluontoisillaLisätiedoilla: PerusopetuksenOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(raumonKoulu),
    suoritukset = List(PerusopetusExampleData.seitsemännenLuokanSuoritus.copy(
      koulutusmoduuli = PerusopetuksenLuokkaAste(6, PerusopetusExampleData.perusopetuksenDiaarinumero),
      luokka = "6A",
      toimipiste = raumonKoulu,
      alkamispäivä = Some(alakoulunLoppu.minusYears(1).withMonth(8).withDayOfMonth(15)),
      vahvistus = vahvistusPaikkakunnalla(alakoulunLoppu.minusDays(2), raumonKoulu, tornio),
    )),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
      NuortenPerusopetuksenOpiskeluoikeusjakso(alakoulunAlku, opiskeluoikeusLäsnä),
      NuortenPerusopetuksenOpiskeluoikeusjakso(alakoulunLoppu, opiskeluoikeusEronnut),
    )),
    lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
      kuljetusetu = Some(Aikajakso(alakoulunAlku, Some(alakoulunAlku.plusYears(3).withMonth(6).withDayOfMonth(2)))),
      sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(alakoulunLoppu.minusYears(3).withMonth(8).withDayOfMonth(16), Some(alakoulunLoppu)))),
      koulukoti = Some(List(Aikajakso(alakoulunLoppu.minusYears(2).withMonth(1).withDayOfMonth(10), Some(alakoulunLoppu.minusYears(2).withMonth(12).withDayOfMonth(16))))),
    )),
  )

  private def kulosaarenYläkoulu: PerusopetuksenOpiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(
    oppilaitos = kulosaarenAlaAste,
    suoritukset = List(PerusopetusExampleData.seitsemännenLuokanSuoritus.copy(toimipiste = kulosaarenAlaAste, luokka = "7A", alkamispäivä = Some(kulosaarenAlku), vahvistus = None)),
    alkamispäivä = kulosaarenAlku,
    päättymispäivä = None,
  )

  private def perusopetus(): PerusopetuksenOpiskeluoikeus =
    PerusopetusExampleData.opiskeluoikeus(
      oppilaitos = raumonKoulu,
      suoritukset = List(
        PerusopetusExampleData.yhdeksännenLuokanSuoritus.copy(
          toimipiste = raumonKoulu,
          alkamispäivä = Some(perusopetuksenLoppu.minusYears(1).withMonth(8).withDayOfMonth(15)),
          vahvistus = vahvistusPaikkakunnalla(perusopetuksenLoppu, raumonKoulu, tornio),
        ),
        PerusopetusExampleData.perusopetuksenOppimääränSuoritus.copy(
          toimipiste = raumonKoulu,
          vahvistus = vahvistusPaikkakunnalla(perusopetuksenLoppu, raumonKoulu, tornio),
        ),
      ),
      alkamispäivä = perusopetuksenAlku,
      päättymispäivä = Some(perusopetuksenLoppu),
    )

  private def lukio(alku: LocalDate, eronnut: Option[LocalDate]): LukionOpiskeluoikeus = LukionOpiskeluoikeus(
    oppilaitos = Some(tornionYhteislyseonLukio),
    tila = LukionOpiskeluoikeudenTila(
      LukionOpiskeluoikeusjakso(alku, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)) ::
        eronnut.map(LukionOpiskeluoikeusjakso(_, opiskeluoikeusEronnut)).toList
    ),
    lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(alku, eronnut))))),
    suoritukset = List(LukionOppimääränSuoritus2019(
      koulutusmoduuli = lukionOppimäärä2019,
      oppimäärä = nuortenOpetussuunnitelma,
      suorituskieli = suomenKieli,
      toimipiste = tornionYhteislyseonLukio,
      osasuoritukset = Some(List(
        oppiaineenSuoritus(lukionÄidinkieli("AI1", pakollinen = true)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(äidinkielenModuuliOppiaineissa("ÄI1")).copy(arviointi = numeerinenArviointi(8, alku.plusMonths(4)))
        ))),
      )),
    )),
  )
}
