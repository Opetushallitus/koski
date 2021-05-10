package fi.oph.koski.koskiuser

import java.net.InetAddress

import fi.oph.koski.koskiuser.AuthenticationUser.fromDirectoryUser
import fi.oph.koski.koskiuser.MockKäyttöoikeusryhmät._
import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.schema.OidOrganisaatio
import fi.oph.koski.userdirectory.DirectoryUser

object MockUsers {
  private def ilmanLuottamuksellisiaTietoja(orgOid: String) = {
    oppilaitosTallentaja(orgOid).copy(organisaatiokohtaisetPalveluroolit = oppilaitosTallentaja(orgOid).organisaatiokohtaisetPalveluroolit.filterNot(_.rooli == Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  }

  val kalle = KoskiMockUser("käyttäjä", "kalle", "1.2.246.562.24.99999999987", (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja).toSet)
  val pärre = KoskiMockUser("käyttäjä", "pärre", "1.2.246.562.24.99999999901", (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja).toSet, "sv")
  val omniaPalvelukäyttäjä = KoskiMockUser("käyttäjä", "omnia-palvelukäyttäjä", "1.2.246.562.24.99999999989", Set(oppilaitosPalvelukäyttäjä(omnia)))
  val omniaKatselija = KoskiMockUser("käyttäjä", "omnia-katselija", "1.2.246.562.24.99999999990", Set(oppilaitosKatselija(omnia)))
  val omniaTallentaja = KoskiMockUser("käyttäjä", "omnia-tallentaja", "1.2.246.562.24.99999999991", Set(oppilaitosTallentaja(omnia)))
  val omniaPääkäyttäjä = KoskiMockUser("omnia-pää", "omnia-pää", "1.2.246.562.24.99999977777", Set(oppilaitosPääkäyttäjä(MockOrganisaatiot.omnia)))
  val tallentajaEiLuottamuksellinen = KoskiMockUser("epäluotettava-tallentaja", "epäluotettava-tallentaja", "1.2.246.562.24.99999999997", Set(ilmanLuottamuksellisiaTietoja(omnia), ilmanLuottamuksellisiaTietoja(jyväskylänNormaalikoulu)))
  val paakayttajaMitatoidytOpiskeluoikeudet = KoskiMockUser("käyttäjä", "mikko", "1.2.246.562.24.99999999987", Set(ophPääkäyttäjä, KäyttöoikeusGlobal(List(Palvelurooli(MITATOIDYT_OPISKELUOIKEUDET)))))
  val paakayttaja = KoskiMockUser("käyttäjä", "pää", "1.2.246.562.24.99999999992", Set(ophPääkäyttäjä, localizationAdmin, KäyttöoikeusGlobal(List(Palvelurooli("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA")))))
  val viranomainen = KoskiMockUser("käyttäjä", "viranomais", "1.2.246.562.24.99999999993", Set(viranomaisKatselija))
  val helsinginKaupunkiPalvelukäyttäjä = KoskiMockUser("stadin-palvelu", "stadin-palvelu", "1.2.246.562.24.99999999994", Set(oppilaitosPalvelukäyttäjä(helsinginKaupunki)))
  val helsinginKaupunkiEsiopetus = KoskiMockUser("stadin-esiopetus", "stadin-esiopetus", "1.2.246.562.24.99999999944", Set(oppilaitosEsiopetusKatselija(helsinginKaupunki)))
  val stadinAmmattiopistoTallentaja = KoskiMockUser("tallentaja", "tallentaja", "1.2.246.562.24.99999999995", Set(oppilaitosTallentaja(MockOrganisaatiot.stadinAmmattiopisto)))
  val stadinAmmattiopistoKatselija = KoskiMockUser("katselija", "katselija", "1.2.246.562.24.99999999985", Set(oppilaitosKatselija(MockOrganisaatiot.stadinAmmattiopisto)))
  val jyväskylänKatselijaEsiopetus = KoskiMockUser("esiopetus", "esiopetus", "1.2.246.562.24.99999999666", Set(oppilaitosEsiopetusKatselija(MockOrganisaatiot.jyväskylänNormaalikoulu)))
  val jyväskylänKatselijaEiLuottamuksellinen = KoskiMockUser("jyvas-eiluottoa", "jyvas-eiluottoa", "1.2.246.562.24.99999999888", Set(ilmanLuottamuksellisiaTietoja(MockOrganisaatiot.jyväskylänNormaalikoulu)))
  val stadinAmmattiopistoPääkäyttäjä = KoskiMockUser("stadinammattiopisto-admin", "stadinammattiopisto-admin", "1.2.246.562.24.99999999986", Set(oppilaitosPääkäyttäjä(MockOrganisaatiot.stadinAmmattiopisto)), "fi", List("koski-oppilaitos-pääkäyttäjä_1494486198456"))
  val stadinVastuukäyttäjä = KoskiMockUser("stadin-vastuu", "stadin-vastuu", "1.2.246.562.24.99999999996", Set(vastuukäyttäjä(helsinginKaupunki)))
  val stadinPääkäyttäjä = KoskiMockUser("stadin-pää", "stadin-pää", "1.2.246.562.24.99999999997", Set(oppilaitosPääkäyttäjä(helsinginKaupunki)), "fi", List("koski-oppilaitos-pääkäyttäjä_1494486198456"))

  val helsinkiTallentaja = KoskiMockUser("hki-tallentaja", "hki-tallentaja", "1.2.246.562.24.99999999977", Set(oppilaitosTallentaja(helsinginKaupunki)))
  val tornioTallentaja = KoskiMockUser("tornio-tallentaja", "tornio-tallentaja", "1.2.246.562.24.99999999988", Set(oppilaitosTallentaja(tornionKaupunki)))
  val helsinkiSekäTornioTallentaja = KoskiMockUser("helsinki-tornio-tallentaja", "helsinki-tornio-tallentaja", "1.2.246.562.24.99999999922", Set(oppilaitosTallentaja(helsinginKaupunki), oppilaitosTallentaja(tornionKaupunki)))
  val pyhtäänTallentaja = KoskiMockUser("pyhtaa-tallentaja", "pyhtaa-tallentaja", "1.2.246.562.24.99999999966", Set(oppilaitosTallentaja(pyhtäänKunta)))
  val jyväskyläTallentaja = KoskiMockUser("jyvaskyla-tallentaja", "jyvaskyla-tallentaja", "1.2.246.562.24.99999999955", Set(oppilaitosTallentaja(jyväskylänYliopisto)))
  val touholaTallentaja = KoskiMockUser("touhola-tallentaja", "touhola-tallentaja", "1.2.246.562.24.99999999933", Set(oppilaitosTallentaja(päiväkotiTouhula)))
  val majakkaTallentaja = KoskiMockUser("majakka-tallentaja", "majakka-tallentaja", "1.2.246.562.24.99999999911", Set(oppilaitosTallentaja(päiväkotiMajakka)))

  val kahdenOrganisaatioPalvelukäyttäjä = KoskiMockUser("palvelu2", "palvelu2", "1.2.246.562.24.99999999998", Set(oppilaitosPalvelukäyttäjä(helsinginKaupunki), oppilaitosPalvelukäyttäjä(MockOrganisaatiot.omnia)))
  val omattiedot = KoskiMockUser("Oppija", "Oili", "1.2.246.562.24.99999999999", Set(oppilaitosTallentaja(omnia)))
  val eiOikkia = KoskiMockUser("EiOikkia", "Otto", "1.2.246.562.24.99999999902", Set(KäyttöoikeusOrg(OidOrganisaatio(lehtikuusentienToimipiste), List(Palvelurooli("OPPIJANUMEROREKISTERI", READ)), juuri = true, oppilaitostyyppi = None)))
  val evira = KoskiMockUser("Evira", "Eeva", "1.2.246.562.24.99999999111", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val kelaSuppeatOikeudet = KoskiMockUser("Kela", "Suppea", "1.2.246.562.24.88888888111", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU), Palvelurooli(LUOTTAMUKSELLINEN_KELA_SUPPEA)))))
  val kelaLaajatOikeudet = KoskiMockUser("Kela", "Laaja", "1.2.246.562.24.88888888222", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU), Palvelurooli(LUOTTAMUKSELLINEN_KELA_LAAJA)))))
  val perusopetusViranomainen = KoskiMockUser("Perusopetus", "Pertti", "1.2.246.562.24.99999999222", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS)))))
  val toinenAsteViranomainen = KoskiMockUser("Toinenaste", "Teuvo", "1.2.246.562.24.99999999333", Set(KäyttöoikeusGlobal(List(Palvelurooli("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA"))), KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE)))))
  val korkeakouluViranomainen = KoskiMockUser("Korkeakoulu", "Kaisa", "1.2.246.562.24.99999999444", Set(KäyttöoikeusViranomainen(List(Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val jyväskylänNormaalikoulunPalvelukäyttäjä = KoskiMockUser("jyväs-palvelu", "jyväs-palvelu", "1.2.246.562.24.99999999777", Set(oppilaitosPalvelukäyttäjä(MockOrganisaatiot.jyväskylänNormaalikoulu)))
  val jyväskylänYliopistonVastuukäyttäjä = KoskiMockUser("jyväs-vastuu", "jyväs-vastuu", "1.2.246.562.24.99999997777", Set(vastuukäyttäjä(MockOrganisaatiot.jyväskylänYliopisto)), "fi", List("Vastuukayttajat"))
  val luovutuspalveluKäyttäjä = KoskiMockUser("Luovutus", "Lasse", "1.2.246.562.24.99999988888", Set(KäyttöoikeusViranomainen(List(Palvelurooli(TIEDONSIIRTO_LUOVUTUSPALVELU), Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val luovutuspalveluKäyttäjäArkaluontoinen = KoskiMockUser("Arkaluontoinen", "Antti", "1.2.246.562.24.88888877777", Set(KäyttöoikeusViranomainen(List(Palvelurooli(TIEDONSIIRTO_LUOVUTUSPALVELU), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT), Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val suomiFiKäyttäjä = luovutuspalveluKäyttäjä.copy(firstname = "Suomi", lastname = "Fi", oid="1.2.246.562.24.99999988889")
  val tilastokeskusKäyttäjä = KoskiMockUser("Tilastokeskus", "Teppo", "1.2.246.562.24.78787878787", Set(KäyttöoikeusViranomainen(List(Palvelurooli(TILASTOKESKUS), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT), Palvelurooli(GLOBAALI_LUKU_PERUSOPETUS),Palvelurooli(GLOBAALI_LUKU_TOINEN_ASTE), Palvelurooli(GLOBAALI_LUKU_KORKEAKOULU)))))
  val valviraKäyttäjä = KoskiMockUser("Valvira", "Ville", "1.2.246.562.24.42042042040", Set(KäyttöoikeusViranomainen(List(Palvelurooli(VALVIRA)))))
  val esiopetusTallentaja = KoskiMockUser("esiopetus-tallentaja", "esiopetus-tallentaja", "1.2.246.562.24.42042042041", Set(KäyttöoikeusOrg(OidOrganisaatio(helsinginKaupunki), List(Palvelurooli(READ_UPDATE_ESIOPETUS), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), true, None)))
  val perusopetusTallentaja =  KoskiMockUser("esiopetus-tallentaja", "esiopetus-tallentaja", "1.2.246.562.24.42042042042", Set(KäyttöoikeusOrg(OidOrganisaatio(jyväskylänNormaalikoulu), List(Palvelurooli(PERUSOPETUS), Palvelurooli(READ_UPDATE), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)), true, None)))
  val oppivelvollisuutietoRajapinta = KoskiMockUser("oppivelvollisuustieto-rajapinnan-kutsuja", "oppivelvollisuustieto-rajapinnan-kutsuja", "1.2.246.562.24.42042042444", Set(KäyttöoikeusGlobal(List(Palvelurooli(OPPIVELVOLLISUUSTIETO_RAJAPINTA)))))

  val users = List(
    kalle,
    pärre,
    omniaPalvelukäyttäjä,
    omniaKatselija,
    omniaTallentaja,
    omniaPääkäyttäjä,
    paakayttaja,
    paakayttajaMitatoidytOpiskeluoikeudet,
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
    helsinkiTallentaja,
    tornioTallentaja,
    helsinkiSekäTornioTallentaja,
    pyhtäänTallentaja,
    jyväskyläTallentaja,
    touholaTallentaja,
    majakkaTallentaja,
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
    valviraKäyttäjä,
    esiopetusTallentaja,
    oppivelvollisuutietoRajapinta
  )
}

trait MockUser extends UserWithPassword {
  def lastname: String
  def firstname: String
  def oid: String
  def käyttöoikeudet: Set[Käyttöoikeus]
  def lang: String
  def käyttöoikeusRyhmät: List[String]

  def ldapUser: DirectoryUser

  def username = ldapUser.etunimet
  def password = username
}

case class KoskiMockUser(lastname: String, firstname: String, oid: String, käyttöoikeudet: Set[Käyttöoikeus], lang: String = "fi", käyttöoikeusRyhmät: List[String] = Nil) extends MockUser {
  lazy val ldapUser = DirectoryUser(oid, käyttöoikeudet.toList, firstname, lastname, Some(lang))
  def toKoskiSpecificSession(käyttöoikeudet: KäyttöoikeusRepository): KoskiSpecificSession = {
    val authUser: AuthenticationUser = fromDirectoryUser(username, ldapUser)
    new KoskiSpecificSession(authUser, "fi", InetAddress.getByName("192.168.0.10"), "", käyttöoikeudet.käyttäjänKäyttöoikeudet(authUser))
  }
}
