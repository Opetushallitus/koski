package fi.oph.koski.valpas.kela

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log._
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.db.ValpasSchema.OppivelvollisuudenKeskeytysRow
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppivelvollisuustiedotRow

class ValpasKelaService(application: KoskiApplication) extends Logging with Timing {
  def findValpasKelaOppijatByHetut(hetut: Seq[String]): Either[HttpStatus, Seq[ValpasKelaOppija]] = {

    val timedBlockName = hetut.length match {
      case 1 => "findValpasKelaOppijatByHetut1"
      case n if n <= 100 => "findValpasKelaOppijatByHetut2To100"
      case n if n <= 500 => "findValpasKelaOppijatByHetut101To500"
      case _ => "findValpasKelaOppijatByHetut501To1000"
    }

    timed(timedBlockName, 10) {
      val oppivelvollisuusTiedot = application.valpasOpiskeluoikeusDatabaseService.getOppivelvollisuusTiedot(hetut)
      Right(asValpasKelaOppijatWithOppivelvollisuudenKeskeytykset(oppivelvollisuusTiedot))
    }
  }

  private def asValpasKelaOppijatWithOppivelvollisuudenKeskeytykset(oppijat: Seq[ValpasOppivelvollisuustiedotRow]): Seq[ValpasKelaOppija] = {
    val keskeytykset: Map[String, Seq[OppivelvollisuudenKeskeytysRow]] =
      application.valpasOppivelvollisuudenKeskeytysRepository
        .getKeskeytykset(oppijat.flatMap(_.kaikkiOppijaOidit))
        .groupBy(_.oppijaOid)
        .withDefaultValue(Seq.empty)

    oppijat.map(oppija => {
      val oppijanKeskeytykset =
        oppija.kaikkiOppijaOidit
          .map(keskeytykset)
          .flatten

      asValpasKelaOppija(oppija, oppijanKeskeytykset)
    })
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
}
