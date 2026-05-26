package fi.oph.koski.organisaatio

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.organisaatio.MockOrganisaatiot.kiipulanAmmattiopisto
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OppilaitostyyppiResolverSpec extends AnyFreeSpec with Matchers {
  private val resolver = new OppilaitostyyppiResolver(MockOrganisaatioRepository, MockKoodistoViitePalvelu)

  "OppilaitostyyppiResolver" - {
    "palauttaa oppilaitoksen tämänhetkisen oppilaitostyypin koodistokoodiviitteenä" in {
      // Kiipulan ammattiopisto on ammatillinen erityisoppilaitos (oppilaitostyyppi 22)
      val tyyppi = resolver.oppilaitostyyppi(kiipulanAmmattiopisto)
      tyyppi.map(_.koodiarvo) should equal(Some("22"))
      tyyppi.map(_.koodistoUri) should equal(Some("oppilaitostyyppi"))
      tyyppi.flatMap(_.nimi) should not equal None
    }

    "palauttaa None tuntemattomalle organisaatiolle" in {
      resolver.oppilaitostyyppi("1.2.246.562.10.00000000000") should equal(None)
    }
  }
}
