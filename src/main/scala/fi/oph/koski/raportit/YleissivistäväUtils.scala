package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.{Aikajakso, Jakso}
import fi.oph.koski.suoritusote.KoulutusModuuliOrdering.järjestäSuffiksinMukaan


case class YleissivistäväOppiaineenTiedot(suoritus: RSuoritusRow, osasuoritukset: Seq[ROsasuoritusRow]) {
  private val suorituksenOsasuoritukset = suoritus match {
    case _: RPäätasonSuoritusRow => osasuoritukset
    case s: ROsasuoritusRow => osasuoritukset.filter(_.ylempiOsasuoritusId.contains(s.osasuoritusId))
  }
  private val hylätytOsasuoritukset = suorituksenOsasuoritukset.filterNot(_.suoritettu)

  private val laajuus = suorituksenOsasuoritukset.map(_.laajuus).sum
  private val hylättyjenLaajuus = hylätytOsasuoritukset.map(_.laajuus).sum

  private val hylätytKurssitStr = if (hylättyjenLaajuus > 0) f" (joista $hylättyjenLaajuus%.1f hylättyjä)" else ""

  override def toString: String = suoritus.arviointiArvosanaKoodiarvo.map(a => f"Arvosana $a, $laajuus%.1f kurssia$hylätytKurssitStr").getOrElse("Ei arvosanaa")
}

sealed trait YleissivistäväRaporttiOppiaineTaiKurssi {
  def nimi: String
  def koulutusmoduuliKoodiarvo: String
  def koulutusmoduuliPaikallinen: Boolean
}

case class YleissivistäväRaporttiOppiaine(nimi: String, koulutusmoduuliKoodiarvo: String, koulutusmoduuliPaikallinen: Boolean) extends YleissivistäväRaporttiOppiaineTaiKurssi {
  def toSheetTitle: String = s"$koulutusmoduuliKoodiarvo ${if (koulutusmoduuliPaikallinen) "p" else "v"} ${nimi.capitalize}"
  def toColumnTitle: String = s"$koulutusmoduuliKoodiarvo ${nimi.capitalize} ${if (koulutusmoduuliPaikallinen) "paikallinen" else "valtakunnallinen"}"
}

case class YleissivistäväRaporttiKurssi(nimi: String, koulutusmoduuliKoodiarvo: String, koulutusmoduuliPaikallinen: Boolean) extends YleissivistäväRaporttiOppiaineTaiKurssi {
  def toColumnTitle: String = s"$koulutusmoduuliKoodiarvo ${nimi.capitalize} ${if (koulutusmoduuliPaikallinen) "paikallinen" else "valtakunnallinen"}"
}

case class YleissivistäväRaporttiOppiaineJaKurssit(oppiaine: YleissivistäväRaporttiOppiaine, kurssit: Seq[YleissivistäväRaporttiKurssi])

object YleissivistäväOppiaineetOrdering extends Ordering[YleissivistäväRaporttiOppiaineJaKurssit] {
  override def compare(x: YleissivistäväRaporttiOppiaineJaKurssit, y: YleissivistäväRaporttiOppiaineJaKurssit): Int = {
    (x.oppiaine.koulutusmoduuliPaikallinen, y.oppiaine.koulutusmoduuliPaikallinen) match {
      case (true, true) => orderByNimi(x.oppiaine, y.oppiaine)
      case (false, false) => orderByKoulutusmoduulikoodiarvo(x.oppiaine, y.oppiaine)
      case (a, b) => a compare b
    }
  }

  private def orderByKoulutusmoduulikoodiarvo(x: YleissivistäväRaporttiOppiaine, y: YleissivistäväRaporttiOppiaine) = {
    koodiarvonJärjestysnumero(x) compare koodiarvonJärjestysnumero(y) match {
      case 0 => orderByNimi(x, y)
      case order => order
    }
  }

