package fi.oph.tor.oppija

trait OppijaRepository {
  def findOppijat(query: String): List[Oppija]
}

class MockOppijaRepository extends OppijaRepository {
  val oppijat = List(
    Oppija("esimerkki", "eero", "010101-123N"),
    Oppija("tekijä", "teija", "150995-914X")
  )

  override def findOppijat(query: String) = {
    oppijat.filter(searchString(_).contains(query.toLowerCase))
  }

  private def searchString(oppija: Oppija) = {
    oppija.toString.toLowerCase
  }
}