package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData.{stadinAmmattiopisto, _}
import fi.oph.koski.documentation.ExampleData.{helsinki, vahvistus}
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExampleData}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.opiskeluoikeudenOidKonflikti
import fi.oph.koski.http.ErrorMatcher.exact
import fi.oph.koski.http.{ErrorMatcher, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.schema._
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.scalatest.FreeSpec

class OppijaValidationSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  "Opiskeluoikeuden lisääminen" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }

      "opiskeluoikeuden oid tunnisteen konflikti" in {
        putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus, henkilö = opiskeluoikeudenOidKonflikti) {
          verifyResponseStatusOk()
        }
        putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(oppilaitos = Some(Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu))), henkilö = opiskeluoikeudenOidKonflikti) {
          verifyResponseStatusOk()
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

        put("api/oppija", body = JsonMethods.compact(makeOppija(defaultHenkilö, List(defaultOpiskeluoikeus))), headers = authHeaders() ++ Map(("Content-type" -> "text/plain"))) {
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
        "palautetaan HTTP 400 virhe" in (putHenkilö(defaultHenkilö.copy(sukunimi = "")) (
          verifyResponseStatus(400, sukunimiPuuttuu)
        ))
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
            (verifyResponseStatusOk()))
        }
      }

      "Käytettäessä oppijan oidia" - {
        "Oid ok" - {
          "palautetaan HTTP 200"  in (putHenkilö(OidHenkilö(MockOppijat.eero.oid)) (verifyResponseStatusOk()))
        }

        "Oid virheellinen" - {
          "palautetaan HTTP 400"  in (putHenkilö(OidHenkilö("123.123.123")) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*henkilö.oid.*regularExpressionMismatch.*".r))))
        }

        "Oppijaa ei löydy oidilla" - {
          "palautetaan HTTP 404"  in (putHenkilö(OidHenkilö("1.2.246.562.24.19999999999")) (verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydy("Oppijaa 1.2.246.562.24.19999999999 ei löydy."))))
        }
      }

      "Käytettäessä oppijan kaikkia tietoja" - {
        "Oid ok" - {
          "palautetaan HTTP 200"  in (putHenkilö(MockOppijat.eero) (verifyResponseStatusOk()))
        }

        "Oid virheellinen" - {
          "palautetaan HTTP 400"  in (putHenkilö(TäydellisetHenkilötiedot("123.123.123", Some("010101-123N"), None, "Testi", "Testi", "Toivola", None, None)) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*henkilö.oid.*regularExpressionMismatch.*".r))))
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

      "Päivitettäessä opiskeluoikeus käyttäen sen oid:ia" - {
        "Oid ok" in {
          val opiskeluoikeus = lastOpiskeluoikeus(MockOppijat.eero.oid)
          putOppija(Oppija(MockOppijat.eero, List(opiskeluoikeus))) {
            verifyResponseStatusOk()
          }
        }

        "Tuntematon oid" in {
          val opiskeluoikeus = lastOpiskeluoikeus(MockOppijat.eero.oid)
          putOppija(Oppija(MockOppijat.eero, List(opiskeluoikeus.withOidAndVersion(oid = Some("1.2.246.562.15.15285175178"), versionumero = None)))) {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta 1.2.246.562.15.15285175178 ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
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
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"Organisaatio 1.2.246.562.10.346830761110 ei ole oppilaitos vaan koulutustoimija","errorType":"vääränTyyppinenOrganisaatio".*""".r)))
        }
      }

      "Kun opinto-oikeutta yritetään lisätä oppilaitokseen, jota ei löydy organisaatiopalvelusta" - {
        "palautetaan HTTP 400 virhe"  in { putOpiskeluoikeus(oppilaitoksella("1.2.246.562.10.146810761111")) (
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Organisaatiota 1.2.246.562.10.146810761111 ei löydy organisaatiopalvelusta","errorType":"organisaatioTuntematon"}.*""".r)))
        }
      }

      "Kun oppilaitoksen oid on virheellistä muotoa" - {
        "palautetaan HTTP 400 virhe"  in { putOpiskeluoikeus(oppilaitoksella("asdf")) (
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*opiskeluoikeudet.0.oppilaitos.oid.*regularExpressionMismatch.*".r)))
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
            verifyResponseStatusOk())
        }
      }
    }

    "Opiskeluoikeuden tila ja päivämäärät" - {
      "Alkaminen ja päättyminen" - {
        "Päivämäärät kunnossa -> palautetaan HTTP 200" in {
          putOpiskeluoikeus(defaultOpiskeluoikeus.copy(arvioituPäättymispäivä = Some(date(2018, 5, 31))))(verifyResponseStatusOk())
        }

        "alkamispäivä tänään -> palautetaan HTTP 200"  in (putOpiskeluoikeus(makeOpiskeluoikeus(alkamispäivä = LocalDate.now)) {
          verifyResponseStatusOk()
        })

        "alkamispäivä tulevaisuudessa -> palautetaan HTTP 200"  in (putOpiskeluoikeus(makeOpiskeluoikeus(alkamispäivä = date(2100, 5, 31))) {
          verifyResponseStatusOk()
        })

        "päättymispäivä tulevaisuudessa -> palautetaan HTTP 400"  in (putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2100, 5, 31))) {
          verifyResponseStatusOk()
        })

        "Päivämääräformaatti virheellinen -> palautetaan HTTP 400" in {
          def withAp(alkamispäivä: String) = {
            val apMap: JObject = JsonMethods.render(Map("alkamispäivä" -> alkamispäivä)).asInstanceOf[JObject];
            val suoritus = createOpiskeluoikeusWithMergedSuoritusJson(apMap).asInstanceOf[JObject];
            suoritus.merge(apMap)
          }

          putOpiskeluoikeusWithSomeMergedJson(Map[String, JValue]()) {
            verifyResponseStatusOk()
          }

          putOpiskeluoikeusWithSomeJson(Map("opiskeluoikeudet" -> List(withAp("2015-01.12")))) {
             verifyResponseStatusOk(400)
          }
        }

        "Väärä päivämääräjärjestys" - {
          "alkamispäivä > päättymispäivä"  in (putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(1999, 5, 31))) {
            verifyResponseStatus(400, List(
              exact(KoskiErrorCategory.badRequest.validation.date.päättymisPäiväEnnenAlkamispäivää, "alkamispäivä (2000-01-01) oltava sama tai aiempi kuin päättymispäivä(1999-05-31)"),
              exact(KoskiErrorCategory.badRequest.validation.date.opiskeluoikeusjaksojenPäivämäärät, "tila.opiskeluoikeusjaksot: 2000-01-01 oltava sama tai aiempi kuin 1999-05-31"),
              exact(KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenAlkamispäivää, "suoritus.alkamispäivä (2000-01-01) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(1999-05-31)")
            ))
          })

          "alkamispäivä > arvioituPäättymispäivä"  in (putOpiskeluoikeus(defaultOpiskeluoikeus.copy(arvioituPäättymispäivä = Some(date(1999, 5, 31)))){
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.arvioituPäättymisPäiväEnnenAlkamispäivää("alkamispäivä (2000-01-01) oltava sama tai aiempi kuin arvioituPäättymispäivä(1999-05-31)"))
          })

          "suoritus.vahvistus.päivä > päättymispäivä" in {
            val oo = päättymispäivällä(defaultOpiskeluoikeus, date(2017, 5, 31))
            val tutkinto: AmmatillinenPäätasonSuoritus = oo.suoritukset.map { case s: AmmatillisenTutkinnonSuoritus => s.copy(vahvistus = vahvistus(date(2017, 6, 30), stadinAmmattiopisto, Some(helsinki)))}.head

            putOpiskeluoikeus(oo.copy(suoritukset = List(tutkinto))) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.päättymispäiväEnnenVahvistusta("suoritus.vahvistus.päivä (2017-06-30) oltava sama tai aiempi kuin päättymispäivä(2017-05-31)"))
            }
          }
        }

        "Päivämäärät vs opiskeluoikeusjaksot" - {
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
          verifyResponseStatusOk()
        }
      }
      "ruotsi riittää" in {
        putOpiskeluoikeus(withName(LocalizedString.swedish("Något"))) {
          verifyResponseStatusOk()
        }
      }
      "englanti riittää" in {
        putOpiskeluoikeus(withName(LocalizedString.english("Something"))) {
          verifyResponseStatusOk()
        }
      }

      "vähintään yksi kieli vaaditaan" in {
        putOpiskeluoikeusWithSomeMergedJson(Map("tyyppi" -> Map("nimi" -> Map.empty[String, String]))) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
        }
      }
    }
  }

  def createOpiskeluoikeusWithMergedSuoritusJson(suoritus: JValue): JValue = {
    val opiskeluoikeus: JObject = JsonSerializer.serializeWithRoot(defaultOpiskeluoikeus).asInstanceOf[JObject];
    val firstSuoritus: JObject = (opiskeluoikeus \ "suoritukset")(0).asInstanceOf[JObject];
    opiskeluoikeus transformField {
      case JField("suoritukset", _) => ("suoritukset", List(firstSuoritus.merge(suoritus)))
    }
  }

  def putOpiskeluoikeusWithSomeMergedJson[A](opiskeluoikeus: JValue, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOpiskeluoikeusWithSomeJson(JsonSerializer.serializeWithRoot(defaultOpiskeluoikeus).merge(opiskeluoikeus), henkilö, headers)(f)
  }

  def putOpiskeluoikeusWithSomeJson[A](opiskeluoikeus: JValue, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(opiskeluoikeus)), headers)(f)
  }

}
