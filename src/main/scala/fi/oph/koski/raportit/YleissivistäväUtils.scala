package fi.oph.koski.raportit

import fi.oph.koski.suoritusote.KoulutusModuuliOrdering.järjestäSuffiksinMukaan


case class YleissivistäväOppiaineenTiedot(arvosana: Option[String], laajuus: Int) {
  override def toString: String = s"${arvosana.fold("Ei arvosanaa")("Arvosana " + _)}, $laajuus ${if (laajuus == 1) "kurssi" else "kurssia"}"
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
