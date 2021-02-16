package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.MockKäyttöoikeusryhmät
import fi.oph.koski.koskiuser.MockUser
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.valpas.valpasuser.ValpasMockKäyttöoikeusryhmät._

object ValpasMockUsers {
  var mockUsersEnabled = false

  val valpasHelsinki = MockUser(
    "käyttäjä",
    "valpas-helsinki",
    "1.2.246.562.24.12312312301",
    Set(kuntakäyttäjä(helsinginKaupunki)))

  val valpasJklNormaalikoulu = MockUser(
    "käyttäjä",
    "valpas-jkl-normaali",
    "1.2.246.562.24.12312312302",
    Set(oppilaitoskäyttäjä(jyväskylänNormaalikoulu)))

  val valpasJklNormaalikouluJaKoskiHelsinkiTallentaja = MockUser(
    "käyttäjä",
    "valpas-jkl-normaali-koski-hki",
    "1.2.246.562.24.12312312303",
    Set(oppilaitoskäyttäjä(jyväskylänNormaalikoulu), MockKäyttöoikeusryhmät.oppilaitosTallentaja(helsinginKaupunki))
  )

  def users = {
    mockUsersEnabled match {
      case true => List(valpasHelsinki, valpasJklNormaalikoulu, valpasJklNormaalikouluJaKoskiHelsinkiTallentaja)
      case false => List()
    }
  }
}

