package fi.oph.koski.massaluovutus

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{KoskiOpiskeluoikeusRow, KoskiTables, SQLHelpers}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Session}
import fi.oph.koski.log.LoggerWithContext
import fi.oph.koski.organisaatio.OrganisaationAlaisetOrganisaatiot
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema}
import fi.oph.koski.util.TryWithLogging
import slick.jdbc.SQLActionBuilder

import java.security.SecureRandom
import scala.util.Using

object MassaluovutusUtils {
  def readDatabaseId(config: Config): String = config.getString("kyselyt.readDatabase")
  def concurrency(config: Config): Int = config.getInt("kyselyt.concurrency")

  def defaultOrganisaatio(implicit user: Session): Either[HttpStatus, Oid] = user match {
    case koskiUser: KoskiSpecificSession =>
      val organisaatiot = koskiUser.juuriOrganisaatiot
      if (organisaatiot.isEmpty) {
        Left(KoskiErrorCategory.forbidden.organisaatio())
      } else if (organisaatiot.size > 1) {
        Left(KoskiErrorCategory.badRequest.massaluovutus.eiYksiselitteinenOrganisaatio())
      } else {
        Right(koskiUser.juuriOrganisaatiot.head.oid)
      }
    case _ =>
      // TODO: toteuta ValpasSessiolle jos tarvitaan
      Left(KoskiErrorCategory.forbidden.organisaatio())
  }

  def QueryResourceManager(logger: LoggerWithContext)(op: Using.Manager => Unit): Either[String, Unit] =
    TryWithLogging.andResources(logger, op).left.map(_.getMessage)

  // Oman hierarkian opiskeluoikeudet ovat piirissä sellaisenaan, mutta ostopalveluyksiköistä
  // vain ostavan koulutustoimijan omat. Ilman koulutustoimijaehtoa kyselyyn osuisivat yksikön
  // kaikki opiskeluoikeudet, myös sen oman koulutustoimijan ja muiden ostajien, joihin ei ole
  // lukuoikeutta — vrt. KoskiSpecificSession.hasVarhaiskasvatusAccess.
  def organisaatioFilter(organisaatiot: OrganisaationAlaisetOrganisaatiot): SQLActionBuilder =
    SQLHelpers.concatMany(
      Some(sql" (oppilaitos_oid = ANY(${organisaatiot.hierarkia}) "),
      organisaatiot.koulutustoimija
        .filter(_ => organisaatiot.ostopalvelu.nonEmpty)
        .map(kt => sql" OR (oppilaitos_oid = ANY(${organisaatiot.ostopalvelu}) AND koulutustoimija_oid = $kt) "),
      Some(sql") "),
    )

  def toKoskeenTallennettavaOpiskeluoikeus(application: KoskiApplication, logger: LoggerWithContext)(row: KoskiOpiskeluoikeusRow): KoskeenTallennettavaOpiskeluoikeus = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.lenientDeserializationWithoutValidation)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) => oo
      case Left(errors) =>
        logger.warn(s"Error deserializing oppijan ${row.oppijaOid} opiskeluoikeus ${row.oid}: ${errors}")
        throw new MassaluovutusException(s"Oppijan ${row.oppijaOid} opiskeluoikeuden ${row.oid} deserialisointi epäonnistui")
    }
  }

  def generatePassword(length: Int): String = {
    val alphanumericChars = ('0' to '9') ++ ('A' to 'Z') ++ ('a' to 'z')
    val random = new SecureRandom()
    Iterator
      .continually(alphanumericChars(random.nextInt(alphanumericChars.length)))
      .take(length)
      .mkString
  }
}

object  QueryFormat {
  val json = "application/json"
  val csv = "text/csv"
  val csvPartition = "text/x-csv-partition"
  val xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
}

class MassaluovutusException(message: String) extends Exception(message)
