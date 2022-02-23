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

  def buildRaportti(request: AikajaksoRaporttiAikarajauksellaRequest, repository: AmmatillisenRaportitRepository, t: LocalizationReader): Seq[AmmatillinenOsittainRaporttiRow] = {
    val data = repository.suoritustiedot(request.oppilaitosOid, OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo, "ammatillinentutkintoosittainen", request.alku, request.loppu)
    data.map(buildRow(request.oppilaitosOid, request.alku, request.loppu, request.osasuoritustenAikarajaus, t))
  }

  private def buildRow(oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, osasuoritustenAikarajaus: Boolean, t: LocalizationReader)(data: (ROpiskeluoikeusRow, RHenkilöRow, Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow])) = {
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

    AmmatillinenOsittainRaporttiRow(
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

  def title(request: AikajaksoRaporttiAikarajauksellaRequest): String = {
    s"Ammatillinen_tutkinnon_osa_ja_osia_${request.oppilaitosOid}_${request.alku}_${request.loppu}"
  }

  def documentation(request: AikajaksoRaporttiAikarajauksellaRequest, loadStarted: LocalDateTime): String =
    s"""
       |Suoritustiedot (Ammatillisen tutkinnon osa/osia)
       |Oppilaitos: ${request.oppilaitosOid}
       |Aikajakso: ${finnishDateFormat.format(request.alku)} - ${finnishDateFormat.format(request.loppu)}
       |Raportti luotu: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadStarted)} tietojen pohjalta)
       |
       |Tarkempi kuvaus joistakin sarakkeista:
       |
       |- Tutkinto: Opiskeluoikeudella olevan päätason suorituksen tutkinto (myös ennen raportin aikajaksoa valmistuneet, ja raportin aikajakson jälkeen alkaneet). Valtakunnalliset tutkinnot käyttävät "koulutus"-koodistoa, https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/koulutus/latest.
       |
       |- Osaamisalat: Ym. tutkinnon osaamisalat (myös ennen/jälkeen raportin aikajaksoa). Valtakunnalliset osaamisalat käyttävät "osaamisala"-koodistoa, https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/osaamisala/latest.
       |
       |- Suorituksen tila: Päätason suorituksen tila.
       |
       |- Viimeisin opiskeluoikeuden tila: opiskeluoikeuden tila raportin aikajakson lopussa. Käyttää "koskiopiskeluoikeudentila" koodistoa, https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/koskiopiskeluoikeudentila/latest.
       |
       |- Rahoitukset: raportin aikajaksolla esiintyvät rahoitusmuodot pilkulla erotettuna (aakkosjärjestyksessä, ei aikajärjestyksessä). Arvot ovat "opintojenrahoitus" koodistosta, https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/opintojenrahoitus/latest.
       |
       |- Suoritettujen opintojen yhteislaajuus: KOSKI-palveluun siirrettyjen ammatillisten tutkinnon osien ja yhteisten tutkinnon osien osa-alueiden yhteislaajuus. Lasketaan koulutuksen järjestäjän tutkinnon osille.
       |
       |- Valmiiden ammatillisten tutkinnon osien lukumäärä: KOSKI-palveluun siirrettyjen ammatillisten tutkinnon osien ja yhteisten tutkinnon osien yhteenlaskettu lukumäärä.
       |
       |- Valinnaisten ammatillisten tutkinnon osien lukumäärä: KOSKI-palveluun siirrettyjen valinnaisten ammatillisten tutkinnon osien yhteenlaskettu lukumäärä.
       |
       |- Tunnustettujen tutkinnon osien osuus valmiista ammatillisista tutkinnon osista: KOSKI-palveluun siirrettyjen tunnustettujen ammatillisten tutkinnon osien yhteenlaskettu lukumäärä.
       |
       |- Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista ammatillisista tutkinnon osista: KOSKI-palveluun siirrettyjen tunnustettujen, rahoituksen piirissä olevien ammatillisten tutkinnon osien yhteenlaskettu lukumäärä.
       |
       |- Suoritettujen ammatillisten tutkinnon osien yhteislaajuus: KOSKI-palveluun siirrettyjen ammatillisten tutkinnon osien  yhteislaajuus. Lasketaan koulutuksen järjestäjän tutkinnon osille siirtämistä laajuuksista.
       |
       |- Valinnaisten ammatillisten tutkinnon osien yhteislaajuus: KOSKI-palveluun siirrettyjen valinnaisten ammatillisten tutkinnon osien  yhteislaajuus. Lasketaan koulutuksen järjestäjän tutkinnon osille siirtämistä laajuuksista.
     """.stripMargin.trim.stripPrefix("\n").stripSuffix("\n")

  def filename(request: AikajaksoRaporttiAikarajauksellaRequest): String = {
    s"Ammatillinen_tutkinnon_osa_ja_osia_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-","")}-${request.loppu.toString.replaceAll("-","")}.xlsx"
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "lähdejärjestelmä" -> Column("Lähdejärjestelmä", width = Some(4000)),
    "lähdejärjestelmänId" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä", width = Some(4000)),
    "sisältyyOpiskeluoikeuteenOid" -> Column("Sisältyy opiskeluoikeuteen", width = Some(4000)),
    "ostettu"-> CompactColumn("Ostettu"),
    "sisältyvätOpiskeluoikeudetOidit" -> Column("Sisältyvät opiskeluoikeudet", width = Some(4000)),
    "sisältyvätOpiskeluoikeudetOppilaitokset" -> Column("Sisältyvien opiskeluoikeuksien oppilaitokset", width = Some(4000)),
    "linkitetynOpiskeluoikeudenOppilaitos" -> Column("Toisen koulutuksen järjestäjän opiskeluoikeus"),
    "aikaleima" -> Column("Päivitetty"),
    "toimipisteOid" -> Column("Toimipiste"),
    "yksiloity" -> Column("Yksilöity", comment = Some("Jos tässä on arvo 'ei', tulee oppija yksilöidä oppijanumerorekisterissä")),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("Hetu"),
    "sukunimi" -> Column("Sukunimi"),
    "etunimet" -> Column("Etunimet"),
    "tutkinto" -> CompactColumn("Tutkinto"),
    "osaamisalat" -> CompactColumn("Osaamisalat"),
    "tutkintonimike" -> CompactColumn("Tutkintonimike"),
    "päätasonSuorituksenNimi" -> CompactColumn("Päätason suorituksen nimi"),
    "päätasonSuorituksenSuoritustapa" -> CompactColumn("Päätason suorituksen suoritustapa"),
    "päätasonSuorituksenTila" -> CompactColumn("Suorituksen tila"),
    "opiskeluoikeudenAlkamispäivä" -> Column("Opiskeluoikeuden alkamispäivä"),
    "viimeisinOpiskeluoikeudenTila" -> CompactColumn("Viimeisin opiskeluoikeuden tila"),
    "viimeisinOpiskeluoikeudenTilaAikajaksonLopussa" -> CompactColumn("Viimeisin opiskeluoikeuden tila aikajakson lopussa"),
    "opintojenRahoitukset" -> CompactColumn("Rahoitukset"),
    "suoritettujenOpintojenYhteislaajuus" -> CompactColumn("Suoritettujen opintojen yhteislaajuus, esim: 35,0 (joista 15,0 korotuksia)"),
    "valmiitAmmatillisetTutkinnonOsatLkm" -> CompactColumn("Valmiiden ammatillisten tutkinnon osien lukumäärä, esim: 10 (joista 5 korotuksia)"),
    "näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn("Valmiissa ammatillisissa tutkinnon osissa olevien näyttöjen lukumäärä, esim: 10 (joista 5 korotuksia)"),
    "tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn("Tunnustettujen tutkinnon osien osuus valmiista ammatillisista tutkinnon osista, esim: 10 (joista 5 korotuksia)"),
    "rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm" -> CompactColumn("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista ammatillisista tutkinnon osista, esim: 10 (joista 5 korotuksia)"),
    "suoritetutAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn("KOSKI-palveluun siirrettyjen ammatillisten tutkinnon osien (valmis- tai kesken-tilaiset) yhteislaajuus, esim: 35,0 (joista 15,0 korotuksia)"),
    "tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn("Tunnustettujen ammatillisten tutkinnon osien yhteislaajuus, esim: 35,0 (joista 15,0 korotuksia)"),
    "valmiitYhteistenTutkinnonOsatLkm" -> CompactColumn("Valmiiden yhteisten tutkinnon osien lukumäärä, esim: 10 (joista 5 korotuksia)"),
    "pakollisetYhteistenTutkinnonOsienOsaalueidenLkm" -> CompactColumn("Pakollisten yhteisten tutkinnon osien osa-alueiden lukumäärä, esim: 10 (joista 5 korotuksia)"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm" -> CompactColumn("Valinnaisten yhteisten tutkinnon osien osa-alueiden lukumäärä, esim: 10 (joista 5 korotuksia)"),
    "tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm" -> CompactColumn("Tunnustettujen tutkinnon osien osa-alueiden osuus valmiista yhteisten tutkinnon osien osa-alueista, esim: 10 (joista 5 korotuksia)"),
    "rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm" -> CompactColumn("Rahoituksen piirissä olevien tutkinnon osien osa-aluiden osuus valmiista tunnustetuista yhteisten tutkinnon osien osa-alueista, esim: 10 (joista 5 korotuksia)"),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm" -> CompactColumn("Tunnustettujen tutkinnon osien osuus valmiista yhteisistä tutkinnon osista, esim: 10 (joista 5 korotuksia)"),
    "rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm" -> CompactColumn("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista yhteisistä tutkinnon osista, esim: 10 (joista 5 korotuksia)"),
    "suoritettujenYhteistenTutkinnonOsienYhteislaajuus" -> CompactColumn("KOSKI-palveluun siirrettyjen yhteisten tutkinnon osien (valmis- tai kesken-tilaiset) yhteislaajuus, esim: 35,0 (joista 15,0 korotuksia)"),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus" -> CompactColumn("Tunnustettujen yhteisten tutkinnon osien yhteislaajuus, esim: 35,0 (joista 15,0 korotuksia)"),
    "suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn("Suoritettujen yhteisten tutkinnon osien osa-alueiden yhteislaajuus, esim: 35,0 (joista 15,0 korotuksia)"),
    "pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn("Pakollisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus, esim: 35,0 (joista 15,0 korotuksia)"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus" -> CompactColumn("Valinnaisten yhteisten tutkinnon osien osa-aluiden yhteislaajuus, esim: 35,0 (joista 15,0 korotuksia)"),
    "valmiitVapaaValintaisetTutkinnonOsatLkm" -> CompactColumn("Valmiiden vapaavalintaisten tutkinnon osien lukumäärä, esim: 10 (joista 5 korotuksia)"),
    "valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm" -> CompactColumn("Valmiiden tutkintoa yksilöllisesti laajentavien tutkinnon osien lukumäärä, esim: 10 (joista 5 korotuksia)")
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
