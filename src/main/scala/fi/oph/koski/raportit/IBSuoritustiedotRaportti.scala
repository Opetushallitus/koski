package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.YleissivistäväUtils.{lengthInDaysInDateRange, removeContinuousSameTila}
import fi.oph.koski.schema._

case class IBSuoritustiedotRaportti(repository: IBSuoritustiedotRaporttiRepository, t: LocalizationReader) extends GlobalExecutionContext {

  def build(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean,
    raportinTyyppi: IBSuoritustiedotRaporttiType
  )(implicit u: KoskiSpecificSession): DataSheet = {
    val rows = repository
      .suoritustiedot(oppilaitosOid, alku, loppu, osasuoritustenAikarajaus, raportinTyyppi.päätasonSuoritusTyyppi)
    DataSheet(
      title = t.get("raportti-excel-suoritukset-sheet-name"),
      rows = rows.map(r => kaikkiOppiaineetVälilehtiRow(r, Seq.empty, alku, loppu)),
      columnSettings = columnSettings(t)
    )
  }

  private def kaikkiOppiaineetVälilehtiRow(row: IBRaporttiRows, oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val lisätiedot = JsonSerializer.extract[Option[LukionOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")

      IBRaporttiRow(
        opiskeluoikeusOid = row.opiskeluoikeus.opiskeluoikeusOid,
        lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
        koulutustoimijaNimi = if(t.language == "sv") row.opiskeluoikeus.koulutustoimijaNimiSv else row.opiskeluoikeus.koulutustoimijaNimi,
        oppilaitoksenNimi = if(t.language == "sv") row.opiskeluoikeus.oppilaitosNimiSv else row.opiskeluoikeus.oppilaitosNimi,
        toimipisteNimi =  if(t.language == "sv") row.päätasonSuoritus.toimipisteNimiSv else row.päätasonSuoritus.toimipisteNimi,
        lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
        aikaleima = row.opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
        yksiloity = row.henkilo.yksiloity,
        oppijaOid = row.opiskeluoikeus.oppijaOid,
        hetu = row.henkilo.hetu,
        sukunimi = row.henkilo.sukunimi,
        etunimet = row.henkilo.etunimet,
        opiskeluoikeudenAlkamispäivä = row.opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
        opiskeluoikeudenViimeisinTila = row.opiskeluoikeus.viimeisinTila,
        opiskeluoikeudenTilatAikajaksonAikana = removeContinuousSameTila(row.aikajaksot).map(_.tila).mkString(", "),
        päätasonSuoritukset = row.päätasonSuoritus.koulutusModuulistaKäytettäväNimi(t.language),
        opiskeluoikeudenPäättymispäivä = row.opiskeluoikeus.päättymispäivä.map(_.toLocalDate),
        rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(", "),
        maksuttomuus = lisätiedot.flatMap(_.maksuttomuus.map(ms => ms.filter(m => m.maksuton && m.overlaps(Aikajakso(alku, Some(loppu)))).map(_.toString).mkString(", "))).filter(_.nonEmpty),
        oikeuttaMaksuttomuuteenPidennetty = lisätiedot.flatMap(_.oikeuttaMaksuttomuuteenPidennetty.map(omps => omps.map(_.toString).mkString(", "))).filter(_.nonEmpty),
        pidennettyPäättymispäivä = lisätiedot.exists(_.pidennettyPäättymispäivä),
        ulkomainenVaihtoOpiskelija = lisätiedot.exists(_.ulkomainenVaihtoopiskelija),
        erityinenKoulutustehtäväJaksot = lisätiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
        ulkomaanjaksot = lisätiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
        sisäoppilaitosmainenMajoitus = lisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
      )
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "lähdejärjestelmä" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
    "koulutustoimijaNimi" -> Column(t.get("raportti-excel-kolumni-koulutustoimijaNimi")),
    "oppilaitoksenNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "toimipisteNimi" -> Column(t.get("raportti-excel-kolumni-toimipisteNimi")),
    "lähdejärjestelmänId" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
    "aikaleima" -> Column(t.get("raportti-excel-kolumni-päivitetty"), comment = Some(t.get("raportti-excel-kolumni-päivitetty-comment"))),
    "yksiloity" -> Column(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
    "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
    "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
    "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    "opiskeluoikeudenViimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-viimeisinTila-comment"))),
    "opiskeluoikeudenTilatAikajaksonAikana" -> Column(t.get("raportti-excel-kolumni-kaikkiTilat"), comment = Some(t.get("raportti-excel-kolumni-kaikkiTilat-comment"))),
    "päätasonSuoritukset" -> Column(t.get("raportti-excel-kolumni-koulutusmoduuliNimet")),
    "opiskeluoikeudenPäättymispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenPäättymispäivä")),
    "rahoitukset" -> Column(t.get("raportti-excel-kolumni-rahoitukset"), comment = Some(t.get("raportti-excel-kolumni-rahoitukset-comment"))),
    "maksuttomuus" -> Column(t.get("raportti-excel-kolumni-maksuttomuus"), comment = Some(t.get("raportti-excel-kolumni-maksuttomuus-comment"))),
    "oikeuttaMaksuttomuuteenPidennetty" -> Column(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty"), comment = Some(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty-comment"))),
    "pidennettyPäättymispäivä" -> Column(t.get("raportti-excel-kolumni-pidennettyPäättymispäivä"), comment = Some(t.get("raportti-excel-kolumni-pidennettyPäättymispäivä-lukio-comment"))),
    "ulkomainenVaihtoOpiskelija" -> Column(t.get("raportti-excel-kolumni-ulkomainenVaihtoOpiskelija"), comment = Some(t.get("raportti-excel-kolumni-ulkomainenVaihtoOpiskelija-lukio-comment"))),
    "erityinenKoulutustehtäväJaksot" -> Column(t.get("raportti-excel-kolumni-erityinenKoulutustehtäväJaksot"), comment = Some(t.get("raportti-excel-kolumni-erityinenKoulutustehtäväJaksot-comment"))),
    "ulkomaanjaksot" -> Column(t.get("raportti-excel-kolumni-ulkomaanjaksot"), comment = Some(t.get("raportti-excel-kolumni-ulkomaanjaksot-comment"))),
    "sisäoppilaitosmainenMajoitus" -> Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-count-comment"))),
  )
}

