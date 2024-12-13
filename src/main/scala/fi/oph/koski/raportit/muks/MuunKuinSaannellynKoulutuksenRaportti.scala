package fi.oph.koski.raportit.muks

import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.Organisaatio

import java.time.LocalDate

object MuunKuinSaannellynKoulutuksenRaportti {
  def buildRaportti(
    raportointiDatabase: RaportointiDatabase,
    oppilaitosOids: Set[Organisaatio.Oid],
    alku: LocalDate,
    loppu: LocalDate,
    t: LocalizationReader
  ): Seq[DataSheet] = {
    val repository = MuksRaportitRepository(raportointiDatabase)

    val rows = repository
      .suoritustiedot(oppilaitosOids, alku, loppu)
      .map(row => MuksRow.apply(row, t))

    val mainSheet = buildSheet(rows, t)

    Seq(mainSheet)
  }

  private def buildSheet(rows: Seq[MuksRow], t: LocalizationReader) =
    DataSheet(
      title = "Muu kuin säännelty koulutus",
      rows = rows,
      columnSettings = Seq(
        "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
        "lähdejärjestelmä" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
        "lähdejärjestelmänId" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
        "koulutustoimijaNimi" -> Column(t.get("raportti-excel-kolumni-koulutustoimijaNimi")),
        "oppilaitoksenNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
        "toimipisteNimi" -> Column(t.get("raportti-excel-kolumni-toimipisteNimi")),
        "päivitetty" -> Column(t.get("raportti-excel-kolumni-päivitetty")),
        "yksilöity" -> Column(t.get("raportti-excel-kolumni-yksiloity")),
        "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
        "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
        "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
        "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
        "kotikunta" -> Column(t.get("raportti-excel-kolumni-kotikunta")),
        "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
        "viimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila")),
        "opiskeluoikeudenTilatAikajaksonAikana" -> Column(t.get("raportti-excel-kolumni-tilatParametrienSisalla")),
        "opintokokonaisuus" -> Column(t.get("raportti-excel-kolumni-opintokokonaisuus")),
        "suorituksenVahvistuspäivä" -> Column(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva")),
        "rahoitukset" -> Column(t.get("raportti-excel-kolumni-rahoitukset")),
        "jotpaAsianumero" -> Column(t.get("raportti-excel-kolumni-jotpaAsianumero")),
        "yhteislaajuus" -> Column(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiTuntia")),
      )
    )
  }

case class MuksRow(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  koulutustoimijaNimi: String,
  oppilaitoksenNimi: String,
  toimipisteNimi: String,
  päivitetty: LocalDate,
  yksilöity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  kotikunta: String,
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  viimeisinTila: Option[String],
  opiskeluoikeudenTilatAikajaksonAikana: String,
  opintokokonaisuus: Option[String],
  suorituksenVahvistuspäivä: Option[LocalDate],
  rahoitukset: String,
  jotpaAsianumero: Option[String],
  yhteislaajuus: Double,
)

object MuksRow {
  def apply(
    data: MuksRaporttiRows,
    t: LocalizationReader
  ): MuksRow = {
    val oo = data.opiskeluoikeus
    val pts = data.päätasonSuoritus
    val henkilö = data.henkilö
    val aikajaksot = data.aikajaksot

    MuksRow(
      opiskeluoikeusOid = oo.opiskeluoikeusOid,
      lähdejärjestelmä = oo.lähdejärjestelmäKoodiarvo,
      lähdejärjestelmänId = oo.lähdejärjestelmäId,
      koulutustoimijaNimi = t.pick(oo.koulutustoimijaNimi, oo.koulutustoimijaNimiSv),
      oppilaitoksenNimi = t.pick(oo.oppilaitosNimi, oo.oppilaitosNimiSv),
      toimipisteNimi = t.pick(pts.toimipisteNimi, pts.toimipisteNimiSv),
      päivitetty = oo.aikaleima.toLocalDateTime.toLocalDate,
      yksilöity = henkilö.yksiloity,
      oppijaOid = henkilö.oppijaOid,
      hetu = henkilö.hetu,
      sukunimi = henkilö.sukunimi,
      etunimet = henkilö.etunimet,
      kotikunta = t.pick(henkilö.kotikuntaNimiFi, henkilö.kotikuntaNimiSv, henkilö.kotikunta.getOrElse("")),
      opiskeluoikeudenAlkamispäivä = oo.alkamispäivä.map(_.toLocalDate),
      viimeisinTila = oo.viimeisinTila,
      opiskeluoikeudenTilatAikajaksonAikana = aikajaksot.map(_.tila).distinct.mkString(", "),
      opintokokonaisuus = pts.opintokokokonaisuusDatasta.map(ok => s"${ok.koodiarvo} ${ok.nimi.map(t.from).getOrElse("")}"),
      suorituksenVahvistuspäivä = pts.vahvistusPäivä.map(_.toLocalDate),
      rahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).distinct.mkString(", "),
      jotpaAsianumero = oo.lisätiedotJotpaAsianumero,
      yhteislaajuus = data.osasuoritukset.map(_.laajuus.toDouble).sum,
    )
  }
}
