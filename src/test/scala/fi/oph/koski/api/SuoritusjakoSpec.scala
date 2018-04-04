package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import org.scalatest.FreeSpec

class SuoritusjakoSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {
  "Suoritusjaon lisääminen" - {
    "onnistuu" - {
      "yhdellä oikeellisella suorituksella" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatusOk()
        }
      }

      "kahdella oikeellisella suorituksella" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }, {
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "6"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatusOk()
        }
      }
    }

    "epäonnistuu" - {
      "yhdellä suorituksella, jota ei löydy" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "9"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(404, KoskiErrorCategory.notFound.suoritustaEiLöydy())
        }
      }

      "kahdella suorituksella, joista toista ei löydy" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }, {
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "9"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(404, KoskiErrorCategory.notFound.suoritustaEiLöydy())
        }
      }

      "puuttellisilla tiedoilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }

      "ylimääräisillä tiedoilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7",
          "extra": "extra"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }

      "epäkelvolla JSON-dokumentilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.json("Epäkelpo JSON-dokumentti"))
        }
      }

      "tyhjällä suorituslistalla" in {
        val json = "[]"

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }
    }
  }
}
