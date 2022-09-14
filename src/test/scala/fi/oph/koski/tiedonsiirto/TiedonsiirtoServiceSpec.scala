package fi.oph.koski.tiedonsiirto

import fi.oph.koski.log.Logging
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Timestamp
import java.time.Instant

class TiedonsiirtoServiceSpec extends AnyFreeSpec with Matchers with Logging {

  "TiedonsiirtoDocument generoi stabiilin id:n dokumentille jos oppijan tiedot puuttuvat" in {
    val aikaleima = Timestamp.from(Instant.now())
    def doc = TiedonsiirtoDocument(
      tallentajaKäyttäjäOid = "tallentaja",
      tallentajaKäyttäjätunnus = None,
      tallentajaOrganisaatioOid = "tallentajaOrganisaatio",
      oppija = None,
      oppilaitokset = None,
      koulutusmuoto = None,
      suoritustiedot = None,
      data = None,
      success = true,
      virheet = List.empty,
      lähdejärjestelmä = None,
      aikaleima = aikaleima
    )

    val doc1 = doc
    val doc2 = doc

    doc1.id shouldBe doc1.id
    doc1.id should not be doc2.id
    doc2.id shouldBe doc2.id
  }
}
