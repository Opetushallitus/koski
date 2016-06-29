package fi.oph.koski.api

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.json.Json
import fi.oph.koski.schema._
import org.scalatest.Matchers

trait OpiskeluOikeusTestMethods extends HttpSpecification with Matchers {
  def lastOpiskeluOikeus(oppijaOid: String): KoskeenTallennettavaOpiskeluoikeus = {
    oppija(oppijaOid).tallennettavatOpiskeluoikeudet.last
  }

  def opiskeluoikeudet(oppijaOid: String): Seq[Opiskeluoikeus] = {
    oppija(oppijaOid).opiskeluoikeudet
  }

  def opiskeluoikeus(oppijaOid: String, tyyppi: String) = {
    opiskeluoikeudet(oppijaOid).find(_.tyyppi.koodiarvo == tyyppi).get
  }

  def oppija(oppijaOid: String): Oppija = {
    authGet("api/oppija/" + oppijaOid) {
      verifyResponseStatus(200)
      Json.read[Oppija](body)
    }
  }
}
