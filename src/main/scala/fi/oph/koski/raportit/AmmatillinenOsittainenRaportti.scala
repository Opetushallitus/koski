package fi.oph.koski.raportit

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.AmmatillinenRaporttiUtils._
import fi.oph.koski.raportit.RaporttiUtils.arvioituAikavälillä
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.{LähdejärjestelmäId, OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}

// scalastyle:off method.length

object AmmatillinenOsittainenRaportti {

  def buildRaportti(
    request: AikajaksoRaporttiAikarajauksellaRequest,
    repository: AmmatillisenRaportitRepository,
    t: LocalizationReader
  ): Seq[AmmatillinenOsittainRaporttiRow] = {
    repository
      .suoritustiedot(
        request.oppilaitosOid,
        OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo,
        "ammatillinentutkintoosittainen",
        request.alku,
        request.loppu
      )
      .map(d => buildRow(request.oppilaitosOid, request.alku, request.loppu, request.osasuoritustenAikarajaus, d, t))
  }

  private def buildRow(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean,
    data: (ROpiskeluoikeusRow, RHenkilöRow, Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow]),
    t: LocalizationReader
  ) = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonSuoritus, sisältyvätOpiskeluoikeudet, unFilteredosasuoritukset) = data
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    val osaamisalat = extractOsaamisalatAikavalilta(päätasonSuoritus, alku, loppu)

    val osasuoritukset = if (osasuoritustenAikarajaus) unFilteredosasuoritukset.filter(arvioituAikavälillä(alku, loppu)) else unFilteredosasuoritukset

    val ammatillisetTutkinnonOsat = osasuoritukset.filter(os => isAmmatillisenTutkinnonOsa(os) | isAmmatillisenKorkeakouluOpintoja(os) | isAmmatillinenJatkovalmiuksiaTukeviaOpintoja(os, unFilteredosasuoritukset))
    val yhteistenTutkinnonOsienSuoritukset = osasuoritukset.filter(isYhteinenTutkinnonOsa)
    val yhteistenTutkinnonOsienOsaSuoritukset = osasuoritukset.filter(isYhteinenTutkinnonOsanOsaalue(_, unFilteredosasuoritukset))
    val vapaastiValittavatTutkinnonOsat = osasuoritukset.filter(tutkinnonOsanRyhmä(_, "3"))
    val tutkintoaYksilöllisestiLaajentavatTutkinnonOsa = osasuoritukset.filter(tutkinnonOsanRyhmä(_, "4"))
    val vahvistuspäivällisetTunnustetutAmmatillisenTutkinnonOsat = tunnustetut(ammatillisetTutkinnonOsat.filter(isVahvistusPäivällinen))
    def oppilaitosNimiLokalisoitu(oo: ROpiskeluoikeusRow, lang: String): String = {
      if(lang == "sv") oo.oppilaitosNimiSv else oo.oppilaitosNimi
    }

    AmmatillinenOsittainRaporttiRow(
      opiskeluoikeusOid = opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
      sisältyyOpiskeluoikeuteenOid = opiskeluoikeus.sisältyyOpiskeluoikeuteenOid.getOrElse(""),
      ostettu = JsonSerializer.validateAndExtract[Boolean](opiskeluoikeus.data \ "ostettu").getOrElse(false),
      sisältyvätOpiskeluoikeudetOidit = sisältyvätOpiskeluoikeudet.map(_.opiskeluoikeusOid).mkString(","),
      sisältyvätOpiskeluoikeudetOppilaitokset = sisältyvätOpiskeluoikeudet
        .map(oo => oppilaitosNimiLokalisoitu(oo, t.language))
        .mkString(","),
      linkitetynOpiskeluoikeudenOppilaitos =
        if (opiskeluoikeus.oppilaitosOid != oppilaitosOid) oppilaitosNimiLokalisoitu(opiskeluoikeus, t.language) else "",
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
      päätasonSuorituksenNimi = päätasonSuoritus.perusteestaKäytettäväNimi(t.language)
        .orElse(päätasonSuoritus.koulutusModuulistaKäytettäväNimi(t.language))
        .getOrElse(""),
      päätasonSuorituksenSuoritustapa = suoritusTavat(List(päätasonSuoritus), t.language),
      päätasonSuorituksenTila = vahvistusPäiväToTila(päätasonSuoritus.vahvistusPäivä, t),
      opiskeluoikeudenAlkamispäivä = opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      viimeisinOpiskeluoikeudenTila = opiskeluoikeus.viimeisinTila,
      viimeisinOpiskeluoikeudenTilaAikajaksonLopussa = aikajaksot.last.tila,
      opintojenRahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).sorted.distinct.mkString(","),
      suoritettujenOpintojenYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(ammatillisetTutkinnonOsat.union(yhteistenTutkinnonOsienOsaSuoritukset).filter(_.arvioituJaHyväksytty)),
      valmiitAmmatillisetTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(ammatillisetTutkinnonOsat.filter(isVahvistusPäivällinen)),
      näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(näytöt(ammatillisetTutkinnonOsat.filter(isVahvistusPäivällinen))),
      tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(vahvistuspäivällisetTunnustetutAmmatillisenTutkinnonOsat),
      rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(rahoituksenPiirissä(tunnustetut(ammatillisetTutkinnonOsat))),
      suoritetutAmmatillisetTutkinnonOsatYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(ammatillisetTutkinnonOsat),
      tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(vahvistuspäivällisetTunnustetutAmmatillisenTutkinnonOsat),
      valmiitYhteistenTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(yhteistenTutkinnonOsienSuoritukset.filter(isVahvistusPäivällinen)),
      pakollisetYhteistenTutkinnonOsienOsaalueidenLkm = suorituksetJaKorotuksetSuoritustenMäärässä(pakolliset(yhteistenTutkinnonOsienOsaSuoritukset)),
      valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm = suorituksetJaKorotuksetSuoritustenMäärässä(valinnaiset(yhteistenTutkinnonOsienOsaSuoritukset)),
      tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(tunnustetut(yhteistenTutkinnonOsienOsaSuoritukset)),
      rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(rahoituksenPiirissä(tunnustetut(yhteistenTutkinnonOsienOsaSuoritukset))),
      tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(tunnustetut(yhteistenTutkinnonOsienSuoritukset.filter(isVahvistusPäivällinen))),
      rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm = suorituksetJaKorotuksetSuoritustenMäärässä(rahoituksenPiirissä(tunnustetut(yhteistenTutkinnonOsienSuoritukset))),
      suoritettujenYhteistenTutkinnonOsienYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(yhteistenTutkinnonOsienSuoritukset),
      tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(tunnustetut(yhteistenTutkinnonOsienSuoritukset.filter(isVahvistusPäivällinen))),
      suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(yhteistenTutkinnonOsienOsaSuoritukset.filter(_.arvioituJaHyväksytty)),
      pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = suorituksetJaKorotuksetLaajuuksina(pakolliset(yhteistenTutkinnonOsienOsaSuoritukset)),
      valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus = suorituksetJaKorotuksetLaajuuksina(valinnaiset(yhteistenTutkinnonOsienOsaSuoritukset)),
      valmiitVapaaValintaisetTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(vapaastiValittavatTutkinnonOsat.filter(isVahvistusPäivällinen)),
      valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm = suorituksetJaKorotuksetSuoritustenMäärässä(tutkintoaYksilöllisestiLaajentavatTutkinnonOsa.filter(isVahvistusPäivällinen))
    )
  }

  def title(request: AikajaksoRaporttiAikarajauksellaRequest, t: LocalizationReader): String = {
    s"${t.get("raportti-excel-ammatillinen-osia-suoritustiedot-tiedoston-etuliite")}_${request.oppilaitosOid}_${request.alku}_${request.loppu}"
  }

  def documentation(
    request: AikajaksoRaporttiAikarajauksellaRequest,
    loadStarted: LocalDateTime,
    t: LocalizationReader
  ): String =
    s"""
       |${t.get("raportti-excel-ammatillinen-osia-suoritustiedot-ohje-title")}
       |${t.get("raportti-excel-ammatillinen-ohje-oppilaitos")}: ${request.oppilaitosOid}
       |${t.get("raportti-excel-ammatillinen-ohje-aikajakso")}: ${finnishDateFormat.format(request.alku)} - ${finnishDateFormat.format(request.loppu)}
       |${t.get("raportti-excel-ammatillinen-ohje-luotu")}: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadStarted)} ${t.get("raportti-excel-ammatillinen-ohje-luotu-takaliite")})
       |
       |${t.get("raportti-excel-ammatillinen-osia-suoritustiedot-ohje-body")}
     """.stripMargin.trim.stripPrefix("\n").stripSuffix("\n")

  def filename(request: AikajaksoRaporttiAikarajauksellaRequest, t: LocalizationReader): String = {
    s"${t.get("raportti-excel-ammatillinen-osia-suoritustiedot-tiedoston-etuliite")}_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-","")}-${request.loppu.toString.replaceAll("-","")}.xlsx"
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "lähdejärjestelmä" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä"), width = Some(4000)),
    "lähdejärjestelmänId" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId"), width = Some(4000)),
    "sisältyyOpiskeluoikeuteenOid" -> Column(t.get("raportti-excel-kolumni-sisältyyOpiskeluoikeuteenOid"), width = Some(4000)),
    "ostettu"-> CompactColumn(t.get("raportti-excel-kolumni-ostettu")),
    "sisältyvätOpiskeluoikeudetOidit" -> Column(t.get("raportti-excel-kolumni-sisältyvätOpiskeluoikeudetOidit"), width = Some(4000)),
    "sisältyvätOpiskeluoikeudetOppilaitokset" -> Column(t.get("raportti-excel-kolumni-sisältyvätOpiskeluoikeudetOppilaitokset"), width = Some(4000)),
    "linkitetynOpiskeluoikeudenOppilaitos" -> Column(t.get("raportti-excel-kolumni-linkitetynOpiskeluoikeudenOppilaitos")),
    "aikaleima" -> Column(t.get("raportti-excel-kolumni-päivitetty")),
    "toimipisteOid" -> Column(t.get("raportti-excel-kolumni-toimipisteOid")),
    "yksiloity" -> Column(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
    "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
    "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
    "tutkinto" -> CompactColumn(t.get("raportti-excel-kolumni-tutkinto")),
    "osaamisalat" -> CompactColumn(t.get("raportti-excel-kolumni-osaamisalat")),
    "tutkintonimike" -> CompactColumn(t.get("raportti-excel-kolumni-tutkintonimike")),
    "päätasonSuorituksenNimi" -> CompactColumn(t.get("raportti-excel-kolumni-päätasonSuorituksenNimi")),
    "päätasonSuorituksenSuoritustapa" -> CompactColumn(t.get("raportti-excel-kolumni-päätasonSuorituksenSuoritustapa")),
    "päätasonSuorituksenTila" -> CompactColumn(t.get("raportti-excel-kolumni-suorituksenTila")),
    "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    "viimeisinOpiskeluoikeudenTila" -> CompactColumn(t.get("raportti-excel-kolumni-viimeisinTila")),
    "viimeisinOpiskeluoikeudenTilaAikajaksonLopussa" -> CompactColumn(t.get("raportti-excel-kolumni-viimeisinOpiskeluoikeudenTilaAikajaksonLopussa")),
    "opintojenRahoitukset" -> CompactColumn(t.get("raportti-excel-kolumni-rahoitukset")),
    "suoritettujenOpintojenYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-suoritettujenOpintojenYhteislaajuus")),
    "valmiitAmmatillisetTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-valmiitAmmatillisetTutkinnonOsatLkm")),
    "näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm")),
    "tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm")),
    "rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm")),
    "suoritetutAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-siirretytAmmatillisetTutkinnonOsatYhteislaajuus")),
    "tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus")),
    "valmiitYhteistenTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-valmiitYhteistenTutkinnonOsatLkm")),
    "pakollisetYhteistenTutkinnonOsienOsaalueidenLkm" -> CompactColumn(t.get("raportti-excel-kolumni-pakollisetYhteistenTutkinnonOsienOsaalueidenLkm")),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm")),
    "tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm")),
    "rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm-tunnustetut")),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm")),
    "rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm" -> CompactColumn(t.get("raportti-excel-kolumni-rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm")),
    "suoritettujenYhteistenTutkinnonOsienYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-siirrettyjenYhteistenTutkinnonOsienYhteislaajuus")),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus")),
    "suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-suoritettujenYhteistenTutkinnonOsienOsaAlueidenYhteislaajuusEsimerkillä")),
    "pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus")),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus")),
    "valmiitVapaaValintaisetTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-valmiitVapaaValintaisetTutkinnonOsatLkm")),
    "valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm" -> CompactColumn(t.get("raportti-excel-kolumni-valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm"))
  )
}

case class AmmatillinenOsittainRaporttiRow(
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
  päätasonSuorituksenSuoritustapa: String,
  päätasonSuorituksenTila: String,
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  viimeisinOpiskeluoikeudenTila: Option[String],
  viimeisinOpiskeluoikeudenTilaAikajaksonLopussa: String,
  opintojenRahoitukset: String,
  suoritettujenOpintojenYhteislaajuus: String,
  valmiitAmmatillisetTutkinnonOsatLkm: String,
  näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm: String,
  tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm: String,
  rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm: String,
  suoritetutAmmatillisetTutkinnonOsatYhteislaajuus: String,
  tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus: String,
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
