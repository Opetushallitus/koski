package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.henkilo._
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Rooli}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import org.json4s.JValue

import java.sql.Timestamp
import java.time.LocalDateTime

class PostgresYtrOpiskeluoikeusRepository(
  val db: DB,
  val actions: PostgresYtrOpiskeluoikeusRepositoryActions
) extends YtrSavedOpiskeluoikeusRepository with DatabaseExecutionContext with QueryMethods with Logging {
  override def findByOppijaOids(oids: List[String])(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus] = {
    actions.findByOppijaOids(oids)
  }

  override def createOrUpdate(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus
  )(implicit user: KoskiSpecificSession): Either[HttpStatus, CreateOrUpdateResult] = {
    actions.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate = true, allowDeleteCompleted = true)
  }

  override def createOrUpdateAlkuperäinenYTRJson(oppijaOid: String, data: JValue)(implicit user: KoskiSpecificSession): HttpStatus = {
    if (!user.hasTallennetutYlioppilastutkinnonOpiskeluoikeudetAccess) {
      throw new RuntimeException(s"Ei oikeuksia tallentaa alkuperäistä jsonia")
    }

    runDbSync(
      KoskiTables.YtrAlkuperäinenData.insertOrUpdate(
        YtrAlkuperäinenDataRow(
          oppijaOid,
          Timestamp.valueOf(LocalDateTime.now),
          data
        )
      )
    )

    HttpStatus.ok
  }

  override def findAlkuperäinenYTRJsonByOppijaOid(oppijaOid: String)(implicit user: KoskiSpecificSession): Option[JValue] = {
    if (!(user.hasGlobalReadAccess && user.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)))) {
      throw new RuntimeException(s"Ei oikeuksia hakea alkuperäistä jsonia")
    }

    runDbSync(
      KoskiTables.YtrAlkuperäinenData.filter(_.oppijaOid === oppijaOid).result
    ).headOption.map(_.data)
  }
}
