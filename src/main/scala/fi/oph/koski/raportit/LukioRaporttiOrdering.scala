package fi.oph.koski.raportit

import fi.oph.koski.suoritusote.KoulutusModuuliOrdering.järjestäSuffiksinMukaan

object LukioRaporttiOppiaineetOrdering extends Ordering[LukioRaporttiOppiaineJaKurssit] {
  override def compare(x: LukioRaporttiOppiaineJaKurssit, y: LukioRaporttiOppiaineJaKurssit): Int = {
    (x.oppiaine.koulutusmoduuliPaikallinen, y.oppiaine.koulutusmoduuliPaikallinen) match {
      case (true, true) => orderByNimi(x.oppiaine, y.oppiaine)
      case (false, false) => orderByKoulutusmoduulikoodiarvo(x.oppiaine, y.oppiaine)
      case (a, b) => a compare b
    }
  }

  private def orderByKoulutusmoduulikoodiarvo(x: LukioRaporttiOppiaine, y: LukioRaporttiOppiaine) = {
    koodiarvonJärjestysnumero(x) compare koodiarvonJärjestysnumero(y) match {
      case 0 => orderByNimi(x, y)
      case order => order
    }
  }

  private def orderByNimi(x: LukioRaporttiOppiaine, y: LukioRaporttiOppiaine) = {
    if (x.koulutusmoduuliKoodiarvo == "MA" && y.koulutusmoduuliKoodiarvo == "MA") {
      orderMatikkaByOppimäärä(x, y)
    } else {
      x.nimi.capitalize compare y.nimi.capitalize
    }
  }

  private def orderMatikkaByOppimäärä(x: LukioRaporttiOppiaine, y: LukioRaporttiOppiaine) = {
    (x.nimi.toLowerCase.contains("pitkä"), y.nimi.toLowerCase.contains("pitkä")) match {
      case (true, false) => -1
      case (false, true) => 1
      case _ => 0
    }
  }

  private def koodiarvonJärjestysnumero(x: LukioRaporttiOppiaine) = {
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

object LukioRaporttiKurssitOrdering {
  lazy val lukioRaporttiKurssitOrdering: Ordering[LukioRaporttiKurssi] = Ordering.by(orderByPaikallisuusJaSuffix)

  private def orderByPaikallisuusJaSuffix(kurssi: LukioRaporttiKurssi) = {
    val (koodiarvoCharacters, koodiarvoDigits) = järjestäSuffiksinMukaan(kurssi.koulutusmoduuliKoodiarvo)

    (kurssi.koulutusmoduuliPaikallinen, koodiarvoCharacters, koodiarvoDigits)
  }
}
