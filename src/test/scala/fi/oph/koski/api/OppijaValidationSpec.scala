package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExampleData}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.schema._
import org.json4s._
import org.scalatest.FreeSpec

class OppijaValidationSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  "Opiskeluoikeuden lisääminen" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(200)
        }
      }
    }

    "Ilman tunnistautumista" - {
      "palautetaan HTTP 401" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus, headers = jsonContent) {
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
        }
      }
    }

    "Epäkelpo JSON-dokumentti" - {
      "palautetaan HTTP 400 virhe"  in (request("api/oppija", "application/json", "not json", "put")
        (verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.json("Epäkelpo JSON-dokumentti"))))
    }

    "Väärä Content-Type" - {
      "palautetaan HTTP 415" in {

        put("api/oppija", body = Json.write(makeOppija(defaultHenkilö, List(defaultOpiskeluoikeus))), headers = authHeaders() ++ Map(("Content-type" -> "text/plain"))) {
          verifyResponseStatus(415, KoskiErrorCategory.unsupportedMediaType.jsonOnly("Wrong content type: only application/json content type with UTF-8 encoding allowed"))
        }
      }
    }

    "Omien tietojen muokkaaminen" - {
      "On estetty" in {
        putOpiskeluoikeus(opiskeluoikeus(oppilaitos = Oppilaitos(omnia), tutkinto = AmmatillinenExampleData.autoalanPerustutkinnonSuoritus(Oppilaitos(omnia))),
                          henkilö = MockOppijat.omattiedot,
                          headers = authHeaders(MockUsers.omattiedot) ++ jsonContent) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
        }
      }
    }

    "Henkilötiedot" - {
      "Nimenä tyhjä merkkijono" - {
        "palautetaan HTTP 400 virhe"  in (putHenkilö(defaultHenkilö.copy(sukunimi = "")) (verifyResponseStatus(400)))
      }


      "Hetun ollessa" - {
        "muodoltaan virheellinen" - {
          "palautetaan HTTP 400 virhe"  in (putHenkilö(defaultHenkilö.copy(hetu = "010101-123123"))
            (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen muoto hetulla: 010101-123123"))))
        }
        "muodoltaan oikea, mutta väärä tarkistusmerkki" - {
          "palautetaan HTTP 400 virhe"  in (putHenkilö(defaultHenkilö.copy(hetu = "010101-123P"))
            (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen tarkistusmerkki hetussa: 010101-123P"))))
        }
        "päivämäärältään tulevaisuudessa" - {
          "palautetaan HTTP 400 virhe"  in (putHenkilö(defaultHenkilö.copy(hetu = "141299A903C"))
            (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Syntymäpäivä hetussa: 141299A903C on tulevaisuudessa"))))
        }
        "päivämäärältään virheellinen" - {
          "palautetaan HTTP 400 virhe"  in (putHenkilö(defaultHenkilö.copy(hetu = "300215-123T"))
            (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen syntymäpäivä hetulla: 300215-123T"))))
        }
        "keinotekoinen (yksilönumero on 9-alkuinen)" - {
          "palautetaan HTTP 400 virhe"  in (putHenkilö(defaultHenkilö.copy(hetu = "091196-935L"))
          (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Keinotekoinen henkilötunnus: 091196-935L"))))
        }
        "validi" - {
          "palautetaan HTTP 200"  in (putHenkilö(defaultHenkilö.copy(hetu = "010101-123N"))
            (verifyResponseStatus(200)))
        }
      }

      "Käytettäessä oppijan oidia" - {
        "Oid ok" - {
          "palautetaan HTTP 200"  in (putHenkilö(OidHenkilö(MockOppijat.eero.oid)) (verifyResponseStatus(200)))
        }

        "Oid virheellinen" - {
          "palautetaan HTTP 400"  in (putHenkilö(OidHenkilö("123.123.123")) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*henkilö.oid.*regularExpressionMismatch.*".r))))
        }

        "Oppijaa ei löydy oidilla" - {
          "palautetaan HTTP 404"  in (putHenkilö(OidHenkilö("1.2.246.562.24.19999999999")) (verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydy("Oppijaa 1.2.246.562.24.19999999999 ei löydy."))))
        }
      }

      "Käytettäessä oppijan kaikkia tietoja" - {
        "Oid ok" - {
          "palautetaan HTTP 200"  in (putHenkilö(MockOppijat.eero) (verifyResponseStatus(200)))
        }

        "Oid virheellinen" - {
          "palautetaan HTTP 400"  in (putHenkilö(TäydellisetHenkilötiedot("123.123.123", "010101-123N", "Testi", "Testi", "Toivola", None, None)) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*henkilö.oid.*regularExpressionMismatch.*".r))))
        }
      }
    }

    "Opiskeluoikeudet" - {
      "Jos lähetetään 0 opiskeluoikeutta" - {
        "palautetaan HTTP 400" in {
          putOppija(Oppija(defaultHenkilö, List())) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tyhjäOpiskeluoikeusLista("Annettiin tyhjä lista opiskeluoikeuksia."))
          }
        }
      }

      "Päivitettäessä opiskeluoikeus käyttäen sen id:tä" - {
        "Id ok" in {
          val opiskeluoikeus = lastOpiskeluoikeus(MockOppijat.eero.oid)
          putOppija(Oppija(MockOppijat.eero, List(opiskeluoikeus))) {
            verifyResponseStatus(200)
          }
        }

        "Tuntematon id" in {
          val opiskeluoikeus = lastOpiskeluoikeus(MockOppijat.eero.oid)
          putOppija(Oppija(MockOppijat.eero, List(opiskeluoikeus.withIdAndVersion(id = Some(0), versionumero = None)))) {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta 0 ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
          }
        }
      }
    }

    "Oppilaitos" - {
      def oppilaitoksella(oid: String) = defaultOpiskeluoikeus.copy(oppilaitos = Some(Oppilaitos(oid)))

      "Kun opinto-oikeutta yritetään lisätä oppilaitokseen, johon käyttäjällä ei ole oikeuksia" - {
        "palautetaan HTTP 403 virhe"  in { putOpiskeluoikeus(oppilaitoksella("1.2.246.562.10.93135224694"), headers = authHeaders(MockUsers.omniaPalvelukäyttäjä) ++ jsonContent) (
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.93135224694")))
        }
      }

      "Kun opinto-oikeutta yritetään lisätä koulutustoimijaan oppilaitoksen sijaan" - {
        "palautetaan HTTP 400 virhe"  in { putOpiskeluoikeus(oppilaitoksella("1.2.246.562.10.346830761110")) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.vääränTyyppinen("Organisaatio 1.2.246.562.10.346830761110 ei ole oppilaitos vaan koulutustoimija")))
        }
      }

      "Kun opinto-oikeutta yritetään lisätä oppilaitokseen, jota ei löydy organisaatiopalvelusta" - {
        "palautetaan HTTP 400 virhe"  in { putOpiskeluoikeus(oppilaitoksella("1.2.246.562.10.146810761111")) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.tuntematon("Organisaatiota 1.2.246.562.10.146810761111 ei löydy organisaatiopalvelusta")))
        }
      }

      "Kun oppilaitoksen oid on virheellistä muotoa" - {
        "palautetaan HTTP 400 virhe"  in { putOpiskeluoikeus(oppilaitoksella("asdf")) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*opiskeluoikeudet.0.oppilaitos.oid.*regularExpressionMismatch.*".r)))
        }
      }

      "Kun annetaan koulutustoimija, joka ei vastaa organisaatiopalvelun oppilaitokselle määrittämää koulutustoimijaa" - {
        "palautetaan HTTP 400 virhe"  in { putOpiskeluoikeus(defaultOpiskeluoikeus.copy(koulutustoimija = Some(Koulutustoimija("1.2.246.562.10.53814745062")))) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.vääräKoulutustoimija("Annettu koulutustoimija 1.2.246.562.10.53814745062 ei vastaa organisaatiopalvelusta löytyvää koulutustoimijaa 1.2.246.562.10.346830761110")))
        }
      }
    }

    "Suorituksen toimipiste" - {
      def toimipisteellä(oid: String) = defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(toimipiste = OidOrganisaatio(oid))))

      "Kun yritetään käyttää toimipistettä, johon käyttäjällä ei ole oikeuksia" - {
        "palautetaan HTTP 403 virhe"  in { putOpiskeluoikeus(toimipisteellä(MockOrganisaatiot.helsinginKaupunki)) (
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + MockOrganisaatiot.helsinginKaupunki)))
        }
      }

      "Kun toimipiste ei ole oppilaitoksen aliorganisaatio" - {
        "Palautetaan HTTP 200" in {
          putOpiskeluoikeus(toimipisteellä(MockOrganisaatiot.omnia)) (
            verifyResponseStatus(200))
        }
      }
    }

    "Opiskeluoikeuden tila ja päivämäärät" - {
      "Alkaminen ja päättyminen" - {
        "Päivämäärät kunnossa -> palautetaan HTTP 200" in {
          putOpiskeluoikeus(defaultOpiskeluoikeus.copy(arvioituPäättymispäivä = Some(date(2018, 5, 31))))(verifyResponseStatus(200))
        }

        "alkamispäivä tänään -> palautetaan HTTP 200"  in (putOpiskeluoikeus(makeOpiskeluoikeus(alkamispäivä = LocalDate.now)) {
          verifyResponseStatus(200)
        })

        "alkamispäivä tulevaisuudessa -> palautetaan HTTP 200"  in (putOpiskeluoikeus(makeOpiskeluoikeus(alkamispäivä = date(2100, 5, 31))) {
          verifyResponseStatus(200)
        })

        "päättymispäivä tulevaisuudessa -> palautetaan HTTP 400"  in (putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2100, 5, 31))) {
          verifyResponseStatus(400,
            KoskiErrorCategory.badRequest.validation.date.tulevaisuudessa("Päivämäärä päättymispäivä (2100-05-31) on tulevaisuudessa"),
            KoskiErrorCategory.badRequest.validation.date.tulevaisuudessa("Päivämäärä suoritus.vahvistus.päivä (2100-05-31) on tulevaisuudessa")
          )
        })

        "Päivämääräformaatti virheellinen -> palautetaan HTTP 400" in {
          putOpiskeluoikeusWithSomeMergedJson(Map("alkamispäivä" -> "2015.01-12")){
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*opiskeluoikeudet.0.alkamispäivä.*yyyy-MM-dd.*".r))
          }
        }
        "Päivämäärä virheellinen -> palautetaan HTTP 400" in {
          putOpiskeluoikeusWithSomeMergedJson(Map("alkamispäivä" -> "2015-01-32")){
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*opiskeluoikeudet.0.alkamispäivä.*yyyy-MM-dd.*".r))
          }
        }

        "Väärä päivämääräjärjestys" - {
          "alkamispäivä > päättymispäivä"  in (putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(1999, 5, 31))) {
            verifyResponseStatus(400,
              KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua("alkamispäivä (2000-01-01) oltava sama tai aiempi kuin päättymispäivä(1999-05-31)"),
              KoskiErrorCategory.badRequest.validation.date.jaksojenJärjestys("tila.opiskeluoikeusjaksot: 2000-01-01 oltava sama tai aiempi kuin 1999-05-31"),
              KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua ("suoritus.alkamispäivä (2000-01-01) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(1999-05-31)")
            )
          })

          "alkamispäivä > arvioituPäättymispäivä"  in (putOpiskeluoikeus(defaultOpiskeluoikeus.copy(arvioituPäättymispäivä = Some(date(1999, 5, 31)))){
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua("alkamispäivä (2000-01-01) oltava sama tai aiempi kuin arvioituPäättymispäivä(1999-05-31)"))
          })
        }

        "Päivämäärät vs opiskeluoikeusjaksot" - {
          "alkamispäivä puuttuu, vaikka opiskeluoikeusjakso on olemassa" in { putOpiskeluoikeus(defaultOpiskeluoikeus.copy(alkamispäivä = None)) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.alkamispäivä("Opiskeluoikeuden alkamispäivä (null) ei vastaa ensimmäisen opiskeluoikeusjakson alkupäivää (2000-01-01)"))
          }}
          "alkamispäivä ei vastaa ensimmäisen opiskeluoikeusjakson päivämäärää" in { putOpiskeluoikeus(defaultOpiskeluoikeus.copy(alkamispäivä = Some(date(1999, 12, 31)))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.alkamispäivä("Opiskeluoikeuden alkamispäivä (1999-12-31) ei vastaa ensimmäisen opiskeluoikeusjakson alkupäivää (2000-01-01)"))
          }}
          "päättymispäivä on annettu, vaikka viimeinen opiskeluoikeus on tilassa Läsnä" in { putOpiskeluoikeus(defaultOpiskeluoikeus.copy(päättymispäivä = Some(date(2010, 12, 31)))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.päättymispäivämäärä("Opiskeluoikeuden päättymispäivä (2010-12-31) ei vastaa opiskeluoikeuden päättävän opiskeluoikeusjakson alkupäivää (null)"))
          }}
          "päättymispäivä ei vastaa opiskelut päättävän jakson päivää" in { putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2010, 12, 30)).copy(päättymispäivä = Some(date(2010, 12, 31)))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.päättymispäivämäärä("Opiskeluoikeuden päättymispäivä (2010-12-31) ei vastaa opiskeluoikeuden päättävän opiskeluoikeusjakson alkupäivää (2010-12-30)"))
          }}
        }

        "Opiskeluoikeuden tila muuttunut vielä valmistumisen jälkeen -> HTTP 400" in (putOpiskeluoikeus(
          lisääTila(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)), date(2016, 6, 30), ExampleData.opiskeluoikeusLäsnä)
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaMuuttunutLopullisenTilanJälkeen("Opiskeluoikeuden tila muuttunut lopullisen tilan (valmistunut) jälkeen"))
        })
      }
    }

    "Lokalisoidut tekstit" - {
      def withName(name: LocalizedString) = defaultOpiskeluoikeus.copy(tyyppi = defaultOpiskeluoikeus.tyyppi.copy(nimi = Some(name)))

      "suomi riittää" in {
        putOpiskeluoikeus(withName(LocalizedString.finnish("Jotain"))) {
          verifyResponseStatus(200)
        }
      }
      "ruotsi riittää" in {
        putOpiskeluoikeus(withName(LocalizedString.swedish("Något"))) {
          verifyResponseStatus(200)
        }
      }
      "englanti riittää" in {
        putOpiskeluoikeus(withName(LocalizedString.english("Something"))) {
          verifyResponseStatus(200)
        }
      }

      "vähintään yksi kieli vaaditaan" in {
        putOpiskeluoikeusWithSomeMergedJson(Map("tyyppi" -> Map("nimi" -> Map()))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*notAnyOf.*".r))
        }
      }
    }
  }

  def putOpiskeluoikeusWithSomeMergedJson[A](opiskeluoikeus: JValue, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(Json.toJValue(defaultOpiskeluoikeus).merge(opiskeluoikeus))), headers)(f)
  }

}