  private def orderByNimi(x: YleissivistäväRaporttiOppiaine, y: YleissivistäväRaporttiOppiaine) = {
    if (x.koulutusmoduuliKoodiarvo == "MA" && y.koulutusmoduuliKoodiarvo == "MA") {
      orderMatikkaByOppimäärä(x, y)
    } else {
      x.nimi.capitalize compare y.nimi.capitalize
    }
  }

  private def orderMatikkaByOppimäärä(x: YleissivistäväRaporttiOppiaine, y: YleissivistäväRaporttiOppiaine) = {
    (x.nimi.toLowerCase.contains("pitkä"), y.nimi.toLowerCase.contains("pitkä")) match {
      case (true, false) => -1
      case (false, true) => 1
      case _ => 0
    }
  }

  private def koodiarvonJärjestysnumero(x: YleissivistäväRaporttiOppiaine) = {
    koodiarvotOrder.getOrElse(x.koulutusmoduuliKoodiarvo, 99999)
  }

  private lazy val koodiarvotOrder = koodiarvot.zipWithIndex.toMap

  private lazy val koodiarvot = List(
    "AI",
    "A1",
    "A2",
    "B1",
    "B2",
    "B3",
    "MA",
    "BI",
    "GE",
    "FY",
    "KE",
    "FI",
    "PS",
    "HI",
    "YH",
    "KT",
    "ET",
    "TE",
    "LI",
    "MU",
    "KU",
    "OP",
    "TO",
    "LD",
    "TVO",
    "MS",
    "OA",
    "XX"
  )
}

object YleissivistäväKurssitOrdering {
  lazy val yleissivistäväKurssitOrdering: Ordering[YleissivistäväRaporttiKurssi] = Ordering.by(orderByPaikallisuusAndSuffix)

  private def orderByPaikallisuusAndSuffix(kurssi: YleissivistäväRaporttiKurssi) = {
    val (koodiarvoCharacters, koodiarvoDigits) = järjestäSuffiksinMukaan(kurssi.koulutusmoduuliKoodiarvo)

    (kurssi.koulutusmoduuliPaikallinen, koodiarvoCharacters, koodiarvoDigits)
  }
}

trait YleissivistäväRaporttiRows {
  def opiskeluoikeus: ROpiskeluoikeusRow
  def henkilo: RHenkilöRow
  def aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow]
  def päätasonSuoritus: RPäätasonSuoritusRow
  def osasuoritukset: Seq[ROsasuoritusRow]
}

object YleissivistäväUtils {
  def rahoitusmuodotOk(row: YleissivistäväRaporttiRows) = {
    val tarkistettavatTilat = Seq("lasna", "valmistunut")
    row.aikajaksot
      .filter(a => tarkistettavatTilat.contains(a.tila))
      .forall(_.opintojenRahoitus.nonEmpty)
  }

  def opetettavatOppiaineetJaNiidenKurssit(
    isOppiaineenOppimäärä: RPäätasonSuoritusRow => Boolean,
    isOppiaine: ROsasuoritusRow => Boolean,
    rows: Seq[YleissivistäväRaporttiRows]
  ): Seq[YleissivistäväRaporttiOppiaineJaKurssit] = {
    rows
      .flatMap(oppianeetJaNiidenKurssit(isOppiaineenOppimäärä, isOppiaine))
      .groupBy(_.oppiaine)
      .map { case (oppiaine, x) =>
      YleissivistäväRaporttiOppiaineJaKurssit(oppiaine, x.flatMap(_.kurssit).distinct.sorted(YleissivistäväKurssitOrdering.yleissivistäväKurssitOrdering))
    }
      .toSeq
      .sorted(YleissivistäväOppiaineetOrdering)
  }

  private def oppianeetJaNiidenKurssit(
    isOppiaineenOppimäärä: RPäätasonSuoritusRow => Boolean,
    isOppiaine: ROsasuoritusRow => Boolean
  ) (row: YleissivistäväRaporttiRows): Seq[YleissivistäväRaporttiOppiaineJaKurssit] = {
    if (isOppiaineenOppimäärä(row.päätasonSuoritus)) {
      Seq(YleissivistäväRaporttiOppiaineJaKurssit(toOppiaine(row.päätasonSuoritus), row.osasuoritukset.map(toKurssi)))
    } else {
      oppiaineetJaNiidenKurssitOppimäärästä(isOppiaine, row)
    }
  }

