package fi.oph.koski.api


import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.MockUsers.{helsinkiPalvelukäyttäjä, stadinAmmattiopistoPalvelukäyttäjä}
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.oppija.MockOppijat.eerola
import fi.oph.koski.schema._
import fi.oph.koski.tiedonsiirto.{ExamplesTiedonsiirto, HenkilönTiedonsiirrot}
import org.scalatest.FreeSpec

class TiedonsiirtoSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val oppija: TäydellisetHenkilötiedot = MockOppijat.tyhjä

  "Automaattinen tiedonsiirto" - {
    "Palvelukäyttäjä" - {
      "onnistuneesta tiedonsiirrosta tallennetaan vain henkilö- ja oppilaitostiedot" in {
        resetFixtures
        putOpiskeluOikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(200)
        }
        verifyTiedonsiirtoLoki(stadinAmmattiopistoPalvelukäyttäjä, Some(defaultHenkilö), Some(ExamplesTiedonsiirto.opiskeluoikeus), errorStored = false, dataStored = false)
      }

      "epäonnistuneesta tiedonsiirrosta tallennetaan kaikki tiedot" in {
        resetFixtures
        putOpiskeluOikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        verifyTiedonsiirtoLoki(stadinAmmattiopistoPalvelukäyttäjä, Some(defaultHenkilö), Some(ExamplesTiedonsiirto.opiskeluoikeus), errorStored = true, dataStored = true)
      }

      "epäkelposta json viestistä tallennetaan vain virhetiedot" in {
        resetFixtures
        submit("put", "api/oppija", body = "not json".getBytes("UTF-8"), headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        verifyTiedonsiirtoLoki(stadinAmmattiopistoPalvelukäyttäjä, None, None, errorStored = true, dataStored = false)
      }
    }
  }

  "Muutos käyttöliittymästä" - {
    "ei tallenneta tiedonsiirtoja" in {
      resetFixtures
      putOpiskeluOikeus(ExamplesTiedonsiirto.opiskeluoikeus.copy(lähdejärjestelmänId = None), henkilö = defaultHenkilö, headers = authHeaders(MockUsers.stadinAmmattiopistoTallentaja) ++ jsonContent) {
        verifyResponseStatus(200)
      }
      getTiedonsiirrot(helsinkiPalvelukäyttäjä) should be(empty)
    }
  }

  "Tiedonsiirtolokin katsominen" - {
    val stadinOpiskeluoikeus = defaultOpiskeluoikeus.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId))
    "hierarkiassa ylempänä oleva käyttäjä voi katsoa hierarkiasssa alempana olevan käyttäjän luomia rivejä" in {
      resetFixtures
      putOpiskeluOikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      verifyTiedonsiirtoLoki(helsinkiPalvelukäyttäjä, Some(defaultHenkilö), Some(stadinOpiskeluoikeus), errorStored = false, dataStored = false)
    }

    "hierarkiassa alempana oleva käyttäjä ei voi katsoa hierarkiasssa ylempänä olevan käyttäjän luomia rivejä" in {
      resetFixtures
      putOpiskeluOikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      getTiedonsiirrot(stadinAmmattiopistoPalvelukäyttäjä) should be(empty)
    }

    "pääkäyttäjä näkee kaikki tiedonsiirrot" in {
      resetFixtures
      putOpiskeluOikeus(stadinOpiskeluoikeus, henkilö = eerola, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      putOpiskeluOikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = eerola.copy(sukunimi = ""), headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      getTiedonsiirrot(MockUsers.paakayttaja).flatMap(_.rivit).flatMap(_.oppija.flatMap(_.hetu)).filter(_ == eerola.hetu) should equal(List(eerola.hetu, eerola.hetu))
    }

    "näytetään virheelliset" in {
      resetFixtures
      putOpiskeluOikeus(stadinOpiskeluoikeus, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      putOpiskeluOikeus(stadinOpiskeluoikeus, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      putOpiskeluOikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      getVirheellisetTiedonsiirrot(helsinkiPalvelukäyttäjä).flatMap(_.rivit) should have size 1
    }

    "onnistunut siirto poistaa virheelliset listalta" in {
      resetFixtures
      putOpiskeluOikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      putOpiskeluOikeus(stadinOpiskeluoikeus, henkilö = eerola.copy(sukunimi = ""), headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      putOpiskeluOikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      getVirheellisetTiedonsiirrot(helsinkiPalvelukäyttäjä).flatMap(_.oppija.flatMap(_.hetu)) should equal(List(eerola.hetu))
    }
  }

  private def verifyTiedonsiirtoLoki(user: UserWithPassword, expectedHenkilö: Option[UusiHenkilö], expectedOpiskeluoikeus: Option[Opiskeluoikeus], errorStored: Boolean, dataStored: Boolean) {
    val tiedonsiirto = getTiedonsiirrot(user).head
    tiedonsiirto.oppija.flatMap(_.hetu) should equal(expectedHenkilö.map(_.hetu))
    tiedonsiirto.rivit.flatMap(_.oppilaitos.getOrElse(Nil)).map(_.oid) should equal(expectedOpiskeluoikeus.map(_.oppilaitos.oid).toList)
    tiedonsiirto.rivit.flatMap(_.virhe).nonEmpty should be(errorStored)
    tiedonsiirto.rivit.flatMap(_.inputData).nonEmpty should be(errorStored)
  }

  private def getTiedonsiirrot(user: UserWithPassword, url: String = "api/tiedonsiirrot"): List[HenkilönTiedonsiirrot] =
    authGet(url, user) {
      verifyResponseStatus(200)
      Json.read[List[HenkilönTiedonsiirrot]](body)
    }

  private def getVirheellisetTiedonsiirrot(user: UserWithPassword) = getTiedonsiirrot(user, "api/tiedonsiirrot/virheet")
}
