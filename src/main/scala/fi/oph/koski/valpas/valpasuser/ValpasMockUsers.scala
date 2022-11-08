package fi.oph.koski.valpas.valpasuser

import java.net.InetAddress
import fi.oph.koski.koskiuser.AuthenticationUser.fromDirectoryUser
import fi.oph.koski.koskiuser.{AuthenticationUser, KäyttöoikeusRepository, MockKäyttöoikeusryhmät, MockUser}
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.userdirectory.{DirectoryClient, DirectoryUser, HenkilönKäyttöoikeudet, OrganisaatioJaKäyttöoikeudet}
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
    hakeutuminenPääkäyttäjä
  )

  val valpasHelsinki = ValpasMockUser(
    "käyttäjä",
    "valpas-helsinki",
    "1.2.246.562.24.12312312301",
    kuntakäyttäjä(helsinginKaupunki)
  )

  val valpasTornio = ValpasMockUser(
    "käyttäjä",
    "valpas-tornio",
    "1.2.246.562.24.4444444444",
    kuntakäyttäjä(tornionKaupunki)
  )

  val valpasHelsinkiJaAapajoenPeruskoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-helsinki-aapajoen-peruskoulu",
    "1.2.246.562.24.12312312375",
    kuntakäyttäjä(helsinginKaupunki)
      ++ peruskoulunKäyttäjä(aapajoenKoulu)
  )

  val valpasPyhtääJaAapajoenPeruskoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-pyhtää",
    "1.2.246.562.24.12312312399",
    kuntakäyttäjä(pyhtäänKunta)
      ++ peruskoulunKäyttäjä(aapajoenKoulu)
  )

  val valpasUseitaKuntia = ValpasMockUser(
    "käyttäjä",
    "valpas-useita-kuntia",
    "1.2.246.562.24.12312312369",
    kuntakäyttäjä(pyhtäänKunta)
      ++ kuntakäyttäjä(helsinginKaupunki)
      ++ kuntakäyttäjä(lakkautettuKunta)
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
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu)
      ++ toisenAsteenKäyttäjä(jyväskylänNormaalikoulu)
  )

  val valpasJklNormaalikouluPelkkäPeruskoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-jkl-normaali-perus",
    "1.2.246.562.24.12312312392",
    peruskoulunKäyttäjä(jyväskylänNormaalikoulu)
  )

  val valpasAapajoenKoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-aapajoen-koulu",
    "1.2.246.562.24.12312312309",
    peruskoulunJossa10LuokkaKäyttäjä(aapajoenKoulu)
      ++ toisenAsteenKäyttäjä(aapajoenKoulu)
      ++ oppilaitosKäyttäjäPelkkäSuorittaminen(aapajoenKoulu)
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
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu)
      ++ toisenAsteenKäyttäjä(jyväskylänNormaalikoulu)
      ++ Set(MockKäyttöoikeusryhmät.oppilaitosTallentaja(helsinginKaupunki))
  )

  val valpasJklNormaalikouluJaValpasHelsinki = ValpasMockUser(
    "käyttäjä",
    "valpas-jkl-normaali-hki",
    "1.2.246.562.24.12312312304",
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu)
      ++ toisenAsteenKäyttäjä(jyväskylänNormaalikoulu)
      ++ kuntakäyttäjä(helsinginKaupunki)
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
    peruskoulunKäyttäjä(jyväskylänNormaalikoulu)
      ++ peruskoulunKäyttäjä(kulosaarenAlaAste)
  )

  val valpasSaksalainenKoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-saksalainen",
    "1.2.246.562.24.12312312858",
    peruskoulunKäyttäjä(saksalainenKoulu)
  )

  val valpasViikinNormaalikoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-viikin-normaalikoulu",
    "1.2.246.562.24.12312312839",
    peruskoulunKäyttäjä(viikinNormaalikoulu)
  )

  val valpasViikinNormaalikouluToinenAste = ValpasMockUser(
    "käyttäjä",
    "valpas-viikin-normaalikoulu-2-aste",
    "1.2.246.562.24.12312312839",
    toisenAsteenKäyttäjä(viikinNormaalikoulu)
  )

  val valpasPelkkäMaksuttomuusKäyttäjä = ValpasMockUser(
    "käyttäjä",
    "valpas-pelkkä-maksuttomuus",
    "1.2.246.562.24.12315312647",
    oppilaitosKäyttäjäPelkkäMaksuttomuus(jyväskylänNormaalikoulu)
  )

  val valpasPelkkäSuorittaminenkäyttäjä = ValpasMockUser(
    "käyttäjä",
    "valpas-pelkkä-suorittaminen",
    "1.2.246.562.24.12315312356",
    oppilaitosKäyttäjäPelkkäSuorittaminen(jyväskylänNormaalikoulu)
      ++ oppilaitosKäyttäjäPelkkäSuorittaminen(helsinginMedialukio)
      ++ oppilaitosKäyttäjäPelkkäSuorittaminen(aapajoenKoulu)
  )

  val valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-pelkkä-suorittaminen-amis",
    "1.2.246.562.24.12315312847",
    oppilaitosKäyttäjäPelkkäSuorittaminen(stadinAmmattiopisto)
  )

  val valpasMaksuttomuusJaHelsinkiKäyttäjä = ValpasMockUser(
    "käyttäjä",
    "valpas-maksuttomuus-hki",
    "1.2.246.562.24.12315312754",
    oppilaitosKäyttäjäPelkkäMaksuttomuus(jyväskylänNormaalikoulu)
      ++ kuntakäyttäjä(helsinginKaupunki)
  )

  val valpasMaksuttomuusJaHelsinkiKoskiKäyttäjä = ValpasMockUser(
    "käyttäjä",
    "valpas-maksuttomuus-koski-hki",
    "1.2.246.562.24.12315312759",
    oppilaitosKäyttäjäPelkkäMaksuttomuus(jyväskylänNormaalikoulu)
      ++ Set(MockKäyttöoikeusryhmät.oppilaitosTallentaja(helsinginKaupunki))
  )

  val valpasAapajoenKouluJaJklNormaalikoulu = ValpasMockUser(
    "käyttäjä",
    "valpas-aapajoen-koulu-jkl-normaali",
    "1.2.246.562.24.12312318582",
    peruskoulunJossa10LuokkaKäyttäjä(aapajoenKoulu)
      ++ toisenAsteenKäyttäjä(aapajoenKoulu)
      ++ peruskoulunKäyttäjä(jyväskylänNormaalikoulu)
  )

  val valpasJklAapajokiHkiPyhtää = ValpasMockUser(
    "käyttäjä",
    "valpas-jkl-aapajoki-hki-pyhtää",
    "1.2.246.562.24.12312312536",
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu) ++
      peruskoulunJossa10LuokkaKäyttäjä(aapajoenKoulu) ++
      kuntakäyttäjä(helsinginKaupunki) ++
      kuntakäyttäjä(pyhtäänKunta)
  )

  val valpasJklYliopistoSuorittaminen = ValpasMockUser(
    "käyttäjä",
    "valpas-jkl-yliopisto-suorittaminen",
    "1.2.246.562.24.12312384682",
    oppilaitosKäyttäjäPelkkäSuorittaminen(jyväskylänYliopisto)
  )

  val valpasHkiSuorittaminen = ValpasMockUser(
    "käyttäjä",
    "valpas-hki-suorittaminen",
    "1.2.246.562.24.12312384683",
    oppilaitosKäyttäjäPelkkäSuorittaminen(helsinginKaupunki)
  )

  val valpasNivelvaiheenKäyttäjä = ValpasMockUser(
    "käyttäjä",
    "valpas-nivelvaihe",
    "1.2.246.562.24.12315318397",
    nivelvaiheenKäyttäjä(jyväskylänNormaalikoulu) ++ nivelvaiheenKäyttäjä(helsinginMedialukio) ++ nivelvaiheenKäyttäjä(varsinaisSuomenKansanopisto)
  )

  val valpasIntSchool = ValpasMockUser(
    "käyttäjä",
    "valpas-int-school",
    "1.2.246.562.24.12312384882",
    peruskoulunJossa10LuokkaKäyttäjä(internationalSchool) ++ toisenAsteenKäyttäjä(internationalSchool)
  )

  val valpasIntSchoolJklHki = ValpasMockUser(
    "käyttäjä",
    "valpas-int-jkl-hki",
    "1.2.246.562.24.12312394717",
    peruskoulunJossa10LuokkaKäyttäjä(internationalSchool) ++ toisenAsteenKäyttäjä(internationalSchool) ++
      nivelvaiheenKäyttäjä(jyväskylänNormaalikoulu) ++ kuntakäyttäjä(helsinginKaupunki)
  )

  val valpasMonta = ValpasMockUser(
    "käyttäjä",
    "valpas-monta",
    "1.2.246.562.24.12312394165",
    peruskoulunJossa10LuokkaKäyttäjä(internationalSchool) ++ toisenAsteenKäyttäjä(internationalSchool) ++
      nivelvaiheenKäyttäjä(jyväskylänNormaalikoulu) ++ kuntakäyttäjä(helsinginKaupunki) ++
      nivelvaiheenKäyttäjä(ressunLukio) ++ kuntakäyttäjä(pyhtäänKunta) ++
      peruskoulunKäyttäjä(kulosaarenAlaAste) ++ nivelvaiheenKäyttäjä(stadinAmmattiopisto) ++
      peruskoulunKäyttäjä(aapajoenKoulu) ++ peruskoulunKäyttäjä(saksalainenKoulu)
  )

  val valpasKela = ValpasMockUser(
    "käyttäjä",
    "valpas-kela",
    "1.2.246.562.24.12312394638",
    kelaLuovutuspalveluKäyttäjä
  )

  val valpasYtl = ValpasMockUser(
    "käyttäjä",
    "valpas-ytl",
    "1.2.246.562.24.15515594655",
    ytlLuovutuspalveluKäyttäjä,
  )

  def users: List[ValpasMockUser] = {
    if (mockUsersEnabled) {
      List(
        valpasOphPääkäyttäjä,
        valpasOphHakeutuminenPääkäyttäjä,
        valpasHelsinki,
        valpasTornio,
        valpasHelsinkiJaAapajoenPeruskoulu,
        valpasPyhtääJaAapajoenPeruskoulu,
        valpasUseitaKuntia,
        valpasHelsinkiPeruskoulu,
        valpasJklNormaalikoulu,
        valpasJklNormaalikouluPelkkäPeruskoulu,
        valpasAapajoenKoulu,
        valpasJklYliopisto,
        valpasJklNormaalikouluJaKoskiHelsinkiTallentaja,
        valpasJklNormaalikouluJaValpasHelsinki,
        valpasKulosaariPeruskoulu,
        valpasUseampiPeruskoulu,
        valpasSaksalainenKoulu,
        valpasViikinNormaalikoulu,
        valpasViikinNormaalikouluToinenAste,
        valpasPelkkäMaksuttomuusKäyttäjä,
        valpasMaksuttomuusJaHelsinkiKäyttäjä,
        valpasMaksuttomuusJaHelsinkiKoskiKäyttäjä,
        valpasPelkkäSuorittaminenkäyttäjä,
        valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu,
        valpasAapajoenKouluJaJklNormaalikoulu,
        valpasJklAapajokiHkiPyhtää,
        valpasJklYliopistoSuorittaminen,
        valpasHkiSuorittaminen,
        valpasNivelvaiheenKäyttäjä,
        valpasIntSchool,
        valpasIntSchoolJklHki,
        valpasMonta,
        valpasKela,
        valpasYtl,
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
  käyttöoikeudetRaw: Seq[OrganisaatioJaKäyttöoikeudet],
  lang: String = "fi",
  käyttöoikeusRyhmät: List[String] = Nil
) extends MockUser {
  val käyttöoikeudet = DirectoryClient.resolveKäyttöoikeudet(HenkilönKäyttöoikeudet(oid, käyttöoikeudetRaw.toList))._2.toSet

  lazy val ldapUser = DirectoryUser(oid, käyttöoikeudet.toList, firstname, lastname, Some(lang))

  def toValpasSession(käyttöoikeudet: KäyttöoikeusRepository): ValpasSession = {
    val authUser: AuthenticationUser = fromDirectoryUser(username, ldapUser)
    new ValpasSession(authUser, "fi", InetAddress.getByName("192.168.0.10"), "", käyttöoikeudet.käyttäjänKäyttöoikeudet(authUser))
  }
}
