package fi.oph.koski.koskiuser

import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.SuorituksenTyyppi
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
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ, Rooli.READ_UPDATE, Rooli.TAITEENPERUSOPETUS_HANKINTAKOULUTUS, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))

  def oppilaitosPalvelukäyttäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ_UPDATE, Rooli.TIEDONSIIRTO, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))

  def oppilaitosPääkäyttäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ, Rooli.KAIKKI_OPISKELUOIKEUS_TYYPIT, Rooli.TIEDONSIIRRON_MITATOINTI, Rooli.KAYTTOLIITTYMASIIRRON_MITATOINTI, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LAHDEJARJESTELMAKYTKENNAN_PURKAMINEN))

  def vastuukäyttäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(organisaatioOid, List(Rooli.READ))

  val ophPääkäyttäjä: OrganisaatioJaKäyttöoikeudet =
    organisaatioKäyttäjä(Opetushallitus.organisaatioOid, List(Rooli.OPHPAAKAYTTAJA, Rooli.YLLAPITAJA, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LAHDEJARJESTELMAKYTKENNAN_PURKAMINEN))

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

  def päätasonSuoritukseenRajoitettuKatselija(opiskeluoikeudenTyyppi: String, päätasonSuorituksenTyyppi: SuorituksenTyyppi.SuorituksenTyyppi) =
    organisaatioKäyttäjä(Opetushallitus.organisaatioOid, List(
      Rooli.OPHKATSELIJA,
      Rooli.rooliPäätasonSuoritukseen(opiskeluoikeudenTyyppi, päätasonSuorituksenTyyppi)
    ))

  def organisaationPäätasonSuoritukseenRajoitettuPäivittäjä(organisaatioOid: String, opiskeluoikeudenTyyppi: String, päätasonSuorituksenTyyppi: SuorituksenTyyppi.SuorituksenTyyppi) =
    organisaatioKäyttäjä(organisaatioOid, List(
      Rooli.READ_UPDATE,
      Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT,
      Rooli.rooliPäätasonSuoritukseen(opiskeluoikeudenTyyppi, päätasonSuorituksenTyyppi)
    ))
}
