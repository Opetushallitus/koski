package fi.oph.koski.api


import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.email.{Email, EmailContent, EmailRecipient, MockEmailSender}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.MockUsers.{helsinkiPalvelukäyttäjä, stadinAmmattiopistoPalvelukäyttäjä}
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import MockOppijat.eerola
import fi.oph.koski.schema._
import fi.oph.koski.tiedonsiirto.{ExamplesTiedonsiirto, HenkilönTiedonsiirrot, Tiedonsiirrot, TiedonsiirtoYhteenveto}
import fi.oph.koski.util.PaginatedResponse
import org.scalatest.FreeSpec

class TiedonsiirtoSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val oppija: TäydellisetHenkilötiedot = MockOppijat.tyhjä

  "Automaattinen tiedonsiirto" - {
    "Palvelukäyttäjä" - {
      "onnistuneesta tiedonsiirrosta tallennetaan vain henkilö- ja oppilaitostiedot" in {
        resetFixtures
        putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(200)
        }
        verifyTiedonsiirtoLoki(stadinAmmattiopistoPalvelukäyttäjä, Some(defaultHenkilö), Some(ExamplesTiedonsiirto.opiskeluoikeus), errorStored = false, dataStored = false, expectedLähdejärjestelmä = Some("winnova"))
      }

      "epäonnistuneesta tiedonsiirrosta tallennetaan kaikki tiedot ja lähetetään email" in {
        MockEmailSender.checkMail
        resetFixtures
        putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        verifyTiedonsiirtoLoki(stadinAmmattiopistoPalvelukäyttäjä, Some(defaultHenkilö), Some(ExamplesTiedonsiirto.opiskeluoikeus), errorStored = true, dataStored = true, expectedLähdejärjestelmä = Some("winnova"))
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
        putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        MockEmailSender.checkMail.length should equal(0)
      }

      "epäkelvosta json viestistä tallennetaan vain virhetiedot ja data" in {
        resetFixtures
        submit("put", "api/oppija", body = "not json".getBytes("UTF-8"), headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        verifyTiedonsiirtoLoki(stadinAmmattiopistoPalvelukäyttäjä, None, None, errorStored = true, dataStored = true, expectedLähdejärjestelmä = None)
      }
    }
  }

  "Muutos käyttöliittymästä" - {
    "ei tallenneta tiedonsiirtoja" in {
      resetFixtures
      putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus.copy(lähdejärjestelmänId = None), henkilö = defaultHenkilö, headers = authHeaders(MockUsers.stadinAmmattiopistoTallentaja) ++ jsonContent) {
        verifyResponseStatus(200)
      }
      getTiedonsiirrot(helsinkiPalvelukäyttäjä) should be(empty)
    }
  }

  "Tiedonsiirtojen yhteenveto" in {
    authGet("api/tiedonsiirrot/yhteenveto") {
      verifyResponseStatus(200)
      val yhteenveto = Json.read[List[TiedonsiirtoYhteenveto]](body)
      yhteenveto.length should be > 0
    }
  }

  "Tiedonsiirtolokin katsominen" - {
    val stadinOpiskeluoikeus = defaultOpiskeluoikeus.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId))
    "hierarkiassa ylempänä oleva käyttäjä voi katsoa hierarkiasssa alempana olevan käyttäjän luomia rivejä" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      verifyTiedonsiirtoLoki(helsinkiPalvelukäyttäjä, Some(defaultHenkilö), Some(stadinOpiskeluoikeus), errorStored = false, dataStored = false, expectedLähdejärjestelmä = Some("winnova"))
    }

    "hierarkiassa alempana oleva käyttäjä ei voi katsoa hierarkiasssa ylempänä olevan käyttäjän luomia rivejä" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      getTiedonsiirrot(stadinAmmattiopistoPalvelukäyttäjä) should be(empty)
    }

    "pääkäyttäjä näkee kaikki tiedonsiirrot" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = eerola, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      putOpiskeluoikeus(ExamplesTiedonsiirto.opiskeluoikeus, henkilö = eerola.copy(sukunimi = ""), headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      getTiedonsiirrot(MockUsers.paakayttaja).flatMap(_.rivit).flatMap(_.oppija.flatMap(_.hetu)).filter(_ == eerola.hetu) should equal(List(eerola.hetu, eerola.hetu))
    }

    "näytetään virheelliset" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      putOpiskeluoikeus(stadinOpiskeluoikeus, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      getVirheellisetTiedonsiirrot(helsinkiPalvelukäyttäjä).flatMap(_.rivit) should have size 1
    }

    "onnistunut siirto poistaa virheelliset listalta" in {
      resetFixtures
      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = eerola.copy(sukunimi = ""), headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      putOpiskeluoikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      getVirheellisetTiedonsiirrot(helsinkiPalvelukäyttäjä).flatMap(_.oppija.flatMap(_.hetu)) should equal(List(eerola.hetu))
    }
  }

  private def verifyTiedonsiirtoLoki(user: UserWithPassword, expectedHenkilö: Option[UusiHenkilö], expectedOpiskeluoikeus: Option[Opiskeluoikeus], errorStored: Boolean, dataStored: Boolean, expectedLähdejärjestelmä: Option[String]) {
    val tiedonsiirto = getTiedonsiirrot(user).head
    tiedonsiirto.oppija.flatMap(_.hetu) should equal(expectedHenkilö.map(_.hetu))
    tiedonsiirto.rivit.flatMap(_.oppilaitos).map(_.oid) should equal(expectedOpiskeluoikeus.map(_.oppilaitos.oid).toList)
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
