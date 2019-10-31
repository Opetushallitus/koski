package fi.oph.koski.ytr

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.util.ClasspathResource
import org.scalatest.FreeSpec

import scala.collection.Iterator.continually

class YtrKoesuoritusSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with ValtuutusTestMethods {
  "Kansalainen" - {
    "näkee koesuorituksensa" in {
      get("koesuoritus/2345K_XX_12345.pdf", headers = kansalainenLoginHeaders("080698-967F")) {
        verifyResponseStatusOk()
        bodyBytes should equal(resourceAsByteArray(s"/mockdata/ytr/2345K_XX_12345.pdf"))
      }
    }


    "ei näe huollettavansa koesuoritusta ilman valtuutusistuntoa" in {
      get("koesuoritus/2345K_XX_12345.pdf?huollettava=true", headers = kansalainenLoginHeaders(MockOppijat.aikuisOpiskelija.hetu.get)) {
        verifyResponseStatus(404, Nil)
      }
    }

    "näkee huollettavansa koesuorituksen luotuaan valtuutusistunnon" in {
      val loginHeaders = kansalainenLoginHeaders(MockOppijat.aikuisOpiskelija.hetu.get)
      get("huoltaja/valitse", headers = loginHeaders) {
        get(s"api/omattiedot/editor/$valtuutusCode", headers = loginHeaders) {
          get("koesuoritus/2345K_XX_12345.pdf?huollettava=true", headers = loginHeaders) {
            verifyResponseStatusOk()
            bodyBytes should equal(resourceAsByteArray(s"/mockdata/ytr/2345K_XX_12345.pdf"))
          }
        }
      }
    }

    "ei näe toisten koesuoritusta" in {
      get("koesuoritus/2345K_XX_12345.pdf", headers = kansalainenLoginHeaders("210244-374K")) {
        verifyResponseStatus(404, Nil)
      }
    }

    "ei näe koesuoritusta jota ei ole olemassa" in {
      get("koesuoritus/not-found-from-s3.pdf", headers = kansalainenLoginHeaders("080698-967F")) {
        verifyResponseStatus(404, Nil)
      }
    }
  }

  "Viranomainen" - {
    "ei näe koesuoritusta" in {
      authGet("koesuoritus/2345K_XX_12345.pdf", defaultUser) {
        verifyResponseStatus(403, Nil)
      }
    }
  }

  private def resourceAsByteArray(resourceName: String): Array[Byte] =
    ClasspathResource.resourceSerializer(resourceName)(inputStream => continually(inputStream.read).takeWhile(_ != -1).map(_.toByte).toArray).get
}
