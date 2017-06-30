package fi.oph.koski.api


import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.email.{Email, EmailContent, EmailRecipient, MockEmailSender}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.eerola
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.MockUsers.helsinginKaupunkiPalvelukäyttäjä
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.schema._
import fi.oph.koski.tiedonsiirto._
import fi.oph.koski.util.PaginatedResponse
import org.scalatest.FreeSpec

class TiedonsiirtoSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val oppija = MockOppijat.tyhjä

  "Automaattinen tiedonsiirto" - {
    "Palvelukäyttäjä" - {
      "onnistuneesta tiedonsiirrosta tallennetaan vain henkilö- ja oppilaitostiedot" in {
        resetFixtures
        putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(200)
        }
        verifyTiedonsiirtoLoki(helsinginKaupunkiPalvelukäyttäjä, Some(defaultHenkilö), Some(ExamplesTiedonsiirto.opiskeluoikeus), errorStored = false, dataStored = false, expectedLähdejärjestelmä = Some("winnova"))
      }

      "epäonnistuneesta tiedonsiirrosta tallennetaan kaikki tiedot ja lähetetään email" in {
        MockEmailSender.checkMail
        resetFixtures
        putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        verifyTiedonsiirtoLoki(helsinginKaupunkiPalvelukäyttäjä, Some(defaultHenkilö), Some(ExamplesTiedonsiirto.opiskeluoikeus), errorStored = true, dataStored = true, expectedLähdejärjestelmä = Some("winnova"))
        val mails = MockEmailSender.checkMail
        mails should equal(List(Email(
          EmailContent(
            "no-reply@opintopolku.fi",
            "Virheellinen Koski-tiedonsiirto",
            "<p>Automaattisessa tiedonsiirrossa tapahtui virhe.</p><p>Käykää ystävällisesti tarkistamassa tapahtuneet tiedonsiirrot osoitteessa: http://localhost:7021/koski/tiedonsiirrot</p>",
            true),
          List(EmailRecipient("stadin-vastuu@example.com")))))
      }

      "toisesta peräkkäisestä epäonnistuneesta tiedonsiirrosta ei lähetetä emailia" in {
        putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        MockEmailSender.checkMail.length should equal(0)
      }

      "epäkelvosta json viestistä tallennetaan vain virhetiedot ja data" in {
        resetFixtures
        submit("put", "api/oppija", body = "not json".getBytes("UTF-8"), headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        KoskiApplicationForTests.elasticSearch.refreshIndex
        verifyTiedonsiirtoLoki(helsinginKaupunkiPalvelukäyttäjä, None, None, errorStored = true, dataStored = true, expectedLähdejärjestelmä = None)
      }
    }
  }

  "Muutos käyttöliittymästä" - {
    "ei tallenneta tiedonsiirtoja" in {
      resetFixtures
      putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus.copy(lähdejärjestelmänId = None), henkilö = defaultHenkilö, headers = authHeaders(MockUsers.stadinAmmattiopistoTallentaja) ++ jsonContent) {
        verifyResponseStatus(200)
      }
      getTiedonsiirrot(helsinginKaupunkiPalvelukäyttäjä) should be(empty)
    }
  }

  "Tiedonsiirtojen yhteenveto" in {
    resetFixtures
    putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
      verifyResponseStatus(200)
    }
    authGet("api/tiedonsiirrot/yhteenveto", user = MockUsers.helsinginKaupunkiPalvelukäyttäjä) {
      verifyResponseStatus(200)
      val yhteenveto = Json.read[List[TiedonsiirtoYhteenveto]](body)
      yhteenveto.length should be > 0
    }
  }

  "Tiedonsiirtojen yhteenveto ei näytä muiden organisaatioiden tietoja" in {
    resetFixtures
    putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
      verifyResponseStatus(200)
    }
    authGet("api/tiedonsiirrot/yhteenveto", user = MockUsers.omniaKatselija) {
      verifyResponseStatus(200)
      val yhteenveto = Json.read[List[TiedonsiirtoYhteenveto]](body)
      yhteenveto.length should be(0)
    }
  }

  "Tiedonsiirtolokin katsominen" - {
    val stadinOpiskeluoikeus = defaultOpiskeluoikeus.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId))
    "hierarkiassa ylempänä oleva käyttäjä voi katsoa hierarkiasssa alempana olevan käyttäjän luomia rivejä" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      verifyTiedonsiirtoLoki(helsinginKaupunkiPalvelukäyttäjä, Some(defaultHenkilö), Some(stadinOpiskeluoikeus), errorStored = false, dataStored = false, expectedLähdejärjestelmä = Some("winnova"))
    }

    "hierarkiassa alempana oleva käyttäjä näkee rivit, joiden oppilaitoksiin hänellä on katseluoikeus" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      getTiedonsiirrot(MockUsers.stadinAmmattiopistoTallentaja).length should equal(1)
    }

    "pääkäyttäjä näkee kaikki tiedonsiirrot" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = eerola, headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      val tiedonsiirtorivit: List[TiedonsiirtoRivi] = getTiedonsiirrot(MockUsers.paakayttaja).flatMap(_.rivit).filter(_.oppija.toList.flatMap(_.hetu).contains(eerola.hetu.get))
      val hetutRiveiltä = tiedonsiirtorivit.flatMap(_.oppija.flatMap(_.hetu)).filter(_ == eerola.hetu.get)
      hetutRiveiltä should equal(List(eerola.hetu.get))
    }

    "näytetään virheelliset" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      putOpiskeluoikeus(stadinOpiskeluoikeus, headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      getVirheellisetTiedonsiirrot(helsinginKaupunkiPalvelukäyttäjä).flatMap(_.rivit) should have size 1
    }

    "onnistunut siirto poistaa virheelliset listalta" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = eerola.copy(sukunimi = ""), headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinginKaupunkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      getVirheellisetTiedonsiirrot(helsinginKaupunkiPalvelukäyttäjä).flatMap(_.oppija.flatMap(_.hetu)) should equal(List(eerola.hetu.get))
    }
  }

  private def verifyTiedonsiirtoLoki(user: UserWithPassword, expectedHenkilö: Option[UusiHenkilö], expectedOpiskeluoikeus: Option[Opiskeluoikeus], errorStored: Boolean, dataStored: Boolean, expectedLähdejärjestelmä: Option[String]) {
    val tiedonsiirto = getTiedonsiirrot(user).head
    tiedonsiirto.oppija.flatMap(_.hetu) should equal(expectedHenkilö.flatMap(_.hetu))
    tiedonsiirto.rivit.flatMap(_.oppilaitos).map(_.oid) should equal(expectedOpiskeluoikeus.map(_.getOppilaitos.oid).toList)
    tiedonsiirto.rivit.flatMap(_.virhe).nonEmpty should be(errorStored)
    tiedonsiirto.rivit.flatMap(_.inputData).nonEmpty should be(dataStored)
    tiedonsiirto.rivit.foreach { rivi =>
      rivi.lähdejärjestelmä should equal(expectedLähdejärjestelmä)
    }
  }

  private def getTiedonsiirrot(user: UserWithPassword, url: String = "api/tiedonsiirrot"): List[HenkilönTiedonsiirrot] =
    authGet(url, user) {
      verifyResponseStatus(200)
      Json.read[PaginatedResponse[Tiedonsiirrot]](body).result.henkilöt
    }

  private def getVirheellisetTiedonsiirrot(user: UserWithPassword) = getTiedonsiirrot(user, "api/tiedonsiirrot/virheet")
}
