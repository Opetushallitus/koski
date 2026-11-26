package fi.oph.koski.raportit.perusopetus

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, CompactColumn, VuosiluokkaRaporttiPaivalta}
import fi.oph.koski.raportointikanta.ROsasuoritusRow
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{LocalizedString, LähdejärjestelmäId}
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import org.json4s.JValue

import java.time.LocalDate

trait PerusopetuksenVuosiluokkaraporttiPohja extends VuosiluokkaRaporttiPaivalta {
  protected def otsikonEtuliiteAvain: String
  protected def tiedostonEtuliiteAvain: String
  protected def dokumentaatioAvain: String

  def title(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String, t: LocalizationReader): String =
    s"${t.get(otsikonEtuliiteAvain)} $vuosiluokka $oppilaitosOid ${finnishDateFormat.format(paiva)}"

  def filename(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String, t: LocalizationReader): String =
    s"${t.get(tiedostonEtuliiteAvain)}_${vuosiluokka}_${oppilaitosOid}_${paiva}.xlsx"

  def documentation(t: LocalizationReader): String = t.get(dokumentaatioAvain)

  protected def haeRivit(
    repository: PerusopetuksenRaportitRepository,
    oppilaitosOids: Seq[Oid],
    paiva: LocalDate,
    kotikuntaPvm: Option[LocalDate],
    vuosiluokka: String,
    t: LocalizationReader
  ): Seq[PerusopetuksenRaporttiRows] =
    if (vuosiluokka == "9") {
      repository.peruskoulunPaattavatJaLuokalleJääneet(tyypit, oppilaitosOids, paiva, kotikuntaPvm, vuosiluokka, t)
    } else {
      repository.perusopetuksenvuosiluokka(tyypit, oppilaitosOids, paiva, kotikuntaPvm, vuosiluokka, t)
    }

  protected def lähdejärjestelmänId(row: PerusopetuksenRaporttiRows): Option[LähdejärjestelmäId] =
    JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")

  protected def oppilaitoksenNimi(row: PerusopetuksenRaporttiRows, t: LocalizationReader): String =
    if (t.language == "sv") row.opiskeluoikeus.oppilaitosNimiSv else row.opiskeluoikeus.oppilaitosNimi

  // Tyhjä, jos raportointipäivän organisaatiohistoriasta puuttuu koulutustoimija: fetchOrganisaatiohistoriat
  // liittää koulutustoimijan inner joinilla.
  protected def oppilaitosRaportointipäivänä(row: PerusopetuksenRaporttiRows): Option[String] =
    row.organisaatiohistoriaResult.headOption
      .filter(_.oppilaitosOid != row.opiskeluoikeus.oppilaitosOid)
      .map(_.toString)

  protected def kotikunta(row: PerusopetuksenRaporttiRows, kotikuntaPvm: Option[LocalDate], t: LocalizationReader): Option[String] = {
    val nykyinenKotikunta = if (t.language == "sv") row.henkilo.kotikuntaNimiSv else row.henkilo.kotikuntaNimiFi
    kotikuntaPvm match {
      case Some(pvm) => row.kotikuntaHistoriassa
        .map(r => Some(if (t.language == "sv") r.kotikunnanNimiSv else r.kotikunnanNimiFi))
        .getOrElse(kotikuntaEiTiedossa(nykyinenKotikunta, pvm, t))
      case None => nykyinenKotikunta
    }
  }

  private def kotikuntaEiTiedossa(nykyinenKotikunta: Option[String], pvm: LocalDate, t: LocalizationReader): Option[String] =
    nykyinenKotikunta.map { kk =>
      s"${t.get("Ei tiedossa")} ${pvm.format(finnishDateFormat)} (${t.get("nykyinen kotikunta on")} $kk)"
    }

  protected def suorituksenTila(row: PerusopetuksenRaporttiRows, t: LocalizationReader): String =
    if (row.päätasonSuoritus.vahvistusPäivä.isDefined) t.get("raportti-excel-default-value-valmis") else t.get("raportti-excel-default-value-kesken")

  protected def suorituksenAlkamispäivä(row: PerusopetuksenRaporttiRows): String =
    JsonSerializer.extract[Option[LocalDate]](row.päätasonSuoritus.data \ "alkamispäivä").getOrElse("").toString

