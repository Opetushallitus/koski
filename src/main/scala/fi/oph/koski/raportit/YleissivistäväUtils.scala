package fi.oph.koski.raportit

import fi.oph.koski.localization.LocalizationReader

import java.lang.Character.isDigit
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.{Aikajakso, Jakso, Koulutusmoduuli}


case class YleissivistäväOppiaineenTiedot(suoritus: RSuoritusRow, osasuoritukset: Seq[ROsasuoritusRow]) {
  private val suorituksenOsasuoritukset = suoritus match {
    case _: RPäätasonSuoritusRow => osasuoritukset
    case s: ROsasuoritusRow => osasuoritukset.filter(_.ylempiOsasuoritusId.contains(s.osasuoritusId))
  }

  private val suorituksenLaajuusYksikkö = {
    suoritus match {
      case s: RPäätasonSuoritusRow => s.koulutusModuulinLaajuusYksikköNimi
      case s: ROsasuoritusRow => s.koulutusModuulinLaajuusYksikköNimi.orElse(
        suorituksenOsasuoritukset
          .find(_.koulutusmoduuliLaajuusYksikkö.nonEmpty)
          .flatMap(r => r.koulutusModuulinLaajuusYksikköNimi)
      )
    }
  }
  private val hylätytOsasuoritukset = suorituksenOsasuoritukset.filterNot(_.arvioituJaHyväksytty)

  private val laajuus = {
      suoritus match {
        case s: ROsasuoritusRow if s.suorituksenTyyppi == "perusopetuksenoppiaineperusopetukseenvalmistavassaopetuksessa" => s.laajuus
        case s: ROsasuoritusRow if s.suorituksenTyyppi == "perusopetukseenvalmistavanopetuksenoppiaine" => s.laajuus
        case _ => suorituksenOsasuoritukset.map(_.laajuus).sum
    }
  }
  private val hylättyjenLaajuus = hylätytOsasuoritukset.map(_.laajuus).sum
  private val luokkaAste = suoritus match {
    case s: ROsasuoritusRow => s.luokkaAsteNimi
    case _ => None
  }

  private def hylätytKurssitStr(t: LocalizationReader) =
    if (hylättyjenLaajuus > 0) f" (${t.get("raportti-excel-default-value-joista")} $hylättyjenLaajuus%.1f ${t.get("raportti-excel-default-value-hylättyjä")})" else ""

  def toStringLokalisoitu(t: LocalizationReader): String = {
    suoritus
      .arviointiArvosanaKoodiarvo
      .map(a => f"${t.get("raportti-excel-default-value-arvosana")} $a, $laajuus%.1f ${suorituksenLaajuusYksikkö.map(_.get(t.language)).getOrElse(t.get("raportti-excel-default-value-kurssia"))}${hylätytKurssitStr(t)}${luokkaAste.map(l => s", ${l.get(t.language)}").getOrElse("")}")
      .getOrElse(t.get("raportti-excel-default-value-ei-arvosanaa"))
  }
}

sealed trait YleissivistäväRaporttiOppiaineTaiKurssi {
  def nimi: String
  def koulutusmoduuliKoodiarvo: String
  def koulutusmoduuliPaikallinen: Boolean
}

case class YleissivistäväRaporttiOppiaine(nimi: String, koulutusmoduuliKoodiarvo: String, koulutusmoduuliPaikallinen: Boolean, oppimääräKoodiarvo: Option[String]) extends YleissivistäväRaporttiOppiaineTaiKurssi {
  def toSheetTitle(t: LocalizationReader): String = s"$koulutusmoduuliKoodiarvo ${if (koulutusmoduuliPaikallinen) t.get("raportti-excel-kolumni-paikallinen-lyhenne") else t.get("raportti-excel-kolumni-valtakunnallinen-lyhenne")} ${nimi.capitalize}"
  def toColumnTitle(t: LocalizationReader): String = s"$koulutusmoduuliKoodiarvo ${nimi.capitalize} ${if (koulutusmoduuliPaikallinen) t.get("raportti-excel-kolumni-paikallinen") else t.get("raportti-excel-kolumni-valtakunnallinen")}"
}

