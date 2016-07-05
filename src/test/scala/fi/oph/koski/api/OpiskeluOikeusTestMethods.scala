package fi.oph.koski.api

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema._
import org.scalatest.Matchers

trait OpiskeluOikeusTestMethods extends HttpSpecification with Matchers {
  def lastOpiskeluOikeus(oppijaOid: String, user: UserWithPassword = defaultUser): KoskeenTallennettavaOpiskeluoikeus = {
    oppija(oppijaOid, user).tallennettavatOpiskeluoikeudet.last
  }

  def opiskeluoikeudet(oppijaOid: String, user: UserWithPassword = defaultUser): Seq[Opiskeluoikeus] = {
    oppija(oppijaOid, user).opiskeluoikeudet
  }

  def opiskeluoikeus(oppijaOid: String, tyyppi: String) = {
    opiskeluoikeudet(oppijaOid).find(_.tyyppi.koodiarvo == tyyppi).get
  }

  def oppija(oppijaOid: String, user: UserWithPassword = defaultUser): Oppija = {
    authGet("api/oppija/" + oppijaOid, user) {
      verifyResponseStatus(200)
      Json.read[Oppija](body)
    }
  }
}
