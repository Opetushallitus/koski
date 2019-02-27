package fi.oph.koski.raportit

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}

case class SuoritustiedotTarkistusRow
(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  sisältyyOpiskeluoikeuteenOid: String,
  sisältyvätOpiskeluoikeudetOidit: String,
  sisältyvätOpiskeluoikeudetOppilaitokset: String,
  aikaleima: LocalDate,
  toimipisteOidit: String,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: Option[String],
  etunimet: Option[String],
  koulutusmoduulit: String,
  osaamisalat: Option[String],
  koulutusmuoto: String,
  opiskeluoikeudenTila: Option[String],
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
  suoritettujenYhteistenTutkinnonOsienYhteislaajuus: Double,
  pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus: Double,
  valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus: Double
)

// scalastyle:off method.length

object SuoritustietojenTarkistus extends AikajaksoRaportti {

  def buildRaportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate) = {
    val result: Seq[(ROpiskeluoikeusRow, Option[RHenkilöRow], List[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow])] = raportointiDatabase.suoritustiedotAikajaksot(oppilaitosOid, OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo, alku, loppu)
    val rows = result.map(buildRow(alku, loppu, _))
    rows
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "lähdejärjestelmä" -> Column("Lähdejärjestelmä", width = Some(4000)),
    "lähdejärjestelmänId" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä", width = Some(4000)),
    "sisältyyOpiskeluoikeuteenOid" -> Column("Sisältyy opiskeluoikeuteen", width = Some(4000)),
    "sisältyvätOpiskeluoikeudetOidit" -> Column("Sisältyvät opiskeluoikeudet", width = Some(4000)),
    "sisältyvätOpiskeluoikeudetOppilaitokset" -> Column("Sisältyvien opiskeluoikeuksien oppilaitokset", width = Some(4000)),
    "aikaleima" -> Column("Päivitetty"),
    "toimipisteOidit" -> Column("Toimipisteet"),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("Hetu"),
    "sukunimi" -> Column("Sukunimi"),
    "etunimet" -> Column("Etunimet"),
    "koulutusmoduulit" -> Column("Tutkinnot"),
    "osaamisalat" -> Column("Osaamisalat"),
    "koulutusmuoto" -> Column("Tutkintonimike"),
    "opiskeluoikeudenTila" -> Column("Suorituksen tila"),
    "suoritettujenOpintojenYhteislaajuus" -> Column("Suoritettujen opintojen yhteislaajuus"),
    "valmiitAmmatillisetTutkinnonOsatLkm" -> Column("Valmiiden ammatillisten tutkinnon osien lukumäärä"),
    "pakollisetAmmatillisetTutkinnonOsatLkm" -> Column("Pakollisten tutkinnon osien lukumäärä"),
    "valinnaisetAmmatillisetTutkinnonOsatLkm" -> Column("Valinnaisten tutkinon osien lukumäärä"),
    "näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm" -> Column("Valmiissa tutkinnon osissa olevien näyttöjen lukumäärä"),
    "tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm" -> Column("Tunnustettujen tutkinnon osien osuus valmiista tutkinnon osista"),
    "rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm" -> Column("Rahoituksen piirissä olvien tutkinnon osien osuus tunnustetuista tutkinnon osista"),
    "suoritetutAmmatillisetTutkinnonOsatYhteislaajuus" -> Column("Suoritettujen ammatillisten tutkinnon osien yhteislaajuus"),
    "pakollisetAmmatillisetTutkinnonOsatYhteislaajuus" -> Column("Pakollisten ammatillisten tutkinnon osien yhteislaajuus"),
    "valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus" -> Column("Valinnaisten ammatillisten tutkinnon osien yhteislaajuus"),
    "valmiitYhteistenTutkinnonOsatLkm" -> Column("Valmiiden yhteisten tutkinnon osien lukumäärä"),
    "pakollisetYhteistenTutkinnonOsienOsaalueidenLkm" -> Column("Pakollisten yhteisten tutkinnon osien osa-alueiden lukumäärä"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm" -> Column("Valinnaisten yhteisten tutkinnon osien osa-aluiden lukumäärä"),
    "tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm" -> Column("Tunnustettujen tutkinnon osien osa-alueiden osuus valmiista tutkinnon osan osa-alueista"),
    "rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm" -> Column("Rahoituksen piirissä olevien tutkinnon osien osa-aluiden osuus valmiista tutkinnon osan osa-alueista"),
    "suoritettujenYhteistenTutkinnonOsienYhteislaajuus" -> Column("Suoritettujen yhteisten tutkinnon osien yhteislaajuus"),
    "pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> Column("Pakollisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus" -> Column("Valinnaisten yhteisten tutkinnon osien osa-aluiden yhteislaajuus")
  )

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String =
    s"opiskelijavuositiedot_${oppilaitosOid}_${alku.toString.replaceAll("-", "")}-${loppu.toString.replaceAll("-", "")}.xlsx"

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String =
    s"Opiskelijavuositiedot $oppilaitosOid ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}"

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp): String =
    s"""
       |Opiskelijavuositiedot (ammatillinen koulutus)
       |Oppilaitos: $oppilaitosOid
       |Aikajakso: ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}
       |Raportti luotu: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadCompleted.toLocalDateTime)} tietojen pohjalta)
     """.stripMargin.trim.stripPrefix("\n").stripSuffix("\n")

  private def buildRow(alku: LocalDate, loppu: LocalDate, data: (ROpiskeluoikeusRow, Option[RHenkilöRow], List[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow])) = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonSuoritukset, sisältyvätOpiskeluoikeudet, osasuoritukset) = data
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    val osaamisalat = päätasonSuoritukset
      .flatMap(s => JsonSerializer.extract[Option[List[Osaamisalajakso]]](s.data \ "osaamisala"))
      .flatten
      .filterNot(j => j.alku.exists(_.isAfter(loppu)))
      .filterNot(j => j.loppu.exists(_.isBefore(alku)))
      .map(_.osaamisala.koodiarvo)
      .sorted
      .distinct

    val ammatillisetTutkinnonOsat = osasuoritukset.filter(isAmmatillinenTutkinnonOsa)
    val valmiitAmmatillisetTutkinnonOsat = ammatillisetTutkinnonOsat.filter(os => os.vahvistusPäivä.isDefined | sisältyyVahvistettuunPäätasonSuoritukseen(os, päätasonSuoritukset))
    val yhteisetTutkinnonOsat = osasuoritukset.filter(isYhteinenTutkinnonOsa)
    val yhteistenTutkinnonOsienOsaAlueet = osasuoritukset.filter(isYhteinenTutkinnonOsanOsaalue)

    SuoritustiedotTarkistusRow(
      opiskeluoikeusOid = opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
      sisältyyOpiskeluoikeuteenOid = opiskeluoikeus.sisältyyOpiskeluoikeuteenOid.getOrElse(""),
      sisältyvätOpiskeluoikeudetOidit = sisältyvätOpiskeluoikeudet.map(_.opiskeluoikeusOid).mkString(","),
      sisältyvätOpiskeluoikeudetOppilaitokset = sisältyvätOpiskeluoikeudet.map(_.oppilaitosNimi).mkString(","),
      aikaleima = opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
      toimipisteOidit = päätasonSuoritukset.map(_.toimipisteOid).sorted.distinct.mkString(","),
      oppijaOid = opiskeluoikeus.oppijaOid,
      hetu = henkilö.flatMap(_.hetu),
      sukunimi = henkilö.map(_.sukunimi),
      etunimet = henkilö.map(_.etunimet),
      koulutusmoduulit = päätasonSuoritukset.map(_.koulutusmoduuliKoodiarvo).sorted.mkString(","),
      osaamisalat = if (osaamisalat.isEmpty) None else Some(osaamisalat.mkString(",")),
      koulutusmuoto = opiskeluoikeus.koulutusmuoto,
      opiskeluoikeudenTila = Some(päätasonSuoritustenTilat(päätasonSuoritukset)),
      suoritettujenOpintojenYhteislaajuus = (dropYhteisetTutkinnonOsat andThen yhteislaajuus)(osasuoritukset),
      valmiitAmmatillisetTutkinnonOsatLkm = valmiitAmmatillisetTutkinnonOsat.size,
      pakollisetAmmatillisetTutkinnonOsatLkm = pakolliset(ammatillisetTutkinnonOsat).size,
      valinnaisetAmmatillisetTutkinnonOsatLkm = valinnaiset(ammatillisetTutkinnonOsat).size,
      näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm = näytöt(valmiitAmmatillisetTutkinnonOsat).size,
      tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm = tunnustetut(valmiitAmmatillisetTutkinnonOsat).size,
      rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm = (tunnustetut andThen rahoituksenPiirissä)(ammatillisetTutkinnonOsat).size,
      suoritetutAmmatillisetTutkinnonOsatYhteislaajuus = yhteislaajuus(ammatillisetTutkinnonOsat),
      pakollisetAmmatillisetTutkinnonOsatYhteislaajuus = (pakolliset andThen yhteislaajuus)(ammatillisetTutkinnonOsat),
      valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus = (valinnaiset andThen yhteislaajuus) (ammatillisetTutkinnonOsat),
      valmiitYhteistenTutkinnonOsatLkm = vahvistuspäivälliset(yhteisetTutkinnonOsat).size,
      pakollisetYhteistenTutkinnonOsienOsaalueidenLkm = pakolliset(yhteistenTutkinnonOsienOsaAlueet).size,
      valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm = valinnaiset(yhteistenTutkinnonOsienOsaAlueet).size,
      tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm = (vahvistuspäivälliset andThen tunnustetut) (yhteistenTutkinnonOsienOsaAlueet).size,
      rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm = (vahvistuspäivälliset andThen rahoituksenPiirissä) (yhteistenTutkinnonOsienOsaAlueet).size,
      suoritettujenYhteistenTutkinnonOsienYhteislaajuus = yhteislaajuus(yhteisetTutkinnonOsat),
      pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = (pakolliset andThen yhteislaajuus) (yhteistenTutkinnonOsienOsaAlueet),
      valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus = (valinnaiset andThen yhteislaajuus) (yhteistenTutkinnonOsienOsaAlueet)
    )
  }

  private def päätasonSuoritustenTilat(päätasonsuoritukset: Seq[RPäätasonSuoritusRow]) = {
    päätasonsuoritukset.flatMap(ps => JsonSerializer.extract[Option[Koodistokoodiviite]](ps.data \ "tila")).map(_.nimi.getOrElse("fi", None)).mkString(",")
  }

  private val sisältyyVahvistettuunPäätasonSuoritukseen: (ROsasuoritusRow, Seq[RPäätasonSuoritusRow]) => Boolean = (os, pss) => pss.exists(ps => ps.päätasonSuoritusId == os.päätasonSuoritusId & ps.vahvistusPäivä.isDefined)

  private val isAmmatillinenTutkinnonOsa: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillisentutkinnonosa"

  private val yhteislaajuus: Seq[ROsasuoritusRow] => Double = osasuoritukset => osasuoritukset.flatMap(_.koulutusmoduuliLaajuusArvo).sum

  private val vahvistuspäivälliset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(_.vahvistusPäivä.isDefined)

  private val tunnustetut: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isTunnustettu)

  private val isTunnustettu: ROsasuoritusRow => Boolean = osasuoritus =>  JsonSerializer.extract[Option[OsaamisenTunnustaminen]](osasuoritus.data \ "tunnustettu").isDefined

  private val valinnaiset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filterNot(isPakollinen)

  private val pakolliset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isPakollinen)

  private val isPakollinen: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.koulutusmoduuliPakollinen.getOrElse(false)

  private val näytöt: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isNäyttö)

  private val isNäyttö: ROsasuoritusRow => Boolean = osasuoritus => JsonSerializer.extract[Option[Näyttö]](osasuoritus.data \ "näyttö").isDefined

  private val rahoituksenPiirissä: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isRahoituksenPiirissä)

  private val isRahoituksenPiirissä: ROsasuoritusRow => Boolean = osasuoritus => JsonSerializer.extract[Option[OsaamisenTunnustaminen]](osasuoritus.data \ "tunnustettu").map(_.rahoituksenPiirissä).getOrElse(false)

  private val dropYhteisetTutkinnonOsat: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filterNot(isYhteinenTutkinnonOsa)

  private val yhteisetTutkinnonOsaKoodit = Seq("101053", "101054", "101055", "101056", "400012", "400013", "400014")

  private val isYhteinenTutkinnonOsa: ROsasuoritusRow => Boolean = osasuoritus => yhteisetTutkinnonOsaKoodit.exists(_ == osasuoritus.koulutusmoduuliKoodiarvo)

  private val isYhteinenTutkinnonOsanOsaalue: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillisentutkinnonosanosaalue"
}
