package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.schema.OrganisaatioWithOid

object MockKäyttöoikeusryhmät {
  def oppilaitosEsiopetusKatselija(juuriOrganisaatio: OrganisaatioWithOid, organisaatio: OrganisaatioWithOid) =
    KäyttöoikeusOrg(juuriOrganisaatio.oid, organisaatio.oid, List(Palvelurooli(READ), Palvelurooli(LUKU_ESIOPETUS), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), None)

  def oppilaitosKatselija(juuriOrganisaatio: OrganisaatioWithOid, organisaatio: OrganisaatioWithOid) =
    KäyttöoikeusOrg(juuriOrganisaatio.oid, organisaatio.oid, List(Palvelurooli(READ), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), None)

  def oppilaitosTodistuksenMyöntäjä(juuriOrganisaatio: OrganisaatioWithOid, organisaatio: OrganisaatioWithOid) =
    KäyttöoikeusOrg(juuriOrganisaatio.oid, organisaatio.oid, List(Palvelurooli(READ)), None)

  def oppilaitosTallentaja(juuriOrganisaatio: OrganisaatioWithOid, organisaatio: OrganisaatioWithOid) =
    KäyttöoikeusOrg(juuriOrganisaatio.oid, organisaatio.oid, List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), None)

  def oppilaitosPalvelukäyttäjä(juuriOrganisaatio: OrganisaatioWithOid, organisaatio: OrganisaatioWithOid) =
    KäyttöoikeusOrg(juuriOrganisaatio.oid, organisaatio.oid, List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(TIEDONSIIRTO), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), None)

  def oppilaitosPääkäyttäjä(juuriOrganisaatio: OrganisaatioWithOid, organisaatio: OrganisaatioWithOid) =
    KäyttöoikeusOrg(juuriOrganisaatio.oid, organisaatio.oid, List(Palvelurooli(READ), Palvelurooli(TIEDONSIIRRON_MITATOINTI), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), None)

  def vastuukäyttäjä(juuriOrganisaatio: OrganisaatioWithOid, organisaatio: OrganisaatioWithOid) =
    KäyttöoikeusOrg(juuriOrganisaatio.oid, organisaatio.oid, List(Palvelurooli(READ)), None)

  val ophPääkäyttäjä = KäyttöoikeusGlobal(List(Palvelurooli(OPHPAAKAYTTAJA), Palvelurooli(YLLAPITAJA), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)))
  val ophKatselija = KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)))
  val viranomaisKatselija = KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA)))
  val localizationAdmin = KäyttöoikeusGlobal(List(Palvelurooli("LOKALISOINTI", "CRUD")))
}
