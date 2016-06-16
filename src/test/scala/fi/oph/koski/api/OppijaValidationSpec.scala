package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._
import fi.oph.koski.koskiuser.MockUsers
import org.json4s._
import org.scalatest.FunSpec

class OppijaValidationSpec extends FunSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  describe("Opiskeluoikeuden lisääminen") {
    describe("Valideilla tiedoilla") {
      it("palautetaan HTTP 200") {
        putOpiskeluOikeus(opiskeluoikeus()) {
          verifyResponseStatus(200)
        }
      }
    }

    describe("Ilman tunnistautumista") {
      it("palautetaan HTTP 401") {
        putOpiskeluOikeus(opiskeluoikeus(), headers = jsonContent) {
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized("Käyttäjä ei ole tunnistautunut."))
        }
      }
    }

    describe("Epäkelpo JSON-dokumentti") {
      it("palautetaan HTTP 400 virhe" ) (request("api/oppija", "application/json", "not json", "put")
        (verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.json("Invalid JSON"))))
    }

    describe("Väärä Content-Type") {
      it("palautetaan HTTP 415") {

        put("api/oppija", body = Json.write(makeOppija(defaultHenkilö, List(opiskeluoikeus()))), headers = authHeaders() ++ Map(("Content-type" -> "text/plain"))) {
          verifyResponseStatus(415, KoskiErrorCategory.unsupportedMediaType.jsonOnly("Wrong content type: only application/json content type with UTF-8 encoding allowed"))
        }
      }
    }

    describe("Henkilötiedot") {
      describe("Nimenä tyhjä merkkijono") {
        it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(sukunimi = "")) (verifyResponseStatus(400)))
      }


      describe("Hetun ollessa") {
        describe("muodoltaan virheellinen") {
          it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(hetu = "010101-123123"))
            (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen muoto hetulla: 010101-123123"))))
        }
        describe("muodoltaan oikea, mutta väärä tarkistusmerkki") {
          it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(hetu = "010101-123P"))
            (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen tarkistusmerkki hetussa: 010101-123P"))))
        }
        describe("päivämäärältään tulevaisuudessa") {
          it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(hetu = "141299A903C"))
            (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Syntymäpäivä hetussa: 141299A903C on tulevaisuudessa"))))
        }
        describe("päivämäärältään virheellinen") {
          it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(hetu = "300215-123T"))
            (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen syntymäpäivä hetulla: 300215-123T"))))
        }
        describe("validi") {
          it("palautetaan HTTP 200" ) (putHenkilö(defaultHenkilö.copy(hetu = "010101-123N"))
            (verifyResponseStatus(200)))
        }
      }

      describe("Käytettäessä oppijan oidia") {
        describe("Oid ok") {
          it("palautetaan HTTP 200" ) (putHenkilö(OidHenkilö(MockOppijat.eero.oid)) (verifyResponseStatus(200)))
        }

        describe("Oid virheellinen") {
          it("palautetaan HTTP 400" ) (putHenkilö(OidHenkilö("123.123.123")) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*ECMA 262 regex.*".r))))
        }

        describe("Oppijaa ei löydy oidilla") {
          it("palautetaan HTTP 404" ) (putHenkilö(OidHenkilö("1.2.246.562.24.19999999999")) (verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydy("Oppijaa 1.2.246.562.24.19999999999 ei löydy."))))
        }
      }

      describe("Käytettäessä oppijan kaikkia tietoja") {
        describe("Oid ok") {
          it("palautetaan HTTP 200" ) (putHenkilö(MockOppijat.eero) (verifyResponseStatus(200)))
        }

        describe("Oid virheellinen") {
          it("palautetaan HTTP 400" ) (putHenkilö(TaydellisetHenkilötiedot("123.123.123", "010101-123N", "Testi", "Testi", "Toivola", None, None)) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*ECMA 262 regex.*".r))))
        }
      }
    }

    describe("Opiskeluoikeudet") {
      describe("Jos lähetetään 0 opiskeluoikeutta") {
        it("palautetaan HTTP 400") {
          putOppija(Oppija(defaultHenkilö, List())) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tyhjäOpiskeluoikeusLista("Annettiin tyhjä lista opiskeluoikeuksia."))
          }
        }
      }

      describe("Päivitettäessä opiskeluoikeus käyttäen sen id:tä") {
        it("Id ok") {
          val opiskeluoikeus = lastOpiskeluOikeus(MockOppijat.eero.oid)
          putOppija(Oppija(MockOppijat.eero, List(opiskeluoikeus))) {
            verifyResponseStatus(200)
          }
        }

        it("Tuntematon id") {
          val opiskeluoikeus = lastOpiskeluOikeus(MockOppijat.eero.oid)
          putOppija(Oppija(MockOppijat.eero, List(opiskeluoikeus.withIdAndVersion(id = Some(0), versionumero = None)))) {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta 0 ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
          }
        }
      }
    }

    describe("Oppilaitos") {
      def oppilaitoksella(oid: String) = opiskeluoikeus().copy(oppilaitos = Oppilaitos(oid))

      describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, johon käyttäjällä ei ole pääsyä") {
        it("palautetaan HTTP 403 virhe" ) { putOpiskeluOikeus(oppilaitoksella("1.2.246.562.10.93135224694"), headers = authHeaders(MockUsers.hiiri) ++ jsonContent) (
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.93135224694")))
        }
      }

      describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, joka ei ole oppilaitos") {
        it("palautetaan HTTP 400 virhe" ) { putOpiskeluOikeus(oppilaitoksella("1.2.246.562.10.346830761110")) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.vääränTyyppinen("Organisaatio 1.2.246.562.10.346830761110 ei ole Oppilaitos")))
        }
      }

      describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, jota ei löydy organisaatiopalvelusta") {
        it("palautetaan HTTP 400 virhe" ) { putOpiskeluOikeus(oppilaitoksella("1.2.246.562.10.146810761111")) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.tuntematon("Organisaatiota 1.2.246.562.10.146810761111 ei löydy organisaatiopalvelusta")))
        }
      }

      describe("Kun oppilaitoksen oid on virheellistä muotoa") {
        it("palautetaan HTTP 400 virhe" ) { putOpiskeluOikeus(oppilaitoksella("asdf")) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*ECMA 262 regex.*".r)))
        }
      }
    }

    describe("Opiskeluoikeuden päivämäärät") {
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200" ) (putOpiskeluOikeusMerged(Map(
          "alkamispäivä" -> "2015-08-01",
          "päättymispäivä" -> "2016-06-30",
          "arvioituPäättymispäivä" -> "2018-05-31"
        ))(verifyResponseStatus(200)))
      }
      describe("Päivämääräformaatti virheellinen") {
        it("palautetaan HTTP 400" ) (putOpiskeluOikeusMerged(Map(
          "alkamispäivä" -> "2015.01-12"
        ))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: 2015.01-12"))))
      }
      describe("Päivämäärä virheellinen") {
        it("palautetaan HTTP 400" ) (putOpiskeluOikeusMerged(Map(
          "alkamispäivä" -> "2015-01-32"
        ))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: 2015-01-32"))))
      }
      describe("Väärä päivämääräjärjestys") {
        it("alkamispäivä > päättymispäivä" ) (putOpiskeluOikeusMerged(Map(
          "alkamispäivä" -> "2015-08-01",
          "päättymispäivä" -> "2014-05-31"
        ))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua("alkamispäivä (2015-08-01) oltava sama tai aiempi kuin päättymispäivä(2014-05-31)"))))

        it("alkamispäivä > arvioituPäättymispäivä" ) (putOpiskeluOikeusMerged(Map(
          "alkamispäivä" -> "2015-08-01",
          "arvioituPäättymispäivä" -> "2014-05-31"
        ))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua("alkamispäivä (2015-08-01) oltava sama tai aiempi kuin arvioituPäättymispäivä(2014-05-31)"))))
      }
    }

    describe("Opiskeluoikeusjaksot"){
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200") (putOpiskeluOikeusMerged(Map("tila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "ammatillinenopiskeluoikeudentila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-06-01", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "ammatillinenopiskeluoikeudentila")),
          Map( "alku" -> "2016-06-02", "tila" -> Map("koodiarvo" -> "paattynyt", "koodistoUri" -> "ammatillinenopiskeluoikeudentila"))
        )))) (verifyResponseStatus(200)))
      }
      describe("alku > loppu") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusMerged(Map("tila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2016-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "ammatillinenopiskeluoikeudentila"))
        )))) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua(
          "tila.opiskeluoikeusjaksot.alku (2016-08-01) oltava sama tai aiempi kuin tila.opiskeluoikeusjaksot.loppu(2015-12-31)"))))
      }
      describe("ei-viimeiseltä jaksolta puuttuu loppupäivä") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusMerged(Map("tila" -> Map("opiskeluoikeusjaksot" -> List(
          Map("alku" -> "2015-08-01", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "ammatillinenopiskeluoikeudentila")),
          Map("alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "ammatillinenopiskeluoikeudentila"))
        )))) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.jaksonLoppupäiväPuuttuu("tila.opiskeluoikeusjaksot: ei-viimeiseltä jaksolta puuttuu loppupäivä")))) }
      describe("jaksot ovat päällekkäiset") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusMerged(Map("tila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2016-01-01", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "ammatillinenopiskeluoikeudentila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "ammatillinenopiskeluoikeudentila"))
        ))))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.jaksotEivätMuodostaJatkumoa("tila.opiskeluoikeusjaksot: jaksot eivät muodosta jatkumoa"))))
      }
      describe("jaksojen väliin jää tyhjää") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusMerged(Map("tila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-10-01", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "ammatillinenopiskeluoikeudentila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "ammatillinenopiskeluoikeudentila"))
        ))))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.jaksotEivätMuodostaJatkumoa("tila.opiskeluoikeusjaksot: jaksot eivät muodosta jatkumoa"))))
      }
    }

    describe("Läsnäolojaksot") {
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200") (putOpiskeluOikeusMerged(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-20", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-05-21", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(200)))
      }
      describe("alku > loppu") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusMerged(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2016-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua("läsnäolotiedot.läsnäolojaksot.alku (2016-08-01) oltava sama tai aiempi kuin läsnäolotiedot.läsnäolojaksot.loppu(2015-12-31)"))))
      }
      describe("ei-viimeiseltä jaksolta puuttuu loppupäivä") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusMerged(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.jaksonLoppupäiväPuuttuu("läsnäolotiedot.läsnäolojaksot: ei-viimeiseltä jaksolta puuttuu loppupäivä"))))
      }
      describe("jaksot ovat päällekkäiset") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusMerged(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2016-01-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.jaksotEivätMuodostaJatkumoa("läsnäolotiedot.läsnäolojaksot: jaksot eivät muodosta jatkumoa"))))
      }
      describe("jaksojen väliin jää tyhjää") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusMerged(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-10-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.jaksotEivätMuodostaJatkumoa("läsnäolotiedot.läsnäolojaksot: jaksot eivät muodosta jatkumoa"))))
      }
    }

    describe("Lokalisoidut tekstit") {
      def withName(name: LocalizedString) = defaultOpiskeluoikeus.copy(tyyppi = defaultOpiskeluoikeus.tyyppi.copy(nimi = Some(name)))

      it("suomi riittää") {
        putOpiskeluOikeus(withName(LocalizedString.finnish("Jotain"))) {
          verifyResponseStatus(200)
        }
      }
      it("ruotsi riittää") {
        putOpiskeluOikeus(withName(LocalizedString.swedish("Något"))) {
          verifyResponseStatus(200)
        }
      }
      it("englanti riittää") {
        putOpiskeluOikeus(withName(LocalizedString.english("Something"))) {
          verifyResponseStatus(200)
        }
      }

      it("vähintään yksi kieli vaaditaan") {
        putOpiskeluOikeusMerged(Map("tyyppi" -> Map("nimi" -> Map()))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*object has missing required properties.*".r))
        }
      }
    }
  }

  def putOpiskeluOikeusMerged[A](opiskeluOikeus: JValue, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(Json.toJValue(defaultOpiskeluoikeus).merge(opiskeluOikeus))), headers)(f)
  }

}
