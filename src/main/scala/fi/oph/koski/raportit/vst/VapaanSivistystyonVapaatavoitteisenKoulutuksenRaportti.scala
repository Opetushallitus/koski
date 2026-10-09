package fi.oph.koski.raportit.vst

import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.Organisaatio

import java.time.LocalDate

object VapaanSivistystyonVapaatavoitteisenKoulutuksenRaportti {
  def buildRaportti(
    raportointiDatabase: RaportointiDatabase,
    oppilaitosOids: Set[Organisaatio.Oid],
    alku: LocalDate,
    loppu: LocalDate,
    t: LocalizationReader
  ): Seq[DataSheet] = {
    val rows = VstRaportitRepository(raportointiDatabase)
      .suoritustiedot(oppilaitosOids, "vstvapaatavoitteinenkoulutus", alku, loppu)
      .map(row => VSTVapaatavoitteinenRow(row, t))

    Seq(buildSheet(rows, t))
  }

  private def buildSheet(rows: Seq[VSTVapaatavoitteinenRow], t: LocalizationReader) =
    DataSheet(
      title = t.get("raportti-excel-vst-vapaatavoitteinen-title"),
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
        "opiskeluoikeudenPäättymispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenPäättymispäivä")),
        "viimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila")),
        "opintokokonaisuusKoodiarvo" -> Column(t.get("raportti-excel-kolumni-opintokokonaisuusKoodiarvo")),
        "opintokokonaisuusNimi" -> Column(t.get("raportti-excel-kolumni-opintokokonaisuusNimi")),
        "yhteislaajuus" -> Column(t.get("raportti-excel-kolumni-yhteislaajuusOpintopisteet")),
        "osasuorituksiaYhteensä" -> Column(t.get("raportti-excel-kolumni-osasuorituksiaYhteensä")),
        "arvioitujaOsasuorituksia" -> Column(t.get("raportti-excel-kolumni-arvioitujaOsasuorituksia")),
        "arviointiPuuttuuOsasuorituksia" -> Column(t.get("raportti-excel-kolumni-arviointiPuuttuuOsasuorituksia")),
        "suoritusVahvistettu" -> Column(t.get("raportti-excel-kolumni-suoritusVahvistettu")),
        "suorituksenVahvistuspäivä" -> Column(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva")),
      )
    )
}

case class VSTVapaatavoitteinenRow(
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
  opiskeluoikeudenPäättymispäivä: Option[LocalDate],
  viimeisinTila: Option[String],
  opintokokonaisuusKoodiarvo: Option[String],
  opintokokonaisuusNimi: Option[String],
  yhteislaajuus: Double,
  osasuorituksiaYhteensä: Int,
  arvioitujaOsasuorituksia: Int,
  arviointiPuuttuuOsasuorituksia: Int,
  suoritusVahvistettu: Boolean,
  suorituksenVahvistuspäivä: Option[LocalDate],
)

object VSTVapaatavoitteinenRow {
  def apply(data: VstRaporttiRows, t: LocalizationReader): VSTVapaatavoitteinenRow = {
    val oo = data.opiskeluoikeus
    val pts = data.päätasonSuoritus
    val henkilö = data.henkilö
    val osasuoritukset = data.osasuoritukset
    val opintokokonaisuus = pts.opintokokokonaisuusDatasta
    // Sisäkkäisten osasuoritusten laajuus sisältyy jo ylemmän tason laajuuteen
    val ylimmänTasonOsasuoritukset = osasuoritukset.filter(_.ylempiOsasuoritusId.isEmpty)
    val arvioidut = osasuoritukset.count(_.arviointiArvosanaKoodiarvo.isDefined)

    VSTVapaatavoitteinenRow(
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
      opiskeluoikeudenPäättymispäivä = oo.päättymispäivä.map(_.toLocalDate),
      viimeisinTila = oo.viimeisinTila,
      opintokokonaisuusKoodiarvo = opintokokonaisuus.map(_.koodiarvo),
      opintokokonaisuusNimi = opintokokonaisuus.flatMap(_.nimi).map(t.from),
      yhteislaajuus = ylimmänTasonOsasuoritukset.map(_.laajuus.toDouble).sum,
      osasuorituksiaYhteensä = osasuoritukset.size,
      arvioitujaOsasuorituksia = arvioidut,
      arviointiPuuttuuOsasuorituksia = osasuoritukset.size - arvioidut,
      suoritusVahvistettu = pts.vahvistusPäivä.isDefined,
      suorituksenVahvistuspäivä = pts.vahvistusPäivä.map(_.toLocalDate),
    )
  }
}
