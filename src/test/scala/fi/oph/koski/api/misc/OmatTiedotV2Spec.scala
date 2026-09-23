package fi.oph.koski.api.misc

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.{DirtiesFixtures, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets

class OmatTiedotV2Spec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsPerusopetus with DirtiesFixtures with Matchers {
  "Omien tietojen uuden käyttöliittymän rajapinta" - {
    "palauttaa samat erityiset henkilötiedot kuin suoritusjako" - {
      "Ahvenanmaan perusopetuksen mukautettu oppimäärä" in {
        getOmatTiedot(KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas.hetu.get) {
          verifyResponseStatusOk()
          bodyString should include("mukautettuOppimäärä")
        }
      }

      "ammatillisen tutkinnon osan mukautettu arviointi" in {
        getOmatTiedot(KoskiSpecificMockOppijat.osittainenammattitutkinto.hetu.get) {
          verifyResponseStatusOk()
          bodyString should include("\"mukautettu\"")
        }
      }
    }

    "ei palauta tietoja, jotka on rajattu vain luottamuksellisten tietojen käyttöoikeudelle" in {
      getOmatTiedot(KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas.hetu.get) {
        verifyResponseStatusOk()
        bodyString should not include("jääLuokalle")
      }
    }
  }

  "Huollettavan tiedot" - {
    val huoltaja = KoskiSpecificMockOppijat.faija.hetu.get
    val huollettava = KoskiSpecificMockOppijat.eskari

    "palautetaan huoltajalle" in {
      getHuollettavanTiedot(huoltaja, huollettava.oid) {
        verifyResponseStatusOk()
        bodyString should include(huollettava.oid)
      }
    }

    "palautetaan ilman kansalaisen omien tietojen erityisiä henkilötietoja" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus, huollettava) {
        verifyResponseStatusOk()
      }

      // Huollettava itse näkee kentän, joten sen puuttuminen huoltajan vastauksesta on aito tulos
      getOmatTiedot(huollettava.hetu.get) {
        verifyResponseStatusOk()
        bodyString should include("yksilöllistettyOppimäärä")
      }

      getHuollettavanTiedot(huoltaja, huollettava.oid) {
        verifyResponseStatusOk()
        bodyString should not include("yksilöllistettyOppimäärä")
      }
    }

    "ei palauteta muiden kuin huollettavien tietoja" in {
      getHuollettavanTiedot(huoltaja, KoskiSpecificMockOppijat.amis.oid) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }

    "omalla oidilla palautetaan omat tiedot erityisine henkilötietoineen" in {
      val oppija = KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas
      getHuollettavanTiedot(oppija.hetu.get, oppija.oid) {
        verifyResponseStatusOk()
        bodyString should include("mukautettuOppimäärä")
      }
    }
  }

  private def getOmatTiedot[A](hetu: String)(f: => A): A =
    get("api/omattiedotV2/oppija", headers = kansalainenLoginHeaders(hetu))(f)

  private def getHuollettavanTiedot[A](hetu: String, oid: String)(f: => A): A =
    get(s"api/omattiedotV2/oppija/$oid", headers = kansalainenLoginHeaders(hetu))(f)

  private def bodyString: String = new String(response.bodyBytes, StandardCharsets.UTF_8)
}
