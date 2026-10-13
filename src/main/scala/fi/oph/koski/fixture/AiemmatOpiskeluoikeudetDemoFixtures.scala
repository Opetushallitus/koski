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

// Demoaineisto aiemmatOpiskeluoikeudet-massaluovutuskyselylle. Kaikki ovat Stadin ammattiopiston nykyisiä opiskelijoita,
// joiden aiemmat opinnot ovat Torniossa (ei muiden testien laskemissa organisaatioissa) tai Omniassa.
object AiemmatOpiskeluoikeudetDemoFixtures {
  private val stadinOpintojenAlku = date(2022, 8, 1)
  private val tornio = Koodistokoodiviite("851", "kunta")
  private val raumonKoulu = Oppilaitos(MockOrganisaatiot.raumonKoulu, None, None)
  private val arpelanPäiväkoti = Oppilaitos(MockOrganisaatiot.arpelanPäiväkoti, None, None)
  private val tornionYhteislyseonLukio = Oppilaitos(MockOrganisaatiot.tornionYhteislyseonLukio, None, None)

  def opiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = List(
    // Esiopetuksen peruste 102/011/2014 on voimassa vasta 31.12.2014 alkaen, siksi tämä oppija on muita nuorempi
    (KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus, esiopetusArkaluontoisillaLisätiedoilla),
    (KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus, perusopetus(date(2015, 8, 13), date(2024, 6, 1), Some(perusopetuksenArkaluontoisetLisätiedot))),
    (KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus, stadinAmmatillinen(alku = date(2024, 8, 1))),

    (KoskiSpecificMockOppijat.aiemmatLukio, perusopetus(date(2010, 8, 16), date(2019, 6, 1))),
    (KoskiSpecificMockOppijat.aiemmatLukio, lukio(alku = date(2019, 8, 1), eronnut = Some(date(2021, 12, 20)))),
    (KoskiSpecificMockOppijat.aiemmatLukio, stadinAmmatillinen()),

    (KoskiSpecificMockOppijat.aiemmatAmmatillinen, perusopetus(date(2009, 8, 15), date(2018, 6, 2))),
    (KoskiSpecificMockOppijat.aiemmatAmmatillinen, AmmatillinenOpiskeluoikeusTestData.katsotaanEronneeksiOpiskeluoikeus(MockOrganisaatiot.omnia, alkamispäivä = date(2018, 8, 1), päättymispäivä = date(2019, 5, 1))),
    (KoskiSpecificMockOppijat.aiemmatAmmatillinen, stadinAmmatillinen()),

    (KoskiSpecificMockOppijat.aiemmatOmaOppilaitos, perusopetus(date(2009, 8, 15), date(2018, 6, 2))),
    (KoskiSpecificMockOppijat.aiemmatOmaOppilaitos, stadinPäättynytAmmatillinen),
    (KoskiSpecificMockOppijat.aiemmatOmaOppilaitos, stadinAmmatillinen()),

    (KoskiSpecificMockOppijat.aiemmatMyöhemminAlkanut, perusopetus(date(2013, 8, 15), date(2022, 6, 4))),
    (KoskiSpecificMockOppijat.aiemmatMyöhemminAlkanut, stadinAmmatillinen()),
    (KoskiSpecificMockOppijat.aiemmatMyöhemminAlkanut, lukio(alku = date(2023, 8, 1), eronnut = None)),

    (KoskiSpecificMockOppijat.aiemmatEiAiempia, stadinAmmatillinen()),
  )

  private def stadinAmmatillinen(alku: LocalDate = stadinOpintojenAlku): AmmatillinenOpiskeluoikeus =
    AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, alkamispäivä = alku)

  // Eri koulutus kuin nykyisessä opiskeluoikeudessa, jotta duplikaattivalidaatio hyväksyy saman oppilaitoksen toisen rivin
  private def stadinPäättynytAmmatillinen: AmmatillinenOpiskeluoikeus = {
    val alku = date(2018, 8, 1)
    val oo = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 351741, diaariNumero = "OPH-2455-2017", alkamispäivä = alku)
    oo.copy(tila = AmmatillinenOpiskeluoikeudenTila(oo.tila.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(date(2020, 5, 31), opiskeluoikeusEronnut)))
  }

  private def esiopetusArkaluontoisillaLisätiedoilla: EsiopetuksenOpiskeluoikeus = EsiopetuksenOpiskeluoikeus(
    oppilaitos = Some(arpelanPäiväkoti),
    suoritukset = List(ExamplesEsiopetus.päiväkotisuoritus(arpelanPäiväkoti).copy(vahvistus = vahvistusPaikkakunnalla(date(2015, 5, 29), arpelanPäiväkoti, tornio))),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
      NuortenPerusopetuksenOpiskeluoikeusjakso(date(2014, 8, 13), opiskeluoikeusLäsnä),
      NuortenPerusopetuksenOpiskeluoikeusjakso(date(2015, 5, 29), opiskeluoikeusValmistunut),
    )),
    lisätiedot = Some(EsiopetuksenOpiskeluoikeudenLisätiedot(
      pidennettyOppivelvollisuus = Some(Aikajakso(date(2014, 8, 13), Some(date(2015, 5, 29)))),
      vammainen = Some(List(Aikajakso(date(2014, 8, 13), Some(date(2014, 12, 31))))),
      vaikeastiVammainen = Some(List(Aikajakso(date(2015, 1, 1), Some(date(2015, 5, 29))))),
      kuljetusetu = Some(Aikajakso(date(2014, 8, 13), Some(date(2015, 5, 29)))),
      sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2014, 9, 1), Some(date(2015, 5, 29))))),
      erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
        alku = Some(date(2014, 8, 13)),
        loppu = Some(date(2015, 5, 29)),
        erityisryhmässä = Some(false),
        tukimuodot = None,
      ))),
    )),
  )

  private def perusopetuksenArkaluontoisetLisätiedot: PerusopetuksenOpiskeluoikeudenLisätiedot = PerusopetuksenOpiskeluoikeudenLisätiedot(
    kuljetusetu = Some(Aikajakso(date(2015, 8, 13), Some(date(2018, 6, 2)))),
    sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2021, 8, 16), Some(date(2024, 6, 1))))),
    koulukoti = Some(List(Aikajakso(date(2022, 1, 10), Some(date(2022, 12, 16))))),
  )

  private def perusopetus(alku: LocalDate, loppu: LocalDate, lisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = None): PerusopetuksenOpiskeluoikeus =
    PerusopetusExampleData.opiskeluoikeus(
      oppilaitos = raumonKoulu,
      suoritukset = List(
        PerusopetusExampleData.yhdeksännenLuokanSuoritus.copy(
          toimipiste = raumonKoulu,
          alkamispäivä = Some(loppu.minusYears(1).withMonth(8).withDayOfMonth(15)),
          vahvistus = vahvistusPaikkakunnalla(loppu, raumonKoulu, tornio),
        ),
        PerusopetusExampleData.perusopetuksenOppimääränSuoritus.copy(
          toimipiste = raumonKoulu,
          vahvistus = vahvistusPaikkakunnalla(loppu, raumonKoulu, tornio),
        ),
      ),
      alkamispäivä = alku,
      päättymispäivä = Some(loppu),
    ).copy(lisätiedot = lisätiedot)

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
