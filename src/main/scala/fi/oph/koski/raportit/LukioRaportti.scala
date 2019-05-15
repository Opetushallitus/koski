package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import org.json4s.JValue

object LukioRaportti {

  private type OppiaineenNimi = String
  private type Koulutusmooduuli_koodiarvo = String
  private type Paikallinen = Boolean

  def buildRaportti(repository: LukioRaportitRepository, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate): DynamicDataSheet = {
    val opiskeluoikeusData = repository.suoritustiedot(oppilaitosOid, alku, loppu)
    val oppilaitoksenOppiaineet = repository.oppilaitoksessaOpetettavatOppiaineet(oppilaitosOid)

    val columnSettings = makeColumnSettings(oppilaitoksenOppiaineet)
    val rows = opiskeluoikeusData.par.map(makeDataRow(_, oppilaitoksenOppiaineet, alku, loppu)).seq

    DynamicDataSheet("Suoritustiedot", rows, columnSettings)
  }

  private def makeColumnSettings(mahdollisetOppiaineet: Seq[(OppiaineenNimi, Koulutusmooduuli_koodiarvo, Paikallinen)]) = {
    opiskeluoikeusColumns ++
      henkiloTietoColumns ++
      tilatietoColums ++
      opintojenSummaTiedotColumns ++
      oppilaitoksenOppiaineetColumns(mahdollisetOppiaineet) ++
      opiskeluoikeudenLisätiedotColums
  }

