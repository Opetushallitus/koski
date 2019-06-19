package fi.oph.koski.raportit

object LukioRaporttiOppiaineetOrdering extends Ordering[LukioRaporttiOppiaineJaKurssit] {
  override def compare(x: LukioRaporttiOppiaineJaKurssit, y: LukioRaporttiOppiaineJaKurssit): Int = {
    orderByPaikallisuus(x.oppiaine, y.oppiaine)
  }

  private def orderByPaikallisuus(x: LukioRaporttiOppiaine, y: LukioRaporttiOppiaine) = (x.koulutusmoduuliPaikallinen, y.koulutusmoduuliPaikallinen) match {
    case (true, true) => orderByNimi(x, y)
    case (false, false) => orderByKoulutusmoduulikoodiarvo(x, y)
    case (a, b) => a compare b
  }

  private def orderByKoulutusmoduulikoodiarvo(x: LukioRaporttiOppiaine, y: LukioRaporttiOppiaine) = {
    koodiarvonJärjestysnumero(x) compare koodiarvonJärjestysnumero(y) match {
      case 0 => orderByNimi(x, y)
      case order => order
    }
  }

  private def orderByNimi(x: LukioRaporttiOppiaine, y: LukioRaporttiOppiaine) = {
    if (x.koulutusmoduuliKoodiarvo == "MA") {
      orderMatikkaByOppimäärä(x, y)
    } else {
      x.nimi.capitalize compare y.nimi.capitalize
    }
  }

  private def orderMatikkaByOppimäärä(x: LukioRaporttiOppiaine, y: LukioRaporttiOppiaine) = {
    if (x.nimi.contains("pitkä") || x.nimi.contains("pitka")) -1 else 1
  }

  private def koodiarvonJärjestysnumero(x: LukioRaporttiOppiaine) = {
    koodiarvotOrder.get(x.koulutusmoduuliKoodiarvo).getOrElse(99999)
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