case class YleissivistäväRaporttiKurssi(nimi: String, koulutusmoduuliKoodiarvo: String, koulutusmoduuliPaikallinen: Boolean) extends YleissivistäväRaporttiOppiaineTaiKurssi {
  def toColumnTitle(t: LocalizationReader): String = s"$koulutusmoduuliKoodiarvo ${nimi.capitalize} ${if (koulutusmoduuliPaikallinen) t.get("raportti-excel-kolumni-paikallinen") else t.get("raportti-excel-kolumni-valtakunnallinen")}"
}

case class YleissivistäväRaporttiOppiaineJaKurssit(oppiaine: YleissivistäväRaporttiOppiaine, kurssit: Seq[YleissivistäväRaporttiKurssi])

object PerusopetukseenValmistavaOppiaineetOrdering extends YleissivistäväRaporttiOppiaineJaKurssitOrdering {
  override def compare(
    x: YleissivistäväRaporttiOppiaineJaKurssit,
    y: YleissivistäväRaporttiOppiaineJaKurssit
  ): Int = {
    (x.oppiaine.koulutusmoduuliPaikallinen, y.oppiaine.koulutusmoduuliPaikallinen) match {
      case (a, b) if a == b => super.compare(x, y)
      case (a, b) => b compare a
    }
  }
}

object YleissivistäväOppiaineetOrdering extends YleissivistäväRaporttiOppiaineJaKurssitOrdering

trait YleissivistäväRaporttiOppiaineJaKurssitOrdering extends Ordering[YleissivistäväRaporttiOppiaineJaKurssit] {
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
    (x.oppimääräKoodiarvo.contains("MAA"), y.oppimääräKoodiarvo.contains("MAA")) match {
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
    val (koodiarvoCharacters, koodiarvoDigits) = KoulutusModuuliOrdering.järjestäSuffiksinMukaan(kurssi.koulutusmoduuliKoodiarvo)

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
    rows: Seq[YleissivistäväRaporttiRows],
    t: LocalizationReader,
    ordering: YleissivistäväRaporttiOppiaineJaKurssitOrdering = YleissivistäväOppiaineetOrdering
  ): Seq[YleissivistäväRaporttiOppiaineJaKurssit] = {
    val oppiaineet = rows
      .flatMap(oppianeetJaNiidenKurssit(isOppiaineenOppimäärä, isOppiaine, t))
    oppiaineet
      .groupBy(_.oppiaine)
      .map { case (oppiaine, x) =>
        YleissivistäväRaporttiOppiaineJaKurssit(oppiaine, x.flatMap(_.kurssit).distinct.sorted(YleissivistäväKurssitOrdering.yleissivistäväKurssitOrdering))
      }
      .toSeq
      .sorted(ordering)
  }

  private def oppianeetJaNiidenKurssit(
    isOppiaineenOppimäärä: RPäätasonSuoritusRow => Boolean,
    isOppiaine: ROsasuoritusRow => Boolean,
    t: LocalizationReader
  ) (row: YleissivistäväRaporttiRows): Seq[YleissivistäväRaporttiOppiaineJaKurssit] = {
    if (isOppiaineenOppimäärä(row.päätasonSuoritus)) {
      Seq(YleissivistäväRaporttiOppiaineJaKurssit(toOppiaine(row.päätasonSuoritus, t), row.osasuoritukset.map(o => toKurssi(o, t))))
    } else {
      oppiaineetJaNiidenKurssitOppimäärästä(isOppiaine, row, t)
    }
  }

  private def oppiaineetJaNiidenKurssitOppimäärästä(
    isOppiaine: ROsasuoritusRow => Boolean,
    row: YleissivistäväRaporttiRows,
    t: LocalizationReader
  ): Seq[YleissivistäväRaporttiOppiaineJaKurssit] = {
    val kurssit = row.osasuoritukset.filter(_.ylempiOsasuoritusId.isDefined).groupBy(_.ylempiOsasuoritusId.get)
    val oppiaineet = row.osasuoritukset.filter(isOppiaine)
    val combineOppiaineWithKurssit = (oppiaine: ROsasuoritusRow) =>
      YleissivistäväRaporttiOppiaineJaKurssit(
        toOppiaine(oppiaine, t),
        kurssit.getOrElse(oppiaine.osasuoritusId, Nil).map(k => toKurssi(k, t))
      )
    oppiaineet.map(combineOppiaineWithKurssit)
  }

