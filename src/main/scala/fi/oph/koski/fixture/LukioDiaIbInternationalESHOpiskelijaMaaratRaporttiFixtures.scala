package fi.oph.koski.fixture

import java.time.LocalDate
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.documentation._
import fi.oph.koski.schema._

object LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures {

  val date = LocalDate.now().withYear(2021)

  val lukionOppimaaraNuorten = LukionOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date.minusYears(4), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      ),
      oppilaitos = Some(helsinginMedialukio),
      lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(
        sisäoppilaitosmainenMajoitus = Some(List(aikajakso))
      )),
      suoritukset = List(
        LukionOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppimäärä,
          oppimäärä = nuortenOpetussuunnitelma,
          suorituskieli = suomenKieli,
          toimipiste = helsinginMedialukio,
          osasuoritukset = None
        )
      )
    )

  val lukionOppimaaraAikuisten = LukionOpiskeluoikeus(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = date.minusYears(4), tila = opiskeluoikeusValiaikaisestiKeskeytynyt, opintojenRahoitus = Some(ExampleData.muutaKauttaRahoitettu)),
        LukionOpiskeluoikeusjakso(alku = date, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.muutaKauttaRahoitettu))
      )
    ),
    oppilaitos = Some(helsinginMedialukio),
    lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(
      ulkomainenVaihtoopiskelija = true
    )),
    suoritukset = List(
      LukionOppimääränSuoritus2015(
        koulutusmoduuli = lukionOppimäärä,
        oppimäärä = aikuistenOpetussuunnitelma,
        suorituskieli = ruotsinKieli,
        toimipiste = helsinginMedialukio,
        ryhmä = Some("12A"),
        osasuoritukset = None
      )
    )
  )

  val lukionAineopiskelija = LukionOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date.minusYears(1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      ),
      lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(
        erityisenKoulutustehtävänJaksot = Some(List(erityisenKoulutustehtävänJakso1))
      )),
      versionumero = None,
      lähdejärjestelmänId = None,
      oppilaitos = Some(ressunLukio),
      suoritukset = List(
        LukionOppiaineenOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppiaine("HI", diaarinumero = Some("60/011/2015")),
          suorituskieli = sloveeni,
          toimipiste = ressunLukio,
          osasuoritukset = None
        ),
        LukionOppiaineenOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppiaine("GE", diaarinumero = Some("60/011/2015")),
          suorituskieli = sloveeni,
          toimipiste = ressunLukio,
          osasuoritukset = None
        ),
        LukionOppiaineenOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppiaine("FY", diaarinumero = Some("60/011/2015")),
          suorituskieli = sloveeni,
          toimipiste = ressunLukio,
          osasuoritukset = None
        )
      )
    )

  val international = InternationalSchoolOpiskeluoikeus(
    oppilaitos = Some(ressunLukio),
    lisätiedot = Some(InternationalSchoolOpiskeluoikeudenLisätiedot(
      erityisenKoulutustehtävänJaksot = Some(List(erityisenKoulutustehtävänJakso2))
    )),
    tila = InternationalSchoolOpiskeluoikeudenTila(
        List(
          InternationalSchoolOpiskeluoikeusjakso(date, opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
    ),
    suoritukset = List(
      ExamplesInternationalSchool.grade9.copy(alkamispäivä = Some(date), vahvistus = None),
      ExamplesInternationalSchool.grade10.copy(alkamispäivä = Some(date), vahvistus = None),
      ExamplesInternationalSchool.grade11.copy(alkamispäivä = Some(date), vahvistus = None),
      ExamplesInternationalSchool.grade12.copy(alkamispäivä = Some(date), vahvistus = None)
    )
  )

  val dia = DIAOpiskeluoikeus(
    oppilaitos = Some(ressunLukio),
    lisätiedot = Some(DIAOpiskeluoikeudenLisätiedot(
    )),
    tila = DIAOpiskeluoikeudenTila(List(
      DIAOpiskeluoikeusjakso(date, opiskeluoikeusAktiivinen, Some(ExampleData.muutaKauttaRahoitettu))
    )),
    suoritukset = List(
      ExamplesDIA.diaValmistavanVaiheenSuoritus.copy(vahvistus = None, toimipiste = ressunLukio),
      ExamplesDIA.diaTutkintovaiheenSuoritus().copy(vahvistus = None, toimipiste = ressunLukio)
    )
  )

  val esh = EuropeanSchoolOfHelsinkiOpiskeluoikeus(
    oppilaitos = Some(ressunLukio),
    tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
      List(
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(date, opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(
      ExamplesEuropeanSchoolOfHelsinki.s5.copy(alkamispäivä = Some(date), vahvistus = None),
      ExamplesEuropeanSchoolOfHelsinki.s6.copy(alkamispäivä = Some(date), vahvistus = None),
      ExamplesEuropeanSchoolOfHelsinki.s7.copy(alkamispäivä = Some(date), vahvistus = None),
    )
  )

  val ib = IBOpiskeluoikeus(
    oppilaitos = Some(ressunLukio),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = date, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.muutaKauttaRahoitettu)),
        LukionOpiskeluoikeusjakso(alku = date.plusYears(1), tila = opiskeluoikeusPäättynyt, opintojenRahoitus = Some(ExampleData.muutaKauttaRahoitettu))
      )
    ),
    suoritukset = List(ExamplesIB.preIBSuoritus)
  )

  val fixedDate = LocalDate.of(2000, 1, 1)

  val nuortenOppimaaraLuva = LukioonValmistavanKoulutuksenOpiskeluoikeus(
    oppilaitos = Some(ressunLukio),
    koulutustoimija = None,
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = fixedDate, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
      )
    ),
    lisätiedot = Some(LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(
      sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(fixedDate, Some(fixedDate.plusDays(2)))))
    )),
    suoritukset = List(
      ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenSuoritus.copy(
        vahvistus = None
      )
    )
  )

  val aikuistenOppimaaraLuva = LukioonValmistavanKoulutuksenOpiskeluoikeus(
    oppilaitos = Some(ressunLukio),
    koulutustoimija = None,
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = fixedDate, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.muutaKauttaRahoitettu))
      )
    ),
    suoritukset = List(
      ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenSuoritus.copy(
        oppimäärä = aikuistenOpetussuunnitelma,
        vahvistus = None
      )
    )
  )

  lazy val erityisenKoulutustehtävänJakso1 = ErityisenKoulutustehtävänJakso(date, Some(date.plusMonths(1)), Koodistokoodiviite("101", "erityinenkoulutustehtava"))
  lazy val erityisenKoulutustehtävänJakso2 = ErityisenKoulutustehtävänJakso(date, Some(date.plusMonths(2)), Koodistokoodiviite("102", "erityinenkoulutustehtava"))
  lazy val aikajakso = Aikajakso(date, Some(date.plusMonths(1)))
}
