package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets

class OmatTiedotV2Spec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
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

  private def getOmatTiedot[A](hetu: String)(f: => A): A =
    get("api/omattiedotV2/oppija", headers = kansalainenLoginHeaders(hetu))(f)

  private def bodyString: String = new String(response.bodyBytes, StandardCharsets.UTF_8)
}
