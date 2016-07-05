package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.AccessType.{read, write}
import fi.oph.koski.schema.Organisaatio

object Käyttöoikeusryhmät {
  type OrganisaatioKäyttöoikeusryhmä = (Organisaatio.Oid, Käyttöoikeusryhmä)
  private var ryhmät: List[Käyttöoikeusryhmä] = Nil

  val oppilaitosKatselija = add(ryhmä("koski-oppilaitos-katselija", "oman organisaation suoritus- ja opiskeluoikeustietojen katselu").withOrgAccess(read))
  // TODO: todistuksenmyöntäjäroolilla ei vielä käyttöä
  val oppilaitosTodistuksenMyöntäjä = add(ryhmä("koski-oppilaitos-todistuksen-myöntäjä", "Tietojen hyväksyntä (tutkinnon myöntäjä, ts. todistuksen myöntäjä (tutkinnon/tutkinnon osan)) Tutkinto/tutkinnon osa vahvistetaan. Tämän jälkeen tutkintoa/tutkinnon osaa ei voida muokata rajapinnan/kälin kautta.").withOrgAccess(read))
  val oppilaitosTallentaja = add(ryhmä("koski-oppilaitos-tallentaja", "tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus, passivointi) käyttöliittymässä").withOrgAccess(read, write))
  val oppilaitosPalvelukäyttäjä = add(ryhmä("koski-oppilaitos-palvelukäyttäjä", "palvelutunnus tiedonsiirroissa: tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus, passivointi)").withOrgAccess(read, write))

  val ophPääkäyttäjä = add(ryhmä("koski-oph-pääkäyttäjä", "CRUP-oikeudet (lisäys, muokkaus, passivointi) Koskessa").withGlobalAccess(read, write))
  val ophKatselija = add(ryhmä("koski-oph-katselija", "näkee kaikki Koski-tiedot").withGlobalAccess(read))

  val viranomaisKatselija = add(ryhmä("koski-viranomainen-katselija", "näkee oikeuksiensa mukaisesti Koski-tiedot").withGlobalAccess(read))
  val viranomaisPääkäyttäjä = add(ryhmä("koski-viranomainen-pääkäyttäjä", "katseluoikeudet, antaa oikeudet Tor-viranomaistietojen katselijalle").withGlobalAccess(read))
  val viranomaisPalvelu = add(ryhmä("koski-viranomainen-palvelukäyttäjä", "palvelutunnus, hakee oikeuksiensa mukaiset Koski-tiedot").withGlobalAccess(read))

  def byName(name: String) = ryhmät.find(_.nimi == name)

  def käyttöoikeusryhmät = ryhmät

  private def add(ryhmä: Käyttöoikeusryhmä) = {
    ryhmät = ryhmät ++ List(ryhmä)
    ryhmä
  }

  private def ryhmä(nimi: String, kuvaus: String) = new Käyttöoikeusryhmä(nimi, kuvaus)
}

class Käyttöoikeusryhmä private[koskiuser](val nimi: String, val kuvaus: String, val orgAccessType: List[AccessType.Value] = Nil, val globalAccessType: List[AccessType.Value] = Nil) {
  def withOrgAccess(newAccess: AccessType.Value*) = {
    new Käyttöoikeusryhmä(nimi, kuvaus, newAccess.toList, globalAccessType)
  }
  def withGlobalAccess(newAccess: AccessType.Value*) = {
    new Käyttöoikeusryhmä(nimi, kuvaus, orgAccessType, newAccess.toList)
  }
  override def toString = "Käyttöoikeusryhmä " + nimi
}