case class IBRaporttiRow(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  koulutustoimijaNimi: String,
  oppilaitoksenNimi: String,
  toimipisteNimi: String,
  lähdejärjestelmänId: Option[String],
  aikaleima: LocalDate,
  yksiloity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  opiskeluoikeudenViimeisinTila: Option[String],
  opiskeluoikeudenTilatAikajaksonAikana: String,
  päätasonSuoritukset: Option[String],
  opiskeluoikeudenPäättymispäivä: Option[LocalDate],
  rahoitukset: String,
  maksuttomuus: Option[String],
  oikeuttaMaksuttomuuteenPidennetty: Option[String],
  pidennettyPäättymispäivä: Boolean,
  ulkomainenVaihtoOpiskelija: Boolean,
  erityinenKoulutustehtäväJaksot: Option[Int],
  ulkomaanjaksot: Option[Int],
  sisäoppilaitosmainenMajoitus: Option[Int],
)


sealed trait IBSuoritustiedotRaporttiType {
  def typeName: String
  def päätasonSuoritusTyyppi: String
}

object IBTutkinnonSuoritusRaportti extends IBSuoritustiedotRaporttiType {
  val typeName = "ibtutkinto"
  val päätasonSuoritusTyyppi: String = "ibtutkinto"
}

object PreIBSuoritusRaportti extends IBSuoritustiedotRaporttiType {
  val typeName = "preiboppimaara"
  val päätasonSuoritusTyyppi: String = "preiboppimaara" //TODO preib ja preib2019 ?
}
