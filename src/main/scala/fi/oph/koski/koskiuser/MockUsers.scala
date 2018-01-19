package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.AuthenticationUser.fromDirectoryUser
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät._
import fi.oph.koski.organisaatio.MockOrganisaatiot.{jyväskylänNormaalikoulu, lehtikuusentienToimipiste, omnia, oppilaitokset}
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.schema.{OidOrganisaatio, Organisaatio}
import fi.oph.koski.userdirectory.DirectoryUser

object MockUsers {
  private val ilmanLuottamuksellisiaTietoja = oppilaitosTallentaja.copy(palveluroolit = oppilaitosTallentaja.palveluroolit.filterNot(_.rooli == "LUOTTAMUKSELLINEN"))

  val kalle = MockUser("käyttäjä", "kalle", "1.2.246.562.24.99999999987", (lehtikuusentienToimipiste :: oppilaitokset).toSet.map((_: String, oppilaitosTallentaja)))
  val pärre = MockUser("käyttäjä", "pärre", "1.2.246.562.24.99999999901", (lehtikuusentienToimipiste :: oppilaitokset).toSet.map((_: String, oppilaitosTallentaja)), "sv")
  val localkoski = MockUser("käyttäjä", "localkoski", "1.2.246.562.24.99999999988", oppilaitokset.toSet.map((_: String, oppilaitosTallentaja)))
  val omniaPalvelukäyttäjä = MockUser("käyttäjä", "omnia-palvelukäyttäjä", "1.2.246.562.24.99999999989", Set((omnia, oppilaitosPalvelukäyttäjä)))
  val omniaKatselija = MockUser("käyttäjä", "omnia-katselija", "1.2.246.562.24.99999999990", Set((omnia, oppilaitosKatselija)))
  val omniaTallentaja = MockUser("käyttäjä", "omnia-tallentaja", "1.2.246.562.24.99999999991", Set((omnia, oppilaitosTallentaja)))
  val tallentajaEiLuottamuksellinen = MockUser("epäluotettava-tallentaja", "epäluotettava-tallentaja", "1.2.246.562.24.99999999997",
    Set((omnia, ilmanLuottamuksellisiaTietoja), (jyväskylänNormaalikoulu, ilmanLuottamuksellisiaTietoja)))
  val paakayttaja = MockUser("käyttäjä", "pää", "1.2.246.562.24.99999999992", Set(
    (Opetushallitus.organisaatioOid, ophPääkäyttäjä),
    (Opetushallitus.organisaatioOid, localizationAdmin),
    (Opetushallitus.organisaatioOid, GlobaaliKäyttöoikeusryhmä("joku-henkilo-ui", "jotain oikeuksia henkilo-ui:ssa", List(Palvelurooli("HENKILONHALLINTA", "CRUD"))))
  ))
  val viranomainen = MockUser("käyttäjä", "viranomais", "1.2.246.562.24.99999999993", Set((Opetushallitus.organisaatioOid, viranomaisKatselija)))
  val helsinginKaupunkiPalvelukäyttäjä = MockUser("stadin-palvelu", "stadin-palvelu", "1.2.246.562.24.99999999994", Set((MockOrganisaatiot.helsinginKaupunki, oppilaitosPalvelukäyttäjä)))
  val stadinAmmattiopistoTallentaja = MockUser("tallentaja", "tallentaja", "1.2.246.562.24.99999999995", Set((MockOrganisaatiot.stadinAmmattiopisto, oppilaitosTallentaja)))
  val stadinAmmattiopistoKatselija = MockUser("katselija", "katselija", "1.2.246.562.24.99999999985", Set((MockOrganisaatiot.stadinAmmattiopisto, oppilaitosKatselija)))
  val stadinVastuukäyttäjä = MockUser("stadin-vastuu", "stadin-vastuu", "1.2.246.562.24.99999999996", Set((MockOrganisaatiot.helsinginKaupunki, vastuukäyttäjä)))
  val hkiTallentaja = MockUser("hki-tallentaja", "hki-tallentaja", "1.2.246.562.24.99999999977", Set((MockOrganisaatiot.helsinginKaupunki, oppilaitosTallentaja)))
  val kahdenOrganisaatioPalvelukäyttäjä = MockUser("palvelu2", "palvelu2", "1.2.246.562.24.99999999998", Set((MockOrganisaatiot.helsinginKaupunki, oppilaitosPalvelukäyttäjä), (MockOrganisaatiot.omnia, oppilaitosPalvelukäyttäjä)))
  val omattiedot = MockUser("Oppija", "Oili", "1.2.246.562.24.99999999999", Set((omnia, oppilaitosTallentaja)))
  val eiOikkia = MockUser("EiOikkia", "Otto", "1.2.246.562.24.99999999902", Set())

  val users = List(
    kalle,
    pärre,
    omniaPalvelukäyttäjä,
    omniaKatselija,
    omniaTallentaja,
    localkoski,
    paakayttaja,
    viranomainen,
    helsinginKaupunkiPalvelukäyttäjä,
    stadinAmmattiopistoTallentaja,
    stadinAmmattiopistoKatselija,
    kahdenOrganisaatioPalvelukäyttäjä,
    omattiedot,
    stadinVastuukäyttäjä,
    tallentajaEiLuottamuksellinen,
    hkiTallentaja,
    eiOikkia
  )
}

case class MockUser(ldapUser: DirectoryUser, käyttöoikeudet: Set[(Organisaatio.Oid, Käyttöoikeusryhmä)]) extends UserWithPassword {
  def toKoskiUser(käyttöoikeudet: KäyttöoikeusRepository) = {
    val authUser: AuthenticationUser = fromDirectoryUser(ldapUser.oid, ldapUser)
    new KoskiSession(authUser, "fi", "192.168.0.10", käyttöoikeudet.käyttäjänKäyttöoikeudet(authUser))
  }
  def oid = ldapUser.oid
  def username = ldapUser.etunimet
  def password = username
}

object MockUser {
  def apply(lastname: String, firstname: String, oid: String, käyttöoikeusryhmät: Set[(Organisaatio.Oid, Käyttöoikeusryhmä)], lang: String = "fi"): MockUser = {
    val oikeudet = käyttöoikeusryhmät.map { case (oid, ryhmä) =>
      oid match {
        case Opetushallitus.organisaatioOid => KäyttöoikeusGlobal(ryhmä.palveluroolit)
        case oid => KäyttöoikeusOrg(OidOrganisaatio(oid), ryhmä.palveluroolit, true, None)
      }
    }.toList
    MockUser(DirectoryUser(oid, oikeudet, firstname, lastname, Some(lang)), käyttöoikeusryhmät)
  }
}

