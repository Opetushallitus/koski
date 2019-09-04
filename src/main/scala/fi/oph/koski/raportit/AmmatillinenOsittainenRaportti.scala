package fi.oph.koski.raportit

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportit.AmmatillinenRaporttiUtils._
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{LähdejärjestelmäId, OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}

// scalastyle:off method.length

object AmmatillinenOsittainenRaportti extends AikajaksoRaportti {

  def buildRaportti(database: RaportointiDatabase, oppilaitosOid: Oid, alku: LocalDate, loppu: LocalDate): Seq[AmmatillinenOsittainRaporttiRow] = {
    val data = AmmatillisenRaportitRepository(database.db).suoritustiedot(oppilaitosOid, OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo, "ammatillinentutkintoosittainen", alku, loppu)
    data.map(buildRow(oppilaitosOid, alku, loppu, _))
  }

  private def buildRow(oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, data: (ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow])) = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonSuoritukset, sisältyvätOpiskeluoikeudet, osasuoritukset) = data
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    val osaamisalat = extractOsaamisalatAikavalilta(päätasonSuoritukset, alku, loppu)
    val yhteistenTutkinnonOsienSuoritukset = osasuoritukset.filter(isYhteinenTutkinnonOsa)
    val yhteistenTutkinnonOsienOsaSuoritukset = osasuoritukset.filter(isAmmatillisenTutkinnonOsanOsaalue)
    val muutSuoritukset = osasuoritukset.filter(isAmmatillisenTutkinnonOsa)
    val vapaastiValittavatTutkinnonOsat = osasuoritukset.filter(tutkinnonOsanRyhmä(_, "3"))
    val tutkintoaYksilöllisestiLaajentavatTutkinnonOsa = osasuoritukset.filter(tutkinnonOsanRyhmä(_, "4"))

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
      toimipisteOidit = päätasonSuoritukset.map(_.toimipisteOid).sorted.distinct.mkString(","),
      yksiloity = henkilö.exists(_.yksiloity),
      oppijaOid = opiskeluoikeus.oppijaOid,
      hetu = henkilö.flatMap(_.hetu),
      sukunimi = henkilö.map(_.sukunimi),
      etunimet = henkilö.map(_.etunimet),
      koulutusmoduulit = päätasonSuoritukset.map(_.koulutusmoduuliKoodiarvo).sorted.mkString(","),
      osaamisalat = if (osaamisalat.isEmpty) None else Some(osaamisalat.mkString(",")),
      tutkintonimikkeet = päätasonSuoritukset.flatMap(tutkintonimike(_)).mkString(","),
      päätasonSuorituksenNimi = päätasonSuoritukset.flatMap(_.koulutusmoduuliNimi).mkString(","),
      päätasonSuorituksenSuoritustapa = suoritusTavat(päätasonSuoritukset),
      päätasonSuoritustenTilat = Some(päätasonSuoritustenTilat(päätasonSuoritukset)),
      opiskeluoikeudenAlkamispäivä = opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      viimeisinOpiskeluoikeudenTila = opiskeluoikeus.viimeisinTila,
      viimeisinOpiskeluoikeudenTilaAikajaksonLopussa = aikajaksot.last.tila,
      opintojenRahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).sorted.distinct.mkString(","),
      suoritettujenOpintojenYhteislaajuus = yhteislaajuus(muutSuoritukset.union(yhteistenTutkinnonOsienOsaSuoritukset)),
      valmiitAmmatillisetTutkinnonOsatLkm = muutSuoritukset.filter(isVahvistusPäivällinen).size,
      näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm = näytöt(muutSuoritukset.filter(isVahvistusPäivällinen)).size,
      tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm = tunnustetut(muutSuoritukset.filter(isVahvistusPäivällinen)).size,
      rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm = rahoituksenPiirissä(tunnustetut(muutSuoritukset)).size,
      suoritetutAmmatillisetTutkinnonOsatYhteislaajuus = yhteislaajuus(muutSuoritukset),
      valmiitYhteistenTutkinnonOsatLkm = yhteistenTutkinnonOsienSuoritukset.filter(isVahvistusPäivällinen).size,
      pakollisetYhteistenTutkinnonOsienOsaalueidenLkm = pakolliset(yhteistenTutkinnonOsienOsaSuoritukset).size,
      valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm = valinnaiset(yhteistenTutkinnonOsienOsaSuoritukset).size,
      tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm = tunnustetut(yhteistenTutkinnonOsienOsaSuoritukset).size,
      rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm = rahoituksenPiirissä(tunnustetut(yhteistenTutkinnonOsienOsaSuoritukset)).size,
      tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm = tunnustetut(yhteistenTutkinnonOsienSuoritukset.filter(isVahvistusPäivällinen)).size,
      rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm = rahoituksenPiirissä(tunnustetut(yhteistenTutkinnonOsienSuoritukset)).size,
      suoritettujenYhteistenTutkinnonOsienYhteislaajuus = yhteislaajuus(yhteistenTutkinnonOsienSuoritukset),
      suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = yhteislaajuus(yhteistenTutkinnonOsienOsaSuoritukset),
      pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = yhteislaajuus(pakolliset(yhteistenTutkinnonOsienOsaSuoritukset)),
      valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus = yhteislaajuus(valinnaiset(yhteistenTutkinnonOsienOsaSuoritukset)),
      valmiitVapaaValintaisetTutkinnonOsatLkm = vapaastiValittavatTutkinnonOsat.filter(isVahvistusPäivällinen).size,
      valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm = tutkintoaYksilöllisestiLaajentavatTutkinnonOsa.filter(isVahvistusPäivällinen).size
    )
  }

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String = {
    s"Ammatillinen_tutkinnon_osa_ja_osia_${oppilaitosOid}_${alku}_${loppu}"
  }

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: LocalDateTime): String =
    s"""
       |Suoritustiedot (Ammatillisen tutkinnon osa/osia)
       |Oppilaitos: $oppilaitosOid
       |Aikajakso: ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}
       |Raportti luotu: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadCompleted)} tietojen pohjalta)
       |
       |Tarkempi kuvaus joistakin sarakkeista:
       |
       |- Tutkinnot: kaikki opiskeluoikeudella olevat päätason suoritusten tutkinnot pilkulla erotettuna (myös ennen raportin aikajaksoa valmistuneet, ja raportin aikajakson jälkeen alkaneet). Valtakunnalliset tutkinnot käyttävät "koulutus"-koodistoa, https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/koulutus/latest.
       |
       |- Osaamisalat: kaikkien ym. tutkintojen osaamisalat pilkulla erotettuna (myös ennen/jälkeen raportin aikajaksoa). Valtakunnalliset osaamisalat käyttävät "osaamisala"-koodistoa, https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/osaamisala/latest.
       |
       |- Suorituksen tila: kaikkien opiskeluoikeuteen kuuluvien päätason suoritusten tilat.
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

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String = {
    s"Ammatillinen_tutkinnon_osa_ja_osia_${oppilaitosOid}_${alku.toString.replaceAll("-","")}-${loppu.toString.replaceAll("-","")}.xlsx"
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
    "toimipisteOidit" -> Column("Toimipisteet"),
    "yksiloity" -> Column("Yksilöity", comment = Some("Jos tässä on arvo 'ei', tulee oppija yksilöidä oppijanumerorekisterissä")),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("Hetu"),
    "sukunimi" -> Column("Sukunimi"),
    "etunimet" -> Column("Etunimet"),
    "koulutusmoduulit" -> CompactColumn("Tutkinnot"),
    "osaamisalat" -> CompactColumn("Osaamisalat"),
    "tutkintonimikkeet" -> CompactColumn("Tutkintonimike"),
    "päätasonSuorituksenNimi" -> CompactColumn("Päätason suorituksen nimi"),
    "päätasonSuorituksenSuoritustapa" -> CompactColumn("Päätason suorituksen suoritustapa"),
    "päätasonSuoritustenTilat" -> CompactColumn("Suorituksen tila"),
    "opiskeluoikeudenAlkamispäivä" -> Column("Opiskeluoikeuden alkamispäivä"),
    "viimeisinOpiskeluoikeudenTila" -> CompactColumn("Viimeisin opiskeluoikeuden tila"),
    "viimeisinOpiskeluoikeudenTilaAikajaksonLopussa" -> CompactColumn("Viimeisin opiskeluoikeuden tila aikajakson lopussa"),
    "opintojenRahoitukset" -> CompactColumn("Rahoitukset"),
    "suoritettujenOpintojenYhteislaajuus" -> CompactColumn("Suorittetujen opintojen yhteislaajuus"),
    "valmiitAmmatillisetTutkinnonOsatLkm" -> CompactColumn("Valmiiden ammatillisten tutkinnon osien lukumäärä"),
    "näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn("Valmiissa ammatillisissa tutkinnon osissa olevien näyttöjen lukumäärä"),
    "tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm" -> CompactColumn("Tunnustettujen tutkinnon osien osuus valmiista ammatilllisista tutkinnon osista"),
    "rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm" -> CompactColumn("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista ammatillisista tutkinnon osista"),
    "suoritetutAmmatillisetTutkinnonOsatYhteislaajuus" -> CompactColumn("KOSKI-palveluun siirrettyjen ammatillisten tutkinnon osien (valmis- tai kesken-tilaiset) yhteislaajuus"),
    "valmiitYhteistenTutkinnonOsatLkm" -> CompactColumn("Valmiiden yhteisten tutkinnon osien lukumäärä"),
    "pakollisetYhteistenTutkinnonOsienOsaalueidenLkm" -> CompactColumn("Pakollisten yhteisten tutkinnon osien osa-alueiden lukumäärä"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm" -> CompactColumn("Valinnaisten yhteisten tutkinnon osien osa-alueiden lukumäärä"),
    "tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm" -> CompactColumn("Tunnustettujen tutkinnon osien osa-alueiden osuus valmiista yhteisten tutkinnon osien osa-alueista"),
    "rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm" -> CompactColumn("Rahoituksen piirissä olevien tutkinnon osien osa-aluiden osuus valmiista tunnustetuista yhteisten tutkinnon osien osa-alueista"),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm" -> CompactColumn("Tunnustettujen tutkinnon osien osuus valmiista yhteisistä tutkinnon osista"),
    "rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm" -> CompactColumn("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista yhteisistä tutkinnon osista"),
    "suoritettujenYhteistenTutkinnonOsienYhteislaajuus" -> CompactColumn("KOSKI-palveluun siirrettyjen yhteisten tutkinnon osien (valmis- tai kesken-tilaiset) yhteislaajuus"),
    "suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn("Suoritettujen yhteisten tutkinnon osien osa-alueiden yhteislaajuus"),
    "pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> CompactColumn("Pakollisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus" -> CompactColumn("Valinnaisten yhteisten tutkinnon osien osa-aluiden yhteislaajuus"),
    "valmiitVapaaValintaisetTutkinnonOsatLkm" -> CompactColumn("Valmiiden vapaavalintaisten tutkinnon osien lukumäärä"),
    "valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm" -> CompactColumn("Valmiiden tutkintoa yksilöllisesti laajentavien tutkinnon osien lukumäärä")
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
  toimipisteOidit: String,
  yksiloity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: Option[String],
  etunimet: Option[String],
  koulutusmoduulit: String,
  osaamisalat: Option[String],
  tutkintonimikkeet: String,
  päätasonSuorituksenNimi: String,
  päätasonSuorituksenSuoritustapa: String,
  päätasonSuoritustenTilat: Option[String],
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  viimeisinOpiskeluoikeudenTila: Option[String],
  viimeisinOpiskeluoikeudenTilaAikajaksonLopussa: String,
  opintojenRahoitukset: String,
  suoritettujenOpintojenYhteislaajuus: Double,
  valmiitAmmatillisetTutkinnonOsatLkm: Int,
  näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm: Int,
  tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm: Int,
  rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm: Int,
  suoritetutAmmatillisetTutkinnonOsatYhteislaajuus: Double,
  valmiitYhteistenTutkinnonOsatLkm: Int,
  pakollisetYhteistenTutkinnonOsienOsaalueidenLkm: Int,
  valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm: Int,
  tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm: Int,
  rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm: Int,
  tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm: Int,
  rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm: Int,
  suoritettujenYhteistenTutkinnonOsienYhteislaajuus: Double,
  suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus: Double,
  pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus: Double,
  valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus: Double,
  valmiitVapaaValintaisetTutkinnonOsatLkm: Int,
  valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm: Int
)
