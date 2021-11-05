package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.DataSheet
import fi.oph.koski.util.Futures

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

case class ValpasKuntarouhintaSheets(data: KuntarouhinnanTulos, t: LocalizationReader) extends GlobalExecutionContext {
  def build(): Seq[DataSheet] = {
    val future = for {
      eiOppivelvollisuuttaSuorittavat <- eiOppivelvollisuuttaSuorittavatDataSheet(data.eiOppivelvollisuuttaSuorittavat)
    } yield Seq(
      eiOppivelvollisuuttaSuorittavat,
    )

    Futures.await(future, atMost = 1.minutes)
  }

  private def eiOppivelvollisuuttaSuorittavatDataSheet(löytyneet: Seq[ValpasRouhintaOppivelvollinen]): Future[DataSheet] = {
    Future {
      DataSheet(
        title = t.get("rouhinta_tab_ei_oppivelvollisuutta_suorittavat"),
        rows = löytyneet.map(ValpasRouhintaOppivelvollinenSheetRow.apply(t)),
        columnSettings = ValpasRouhintaOppivelvollinenSheetRow.columnSettings(t),
      )
    }
  }
}
