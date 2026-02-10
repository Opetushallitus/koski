package fi.oph.koski.raportit.vst

import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.Organisaatio

import java.time.LocalDate

object JatkuvanOppimisenVapaanSivistystyonRaportti {
  def buildRaportti(
    raportointiDatabase: RaportointiDatabase,
    oppilaitosOids: Set[Organisaatio.Oid],
    alku: LocalDate,
    loppu: LocalDate,
    t: LocalizationReader
  ): Seq[DataSheet] = {
    val repository = VstRaportitRepository(raportointiDatabase)

    val rows = repository
      .suoritustiedot(oppilaitosOids, "vstjotpakoulutus", alku, loppu)
      .map(row => VSTJotpaRow.apply(row, t))

    val mainSheet = buildSheet(rows, t)

    Seq(mainSheet)
  }

  private def buildSheet(rows: Seq[VSTJotpaRow], t: LocalizationReader) =
    DataSheet(
      title = "Jatkuvan oppimisen vapaan sivistyön koulutus",
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
        "oppijaMasterOid" -> Column(t.get("raportti-excel-kolumni-oppijaMasterOid")),
        "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
        "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
        "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
        "kotikunta" -> Column(t.get("raportti-excel-kolumni-kotikunta")),
        "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
        "viimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila")),
        "opiskeluoikeudenTilatAikajaksonAikana" -> Column(t.get("raportti-excel-kolumni-tilatParametrienSisalla")),
        "suorituksenTyyppi" -> Column(t.get("raportti-excel-kolumni-suorituksenTyyppi")),
        "opintokokonaisuus" -> Column(t.get("raportti-excel-kolumni-opintokokonaisuus")),
        "suorituksenVahvistuspäivä" -> Column(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva")),
        "rahoitukset" -> Column(t.get("raportti-excel-kolumni-rahoitukset")),
        "jotpaAsianumero" -> Column(t.get("raportti-excel-kolumni-jotpaAsianumero")),
        "yhteislaajuus" -> Column(t.get("raportti-excel-kolumni-yhteislaajuusKaikki")),
        "yhteislaajuusHyväksytyt" -> Column(t.get("raportti-excel-kolumni-yhteislaajuusHyväksytyt")),
      )
    )
  }

case class VSTJotpaRow(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  koulutustoimijaNimi: String,
  oppilaitoksenNimi: String,
  toimipisteNimi: String,
  päivitetty: LocalDate,
  yksilöity: Boolean,
  oppijaOid: String,
  oppijaMasterOid: Option[String],
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  kotikunta: String,
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  viimeisinTila: Option[String],
  opiskeluoikeudenTilatAikajaksonAikana: String,
  suorituksenTyyppi: String,
  opintokokonaisuus: Option[String],
  suorituksenVahvistuspäivä: Option[LocalDate],
  rahoitukset: String,
  jotpaAsianumero: Option[String],
  yhteislaajuus: Double,
  yhteislaajuusHyväksytyt: Double,
)

object VSTJotpaRow {
  def apply(
    data: VstRaporttiRows,
    t: LocalizationReader
  ): VSTJotpaRow = {
    val oo = data.opiskeluoikeus
    val pts = data.päätasonSuoritus
    val henkilö = data.henkilö
    val aikajaksot = data.aikajaksot
    val osasuoritukset = data.osasuoritukset

    val laajuudet = osasuoritukset.map(os => (
      os.arviointiHyväksytty.contains(true),
      os.laajuus.toDouble,
    ))

    VSTJotpaRow(
      opiskeluoikeusOid = oo.opiskeluoikeusOid,
      lähdejärjestelmä = oo.lähdejärjestelmäKoodiarvo,
      lähdejärjestelmänId = oo.lähdejärjestelmäId,
      koulutustoimijaNimi = t.pick(oo.koulutustoimijaNimi, oo.koulutustoimijaNimiSv),
      oppilaitoksenNimi = t.pick(oo.oppilaitosNimi, oo.oppilaitosNimiSv),
      toimipisteNimi = t.pick(pts.toimipisteNimi, pts.toimipisteNimiSv),
      päivitetty = oo.aikaleima.toLocalDateTime.toLocalDate,
      yksilöity = henkilö.yksiloity,
      oppijaOid = henkilö.oppijaOid,
      oppijaMasterOid = oo.oppijaMasterOid,
      hetu = henkilö.hetu,
      sukunimi = henkilö.sukunimi,
      etunimet = henkilö.etunimet,
      kotikunta = t.pick(henkilö.kotikuntaNimiFi, henkilö.kotikuntaNimiSv, henkilö.kotikunta.getOrElse("")),
      opiskeluoikeudenAlkamispäivä = oo.alkamispäivä.map(_.toLocalDate),
      viimeisinTila = oo.viimeisinTila,
      opiskeluoikeudenTilatAikajaksonAikana = aikajaksot.map(_.tila).distinct.mkString(", "),
      suorituksenTyyppi = pts.suorituksenTyyppi,
      opintokokonaisuus = pts.opintokokokonaisuusDatasta.map(ok => s"${ok.koodiarvo} ${ok.nimi.map(t.from).getOrElse("")}"),
      suorituksenVahvistuspäivä = pts.vahvistusPäivä.map(_.toLocalDate),
      rahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).distinct.mkString(", "),
      jotpaAsianumero = oo.lisätiedotJotpaAsianumero,
      yhteislaajuus = laajuudet.map(_._2).sum,
      yhteislaajuusHyväksytyt = laajuudet.filter(_._1).map(_._2).sum,
    )
  }
}
