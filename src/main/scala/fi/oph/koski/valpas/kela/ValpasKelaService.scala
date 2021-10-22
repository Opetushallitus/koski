package fi.oph.koski.valpas.kela

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log._
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.db.ValpasSchema.OppivelvollisuudenKeskeytysRow
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppivelvollisuustiedotRow

class ValpasKelaService(application: KoskiApplication) extends Logging {
  def findValpasKelaOppijaByHetu(hetu: String): Either[HttpStatus, ValpasKelaOppija] = {
    application.valpasOpiskeluoikeusDatabaseService.getOppivelvollisuusTiedot(hetu)
      .headOption
      .map(asValpasKelaOppijaWithOppivelvollisuudenKeskeytykset)
      .toRight(notFound("(hetu)"))
  }

  private def asValpasKelaOppijaWithOppivelvollisuudenKeskeytykset(oppija: ValpasOppivelvollisuustiedotRow): ValpasKelaOppija = {
    val keskeytykset = application.valpasOppivelvollisuudenKeskeytysRepository.getKeskeytykset(oppija.kaikkiOppijaOidit)
    asValpasKelaOppija(oppija, keskeytykset)
  }

  private def asValpasKelaOppija(
    dbRow: ValpasOppivelvollisuustiedotRow,
    keskeytykset: Seq[OppivelvollisuudenKeskeytysRow]
  ): ValpasKelaOppija =
  {
    ValpasKelaOppija(
      henkilö = ValpasKelaHenkilö(
        oid = dbRow.oppijaOid,
        hetu = dbRow.hetu,
        oppivelvollisuusVoimassaAsti = dbRow.oppivelvollisuusVoimassaAsti,
        oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = Some(dbRow.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti)
      ),
      oppivelvollisuudenKeskeytykset = keskeytykset.map(asValpasKelaOppivelvollisuudenKeskeytys)
    )
  }

  private def asValpasKelaOppivelvollisuudenKeskeytys(
    keskeytys: OppivelvollisuudenKeskeytysRow
  ): ValpasKelaOppivelvollisuudenKeskeytys = ValpasKelaOppivelvollisuudenKeskeytys(
    uuid = keskeytys.uuid.toString,
    alku = keskeytys.alku,
    loppu = keskeytys.loppu,
    luotu = keskeytys.luotu,
    peruttu = keskeytys.peruttu
  )

  private def notFound(tunniste: String): HttpStatus = ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(
    "Oppijaa " + tunniste + " ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."
  )
}
