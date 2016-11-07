package fi.oph.koski.koskiuser

import fi.oph.koski.henkilo.AuthenticationServiceClient.Palvelurooli
import fi.oph.koski.koskiuser.AccessType.{read, write}
import fi.oph.koski.koskiuser.Rooli._

object Käyttöoikeusryhmät {
  private var ryhmät: List[Käyttöoikeusryhmä] = Nil

  val oppilaitosKatselija = add(OrganisaationKäyttöoikeusryhmä("koski-oppilaitos-katselija", "oman organisaation suoritus- ja opiskeluoikeustietojen katselu", List(Palvelurooli(READ))))
  val oppilaitosTodistuksenMyöntäjä = add(OrganisaationKäyttöoikeusryhmä("koski-oppilaitos-todistuksen-myöntäjä", "Tietojen hyväksyntä (tutkinnon myöntäjä, ts. todistuksen myöntäjä (tutkinnon/tutkinnon osan)) Tutkinto/tutkinnon osa vahvistetaan. Tämän jälkeen tutkintoa/tutkinnon osaa ei voida muokata rajapinnan/kälin kautta.", List(Palvelurooli(READ))))
  val oppilaitosTallentaja = add(OrganisaationKäyttöoikeusryhmä("koski-oppilaitos-tallentaja", "tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus, passivointi) käyttöliittymässä", List(Palvelurooli(READ), Palvelurooli(READ_UPDATE))))
  val oppilaitosPalvelukäyttäjä = add(OrganisaationKäyttöoikeusryhmä("koski-oppilaitos-palvelukäyttäjä", "palvelutunnus tiedonsiirroissa: tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus, passivointi)", List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(TIEDONSIIRTO))))
  val vastuukäyttäjä = add(OrganisaationKäyttöoikeusryhmä("Vastuukayttajat", "organisaation vastuukäyttäjä, jolle Koski lähettää tiedonsiirtojen virhe-sähköpostit", List(Palvelurooli(READ))))

  val ophPääkäyttäjä = add(GlobaaliKäyttöoikeusryhmä("koski-oph-pääkäyttäjä", "CRUP-oikeudet (lisäys, muokkaus, passivointi) Koskessa", List(Palvelurooli(OPHPAAKAYTTAJA), Palvelurooli(YLLAPITAJA))))
  val ophKatselija = add(GlobaaliKäyttöoikeusryhmä("koski-oph-katselija", "näkee kaikki Koski-tiedot", List(Palvelurooli(OPHKATSELIJA))))
  val ophKoskiYlläpito = add(GlobaaliKäyttöoikeusryhmä("koski-oph-ylläpito", "Koski-ylläpitokäyttäjä, ei pääsyä oppijoiden tietoihin", List(Palvelurooli(YLLAPITAJA))))

  val viranomaisKatselija = add(GlobaaliKäyttöoikeusryhmä("koski-viranomainen-katselija", "näkee oikeuksiensa mukaisesti Koski-tiedot", List(Palvelurooli(OPHKATSELIJA))))
  val viranomaisPääkäyttäjä = add(GlobaaliKäyttöoikeusryhmä("koski-viranomainen-pääkäyttäjä", "katseluoikeudet, antaa oikeudet Koski-viranomaistietojen katselijalle", List(Palvelurooli(OPHKATSELIJA))))
  val viranomaisPalvelu = add(GlobaaliKäyttöoikeusryhmä("koski-viranomainen-palvelukäyttäjä", "palvelutunnus, hakee oikeuksiensa mukaiset Koski-tiedot", List(Palvelurooli(OPHKATSELIJA), Palvelurooli(TIEDONSIIRTO))))

  def byName(name: String) = ryhmät.find(_.nimi == name)
  def käyttöoikeusryhmät = ryhmät

  private def add[R <: Käyttöoikeusryhmä](ryhmä: R) = {
    ryhmät = ryhmät.+:(ryhmä)
    ryhmä
  }
}