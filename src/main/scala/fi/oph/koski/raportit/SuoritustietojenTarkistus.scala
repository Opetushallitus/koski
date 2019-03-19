package fi.oph.koski.raportit

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}


// scalastyle:off method.length

object SuoritustietojenTarkistus extends AikajaksoRaportti {

  def buildRaportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate) = {
    val result: Seq[(ROpiskeluoikeusRow, Option[RHenkilöRow], List[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow])] = raportointiDatabase.suoritustiedotAikajaksot(oppilaitosOid, OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo, alku, loppu)
    val rows = result.map(buildRow(oppilaitosOid, alku, loppu, _))
    rows
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
    "koulutusmuoto" -> Column("Tutkintonimike"),
    "päätasonSuoritustenTilat" -> Column("Suorituksen tila"),
    "opintojenRahoitukset" -> Column("Rahoitukset"),
    "painotettuKeskiarvo" -> Column("Painotettu keskiarvo"),
    "suoritettujenOpintojenYhteislaajuus" -> Column("Suoritettujen opintojen yhteislaajuus"),
    "valmiitAmmatillisetTutkinnonOsatLkm" -> Column("Valmiiden ammatillisten tutkinnon osien lukumäärä"),
    "pakollisetAmmatillisetTutkinnonOsatLkm" -> Column("Pakollisten ammatillisten tutkinnon osien lukumäärä"),
    "valinnaisetAmmatillisetTutkinnonOsatLkm" -> Column("Valinnaisten ammatillisten tutkinnon osien lukumäärä"),
    "näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm" -> Column("Valmiissa ammatillisissa tutkinnon osissa olevien näyttöjen lukumäärä"),
    "tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm" -> Column("Tunnustettujen tutkinnon osien osuus valmiista ammatillisista tutkinnon osista"),
    "rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm" -> Column("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista ammatillisista tutkinnon osista"),
    "suoritetutAmmatillisetTutkinnonOsatYhteislaajuus" -> Column("Suoritettujen ammatillisten tutkinnon osien yhteislaajuus"),
    "pakollisetAmmatillisetTutkinnonOsatYhteislaajuus" -> Column("Pakollisten ammatillisten tutkinnon osien yhteislaajuus"),
    "valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus" -> Column("Valinnaisten ammatillisten tutkinnon osien yhteislaajuus"),
    "valmiitYhteistenTutkinnonOsatLkm" -> Column("Valmiiden yhteisten tutkinnon osien lukumäärä"),
    "pakollisetYhteistenTutkinnonOsienOsaalueidenLkm" -> Column("Pakollisten yhteisten tutkinnon osien osa-alueiden lukumäärä"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm" -> Column("Valinnaisten yhteisten tutkinnon osien osa-aluiden lukumäärä"),
    "tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm" -> Column("Tunnustettujen tutkinnon osien osa-alueiden osuus valmiista yhteisten tutkinnon osien osa-alueista"),
    "rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm" -> Column("Rahoituksen piirissä olevien tutkinnon osien osa-aluiden osuus valmiista yhteisten tutkinnon osien osa-alueista"),
    "tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm" -> Column("Tunnustettujen tutkinnon osien osuus valmiista yhteisistä tutkinnon osista"),
    "rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm" -> Column("Rahoituksen piirissä olevien tutkinnon osien osuus tunnustetuista yhteisistä tutkinnon osista"),
    "suoritettujenYhteistenTutkinnonOsienYhteislaajuus" -> Column("Suoritettujen yhteisten tutkinnon osien yhteislaajuus"),
    "suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> Column("Suoritettujen yhteisten tutkinnon osien osa-alueiden yhteislaajuus"),
    "pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus" -> Column("Pakollisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus"),
    "valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus" -> Column("Valinnaisten yhteisten tutkinnon osien osa-aluiden yhteislaajuus")
  )

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String =
    s"suoritustiedot_${oppilaitosOid}_${alku.toString.replaceAll("-", "")}-${loppu.toString.replaceAll("-", "")}.xlsx"

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String =
    s"Suoritustiedot $oppilaitosOid ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}"

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp): String =
    s"""
       |Suroritustietojen tarkistus (ammatillinen koulutus)
       |Oppilaitos: $oppilaitosOid
       |Aikajakso: ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}
       |Raportti luotu: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadCompleted.toLocalDateTime)} tietojen pohjalta)
     """.stripMargin.trim.stripPrefix("\n").stripSuffix("\n")

  private def buildRow(oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, data: (ROpiskeluoikeusRow, Option[RHenkilöRow], List[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow], Seq[ROsasuoritusRow])) = {
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

    val ammatillisetTutkinnonOsatJaOsasuoritukset = ammatillisetTutkinnonOsatJaOsasuorituksetFrom(osasuoritukset)
    val valmiitAmmatillisetTutkinnonOsatJaOsasuoritukset = ammatillisetTutkinnonOsatJaOsasuoritukset.filter(os => isVahvistusPäivällinen(os) || isArvioinniton(os) || sisältyyVahvistettuunPäätasonSuoritukseen(os, päätasonSuoritukset))
    val yhteisetTutkinnonOsat = osasuoritukset.filter(isYhteinenTutkinnonOsa)
    val yhteistenTutkinnonOsienOsaAlueet = osasuoritukset.filter(isYhteinenTutkinnonOsanOsaalue(_, osasuoritukset))

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
      koulutusmuoto = opiskeluoikeus.koulutusmuoto,
      päätasonSuoritustenTilat = Some(päätasonSuoritustenTilat(päätasonSuoritukset)),
      opintojenRahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).sorted.distinct.mkString(","),
      painotettuKeskiarvo = päätasonSuoritukset.flatMap(ps => JsonSerializer.extract[Option[Float]](ps.data \ "keskiarvo")).mkString(","),
      suoritettujenOpintojenYhteislaajuus = yhteislaajuus(ammatillisetTutkinnonOsatJaOsasuoritukset.union(yhteistenTutkinnonOsienOsaAlueet)),
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
      valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus = yhteislaajuus(valinnaiset(yhteistenTutkinnonOsienOsaAlueet))
    )
  }

  private def päätasonSuoritustenTilat(päätasonsuoritukset: Seq[RPäätasonSuoritusRow]) = {
    päätasonsuoritukset.map(ps => vahvistusPäivätToTila(ps.vahvistusPäivä)).mkString(",")
  }

  private def vahvistusPäivätToTila(vahvistusPäivä: Option[Date]) = vahvistusPäivä match {
      case Some(päivä) if isTulevaisuudessa(päivä) =>  s"Kesken, Valmistuu ${päivä.toLocalDate}"
      case Some(_) =>  "Valmis"
      case _ => "Kesken"
    }

  private def isAnyOf(osasuoritus: ROsasuoritusRow, fs: (ROsasuoritusRow => Boolean)*) = fs.exists(f => f(osasuoritus))

  private val tutkinnonOsanRyhmä: (ROsasuoritusRow, String) => Boolean = (osasuoritus, koodiarvo) => JsonSerializer.extract[Option[Koodistokoodiviite]](osasuoritus.data \ "tutkinnonOsanRyhmä") match {
    case Some(viite) => viite.koodiarvo == koodiarvo
    case _ => false
  }

  private val sisältyyVahvistettuunPäätasonSuoritukseen: (ROsasuoritusRow, Seq[RPäätasonSuoritusRow]) => Boolean = (os, pss) => pss.exists(ps => ps.päätasonSuoritusId == os.päätasonSuoritusId & ps.vahvistusPäivä.isDefined)

  private val isAmmatillisenLukioOpintoja: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillinenlukionopintoja"

  private val isAmmatillinenMuitaOpintoValmiuksiaTukeviaOpintoja: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillinenmuitaopintovalmiuksiatukeviaopintoja"

  private val isAmmatillisenKorkeakouluOpintoja: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillinenkorkeakouluopintoja"

  private val isAmmatillinenTutkinnonOsaaPienempiKokonaisuus: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillisentutkinnonosaapienempikokonaisuus"

  private val yhteislaajuus: Seq[ROsasuoritusRow] => Double = osasuoritukset => osasuoritukset.flatMap(_.koulutusmoduuliLaajuusArvo).sum

  private val isVahvistusPäivällinen: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.vahvistusPäivä.isDefined

  private val tunnustetut: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isTunnustettu)

  private val isTunnustettu: ROsasuoritusRow => Boolean = osasuoritus => JsonSerializer.extract[Option[OsaamisenTunnustaminen]](osasuoritus.data \ "tunnustettu").isDefined

  private val valinnaiset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filterNot(isPakollinen)

  private val pakolliset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isPakollinen)

  private val isPakollinen: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.koulutusmoduuliPakollinen.getOrElse(false)

  private val näytöt: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isNäyttö)

  private val isNäyttö: ROsasuoritusRow => Boolean = osasuoritus => JsonSerializer.extract[Option[Näyttö]](osasuoritus.data \ "näyttö").isDefined

  private val rahoituksenPiirissä: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isRahoituksenPiirissä)

  private val isRahoituksenPiirissä: ROsasuoritusRow => Boolean = osasuoritus => JsonSerializer.extract[Option[OsaamisenTunnustaminen]](osasuoritus.data \ "tunnustettu").exists(_.rahoituksenPiirissä)

  private val isYhteinenTutkinnonOsa: ROsasuoritusRow => Boolean = osasuoritus => tutkinnonOsanRyhmä(osasuoritus, "2")

  private val isAmmatillisenTutkinnonOsanOsaalue: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillisentutkinnonosanosaalue"

  private val osasuorituksenaOsissa: (ROsasuoritusRow, Seq[ROsasuoritusRow]) => Boolean = (osasuoritus, osat) => osat.exists(osa => osasuoritus.ylempiOsasuoritusId.contains(osa.osasuoritusId))

  private val isYhteinenTutkinnonOsanOsaalue: (ROsasuoritusRow, Seq[ROsasuoritusRow]) => Boolean = (osasuoritus, osasuoritukset) => {
    val osat = osasuoritukset.filter(isYhteinenTutkinnonOsa)
    isAmmatillisenTutkinnonOsanOsaalue(osasuoritus) & osasuorituksenaOsissa(osasuoritus, osat)
  }

  private val isAmmatillisenYhteisenTutkinnonOsienOsaalue: (ROsasuoritusRow, Seq[ROsasuoritusRow]) => Boolean = (osasuoritus, osasuoritukset) => {
    val osat = osasuoritukset.filter(os => os.suorituksenTyyppi == "ammatillisentutkinnonosa" & !isYhteinenTutkinnonOsa(os))
    isAmmatillisenTutkinnonOsanOsaalue(osasuoritus) & osasuorituksenaOsissa(osasuoritus, osat)
  }

  private val isArvioinniton: ROsasuoritusRow => Boolean = osasuoritus => isAnyOf(osasuoritus,
    isAmmatillisenLukioOpintoja,
    isAmmatillisenKorkeakouluOpintoja,
    isAmmatillinenMuitaOpintoValmiuksiaTukeviaOpintoja,
    isAmmatillinenTutkinnonOsaaPienempiKokonaisuus,
    isAmmatillisenTutkinnonOsanOsaalue
  )

  private val tutkinnonosatvalinnanmahdollisuusKoodiarvot = Seq("1,", "2")

  private val isAmmatillisenTutkinnonOsa: ROsasuoritusRow => Boolean = osasuoritus => {
    !tutkinnonosatvalinnanmahdollisuusKoodiarvot.contains(osasuoritus.koulutusmoduuliKoodiarvo) &&
      osasuoritus.suorituksenTyyppi == "ammatillisentutkinnonosa" &&
      tutkinnonOsanRyhmä(osasuoritus, "1")
  }

  private def ammatillisetTutkinnonOsatJaOsasuorituksetFrom(osasuoritukset: Seq[ROsasuoritusRow]) = {
    osasuoritukset.filter(os =>
      isAnyOf(os,
        isAmmatillisenLukioOpintoja,
        isAmmatillisenKorkeakouluOpintoja,
        isAmmatillinenMuitaOpintoValmiuksiaTukeviaOpintoja,
        isAmmatillinenTutkinnonOsaaPienempiKokonaisuus,
        isAmmatillisenTutkinnonOsa,
        isAmmatillisenYhteisenTutkinnonOsienOsaalue(_, osasuoritukset)
      ))
  }

  private def isTulevaisuudessa(date: Date) = date.toLocalDate.isAfter(LocalDate.now())
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
  koulutusmuoto: String,
  päätasonSuoritustenTilat: Option[String],
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
  valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus: Double
)
