package fi.oph.koski.raportointikanta

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.http.HttpTester
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

trait RaportointikantaTestMethods extends HttpTester with LocalJettyHttpSpecification {

  val raportointikantaQueryHelper = RaportointikantaQueryHelper
  val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  def loadRaportointikantaFixtures[A] = {
    authGet("api/raportointikanta/clear") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/opiskeluoikeudet") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/henkilot") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/organisaatiot") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/koodistot") { verifyResponseStatusOk() }
  }
}

object RaportointikantaQueryHelper {

  private val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  def oppijaByOid(oid: String) = {
    raportointiDatabase.runDbSync(raportointiDatabase.RHenkilöt.filter(_.oppijaOid === oid).result)
  }

  def opiskeluoikeusByOppijaOidAndKoulutusmuoto(oid: String, koulutusmuoto: String) = {
    raportointiDatabase.runDbSync(raportointiDatabase.ROpiskeluoikeudet.filter(_.oppijaOid === oid).filter(_.koulutusmuoto === koulutusmuoto).result)
  }
}
