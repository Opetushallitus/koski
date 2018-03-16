package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.Rooli._

object Käyttöoikeusryhmät {
  private var ryhmät: List[Käyttöoikeusryhmä] = Nil

  val oppilaitosKatselija = add(OrganisaationKäyttöoikeusryhmä("KOSKI-katselija", "oman organisaation suoritus- ja opiskeluoikeustietojen katselu", List(Palvelurooli(READ), Palvelurooli(LUOTTAMUKSELLINEN))))
  val oppilaitosTallentaja = add(OrganisaationKäyttöoikeusryhmä("KOSKI-tallentaja", "tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus) käyttöliittymässä", List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(LUOTTAMUKSELLINEN))))
  val oppilaitosPalvelukäyttäjä = add(OrganisaationKäyttöoikeusryhmä("KOSKI-palvelukäyttäjä", "palvelutunnus tiedonsiirroissa: tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus, passivointi)", List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(TIEDONSIIRTO), Palvelurooli(LUOTTAMUKSELLINEN))))
  val oppilaitosPääkäyttäjä = add(OrganisaationKäyttöoikeusryhmä("KOSKI-pääkäyttäjä", "tietojen katselu ja mitätöinti käyttöliittymässä", List(Palvelurooli(READ), Palvelurooli(TIEDONSIIRRON_MITATOINTI), Palvelurooli(LUOTTAMUKSELLINEN))))
  val vastuukäyttäjä = add(OrganisaationKäyttöoikeusryhmä("Vastuukayttajat", "organisaation vastuukäyttäjä, jolle Koski lähettää tiedonsiirtojen virhe-sähköpostit", List(Palvelurooli(READ))))

  val ophPääkäyttäjä = add(GlobaaliKäyttöoikeusryhmä("koski-oph-pääkäyttäjä", "CRUP-oikeudet (lisäys, muokkaus, passivointi) Koskessa", List(Palvelurooli(OPHPAAKAYTTAJA), Palvelurooli(YLLAPITAJA), Palvelurooli(LUOTTAMUKSELLINEN))))
  val ophKatselija = add(GlobaaliKäyttöoikeusryhmä("koski-oph-katselija", "näkee kaikki Koski-tiedot", List(Palvelurooli(OPHKATSELIJA), Palvelurooli(LUOTTAMUKSELLINEN))))
  val ophKoskiYlläpito = add(GlobaaliKäyttöoikeusryhmä("koski-oph-ylläpito", "Koski-ylläpitokäyttäjä, ei pääsyä oppijoiden tietoihin", List(Palvelurooli(YLLAPITAJA))))

  val viranomaisKatselija = add(GlobaaliKäyttöoikeusryhmä("koski-viranomainen-katselija", "näkee oikeuksiensa mukaisesti Koski-tiedot", List(Palvelurooli(OPHKATSELIJA))))
  val viranomaisPääkäyttäjä = add(GlobaaliKäyttöoikeusryhmä("koski-viranomainen-pääkäyttäjä", "katseluoikeudet, antaa oikeudet Koski-viranomaistietojen katselijalle", List(Palvelurooli(OPHKATSELIJA))))
  val viranomaisPalvelu = add(GlobaaliKäyttöoikeusryhmä("koski-viranomainen-palvelukäyttäjä", "palvelutunnus, hakee oikeuksiensa mukaiset Koski-tiedot", List(Palvelurooli(OPHKATSELIJA), Palvelurooli(TIEDONSIIRTO))))

  val localizationAdmin = add(GlobaaliKäyttöoikeusryhmä("lokalisaatio-admin", "kirjoitusoikeudet lokalisaatiopalveluun", List(Palvelurooli("LOKALISOINTI", "CRUD"))))

  def byName(name: String) = ryhmät.find(_.nimi == name)
  def käyttöoikeusryhmät = ryhmät

  private def add[R <: Käyttöoikeusryhmä](ryhmä: R) = {
    ryhmät = ryhmät.+:(ryhmä)
    ryhmä
  }
}
