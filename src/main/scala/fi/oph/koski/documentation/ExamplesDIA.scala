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
    diaValmistavaVaiheAineSuoritus(diaOppiaineÄidinkieli("DE", laajuus = 3), List(
      (diaValmistavaLukukausi("1", 1), "3"),
      (diaValmistavaLukukausi("2", 2), "5")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineÄidinkieli("FI", laajuus = 3), List(
      (diaValmistavaLukukausi("1", 1), "2"),
      (diaValmistavaLukukausi("2", 2), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineKieliaine("A", "EN", laajuus = 3), List(
      (diaValmistavaLukukausi("1", 3), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineKieliaine("B1", "SV", laajuus = 3), List(
      (diaValmistavaLukukausi("1", 1), "2"),
      (diaValmistavaLukukausi("2", 2), "2")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineLisäaineKieli("B2", "LA", laajuus = 2), List(
      (diaValmistavaLukukausi("1", 1), "4"),
      (diaValmistavaLukukausi("2", 1), "4")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineKieliaine("B3", "RU", laajuus = 3), List(
      (diaValmistavaLukukausi("1", 1), "4"),
      (diaValmistavaLukukausi("2", 2), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("KU", "1", laajuus = 2), List(
      (diaValmistavaLukukausi("1", 1), "4"),
      (diaValmistavaLukukausi("2", 1), "5")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("MA", "2", laajuus = 4), List(
      (diaValmistavaLukukausi("1", 2), "3"),
      (diaValmistavaLukukausi("2", 2), "1")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("FY", "2", laajuus = 2), List(
      (diaValmistavaLukukausi("1", 1), "3"),
      (diaValmistavaLukukausi("2", 1), "2")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("KE", "2", laajuus = 2), List(
      (diaValmistavaLukukausi("1", 1), "2"),
      (diaValmistavaLukukausi("2", 1), "4")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("TI", "2", laajuus = 2), List(
      (diaValmistavaLukukausi("1", 1), "1"),
      (diaValmistavaLukukausi("2", 1), "2")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("HI", "3", laajuus = 2), List(
      (diaValmistavaLukukausi("1", 1), "3"),
      (diaValmistavaLukukausi("2", 1), "4")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("TA", "3", laajuus = 2), List(
      (diaValmistavaLukukausi("1", 1), "5"),
      (diaValmistavaLukukausi("2", 1), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("MAA", "3", laajuus = 2), List(
      (diaValmistavaLukukausi("1", 1), "3"),
      (diaValmistavaLukukausi("2", 1), "3")
    )),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("FI", "3", laajuus = 2), List(
      (diaValmistavaLukukausi("1", 1), "1"),
      (diaValmistavaLukukausi("2", 1), "1")
    ))
  )

  def osasuorituksetTutkintovaihe: List[DIAOppiaineenTutkintovaiheenSuoritus] = List(
    diaTutkintoAineSuoritus(diaOppiaineÄidinkieli("DE", laajuus = 10), List(
      (diaTutkintoLukukausi("3", 2), "3"),
      (diaTutkintoLukukausi("4", 2), "5"),
      (diaTutkintoLukukausi("5", 2), "4"),
      (diaTutkintoLukukausi("6", 4), "3")
    )),
    diaTutkintoAineSuoritus(diaOppiaineÄidinkieli("FI", laajuus = 8), List(
      (diaTutkintoLukukausi("3", 2), "2"),
      (diaTutkintoLukukausi("4", 2), "3"),
      (diaTutkintoLukukausi("5", 2), "3"),
      (diaTutkintoLukukausi("6", 2), "3")
    )),
    diaTutkintoAineSuoritus(diaOppiaineKieliaine("A", "EN", laajuus = 6), List(
      (diaTutkintoLukukausi("3", 2), "2"),
      (diaTutkintoLukukausi("4", 2), "3"),
      (diaTutkintoLukukausi("5", 2), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaineKieliaine("B1", "SV", laajuus = 6), List(
      (diaTutkintoLukukausi("3", 1), "2"),
      (diaTutkintoLukukausi("4", 1), "2"),
      (diaTutkintoLukukausi("5", 1), "4"),
      (diaTutkintoLukukausi("6", 3), "3")
    )),
    diaTutkintoAineSuoritus(diaOppiaineLisäaineKieli("B2", "LA", laajuus = 4), List(
      (diaTutkintoLukukausi("3", 1), "3"),
      (diaTutkintoLukukausi("4", 1), "3"),
      (diaTutkintoLukukausi("5", 1), "2"),
      (diaTutkintoLukukausi("6", 1), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaineKieliaine("B3", "RU", laajuus = 6), List(
      (diaTutkintoLukukausi("3", 1), "4"),
      (diaTutkintoLukukausi("4", 1), "3"),
      (diaTutkintoLukukausi("5", 1), "4"),
      (diaTutkintoLukukausi("6", 3), "3")
    )),
    diaTutkintoAineSuoritus(diaOppiaineMuu("KU", "1", laajuus = 4), List(
      (diaTutkintoLukukausi("3", 1), "4"),
      (diaTutkintoLukukausi("4", 1), "3"),
      (diaTutkintoLukukausi("5", 1), "2"),
      (diaTutkintoLukukausi("6", 1), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaineMuu("MA", "2", laajuus = 8), List(
      (diaTutkintoLukukausi("3", 2), "3"),
      (diaTutkintoLukukausi("4", 2), "1"),
      (diaTutkintoLukukausi("5", 2), "1"),
      (diaTutkintoLukukausi("6", 2), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaineMuu("FY", "2", laajuus = 6), List(
      (diaTutkintoLukukausi("3", 1), "2"),
      (diaTutkintoLukukausi("4", 1), "2"),
      (diaTutkintoLukukausi("5", 1), "1"),
      (diaTutkintoLukukausi("6", 3), "1")
    )),
    diaTutkintoAineSuoritus(diaOppiaineMuu("KE", "2", laajuus = 6), List(
      (diaTutkintoLukukausi("3", 1), "3"),
      (diaTutkintoLukukausi("4", 1), "2"),
      (diaTutkintoLukukausi("5", 1), "1"),
      (diaTutkintoLukukausi("6", 3), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaineMuu("TI", "2", laajuus = 4), List(
      (diaTutkintoLukukausi("3", 1), "2"),
      (diaTutkintoLukukausi("4", 1), "1"),
      (diaTutkintoLukukausi("5", 1), "1"),
      (diaTutkintoLukukausi("6", 1), "1")
    )),
    diaTutkintoAineSuoritus(diaOppiaineMuu("HI", "3", laajuus = 6), List(
      (diaTutkintoLukukausi("3", 1), "3"),
      (diaTutkintoLukukausi("4", 1), "4"),
      (diaTutkintoLukukausi("5", 1), "3"),
      (diaTutkintoLukukausi("6", 3), "1")
    ), suorituskieli = Some("FI")),
    diaTutkintoAineSuoritus(diaOppiaineMuu("TA", "3", laajuus = 6), List(
      (diaTutkintoLukukausi("3", 1), "4"),
      (diaTutkintoLukukausi("4", 1), "3"),
      (diaTutkintoLukukausi("5", 1), "2"),
      (diaTutkintoLukukausi("6", 3), "3")
    )),
    diaTutkintoAineSuoritus(diaOppiaineMuu("MAA", "3", laajuus = 6), List(
      (diaTutkintoLukukausi("3", 1), "3"),
      (diaTutkintoLukukausi("4", 1), "5"),
      (diaTutkintoLukukausi("5", 1), "3"),
      (diaTutkintoLukukausi("6", 3), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaineMuu("FI", "3", laajuus = 4), List(
      (diaTutkintoLukukausi("3", 1), "2"),
      (diaTutkintoLukukausi("4", 1), "2"),
      (diaTutkintoLukukausi("5", 1), "2"),
      (diaTutkintoLukukausi("6", 1), "2")
    )),
    diaTutkintoAineSuoritus(diaOppiaineLisäaine("CCEA", laajuus = 1), List(
      (diaTutkintoLukukausi("5", 0.5f), "3"),
      (diaTutkintoLukukausi("6", 0.5f), "1")
    )),
    diaTutkintoAineSuoritus(diaOppiaineLisäaine("MASY", laajuus = 2), List(
      (diaTutkintoLukukausi("3", 0.5f), "2"),
      (diaTutkintoLukukausi("4", 0.5f), "1"),
      (diaTutkintoLukukausi("5", 0.5f), "2"),
      (diaTutkintoLukukausi("6", 0.5f), "4")
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
        arviointi = diaValmistavaVaiheArviointi(arvosana)
      )
    })
  )

  def diaTutkintoAineSuoritus(oppiaine: DIAOppiaine, lukukaudet: List[(DIAOppiaineenTutkintovaiheenLukukausi, String)] = Nil, suorituskieli: Option[String] = None) = DIAOppiaineenTutkintovaiheenSuoritus(
    koulutusmoduuli = oppiaine,
    suorituskieli = suorituskieli.map(k => Koodistokoodiviite(koodiarvo = k, koodistoUri = "kieli")),
    osasuoritukset = Some(lukukaudet.map { case (lukukausi, arvosana) =>
      DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
        koulutusmoduuli = lukukausi,
        arviointi = diaTutkintovaiheenArviointi(arvosana)
      )
    })
  )

  def diaOppiaineMuu(aine: String, osaAlue: String, laajuus: Int) = DIAOppiaineMuu(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = aine),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus)),
    osaAlue = Koodistokoodiviite(koodiarvo = osaAlue, koodistoUri = "diaosaalue")
  )

  def diaOppiaineKieliaine(taso: String, kieli: String, laajuus: Int) = DIAOppiaineKieli(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = taso),
    kieli = Koodistokoodiviite(koodistoUri = "kielivalikoima", koodiarvo = kieli),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus))
  )

  def diaOppiaineÄidinkieli(kieli: String, laajuus: Int) = DIAOppiaineÄidinkieli(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = "AI"),
    kieli = Koodistokoodiviite(koodistoUri = "oppiainediaaidinkieli", koodiarvo = kieli),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus))
  )

  def diaOppiaineLisäaine(aine: String, laajuus: Int) = DIAOppiaineLisäaine(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = aine),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus))
  )

  def diaOppiaineLisäaineKieli(taso: String, kieli: String, laajuus: Int) = DIAOppiaineLisäaineKieli(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = taso),
    kieli = Koodistokoodiviite(koodistoUri = "kielivalikoima", koodiarvo = kieli),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus))
  )

  def diaValmistavaLukukausi(lukukausi: String, laajuus: Float) = DIAOppiaineenValmistavanVaiheenLukukausi(
    tunniste = Koodistokoodiviite(koodiarvo = lukukausi, koodistoUri = "dialukukausi"),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus))
  )

  def diaTutkintoLukukausi(lukukausi: String, laajuus: Float) = DIAOppiaineenTutkintovaiheenLukukausi(
    tunniste = Koodistokoodiviite(koodiarvo = lukukausi, koodistoUri = "dialukukausi"),
    laajuus = Some(LaajuusVuosiviikkotunneissa(laajuus))
  )

  def diaTutkintovaiheenArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[DIAOppiaineenTutkintovaiheenNumeerinenArviointi]] = {
    Some(List(DIAOppiaineenTutkintovaiheenNumeerinenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkodiatutkinto"), päivä = Some(päivä))))
  }

  def diaValmistavaVaiheArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[DIAOppiaineenValmistavanVaiheenLukukaudenArviointi]] = {
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
