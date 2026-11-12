package fi.oph.koski.raportointikanta

import fi.oph.koski.db.RaportointiDatabaseConfig
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods


trait RaportointikantaTestMethods extends KoskiHttpSpec {
  implicit val formats: DefaultFormats = DefaultFormats

  lazy val mainRaportointiDb: RaportointiDatabase = KoskiApplicationForTests.raportointiDatabase
  lazy val confidentalRaportointiDb: ConfidentialRaportointiDatabase = mainRaportointiDb.confidential.get

  lazy val tempRaportointiDb: RaportointiDatabase = new RaportointiDatabase(
    new RaportointiDatabaseConfig(KoskiApplicationForTests.config, schema = Temp)
  )
  lazy val tempConfidentalRaportointiDb: ConfidentialRaportointiDatabase = tempRaportointiDb.confidential.get

  def reloadRaportointikanta(): Unit = {
    authGet("api/test/raportointikanta/load?force=true&fullReload=true") { verifyResponseStatusOk() }
    Wait.until(loadComplete)
  }

  def updateRaportointikanta(): Unit = {
    authGet("api/test/raportointikanta/load?force=true") { verifyResponseStatusOk() }
    Wait.until(loadComplete)
  }

  def loadComplete = authGet("api/raportointikanta/status") {
    val isComplete = (JsonMethods.parse(body) \ "public" \ "isComplete").extract[Boolean]
    val isLoading = (JsonMethods.parse(body) \ "etl" \ "isLoading").extract[Boolean]
    isComplete && !isLoading
  }
}
