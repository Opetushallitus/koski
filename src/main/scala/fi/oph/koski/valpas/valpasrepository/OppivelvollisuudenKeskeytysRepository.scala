package fi.oph.koski.valpas.valpasrepository

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.util.UuidUtils
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.db.ValpasDatabase
import fi.oph.koski.valpas.db.ValpasSchema.{OppivelvollisuudenKeskeytys, OppivelvollisuudenKeskeytysRow, OppivelvollisuudenKeskeytyshistoria, OppivelvollisuudenKeskeytyshistoriaRow}

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

class OppivelvollisuudenKeskeytysRepository(database: ValpasDatabase, config: Config) extends QueryMethods with Logging {
  protected val db: DB = database.db

  def getKeskeytys(id: UUID): Option[OppivelvollisuudenKeskeytysRow] = {
    runDbSync(
      OppivelvollisuudenKeskeytys
        .filter(_.uuid === id)
        .result
    ).headOption
  }

  def getKeskeytykset(oppijaOids: Seq[String]): Seq[OppivelvollisuudenKeskeytysRow] = {
    runDbSync(
      OppivelvollisuudenKeskeytys
        .filter(_.oppijaOid inSetBind oppijaOids)
        .filter(_.peruttu =!= true)
        .sortBy(_.luotu.desc)
        .result
    )
  }

  def setKeskeytys(row: OppivelvollisuudenKeskeytysRow): Either[HttpStatus, OppivelvollisuudenKeskeytysRow] = {
    runDbSync(
      OppivelvollisuudenKeskeytys
        .returning(OppivelvollisuudenKeskeytys)
        .insertOrUpdate(row)
    ).toRight(ValpasErrorCategory.internalError())
  }

  def updateKeskeytys(keskeytys: OppivelvollisuudenKeskeytyksenMuutos): Either[HttpStatus, OppivelvollisuudenKeskeytysRow] = {
    UuidUtils.optionFromString(keskeytys.id)
      .toRight(ValpasErrorCategory.badRequest.validation.epävalidiUuid())
      .flatMap(uuid => {
        val query = for { l <- OppivelvollisuudenKeskeytys if l.uuid === uuid } yield (l.alku, l.loppu)
        runDbSync(query.update(keskeytys.alku, keskeytys.loppu)) match {
          case 0 => Left(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
          case _ => getKeskeytys(uuid).toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
        }
      })
  }

  def deleteKeskeytys(uuid: UUID): Either[HttpStatus, OppivelvollisuudenKeskeytysRow] = {
    val query = for { l <- OppivelvollisuudenKeskeytys if l.uuid === uuid } yield l.peruttu
    runDbSync(query.update(true)) match {
      case 0 => Left(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
      case _ => getKeskeytys(uuid).toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
    }
  }

  def addToHistory(tekijäOid: String)(row: OppivelvollisuudenKeskeytysRow): Unit = {
    runDbSync(OppivelvollisuudenKeskeytyshistoria += OppivelvollisuudenKeskeytyshistoriaRow(
      ovKeskeytysUuid = row.uuid,
      muutosTehty = LocalDateTime.now(),
      muutoksenTekijä = tekijäOid,
      oppijaOid = row.oppijaOid,
      alku = row.alku,
      loppu = row.loppu,
      luotu = row.luotu,
      tekijäOid = row.tekijäOid,
      tekijäOrganisaatioOid = row.tekijäOrganisaatioOid,
      peruttu = row.peruttu,
    ))
  }

  def getHistory(ovKeskeytysUuid: UUID): Seq[OppivelvollisuudenKeskeytyshistoriaRow] = {
    runDbSync(OppivelvollisuudenKeskeytyshistoria
      .filter(_.ovKeskeytysUuid === ovKeskeytysUuid)
      .sortBy(_.muutosTehty)
      .result
    )
  }

  def truncate(): Unit = {
    if (Environment.isMockEnvironment(config)) {
      runDbSync(OppivelvollisuudenKeskeytyshistoria.delete)
      runDbSync(OppivelvollisuudenKeskeytys.delete)
    } else {
      throw new RuntimeException("Oppivelvollisuuden keskeytyksiä ei voi tyhjentää tuotantotilassa")
    }
  }
}

case class OppivelvollisuudenKeskeytyksenMuutos(
  id: String,
  alku: LocalDate,
  loppu: Option[LocalDate], // Jos None --> voimassa toistaiseksi
)
