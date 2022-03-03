package fi.oph.koski.raportit

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.AmmatillinenRaporttiUtils._
import fi.oph.koski.raportit.RaporttiUtils.arvioituAikavälillä
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}


// scalastyle:off method.length

object AmmatillinenTutkintoRaportti {

  def buildRaportti(
    request: AikajaksoRaporttiAikarajauksellaRequest,
    repository: AmmatillisenRaportitRepository,
    t: LocalizationReader
  ): Seq[SuoritustiedotTarkistusRow] = {
    repository.suoritustiedot(
      request.oppilaitosOid,
      OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo,
      "ammatillinentutkinto",
      request.alku,
      request.loppu
    ).map(d => buildRows(request.oppilaitosOid, request.alku, request.loppu, request.osasuoritustenAikarajaus, d, t))
  }

  private def buildRows(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean,
    data: (ROpiskeluoikeusRow, RHenkilöRow, Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow]),
    t: LocalizationReader
  ): SuoritustiedotTarkistusRow = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonSuoritus, sisältyvätOpiskeluoikeudet, unfilteredOsasuoritukset) = data
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    val osaamisalat = extractOsaamisalatAikavalilta(päätasonSuoritus, alku, loppu)

    val osasuoritukset = if (osasuoritustenAikarajaus) unfilteredOsasuoritukset.filter(arvioituAikavälillä(alku, loppu)) else unfilteredOsasuoritukset

    val ammatillisetTutkinnonOsatJaOsasuoritukset = osasuoritukset.filter(os => isAmmatillisenTutkinnonOsa(os) | isAmmatillisenKorkeakouluOpintoja(os) | isAmmatillinenJatkovalmiuksiaTukeviaOpintoja(os, osasuoritukset))
    val valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset = ammatillisetTutkinnonOsatJaOsasuoritukset.filter(os => isVahvistusPäivällinen(os) || isArvioinniton(os) || sisältyyVahvistettuunPäätasonSuoritukseen(os, päätasonSuoritus))
    val yhteisetTutkinnonOsat = osasuoritukset.filter(isYhteinenTutkinnonOsa)
    val yhteistenTutkinnonOsienOsaAlueet = osasuoritukset.filter(isYhteinenTutkinnonOsanOsaalue(_, unfilteredOsasuoritukset))
    val vapaastiValittavatTutkinnonOsat = osasuoritukset.filter(tutkinnonOsanRyhmä(_, "3"))
    val tutkintoaYksilöllisestiLaajentavatTutkinnonOsat = osasuoritukset.filter(tutkinnonOsanRyhmä(_, "4"))

    SuoritustiedotTarkistusRow(
      opiskeluoikeusOid = opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
      sisältyyOpiskeluoikeuteenOid = opiskeluoikeus.sisältyyOpiskeluoikeuteenOid.getOrElse(""),
      ostettu = JsonSerializer.validateAndExtract[Boolean](opiskeluoikeus.data \ "ostettu").getOrElse(false),
      sisältyvätOpiskeluoikeudetOidit = sisältyvätOpiskeluoikeudet.map(_.opiskeluoikeusOid).mkString(","),
      sisältyvätOpiskeluoikeudetOppilaitokset = sisältyvätOpiskeluoikeudet.map(_.oppilaitosNimi).mkString(","),
      linkitetynOpiskeluoikeudenOppilaitos = if (opiskeluoikeus.oppilaitosOid != oppilaitosOid) opiskeluoikeus.oppilaitosNimi else "",
      aikaleima = opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
      toimipisteOid = päätasonSuoritus.toimipisteOid,
      yksiloity = henkilö.yksiloity,
      oppijaOid = opiskeluoikeus.oppijaOid,
      hetu = henkilö.hetu,
      sukunimi = henkilö.sukunimi,
      etunimet = henkilö.etunimet,
      tutkinto = päätasonSuoritus.koulutusmoduuliKoodiarvo,
      osaamisalat = if (osaamisalat.isEmpty) None else Some(osaamisalat.mkString(",")),
      tutkintonimike = tutkintonimike(päätasonSuoritus, t.language).getOrElse(""),
      päätasonSuorituksenNimi = päätasonSuoritus.koulutusModuulistaKäytettäväNimi(t.language).getOrElse(""),
      päätasonSuorituksenSuoritusTapa = suoritusTavat(List(päätasonSuoritus), t.language),
      päätasonSuorituksenTila = vahvistusPäiväToTila(päätasonSuoritus.vahvistusPäivä, t),
      opiskeluoikeudenAlkamispäivä = opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      viimeisinOpiskeluoikeudenTila = opiskeluoikeus.viimeisinTila,
      viimeisinOpiskeluoikeudenTilaAikajaksonLopussa = aikajaksot.last.tila,
      opintojenRahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).sorted.distinct.mkString(","),
      painotettuKeskiarvo = JsonSerializer.extract[Option[Float]](päätasonSuoritus.data \ "keskiarvo") match {
        case Some(f) => f.toString
        case _ => ""
      },
      suoritettujenOpintojenYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(ammatillisetTutkinnonOsatJaOsasuoritukset ++ yhteisetTutkinnonOsat ++ vapaastiValittavatTutkinnonOsat ++ tutkintoaYksilöllisestiLaajentavatTutkinnonOsat),
      valmiitAmmatillisetTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset),
      pakollisetAmmatillisetTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(pakolliset(ammatillisetTutkinnonOsatJaOsasuoritukset)),
      valinnaisetAmmatillisetTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(valinnaiset(ammatillisetTutkinnonOsatJaOsasuoritukset)),
      näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(näytöt(valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset)),
      tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(tunnustetut(valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset)),
      rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(rahoituksenPiirissä(tunnustetut(ammatillisetTutkinnonOsatJaOsasuoritukset))),
      suoritetutAmmatillisetTutkinnonOsatYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(ammatillisetTutkinnonOsatJaOsasuoritukset),
      tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(tunnustetut(valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset)),
      pakollisetAmmatillisetTutkinnonOsatYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(pakolliset(ammatillisetTutkinnonOsatJaOsasuoritukset)),
      valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(valinnaiset(ammatillisetTutkinnonOsatJaOsasuoritukset)),
      valmiitYhteistenTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(yhteisetTutkinnonOsat.filter(isVahvistusPäivällinen)),
      pakollisetYhteistenTutkinnonOsienOsaalueidenLkm = suorituksetJaKorotuksetSuoritustenMäärässä(pakolliset(yhteistenTutkinnonOsienOsaAlueet)),
      valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm = suorituksetJaKorotuksetSuoritustenMäärässä(valinnaiset(yhteistenTutkinnonOsienOsaAlueet)),
      tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(tunnustetut(yhteistenTutkinnonOsienOsaAlueet)),
      rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(rahoituksenPiirissä(yhteistenTutkinnonOsienOsaAlueet)),
      tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(tunnustetut(yhteisetTutkinnonOsat)),
      rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(rahoituksenPiirissä(tunnustetut(yhteisetTutkinnonOsat))),
      suoritettujenYhteistenTutkinnonOsienYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(yhteisetTutkinnonOsat),
      tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(tunnustetut(yhteisetTutkinnonOsat)),
      suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(yhteistenTutkinnonOsienOsaAlueet),
      pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(pakolliset(yhteistenTutkinnonOsienOsaAlueet)),
      valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus = suorituksetJaKorotuksetLaajuuksina(valinnaiset(yhteistenTutkinnonOsienOsaAlueet)),
      valmiitVapaaValintaisetTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(vapaastiValittavatTutkinnonOsat.filter(isVahvistusPäivällinen)),
      valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(tutkintoaYksilöllisestiLaajentavatTutkinnonOsat.filter(isVahvistusPäivällinen))
    )
  }

  def filename(request: AikajaksoRaporttiAikarajauksellaRequest, t: LocalizationReader): String =
    s"${t.get("raportti-excel-ammatillinen-suoritustiedot-tiedoston-etuliite")}_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-", "")}-${request.loppu.toString.replaceAll("-", "")}.xlsx"

  def title(request: AikajaksoRaporttiAikarajauksellaRequest, t: LocalizationReader): String =
    s"${t.get("raportti-excel-ammatillinen-suoritustiedot-title")} ${request.oppilaitosOid} ${finnishDateFormat.format(request.alku)} - ${finnishDateFormat.format(request.loppu)}"

  def documentation(request: AikajaksoRaporttiAikarajauksellaRequest, loadStarted: LocalDateTime, t: LocalizationReader): String =
    s"""
       |${t.get("raportti-excel-ammatillinen-suoritustiedot-ohje-title")}
       |${t.get("raportti-excel-ammatillinen-ohje-oppilaitos")}: ${request.oppilaitosOid}
       |${t.get("raportti-excel-ammatillinen-ohje-aikajakso")}: ${finnishDateFormat.format(request.alku)} - ${finnishDateFormat.format(request.loppu)}
       |${t.get("raportti-excel-ammatillinen-ohje-luotu")}: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadStarted)} ${t.get("raportti-excel-ammatillinen-ohje-luotu-takaliite")})
       |
       |${t.get("raportti-excel-ammatillinen-suoritustiedot-ohje-body")}
     """.stripMargin.trim.stripPrefix("\n").stripSuffix("\n")

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "lähdejärjestelmä" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä"), width = Some(4000)),
    "lähdejärjestelmänId" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId"), width = Some(4000)),
    "sisältyyOpiskeluoikeuteenOid" -> Column(t.get("raportti-excel-kolumni-sisältyyOpiskeluoikeuteenOid"), width = Some(4000)),
    "ostettu" -> Column(t.get("raportti-excel-kolumni-ostettu"), width = Some(2000)),
    "sisältyvätOpiskeluoikeudetOidit" -> Column(t.get("raportti-excel-kolumni-sisältyvätOpiskeluoikeudetOidit"), width = Some(4000)),
    "sisältyvätOpiskeluoikeudetOppilaitokset" -> Column(t.get("raportti-excel-kolumni-sisältyvätOpiskeluoikeudetOppilaitokset"), width = Some(4000)),
    "linkitetynOpiskeluoikeudenOppilaitos" -> Column(t.get("raportti-excel-kolumni-linkitetynOpiskeluoikeudenOppilaitos")),
    "aikaleima" -> Column(t.get("raportti-excel-kolumni-päivitetty")),
    "toimipisteOid" -> Column(t.get("raportti-excel-kolumni-toimipisteOid")),
    "yksiloity" -> CompactColumn(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
    "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
    "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
    "tutkinto" -> CompactColumn(t.get("raportti-excel-kolumni-tutkinto")),
    "osaamisalat" -> CompactColumn(t.get("raportti-excel-kolumni-osaamisalat")),
    "tutkintonimike" -> CompactColumn(t.get("raportti-excel-kolumni-tutkintonimike")),
    "päätasonSuorituksenNimi" -> CompactColumn(t.get("raportti-excel-kolumni-päätasonSuorituksenNimi")),
    "päätasonSuorituksenSuoritusTapa" -> CompactColumn(t.get("raportti-excel-kolumni-päätasonSuorituksenSuoritustapa")),
    "päätasonSuorituksenTila" -> CompactColumn(t.get("raportti-excel-kolumni-suorituksenTila")),
    "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    "viimeisinOpiskeluoikeudenTila" -> CompactColumn(t.get("raportti-excel-kolumni-viimeisinTila")),
    "viimeisinOpiskeluoikeudenTilaAikajaksonLopussa" -> CompactColumn(t.get("raportti-excel-kolumni-viimeisinOpiskeluoikeudenTilaAikajaksonLopussa")),
    "opintojenRahoitukset" -> CompactColumn(t.get("raportti-excel-kolumni-rahoitukset")),
    "painotettuKeskiarvo" -> CompactColumn(t.get("raportti-excel-kolumni-painotettuKeskiarvo")),
    "suoritettujenOpintojenYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-suoritettujenOpintojenYhteislaajuus")),
    "valmiitAmmatillisetTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-valmiitAmmatillisetTutkinnonOsatLkm")),
    "pakollisetAmmatillisetTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-pakollisetAmmatillisetTutkinnonOsatLkm")),
    "valinnaisetAmmatillisetTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaisetAmmatillisetTutkinnonOsatLkm")),
    "näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm")),
    "tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm")),
    "rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm")),
    "suoritetutAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-suoritetutAmmatillisetTutkinnonOsatYhteislaajuus")),
    "tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus")),
    "pakollisetAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-pakollisetAmmatillisetTutkinnonOsatYhteislaajuus")),
    "valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus")),
    "valmiitYhteistenTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-valmiitYhteistenTutkinnonOsatLkm")),
    "pakollisetYhteistenTutkinnonOsienOsaalueidenLkm" -> CompactColumn(t.get("raportti-excel-kolumni-pakollisetYhteistenTutkinnonOsienOsaalueidenLkm")),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm")),
    "tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm")),
    "rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm")),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm")),
    "rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm")),
    "suoritettujenYhteistenTutkinnonOsienYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-suoritettujenYhteistenTutkinnonOsienYhteislaajuus")),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus")),
    "suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-suoritettujenYhteistenTutkinnonOsienOsaAlueidenYhteislaajuusEsimerkillä")),
    "pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus")),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus")),
    "valmiitVapaaValintaisetTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-valmiitVapaaValintaisetTutkinnonOsatLkm")),
    "valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm"))
  )
}

