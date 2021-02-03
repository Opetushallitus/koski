package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.MockUser
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.valpas.valpasuser.ValpasMockKäyttöoikeusryhmät._

object ValpasMockUsers {
  var mockUsersEnabled = false

  val valpasHelsinki = MockUser(
    "käyttäjä",
    "valpas-helsinki",
    "1.2.246.562.24.99999999587",
    Set(kuntakäyttäjä(helsinginKaupunki)))

  val valpasJklNormaalikoulu = MockUser(
    "käyttäjä",
    "valpas-jkl-normaali",
    "1.2.246.562.24.99999999487",
    Set(oppilaitoskäyttäjä(jyväskylänNormaalikoulu)))

  def users = {
    mockUsersEnabled match {
      case true => List(valpasHelsinki, valpasJklNormaalikoulu)
      case false => List()
    }
  }
}