  private def oppiaineetJaNiidenKurssitOppimäärästä(
    isOppiaine: ROsasuoritusRow => Boolean,
    row: YleissivistäväRaporttiRows
  ): Seq[YleissivistäväRaporttiOppiaineJaKurssit] = {
    val kurssit = row.osasuoritukset.filter(_.ylempiOsasuoritusId.isDefined).groupBy(_.ylempiOsasuoritusId.get)
    val oppiaineet = row.osasuoritukset.filter(isOppiaine)
    val combineOppiaineWithKurssit = (oppiaine: ROsasuoritusRow) =>
      YleissivistäväRaporttiOppiaineJaKurssit(
        toOppiaine(oppiaine),
        kurssit.getOrElse(oppiaine.osasuoritusId, Nil).map(toKurssi)
      )
    oppiaineet.map(combineOppiaineWithKurssit)
  }

  private def toOppiaine(row: RSuoritusRow) = row match {
    case s: RPäätasonSuoritusRow =>
      YleissivistäväRaporttiOppiaine(
        s.suorituksestaKäytettäväNimi.getOrElse("ei nimeä"),
        s.koulutusmoduuliKoodiarvo,
        !s.koulutusmoduuliKoodisto.contains("koskioppiaineetyleissivistava")
      )
    case s: ROsasuoritusRow =>
      YleissivistäväRaporttiOppiaine(
        s.suorituksestaKäytettäväNimi.getOrElse("ei nimeä"),
        s.koulutusmoduuliKoodiarvo,
        s.koulutusmoduuliPaikallinen
      )
  }

  private def toKurssi(row: ROsasuoritusRow) = {
    YleissivistäväRaporttiKurssi(
      row.koulutusmoduuliNimi.getOrElse("ei nimeä"),
      row.koulutusmoduuliKoodiarvo,
      row.koulutusmoduuliPaikallinen
    )
  }

  def oppiaineidentiedot(
    paatasonsuoritus: RPäätasonSuoritusRow,
    osasuoritukset: Seq[ROsasuoritusRow],
    oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    isOppiaineenOppimäärä: RPäätasonSuoritusRow => Boolean
  ): Seq[String] = {

    def oppiaineenTiedot(oppiaine: YleissivistäväRaporttiOppiaine) = if (isOppiaineenOppimäärä(paatasonsuoritus)) {
      if (paatasonsuoritus.matchesWith(oppiaine)) {
        List(YleissivistäväOppiaineenTiedot(paatasonsuoritus, osasuoritukset))
      } else {
        Nil
      }
    } else {
      val osasuorituksetMap = osasuoritukset.groupBy(_.koulutusmoduuliKoodiarvo)
      val oppiaineenSuoritukset = osasuorituksetMap.getOrElse(oppiaine.koulutusmoduuliKoodiarvo, Nil).filter(_.matchesWith(oppiaine))
      oppiaineenSuoritukset.map(suoritus => YleissivistäväOppiaineenTiedot(suoritus, osasuoritukset))
    }

    oppiaineet
      .map(_.oppiaine)
      .map(oppiaineenTiedot(_).map(_.toString).mkString(","))
  }

  def removeContinuousSameTila(aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow]): Seq[ROpiskeluoikeusAikajaksoRow] = {
    if (aikajaksot.size < 2) {
      aikajaksot
    } else {
      val rest = aikajaksot.dropWhile(_.tila == aikajaksot.head.tila)
      aikajaksot.head +: removeContinuousSameTila(rest)
    }
  }

  def lengthInDaysInDateRange(jakso: Jakso, alku: LocalDate, loppu: LocalDate): Int = {
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
