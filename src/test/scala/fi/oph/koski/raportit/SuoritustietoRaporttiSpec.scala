package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{FreeSpec, Matchers}

class SuoritustietoRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods {

  "Suoritustietojen tarkistusraportti" - {

    "Raportti sisältää oikeat tiedot" in {
      loadRaportointikantaFixtures
      val result = SuoritustietojenTarkistus.buildRaportti(raportointiDatabase, MockOrganisaatiot.stadinAmmattiopisto, LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"))
      result.size shouldBe(16)
    }
    "Suoritettujen opintojen yhteislaajuus lasketaan oikein" - {
    }
    "Ammatillisen tutkinnon osat" - {
    }
    "Yhteiset tutkinnon osat" - {
    }
  }
}