  protected def suorituksenVahvistuspäivä(row: PerusopetuksenRaporttiRows): String =
    row.päätasonSuoritus.vahvistusPäivä.getOrElse("").toString

  protected def jääLuokalle(row: PerusopetuksenRaporttiRows): Boolean =
    JsonSerializer.extract[Option[Boolean]](row.päätasonSuoritus.data \ "jääLuokalle").getOrElse(false)

  protected def isPakollinen(osasuoritus: ROsasuoritusRow): Boolean =
    JsonSerializer.extract[Option[Boolean]](osasuoritus.data \ "koulutusmoduuli" \ "pakollinen").getOrElse(false)

  protected def oppiaineenNimi(osasuoritus: ROsasuoritusRow, t: LocalizationReader): String = {
    val muuAine = lokalisoituNimi(osasuoritus.data \ "koulutusmoduuli" \ "tunniste" \ "nimi", t)
    val kieliAine = lokalisoituNimi(osasuoritus.data \ "koulutusmoduuli" \ "kieli" \ "nimi", t)
    kieliAine.orElse(muuAine).getOrElse(t.get("raportti-excel-default-value-oppiaine-puuttuu"))
  }

  private def lokalisoituNimi(j: JValue, t: LocalizationReader): Option[String] =
    JsonSerializer.extract[Option[LocalizedString]](j).map(_.get(t.language))

  protected def nimiJaKoodi(osasuoritus: ROsasuoritusRow, t: LocalizationReader): String =
    s"${oppiaineenNimi(osasuoritus, t)} (${osasuoritus.koulutusmoduuliKoodiarvo})"

  protected def compactLisätiedotColumn(title: String, t: LocalizationReader): Column =
    CompactColumn(title, comment = Some(t.get("raportti-excel-kolumni-compactLisätiedotColumn-comment")))

  // Rivin case classin alussa on oltava samannimiset kentät samassa järjestyksessä
  protected def yhteisetAlkusarakkeet(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "oppilaitoksenNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi"), comment = Some(t.get("raportti-excel-kolumni-oppilaitoksenNimi-comment"))),
    "oppilaitosRaportointipäivänä" -> Column(t.get("raportti-excel-kolumni-oppilaitosRaportointipäivänä"), comment = Some(t.get("raportti-excel-kolumni-oppilaitosRaportointipäivänä-comment"))),
    "lähdejärjestelmä" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
    "lähdejärjestelmänId" -> CompactColumn(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
    "päivitetty" -> CompactColumn(t.get("raportti-excel-kolumni-päivitetty"), comment = Some(t.get("raportti-excel-kolumni-päivitetty-comment"))),
    "yksiloity" -> Column(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "oppijaMasterOid" -> Column(t.get("raportti-excel-kolumni-oppijaMasterOid")),
    "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
    "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
    "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
    "sukupuoli" -> Column(t.get("raportti-excel-kolumni-sukupuoli"), comment = Some(t.get("raportti-excel-kolumni-sukupuoli-comment"))),
    "kotikunta" -> Column(t.get("raportti-excel-kolumni-kotikunta"), comment = Some(t.get("raportti-excel-kolumni-kotikunta-comment"))),
    "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    "viimeisinTila" -> CompactColumn(t.get("raportti-excel-kolumni-viimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-viimeisinTila-comment"))),
    "tilaHakupaivalla" -> CompactColumn(t.get("raportti-excel-kolumni-tilaHakupaivalla"), comment = Some(t.get("raportti-excel-kolumni-tilaHakupaivalla-comment"))),
    "suorituksenTila" -> CompactColumn(t.get("raportti-excel-kolumni-suorituksenTila")),
    "suorituksenAlkamispaiva" -> CompactColumn(t.get("raportti-excel-kolumni-suorituksenAlkamispaiva")),
    "suorituksenVahvistuspaiva" -> CompactColumn(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva")),
    "jaaLuokalle" -> CompactColumn(t.get("raportti-excel-kolumni-jaaLuokalle")),
    "luokka" -> CompactColumn(t.get("raportti-excel-kolumni-luokka")),
    "voimassaolevatVuosiluokat" -> CompactColumn(t.get("raportti-excel-kolumni-voimassaolevatVuosiluokat"), comment = Some(t.get("raportti-excel-kolumni-voimassaolevatVuosiluokat-comment"))),
  )
}
