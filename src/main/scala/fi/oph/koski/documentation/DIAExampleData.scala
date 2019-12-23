package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object DIAExampleData {
  lazy val saksalainenKoulu: Oppilaitos = MockOrganisaatiot.saksalainenKoulu.copy(oppilaitosnumero = Some(Koodistokoodiviite("00085", None, "oppilaitosnumero", None)), nimi = Some("Helsingin Saksalainen koulu"))

  def laajuus(laajuus: Float, yksikkö: String = "3"): Some[LaajuusVuosiviikkotunneissa] = Some(LaajuusVuosiviikkotunneissa(laajuus, Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = yksikkö)))

  def diaValmistavaVaiheAineSuoritus(oppiaine: DIAOppiaine, lukukaudet: Option[List[(DIAOppiaineenValmistavanVaiheenLukukausi, String)]] = None) = DIAOppiaineenValmistavanVaiheenSuoritus(
    koulutusmoduuli = oppiaine,
    osasuoritukset = lukukaudet.map(_.map { case (lukukausi, arvosana) =>
      DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus(
        koulutusmoduuli = lukukausi,
        arviointi = diaValmistavaVaiheArviointi(arvosana)
      )
    })
  )

  def diaTutkintoAineSuoritus(oppiaine: DIAOppiaine,
                              lukukaudet: Option[List[(DIAOppiaineenTutkintovaiheenOsasuoritus, String)]] = None,
                              suorituskieli: Option[String] = None,
                              koetuloksenNelinkertainenPistemäärä: Option[Int] = None,
                              vastaavuustodistuksenTiedot: Option[DIAVastaavuustodistuksenTiedot] = None) =
    DIAOppiaineenTutkintovaiheenSuoritus(
      koulutusmoduuli = oppiaine,
      suorituskieli = suorituskieli.map(k => Koodistokoodiviite(koodiarvo = k, koodistoUri = "kieli")),
      koetuloksenNelinkertainenPistemäärä = koetuloksenNelinkertainenPistemäärä,
      vastaavuustodistuksenTiedot = vastaavuustodistuksenTiedot,
      osasuoritukset = lukukaudet.map(_.map { case (lukukausi, arvosana) =>
        DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
          koulutusmoduuli = lukukausi,
          arviointi = diaTutkintovaiheenArviointi(arvosana)
        )
      })
    )

  def diaOppiaineMuu(aine: String, osaAlue: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = DIAOppiaineMuu(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = aine),
    laajuus = laajuus,
    osaAlue = Koodistokoodiviite(koodiarvo = osaAlue, koodistoUri = "diaosaalue")
  )

  def diaOppiaineKieliaine(taso: String, kieli: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = DIAOppiaineKieli(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = taso),
    kieli = Koodistokoodiviite(koodistoUri = "kielivalikoima", koodiarvo = kieli),
    laajuus = laajuus
  )

  def diaOppiaineÄidinkieli(kieli: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = DIAOppiaineÄidinkieli(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = "AI"),
    kieli = Koodistokoodiviite(koodistoUri = "oppiainediaaidinkieli", koodiarvo = kieli),
    laajuus = laajuus
  )

  def diaOppiaineLisäaine(aine: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = DIAOppiaineLisäaine(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = aine),
    laajuus = laajuus
  )

  def diaOppiaineLisäaineKieli(taso: String, kieli: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = DIAOppiaineLisäaineKieli(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetdia", koodiarvo = taso),
    kieli = Koodistokoodiviite(koodistoUri = "kielivalikoima", koodiarvo = kieli),
    laajuus = laajuus
  )

  def diaTutkintovaiheenArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[DIAOppiaineenTutkintovaiheenNumeerinenArviointi]] = {
    Some(List(DIAOppiaineenTutkintovaiheenNumeerinenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkodiatutkinto"), päivä = Some(päivä))))
  }

  def diaValmistavaVaiheArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[DIAOppiaineenValmistavanVaiheenLukukaudenArviointi]] = {
    Some(List(DIAOppiaineenValmistavanVaiheenLukukaudenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkodiavalmistava"), päivä = Some(päivä))))
  }

  def diaValmistavaLukukausi(lukukausi: String = "1", laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = DIAOppiaineenValmistavanVaiheenLukukausi(
    tunniste = Koodistokoodiviite(koodiarvo = lukukausi, koodistoUri = "dialukukausi"),
    laajuus = laajuus
  )

  def diaTutkintoLukukausi(lukukausi: String = "3", laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = DIAOppiaineenTutkintovaiheenLukukausi(
    tunniste = Koodistokoodiviite(koodiarvo = lukukausi, koodistoUri = "dialukukausi"),
    laajuus = laajuus
  )

  def diaPäättökoe(koe: String = "kirjallinenkoe") = DIAPäättökoe(tunniste = Koodistokoodiviite(koodiarvo = koe, koodistoUri = "diapaattokoe"))
}
