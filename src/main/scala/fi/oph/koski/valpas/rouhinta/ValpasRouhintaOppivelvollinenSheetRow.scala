package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.util.ChainingSyntax.stringOps
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.Column
import fi.oph.koski.schema.Koodistokoodiviite

case class ValpasRouhintaOppivelvollinenSheetRow(
  sukunimi: String,
  etunimet: String,
  oppijaOid: String,
  hetu: Option[String],
  ooPäättymispäivä: String,
  ooViimeisinTila: Option[String],
  ooKoulutusmuoto: Option[String],
  ooToimipiste: Option[String],
  keskeytys: String,
  kuntailmoitusKohde: Option[String],
  kuntailmoitusPvm: Option[String]
) {
  def toSeq: Seq[Any] = this.productIterator.toList
}

object ValpasRouhintaOppivelvollinenSheetRow {
  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "sukunimi" -> Column(t.get("rouhinta_sukunimi")),
    "etunimet" -> Column(t.get("rouhinta_etunimet")),
    "oppijaOid" -> Column(t.get("rouhinta_oppijanumero")),
    "hetu" -> Column(t.get("rouhinta_hetu")),
    "ooPäättymispäivä" -> Column(
      t.get("rouhinta_oo_päättymispäivä").autowrap(40),
      comment = Some(t.get("rouhinta_oo_päättymispäivä_comment"))
    ),
    "ooViimeisinTila" -> Column(
      t.get("rouhinta_viimeisin_tila").autowrap(40),
      comment = Some(t.get("rouhinta_viimeisin_tila_comment"))
    ),
    "ooKoulutusmuoto" -> Column(t.get("rouhinta_koulutusmuoto"), comment = Some(t.get("rouhinta_koulutusmuoto_comment"))),
    "ooToimipiste" -> Column(t.get("rouhinta_toimipiste"), comment = Some(t.get("rouhinta_toimipiste_comment"))),
    "keskeytys" -> Column(t.get("rouhinta_ov_keskeytys"), comment = Some(t.get("rouhinta_ov_keskeytys_comment"))),
    "kuntailmoitusKohde" -> Column(t.get("rouhinta_kuntailmoitus_kohde"), comment = Some(t.get("rouhinta_kuntailmoitus_kohde_comment"))),
    "kuntailmoitusPvm" -> Column(t.get("rouhinta_kuntailmoitus_pvm"), comment = Some(t.get("rouhinta_kuntailmoitus_pvm_comment"))),
  )

  def apply(t: LocalizationReader)(tiedot: ValpasRouhintaOppivelvollinen): ValpasRouhintaOppivelvollinenSheetRow = {
    val oo = tiedot.viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus

    val kuntailmoitusKohde =
      tiedot.aktiivinenKuntailmoitus
        .flatMap(_.kunta.kotipaikka.map(kp => t.fromKoodiviite("rouhunta_tieto_puuttuu")(kp)))
    val kuntailmoitusPvm =
      tiedot.aktiivinenKuntailmoitus
        .flatMap(_.aikaleima.map(al => t.format(al.toLocalDate)))

    ValpasRouhintaOppivelvollinenSheetRow(
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
      kuntailmoitusKohde = kuntailmoitusKohde,
      kuntailmoitusPvm = kuntailmoitusPvm
    )
  }

  def suorituksenTyyppiToKoulutustyyppi(tyyppi: Koodistokoodiviite, t: LocalizationReader): String = {
    tyyppi.koodiarvo match {
      case "valma" => t.get("koulutustyyppi_valma")
      case "telma" => t.get("koulutustyyppi_telma")
      case "vst" => t.get("koulutustyyppi_vst")
      case s: String if s.startsWith("ib") || s.startsWith("preib") => t.get("koulutustyyppi_ib")
      case s: String if s.startsWith("internationalschool") => t.get("koulutustyyppi_internationalschool")
      // TODO: TOR-1685 Eurooppalainen koulu
      case s: String if s.startsWith("dia") => t.get("koulutustyyppi_dia")
      case "perusopetuksenvuosiluokka" => t.get("koulutustyyppi_perusopetus")
      case s: String if s.startsWith("aikuistenperusopetuksen") => t.get("koulutustyyppi_aikuistenperusopetus")
      case _ => tyyppi.nimi.map(t.from).getOrElse(tyyppi.koodiarvo)
    }
  }
}
