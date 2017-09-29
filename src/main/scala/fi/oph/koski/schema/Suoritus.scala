package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.unlocalized
import fi.oph.scalaschema.annotation._

trait Suoritus {
  @Description("Suorituksen tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) ja eri tasoihin (tutkinto, tutkinnon osa, kurssi, oppiaine...) liittyvät suoritukset")
  @KoodistoUri("suorituksentyyppi")
  @Hidden
  @Discriminator
  def tyyppi: Koodistokoodiviite
  @Representative
  def koulutusmoduuli: Koulutusmoduuli
  @Description("Suorituksen alkamispäivä. Muoto YYYY-MM-DD")
  def alkamispäivä: Option[LocalDate] = None
  @Description("Suorituksen tila (KESKEN, VALMIS, KESKEYTYNYT)")
  @KoodistoUri("suorituksentila")
  @SyntheticProperty
  @ReadOnly("Suorituksen tila päätellään automaattisesti. Koski ei enää käsittele tila-kentän arvoa. Kenttä poistetaan tulevaisuudessa tarpeettomana.")
  @Hidden
  def tila: Option[Koodistokoodiviite] = None
  @Description("Arviointi. Jos listalla useampi arviointi, tulkitaan myöhemmät arvioinnit arvosanan korotuksiksi edellisiin samalla listalla oleviin arviointeihin. Jos aiempaa, esimerkiksi väärin kirjattua, arviota korjataan, ei listalle tule uutta arviota")
  def arviointi: Option[List[Arviointi]]
  @Description("Suorituksen virallinen vahvistus (päivämäärä, henkilöt). Vaaditaan kun suorituksen tila on VALMIS")
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
  def tarvitseeVahvistuksen: Boolean = false
  def valmis = vahvistus.isDefined || !tarvitseeVahvistuksen && arviointi.toList.nonEmpty
  def arviointiPuuttuu = arviointi.isEmpty
  def kesken = !valmis
}

trait Suorituskielellinen {
  @Description("Opintojen suorituskieli")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID309", "opintosuorituksen kieli")
  def suorituskieli: Koodistokoodiviite
}

trait MahdollisestiSuorituskielellinen {
  @Description("Opintojen suorituskieli")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID309", "opintosuorituksen kieli")
  def suorituskieli: Option[Koodistokoodiviite]
}

trait Arvioinniton extends Suoritus {
  def arviointi = None
  override def arviointiPuuttuu = false
  def mutuallyExclusiveVahvistuksetonArvioinniton = {}
}

trait Toimipisteellinen extends OrganisaatioonLiittyvä {
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu. Jos oppilaitoksella ei ole toimipisteitä, syötetään tähän oppilaitoksen tiedot")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  @Title("Oppilaitos / toimipiste")
  def toimipiste: OrganisaatioWithOid
  def omistajaOrganisaatio = Some(toimipiste)
}

trait Ryhmällinen {
  @Description("Ryhmän tunniste")
  def ryhmä: Option[String]
}

trait PäätasonSuoritus extends Suoritus with Toimipisteellinen {
  override def tarvitseeVahvistuksen = true
  def mutuallyExclusivePäätasoVahvistukseton = {}
}

trait Todistus extends PäätasonSuoritus with Suorituskielellinen {
  @MultiLineString(3)
  def todistuksellaNäkyvätLisätiedot: Option[LocalizedString]
}

trait Vahvistukseton extends Suoritus {
  override def vahvistus: Option[Vahvistus] = None
  def mutuallyExclusivePäätasoVahvistukseton = {}
  def mutuallyExclusiveVahvistuksetonArvioinniton = {}
}

trait MonikielinenSuoritus {
  @Description("Opintojen muut suorituskielet. Ne muut (kuin koulun opetuskielet) kielet joilla on opetettu vähintään 25% oppilaan oppitunneista lukuvuoden aikana")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID308", "koulutusorganisaation opetuskieli")
  def muutSuorituskielet: Option[List[Koodistokoodiviite]]
}
