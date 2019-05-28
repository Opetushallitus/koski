package fi.oph.koski.raportit

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}


// scalastyle:off method.length

object AmmatillinenTutkintoRaportti extends AikajaksoRaportti with AmmatillinenRaporttiUtils {

  def buildRaportti(database: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate): Seq[SuoritustiedotTarkistusRow] = {
    val data = AmmatillisenRaportitRepository(database.db).suoritustiedot(oppilaitosOid, OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo, "ammatillinentutkinto", alku, loppu)
    data.map(buildRow(oppilaitosOid, alku, loppu, _))
  }

  private def buildRow(oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, data: (ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow])) = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonSuoritukset, sisältyvätOpiskeluoikeudet, osasuoritukset) = data
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    val osaamisalat = extractOsaamisalatAikavalilta(päätasonSuoritukset, alku, loppu)

    val ammatillisetTutkinnonOsatJaOsasuoritukset = ammatillisetTutkinnonOsatJaOsasuorituksetFrom(osasuoritukset)
    val valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset = ammatillisetTutkinnonOsatJaOsasuoritukset.filter(os => isVahvistusPäivällinen(os) || isArvioinniton(os) || sisältyyVahvistettuunPäätasonSuoritukseen(os, päätasonSuoritukset))
    val yhteisetTutkinnonOsat = osasuoritukset.filter(isYhteinenTutkinnonOsa)
    val yhteistenTutkinnonOsienOsaAlueet = osasuoritukset.filter(isYhteinenTutkinnonOsanOsaalue(_, osasuoritukset))
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
      toimipisteOidit = päätasonSuoritukset.map(_.toimipisteOid).sorted.distinct.mkString(","),
      oppijaOid = opiskeluoikeus.oppijaOid,
      hetu = henkilö.flatMap(_.hetu),
      sukunimi = henkilö.map(_.sukunimi),
      etunimet = henkilö.map(_.etunimet),
      koulutusmoduulit = päätasonSuoritukset.map(_.koulutusmoduuliKoodiarvo).sorted.mkString(","),
      osaamisalat = if (osaamisalat.isEmpty) None else Some(osaamisalat.mkString(",")),
      tutkintonimikkeet = päätasonSuoritukset.flatMap(tutkintonimike(_)).map(_.get("fi")).mkString(","),
      päätasonSuorituksenSuoritusTapa = suoritusTavat(päätasonSuoritukset),
      päätasonSuoritustenTilat = Some(päätasonSuoritustenTilat(päätasonSuoritukset)),
      opiskeluoikeudenAlkamispäivä = opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      viimeisinOpiskeluoikeudenTila = opiskeluoikeus.viimeisinTila,
      viimeisinOpiskeluoikeudenTilaAikajaksonLopussa = aikajaksot.last.tila,
      opintojenRahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).sorted.distinct.mkString(","),
      painotettuKeskiarvo = päätasonSuoritukset.flatMap(ps => JsonSerializer.extract[Option[Float]](ps.data \ "keskiarvo")).mkString(","),
      suoritettujenOpintojenYhteislaajuus = yhteislaajuus(ammatillisetTutkinnonOsatJaOsasuoritukset ++ yhteisetTutkinnonOsat ++ vapaastiValittavatTutkinnonOsat ++ tutkintoaYksilöllisestiLaajentavatTutkinnonOsat),
      valmiitAmmatillisetTutkinnonOsatLkm = valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset.size,
      pakollisetAmmatillisetTutkinnonOsatLkm = pakolliset(ammatillisetTutkinnonOsatJaOsasuoritukset).size,
      valinnaisetAmmatillisetTutkinnonOsatLkm = valinnaiset(ammatillisetTutkinnonOsatJaOsasuoritukset).size,
      näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm = näytöt(valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset).size,
      tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm = tunnustetut(valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset).size,
      rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm = rahoituksenPiirissä(tunnustetut(ammatillisetTutkinnonOsatJaOsasuoritukset)).size,
      suoritetutAmmatillisetTutkinnonOsatYhteislaajuus = yhteislaajuus(ammatillisetTutkinnonOsatJaOsasuoritukset),
      pakollisetAmmatillisetTutkinnonOsatYhteislaajuus = yhteislaajuus(pakolliset(ammatillisetTutkinnonOsatJaOsasuoritukset)),
      valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus = yhteislaajuus(valinnaiset(ammatillisetTutkinnonOsatJaOsasuoritukset)),
      valmiitYhteistenTutkinnonOsatLkm = yhteisetTutkinnonOsat.size,
      pakollisetYhteistenTutkinnonOsienOsaalueidenLkm = pakolliset(yhteistenTutkinnonOsienOsaAlueet).size,
      valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm = valinnaiset(yhteistenTutkinnonOsienOsaAlueet).size,
      tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm = tunnustetut(yhteistenTutkinnonOsienOsaAlueet).size,
      rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm = rahoituksenPiirissä(yhteistenTutkinnonOsienOsaAlueet).size,
      tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm = tunnustetut(yhteisetTutkinnonOsat).size,
      rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm = rahoituksenPiirissä(tunnustetut(yhteisetTutkinnonOsat)).size,
      suoritettujenYhteistenTutkinnonOsienYhteislaajuus = yhteislaajuus(yhteisetTutkinnonOsat),
      suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = yhteislaajuus(yhteistenTutkinnonOsienOsaAlueet),
      pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = yhteislaajuus(pakolliset(yhteistenTutkinnonOsienOsaAlueet)),
      valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus = yhteislaajuus(valinnaiset(yhteistenTutkinnonOsienOsaAlueet)),
      valmiitVapaaValintaisetTutkinnonOsatLkm = vapaastiValittavatTutkinnonOsat.filter(isVahvistusPäivällinen).size,
      valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm = tutkintoaYksilöllisestiLaajentavatTutkinnonOsat.filter(isVahvistusPäivällinen).size
    )
  }

  private val isArvioinniton: ROsasuoritusRow => Boolean = osasuoritus => isAnyOf(osasuoritus,
    isAmmatillisenLukioOpintoja,
    isAmmatillisenKorkeakouluOpintoja,
    isAmmatillinenMuitaOpintoValmiuksiaTukeviaOpintoja,
    isAmmatillisenTutkinnonOsanOsaalue
  )

  private def ammatillisetTutkinnonOsatJaOsasuorituksetFrom(osasuoritukset: Seq[ROsasuoritusRow]) = {
    osasuoritukset.filter(os =>
      isAnyOf(os,
        isAmmatillisenLukioOpintoja,
        isAmmatillisenKorkeakouluOpintoja,
        isAmmatillinenMuitaOpintoValmiuksiaTukeviaOpintoja,
        isAmmatillisenTutkinnonOsa,
        isAmmatillisenYhteisenTutkinnonOsienOsaalue(_, osasuoritukset)
      ))
  }

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String =
    s"suoritustiedot_${oppilaitosOid}_${alku.toString.replaceAll("-", "")}-${loppu.toString.replaceAll("-", "")}.xlsx"

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String =
    s"Suoritustiedot $oppilaitosOid ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}"

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp): String =
    s"""
       |Suoritustiedot (ammatillinen tutkinto)
       |Oppilaitos: $oppilaitosOid
       |Aikajakso: ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}
       |Raportti luotu: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadCompleted.toLocalDateTime)} tietojen pohjalta)
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
       |- Painotettu keskiarvo: kaikkien opiskeluoikeuteen kuuluvien päätason suoritusten painotetut keskiarvot.
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
    "toimipisteOidit" -> Column("Toimipisteet"),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("Hetu"),
    "sukunimi" -> Column("Sukunimi"),
    "etunimet" -> Column("Etunimet"),
    "koulutusmoduulit" -> CompactColumn("Tutkinnot"),
    "osaamisalat" -> CompactColumn("Osaamisalat"),
    "tutkintonimikkeet" -> CompactColumn("Tutkintonimike"),
    "päätasonSuorituksenSuoritusTapa" -> CompactColumn("Päätason suorituksen suoritustapa"),
    "päätasonSuoritustenTilat" -> CompactColumn("Suorituksen tila"),
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
  toimipisteOidit: String,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: Option[String],
  etunimet: Option[String],
  koulutusmoduulit: String,
  osaamisalat: Option[String],
  tutkintonimikkeet: String,
  päätasonSuorituksenSuoritusTapa: String,
  päätasonSuoritustenTilat: Option[String],
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  viimeisinOpiskeluoikeudenTila: Option[String],
  viimeisinOpiskeluoikeudenTilaAikajaksonLopussa: String,
  opintojenRahoitukset: String,
  painotettuKeskiarvo: String,
  suoritettujenOpintojenYhteislaajuus: Double,
  valmiitAmmatillisetTutkinnonOsatLkm: Int,
  pakollisetAmmatillisetTutkinnonOsatLkm: Int,
  valinnaisetAmmatillisetTutkinnonOsatLkm: Int,
  näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm: Int,
  tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm: Int,
  rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm: Int,
  suoritetutAmmatillisetTutkinnonOsatYhteislaajuus: Double,
  pakollisetAmmatillisetTutkinnonOsatYhteislaajuus: Double,
  valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus: Double,
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
