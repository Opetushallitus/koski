package fi.oph.common.koskiuser

import fi.oph.common.koskiuser.Rooli._
import fi.oph.koski.schema.{Koulutustoimija, OidOrganisaatio}

object MockKäyttöoikeusryhmät {
  def oppilaitosEsiopetusKatselija(organisaatioOid: String) = KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), List(Palvelurooli(READ), Palvelurooli(LUKU_ESIOPETUS), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), true, None)
  def oppilaitosKatselija(organisaatioOid: String) = KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), List(Palvelurooli(READ), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), true, None)
  def oppilaitosTodistuksenMyöntäjä(organisaatioOid: String) = KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), List(Palvelurooli(READ)), true, None)
  def oppilaitosTallentaja(organisaatioOid: String) = KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), true, None)
  def varhaisKasvatusTallentaja(koulutustoimijaOid: String, organisaatioOid: String) =
    KäyttöoikeusVarhaiskasvatusToimipiste(Koulutustoimija(koulutustoimijaOid), OidOrganisaatio(organisaatioOid), List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)))
  def oppilaitosPalvelukäyttäjä(organisaatioOid: String) = KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(TIEDONSIIRTO), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), true, None)
  def oppilaitosPääkäyttäjä(organisaatioOid: String) = KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), List(Palvelurooli(READ), Palvelurooli(TIEDONSIIRRON_MITATOINTI), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), true, None)
  def vastuukäyttäjä(organisaatioOid: String) = KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), List(Palvelurooli(READ)), true, None)

  val ophPääkäyttäjä = KäyttöoikeusGlobal(List(Palvelurooli(OPHPAAKAYTTAJA), Palvelurooli(YLLAPITAJA), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)))
  val ophKatselija = KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)))
  val viranomaisKatselija = KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA)))
  val localizationAdmin = KäyttöoikeusGlobal(List(Palvelurooli("LOKALISOINTI", "CRUD")))
}
