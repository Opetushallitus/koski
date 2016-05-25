package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString.unlocalized
import fi.oph.scalaschema.annotation._

trait Suoritus {
  @Description("Suorituksen tyyppi")
  @KoodistoUri("suorituksentyyppi")
  def tyyppi: Koodistokoodiviite
  def koulutusmoduuli: Koulutusmoduuli
  @Description("Paikallinen tunniste suoritukselle. Tiedonsiirroissa tarpeellinen, jotta voidaan varmistaa päivitysten osuminen oikeaan suoritukseen")
  def paikallinenId: Option[String]
  @Description("Suorituksen alkamispäivä")
  def alkamispäivä: Option[LocalDate] = None
  @Description("Opintojen suorituskieli")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID309", "opintosuorituksen kieli")
  def suorituskieli: Option[Koodistokoodiviite]
  @Description("Suorituksen tila (KESKEN, VALMIS, KESKEYTYNYT)")
  @KoodistoUri("suorituksentila")
  def tila: Koodistokoodiviite
  @Description("Arviointi. Jos listalla useampi arviointi, tulkitaan myöhemmät arvioinnit arvosanan korotuksiksi. Jos aiempaa, esimerkiksi väärin kirjattua, arviota korjataan, ei listalle tule uutta arviota")
  def arviointi: Option[List[Arviointi]]
  @Description("Suorituksen virallinen vahvistus (päivämäärä, henkilöt). Vaaditaan silloin, kun suorituksen tila on VALMIS.")
  def vahvistus: Option[Vahvistus]
  def osasuoritukset: Option[List[Suoritus]] = None

  def osasuoritusLista: List[Suoritus] = osasuoritukset.toList.flatten
  def rekursiivisetOsasuoritukset: List[Suoritus] = {
    osasuoritusLista ++ osasuoritusLista.flatMap(_.rekursiivisetOsasuoritukset)
  }
  def arvosanaKirjaimin = arviointi.toList.flatten.lastOption.map(_.arvosanaKirjaimin).getOrElse(unlocalized(""))
  def arvosanaNumeroin = arviointi.toList.flatten.lastOption.flatMap(_.arvosanaNumeroin).getOrElse("")
  def tarvitseeVahvistuksen = true
}
