package fi.oph.koski.organisaatio

import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

// organisaationAlaisetOrganisaatiot palauttaa tarkoituksella kaksi erillistä listaa, eikä
// niitä saa yhdistää: hierarkian opiskeluoikeudet ovat kyselyn piirissä sellaisenaan, mutta
// ostopalveluyksiköistä vain ostavan koulutustoimijan omat. Yksikössä on tyypillisesti myös
// sen oman koulutustoimijan ja muiden ostajien opiskeluoikeuksia, joihin ei ole lukuoikeutta.
class OrganisaatioServiceSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec {
  private val organisaatioService = KoskiApplicationForTests.organisaatioService

  private def alaisetOrganisaatiot(organisaatioOid: String, user: KoskiMockUser): OrganisaationAlaisetOrganisaatiot = {
    implicit val session: KoskiSpecificSession =
      user.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)
    organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid)
  }

  "organisaationAlaisetOrganisaatiot" - {
    "Varhaiskasvatuksen järjestäjä, jolla on ostopalveluesiopetusta" - {
      lazy val organisaatiot =
        alaisetOrganisaatiot(MockOrganisaatiot.helsinginKaupunki, MockUsers.helsinkiKatselija)

      "Hierarkia sisältää oman organisaation oppilaitokset" in {
        organisaatiot.hierarkia should contain(MockOrganisaatiot.helsinginKaupunki)
        organisaatiot.hierarkia should contain(MockOrganisaatiot.kulosaarenAlaAste)
      }

      "Hierarkia ei sisällä ostopalveluyksiköitä" in {
        organisaatiot.hierarkia should not contain MockOrganisaatiot.jyväskylänNormaalikoulu
        organisaatiot.hierarkia should not contain MockOrganisaatiot.päiväkotiTouhula
      }

      "Ostopalveluyksiköt palautetaan erillisenä listana" in {
        organisaatiot.ostopalvelu should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
        organisaatiot.ostopalvelu should contain(MockOrganisaatiot.päiväkotiTouhula)
        organisaatiot.ostopalvelu should contain(MockOrganisaatiot.päiväkotiMajakka)
      }

      // Ilman tätä kutsupaikka ei voi rajata ostopalveluyksikön tietueita ostajan omiin.
      "Koulutustoimija palautetaan ostopalvelutietueiden rajaamista varten" in {
        organisaatiot.koulutustoimija should equal(Some(MockOrganisaatiot.helsinginKaupunki))
      }
    }

    "Oppilaitos ei ole koulutustoimija, joten ostopalvelua ei laajenneta" in {
      val organisaatiot =
        alaisetOrganisaatiot(MockOrganisaatiot.stadinAmmattiopisto, MockUsers.helsinkiKatselija)

      organisaatiot.hierarkia should contain(MockOrganisaatiot.stadinAmmattiopisto)
      organisaatiot.koulutustoimija should be(None)
      organisaatiot.ostopalvelu should be(empty)
    }

    "Tuntematon organisaatio palauttaa tyhjän tuloksen" in {
      val organisaatiot =
        alaisetOrganisaatiot("1.2.246.562.10.00000000000", MockUsers.helsinkiKatselija)

      organisaatiot.koulutustoimija should be(None)
      organisaatiot.hierarkia should be(empty)
      organisaatiot.ostopalvelu should be(empty)
    }
  }
}
