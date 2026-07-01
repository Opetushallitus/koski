package fi.oph.koski.massaluovutus.suorituspalvelu

import fi.oph.koski.json.GenericJsonFormats
import fi.oph.koski.koskiuser.{KoskiSpecificSession, MockUsers}
import fi.oph.koski.log.{AuditLogTester, LogConfiguration}
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.json4s.Formats
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SuorituspalveluQueryAuditLogSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private implicit val formats: Formats = GenericJsonFormats.genericFormats
  private lazy val käyttöoikeuspalvelu = KoskiApplicationForTests.käyttöoikeusRepository
  private implicit lazy val session: KoskiSpecificSession = MockUsers.paakayttaja.toKoskiSpecificSession(käyttöoikeuspalvelu)

  private val oppijaOid = "1.2.246.562.24.00000000001"

  // Riittävän monta oidia, jotta yhteen audit-viestiin pakattuna ylitettäisiin LogConfiguration.logMessageMaxLength.
  private def oidit(n: Int): Seq[String] = (1 to n).map(i => f"1.2.246.562.15.$i%011d")

  private def supaViestit: Seq[(String, JObject)] =
    AuditLogTester.getLogMessages
      .map(raw => (raw, parse(raw)))
      .collect { case (raw, o: JObject) => (raw, o) }
      .filter { case (_, o) => o.values.get("operation").contains("SUORITUSPALVELU_OPISKELUOIKEUS_HAKU") }

  private def loggatutOidit(viesti: JObject): Seq[String] =
    (viesti \ "target" \ "opiskeluoikeusOid") match {
      case JString(oidit) => oidit.split(",").filter(_.nonEmpty).toSeq
      case _ => Nil
    }

  "SuorituspalveluQuery.auditLog" - {
    "Pilkkoo oidit useaan audit-viestiin eikä heitä poikkeusta, vaikka oppijalla olisi valtava määrä opiskeluoikeuksia" in {
      AuditLogTester.clearMessages()
      val kaikkiOidit = oidit(500)

      noException should be thrownBy SuorituspalveluQuery.auditLog(oppijaOid, kaikkiOidit)

      val viestit = supaViestit
      withClue("oidit tulee pilkkoa useaan viestiin") {
        viestit.size should be > 1
      }
      withClue("jokaisen viestin tulee pysyä audit-lokin pituusrajan alla") {
        viestit.foreach { case (raw, _) => raw.length should be <= LogConfiguration.logMessageMaxLength }
      }
      withClue("kaikkien oidien tulee tallentua lokiin, eikä yksikään saa kadota") {
        viestit.flatMap { case (_, o) => loggatutOidit(o) }.toSet should equal(kaikkiOidit.toSet)
      }
    }

    "Kirjaa yhden viestin ilman opiskeluoikeus-oideja, kun oideja ei ole" in {
      AuditLogTester.clearMessages()
      SuorituspalveluQuery.auditLog(oppijaOid, Nil)

      val viestit = supaViestit
      viestit.size should equal(1)
      loggatutOidit(viestit.head._2) should be(empty)
    }
  }
}
