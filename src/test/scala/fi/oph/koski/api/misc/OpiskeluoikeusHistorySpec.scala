package fi.oph.koski.api.misc

import fi.oph.koski.db.KoskiTables.{KoskiOpiskeluOikeudet, KoskiOpiskeluoikeusHistoria}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.{AmmatillinenOldExamples, ReforminMukainenErikoisammattitutkintoExample => Reformi}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.reformitutkinto
import fi.oph.koski.http.KoskiErrorCategory.notFound
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.{AuditLogTester, RootLogTester}
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeushistoriaErrorRepository, OpiskeluoikeushistoriaVirhe}
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.{DatabaseTestMethods, DirtiesFixtures, KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.JsonAST.{JArray, JNothing}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec

import java.time.{LocalDate, LocalDateTime}

class OpiskeluoikeusHistorySpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethodsAmmatillinen
    with HistoryTestMethods
    with DatabaseTestMethods
    with DirtiesFixtures {

  val uusiOpiskeluoikeus = defaultOpiskeluoikeus
  val oppija = KoskiSpecificMockOppijat.tyhjä

  "Muutoshistoria" - {
    "Luotaessa uusi opiskeluoikeus" - {
      "Luodaan historiarivi" in {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus, resetFixtures = true)
        verifyHistory(opiskeluoikeus.oid.get, List(1))
      }

      "osasuorituksilla" in {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, AmmatillinenOldExamples.full.opiskeluoikeudet(0), resetFixtures = true)
        verifyHistory(opiskeluoikeus.oid.get, List(1))
      }
    }
    "Päivitettäessä" - {
      "Luodaan uusi versiorivi" in {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus, resetFixtures = true)
        val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluoikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now)))
        verifyHistory(modified.oid.get, List(1, 2))
      }

      "Jos mikään ei ole muuttunut" - {
        "Ei luoda uutta versioriviä" in {
          val uusiAikaleima = Some(LocalDateTime.now())
          val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus, resetFixtures = true).copy(aikaleima = uusiAikaleima) // varmistetaan samalla että uusi arvo aikaleima-kentässä ei vaikuta vertailuun
          val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluoikeus)
          verifyHistory(modified.oid.get, List(1))
        }
      }

      "Kun syötteessä annetaan versionumero" - {
        "Versionumero sama kuin viimeisin" - {
          "Päivitys hyväksytään" in {
            val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus, resetFixtures = true)
            val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluoikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now), versionumero = Some(1)))
            verifyHistory(modified.oid.get, List(1, 2))
          }
        }

        "Versionumero ei täsmää" - {
          "Päivitys hylätään" in {
            val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus, resetFixtures = true)
            val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluoikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now), versionumero = Some(3)), {
              verifyResponseStatus(409, KoskiErrorCategory.conflict.versionumero("Annettu versionumero 3 <> 1"))
            })
            verifyHistory(modified.oid.get, List(1))
          }
        }
      }
    }

    "Käyttöoikeudet" - {
      "Kun haetaan historiaa opiskeluoikeudelle, johon käyttäjällä ei oikeuksia" - {
        "Palautetaan 404" in {
          val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
          authGet("api/opiskeluoikeus/historia/" + opiskeluoikeus.oid.get, MockUsers.omniaPalvelukäyttäjä) {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
          }
        }
      }
    }

    "Tuntematon id" - {
      "Palautetaan HTTP 400" in {
        authGet("api/opiskeluoikeus/historia/123456789") {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
        }
      }
    }
    "Versiohistorian hakeminen" - {
      "Onnistuu ja tuottaa auditlog-merkinnän" in {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus, resetFixtures = true)
        authGet("api/opiskeluoikeus/historia/" + opiskeluoikeus.oid.get) {
          val JArray(muutokset) = readHistory.head.muutos
          muutokset should not(be(empty))
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "MUUTOSHISTORIA_KATSOMINEN"))
        }
      }

      "Ei näytä muutoksia käyttäjälle jolta puuttuu LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli" in {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus, resetFixtures = true)
        authGet("api/opiskeluoikeus/historia/" + opiskeluoikeus.oid.get, user = MockUsers.stadinVastuukäyttäjä) {
          readHistory.map(_.muutos) should equal(List(JNothing))
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "MUUTOSHISTORIA_KATSOMINEN"))
        }
      }
    }

    "Yksittäisen version hakeminen" - {
      "Onnistuu ja tuottaa auditlog-merkinnän" in {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus, resetFixtures = true)
        authGet("api/opiskeluoikeus/historia/" + opiskeluoikeus.oid.get + "/1") {
          verifyResponseStatusOk()
          val versio = readOpiskeluoikeus
          versio should equal(opiskeluoikeus)
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "MUUTOSHISTORIA_KATSOMINEN"))
        }
      }
      "Tuntematon versionumero" - {
        "Palautetaan 404" in {
          val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus, resetFixtures = true)
          authGet("api/opiskeluoikeus/historia/" + opiskeluoikeus.oid.get + "/2") {
            verifyResponseStatus(404, ErrorMatcher.regex(notFound.versiotaEiLöydy, """Versiota 2 ei löydy opiskeluoikeuden [^ ]+ historiasta.""".r))
          }
        }
      }
    }

    "Virheentarkistus" - {
      "Virhe historiatietojen tuottamisesta lokitetaan" in {
        val suoritus = Reformi.tutkinnonSuoritus.copy(osasuoritukset = Some(Reformi.osasuoritukset.drop(1)))
        val opiskeluoikeus = putOpiskeluoikeus(Reformi.opiskeluoikeus.copy(suoritukset = List(suoritus)), henkilö = reformitutkinto) {
          val oo = readPutOppijaResponse.opiskeluoikeudet.head
          updateOpiskeluoikeusHistory(oo.oid, oo.versionumero, "[]")
          oo
        }

        putOpiskeluoikeus(Reformi.opiskeluoikeus.copy(suoritukset = List(Reformi.tutkinnonSuoritus)), henkilö = reformitutkinto) {
          verifyResponseStatusOk()
        }

        RootLogTester.getLogMessages.find(_.startsWith("Virhe")).get should equal(s"""
           |Virhe opiskeluoikeushistoriarivin tuottamisessa opiskeluoikeudelle ${opiskeluoikeus.oid}/${opiskeluoikeus.versionumero + 1}: [ {
           |  "op" : "copy",
           |  "path" : "/suoritukset/0/osasuoritukset/-",
           |  "from" : "/suoritukset/0/osasuoritukset/3"
           |} ]
          """.stripMargin.trim)

        opiskeluoikeushistoriaErrors.length should equal(1)

        resetFixtures() // Siivoa muutokset
      }

      "Virhe historiatiedoissa lokitetaan" in {
        val suoritus = Reformi.tutkinnonSuoritus.copy(osasuoritukset = Some(Reformi.osasuoritukset.drop(1)))
        putOpiskeluoikeus(Reformi.opiskeluoikeus.copy(suoritukset = List(suoritus)), henkilö = reformitutkinto) {
          val oo = readPutOppijaResponse.opiskeluoikeudet.head
          updateOpiskeluoikeusHistory(oo.oid, oo.versionumero, " [{\"op\": \"remove\", \"path\": \"/suoritukset/0/osasuoritukset/0/näyttö\"}]")
        }

        val opiskeluoikeus = putOpiskeluoikeus(Reformi.opiskeluoikeus.copy(suoritukset = List(Reformi.tutkinnonSuoritus)), henkilö = reformitutkinto) {
          val oo = readPutOppijaResponse.opiskeluoikeudet.head
          updateOpiskeluoikeusHistory(oo.oid, oo.versionumero, "[]")
          oo
        }

        putOpiskeluoikeus(Reformi.opiskeluoikeus.copy(suoritukset = List(suoritus)), henkilö = reformitutkinto) {
          verifyResponseStatusOk()
        }

        val logString = RootLogTester.getLogMessages.find(_.startsWith("Virhe opiskeluoikeushistorian validoinnissa")).get
        logString should equal(s"Virhe opiskeluoikeushistorian validoinnissa: Opiskeluoikeuden ${opiskeluoikeus.oid} historiaversion patch 4 epäonnistui")

        opiskeluoikeushistoriaErrors.length should equal(1)
      }
    }
  }

  private lazy val opiskeluoikeushistoriaErrorRepository = new OpiskeluoikeushistoriaErrorRepository(KoskiApplicationForTests.masterDatabase.db)

  private def updateOpiskeluoikeusHistory(oid: String, version: Int, muutos: String) = {
    val id = runDbSync(KoskiOpiskeluOikeudet.filter(_.oid === oid).map(_.id).result.head)
    runDbSync(
      KoskiOpiskeluoikeusHistoria.filter(row => row.opiskeluoikeusId === id && row.versionumero === version)
      .map(_.muutos)
      .update(JsonMethods.parse(muutos))
    )
  }

  private def opiskeluoikeushistoriaErrors: Seq[OpiskeluoikeushistoriaVirhe] =
    opiskeluoikeushistoriaErrorRepository.getAll
}
