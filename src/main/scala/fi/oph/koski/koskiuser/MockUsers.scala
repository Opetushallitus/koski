package fi.oph.koski.koskiuser

import java.net.InetAddress

import fi.oph.koski.koskiuser.AuthenticationUser.fromDirectoryUser
import fi.oph.koski.koskiuser.MockKäyttöoikeusryhmät._
import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.schema.OrganisaatioWithOid
import fi.oph.koski.userdirectory.DirectoryUser

object MockUsers {
  private def ilmanLuottamuksellisiaTietoja(org: OrganisaatioWithOid) = {
    oppilaitosTallentaja(org, org).copy(organisaatiokohtaisetPalveluroolit = oppilaitosTallentaja(org, org).organisaatiokohtaisetPalveluroolit.filterNot(_.rooli == Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  }

  val kalle = MockUser("käyttäjä", "kalle", "1.2.246.562.24.99999999987", oppilaitokset.map(o => oppilaitosTallentaja(o, o)).toSet)
  val pärre = MockUser("käyttäjä", "pärre", "1.2.246.562.24.99999999901", oppilaitokset.map(o => oppilaitosTallentaja(o, o)).toSet, "sv")
  val localkoski = MockUser("käyttäjä", "localkoski", "1.2.246.562.24.99999999988", oppilaitokset.map(o => oppilaitosTallentaja(o, o)).toSet)
  val omniaPalvelukäyttäjä = MockUser("käyttäjä", "omnia-palvelukäyttäjä", "1.2.246.562.24.99999999989", Set(oppilaitosPalvelukäyttäjä(omnia, omnia)))
  val omniaKatselija = MockUser("käyttäjä", "omnia-katselija", "1.2.246.562.24.99999999990", Set(oppilaitosKatselija(omnia, omnia)))
  val omniaTallentaja = MockUser("käyttäjä", "omnia-tallentaja", "1.2.246.562.24.99999999991", Set(oppilaitosTallentaja(omnia, omnia)))
  val omniaPääkäyttäjä = MockUser("omnia-pää", "omnia-pää", "1.2.246.562.24.99999977777", Set(oppilaitosPääkäyttäjä(omnia, omnia)))
  val tallentajaEiLuottamuksellinen = MockUser("epäluotettava-tallentaja", "epäluotettava-tallentaja", "1.2.246.562.24.99999999997", Set(ilmanLuottamuksellisiaTietoja(omnia), ilmanLuottamuksellisiaTietoja(jyväskylänNormaalikoulu)))
  val paakayttaja = MockUser("käyttäjä", "pää", "1.2.246.562.24.99999999992", Set(ophPääkäyttäjä, localizationAdmin, KäyttöoikeusGlobal(List(Palvelurooli("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA")))))
  val viranomainen = MockUser("käyttäjä", "viranomais", "1.2.246.562.24.99999999993", Set(viranomaisKatselija))
  val helsinginKaupunkiPalvelukäyttäjä = MockUser("stadin-palvelu", "stadin-palvelu", "1.2.246.562.24.99999999994", Set(oppilaitosPalvelukäyttäjä(helsinginKaupunki, helsinginKaupunki)))
  val helsinginKaupunkiEsiopetus = MockUser("stadin-esiopetus", "stadin-esiopetus", "1.2.246.562.24.99999999944", Set(oppilaitosEsiopetusKatselija(helsinginKaupunki, helsinginKaupunki)))
  val stadinAmmattiopistoTallentaja = MockUser("tallentaja", "tallentaja", "1.2.246.562.24.99999999995", Set(oppilaitosTallentaja(stadinAmmattiopisto, stadinAmmattiopisto)))
  val stadinAmmattiopistoKatselija = MockUser("katselija", "katselija", "1.2.246.562.24.99999999985", Set(oppilaitosKatselija(stadinAmmattiopisto, stadinAmmattiopisto)))
  val jyväskylänKatselijaEsiopetus = MockUser("esiopetus", "esiopetus", "1.2.246.562.24.99999999666", Set(oppilaitosEsiopetusKatselija(jyväskylänNormaalikoulu, jyväskylänNormaalikoulu)))
  val jyväskylänKatselijaEiLuottamuksellinen = MockUser("jyvas-eiluottoa", "jyvas-eiluottoa", "1.2.246.562.24.99999999888", Set(ilmanLuottamuksellisiaTietoja(jyväskylänNormaalikoulu)))
  val stadinAmmattiopistoPääkäyttäjä = MockUser("stadinammattiopisto-admin", "stadinammattiopisto-admin", "1.2.246.562.24.99999999986", Set(oppilaitosPääkäyttäjä(stadinAmmattiopisto, stadinAmmattiopisto)), "fi", List("koski-oppilaitos-pääkäyttäjä_1494486198456"))
  val stadinVastuukäyttäjä = MockUser("stadin-vastuu", "stadin-vastuu", "1.2.246.562.24.99999999996", Set(vastuukäyttäjä(helsinginKaupunki, helsinginKaupunki)))
  val stadinPääkäyttäjä = MockUser("stadin-pää", "stadin-pää", "1.2.246.562.24.99999999997", Set(oppilaitosPääkäyttäjä(helsinginKaupunki, helsinginKaupunki)), "fi", List("koski-oppilaitos-pääkäyttäjä_1494486198456"))
  val hkiTallentaja = MockUser("hki-tallentaja", "hki-tallentaja", "1.2.246.562.24.99999999977", Set(oppilaitosTallentaja(helsinginKaupunki, helsinginKaupunki)))
  val kahdenOrganisaatioPalvelukäyttäjä = MockUser("palvelu2", "palvelu2", "1.2.246.562.24.99999999998", Set(oppilaitosPalvelukäyttäjä(helsinginKaupunki, helsinginKaupunki), oppilaitosPalvelukäyttäjä(omnia, omnia)))
  val omattiedot = MockUser("Oppija", "Oili", "1.2.246.562.24.99999999999", Set(oppilaitosTallentaja(omnia, omnia)))
  val eiOikkia = MockUser("EiOikkia", "Otto", "1.2.246.562.24.99999999902", Set(KäyttöoikeusOrg(lehtikuusentienToimipiste.oid, lehtikuusentienToimipiste.oid, List(Palvelurooli("OPPIJANUMEROREKISTERI", READ)), oppilaitostyyppi = None)))
  val evira = MockUser("Evira", "Eeva", "1.2.246.562.24.99999999111", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val kelaSuppeatOikeudet = MockUser("Kela", "Suppea", "1.2.246.562.24.88888888111", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU), Palvelurooli(LUOTTAMUKSELLINEN_KELA_SUPPEA)))))
  val kelaLaajatOikeudet = MockUser("Kela", "Laaja", "1.2.246.562.24.88888888222", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU), Palvelurooli(LUOTTAMUKSELLINEN_KELA_LAAJA)))))
  val perusopetusViranomainen = MockUser("Perusopetus", "Pertti", "1.2.246.562.24.99999999222", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS)))))
  val toinenAsteViranomainen = MockUser("Toinenaste", "Teuvo", "1.2.246.562.24.99999999333", Set(KäyttöoikeusGlobal(List(Palvelurooli("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA"))), KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE)))))
  val korkeakouluViranomainen = MockUser("Korkeakoulu", "Kaisa", "1.2.246.562.24.99999999444", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val jyväskylänNormaalikoulunPalvelukäyttäjä = MockUser("jyväs-palvelu", "jyväs-palvelu", "1.2.246.562.24.99999999777", Set(oppilaitosPalvelukäyttäjä(jyväskylänNormaalikoulu, jyväskylänNormaalikoulu)))
  val jyväskylänYliopistonVastuukäyttäjä = MockUser("jyväs-vastuu", "jyväs-vastuu", "1.2.246.562.24.99999997777", Set(vastuukäyttäjä(jyväskylänYliopisto, jyväskylänYliopisto)), "fi", List("Vastuukayttajat"))
  val luovutuspalveluKäyttäjä = MockUser("Luovutus", "Lasse", "1.2.246.562.24.99999988888", Set(KäyttöoikeusViranomainen(List(Palvelurooli(TIEDONSIIRTO_LUOVUTUSPALVELU), Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val luovutuspalveluKäyttäjäArkaluontoinen = MockUser("Arkaluontoinen", "Antti", "1.2.246.562.24.88888877777", Set(KäyttöoikeusViranomainen(List(Palvelurooli(TIEDONSIIRTO_LUOVUTUSPALVELU), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT), Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val suomiFiKäyttäjä = luovutuspalveluKäyttäjä.copy(firstname = "Suomi", lastname = "Fi", oid="1.2.246.562.24.99999988889")
  val tilastokeskusKäyttäjä = MockUser("Tilastokeskus", "Teppo", "1.2.246.562.24.78787878787", Set(KäyttöoikeusViranomainen(List(Palvelurooli(TILASTOKESKUS), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT), Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val valviraKäyttäjä = MockUser("Valvira", "Ville", "1.2.246.562.24.42042042040", Set(KäyttöoikeusViranomainen(List(Palvelurooli(VALVIRA)))))

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
    helsinginKaupunkiEsiopetus,
    stadinAmmattiopistoPääkäyttäjä,
    stadinAmmattiopistoTallentaja,
    stadinAmmattiopistoKatselija,
    jyväskylänKatselijaEsiopetus,
    jyväskylänKatselijaEiLuottamuksellinen,
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
    kelaSuppeatOikeudet,
    kelaLaajatOikeudet,
    perusopetusViranomainen,
    toinenAsteViranomainen,
    korkeakouluViranomainen,
    luovutuspalveluKäyttäjä,
    luovutuspalveluKäyttäjäArkaluontoinen,
    suomiFiKäyttäjä,
    tilastokeskusKäyttäjä,
    valviraKäyttäjä
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

