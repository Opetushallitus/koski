package fi.oph.koski.raportit

import fi.oph.koski.documentation.ExampleData.suomenKieli
import fi.oph.koski.documentation.ExamplesLukio2019.{lops2019perusteenDiaarinumero, lukionOppimäärä2019}
import fi.oph.koski.documentation.Lukio2019ExampleData
import fi.oph.koski.documentation.Lukio2019ExampleData.{moduulinSuoritusOppiaineissa, muuModuuliOppiaineissa, numeerinenArviointi, numeerinenLukionOppiaineenArviointi, oppiaineenSuoritus, paikallinenOpintojakso, paikallisenOpintojaksonSuoritus}
import fi.oph.koski.documentation.LukioExampleData.nuortenOpetussuunnitelma
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.schema.{LocalizedString, LukionOppiaineenSuoritus2019, LukionOppiaineidenOppimäärienSuoritus2019, LukionOppiaineidenOppimäärät2019, LukionOppimääränSuoritus2019, OsaamisenTunnustaminen, PaikallinenKoodi, PaikallinenLukionOppiaine2019}
import java.time.LocalDate.{of => date}

object Lukio2019RaaportitTestData {
  lazy val tunnustus = Some(OsaamisenTunnustaminen(
    osaaminen = None,
    selite = LocalizedString.finnish("osaamisen tunnustaminen")
  ))

  lazy val korotettuArviointi = numeerinenArviointi(8, date(2000, 1, 1)).toList.flatten ::: numeerinenArviointi(9, date(2001, 1, 1)).toList.flatten

  lazy val oppiaineSuoritukset: List[LukionOppiaineenSuoritus2019] = List(
    oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1").copy(laajuus = Lukio2019ExampleData.laajuus(2))).copy(arviointi = numeerinenArviointi(8, date(2000, 1, 1))),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2").
        copy(laajuus = Lukio2019ExampleData.laajuus(2))).copy(arviointi = numeerinenArviointi(8, date(2000, 1, 1)), tunnustettu = tunnustus),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3").
        copy(laajuus = Lukio2019ExampleData.laajuus(2))).copy(arviointi = Some(korotettuArviointi), tunnustettu = tunnustus),
    ))),
    oppiaineenSuoritus(PaikallinenLukionOppiaine2019(PaikallinenKoodi("ITT", LocalizedString.finnish("Tanssi ja liike")), LocalizedString.finnish("Tanssi ja liike"), pakollinen = false)).copy(arviointi = numeerinenLukionOppiaineenArviointi(8)).copy(osasuoritukset = Some(List(
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT234", "Tanssin taito", "Perinteiset suomalaiset tanssit, valssi jne").copy(laajuus = Lukio2019ExampleData.laajuus(2), pakollinen = false)).copy(arviointi = numeerinenArviointi(10, date(2000, 1, 1))),
    )))
  )

  lazy val oppiaineidenOppimäärienSuoritus = LukionOppiaineidenOppimäärienSuoritus2019(
    koulutusmoduuli = LukionOppiaineidenOppimäärät2019(perusteenDiaarinumero = lops2019perusteenDiaarinumero),
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    toimipiste = jyväskylänNormaalikoulu,
    osasuoritukset = Some(oppiaineSuoritukset)
  )

  lazy val oppimääränSuoritus = LukionOppimääränSuoritus2019(
    koulutusmoduuli = lukionOppimäärä2019,
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    vahvistus = None,
    toimipiste = jyväskylänNormaalikoulu,
    osasuoritukset = Some(oppiaineSuoritukset),
  )
}
