package fi.oph.koski.api

import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.MockUsers.{helsinkiPalvelukäyttäjä, stadinAmmattiopistoPalvelukäyttäjä}
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot.omnia
import fi.oph.koski.schema._
import fi.oph.koski.tiedonsiirto.HenkilönTiedonsiirrot
import org.scalatest.FreeSpec

class TiedonsiirtoSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val oppija: TäydellisetHenkilötiedot = MockOppijat.tyhjä
  val opiskeluoikeus: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
    lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId),
    oppilaitos = Oppilaitos(omnia),
    suoritukset = List(tutkintoSuoritus.copy(toimipiste = Oppilaitos(omnia)))
  )
  val palvelukäyttäjä = MockUsers.omniaPalvelukäyttäjä

  "Automaattinen tiedonsiirto" - {
    "Palvelukäyttäjä" - {
      "onnistuneesta tiedonsiirrosta tallennetaan vain henkilö- ja oppilaitostiedot" in {
        resetFixtures
        putOpiskeluOikeus(opiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(palvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(200)
        }
        verifyTiedonsiirtoLoki(palvelukäyttäjä, Some(defaultHenkilö), Some(opiskeluoikeus), errorStored = false, dataStored = false)
      }

      "epäonnistuneesta tiedonsiirrosta tallennetaan kaikki tiedot" in {
        resetFixtures
        putOpiskeluOikeus(opiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(palvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        verifyTiedonsiirtoLoki(palvelukäyttäjä, Some(defaultHenkilö), Some(opiskeluoikeus), errorStored = true, dataStored = true)
      }

      "epäkelposta json viestistä tallennetaan vain virhetiedot" in {
        resetFixtures
        submit("put", "api/oppija", body = "not json".getBytes("UTF-8"), headers = authHeaders(palvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400)
        }
        verifyTiedonsiirtoLoki(palvelukäyttäjä, None, None, errorStored = true, dataStored = false)
      }
    }
  }

  "Muutos käyttöliittymästä" - {
    "ei tallenneta tiedonsiirtoja" in {
      resetFixtures
      putOpiskeluOikeus(opiskeluoikeus.copy(lähdejärjestelmänId = None), henkilö = defaultHenkilö, headers = authHeaders(MockUsers.omniaTallentaja) ++ jsonContent) {
        verifyResponseStatus(200)
      }
      getTiedonsiirrot(palvelukäyttäjä) should be(empty)
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
      putOpiskeluOikeus(stadinOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(helsinkiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(200)
      }

      putOpiskeluOikeus(opiskeluoikeus, henkilö = defaultHenkilö.copy(sukunimi = ""), headers = authHeaders(palvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(400)
      }

      getTiedonsiirrot(MockUsers.paakayttaja).flatMap(_.rivit) should have size 2
    }
  }

  private def verifyTiedonsiirtoLoki(user: UserWithPassword, expectedHenkilö: Option[UusiHenkilö], expectedOpiskeluoikeus: Option[Opiskeluoikeus], errorStored: Boolean, dataStored: Boolean) {
    val tiedonsiirto = getTiedonsiirrot(user).head
    tiedonsiirto.oppija.flatMap(_.hetu) should equal(expectedHenkilö.map(_.hetu))
    tiedonsiirto.rivit.flatMap(_.oppilaitos.getOrElse(Nil)).map(_.oid) should equal(expectedOpiskeluoikeus.map(_.oppilaitos.oid).toList)
    tiedonsiirto.rivit.flatMap(_.virhe).nonEmpty should be(errorStored)
    tiedonsiirto.rivit.flatMap(_.inputData).nonEmpty should be(errorStored)
  }

  private def getTiedonsiirrot(user: UserWithPassword): List[HenkilönTiedonsiirrot] =
    authGet("api/tiedonsiirrot", user) {
      verifyResponseStatus(200)
      Json.read[List[HenkilönTiedonsiirrot]](body)
    }
}
