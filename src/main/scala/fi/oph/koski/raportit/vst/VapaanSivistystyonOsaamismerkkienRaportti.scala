package fi.oph.koski.raportit.vst

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.{Koodistokoodiviite, Organisaatio}

import java.time.LocalDate

object VapaanSivistystyonOsaamismerkkienRaportti {
  def buildRaportti(
    raportointiDatabase: RaportointiDatabase,
    oppilaitosOids: Set[Organisaatio.Oid],
    alku: LocalDate,
    loppu: LocalDate,
    t: LocalizationReader
  ): Seq[DataSheet] = {
    val rows = VstRaportitRepository(raportointiDatabase)
      .suoritustiedot(oppilaitosOids, "vstosaamismerkki", alku, loppu)
      .map(row => VSTOsaamismerkkiRow(row, t))

    Seq(buildSheet(rows, t))
  }

  private def buildSheet(rows: Seq[VSTOsaamismerkkiRow], t: LocalizationReader) =
    DataSheet(
      title = t.get("raportti-excel-vst-osaamismerkit-title"),
      rows = rows,
      columnSettings = Seq(
        "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
        "oppijaMasterOid" -> Column(t.get("raportti-excel-kolumni-oppijaMasterOid")),
        "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
        "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
        "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
        "yksilöity" -> Column(t.get("raportti-excel-kolumni-yksiloity")),
        "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
        "lähdejärjestelmä" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
        "lähdejärjestelmänId" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
        "koulutustoimijaNimi" -> Column(t.get("raportti-excel-kolumni-koulutustoimijaNimi")),
        "oppilaitoksenNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
        "toimipisteNimi" -> Column(t.get("raportti-excel-kolumni-toimipisteNimi")),
        "päivitetty" -> Column(t.get("raportti-excel-kolumni-päivitetty")),
        "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
        "opiskeluoikeudenPäättymispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenPäättymispäivä")),
        "viimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila")),
        "osaamismerkkiKoodiarvo" -> Column(t.get("raportti-excel-kolumni-osaamismerkkiKoodiarvo")),
        "osaamismerkkiNimi" -> Column(t.get("raportti-excel-kolumni-osaamismerkkiNimi")),
        "suoritusArvioitu" -> Column(t.get("raportti-excel-kolumni-suoritusArvioitu"), comment = Some(t.get("raportti-excel-kolumni-suoritusArvioitu-comment"))),
        "suorituksenArviointipaiva" -> Column(t.get("raportti-excel-kolumni-suorituksenArviointipaiva")),
        "suoritusVahvistettu" -> Column(t.get("raportti-excel-kolumni-suoritusVahvistettu"), comment = Some(t.get("raportti-excel-kolumni-suoritusVahvistettu-comment"))),
        "suorituksenVahvistuspäivä" -> Column(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva")),
      )
    )
}

case class VSTOsaamismerkkiRow(
  oppijaOid: String,
  oppijaMasterOid: Option[String],
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  yksilöity: Boolean,
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  koulutustoimijaNimi: String,
  oppilaitoksenNimi: String,
  toimipisteNimi: String,
  päivitetty: LocalDate,
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  opiskeluoikeudenPäättymispäivä: Option[LocalDate],
  viimeisinTila: Option[String],
  osaamismerkkiKoodiarvo: String,
  osaamismerkkiNimi: Option[String],
  suoritusArvioitu: Boolean,
  suorituksenArviointipaiva: Option[LocalDate],
  suoritusVahvistettu: Boolean,
  suorituksenVahvistuspäivä: Option[LocalDate],
)

object VSTOsaamismerkkiRow {
  def apply(data: VstRaporttiRows, t: LocalizationReader): VSTOsaamismerkkiRow = {
    val oo = data.opiskeluoikeus
    val pts = data.päätasonSuoritus
    val henkilö = data.henkilö
    val osaamismerkki = JsonSerializer.extract[Option[Koodistokoodiviite]](pts.data \ "koulutusmoduuli" \ "tunniste")

    VSTOsaamismerkkiRow(
      oppijaOid = henkilö.oppijaOid,
      oppijaMasterOid = oo.oppijaMasterOid,
      hetu = henkilö.hetu,
      sukunimi = henkilö.sukunimi,
      etunimet = henkilö.etunimet,
      yksilöity = henkilö.yksiloity,
      opiskeluoikeusOid = oo.opiskeluoikeusOid,
      lähdejärjestelmä = oo.lähdejärjestelmäKoodiarvo,
      lähdejärjestelmänId = oo.lähdejärjestelmäId,
      koulutustoimijaNimi = t.pick(oo.koulutustoimijaNimi, oo.koulutustoimijaNimiSv),
      oppilaitoksenNimi = t.pick(oo.oppilaitosNimi, oo.oppilaitosNimiSv),
      toimipisteNimi = t.pick(pts.toimipisteNimi, pts.toimipisteNimiSv),
      päivitetty = oo.aikaleima.toLocalDateTime.toLocalDate,
      opiskeluoikeudenAlkamispäivä = oo.alkamispäivä.map(_.toLocalDate),
      opiskeluoikeudenPäättymispäivä = oo.päättymispäivä.map(_.toLocalDate),
      viimeisinTila = oo.viimeisinTila,
      osaamismerkkiKoodiarvo = pts.koulutusmoduuliKoodiarvo,
      osaamismerkkiNimi = osaamismerkki.flatMap(_.nimi).map(t.from).orElse(pts.koulutusmoduuliNimi),
      suoritusArvioitu = pts.arviointiArvosanaKoodiarvo.isDefined,
      suorituksenArviointipaiva = pts.arviointiPäivä.map(_.toLocalDate),
      suoritusVahvistettu = pts.vahvistusPäivä.isDefined,
      suorituksenVahvistuspäivä = pts.vahvistusPäivä.map(_.toLocalDate),
    )
  }
}
