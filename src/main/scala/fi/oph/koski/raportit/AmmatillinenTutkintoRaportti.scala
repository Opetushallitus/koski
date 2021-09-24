package fi.oph.koski.raportit

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportit.AmmatillinenRaporttiUtils._
import fi.oph.koski.raportit.RaporttiUtils.arvioituAikavälillä
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}


// scalastyle:off method.length

object AmmatillinenTutkintoRaportti {

  def buildRaportti(request: AikajaksoRaporttiAikarajauksellaRequest, repository: AmmatillisenRaportitRepository): Seq[SuoritustiedotTarkistusRow] = {
    val data = repository.suoritustiedot(request.oppilaitosOid, OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo, "ammatillinentutkinto", request.alku, request.loppu)
    data.map(buildRows(request.oppilaitosOid, request.alku, request.loppu, request.osasuoritustenAikarajaus))
  }

  private def buildRows(oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, osasuoritustenAikarajaus: Boolean)(data: (ROpiskeluoikeusRow, RHenkilöRow, Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow])): SuoritustiedotTarkistusRow = {
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
      tutkintonimike = tutkintonimike(päätasonSuoritus).getOrElse(""),
      päätasonSuorituksenNimi = päätasonSuoritus.koulutusmoduuliNimi.getOrElse(""),
      päätasonSuorituksenSuoritusTapa = suoritusTavat(List(päätasonSuoritus)),
      päätasonSuorituksenTila = vahvistusPäiväToTila(päätasonSuoritus.vahvistusPäivä),
      opiskeluoikeudenAlkamispäivä = opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      viimeisinOpiskeluoikeudenTila = opiskeluoikeus.viimeisinTila,
      viimeisinOpiskeluoikeudenTilaAikajaksonLopussa = aikajaksot.last.tila,
      opintojenRahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).sorted.distinct.mkString(","),
      painotettuKeskiarvo = JsonSerializer.extract[Option[Float]](päätasonSuoritus.data \ "keskiarvo") match { case Some(f) => f.toString case _ => ""},
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

  def filename(request: AikajaksoRaporttiAikarajauksellaRequest): String =
    s"suoritustiedot_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-", "")}-${request.loppu.toString.replaceAll("-", "")}.xlsx"

  def title(request: AikajaksoRaporttiAikarajauksellaRequest): String =
    s"Suoritustiedot ${request.oppilaitosOid} ${finnishDateFormat.format(request.alku)} - ${finnishDateFormat.format(request.loppu)}"

  def documentation(request: AikajaksoRaporttiAikarajauksellaRequest, loadStarted: LocalDateTime): String =
    s"""
       |Suoritustiedot (ammatillinen tutkinto)
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
       |- Painotettu keskiarvo: Opiskeluoikeuteen kuuluvan päätason suorituksen painotettu keskiarvo.
       |
       |- Suoritettujen opintojen yhteislaajuus: KOSKI-palveluun siirrettyjen ammatillisten tutkinnon osien (mukaan lukien otsikoiden ”Korkeakouluopinnot” sekä ”Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja löytyvät osasuoritukset) ja yhteisten tutkinnon osien osa-alueiden yhteislaajuus. Lasketaan koulutuksen järjestäjän tutkinnon osille tai niiden osa-alueille siirtämistä laajuuksista.
       |
       |- Valmiiden ammatillisten tutkinnon osien lukumäärä: KOSKI-palveluun siirrettyjen ammatillisten tutkinnon osien yhteenlaskettu lukumäärä mukaan lukien otsikoiden ”Korkeakouluopinnot” sekä ”Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja” löytyvät osasuoritukset.
       |
       |- Valinnaisten ammatillisten tutkinnon osien lukumäärä: KOSKI-palveluun siirrettyjen valinnaisten ammatillisten tutkinnon osien yhteenlaskettu lukumäärä mukaan lukien otsikoiden ”Korkeakouluopinnot” sekä ”Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja” löytyvät osasuoritukset.
       |
       |- Tunnustettujen tutkinnon osien osuus valmiista ammatillisista tutkinnon osista: KOSKI-palveluun siirrettyjen tunnustettujen ammatillisten tutkinnon osien yhteenlaskettu lukumäärä mukaan lukien otsikoiden ”Korkeakouluopinnot” sekä ”Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja” löytyvät osasuoritukset.
       |
       |- Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista ammatillisista tutkinnon osista: KOSKI-palveluun siirrettyjen tunnustettujen, rahoituksen piirissä olevien ammatillisten tutkinnon osien yhteenlaskettu lukumäärä mukaan lukien otsikoiden ”Korkeakouluopinnot” sekä ”Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja” löytyvät osasuoritukset.
       |
       |- Suoritettujen ammatillisten tutkinnon osien yhteislaajuus: KOSKI-palveluun siirrettyjen ammatillisten tutkinnon osien (mukaan lukien otsikoiden ”Korkeakouluopinnot” sekä ”Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja löytyvät osasuoritukset) yhteislaajuus. Lasketaan koulutuksen järjestäjän tutkinnon osille siirtämistä laajuuksista.
       |
       |- Valinnaisten ammatillisten tutkinnon osien yhteislaajuus: KOSKI-palveluun siirrettyjen valinnaisten ammatillisten tutkinnon osien (mukaan lukien otsikoiden ”Korkeakouluopinnot” sekä ”Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja löytyvät osasuoritukset) yhteislaajuus. Lasketaan koulutuksen järjestäjän tutkinnon osille siirtämistä laajuuksista.
     """.stripMargin.trim.stripPrefix("\n").stripSuffix("\n")

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "lähdejärjestelmä" -> Column("Lähdejärjestelmä", width = Some(4000)),
    "lähdejärjestelmänId" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä", width = Some(4000)),
    "sisältyyOpiskeluoikeuteenOid" -> Column("Sisältyy opiskeluoikeuteen", width = Some(4000)),
    "ostettu"-> Column("Ostettu", width = Some(2000)),
    "sisältyvätOpiskeluoikeudetOidit" -> Column("Sisältyvät opiskeluoikeudet", width = Some(4000)),
    "sisältyvätOpiskeluoikeudetOppilaitokset" -> Column("Sisältyvien opiskeluoikeuksien oppilaitokset", width = Some(4000)),
    "linkitetynOpiskeluoikeudenOppilaitos" -> Column("Toisen koulutuksen järjestäjän opiskeluoikeus"),
    "aikaleima" -> Column("Päivitetty"),
    "toimipisteOid" -> Column("Toimipiste"),
    "yksiloity" -> CompactColumn("Yksilöity", comment = Some("Jos tässä on arvo 'ei', tulee oppija yksilöidä oppijanumerorekisterissä")),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("Hetu"),
    "sukunimi" -> Column("Sukunimi"),
    "etunimet" -> Column("Etunimet"),
    "tutkinto" -> CompactColumn("Tutkinto"),
    "osaamisalat" -> CompactColumn("Osaamisalat"),
    "tutkintonimike" -> CompactColumn("Tutkintonimike"),
    "päätasonSuorituksenNimi" -> CompactColumn("Päätason suorituksen nimi"),
    "päätasonSuorituksenSuoritusTapa" -> CompactColumn("Päätason suorituksen suoritustapa"),
    "päätasonSuorituksenTila" -> CompactColumn("Suorituksen tila"),
    "opiskeluoikeudenAlkamispäivä" -> Column("Opiskeluoikeuden alkamispäivä"),
    "viimeisinOpiskeluoikeudenTila" -> CompactColumn("Viimeisin opiskeluoikeuden tila"),
    "viimeisinOpiskeluoikeudenTilaAikajaksonLopussa" -> CompactColumn("Viimeisin opiskeluoikeuden tila aikajakson lopussa"),
    "opintojenRahoitukset" -> CompactColumn("Rahoitukset"),
    "painotettuKeskiarvo" -> CompactColumn("Painotettu keskiarvo"),
    "suoritettujenOpintojenYhteislaajuus" -> CompactColumn("Suoritettujen opintojen yhteislaajuus"),
    "valmiitAmmatillisetTutkinnonOsatLkm" -> CompactColumn("Valmiiden ammatillisten tutkinnon osien lukumäärä"),
    "pakollisetAmmatillisetTutkinnonOsatLkm" -> CompactColumn("Pakollisten ammatillisten tutkinnon osien lukumäärä"),
    "valinnaisetAmmatillisetTutkinnonOsatLkm" -> CompactColumn("Valinnaisten ammatillisten tutkinnon osien lukumäärä"),
    "näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn("Valmiissa ammatillisissa tutkinnon osissa olevien näyttöjen lukumäärä"),
    "tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn("Tunnustettujen tutkinnon osien osuus valmiista ammatillisista tutkinnon osista"),
    "rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm" -> CompactColumn("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista ammatillisista tutkinnon osista"),
    "suoritetutAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn("Suoritettujen ammatillisten tutkinnon osien yhteislaajuus"),
    "tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn("Tunnustettujen ammatillisten tutkinnon osien yhteislaajuus"),
    "pakollisetAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn("Pakollisten ammatillisten tutkinnon osien yhteislaajuus"),
    "valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn("Valinnaisten ammatillisten tutkinnon osien yhteislaajuus"),
    "valmiitYhteistenTutkinnonOsatLkm" -> CompactColumn("Valmiiden yhteisten tutkinnon osien lukumäärä"),
    "pakollisetYhteistenTutkinnonOsienOsaalueidenLkm" -> CompactColumn("Pakollisten yhteisten tutkinnon osien osa-alueiden lukumäärä"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm" -> CompactColumn("Valinnaisten yhteisten tutkinnon osien osa-aluiden lukumäärä"),
    "tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm" -> CompactColumn("Tunnustettujen tutkinnon osien osa-alueiden osuus valmiista yhteisten tutkinnon osien osa-alueista"),
    "rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm" -> CompactColumn("Rahoituksen piirissä olevien tutkinnon osien osa-aluiden osuus valmiista yhteisten tutkinnon osien osa-alueista"),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm" -> CompactColumn("Tunnustettujen tutkinnon osien osuus valmiista yhteisistä tutkinnon osista"),
    "rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm" -> CompactColumn("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista yhteisistä tutkinnon osista"),
    "suoritettujenYhteistenTutkinnonOsienYhteislaajuus" -> CompactColumn("Suoritettujen yhteisten tutkinnon osien yhteislaajuus"),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus" -> CompactColumn("Tunnustettujen yhteisten tutkinnon osien yhteislaajuus"),
    "suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn("Suoritettujen yhteisten tutkinnon osien osa-alueiden yhteislaajuus"),
    "pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn("Pakollisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus" -> CompactColumn("Valinnaisten yhteisten tutkinnon osien osa-aluiden yhteislaajuus"),
    "valmiitVapaaValintaisetTutkinnonOsatLkm" -> CompactColumn("Valmiiden vapaavalintaisten tutkinnon osien lukumäärä"),
    "valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm" -> CompactColumn("Valmiiden tutkintoa yksilöllisesti laajentavien tutkinnon osien lukumäärä")
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
