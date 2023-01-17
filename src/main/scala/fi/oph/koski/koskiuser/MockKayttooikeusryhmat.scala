package fi.oph.koski.koskiuser

import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.userdirectory.{OrganisaatioJaKäyttöoikeudet, PalveluJaOikeus}

object MockKäyttöoikeusryhmät {
  def oppilaitosEsiopetusKatselija(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ, Rooli.LUKU_ESIOPETUS, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))

  def oppilaitosKatselija(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ, Rooli.KAIKKI_OPISKELUOIKEUS_TYYPIT, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))

  def oppilaitosTodistuksenMyöntäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ))

  def oppilaitosTallentaja(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.KAIKKI_OPISKELUOIKEUS_TYYPIT, Rooli.READ_UPDATE, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))

  def oppilaitosTallentajaTaiteenPerusopetusHankintakoulutus(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ, Rooli.READ_UPDATE, Rooli.TAITEENPERUSOPETUS_HANKINTAKOULUTUS))

  def oppilaitosPalvelukäyttäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ, Rooli.READ_UPDATE, Rooli.TIEDONSIIRTO, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))

  def oppilaitosPääkäyttäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ, Rooli.KAIKKI_OPISKELUOIKEUS_TYYPIT, Rooli.TIEDONSIIRRON_MITATOINTI, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))

  def vastuukäyttäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ))

  val ophPääkäyttäjä: OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(Opetushallitus.organisaatioOid, List(Rooli.OPHPAAKAYTTAJA, Rooli.YLLAPITAJA, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))

  val ophKatselija: OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(Opetushallitus.organisaatioOid, List(Rooli.OPHKATSELIJA, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))

  val viranomaisKatselija: OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(Opetushallitus.organisaatioOid, List(Rooli.OPHKATSELIJA))

  val localizationAdmin: OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(Opetushallitus.organisaatioOid, List("CRUD"), "LOKALISOINTI")

  def ilmanLuottamuksellisiaTietoja(orgOid: String): OrganisaatioJaKäyttöoikeudet = {
    oppilaitosTallentaja(orgOid).copy(
      kayttooikeudet = oppilaitosTallentaja(orgOid).kayttooikeudet.filterNot(_.oikeus == Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  }

  def organisaatioKäyttäjä(organisaatioOid: String, roolit: List[String], palvelu: String = "KOSKI"): OrganisaatioJaKäyttöoikeudet = {
    OrganisaatioJaKäyttöoikeudet(organisaatioOid,
      roolit.map(PalveluJaOikeus(palvelu, _))
    )
  }
}
