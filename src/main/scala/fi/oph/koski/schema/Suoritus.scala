package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.unlocalized
import fi.oph.scalaschema.annotation._

trait Suoritus {
  @Description("Suorituksen tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) ja eri tasoihin (tutkinto, tutkinnon osa, kurssi, oppiaine...) liittyvät suoritukset")
  @KoodistoUri("suorituksentyyppi")
  @Hidden
  def tyyppi: Koodistokoodiviite
  @Representative
  def koulutusmoduuli: Koulutusmoduuli
  @Description("Suorituksen alkamispäivä. Muoto YYYY-MM-DD")
  def alkamispäivä: Option[LocalDate] = None
  @Description("Opintojen suorituskieli")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID309", "opintosuorituksen kieli")
  def suorituskieli: Option[Koodistokoodiviite]
  @Description("Suorituksen tila (KESKEN, VALMIS, KESKEYTYNYT)")
  @KoodistoUri("suorituksentila")
  def tila: Koodistokoodiviite
  @Description("Arviointi. Jos listalla useampi arviointi, tulkitaan myöhemmät arvioinnit arvosanan korotuksiksi edellisiin samalla listalla oleviin arviointeihin. Jos aiempaa, esimerkiksi väärin kirjattua, arviota korjataan, ei listalle tule uutta arviota")
  def arviointi: Option[List[Arviointi]]
  @Description("Suorituksen virallinen vahvistus (päivämäärä, henkilöt). Vaaditaan silloin, kun suorituksen tila on VALMIS.")
  def vahvistus: Option[Vahvistus]
  def osasuoritukset: Option[List[Suoritus]] = None

  def osasuoritusLista: List[Suoritus] = osasuoritukset.toList.flatten
  def rekursiivisetOsasuoritukset: List[Suoritus] = {
    osasuoritusLista ++ osasuoritusLista.flatMap(_.rekursiivisetOsasuoritukset)
  }
  def viimeisinArviointi = arviointi.toList.flatten.lastOption
  def arvosanaKirjaimin: LocalizedString = viimeisinArviointi.map(_.arvosanaKirjaimin).getOrElse(unlocalized(""))
  def arvosanaNumeroin: Option[LocalizedString] = viimeisinArviointi.flatMap(_.arvosanaNumeroin)
  def sanallinenArviointi: Option[LocalizedString] = viimeisinArviointi.flatMap {
    case a: SanallinenArviointi => a.kuvaus
    case _ => None
  }
  def tarvitseeVahvistuksen = true
  def valmis = tila.koodiarvo == "VALMIS"
}

trait ValmentavaSuoritus extends Suoritus with Toimipisteellinen {
  def todistuksellaNäkyvätLisätiedot: Option[LocalizedString]
  def arviointi = None
  override def osasuoritukset: Option[List[ValmentavanKoulutuksenOsanSuoritus]] = None
}

trait Toimipisteellinen extends OrganisaatioonLiittyvä {
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu. Jos oppilaitoksella ei ole toimipisteitä, syötetään tähän oppilaitoksen tiedot.")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  def toimipiste: OrganisaatioWithOid
  def omistajaOrganisaatio = toimipiste
}

trait PäätasonSuoritus extends Suoritus with Toimipisteellinen {
}