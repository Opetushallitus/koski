package fi.oph.koski.valpas.valpasrepository

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.valpas.db.ValpasSchema.{OppivelvollisuudenKeskeytysRow, OppivelvollisuudenKeskeytyshistoriaRow}
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

class OppivelvollisuudenKeskeytysRepositoryService(application: KoskiApplication) extends Logging {
  protected lazy val db = application.valpasOppivelvollisuudenKeskeytysRepository
  protected lazy val rajapäivät = application.valpasRajapäivätService
  protected lazy val localization = application.valpasLocalizationRepository

  def getKeskeytykset(oppijaOids: Seq[String]): Seq[ValpasOppivelvollisuudenKeskeytys] =
    db.getKeskeytykset(oppijaOids)
      .map(toValpasOppivelvollisuudenKeskeytys)

  def create
    (keskeytys: UusiOppivelvollisuudenKeskeytys)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppivelvollisuudenKeskeytys] = {
    Right(OppivelvollisuudenKeskeytysRow(
      oppijaOid = keskeytys.oppijaOid,
      alku = keskeytys.alku,
      loppu = keskeytys.loppu,
      tekijäOid = session.oid,
      tekijäOrganisaatioOid = keskeytys.tekijäOrganisaatioOid,
      luotu = LocalDateTime.now(),
    ))
      .flatMap(validateRow)
      .flatMap(db.setKeskeytys)
      .tap(db.addToHistory(session.user.oid))
      .map(toValpasOppivelvollisuudenKeskeytys)
  }

  def getSuppeatTiedot(uuid: UUID): Option[ValpasOppivelvollisuudenKeskeytys] = {
    getLaajatTiedot(uuid).map(toValpasOppivelvollisuudenKeskeytys)
  }

  def getLaajatTiedot(uuid: UUID): Option[OppivelvollisuudenKeskeytysRow] = {
    db.getKeskeytys(uuid)
  }

  def update
    (keskeytys: OppivelvollisuudenKeskeytyksenMuutos)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppivelvollisuudenKeskeytys] = {
    Right(keskeytys)
      .flatMap(validateUpdate)
      .flatMap(db.updateKeskeytys)
      .tap(db.addToHistory(session.user.oid))
      .map(toValpasOppivelvollisuudenKeskeytys)
  }

  def delete
    (uuid: UUID)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppivelvollisuudenKeskeytys] = {
    db.deleteKeskeytys(uuid)
      .tap(db.addToHistory(session.user.oid))
      .map(toValpasOppivelvollisuudenKeskeytys)
  }

  def getMuutoshistoria(uuid: UUID): Seq[OppivelvollisuudenKeskeytyshistoriaRow] =
    db.getHistory(uuid)

  def toValpasOppivelvollisuudenKeskeytys(row: OppivelvollisuudenKeskeytysRow): ValpasOppivelvollisuudenKeskeytys =
    ValpasOppivelvollisuudenKeskeytys.apply(rajapäivät.tarkastelupäivä)(row)

  private def isBetween(date: LocalDate)(start: LocalDate, end: Option[LocalDate]): Boolean = {
    date.compareTo(start) >= 0 && end.forall(date.compareTo(_) <= 0)
  }

  private def validateRow
    (row: OppivelvollisuudenKeskeytysRow)
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppivelvollisuudenKeskeytysRow] = {
    getValidationError(row.alku, row.loppu).map(_ => row)
  }

  private def validateUpdate
    (row: OppivelvollisuudenKeskeytyksenMuutos)
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppivelvollisuudenKeskeytyksenMuutos] = {
    getValidationError(row.alku, row.loppu).map(_ => row)
  }

  private def getValidationError
    (alku: LocalDate, loppu: Option[LocalDate])
    (implicit session: ValpasSession)
  : Either[HttpStatus, Unit] = {
    val t = new LocalizationReader(localization, session.lang)
    if (loppu.exists(_.isBefore(alku))) {
      Left(ValpasErrorCategory.badRequest.validation.virheellinenPäivämäärä(t.get("ovkeskeytys__validaatio_alku_ja_loppupäivä")))
    } else if (alku.isBefore(rajapäivät.ilmoitustenEnsimmäinenTallennuspäivä)) {
      Left(ValpasErrorCategory.badRequest.validation.virheellinenPäivämäärä(t.get("ovkeskeytys__validaatio_alkupäivä", Map(
        "pvm" -> rajapäivät.ilmoitustenEnsimmäinenTallennuspäivä.format(finnishDateFormat)
      ))))
    } else {
      Right(Unit)
    }
  }
}

case class ValpasOppivelvollisuudenKeskeytys(
  id: String,
  tekijäOrganisaatioOid: Organisaatio.Oid,
  alku: LocalDate,
  loppu: Option[LocalDate],
  voimassa: Boolean,
  tulevaisuudessa: Boolean,
)

object ValpasOppivelvollisuudenKeskeytys {
  def apply
    (tarkastelupäivä: LocalDate)
    (row: OppivelvollisuudenKeskeytysRow)
  : ValpasOppivelvollisuudenKeskeytys = {
    val tarkastelupäiväAikavälillä = isBetween(tarkastelupäivä) _
    def keskeytysTulevaisuudessa(alku: LocalDate) = isBetween(alku)(tarkastelupäivä.plusDays(1), None)

    ValpasOppivelvollisuudenKeskeytys(
      id = row.uuid.toString,
      tekijäOrganisaatioOid = row.tekijäOrganisaatioOid,
      alku = row.alku,
      loppu = row.loppu,
      voimassa = !row.peruttu && tarkastelupäiväAikavälillä(row.alku, row.loppu),
      tulevaisuudessa = !row.peruttu && keskeytysTulevaisuudessa(row.alku),
    )
  }

  private def isBetween(date: LocalDate)(start: LocalDate, end: Option[LocalDate]): Boolean = {
    date.compareTo(start) >= 0 && end.forall(date.compareTo(_) <= 0)
  }
}

case class UusiOppivelvollisuudenKeskeytys(
  oppijaOid: String,
  alku: LocalDate,
  loppu: Option[LocalDate], // Jos None --> voimassa toistaiseksi
  tekijäOrganisaatioOid: String,
)