  private def makeDataRow
  (
    data: (ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow]),
    mahdollisetOppiaineet: Seq[(OppiaineenNimi, Koulutusmooduuli_koodiarvo, Paikallinen)],
    alku: LocalDate, loppu: LocalDate
  ) = {
    val (opiskeluoikeus, henkilo, aikajaksot, paatasonsuoritus, osasuoritukset) = data
    val opiskeluoikeudenLisätiedot = JsonSerializer.extract[Option[LukionOpiskeluoikeudenLisätiedot]](opiskeluoikeus.data \ "lisätiedot")

    opiskeluoikeudentiedot(opiskeluoikeus) ++
      henkilotiedot(henkilo) ++
      tilatiedot(opiskeluoikeus, aikajaksot, paatasonsuoritus, alku, loppu) ++
      opintojenSummaTiedot(osasuoritukset) ++
      järjestettävienOppiaineidenTiedot(mahdollisetOppiaineet, paatasonsuoritus, osasuoritukset) ++
      opiskeluoikeudenLisätietojenTiedot(opiskeluoikeudenLisätiedot, alku, loppu)
  }

  private val opiskeluoikeusColumns = Seq(
    CompactColumn("Opiskeluoikeuden oid"),
    CompactColumn("Oppilaitoksen nimi"),
    CompactColumn("Lähdejärjestelmä"),
    CompactColumn("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
    Column("Oppijan oid")
  )

  private def opiskeluoikeudentiedot(oo: ROpiskeluoikeusRow) = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](oo.data \ "lähdejärjestelmänId")

    Seq(
      oo.opiskeluoikeusOid,
      oo.oppilaitosNimi,
      lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId.flatMap(_.id),
      oo.oppijaOid
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
    CompactColumn("Sisäoppilaitosmainen majoitus"),
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
    Column("Hetu"),
    Column("Sukunimi"),
    Column("Etunimet")
  )

  private def henkilotiedot(henkilo: Option[RHenkilöRow]) = Seq(
    henkilo.flatMap(_.hetu),
    henkilo.map(_.sukunimi),
    henkilo.map(_.etunimet)
  )

  private def oppilaitoksenOppiaineetColumns(mahdollisetOppiainee: Seq[(OppiaineenNimi, Koulutusmooduuli_koodiarvo, Paikallinen)]) = {
    mahdollisetOppiainee.map { case (nimi, koodiarvo, paikallinen) =>
      CompactColumn(s"${nimi} (${koodiarvo}) ${if (paikallinen) "paikallinen" else "valtakunnallinen"}")
    }
  }

  private val opintojenSummaTiedotColumns = Seq(
    CompactColumn("Yhteislaajuus")
  )

  private def opintojenSummaTiedot(osasuoritukset: Seq[ROsasuoritusRow]) = Seq(
    osasuoritukset.filter(_.suorituksenTyyppi == "lukionkurssi").flatMap(_.koulutusmoduuliLaajuusArvo.map(_.toDouble)).sum
  )

  private def järjestettävienOppiaineidenTiedot(mahdollisetOppiaineet: Seq[(OppiaineenNimi, Koulutusmooduuli_koodiarvo, Paikallinen)], paatasonsuoritus: RPäätasonSuoritusRow, osasuoritukset: Seq[ROsasuoritusRow]) = {
    if (paatasonsuoritus.suorituksenTyyppi == "lukionoppiaineenoppimaara") {
      mahdollisetOppiaineet.map(oppiaine => if (matchingOppiaine(Left(paatasonsuoritus), oppiaine)) paatasonsuoritusArvosanaLaajuus(paatasonsuoritus, osasuoritukset) else "")
    } else {
      mahdollisetOppiaineet.map(oppiaine => osasuoritukset.filter(os => matchingOppiaine(Right(os), oppiaine)).map(osasuoritusArvosanaLaajuus(_, osasuoritukset)).mkString(","))
    }
  }

  private def matchingOppiaine(suoritusRow: Either[RPäätasonSuoritusRow, ROsasuoritusRow], oppiaine: (OppiaineenNimi, Koulutusmooduuli_koodiarvo, Paikallinen)) = {
    val (oppiaineNimi, oppiaineKoodiarvo, oppiainePaikallinen) = oppiaine
    val (suoritusNimi, suoritusKoodiarvo, suoritusPaikallinen) = suoritusRow match {
      case Left(paatasonsuoritus) => (extractKurssinNimi(paatasonsuoritus.data), paatasonsuoritus.koulutusmoduuliKoodiarvo, paatasonsuoritus.koulutusmoduuliKoodisto == "koskioppiaineyleissivistava")
      case Right(osasuoritus) => (extractKurssinNimi(osasuoritus.data), osasuoritus.koulutusmoduuliKoodiarvo, osasuoritus.koulutusmoduuliPaikallinen)
    }
    oppiaineKoodiarvo == suoritusKoodiarvo && oppiainePaikallinen == suoritusPaikallinen && suoritusNimi.exists(_ == oppiaineNimi)
  }

  private def extractKurssinNimi(json: JValue) = {
    val kieliOppiaine = getFinnishName(json \ "koulutusmoduuli" \ "kieli" \ "nimi")
    val oppimäärällinenOppiaine = getFinnishName(json \ "koulutusmoduuli" \ "oppimäärä" \ "nimi")
    val muuOppiaine = getFinnishName(json \ "koulutusmoduuli" \ "tunniste" \ "nimi")

    if (kieliOppiaine.isDefined) {
      kieliOppiaine
    } else if (oppimäärällinenOppiaine.isDefined) {
      oppimäärällinenOppiaine
    } else if (muuOppiaine.isDefined) {
      muuOppiaine
    } else {
      None
    }
  }

  private def getFinnishName(j: JValue) = JsonSerializer.extract[Option[LocalizedString]](j).map(_.get("fi"))

  private def osasuoritusArvosanaLaajuus(osasuoritus: ROsasuoritusRow, osasuoritukset: Seq[ROsasuoritusRow]) = {
    val laajuus = osasuoritukset.filter(_.ylempiOsasuoritusId.exists(_ == osasuoritus.osasuoritusId)).size
    arvosanaLaajuus(osasuoritus.arviointiArvosanaKoodiarvo, laajuus)
  }

  private def paatasonsuoritusArvosanaLaajuus(paatasonsuoritus: RPäätasonSuoritusRow, osasuoritukset: Seq[ROsasuoritusRow]) = {
    arvosanaLaajuus(paatasonsuoritus.arviointiArvosanaKoodiarvo, osasuoritukset.size)
  }

  private def arvosanaLaajuus(arvosana: Option[String], laajuus: Int) = {
    s"${arvosana.map("Arvosana " + _).getOrElse("Ei arvosanaa")}, ${laajuus} ${if (laajuus == 1) "kurssi" else "kurssia"}"
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
}
