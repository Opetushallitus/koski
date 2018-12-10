package fi.oph.koski.koskiuser

import java.net.InetAddress

import fi.oph.koski.koskiuser.AuthenticationUser.fromDirectoryUser
import fi.oph.koski.koskiuser.MockKäyttöoikeusryhmät._
import fi.oph.koski.koskiuser.Rooli.{GLOBAALI_LUKU_KORKEAKOULU, GLOBAALI_LUKU_PERUSOPETUS, GLOBAALI_LUKU_TOINEN_ASTE}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.{jyväskylänNormaalikoulu, lehtikuusentienToimipiste, omnia, oppilaitokset}
import fi.oph.koski.userdirectory.DirectoryUser

object MockUsers {
  private def ilmanLuottamuksellisiaTietoja(orgOid: String) = {
    oppilaitosTallentaja(orgOid).copy(organisaatiokohtaisetPalveluroolit = oppilaitosTallentaja(orgOid).organisaatiokohtaisetPalveluroolit.filterNot(_.rooli == "LUOTTAMUKSELLINEN"))
  }

  val kalle = MockUser("käyttäjä", "kalle", "1.2.246.562.24.99999999987", (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja).toSet)

  val pärre = MockUser("käyttäjä", "pärre", "1.2.246.562.24.99999999901", (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja).toSet, "sv")
  val localkoski = MockUser("käyttäjä", "localkoski", "1.2.246.562.24.99999999988", oppilaitokset.map(oppilaitosTallentaja).toSet)
  val omniaPalvelukäyttäjä = MockUser("käyttäjä", "omnia-palvelukäyttäjä", "1.2.246.562.24.99999999989", Set(oppilaitosPalvelukäyttäjä(omnia)))
  val omniaKatselija = MockUser("käyttäjä", "omnia-katselija", "1.2.246.562.24.99999999990", Set(oppilaitosKatselija(omnia)))
  val omniaTallentaja = MockUser("käyttäjä", "omnia-tallentaja", "1.2.246.562.24.99999999991", Set(oppilaitosTallentaja(omnia)))
  val omniaPääkäyttäjä = MockUser("omnia-pää", "omnia-pää", "1.2.246.562.24.99999977777", Set(oppilaitosPääkäyttäjä(MockOrganisaatiot.omnia)))
  val tallentajaEiLuottamuksellinen = MockUser("epäluotettava-tallentaja", "epäluotettava-tallentaja", "1.2.246.562.24.99999999997", Set(ilmanLuottamuksellisiaTietoja(omnia), ilmanLuottamuksellisiaTietoja(jyväskylänNormaalikoulu)))
  val paakayttaja = MockUser("käyttäjä", "pää", "1.2.246.562.24.99999999992", Set(ophPääkäyttäjä, localizationAdmin, KäyttöoikeusGlobal(List(Palvelurooli("HENKILONHALLINTA", "CRUD")))))
  val viranomainen = MockUser("käyttäjä", "viranomais", "1.2.246.562.24.99999999993", Set(viranomaisKatselija))
  val helsinginKaupunkiPalvelukäyttäjä = MockUser("stadin-palvelu", "stadin-palvelu", "1.2.246.562.24.99999999994", Set(oppilaitosPalvelukäyttäjä(MockOrganisaatiot.helsinginKaupunki)))
  val stadinAmmattiopistoTallentaja = MockUser("tallentaja", "tallentaja", "1.2.246.562.24.99999999995", Set(oppilaitosTallentaja(MockOrganisaatiot.stadinAmmattiopisto)))
  val stadinAmmattiopistoKatselija = MockUser("katselija", "katselija", "1.2.246.562.24.99999999985", Set(oppilaitosKatselija(MockOrganisaatiot.stadinAmmattiopisto)))
  val stadinAmmattiopistoPääkäyttäjä = MockUser("stadinammattiopisto-admin", "stadinammattiopisto-admin", "1.2.246.562.24.99999999986", Set(oppilaitosPääkäyttäjä(MockOrganisaatiot.stadinAmmattiopisto)), "fi", List("koski-oppilaitos-pääkäyttäjä_1494486198456"))
  val stadinVastuukäyttäjä = MockUser("stadin-vastuu", "stadin-vastuu", "1.2.246.562.24.99999999996", Set(vastuukäyttäjä(MockOrganisaatiot.helsinginKaupunki)))
  val stadinPääkäyttäjä = MockUser("stadin-pää", "stadin-pää", "1.2.246.562.24.99999999997", Set(oppilaitosPääkäyttäjä(MockOrganisaatiot.helsinginKaupunki)), "fi", List("koski-oppilaitos-pääkäyttäjä_1494486198456"))
  val hkiTallentaja = MockUser("hki-tallentaja", "hki-tallentaja", "1.2.246.562.24.99999999977", Set(oppilaitosTallentaja(MockOrganisaatiot.helsinginKaupunki)))
  val kahdenOrganisaatioPalvelukäyttäjä = MockUser("palvelu2", "palvelu2", "1.2.246.562.24.99999999998", Set(oppilaitosPalvelukäyttäjä(MockOrganisaatiot.helsinginKaupunki), oppilaitosPalvelukäyttäjä(MockOrganisaatiot.omnia)))
  val omattiedot = MockUser("Oppija", "Oili", "1.2.246.562.24.99999999999", Set(oppilaitosTallentaja(omnia)))
  val eiOikkia = MockUser("EiOikkia", "Otto", "1.2.246.562.24.99999999902", Set())
  val evira = MockUser("Evira", "Eeva", "1.2.246.562.24.99999999111", Set(KäyttöoikeusGlobalByKoulutusmuoto(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val perusopetusViranomainen = MockUser("Perusopetus", "Pertti", "1.2.246.562.24.99999999222", Set(KäyttöoikeusGlobalByKoulutusmuoto(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS)))))
  val toinenAsteViranomainen = MockUser("Toinenaste", "Teuvo", "1.2.246.562.24.99999999333", Set(KäyttöoikeusGlobalByKoulutusmuoto(List(Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE)))))
  val korkeakouluViranomainen = MockUser("Korkeakoulu", "Kaisa", "1.2.246.562.24.99999999444", Set(KäyttöoikeusGlobalByKoulutusmuoto(List(Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val jyväskylänNormaalikoulunPalvelukäyttäjä = MockUser("jyväs-palvelu", "jyväs-palvelu", "1.2.246.562.24.99999999777", Set(oppilaitosPalvelukäyttäjä(MockOrganisaatiot.jyväskylänNormaalikoulu)))
  val jyväskylänYliopistonVastuukäyttäjä = MockUser("jyväs-vastuu", "jyväs-vastuu", "1.2.246.562.24.99999997777", Set(vastuukäyttäjä(MockOrganisaatiot.jyväskylänYliopisto)), "fi", List("Vastuukayttajat"))
  val luovutuspalveluKäyttäjä = MockUser("Luovutus", "Lasse", "1.2.246.562.24.99999988888", Set(KäyttöoikeusGlobalLuovutuspalvelu, KäyttöoikeusGlobalByKoulutusmuoto(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val suomiFiKäyttäjä = luovutuspalveluKäyttäjä.copy(firstname = "Suomi", lastname = "Fi", oid="1.2.246.562.24.99999988889")

  val users = List(
    kalle,
    pärre,
    omniaPalvelukäyttäjä,
    omniaKatselija,
    omniaTallentaja,
    omniaPääkäyttäjä,
    localkoski,
    paakayttaja,
    viranomainen,
    helsinginKaupunkiPalvelukäyttäjä,
    stadinAmmattiopistoPääkäyttäjä,
    stadinAmmattiopistoTallentaja,
    stadinAmmattiopistoKatselija,
    kahdenOrganisaatioPalvelukäyttäjä,
    omattiedot,
    stadinVastuukäyttäjä,
    stadinPääkäyttäjä,
    tallentajaEiLuottamuksellinen,
    hkiTallentaja,
    eiOikkia,
    jyväskylänNormaalikoulunPalvelukäyttäjä,
    jyväskylänYliopistonVastuukäyttäjä,
    evira,
    perusopetusViranomainen,
    toinenAsteViranomainen,
    korkeakouluViranomainen,
    luovutuspalveluKäyttäjä,
    suomiFiKäyttäjä
  )
}

case class MockUser(lastname: String, firstname: String, oid: String, käyttöoikeudet: Set[Käyttöoikeus], lang: String = "fi", käyttöoikeusRyhmät: List[String] = Nil) extends UserWithPassword {
  lazy val ldapUser = DirectoryUser(oid, käyttöoikeudet.toList, firstname, lastname, Some(lang))
  def toKoskiUser(käyttöoikeudet: KäyttöoikeusRepository) = {
    val authUser: AuthenticationUser = fromDirectoryUser(username, ldapUser)
    new KoskiSession(authUser, "fi", InetAddress.getByName("192.168.0.10"), "", käyttöoikeudet.käyttäjänKäyttöoikeudet(authUser))
  }
  def username = ldapUser.etunimet
  def password = username
}

