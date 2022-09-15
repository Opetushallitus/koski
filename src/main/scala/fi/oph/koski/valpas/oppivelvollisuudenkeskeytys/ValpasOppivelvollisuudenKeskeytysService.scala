package fi.oph.koski.valpas.oppivelvollisuudenkeskeytys

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.util.UuidUtils
import fi.oph.koski.valpas.db.ValpasSchema
import fi.oph.koski.valpas.db.ValpasSchema.OppivelvollisuudenKeskeytyshistoriaRow
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppijaLaajatTiedot
import fi.oph.koski.valpas.valpasrepository.{OppivelvollisuudenKeskeytyksenMuutos, UusiOppivelvollisuudenKeskeytys, ValpasOppivelvollisuudenKeskeytys}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.koski.valpas.oppija.{ValpasAccessResolver, ValpasErrorCategory}

import java.util.UUID

class ValpasOppivelvollisuudenKeskeytysService(
  application: KoskiApplication
) extends Logging {
  private val ovKeskeytysRepositoryService = application.valpasOppivelvollisuudenKeskeytysRepositoryService
  private val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService

  private val accessResolver = new ValpasAccessResolver

  def addOppivelvollisuudenKeskeytys
    (keskeytys: UusiOppivelvollisuudenKeskeytys)
      (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppivelvollisuudenKeskeytys] = {
    val haeMyösVainOppijanumerorekisterissäOleva = accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)

    for {
      saaTehdäIlmoituksen             <- accessResolver.assertAccessToOrg(ValpasRooli.KUNTA, keskeytys.tekijäOrganisaatioOid)
      oppija                          <- oppijaLaajatTiedotService.getOppijaLaajatTiedot(keskeytys.oppijaOid, haeMyösVainOppijanumerorekisterissäOleva)
      oppijaJonkaOvVoidaanKeskeyttää  <- validateKeskeytettäväOppija(oppija)
      ovKeskeytys                     <- ovKeskeytysRepositoryService.create(keskeytys)
    } yield ovKeskeytys
  }

  def updateOppivelvollisuudenKeskeytys
    (muutos: OppivelvollisuudenKeskeytyksenMuutos)
      (implicit session: ValpasSession)
  : Either[HttpStatus, (ValpasSchema.OppivelvollisuudenKeskeytysRow, ValpasOppivelvollisuudenKeskeytys)] = {
    val haeMyösVainOppijanumerorekisterissäOleva = accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)

    UuidUtils.optionFromString(muutos.id)
      .toRight(ValpasErrorCategory.badRequest.validation.epävalidiUuid())
      .flatMap(uuid => ovKeskeytysRepositoryService.getLaajatTiedot(uuid).toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia()))
      .flatMap(keskeytys => {
        accessResolver
          .assertAccessToOrg(ValpasRooli.KUNTA, keskeytys.tekijäOrganisaatioOid)
          .flatMap(_ => oppijaLaajatTiedotService.getOppijaLaajatTiedot(keskeytys.oppijaOid, haeMyösVainOppijanumerorekisterissäOleva))
          .flatMap(accessResolver.withOppijaAccess(_))
          .flatMap(_ => ovKeskeytysRepositoryService.update(muutos))
          .map(_ => (keskeytys, ovKeskeytysRepositoryService.getSuppeatTiedot(UUID.fromString(muutos.id)).get))
      })
  }

  def deleteOppivelvollisuudenKeskeytys
    (uuid: UUID)
      (implicit session: ValpasSession)
  : Either[HttpStatus, (ValpasSchema.OppivelvollisuudenKeskeytysRow, ValpasOppivelvollisuudenKeskeytys)] = {
    val haeMyösVainOppijanumerorekisterissäOleva = accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)

    ovKeskeytysRepositoryService.getLaajatTiedot(uuid)
      .toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
      .flatMap(keskeytys => {
        accessResolver
          .assertAccessToOrg(ValpasRooli.KUNTA, keskeytys.tekijäOrganisaatioOid)
          .flatMap(_ => oppijaLaajatTiedotService.getOppijaLaajatTiedot(keskeytys.oppijaOid, haeMyösVainOppijanumerorekisterissäOleva))
          .flatMap(accessResolver.withOppijaAccess(_))
          .flatMap(_ => ovKeskeytysRepositoryService.delete(uuid))
          .map(k => (keskeytys, ovKeskeytysRepositoryService.getSuppeatTiedot(UUID.fromString(k.id)).get))
      })
  }

  def getOppivelvollisuudenKeskeytyksenMuutoshistoria
    (uuid: UUID)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppivelvollisuudenKeskeytyshistoriaRow]] = {
    val haeMyösVainOppijanumerorekisterissäOleva = accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)

    val ovKeskeytyshistoria = ovKeskeytysRepositoryService.getMuutoshistoria(uuid)
    ovKeskeytyshistoria
      .headOption
      .toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
      .map(_.oppijaOid)
      .flatMap(o => oppijaLaajatTiedotService.getOppijaLaajatTiedot(o, haeMyösVainOppijanumerorekisterissäOleva)) // Tarkasta oikeus katsoa oppijan tietoja getOppijaLaajatTiedot avulla
      .map(_ => ovKeskeytyshistoria)
  }

  private def validateKeskeytettäväOppija(oppija: ValpasOppijaLaajatTiedot): Either[HttpStatus, ValpasOppijaLaajatTiedot] =
    if (oppija.oppivelvollisuudestaVapautettu.isDefined) {
      Left(ValpasErrorCategory.badRequest.validation.oppivelvollisuudenKeskeytyksenKohde("Oppivelvollisuuden keskeytystä ei voi kirjata henkilölle, joka on vapautettu oppivelvollisuudesta."))
    } else {
      Right(oppija)
    }
}
