package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.MockKäyttöoikeusryhmät
import fi.oph.koski.koskiuser.MockUser
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.valpas.valpasuser.ValpasMockKäyttöoikeusryhmät._

object ValpasMockUsers {
  var mockUsersEnabled = false

  val valpasOphPääkäyttäjä = MockUser(
    "pääkäyttäjä",
    "valpas-pää",
    "1.2.246.562.24.12312312300",
    pääkäyttäjä
  )

  val valpasHelsinki = MockUser(
    "käyttäjä",
    "valpas-helsinki",
    "1.2.246.562.24.12312312301",
    kuntakäyttäjä(helsinginKaupunki)
  )

  val valpasJklNormaalikoulu = MockUser(
    "käyttäjä",
    "valpas-jkl-normaali",
    "1.2.246.562.24.12312312302",
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu) ++ toisenAsteenKäyttäjä(jyväskylänNormaalikoulu)
  )

  val valpasJklNormaalikouluJaKoskiHelsinkiTallentaja = MockUser(
    "käyttäjä",
    "valpas-jkl-normaali-koski-hki",
    "1.2.246.562.24.12312312303",
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu) ++ toisenAsteenKäyttäjä(jyväskylänNormaalikoulu) ++ Set(MockKäyttöoikeusryhmät.oppilaitosTallentaja(helsinginKaupunki))
  )

  val valpasJklNormaalikouluJaValpasHelsinki = MockUser(
    "käyttäjä",
    "valpas-jkl-normaali-hki",
    "1.2.246.562.24.12312312304",
    peruskoulunJossa10LuokkaKäyttäjä(jyväskylänNormaalikoulu) ++ toisenAsteenKäyttäjä(jyväskylänNormaalikoulu) ++ kuntakäyttäjä(helsinginKaupunki)
  )


  def users = {
    mockUsersEnabled match {
      case true => List(valpasOphPääkäyttäjä, valpasHelsinki, valpasJklNormaalikoulu, valpasJklNormaalikouluJaKoskiHelsinkiTallentaja, valpasJklNormaalikouluJaValpasHelsinki)
      case false => List()
    }
  }
}