case class SuoritustiedotTarkistusRow
(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  sisältyyOpiskeluoikeuteenOid: String,
  ostettu: Boolean,
  sisältyvätOpiskeluoikeudetOidit: String,
  sisältyvätOpiskeluoikeudetOppilaitokset: String,
  linkitetynOpiskeluoikeudenOppilaitos: String,
  aikaleima: LocalDate,
  toimipisteOid: String,
  yksiloity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  tutkinto: String,
  osaamisalat: Option[String],
  tutkintonimike: String,
  päätasonSuorituksenNimi: String,
  päätasonSuorituksenSuoritusTapa: String,
  päätasonSuorituksenTila: String,
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  viimeisinOpiskeluoikeudenTila: Option[String],
  viimeisinOpiskeluoikeudenTilaAikajaksonLopussa: String,
  opintojenRahoitukset: String,
  painotettuKeskiarvo: String,
  suoritettujenOpintojenYhteislaajuus: String,
  valmiitAmmatillisetTutkinnonOsatLkm: String,
  pakollisetAmmatillisetTutkinnonOsatLkm: String,
  valinnaisetAmmatillisetTutkinnonOsatLkm: String,
  näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm: String,
  tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm: String,
  rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm: String,
  suoritetutAmmatillisetTutkinnonOsatYhteislaajuus: String,
  tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus: String,
  pakollisetAmmatillisetTutkinnonOsatYhteislaajuus: String,
  valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus: String,
  valmiitYhteistenTutkinnonOsatLkm: String,
  pakollisetYhteistenTutkinnonOsienOsaalueidenLkm: String,
  valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm: String,
  tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm: String,
  rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm: String,
  tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm: String,
  rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm: String,
  suoritettujenYhteistenTutkinnonOsienYhteislaajuus: String,
  tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus: String,
  suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus: String,
  pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus: String,
  valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus: String,
  valmiitVapaaValintaisetTutkinnonOsatLkm: String,
  valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm: String
)
