package fi.oph.koski.fixture

import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.documentation.{ExampleData, ExamplesDIA, ExamplesIB, ExamplesInternationalSchool}
import fi.oph.koski.schema._

object LukioKurssikertymaRaporttiFixtures {

  lazy val date = LocalDate.of(2016, 8, 6)

  lazy val raportinAikajaksoAlku = date.minusMonths(13)
  lazy val raportinAikajaksoLoppu = date.plusMonths(2)

  lazy val oppimaara = LukionOpiskeluoikeus(
    tila = LukionOpiskeluoikeudenTila(List(
      LukionOpiskeluoikeusjakso(alku = date.minusDays(10), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
      LukionOpiskeluoikeusjakso(alku = date.plusDays(10), tila = opiskeluoikeusEronnut, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
    )),
    oppilaitos = Some(helsinginMedialukio),
    suoritukset = List(
      LukionOppimääränSuoritus2015(
        koulutusmoduuli = lukionOppimäärä,
        oppimäärä = nuortenOpetussuunnitelma,
        suorituskieli = suomenKieli,
        toimipiste = helsinginMedialukio,
        osasuoritukset = Some(List(
          matematiikanOppiaine
        ))
      )
    )
  )

  lazy val dateMuutaKauttaRahoitettu = date.plusDays(10)
  lazy val dateEronnut = date.plusDays(20)
  lazy val aineopiskelijaEronnut = LukionOpiskeluoikeus(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = date, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(alku = dateMuutaKauttaRahoitettu, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.muutaKauttaRahoitettu)),
        LukionOpiskeluoikeusjakso(alku = dateEronnut, tila = opiskeluoikeusEronnut, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
      )
    ),
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
        osasuoritukset = Some(mantsanKurssit)
      ),
      LukionOppiaineenOppimääränSuoritus2015(
        koulutusmoduuli = matematiikka("MAA", perusteenDiaarinumero = Some("60/011/2015")),
        suorituskieli = sloveeni,
        toimipiste = ressunLukio,
        osasuoritukset = Some(matematiikanKurssit)
      ),
      LukionOppiaineenOppimääränSuoritus2015(
        koulutusmoduuli = lukionOppiaine("FI", diaarinumero = Some("60/011/2015")),
        suorituskieli = sloveeni,
        toimipiste = ressunLukio,
        osasuoritukset = Some(filosofianKurssit)
      ),
    )
  )

  lazy val dateValmistunut = dateMuutaKauttaRahoitettu
  lazy val aineopiskelijaValmistunut = LukionOpiskeluoikeus(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = date, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(alku = dateValmistunut, tila = opiskeluoikeusPäättynyt, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
      )
    ),
    versionumero = None,
    lähdejärjestelmänId = None,
    oppilaitos = Some(ressunLukio),
    suoritukset = List(
      LukionOppiaineenOppimääränSuoritus2015(
        koulutusmoduuli = lukionOppiaine("FI", diaarinumero = Some("60/011/2015")),
        suorituskieli = sloveeni,
        toimipiste = ressunLukio,
        vahvistus = vahvistusPaikkakunnalla(päivä = dateValmistunut),
        arviointi = arviointi("8"),
        osasuoritukset = Some(filosofianKurssit),
      ),
    )
  )

  lazy val matematiikanOppiaine = suoritus(matematiikka("MAA", None)).copy(osasuoritukset = Some(matematiikanKurssit))

  lazy val matematiikanKurssit = List(
    kurssi(syventäväKurssi("MAA1", "MAA1", "MAA1")).copy(arviointi = kurssinArviointiKorotettuMyöhemmin),
    kurssi(syventäväKurssi("MAA14", "MAA14", "MAA14")).copy(tunnustettu = Some(tunnustettuRahoituksenPiirissa)),
    kurssi(syventäväKurssi("MAA16", "MAA16", "MAA16")).copy(tunnustettu = Some(tunnustettu)),
    kurssi(valtakunnallinenKurssi("MAA2")).copy(tunnustettu = Some(tunnustettuRahoituksenPiirissa)),
    kurssi(valtakunnallinenKurssi("MAA3")).copy(tunnustettu = Some(tunnustettu)),
    kurssi(valtakunnallinenKurssi("MAA4")).copy(arviointi = kurssinArviointiOsallistunut),
    kurssi(valtakunnallinenKurssi("MAA5")),
    kurssi(valtakunnallinenKurssi("MAA6")).copy(arviointi = kurssinArviointiJaksoaEnnen),
    kurssi(valtakunnallinenKurssi("MAA7")).copy(arviointi = kurssinArviointiJaksonJalkeen),
    kurssi(valtakunnallinenKurssi("MAA8")).copy(arviointi = kurssinArviointiJaksonJalkeen),
  )

  lazy val filosofianKurssit = List(
    kurssi(valtakunnallinenKurssi("FI1")).copy(arviointi = kurssinArviointiMuutaKauttaRahoitetullaJaksolla),
    kurssi(valtakunnallinenKurssi("FI2")).copy(arviointi = kurssinArviointiOpintooikeudenJalkeen),
  )

  lazy val kurssinArviointi = Some(List(NumeerinenLukionArviointi(Koodistokoodiviite(koodiarvo = "8", koodistoUri = "arviointiasteikkoyleissivistava"), date)))
  lazy val kurssinArviointiKorotettuMyöhemmin= Some(List(
    NumeerinenLukionArviointi(Koodistokoodiviite(koodiarvo = "8", koodistoUri = "arviointiasteikkoyleissivistava"), date.minusYears(1)),
    NumeerinenLukionArviointi(Koodistokoodiviite(koodiarvo = "9", koodistoUri = "arviointiasteikkoyleissivistava"), date)
  ))
  lazy val kurssinArviointiOsallistunut = Some(List(SanallinenLukionArviointi(Koodistokoodiviite(koodiarvo = "O", koodistoUri = "arviointiasteikkoyleissivistava"), None, date)))
  lazy val kurssinArviointiJaksonJalkeen = Some(List(NumeerinenLukionArviointi(Koodistokoodiviite(koodiarvo = "8", koodistoUri = "arviointiasteikkoyleissivistava"), raportinAikajaksoLoppu.plusDays(1))))
  lazy val kurssinArviointiJaksoaEnnen = Some(List(NumeerinenLukionArviointi(Koodistokoodiviite(koodiarvo = "8", koodistoUri = "arviointiasteikkoyleissivistava"), raportinAikajaksoAlku.minusDays(1))))
  lazy val kurssinArviointiMuutaKauttaRahoitetullaJaksolla = Some(List(NumeerinenLukionArviointi(Koodistokoodiviite(koodiarvo = "8", koodistoUri = "arviointiasteikkoyleissivistava"), dateMuutaKauttaRahoitettu)))
  lazy val kurssinArviointiOpintooikeudenJalkeen = Some(List(NumeerinenLukionArviointi(Koodistokoodiviite(koodiarvo = "7", koodistoUri = "arviointiasteikkoyleissivistava"), dateEronnut.plusDays(1))))

  lazy val mantsanKurssit = List(
    kurssi(valtakunnallinenKurssi("GE1").copy(kurssinTyyppi = syventävä)),
    kurssi(valtakunnallinenKurssi("GE2").copy(kurssinTyyppi = syventävä)).copy(tunnustettu = Some(tunnustettu)),
    kurssi(valtakunnallinenKurssi("GE3").copy(kurssinTyyppi = syventävä)).copy(tunnustettu = Some(tunnustettuRahoituksenPiirissa))
  )

  lazy val tunnustettuRahoituksenPiirissa = OsaamisenTunnustaminen(osaaminen = None, selite = LocalizedString.finnish("tunnustettu"), rahoituksenPiirissä = true)
  lazy val tunnustettu = OsaamisenTunnustaminen(osaaminen = None, selite = LocalizedString.finnish("tunnustettu"), rahoituksenPiirissä = false)

  private def kurssi(k: LukionKurssi2015) = kurssisuoritus(k).copy(arviointi = kurssinArviointi)
}
