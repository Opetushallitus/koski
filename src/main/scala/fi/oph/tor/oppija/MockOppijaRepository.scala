package fi.oph.tor.oppija

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.schema.{Henkilö, FullHenkilö}

class MockOppijaRepository extends OppijaRepository {
  val eero = oppija(generateId, "esimerkki", "eero", "010101-123N")
  val eerola = oppija(generateId, "eerola", "jouni", "")
  val markkanen = oppija(generateId, "markkanen", "eero", "")
  val teija = oppija(generateId, "tekijä", "teija", "150995-914X")
  val tero = oppija(generateId, "tunkkila", "tero", "091095-9833")
  val presidentti = oppija(generateId, "Presidentti", "Tasavallan", "")

  private def oppija(id : String, suku: String, etu: String, hetu: String) = FullHenkilö(id, hetu, etu, etu, suku)

  def defaultOppijat = List(
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
    if (query.toLowerCase.contains("error")) {
      throw new RuntimeException("Testing error handling")
    }
    oppijat.filter(searchString(_).contains(query))
  }

  override def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String): Either[HttpStatus, Henkilö.Id] = {
    if (sukunimi == "error") {
      throw new RuntimeException("Testing error handling")
    } else if (oppijat.find { o => (o.hetu == hetu) } .isDefined) {
      Left(HttpStatus.conflict("conflict"))
    } else {
      val newOppija = oppija(generateId, sukunimi, etunimet, hetu)
      oppijat = oppijat :+ newOppija
      Right(newOppija.oid)
    }
  }

  private def searchString(oppija: FullHenkilö) = {
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

  override def findByOid(id: String): Option[FullHenkilö] = oppijat.filter(_.oid == id).headOption
}
