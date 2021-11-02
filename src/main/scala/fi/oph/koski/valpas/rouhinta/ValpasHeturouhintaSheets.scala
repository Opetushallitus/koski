package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.util.Futures

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

case class ValpasHeturouhintaSheets(data: HeturouhinnanTulos, t: LocalizationReader) extends GlobalExecutionContext {
  def build(): Seq[DataSheet] = {
    val future = for {
      eiOppivelvollisuuttaSuorittavat <- eiOppivelvollisuuttaSuorittavatDataSheet(data.eiOppivelvollisuuttaSuorittavat)
      oppivelvollisuuttaSuorittavat <- oppivelvollisuuttaSuorittavatDataSheet(data.oppivelvollisuuttaSuorittavat)
      onrUlkopuoliset <- oppijanumerorekisterinUlkopuolisetDataSheet(data.oppijanumerorekisterinUlkopuoliset)
      ovlUlkopuoliset <- oppivelvollisuudenUlkopuolisetDataSheet(data.oppivelvollisuudenUlkopuoliset)
      virheelliset    <- virheellisetHetutDataSheet(data.virheellisetHetut)
    } yield Seq(
      eiOppivelvollisuuttaSuorittavat,
      oppivelvollisuuttaSuorittavat,
      onrUlkopuoliset,
      ovlUlkopuoliset,
      virheelliset,
    )

    Futures.await(future, atMost = 1.minutes)
  }

  private def eiOppivelvollisuuttaSuorittavatDataSheet(löytyneet: Seq[RouhintaOppivelvollinen]): Future[DataSheet] = {
    Future {
      DataSheet(
        title = t.get("rouhinta_tab_ei_oppivelvollisuutta_suorittavat"),
        rows = löytyneet.map(ValpasRouhintaOppivelvollinenSheetRow.apply(t)),
        columnSettings = ValpasRouhintaOppivelvollinenSheetRow.columnSettings(t),
      )
    }
  }

  private def oppivelvollisuuttaSuorittavatDataSheet =
    pelkkäHetuDataSheet("rouhinta_tab_oppivelvollisuutta_suorittavat")(_)

  private def oppijanumerorekisterinUlkopuolisetDataSheet =
    pelkkäHetuDataSheet("rouhinta_tab_onr_ulkopuoliset")(_)

  private def oppivelvollisuudenUlkopuolisetDataSheet =
    pelkkäHetuDataSheet("rouhinta_tab_ovl_ulkopuoliset")(_)

  private def virheellisetHetutDataSheet =
    pelkkäHetuDataSheet("rouhinta_tab_virheelliset_hetut")(_)

  private def pelkkäHetuDataSheet(titleKey: String)(hetut: Seq[RouhintaPelkkäHetu]): Future[DataSheet] = {
    Future {
      DataSheet(
        title = t.get(titleKey),
        rows = hetut.map(ValpasRouhintaPelkkäHetuSheetRow.apply),
        columnSettings = ValpasRouhintaPelkkäHetuSheetRow.columnSettings(t),
      )
    }
  }
}

case class ValpasRouhintaPelkkäHetuSheetRow(
  hetu: String
) {
  def toSeq: Seq[Any] = this.productIterator.toList
}

object ValpasRouhintaPelkkäHetuSheetRow {
  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "hetu" -> Column(t.get("rouhinta_hetu")),
  )

  def apply(tiedot: RouhintaPelkkäHetu): ValpasRouhintaPelkkäHetuSheetRow = {
    ValpasRouhintaPelkkäHetuSheetRow(hetu = tiedot.hetu)
  }
}

