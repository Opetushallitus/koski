package fi.oph.koski.api

import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{UserWithPassword, MockUser, MockUsers}
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot.omnia
import fi.oph.koski.schema.{OrganisaatioWithOid, Oppilaitos, TäydellisetHenkilötiedot}
import fi.oph.koski.tiedonsiirto.HenkilönTiedonsiirrot
import org.scalatest.FreeSpec

class TiedonsiirtoSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val oppija: TäydellisetHenkilötiedot = MockOppijat.tyhjä

  "Automaattinen tiedonsiirto" - {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(
      lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId),
      oppilaitos = Oppilaitos(omnia),
      suoritukset = List(tutkintoSuoritus.copy(toimipiste = Oppilaitos(omnia)))
    )
    val palvelukäyttäjä: MockUser = MockUsers.hiiri

    "Palvelukäyttäjä" - {
      "tallennetaan onnistunut tiedonsiirto" in {
        resetFixtures
        putOpiskeluOikeus(opiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(palvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(200)
        }
        val tiedonsiirto = getTiedonsiirrot(palvelukäyttäjä).head
        tiedonsiirto.oppija.get.hetu.get should equal(defaultHenkilö.hetu)
        tiedonsiirto.rivit.head.oppilaitos.get.head.oid should equal(opiskeluoikeus.oppilaitos.oid)
      }
      "tallennetaan epäonnistunut tiedonsiirto" in { }
    }
    "Muu käyttäjä" - {
      "tulee virhe" in { }
    }
  }

  "Muutos käyttöliittymästä" - {
    "ei tallenneta tiedonsiirtoja" in { }
  }

  private def getTiedonsiirrot(user: UserWithPassword): List[HenkilönTiedonsiirrot] =
    authGet("api/tiedonsiirrot", user) {
      verifyResponseStatus(200)
      Json.read[List[HenkilönTiedonsiirrot]](body)
    }
}