  private def toOppiaine(row: RSuoritusRow, t: LocalizationReader) = row match {
    case s: RPäätasonSuoritusRow =>
      YleissivistäväRaporttiOppiaine(
        s.suorituksestaKäytettäväNimi(t.language).getOrElse(t.get("raportti-excel-default-value-ei-nimeä")),
        s.koulutusmoduuliKoodiarvo,
        !s.koulutusmoduuliKoodisto.contains("koskioppiaineetyleissivistava"),
        s.oppimääräKoodiarvoDatasta
      )
    case s: ROsasuoritusRow =>
      YleissivistäväRaporttiOppiaine(
        s.suorituksestaKäytettäväNimi(t.language).getOrElse(t.get("raportti-excel-default-value-ei-nimeä")),
        s.koulutusmoduuliKoodiarvo,
        s.koulutusmoduuliPaikallinen,
        s.oppimääräKoodiarvo
      )
  }

  private def toKurssi(row: ROsasuoritusRow, t: LocalizationReader) = {
    YleissivistäväRaporttiKurssi(
      row.koulutusModuulistaKäytettäväNimi(t.language).getOrElse(t.get("raportti-excel-default-value-ei-nimeä")),
      row.koulutusmoduuliKoodiarvo,
      row.koulutusmoduuliPaikallinen
    )
  }

  def oppiaineidentiedot(
    paatasonsuoritus: RPäätasonSuoritusRow,
    osasuoritukset: Seq[ROsasuoritusRow],
    oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    isOppiaineenOppimäärä: RPäätasonSuoritusRow => Boolean,
    t: LocalizationReader
  ): Seq[String] = {

    def oppiaineenTiedot(oppiaine: YleissivistäväRaporttiOppiaine) = if (isOppiaineenOppimäärä(paatasonsuoritus)) {
      if (paatasonsuoritus.matchesWith(oppiaine, t.language)) {
        List(YleissivistäväOppiaineenTiedot(paatasonsuoritus, osasuoritukset))
      } else {
        Nil
      }
    } else {
      val osasuorituksetMap = osasuoritukset.groupBy(_.koulutusmoduuliKoodiarvo)
      val oppiaineenSuoritukset = osasuorituksetMap.getOrElse(oppiaine.koulutusmoduuliKoodiarvo, Nil).filter(_.matchesWith(oppiaine, t.language))
      oppiaineenSuoritukset.map(suoritus => YleissivistäväOppiaineenTiedot(suoritus, osasuoritukset))
    }

    oppiaineet
      .map(_.oppiaine)
      .map(oppiaineenTiedot(_).map(_.toStringLokalisoitu(t)).mkString(","))
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

object KoulutusModuuliOrdering {
  // Käsittelee tunnisteen numeerisen suffiksin lukuna
  lazy val orderByTunniste: Ordering[Koulutusmoduuli] = Ordering.by(järjestäKoulutusmoduuliSuffiksinMukaan)

  def järjestäSuffiksinMukaan(koodiarvo: String) = {
    val numericSuffix = koodiarvo.reverse.takeWhile(isDigit).reverse
    if (numericSuffix.isEmpty) {
      (koodiarvo, None)
    } else {
      (koodiarvo.substring(0, koodiarvo.length - numericSuffix.length), Some(numericSuffix.toInt))
    }
  }

  private def järjestäKoulutusmoduuliSuffiksinMukaan(koulutusmoduuli: Koulutusmoduuli) = {
    val koodiarvo: String = koulutusmoduuli.tunniste.koodiarvo
    järjestäSuffiksinMukaan(koodiarvo)
  }
}
