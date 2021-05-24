package fi.oph.koski.valpas.valpasuser

import java.net.InetAddress

import fi.oph.koski.koskiuser.AuthenticationUser.fromDirectoryUser
import fi.oph.koski.koskiuser.{AuthenticationUser, Käyttöoikeus, KäyttöoikeusRepository, MockKäyttöoikeusryhmät, MockUser}
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.userdirectory.DirectoryUser
import fi.oph.koski.valpas.valpasuser.ValpasMockKäyttöoikeusryhmät._

object ValpasMockUsers {
  var mockUsersEnabled = false

  val valpasOphPääkäyttäjä = ValpasMockUser(
    "pääkäyttäjä",
    "valpas-pää",
    "1.2.246.562.24.12312312300",
    pääkäyttäjä
  )

  val valpasOphHakeutuminenPääkäyttäjä = ValpasMockUser(
    "pääkäyttäjä",
    "valpas-pää-hakeutuminen",
    "1.2.246.562.24.12312312200",
    pääkäyttäjä
  )

  val valpasHelsinki = ValpasMockUser(
    "käyttäjä",
    "valpas-helsinki",
    "1.2.246.562.24.12312312301",
    kuntakäyttäjä(helsinginKaupunki)
  )

  val valpasHelsinkiJaAapajoenPeruskoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-helsinki-aapajoen-peruskoulu",
    "1.2.246.562.24.12312312375",
    kuntakäyttäjä(helsinginKaupunki) ++ peruskoulunKäyttäjä(aapajoenKoulu)
  )

  val valpasPyhtääJaAapajoenPeruskoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-pyhtää",
    "1.2.246.562.24.12312312399",
    kuntakäyttäjä(pyhtäänKunta) ++ peruskoulunKäyttäjä(aapajoenKoulu)
  )

  val valpasHelsinkiPeruskoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-helsinki-peruskoulu",
    "1.2.246.562.24.12312312666",
    peruskoulunKäyttäjä(helsinginKaupunki)
  )

  val valpasJklNormaalikoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-jkl-normaali",
    "1.2.246.562.24.12312312302",
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu) ++ toisenAsteenKäyttäjä(jyväskylänNormaalikoulu)
  )

  val valpasAapajoenKoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-aapajoen-koulu",
    "1.2.246.562.24.12312312309",
    peruskoulunJossa10LuokkaKäyttäjä(aapajoenKoulu) ++ toisenAsteenKäyttäjä(aapajoenKoulu)
  )

  val valpasJklYliopisto = ValpasMockUser(
    "käyttäjä",
    "valpas-jkl-yliopisto",
    "1.2.246.562.24.12312317302",
    peruskoulunKäyttäjä(jyväskylänYliopisto)
  )

  val valpasJklNormaalikouluJaKoskiHelsinkiTallentaja = ValpasMockUser(
    "käyttäjä",
    "valpas-jkl-normaali-koski-hki",
    "1.2.246.562.24.12312312303",
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu) ++ toisenAsteenKäyttäjä(jyväskylänNormaalikoulu) ++ Set(MockKäyttöoikeusryhmät.oppilaitosTallentaja(helsinginKaupunki))
  )

  val valpasJklNormaalikouluJaValpasHelsinki = ValpasMockUser(
    "käyttäjä",
    "valpas-jkl-normaali-hki",
    "1.2.246.562.24.12312312304",
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu) ++ toisenAsteenKäyttäjä(jyväskylänNormaalikoulu) ++ kuntakäyttäjä(helsinginKaupunki)
  )

  val valpasKulosaariPeruskoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-kulosaari",
    "1.2.246.562.24.12312312777",
    peruskoulunKäyttäjä(kulosaarenAlaAste)
  )

  val valpasUseampiPeruskoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-useampi-peruskoulu",
    "1.2.246.562.24.12315312323",
    peruskoulunKäyttäjä(jyväskylänNormaalikoulu) ++ peruskoulunKäyttäjä(kulosaarenAlaAste)
  )

  val valpasSaksalainenKoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-saksalainen",
    "1.2.246.562.24.12312312858",
    peruskoulunKäyttäjä(saksalainenKoulu)
  )

  val valpasPelkkäMaksuttomuuskäyttäjä = ValpasMockUser(
    "käyttäjä",
    "valpas-pelkkä-maksuttomuus",
    "1.2.246.562.24.12315312647",
    oppilaitosKäyttäjäPelkkäMaksuttomuus(jyväskylänNormaalikoulu)
  )

  val valpasMaksuttomuusJaHelsinkiKäyttäjä = ValpasMockUser(
    "käyttäjä",
    "valpas-maksuttomuus-hki",
    "1.2.246.562.24.12315312754",
    oppilaitosKäyttäjäPelkkäMaksuttomuus(jyväskylänNormaalikoulu) ++ kuntakäyttäjä(helsinginKaupunki)
  )

  val valpasMaksuttomuusJaHelsinkiKoskiKäyttäjä = ValpasMockUser(
    "käyttäjä",
    "valpas-maksuttomuus-koski-hki",
    "1.2.246.562.24.12315312759",
    oppilaitosKäyttäjäPelkkäMaksuttomuus(jyväskylänNormaalikoulu) ++ Set(MockKäyttöoikeusryhmät.oppilaitosTallentaja(helsinginKaupunki))
  )

  def users: List[ValpasMockUser] = {
    if (mockUsersEnabled) {
      List(
        valpasOphPääkäyttäjä,
        valpasOphHakeutuminenPääkäyttäjä,
        valpasHelsinki,
        valpasHelsinkiJaAapajoenPeruskoulu,
        valpasPyhtääJaAapajoenPeruskoulu,
        valpasHelsinkiPeruskoulu,
        valpasJklNormaalikoulu,
        valpasAapajoenKoulu,
        valpasJklYliopisto,
        valpasJklNormaalikouluJaKoskiHelsinkiTallentaja,
        valpasJklNormaalikouluJaValpasHelsinki,
        valpasKulosaariPeruskoulu,
        valpasUseampiPeruskoulu,
        valpasSaksalainenKoulu,
        valpasPelkkäMaksuttomuuskäyttäjä,
        valpasMaksuttomuusJaHelsinkiKäyttäjä,
        valpasMaksuttomuusJaHelsinkiKoskiKäyttäjä
      )
    } else {
      List()
    }
  }
}

case class ValpasMockUser(
  lastname: String,
  firstname: String,
  oid: String,
  käyttöoikeudet: Set[Käyttöoikeus],
  lang: String = "fi",
  käyttöoikeusRyhmät: List[String] = Nil
) extends MockUser {
  lazy val ldapUser = DirectoryUser(oid, käyttöoikeudet.toList, firstname, lastname, Some(lang))

  def toValpasSession(käyttöoikeudet: KäyttöoikeusRepository): ValpasSession = {
    val authUser: AuthenticationUser = fromDirectoryUser(username, ldapUser)
    new ValpasSession(authUser, "fi", InetAddress.getByName("192.168.0.10"), "", käyttöoikeudet.käyttäjänKäyttöoikeudet(authUser))
  }
}
