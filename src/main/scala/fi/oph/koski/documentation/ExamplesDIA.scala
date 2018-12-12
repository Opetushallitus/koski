package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData.{englanti, helsinki}
import fi.oph.koski.documentation.DIAExampleData.saksalainenKoulu
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

object ExamplesDIA {
  def osasuorituksetValmistavaVaihe: List[DIAOppiaineenValmistavanVaiheenSuoritus] = List(
    diaValmistavaVaiheAineSuoritus(diaÄidinkieli("DE", laajuus = 3), List(
      (diaValmistavaLukukausi("1"), "3"),
      (diaValmistavaLukukausi("2"), "5")
    )),
    diaValmistavaVaiheAineSuoritus(diaÄidinkieli("FI", laajuus = 3), List(
      (diaValmistavaLukukausi("1"), "2"),
      (diaValmistavaLukukausi("2"), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaKieliaine("A", "EN", laajuus = 3), List(
      (diaValmistavaLukukausi("1"), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaKieliaine("B1", "SV", laajuus = 3), List(
      (diaValmistavaLukukausi("1"), "2"),
      (diaValmistavaLukukausi("2"), "2")
    )),
    diaValmistavaVaiheAineSuoritus(diaKieliaine("B2", "LA", laajuus = 2), List(
      (diaValmistavaLukukausi("1"), "4"),
      (diaValmistavaLukukausi("2"), "4")
    )),
    diaValmistavaVaiheAineSuoritus(diaKieliaine("B3", "RU", laajuus = 3), List(
      (diaValmistavaLukukausi("1"), "4"),
      (diaValmistavaLukukausi("2"), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaine("KU", "1", laajuus = 2), List(
      (diaValmistavaLukukausi("1"), "4"),
      (diaValmistavaLukukausi("2"), "5")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaine("MA", "2", laajuus = 4), List(
      (diaValmistavaLukukausi("1"), "3"),
      (diaValmistavaLukukausi("2"), "1")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaine("FY", "2", laajuus = 2), List(
      (diaValmistavaLukukausi("1"), "3"),
      (diaValmistavaLukukausi("2"), "2")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaine("KE", "2", laajuus = 2), List(
      (diaValmistavaLukukausi("1"), "2"),
      (diaValmistavaLukukausi("2"), "4")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaine("TI", "2", laajuus = 2), List(
      (diaValmistavaLukukausi("1"), "1"),
      (diaValmistavaLukukausi("2"), "2")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaine("HI", "3", laajuus = 2), List(
      (diaValmistavaLukukausi("1"), "3"),
      (diaValmistavaLukukausi("2"), "4")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaine("TA", "3", laajuus = 2), List(
      (diaValmistavaLukukausi("1"), "5"),
      (diaValmistavaLukukausi("2"), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaine("MAA", "3", laajuus = 2), List(
      (diaValmistavaLukukausi("1"), "3"),
      (diaValmistavaLukukausi("2"), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaine("FI", "3", laajuus = 2), List(
      (diaValmistavaLukukausi("1"), "1"),
      (diaValmistavaLukukausi("2"), "1")
    ))
  )

  def osasuorituksetTutkintovaihe: List[DIAOppiaineenTutkintovaiheenSuoritus] = List(
    diaTutkintoAineSuoritus(diaÄidinkieli("DE", laajuus = 10), List(
      (diaTutkintoLukukausi("3"), "3"),
      (diaTutkintoLukukausi("4"), "5"),
      (diaTutkintoLukukausi("5"), "4"),
      (diaTutkintoLukukausi("6"), "3")
    )),
    diaTutkintoAineSuoritus(diaÄidinkieli("FI", laajuus = 8), List(
      (diaTutkintoLukukausi("3"), "2"),
      (diaTutkintoLukukausi("4"), "3"),
      (diaTutkintoLukukausi("5"), "3"),
      (diaTutkintoLukukausi("6"), "3")
    )),
    diaTutkintoAineSuoritus(diaKieliaine("A", "EN", laajuus = 6), List(
      (diaTutkintoLukukausi("3"), "2"),
      (diaTutkintoLukukausi("4"), "3"),
      (diaTutkintoLukukausi("5"), "2")
    )),
    diaTutkintoAineSuoritus(diaKieliaine("B1", "SV", laajuus = 6), List(
      (diaTutkintoLukukausi("3"), "2"),
      (diaTutkintoLukukausi("4"), "2"),
      (diaTutkintoLukukausi("5"), "4"),
      (diaTutkintoLukukausi("6"), "3")
    )),
    diaTutkintoAineSuoritus(diaKieliaine("B2", "LA", laajuus = 4), List(
      (diaTutkintoLukukausi("3"), "3"),
      (diaTutkintoLukukausi("4"), "3"),
      (diaTutkintoLukukausi("5"), "2"),
      (diaTutkintoLukukausi("6"), "2")
    )),
    diaTutkintoAineSuoritus(diaKieliaine("B3", "RU", laajuus = 6), List(
      (diaTutkintoLukukausi("3"), "4"),
      (diaTutkintoLukukausi("4"), "3"),
      (diaTutkintoLukukausi("5"), "4"),
      (diaTutkintoLukukausi("6"), "3")
    )),
    diaTutkintoAineSuoritus(diaOppiaine("KU", "1", laajuus = 4), List(
      (diaTutkintoLukukausi("3"), "4"),
      (diaTutkintoLukukausi("4"), "3"),
      (diaTutkintoLukukausi("5"), "2"),
      (diaTutkintoLukukausi("6"), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaine("MA", "2", laajuus = 8), List(
      (diaTutkintoLukukausi("3"), "3"),
      (diaTutkintoLukukausi("4"), "1"),
      (diaTutkintoLukukausi("5"), "1"),
      (diaTutkintoLukukausi("6"), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaine("FY", "2", laajuus = 6), List(
      (diaTutkintoLukukausi("3"), "2"),
      (diaTutkintoLukukausi("4"), "2"),
      (diaTutkintoLukukausi("5"), "1"),
      (diaTutkintoLukukausi("6"), "1")
    )),
    diaTutkintoAineSuoritus(diaOppiaine("KE", "2", laajuus = 6), List(
      (diaTutkintoLukukausi("3"), "3"),
      (diaTutkintoLukukausi("4"), "2"),
      (diaTutkintoLukukausi("5"), "1"),
      (diaTutkintoLukukausi("6"), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaine("TI", "2", laajuus = 4), List(
      (diaTutkintoLukukausi("3"), "2"),
      (diaTutkintoLukukausi("4"), "1"),
      (diaTutkintoLukukausi("5"), "1"),
      (diaTutkintoLukukausi("6"), "1")
    )),
    diaTutkintoAineSuoritus(diaOppiaine("HI", "3", laajuus = 6), List(
      (diaTutkintoLukukausi("3"), "3"),
      (diaTutkintoLukukausi("4"), "4"),
      (diaTutkintoLukukausi("5"), "3"),
      (diaTutkintoLukukausi("6"), "1")
    ), suorituskieli = Some("FI")),
    diaTutkintoAineSuoritus(diaOppiaine("TA", "3", laajuus = 6), List(
      (diaTutkintoLukukausi("3"), "4"),
      (diaTutkintoLukukausi("4"), "3"),
      (diaTutkintoLukukausi("5"), "2"),
      (diaTutkintoLukukausi("6"), "3")
    )),
    diaTutkintoAineSuoritus(diaOppiaine("MAA", "3", laajuus = 6), List(
      (diaTutkintoLukukausi("3"), "3"),
      (diaTutkintoLukukausi("4"), "5"),
      (diaTutkintoLukukausi("5"), "3"),
      (diaTutkintoLukukausi("6"), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaine("FI", "3", laajuus = 4), List(
      (diaTutkintoLukukausi("3"), "2"),
      (diaTutkintoLukukausi("4"), "2"),
      (diaTutkintoLukukausi("5"), "2"),
      (diaTutkintoLukukausi("6"), "2")
    )),
    diaTutkintoAineSuoritus(diaMuuValinnainen("CCEA", laajuus = 1), List(
      (diaTutkintoLukukausi("5"), "3"),
      (diaTutkintoLukukausi("6"), "1")
    )),
    diaTutkintoAineSuoritus(diaMuuValinnainen("MASY", laajuus = 2), List(
      (diaTutkintoLukukausi("3"), "2"),
      (diaTutkintoLukukausi("4"), "1"),
      (diaTutkintoLukukausi("5"), "2"),
      (diaTutkintoLukukausi("6"), "4")
    ))
  )

  def diaValmistavanVaiheenSuoritus = DIAValmistavanVaiheenSuoritus(
    toimipiste = saksalainenKoulu,
    suorituskieli = englanti,
    vahvistus = ExampleData.vahvistusPaikkakunnalla(org = saksalainenKoulu, kunta = helsinki),
    osasuoritukset = Some(osasuorituksetValmistavaVaihe)
  )

  def diaTutkintovaiheenSuoritus(kokonaispistemäärä: Option[Int] = None) = DIATutkinnonSuoritus(
    toimipiste = saksalainenKoulu,
    suorituskieli = englanti,
    vahvistus = ExampleData.vahvistusPaikkakunnalla(org = saksalainenKoulu, kunta = helsinki),
    kokonaispistemäärä = kokonaispistemäärä,
    osasuoritukset = Some(osasuorituksetTutkintovaihe)
  )

  def diaValmistavaVaiheAineSuoritus(oppiaine: DIAOppiaine, lukukaudet: List[(DIAOppiaineenValmistavanVaiheenLukukausi, String)] = Nil) = DIAOppiaineenValmistavanVaiheenSuoritus(
    koulutusmoduuli = oppiaine,
    osasuoritukset = Some(lukukaudet.map { case (lukukausi, arvosana) =>
      DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus(
        koulutusmoduuli = lukukausi,
        arviointi = diaTutkintovaiheArviointi(arvosana)
      )
    })
  )

  def diaTutkintoAineSuoritus(oppiaine: DIAOppiaine, lukukaudet: List[(DIAOppiaineenTutkintovaiheenLukukausi, String)] = Nil, suorituskieli: Option[String] = None) = DIAOppiaineenTutkintovaiheenSuoritus(
    koulutusmoduuli = oppiaine,
    suorituskieli = suorituskieli.map(k => Koodistokoodiviite(koodiarvo = k, koodistoUri = "kieli")),
    osasuoritukset = Some(lukukaudet.map { case (lukukausi, arvosana) =>
      DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
        koulutusmoduuli = lukukausi,
        arviointi = diaValmistavaVaiheArviointi(arvosana)
      )
    })
  )

  def diaOppiaine(aine: String, osaAlue: String, laajuus: Int) = DIAOppiaineMuu(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = aine),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus)),
    osaAlue = Koodistokoodiviite(koodiarvo = osaAlue, koodistoUri = "diaosaalue")
  )

  def diaKieliaine(taso: String, kieli: String, laajuus: Int) = DIAOppiaineKieli(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = taso),
    kieli = Koodistokoodiviite(koodistoUri = "kielivalikoima", koodiarvo = kieli),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus))
  )

  def diaÄidinkieli(kieli: String, laajuus: Int) = DIAOppiaineÄidinkieli(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = "AI"),
    kieli = Koodistokoodiviite(koodistoUri = "oppiainediaaidinkieli", koodiarvo = kieli),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus))
  )

  def diaMuuValinnainen(aine: String, laajuus: Int) = DIAOppiaineLisäaine(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = aine),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus))
  )

  def diaValmistavaLukukausi(lukukausi: String) = DIAOppiaineenValmistavanVaiheenLukukausi(
    tunniste = Koodistokoodiviite(koodiarvo = lukukausi, koodistoUri = "dialukukausi")
  )

  def diaTutkintoLukukausi(lukukausi: String) = DIAOppiaineenTutkintovaiheenLukukausi(
    tunniste = Koodistokoodiviite(koodiarvo = lukukausi, koodistoUri = "dialukukausi")
  )

  def diaValmistavaVaiheArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[DIAOppiaineenTutkintovaiheenNumeerinenArviointi]] = {
    Some(List(DIAOppiaineenTutkintovaiheenNumeerinenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkodiatutkinto"), päivä = Some(päivä))))
  }

  def diaTutkintovaiheArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[DIAOppiaineenValmistavanVaiheenLukukaudenArviointi]] = {
    Some(List(DIAOppiaineenValmistavanVaiheenLukukaudenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkodiavalmistava"), päivä = Some(päivä))))
  }

  val opiskeluoikeus = DIAOpiskeluoikeus(
    oppilaitos = Some(saksalainenKoulu),
    päättymispäivä = Some(date(2016, 6, 4)),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen),
        LukionOpiskeluoikeusjakso(date(2016, 6, 4), LukioExampleData.opiskeluoikeusPäättynyt)
      )
    ),
    suoritukset = List(diaValmistavanVaiheenSuoritus, diaTutkintovaiheenSuoritus(Some(870)))
  )

  val examples = List(
    Example("dia", "dia", Oppija(asUusiOppija(MockOppijat.dia), List(opiskeluoikeus))),
  )
}
