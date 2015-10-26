package fi.oph.tor.oppija

import fi.oph.tor.http.HttpError

class MockOppijaRepository extends OppijaRepository {
  val eero = oppija(generateId, "esimerkki", "eero", "010101-123N")
  val eerola = oppija(generateId, "eerola", "jouni", "")
  val markkanen = oppija(generateId, "markkanen", "eero", "")
  val teija = oppija(generateId, "tekijä", "teija", "150995-914X")
  val tero = oppija(generateId, "tunkkila", "tero", "091095-9833")
  val presidentti = oppija(generateId, "Presidentti", "Tasavallan", "")

  private def oppija(id : String, suku: String, etu: String, hetu: String) = Oppija(Some(id), Some(hetu), Some(etu), Some(etu), Some(suku))

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

  override def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String): Either[HttpError, Oppija.Id] = {
    if (oppijat.find { o => (o.hetu == Some(hetu)) } .isDefined) {
      Left(HttpError(409, "conflict"))
    } else {
      val newOppija = oppija(generateId, sukunimi, etunimet, hetu)
      oppijat = oppijat :+ newOppija
      Right(newOppija.oid.get)
    }
  }

  private def searchString(oppija: Oppija) = {
    oppija.toString.toUpperCase
  }

  private def generateId(): String = {
    idCounter = idCounter + 1
    "1.2.246.562.24.0000000000" + idCounter
  }

  override def resetFixtures {
    oppijat = defaultOppijat
    idCounter = defaultOppijat.length
  }

  override def findByOid(id: String): Option[Oppija] = oppijat.filter(_.oid.get == id).headOption
}
