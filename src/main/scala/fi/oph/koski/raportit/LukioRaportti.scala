package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.Futures

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object LukioRaportti {

  private lazy val lukionoppiaineenoppimaara = "lukionoppiaineenoppimaara"
  private lazy val lukionoppiaine = "lukionoppiaine"
  private lazy val lukionmuuopinto = "lukionmuuopinto"

  def buildRaportti(repository: LukioRaportitRepository, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate): Seq[DynamicDataSheet] = {
    implicit val executionContext = ExecutionContext.fromExecutor(new java.util.concurrent.ForkJoinPool(10))

    val rows = repository.suoritustiedot(oppilaitosOid, alku, loppu)
    val oppiaineetJaKurssit = lukiossaOpetettavatOppiaineetJaNiidenKurssit(rows)

    val future = for {
      oppiaineJaLisätiedot <- oppiaineJaLisätiedotSheet(rows, oppiaineetJaKurssit, alku, loppu)
      kurssit <- kurssiSheets(rows, oppiaineetJaKurssit)
    } yield (oppiaineJaLisätiedot +: kurssit)

    Futures.await(future, atMost = 6.minutes)
  }

  private def lukiossaOpetettavatOppiaineetJaNiidenKurssit(rows: Seq[LukioRaporttiRows]) = {
    rows.flatMap(oppianeetJaNiidenKurssit).groupBy(_.oppiaine).map { case (oppiaine, x) =>
      LukioRaporttiOppiaineJaKurssit(oppiaine, x.flatMap(_.kurssit).distinct)
    }.toSeq
  }

  private def oppianeetJaNiidenKurssit(row: LukioRaporttiRows) = {
    val kurssit = row.osasuoritukset.filter(_.ylempiOsasuoritusId.isDefined).groupBy(_.ylempiOsasuoritusId.get)
    val oppiaineet = row.osasuoritukset.filter(isLukionOppiaine)
    val combineOppiaineWithKurssit = (oppiaine: ROsasuoritusRow) => LukioRaporttiOppiaineJaKurssit(toOppiaine(oppiaine), kurssit.getOrElse(oppiaine.osasuoritusId, Nil).map(toKurssi))

    oppiaineet.map(combineOppiaineWithKurssit)
  }

  private def isLukionOppiaine(osasuoritus: ROsasuoritusRow) = osasuoritus.suorituksenTyyppi == lukionoppiaine || osasuoritus.suorituksenTyyppi == lukionmuuopinto

  private def toOppiaine(row: ROsasuoritusRow) = LukioRaporttiOppiaine(row.suorituksestaKäytettäväNimi.getOrElse("ei nimeä"), row.koulutusmoduuliKoodiarvo, row.koulutusmoduuliPaikallinen)
  private def toKurssi(row: ROsasuoritusRow) = LukioRaporttiKurssi(row.koulutusmoduuliNimi.getOrElse("ei nimeä"), row.koulutusmoduuliKoodiarvo, row.koulutusmoduuliPaikallinen)

  private def oppiaineJaLisätiedotSheet(opiskeluoikeusData: Seq[LukioRaporttiRows], oppiaineetJaKurssit: Seq[LukioRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate)(implicit executionContext: ExecutionContextExecutor) = {
    Future {
      DynamicDataSheet(
        title = "Oppiaineet ja lisätiedot",
        rows = opiskeluoikeusData.map(oppiaineJaLisätiedotRow(_, oppiaineetJaKurssit, alku, loppu)),
        columnSettings = oppiaineJaLisätiedotColumns(oppiaineetJaKurssit)
      )
    }(executionContext)
  }

  private def oppiaineJaLisätiedotColumns(oppiaineetJaKurssit: Seq[LukioRaporttiOppiaineJaKurssit]) = {
    opiskeluoikeusColumns ++
      henkiloTietoColumns ++
      tilatietoColums ++
      opintojenSummaTiedotColumns ++
      oppilaitoksenOppiaineetColumns(oppiaineetJaKurssit) ++
      opiskeluoikeudenLisätiedotColums
  }

  private def oppiaineJaLisätiedotRow(data: LukioRaporttiRows, oppiaineetJaKurssit: Seq[LukioRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    val opiskeluoikeudenLisätiedot = JsonSerializer.extract[Option[LukionOpiskeluoikeudenLisätiedot]](data.opiskeluoikeus.data \ "lisätiedot")

    opiskeluoikeudentiedot(data.opiskeluoikeus, data.päätasonSuoritus) ++
      henkilotiedot(data.henkilo) ++
      tilatiedot(data.opiskeluoikeus, data.aikajaksot, data.päätasonSuoritus, alku, loppu) ++
      opintojenSummaTiedot(data.osasuoritukset) ++
      järjestettävienOppiaineidenTiedot(oppiaineetJaKurssit, data.päätasonSuoritus, data.osasuoritukset) ++
      opiskeluoikeudenLisätietojenTiedot(opiskeluoikeudenLisätiedot, alku, loppu)
  }

  private val opiskeluoikeusColumns = Seq(
    CompactColumn("Opiskeluoikeuden oid"),
    CompactColumn("Oppilaitoksen nimi"),
    CompactColumn("Lähdejärjestelmä"),
    CompactColumn("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
    CompactColumn("Koulutustoimija"),
    CompactColumn("Toimipiste")
  )

  private def opiskeluoikeudentiedot(oo: ROpiskeluoikeusRow, pts: RPäätasonSuoritusRow) = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](oo.data \ "lähdejärjestelmänId")

    Seq(
      oo.opiskeluoikeusOid,
      oo.oppilaitosNimi,
      lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId.flatMap(_.id),
      oo.koulutustoimijaNimi,
      pts.toimipisteNimi
    )
  }

  private val tilatietoColums = Seq(
    CompactColumn("Opiskeluoikeuden viimeisin tila"),
    CompactColumn("Opiskeluoikeuden tilat aikajakson aikana"),
    CompactColumn("Suorituksen tyyppi"),
    CompactColumn("Suorituksen tila"),
    CompactColumn("Suorituksen alkamispäivä"),
    CompactColumn("Suorituksen vahvistuspäivä"),
    CompactColumn("Läsnäolopäiviä aikajakson aikana"),
    CompactColumn("Rahoitukset"),
    CompactColumn("Ryhmä")
  )

  private def tilatiedot(oo: ROpiskeluoikeusRow, aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow], paatasonSuoritus: RPäätasonSuoritusRow, alku: LocalDate, loppu: LocalDate) = {
    Seq(
      oo.viimeisinTila,
      removeContinuousSameTila(aikajaksot).map(_.tila).mkString(","),
      paatasonSuoritus.suorituksenTyyppi,
      if (paatasonSuoritus.vahvistusPäivä.isDefined) "valmis" else "kesken",
      JsonSerializer.extract[Option[LocalDate]](paatasonSuoritus.data \ "alkamispäivä"),
      paatasonSuoritus.vahvistusPäivä.map(_.toLocalDate),
      aikajaksot.filter(_.tila == "lasna").map(j => Aikajakso(j.alku.toLocalDate, Some(j.loppu.toLocalDate))).map(lengthInDaysInDateRange(_, alku, loppu)).sum,
      aikajaksot.flatMap(_.opintojenRahoitus).mkString(","),
      JsonSerializer.extract[Option[String]](paatasonSuoritus.data \ "ryhmä")
    )
  }

  private val opiskeluoikeudenLisätiedotColums = Seq(
    CompactColumn("Pidennetty Päättymispäivä"),
    CompactColumn("Ulkomainen vaihto-opiskelija"),
    CompactColumn("Yksityisopiskelija"),
    CompactColumn("Ulkomaanajaksot"),
    CompactColumn("Erityisen koulutustehtävän tehtävät"),
    CompactColumn("Erityisen koulutustehtävän jaksot"),
    CompactColumn("Sisäoppilaitosmainen majoitus")
  )

  private def opiskeluoikeudenLisätietojenTiedot(lisatiedot: Option[LukionOpiskeluoikeudenLisätiedot], alku: LocalDate, loppu: LocalDate) = Seq(
    lisatiedot.exists(_.pidennettyPäättymispäivä),
    lisatiedot.exists(_.ulkomainenVaihtoopiskelija),
    lisatiedot.exists(_.yksityisopiskelija),
    lisatiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
    lisatiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.flatMap(_.tehtävä.nimi.map(_.get("fi"))).mkString(","))),
    lisatiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
    lisatiedot.flatMap(_.sisäoppilaitosmainenMajoitus.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum))
  )

  private val henkiloTietoColumns = Seq(
    Column("Oppijan oid"),
    Column("Hetu"),
    Column("Sukunimi"),
    Column("Etunimet")
  )

  private def henkilotiedot(henkilo: Option[RHenkilöRow]) = Seq(
    henkilo.map(_.oppijaOid),
    henkilo.flatMap(_.hetu),
    henkilo.map(_.sukunimi),
    henkilo.map(_.etunimet)
  )

  private def oppilaitoksenOppiaineetColumns(oppiaineetJaKurssit: Seq[LukioRaporttiOppiaineJaKurssit]) = {
    oppiaineetJaKurssit.map { o =>
      val oppiaine = o.oppiaine
      CompactColumn(s"${oppiaine.nimi} (${oppiaine.koulutusmoduuliKoodiarvo}) ${if (oppiaine.koulutusmoduuliPaikallinen) "paikallinen" else "valtakunnallinen"}")
    }
  }

  private val opintojenSummaTiedotColumns = Seq(
    CompactColumn("Yhteislaajuus")
  )

  private def opintojenSummaTiedot(osasuoritukset: Seq[ROsasuoritusRow]) = Seq(
    osasuoritukset.filter(_.suorituksenTyyppi == "lukionkurssi").flatMap(_.koulutusmoduuliLaajuusArvo.map(_.toDouble)).sum
  )

  private def järjestettävienOppiaineidenTiedot(oppiaineetJaKurssit: Seq[LukioRaporttiOppiaineJaKurssit], paatasonsuoritus: RPäätasonSuoritusRow, osasuoritukset: Seq[ROsasuoritusRow]) = {
    val oppiaineetTiedot = if (paatasonsuoritus.suorituksenTyyppi == lukionoppiaineenoppimaara) {
      (oppiaine: LukioRaporttiOppiaine) => if (paatasonsuoritus.matchesWith(oppiaine)) { paatasonsuoritusArvosanaLaajuus(paatasonsuoritus, osasuoritukset) } else { "" }
    } else {
      val osasuorituksetByKoodiarvo = osasuoritukset.groupBy(_.koulutusmoduuliKoodiarvo)
      val oppiaineenOsasuoritukset = (oppiaine: LukioRaporttiOppiaine) => osasuorituksetByKoodiarvo.getOrElse(oppiaine.koulutusmoduuliKoodiarvo, Nil).filter(_.matchesWith(oppiaine))
      (oppiaine: LukioRaporttiOppiaine) => oppiaineenOsasuoritukset(oppiaine).map(osasuoritusArvosanaLaajuus(osasuoritukset)).mkString(",")
    }

    oppiaineetJaKurssit.map(o => oppiaineetTiedot(o.oppiaine))
  }

  private def osasuoritusArvosanaLaajuus(osasuoritukset: Seq[ROsasuoritusRow])(osasuoritus: ROsasuoritusRow)= {
    val laajuus = osasuoritukset.count(_.ylempiOsasuoritusId.contains(osasuoritus.osasuoritusId))
    arvosanaLaajuus(osasuoritus.arviointiArvosanaKoodiarvo, laajuus)
  }

  private def paatasonsuoritusArvosanaLaajuus(paatasonsuoritus: RPäätasonSuoritusRow, osasuoritukset: Seq[ROsasuoritusRow]) = {
    arvosanaLaajuus(paatasonsuoritus.arviointiArvosanaKoodiarvo, osasuoritukset.size)
  }

  private def arvosanaLaajuus(arvosana: Option[String], laajuus: Int) = {
    s"${arvosana.map("Arvosana " + _).getOrElse("Ei arvosanaa")}, $laajuus ${if (laajuus == 1) "kurssi" else "kurssia"}"
  }

  private[raportit] def removeContinuousSameTila(aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow]): Seq[ROpiskeluoikeusAikajaksoRow] = {
    if (aikajaksot.size < 2) {
      aikajaksot
    } else {
      val rest = aikajaksot.dropWhile(_.tila == aikajaksot.head.tila)
      aikajaksot.head +: removeContinuousSameTila(rest)
    }
  }

  private[raportit] def lengthInDaysInDateRange(jakso: Jakso, alku: LocalDate, loppu: LocalDate) = {
    val hakuvali = Aikajakso(alku, Some(loppu))
    if (jakso.overlaps(hakuvali)) {
      val start = if (jakso.alku.isBefore(alku)) alku else jakso.alku
      val end = if (jakso.loppu.exists(_.isBefore(loppu))) jakso.loppu.get else loppu
      ChronoUnit.DAYS.between(start, end).toInt + 1
    } else {
      0
    }
  }

  private def kurssitColumnSettings(kurssit: Seq[LukioRaporttiKurssi]) = {
    kurssit.map(_.toColumnTitle).map(CompactColumn(_))
  }

  private def kurssiSheets(data: Seq[LukioRaporttiRows], suoritusData: Seq[LukioRaporttiOppiaineJaKurssit])(implicit executionContext: ExecutionContextExecutor) = {
    Future {
      suoritusData.par.map(kurssiSheet(_, data)).seq.sortBy(_.title)
    }(executionContext)
  }

  private def kurssiSheet(oppiaineJaKurssit: LukioRaporttiOppiaineJaKurssit, data: Seq[LukioRaporttiRows]) = {
    val oppiaine = oppiaineJaKurssit.oppiaine
    val kurssit = oppiaineJaKurssit.kurssit
    val filtered = data.filter(notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine))

    DynamicDataSheet(
      title = oppiaine.toSheetTitle,
      rows = filtered.map(kurssiSheetRow(_, kurssit)),
      columnSettings = henkiloTietoColumns ++ kurssitColumnSettings(kurssit)
    )
  }

  private def notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine: LukioRaporttiOppiaine)(data: LukioRaporttiRows) = {
    data.päätasonSuoritus.suorituksenTyyppi != lukionoppiaineenoppimaara || data.päätasonSuoritus.matchesWith(oppiaine)
  }

  private def kurssiSheetRow(data: LukioRaporttiRows, kurssit: Seq[LukioRaporttiKurssi]) = {
    henkilotiedot(data.henkilo) ++
      kurssienTiedot(kurssit, data)
  }

  private def kurssienTiedot(kurssit: Seq[LukioRaporttiKurssi], data: LukioRaporttiRows) = {
    val osasuoritukset = data.osasuoritukset.groupBy(_.koulutusmoduuliKoodiarvo)
    kurssit.map { kurssi =>
      osasuoritukset.getOrElse(kurssi.koulutusmoduuliKoodiarvo, Nil).filter(_.matchesWith(kurssi)).map(kurssitiedot).mkString(",")
    }
  }

  private def kurssitiedot(osasuoritus: ROsasuoritusRow) = {
    val arvosana = osasuoritus.arviointiArvosanaKoodiarvo.map("Arvosana " + _) getOrElse ("Ei arvosanaa")
    val laajuus = osasuoritus.koulutusmoduuliLaajuusArvo.map("Laajuus " + _).getOrElse("Ei laajuutta")
    val kurssityyppi = JsonSerializer.extract[Option[Koodistokoodiviite]](osasuoritus.data \ "koulutusmoduuli" \ "kurssinTyyppi").map(_.koodiarvo).getOrElse("Ei tyyppiä")
    val isTunnustettu = JsonSerializer.extract[Option[OsaamisenTunnustaminen]](osasuoritus.data \ "tunnustettu").isDefined
    val tunnustettu = if (isTunnustettu) ",tunnustettu" else ""

    s"${arvosana},${laajuus},${kurssityyppi}${tunnustettu}"
  }
}

sealed trait LukioRaporttiOppiaineTaiKurssi {
  def nimi: String
  def koulutusmoduuliKoodiarvo: String
  def koulutusmoduuliPaikallinen: Boolean
}

case class LukioRaporttiOppiaine(nimi: String, koulutusmoduuliKoodiarvo: String, koulutusmoduuliPaikallinen: Boolean) extends LukioRaporttiOppiaineTaiKurssi {
  def toSheetTitle: String = s"$koulutusmoduuliKoodiarvo ${if (koulutusmoduuliPaikallinen) "p" else "v"} $nimi"
}

case class LukioRaporttiKurssi(nimi: String, koulutusmoduuliKoodiarvo: String, koulutusmoduuliPaikallinen: Boolean) extends LukioRaporttiOppiaineTaiKurssi {
  def toColumnTitle: String = s"$nimi $koulutusmoduuliKoodiarvo ${if (koulutusmoduuliPaikallinen) "paikallinen" else "valtakunnallinen"}"
}

case class LukioRaporttiOppiaineJaKurssit(oppiaine: LukioRaporttiOppiaine, kurssit: Seq[LukioRaporttiKurssi])
