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

object SuoritustietojenTarkistus {

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

    val ammatillisetTutkinnonOsat = osasuoritukset.filter(_.suorituksenTyyppi == "ammatillisentutkinnonosa")
    val valmiitAmmatillisetTutkinnonOsat = getValmiitAmmatillisetTutkinnonOsat(päätasonSuoritukset, ammatillisetTutkinnonOsat)
    val yhteisetTutkinnonOsat = getYhteisetTutkinnonOsat(ammatillisetTutkinnonOsat)
    val yhteistenTutkinnonOsienOsaAlueet = osasuoritukset.filter(_.suorituksenTyyppi == "ammatillisentutkinnonosanosaalue")

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
      opiskeluoikeudenTila = Some(suorituksenTilaStr(päätasonSuoritukset)),
      suoritettujenOpintojenYhteislaajuus = yhteislaajuus(osasuoritukset),
      valmiitAmmatillisetTutkinnonOsatLkm = valmiitAmmatillisetTutkinnonOsat.size,
      pakollisetAmmatillisetTutkinnonOsatLkm = pakolliset(ammatillisetTutkinnonOsat).size,
      valinnaisetAmmatillisetTutkinnonOsatLkm = valinnaiset(ammatillisetTutkinnonOsat).size,
      näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm = näytöt(valmiitAmmatillisetTutkinnonOsat).size,
      tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm = tunnustetut(valmiitAmmatillisetTutkinnonOsat).size,
      rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm = (tunnustetut andThen rahoituksenPiirissä) (ammatillisetTutkinnonOsat).size,
      suoritetutAmmatillisetTutkinnonOsatYhteislaajuus = yhteislaajuus(ammatillisetTutkinnonOsat),
      pakollisetAmmatillisetTutkinnonOsatYhteislaajuus = (pakolliset andThen yhteislaajuus) (ammatillisetTutkinnonOsat),
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

  private def suorituksenTilaStr(päätasonsuoritukset: Seq[RPäätasonSuoritusRow]) = {
    päätasonsuoritukset.flatMap(ps => JsonSerializer.extract[Option[Koodistokoodiviite]](ps.data \ "tila")).map(_.nimi.getOrElse("fi", None)).mkString(",")
  }

  private def getYhteisetTutkinnonOsat(osasuoritukset: Seq[ROsasuoritusRow]) = {
    osasuoritukset.filter { os =>
      JsonSerializer.extract[Option[Koodistokoodiviite]](os.data \ "tutkinnonOsanRyhmä") match {
        case Some(viite) if (viite.koodiarvo == "2") => true
        case _ => false
      }
    }
  }

  private def getValmiitAmmatillisetTutkinnonOsat(päätasonsuoritukset: Seq[RPäätasonSuoritusRow], osasuoritukset: Seq[ROsasuoritusRow]) = {
    def sisältyyValmiiseenpäätasonSuoritukseen(os: ROsasuoritusRow) = päätasonsuoritukset.exists(ps => ps.päätasonSuoritusId == os.päätasonSuoritusId & ps.vahvistusPäivä.isDefined)

    osasuoritukset.filter(os => os.vahvistusPäivä.isDefined | sisältyyValmiiseenpäätasonSuoritukseen(os))
  }

  private val yhteislaajuus: Seq[ROsasuoritusRow] => Double = osasuoritukset => osasuoritukset.flatMap(_.koulutusmoduuliLaajuusArvo).sum

  private val vahvistuspäivälliset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(_.vahvistusPäivä.isDefined)

  private val tunnustetut: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isTunnustettu)

  private val valinnaiset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filterNot(isPakollinen)

  private val pakolliset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isPakollinen)

  private val näytöt: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isNäyttö)

  private val rahoituksenPiirissä: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isRahoituksenPiirissä)

  private def isTunnustettu(os: ROsasuoritusRow) = JsonSerializer.extract[Option[OsaamisenTunnustaminen]](os.data \ "tunnustettu").isDefined

  private def isPakollinen(os: ROsasuoritusRow) = os.koulutusmoduuliPakollinen.getOrElse(false)

  private def isNäyttö(os: ROsasuoritusRow) = JsonSerializer.extract[Option[Näyttö]](os.data \ "näyttö").isDefined

  private def isRahoituksenPiirissä(os: ROsasuoritusRow) = JsonSerializer.extract[Option[OsaamisenTunnustaminen]](os.data \ "tunnustettu").map(_.rahoituksenPiirissä).getOrElse(false)
}
