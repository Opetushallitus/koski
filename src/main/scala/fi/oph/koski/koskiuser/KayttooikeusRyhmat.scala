package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.AccessType.{read, write}
import fi.oph.koski.schema.Organisaatio

object Käyttöoikeusryhmät {
  type OrganisaatioKäyttöoikeusryhmä = (Organisaatio.Oid, Käyttöoikeusryhmä)
  private var ryhmät: List[Käyttöoikeusryhmä] = Nil

  val oppilaitosKatselija = add(OrganisaationKäyttöoikeusryhmä("koski-oppilaitos-katselija", "oman organisaation suoritus- ja opiskeluoikeustietojen katselu", List(read)))
  // TODO: todistuksenmyöntäjäroolilla ei vielä käyttöä
  val oppilaitosTodistuksenMyöntäjä = add(OrganisaationKäyttöoikeusryhmä("koski-oppilaitos-todistuksen-myöntäjä", "Tietojen hyväksyntä (tutkinnon myöntäjä, ts. todistuksen myöntäjä (tutkinnon/tutkinnon osan)) Tutkinto/tutkinnon osa vahvistetaan. Tämän jälkeen tutkintoa/tutkinnon osaa ei voida muokata rajapinnan/kälin kautta.", List(read)))
  val oppilaitosTallentaja = add(OrganisaationKäyttöoikeusryhmä("koski-oppilaitos-tallentaja", "tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus, passivointi) käyttöliittymässä", List(read, write)))
  val oppilaitosPalvelukäyttäjä = add(OrganisaationKäyttöoikeusryhmä("koski-oppilaitos-palvelukäyttäjä", "palvelutunnus tiedonsiirroissa: tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus, passivointi)", List(read, write)))

  val ophPääkäyttäjä = add(GlobaaliKäyttöoikeusryhmä("koski-oph-pääkäyttäjä", "CRUP-oikeudet (lisäys, muokkaus, passivointi) Koskessa", List(read, write)))
  val ophKatselija = add(GlobaaliKäyttöoikeusryhmä("koski-oph-katselija", "näkee kaikki Koski-tiedot", List(read)))
  val ophKoskiYlläpito = add(GlobaaliKäyttöoikeusryhmä("koski-oph-ylläpito", "Koski-ylläpitokäyttäjä, ei pääsyä oppijoiden tietoihin", Nil))

  val viranomaisKatselija = add(GlobaaliKäyttöoikeusryhmä("koski-viranomainen-katselija", "näkee oikeuksiensa mukaisesti Koski-tiedot", List(read)))
  val viranomaisPääkäyttäjä = add(GlobaaliKäyttöoikeusryhmä("koski-viranomainen-pääkäyttäjä", "katseluoikeudet, antaa oikeudet Tor-viranomaistietojen katselijalle", List(read)))
  val viranomaisPalvelu = add(GlobaaliKäyttöoikeusryhmä("koski-viranomainen-palvelukäyttäjä", "palvelutunnus, hakee oikeuksiensa mukaiset Koski-tiedot", List(read)))

  def byName(name: String) = ryhmät.find(_.nimi == name)

  def käyttöoikeusryhmät = ryhmät

  private def add[R <: Käyttöoikeusryhmä](ryhmä: R) = {
    ryhmät = ryhmät ++ List(ryhmä)
    ryhmä
  }
}

sealed trait Käyttöoikeusryhmä {
  def nimi: String
  def kuvaus: String
  def orgAccessType: List[AccessType.Value]
  def globalAccessType: List[AccessType.Value]
  override def toString = "Käyttöoikeusryhmä " + nimi
}

case class OrganisaationKäyttöoikeusryhmä private[koskiuser](val nimi: String, val kuvaus: String, val orgAccessType: List[AccessType.Value] = Nil) extends Käyttöoikeusryhmä {
  def globalAccessType = Nil
}

case class GlobaaliKäyttöoikeusryhmä private[koskiuser](val nimi: String, val kuvaus: String, val globalAccessType: List[AccessType.Value] = Nil) extends Käyttöoikeusryhmä {
  def orgAccessType = Nil
}