package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.util.Futures

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

case class ValpasHeturouhintaSheets(data: HeturouhinnanTulos, t: LocalizationReader) extends GlobalExecutionContext {
  def build(): Seq[DataSheet] = {
    val future = for {
      oppivelvolliset <- oppivelvollisetDataSheet(data.oppivelvolliset)
      onrUlkopuoliset <- oppijanumerorekisterinUlkopuolisetDataSheet(data.oppijanumerorekisterinUlkopuoliset)
      ovlUlkopuoliset <- oppivelvollisuudenUlkopuolisetDataSheet(data.oppivelvollisuudenUlkopuoliset)
      virheelliset    <- virheellisetHetutDataSheet(data.virheellisetHetut)
    } yield Seq(
      oppivelvolliset,
      onrUlkopuoliset,
      ovlUlkopuoliset,
      virheelliset,
    )

    Futures.await(future, atMost = 1.minutes)
  }

  private def oppivelvollisetDataSheet(löytyneet: Seq[RouhintaOppivelvollinen]): Future[DataSheet] = {
    Future {
      DataSheet(
        title = t.get("rouhinta_tab_oppivelvolliset"),
        rows = löytyneet.map(OppivelvollinenRow.apply(t)),
        columnSettings = OppivelvollinenRow.columnSettings(t),
      )
    }
  }

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
        rows = hetut.map(PelkkäHetuRow.apply),
        columnSettings = PelkkäHetuRow.columnSettings(t),
      )
    }
  }
}

case class OppivelvollinenRow(
  sukunimi: String,
  etunimet: String,
  oppijaOid: String,
  hetu: Option[String],
  ooPäättymispäivä: String,
  ooViimeisinTila: Option[String],
  ooKoulutusmuoto: Option[String],
  ooToimipiste: Option[String],
  keskeytys: String,
) {
  def toSeq: Seq[Any] = this.productIterator.toList
}

object OppivelvollinenRow {
  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "sukunimi" -> Column(t.get("rouhinta_sukunimi")),
    "etunimet" -> Column(t.get("rouhinta_etunimet")),
    "oppijaOid" -> Column(t.get("rouhinta_oppijanumero")),
    "hetu" -> Column(t.get("rouhinta_hetu")),
    "ooPäättymispäivä" -> Column(t.get("rouhinta_oo_päättymispäivä"), comment = Some(t.get("rouhinta_oo_päättymispäivä_comment"))),
    "ooViimeisinTila" -> Column(t.get("rouhinta_viimeisin_tila"), comment = Some(t.get("rouhinta_viimeisin_tila_comment"))),
    "ooKoulutusmuoto" -> Column(t.get("rouhinta_koulutusmuoto"), comment = Some(t.get("rouhinta_koulutusmuoto_comment"))),
    "ooToimipiste" -> Column(t.get("rouhinta_toimipiste"), comment = Some(t.get("rouhinta_toimipiste_comment"))),
    "keskeytys" -> Column(t.get("rouhinta_ov_keskeytys"), comment = Some(t.get("rouhinta_ov_keskeytys_comment"))),
  )

  def apply(t: LocalizationReader)(tiedot: RouhintaOppivelvollinen): OppivelvollinenRow = {
    val oo = tiedot.viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus

    OppivelvollinenRow(
      sukunimi = tiedot.sukunimi,
      etunimet = tiedot.etunimet,
      oppijaOid = tiedot.oppijanumero,
      hetu = tiedot.hetu,
      ooPäättymispäivä = oo match {
        case Some(oo) => oo.päättymispäivä.map(t.formatDateString).getOrElse(t.get("rouhinta_ei_päättynyt"))
        case None => t.get("rouhinta_ei_opiskeluoikeutta")
      },
      ooViimeisinTila = oo.map(_.viimeisinTila).map(t.fromKoodiviite("rouhunta_tieto_puuttuu")),
      ooKoulutusmuoto = oo.map(o => suorituksenTyyppiToKoulutustyyppi(o.suorituksenTyyppi, t)),
      ooToimipiste = oo.map(_.toimipiste).map(t.from),
      keskeytys = tiedot.oppivelvollisuudenKeskeytys
        .map(keskeytys => t.format(keskeytys.alku) + " - " + keskeytys.loppu.map(t.format).getOrElse(""))
        .map(_.trim)
        .mkString(", "),
    )
  }

  def suorituksenTyyppiToKoulutustyyppi(tyyppi: Koodistokoodiviite, t: LocalizationReader): String = {
    tyyppi.koodiarvo match {
      case "valma" => t.get("koulutustyyppi_valma")
      case "telma" => t.get("koulutustyyppi_telma")
      case "vst" => t.get("koulutustyyppi_vst")
      case s: String if s.startsWith("ib") || s.startsWith("preib") => t.get("koulutustyyppi_ib")
      case s: String if s.startsWith("internationalschool") => t.get("koulutustyyppi_internationalschool")
      case s: String if s.startsWith("dia") => t.get("koulutustyyppi_dia")
      case "perusopetuksenvuosiluokka" => t.get("koulutustyyppi_perusopetus")
      case s: String if s.startsWith("aikuistenperusopetuksen") => t.get("koulutustyyppi_aikuistenperusopetus")
      case _ => tyyppi.nimi.map(t.from).getOrElse(tyyppi.koodiarvo)
    }
  }
}

case class PelkkäHetuRow(
  hetu: String
) {
  def toSeq: Seq[Any] = this.productIterator.toList
}

object PelkkäHetuRow {
  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "hetu" -> Column(t.get("rouhinta_hetu")),
  )

  def apply(tiedot: RouhintaPelkkäHetu): PelkkäHetuRow = {
    PelkkäHetuRow(hetu = tiedot.hetu)
  }
}

