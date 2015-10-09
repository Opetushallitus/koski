package fi.oph.tor.oppija

class MockOppijaRepository extends OppijaRepository {
  val eero = Oppija(generateId, "esimerkki", "eero", "010101-123N")
  val eerola = Oppija(generateId, "eerola", "jouni", "")
  val markkanen = Oppija(generateId, "markkanen", "eero", "")
  val teija = Oppija(generateId, "tekijä", "teija", "150995-914X")
  val tero = Oppija(generateId, "tunkkila", "tero", "091095-9833")
  val presidentti = Oppija(generateId, "Presidentti", "Tasavallan", "")

  private def defaultOppijat = List(
    eero,
    eerola,
    markkanen,
    teija,
    tero,
    presidentti
  )

  private var idCounter = 0

  private var oppijat = defaultOppijat

  override def findOppijat(query: String) = {
    if(query.toLowerCase.contains("error")) {
      throw new RuntimeException("BOOM!")
    }
    oppijat.filter(searchString(_).contains(query))
  }

  override def create(oppija: CreateOppija): OppijaCreationResult = {
    if (oppijat.find(o => o.hetu == oppija.hetu).isDefined) {
      Failed(409, "conflict")
    } else {
      val newOppija = Oppija(generateId, oppija.sukunimi, oppija.etunimet, oppija.hetu)
      oppijat = oppijat :+ newOppija
      Created(newOppija.oid)
    }
  }

  private def searchString(oppija: Oppija) = {
    oppija.toString.toUpperCase
  }

  private def generateId(): String = {
    idCounter = idCounter + 1
    "1.2.246.562.24.0000000000" + idCounter
  }

  override def resetMocks {
    oppijat = defaultOppijat
    idCounter = defaultOppijat.length
  }

  override def findById(id: String): Option[Oppija] = oppijat.filter(_.oid == id).headOption
}
