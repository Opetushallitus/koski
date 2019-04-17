package fi.oph.koski.raportit

import java.sql.Timestamp
import java.time.LocalDate

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{LähdejärjestelmäId, Organisaatio}

// scalastyle:off method.length

object AmmatillinenOsittainenRaportti extends AikajaksoRaportti with AmmatillinenRaporttiUtils {

  def buildRaportti(database: RaportointiDatabase, oppilaitosOid: Oid, alku: LocalDate, loppu: LocalDate): Seq[AmmatillinenOsittainRaporttiRow] = {
    val data = AmmatillisenRaportitRepository(database.db).suoritustiedot(oppilaitosOid, OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo, "ammatillinentutkintoosittainen", alku, loppu)
    data.map(buildRow(oppilaitosOid, alku, loppu, _))
  }

  private def buildRow(oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, data: (ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow])) = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonSuoritukset, sisältyvätOpiskeluoikeudet, osasuoritukset) = data
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    val osaamisalat = extractOsaamisalatAikavalilta(päätasonSuoritukset, alku, loppu)
    val yhteistenTutkinnonOsienSuoritukset = osasuoritukset.filter(tutkinnonOsanRyhmä(_, "2"))
    val yhteistenTutkinnonOsienOsaSuoritukset = osasuoritukset.filter(isAmmatillisenTutkinnonOsanOsaalue)
    val muutSuoritukset = osasuoritukset.filter(tutkinnonOsanRyhmä(_, "1", "3", "4"))

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
      oppijaOid = opiskeluoikeus.oppijaOid,
      hetu = henkilö.flatMap(_.hetu),
      sukunimi = henkilö.map(_.sukunimi),
      etunimet = henkilö.map(_.etunimet),
      koulutusmoduulit = päätasonSuoritukset.map(_.koulutusmoduuliKoodiarvo).sorted.mkString(","),
      osaamisalat = if (osaamisalat.isEmpty) None else Some(osaamisalat.mkString(",")),
      tutkintonimikkeet = päätasonSuoritukset.flatMap(tutkintonimike(_)).map(_.get("fi")).mkString(","),
      päätasonSuoritustenTilat = Some(päätasonSuoritustenTilat(päätasonSuoritukset)),
      viimeisinOpiskeluoikeudenTila = aikajaksot.last.tila,
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
      valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus = yhteislaajuus(valinnaiset(yhteistenTutkinnonOsienOsaSuoritukset))
    )
  }

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String = {
    s"Ammatillinen_tutkinnon_osa_ja_osia_${oppilaitosOid}_${alku}_${loppu}"
  }

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp): String = ""

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String = {
    s"Ammatillinen_tutkinnon_osa_ja_osia_${oppilaitosOid}_${alku.toString.replaceAll("-","")}-${loppu.toString.replaceAll("-","")}.xlsx"
  }

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
    "koulutusmoduulit" -> Column("Tutkinnot"),
    "osaamisalat" -> Column("Osaamisalat"),
    "tutkintonimikkeet" -> Column("Tutkintonimike"),
    "päätasonSuoritustenTilat" -> Column("Suorituksen tila"),
    "viimeisinOpiskeluoikeudenTila" -> Column("Viimeisin opiskeluoikeuden tila"),
    "opintojenRahoitukset" -> Column("Rahoitukset"),
    "suoritettujenOpintojenYhteislaajuus" -> Column("Suorittetujen opintojen yhteislaajuus"),
    "valmiitAmmatillisetTutkinnonOsatLkm" -> Column("Valmiiden ammatillisten tutkinnon osien lukumäärä"),
    "näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm" -> Column("Valmiissa ammatillisissa tutkinnon osissa olevien näyttöjen lukumäärä"),
    "tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm" -> Column("Tunnustettujen tutkinnon osien osuus valmiista ammatilllisista tutkinnon osista"),
    "rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm" -> Column("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista ammatillisista tutkinnon osista"),
    "suoritetutAmmatillisetTutkinnonOsatYhteislaajuus" -> Column("Suoritettujen ammatillisten tutkinnon osien yhteislaajuus"),
    "valmiitYhteistenTutkinnonOsatLkm" -> Column("Valmiiden yhteisten tutkinnon osien lukumäärä"),
    "pakollisetYhteistenTutkinnonOsienOsaalueidenLkm" -> Column("Pakollisten yhteisten tutkinnon osien osa-alueiden lukumäärä"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm" -> Column("Valinnaisten yhteisten tutkinnon osien osa-alueiden lukumäärä"),
    "tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm" -> Column("Tunnustettujen tutkinnon osien osa-alueiden osuus valmiista yhteisten tutkinnon osien osa-alueista"),
    "rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm" -> Column("Rahoituksen piirissä olevien tutkinnon osien osa-aluiden osuus valmiista yhteisten tutkinnon osien osa-alueista"),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm" -> Column("Tunnustettujen tutkinnon osien osuus valmiista yhteisistä tutkinnon osista"),
    "rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm" -> Column("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista yhteisistä tutkinnon osista"),
    "suoritettujenYhteistenTutkinnonOsienYhteislaajuus" -> Column("Suoritettujen yhteisten tutkinnon osien yhteislaajuus"),
    "suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> Column("Suoritettujen yhteisten tutkinnon osien osa-alueiden yhteislaajuus"),
    "pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> Column("Pakollisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus" -> Column("Valinnaisten yhteisten tutkinnon osien osa-aluiden yhteislaajuus")
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
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: Option[String],
  etunimet: Option[String],
  koulutusmoduulit: String,
  osaamisalat: Option[String],
  tutkintonimikkeet: String,
  päätasonSuoritustenTilat: Option[String],
  viimeisinOpiskeluoikeudenTila: String,
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
  valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus: Double
)
