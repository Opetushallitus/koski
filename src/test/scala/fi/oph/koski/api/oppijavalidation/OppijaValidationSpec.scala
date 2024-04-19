package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.AmmatillinenExampleData.{stadinAmmattiopisto, _}
import fi.oph.koski.documentation.ExampleData.{helsinki, vahvistus}
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.opiskeluoikeudenOidKonflikti
import fi.oph.koski.http.ErrorMatcher.exact
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.schema._
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import scala.io.Source

class OppijaValidationSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  "Opiskeluoikeuden lisääminen" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }

      "opiskeluoikeuden oid tunnisteen konflikti" in {
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus, henkilö = opiskeluoikeudenOidKonflikti) {
          verifyResponseStatusOk()
        }
        putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(oppilaitos = Some(Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu))), henkilö = opiskeluoikeudenOidKonflikti) {
          verifyResponseStatusOk()
        }
      }
    }

    "Ilman tunnistautumista" - {
      "palautetaan HTTP 401" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus, headers = jsonContent) {
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
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus(oppilaitos = Oppilaitos(omnia), tutkinto = AmmatillinenExampleData.autoalanPerustutkinnonSuoritus(Oppilaitos(omnia))),
                                      henkilö = KoskiSpecificMockOppijat.omattiedot,
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
        "keinotekoinen (yksilönumero on 9-alkuinen)" - {
          "palautetaan HTTP 400 virhe" in (putHenkilö(defaultHenkilö.copy(hetu = "091196-935L"))
          (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Keinotekoinen henkilötunnus: 091196-935L"))))
        }
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
        "validi" - {
          "palautetaan HTTP 200"  in (putHenkilö(defaultHenkilö.copy(hetu = "010101-123N"))
            (verifyResponseStatusOk()))
        }
      }

      "Kun kutsumanimi" - {
        "puuttuu" - {
          "kutsumanimeksi kopioidaan ensimmäinen etunimi" in {
            putHenkilö(UusiHenkilö("020785-515F", "Pekka Juhani", None, "Mykkänen"))(verifyResponseStatusOk())
            oppijaByHetu("020785-515F").henkilö.asInstanceOf[TäydellisetHenkilötiedot].kutsumanimi should equal("Pekka")
          }
        }

        "ei ole yksi etunimistä" - {
          "kutsumanimeksi kopioidaan ensimmäinen etunimi" in {
            putHenkilö(UusiHenkilö("131078-0816", "Pekka Juhani", Some("Ei etunimi"), "Mykkänen"))(verifyResponseStatusOk())
            oppijaByHetu("131078-0816").henkilö.asInstanceOf[TäydellisetHenkilötiedot].kutsumanimi should equal("Pekka")
          }
        }

        "on annettu ja oikein" - {
          "käytetään annettua kutsumanimeä" in {
            putHenkilö(UusiHenkilö("180808-760M", "Johanna Tia-Maria", Some("Tia-Maria"), "Mykkänen"))(verifyResponseStatusOk())
            oppijaByHetu("180808-760M").henkilö.asInstanceOf[TäydellisetHenkilötiedot].kutsumanimi should equal("Tia-Maria")
          }
        }

        "sisältää ylimääräisiä välilyöntejä" -{
          "kutsumanimi päätellään tästä huolimatta oikein" in {
            putHenkilö(UusiHenkilö("160863-795N", "      Johanna       Tia-Maria       ", None, "Mykkänen"))(verifyResponseStatusOk())
            oppijaByHetu("160863-795N").henkilö.asInstanceOf[TäydellisetHenkilötiedot].kutsumanimi should equal("Johanna")
          }
        }
      }

      "Käytettäessä oppijan oidia" - {
        "Oid ok" - {
          "palautetaan HTTP 200"  in {
            mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.eero)
            putHenkilö(OidHenkilö(KoskiSpecificMockOppijat.eero.oid)) (verifyResponseStatusOk())
          }
        }

        "Oid virheellinen" - {
          "palautetaan HTTP 400"  in {
            putHenkilö(OidHenkilö("123.123.123")) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*henkilö.oid.*regularExpressionMismatch.*".r)))
          }
        }

        "Oppijaa ei löydy oidilla" - {
          "palautetaan HTTP 404"  in {
            putHenkilö(OidHenkilö("1.2.246.562.24.19999999999")) (verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydy("Oppijaa 1.2.246.562.24.19999999999 ei löydy.")))
          }
        }
      }

      "Käytettäessä oppijan kaikkia tietoja" - {
        "Oid ok" - {
          "palautetaan HTTP 200"  in {
            mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.eero)
            putHenkilö(KoskiSpecificMockOppijat.eero) (verifyResponseStatusOk())
          }
        }

        "Oid virheellinen" - {
          "palautetaan HTTP 400"  in {
            putHenkilö(TäydellisetHenkilötiedot("123.123.123", Some("010101-123N"), None, "Testi", "Testi", "Toivola", None, None)) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*henkilö.oid.*regularExpressionMismatch.*".r)))
          }
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
          val opiskeluoikeus = lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid)
          putOppija(Oppija(KoskiSpecificMockOppijat.eero, List(opiskeluoikeus))) {
            verifyResponseStatusOk()
          }
        }

        "Tuntematon oid" in {
          val eero = KoskiSpecificMockOppijat.eero
          val opiskeluoikeus = lastOpiskeluoikeus(eero.oid)
          clearOppijanOpiskeluoikeudet(eero.oid)
          putOppija(Oppija(eero, List(opiskeluoikeus.withOidAndVersion(oid = Some("1.2.246.562.15.15285175178").copy(), versionumero = None)))) {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta 1.2.246.562.15.15285175178 ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
          }
        }

        "Tuntematon oid ja tunnistetaan duplikaatiksi" in {
          val opiskeluoikeus = lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid)
          putOppija(Oppija(KoskiSpecificMockOppijat.eero, List(opiskeluoikeus.withOidAndVersion(oid = Some("1.2.246.562.15.15285175178"), versionumero = None)))) {
            verifyResponseStatus(409, KoskiErrorCategory.conflict.exists("Vastaava opiskeluoikeus on jo olemassa."))
          }
        }

        "Tyypin muutos" in {
          val opiskeluoikeus = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ysiluokkalainen.oid)
          putOppija(Oppija(KoskiSpecificMockOppijat.ysiluokkalainen, List(aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen.withOidAndVersion(opiskeluoikeus.oid, versionumero = None)))) {
            verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos(s"Opiskeluoikeuden tyyppiä ei voi vaihtaa. Vanha tyyppi ${opiskeluoikeus.tyyppi.koodiarvo}. Uusi tyyppi ${aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen.tyyppi.koodiarvo}."))
          }
        }
      }
    }

    "Jakso" - {
      "Jos lähetetään jakso jonka loppupäivämäärä on ennen alkupäivämäärää" - {
        "palautetaan HTTP 400" in {
          val oo = defaultOpiskeluoikeus.copy(lisätiedot = Some(opiskeluoikeudenLisätiedot.copy(
            majoitus = Some(List(Aikajakso(
              alku = date(2020, 5, 31),
              loppu = Some(date(2019, 5, 31))
            ))
          ))))
          setupOppijaWithOpiskeluoikeus(oo) {
            verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"Jakson alku 2020-05-31 on jakson lopun 2019-05-31 jälkeen","errorType":"jaksonLoppuEnnenAlkua".*""".r))
          }
        }
      }
    }

    "Oppilaitos" - {
      def oppilaitoksella(oid: String) = defaultOpiskeluoikeus.copy(oppilaitos = Some(Oppilaitos(oid)))

      "Kun opinto-oikeutta yritetään lisätä oppilaitokseen, johon käyttäjällä ei ole oikeuksia" - {
        "palautetaan HTTP 403 virhe"  in { setupOppijaWithOpiskeluoikeus(oppilaitoksella("1.2.246.562.10.93135224694"), headers = authHeaders(MockUsers.omniaPalvelukäyttäjä) ++ jsonContent) (
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.93135224694")))
        }
      }

      "Kun opinto-oikeutta yritetään lisätä koulutustoimijaan oppilaitoksen sijaan" - {
        "palautetaan HTTP 400 virhe"  in { setupOppijaWithOpiskeluoikeus(oppilaitoksella("1.2.246.562.10.346830761110")) (
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"Organisaatio 1.2.246.562.10.346830761110 ei ole oppilaitos vaan koulutustoimija","errorType":"vääränTyyppinenOrganisaatio".*""".r)))
        }
      }

      "Kun opinto-oikeutta yritetään lisätä oppilaitokseen, jota ei löydy organisaatiopalvelusta" - {
        "palautetaan HTTP 400 virhe"  in { setupOppijaWithOpiskeluoikeus(oppilaitoksella("1.2.246.562.10.146810761111")) (
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Organisaatiota 1.2.246.562.10.146810761111 ei löydy organisaatiopalvelusta","errorType":"organisaatioTuntematon"}.*""".r)))
        }
      }

      "Kun oppilaitoksen oid on virheellistä muotoa" - {
        "palautetaan HTTP 400 virhe"  in { setupOppijaWithOpiskeluoikeus(oppilaitoksella("asdf")) (
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*opiskeluoikeudet.0.oppilaitos.oid.*regularExpressionMismatch.*".r)))
        }
      }

      "Kun annetaan koulutustoimija, joka ei vastaa organisaatiopalvelun oppilaitokselle määrittämää koulutustoimijaa" - {
        "palautetaan HTTP 400 virhe"  in { setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(koulutustoimija = Some(Koulutustoimija("1.2.246.562.10.53814745062")))) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.vääräKoulutustoimija("Annettu koulutustoimija 1.2.246.562.10.53814745062 ei vastaa organisaatiopalvelusta löytyvää koulutustoimijaa 1.2.246.562.10.346830761110")))
        }
      }
    }

    "Suorituksen toimipiste" - {
      def toimipisteellä(oid: String) = defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(toimipiste = OidOrganisaatio(oid))))

      "Kun yritetään käyttää toimipistettä, johon käyttäjällä ei ole oikeuksia" - {
        "palautetaan HTTP 403 virhe"  in { setupOppijaWithOpiskeluoikeus(toimipisteellä(MockOrganisaatiot.helsinginKaupunki)) (
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + MockOrganisaatiot.helsinginKaupunki)))
        }
      }

      "Kun toimipiste ei ole oppilaitoksen aliorganisaatio" - {
        "Palautetaan HTTP 200" in {
          setupOppijaWithOpiskeluoikeus(toimipisteellä(MockOrganisaatiot.omnia)) (
            verifyResponseStatusOk())
        }
      }
    }

    "Opiskeluoikeuden tila ja päivämäärät" - {
      "Alkaminen ja päättyminen" - {
        "Päivämäärät kunnossa -> palautetaan HTTP 200" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(arvioituPäättymispäivä = Some(date(2018, 5, 31))))(verifyResponseStatusOk())
        }

        "alkamispäivä tänään -> palautetaan HTTP 200"  in (setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(alkamispäivä = LocalDate.now)) {
          verifyResponseStatusOk()
        })

        "alkamispäivä tulevaisuudessa -> palautetaan HTTP 200"  in (setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(alkamispäivä = date(2100, 5, 31))) {
          verifyResponseStatusOk()
        })

        "päättymispäivä tulevaisuudessa -> palautetaan HTTP 400"  in (setupOppijaWithOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2100, 5, 31))) {
          verifyResponseStatusOk()
        })

        "Päivämääräformaatti virheellinen -> palautetaan HTTP 400" in {
          def withAp(alkamispäivä: String) = {
            val apMap: JObject = JsonMethods.render(Map("alkamispäivä" -> alkamispäivä)).asInstanceOf[JObject];
            val suoritus = createOpiskeluoikeusWithMergedSuoritusJson(apMap).asInstanceOf[JObject];
            suoritus.merge(apMap)
          }

          mitätöiOppijanKaikkiOpiskeluoikeudet()
          putOpiskeluoikeusWithSomeMergedJson(Map[String, JValue]()) {
            verifyResponseStatusOk()
          }

          putOpiskeluoikeusWithSomeJson(Map("opiskeluoikeudet" -> List(withAp("2015-01.12")))) {
             verifyResponseStatusOk(400)
          }
        }

        "Väärä päivämääräjärjestys" - {
          "alkamispäivä > päättymispäivä"  in (setupOppijaWithOpiskeluoikeus(päättymispäivällä(makeOpiskeluoikeus(alkamispäivä = date(2020, 1, 1)), päättymispäivä = date(2019, 5, 31))) {
            verifyResponseStatus(400, List(
              exact(KoskiErrorCategory.badRequest.validation.date.päättymisPäiväEnnenAlkamispäivää, "alkamispäivä (2020-01-01) oltava sama tai aiempi kuin päättymispäivä (2019-05-31)"),
              exact(KoskiErrorCategory.badRequest.validation.date.opiskeluoikeusjaksojenPäivämäärät, "tila.opiskeluoikeusjaksot: 2020-01-01 on oltava aiempi kuin 2019-05-31"),
              exact(KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenAlkamispäivää, "suoritus.alkamispäivä (2020-01-01) oltava sama tai aiempi kuin suoritus.vahvistus.päivä (2019-05-31)"),
            ))
          })

          "alkamispäivä > arvioituPäättymispäivä"  in (setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(arvioituPäättymispäivä = Some(date(1999, 5, 31)))){
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.arvioituPäättymisPäiväEnnenAlkamispäivää("alkamispäivä (2000-01-01) oltava sama tai aiempi kuin arvioituPäättymispäivä (1999-05-31)"))
          })

          "Opiskeluoikeuden voi mitätöidä, vaikka muutosvalidaatiot epäonnistuisivat" in {
            val oo = setupOppijaWithAndGetOpiskeluoikeus(defaultOpiskeluoikeus)

            val validoitumatonOo = oo.copy(arvioituPäättymispäivä = Some(date(1999, 5, 31)))

            putOpiskeluoikeus(validoitumatonOo) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.arvioituPäättymisPäiväEnnenAlkamispäivää("alkamispäivä (2000-01-01) oltava sama tai aiempi kuin arvioituPäättymispäivä (1999-05-31)"))
            }

            authGet("api/opiskeluoikeus/" + oo.oid.get, defaultUser) {
              verifyResponseStatusOk()
            }

            putOpiskeluoikeus(validoitumatonOo
              .copy(
                tila = oo.tila.copy(opiskeluoikeusjaksot = validoitumatonOo.tila.opiskeluoikeusjaksot ++ List(
                  AmmatillinenOpiskeluoikeusjakso(
                    alku = LocalDate.now,
                    tila = ExampleData.opiskeluoikeusMitätöity
                  )
                ))
              )
            ){
              verifyResponseStatusOk()
            }

            authGet("api/opiskeluoikeus/" + oo.oid.get, defaultUser) {
              verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
            }
          }

          "suoritus.vahvistus.päivä > päättymispäivä" in {
            val oo = päättymispäivällä(defaultOpiskeluoikeus, date(2017, 5, 31))
            val tutkinto: AmmatillinenPäätasonSuoritus = oo.suoritukset.collect {
              case s: AmmatillisenTutkinnonSuoritus => s.copy(vahvistus = vahvistus(date(2017, 6, 30), stadinAmmattiopisto, Some(helsinki)))
            }.head

            setupOppijaWithOpiskeluoikeus(oo.copy(suoritukset = List(tutkinto))) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.päättymispäiväEnnenVahvistusta("suoritus.vahvistus.päivä (2017-06-30) oltava sama tai aiempi kuin päättymispäivä (2017-05-31)"))
            }
          }

          "suoritus.alkamispäivä < opiskeluoikeus.alkamispäivä" in {
            val oo = alkamispäivällä(defaultOpiskeluoikeus, date(2020, 4, 20))
            val tutkinto: AmmatillinenPäätasonSuoritus = oo.suoritukset.collect {
              case s: AmmatillisenTutkinnonSuoritus => s.copy(alkamispäivä = Some(date(2020, 4, 16)))
            }.head
            val ooSuorituksilla = oo.copy(suoritukset = List(tutkinto))

            setupOppijaWithOpiskeluoikeus(ooSuorituksilla) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.suorituksenAlkamispäiväEnnenOpiskeluoikeudenAlkamispäivää(
                "opiskeluoikeuden ensimmäisen tilan alkamispäivä (2020-04-20) oltava sama tai aiempi kuin päätason suorituksen alkamispäivä (2020-04-16)"
              ))
            }
          }
        }

        "Opiskeluoikeuden tila muuttunut vielä valmistumisen jälkeen -> HTTP 400" in (setupOppijaWithOpiskeluoikeus(
          lisääTila(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)), date(2016, 6, 30), ExampleData.opiskeluoikeusLäsnä)
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaMuuttunutLopullisenTilanJälkeen("Opiskeluoikeuden tila muuttunut lopullisen tilan (valmistunut) jälkeen"))
        })

        "Kaksi tilaa samalla alkupäivämäärällä" -  {
          "palautetaan 400 jos viimeinen tila ei ole 'mitatoity'" in {
            val opiskeluoikeus = lisääTiloja(makeOpiskeluoikeus(), List(
              (date(2018, 1, 1), Koodistokoodiviite("valiaikaisestikeskeytynyt", "koskiopiskeluoikeudentila")),
              (date (2018, 1, 1), Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila"))
            ))
            setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.opiskeluoikeusjaksojenPäivämäärät("tila.opiskeluoikeusjaksot: valiaikaisestikeskeytynyt 2018-01-01 ei voi olla samalla päivämäärällä kuin lasna 2018-01-01"))
            }
          }
          "mitatoity tila sallitaan viimeisenä" in {
            val mitätöitäväOpiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(makeOpiskeluoikeus())

            val opiskeluoikeus = lisääTiloja(mitätöitäväOpiskeluoikeus, List(
              (date(2018, 1, 1), Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila")),
              (date(2018, 1, 1), Koodistokoodiviite("mitatoity", "koskiopiskeluoikeudentila"))
            ))
            putOppija(makeOppija(defaultHenkilö, List(opiskeluoikeus))) {
              verifyResponseStatusOk()
            }
          }
        }

        "Päättävän tilan tyyppi" - {
          "kaksi päättävää tilaa kun viimeinen on mitatoity" in {
            val mitätöitäväOpiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(makeOpiskeluoikeus())

            val opiskeluoikeus = lisääTiloja(mitätöitäväOpiskeluoikeus, List(
              (date(2018, 1, 1), Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila")),
              (date(2018, 2, 2), Koodistokoodiviite("mitatoity", "koskiopiskeluoikeudentila"))
            ))
            putOppija(makeOppija(defaultHenkilö, List(opiskeluoikeus))) {
              verifyResponseStatusOk()
            }
          }
          "mitatoity tilan tulee olla viimeinen" in {
            val opiskeluoikeus = lisääTiloja(makeOpiskeluoikeus(), List(
              (date(2018, 1, 1), Koodistokoodiviite("mitatoity", "koskiopiskeluoikeudentila")),
              (date(2018, 1, 2), Koodistokoodiviite("eronnut", "koskiopiskeluoikeudentila"))
            ))
            setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.montaPäättävääTilaa("Opiskeluoikeudella voi olla vain yksi opiskeluoikeuden päättävä tila"))
            }
          }
          "Päättäviä tiloja voi olla vain yksi" in {
            val opiskeluoikeus = lisääTiloja(makeOpiskeluoikeus(), List(
              (date(2017, 1, 1), Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila")),
              (date(2018, 1, 1), Koodistokoodiviite("eronnut", "koskiopiskeluoikeudentila"))
            ))
            setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.montaPäättävääTilaa("Opiskeluoikeudella voi olla vain yksi opiskeluoikeuden päättävä tila"))
            }
          }
        }
      }
    }

    "Lokalisoidut tekstit" - {
      def withName(name: LocalizedString) = defaultOpiskeluoikeus.copy(tyyppi = defaultOpiskeluoikeus.tyyppi.copy(nimi = Some(name)))

      "suomi riittää" in {
        setupOppijaWithOpiskeluoikeus(withName(LocalizedString.finnish("Jotain"))) {
          verifyResponseStatusOk()
        }
      }
      "ruotsi riittää" in {
        setupOppijaWithOpiskeluoikeus(withName(LocalizedString.swedish("Något"))) {
          verifyResponseStatusOk()
        }
      }
      "englanti riittää" in {
        setupOppijaWithOpiskeluoikeus(withName(LocalizedString.english("Something"))) {
          verifyResponseStatusOk()
        }
      }

      "vähintään yksi kieli vaaditaan" in {
        putOpiskeluoikeusWithSomeMergedJson(Map("tyyppi" -> Map("nimi" -> Map.empty[String, String]))) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
        }
      }
    }

    "Kentät cleanForTesting ja ignoreKoskiValidator" - {
      "Kun kentät määritelty, local-ympäristöön voidaan ladata 'rikkinäinen' opiskeluoikeus" in {
        val json = JsonMethods.parse(Source.fromFile("src/test/resources/rikkinäinen_opiskeluoikeus.json").mkString)
        val oid = putOppija(json, headers = authHeaders() ++ jsonContent) {
          verifyResponseStatusOk()
          implicit val formats = DefaultFormats
          (JsonMethods.parse(body) \ "henkilö" \ "oid").extract[String]
        }
        val oo = lastOpiskeluoikeus(oid)
        oo.suoritukset.length should equal (1)
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
