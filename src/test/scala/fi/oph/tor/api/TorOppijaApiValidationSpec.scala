package fi.oph.tor.api

import java.time.LocalDate

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json.toJValue
import fi.oph.tor.schema.TorOppija
import org.scalatest.FunSpec

class TorOppijaApiValidationSpec extends FunSpec with OpiskeluOikeusTestMethods {
  describe("Opiskeluoikeuden lisääminen") {
    describe("Valideilla tiedoilla") {
      it("palautetaan HTTP 200") {
        putOpiskeluOikeusAjax(Map()) {
          verifyResponseCode(200)
        }
      }
    }

    describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, johon käyttäjällä ei ole pääsyä") {
      it("palautetaan HTTP 403 virhe" ) { putOpiskeluOikeusAjax(Map(
          "oppilaitos" -> Map("oid" -> "1.2.246.562.10.346830761110"))
        )(verifyResponseCode(403, "Ei oikeuksia organisatioon 1.2.246.562.10.346830761110"))
      }}

    describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, jota ei löydy organisaatiopalvelusta") {
      it("palautetaan HTTP 400 virhe" ) (putOpiskeluOikeusAjax(Map("oppilaitos" -> Map("oid" -> "tuuba")))
        (verifyResponseCode(400, "Organisaatiota tuuba ei löydy organisaatiopalvelusta")))
    }

    describe("Nimenä tyhjä merkkijono") {
      it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(Map(
        "henkilö" -> Map("sukunimi" -> "")
      )) (verifyResponseCode(400)))
    }

    describe("Epäkelpo JSON-dokumentti") {
      it("palautetaan HTTP 400 virhe" ) (sendAjax("api/oppija", "application/json", "not json", "put")
        (verifyResponseCode(400, "Invalid JSON")))
    }

    describe("Kun yritetään lisätä opinto-oikeus virheelliseen perusteeseen") {
      it("palautetaan HTTP 400 virhe" ) {
        putOpiskeluOikeusAjax(Map(
          "suoritus" -> Map("koulutusmoduulitoteutus" -> Map("koulutusmoduuli" -> Map("perusteenDiaarinumero" -> "39/xxx/2014")))
        )) (verifyResponseCode(400, "Tutkinnon peruste on virheellinen: 39/xxx/2014"))
      }
    }

    describe("Kun yritetään lisätä opinto-oikeus ilman perustetta") {
      it("palautetaan HTTP 400 virhe" ) {
        putOpiskeluOikeusAjax(Map(
          "suoritus" -> Map("koulutusmoduulitoteutus" -> Map("koulutusmoduuli" -> Map("perusteenDiaarinumero"-> "")))
        )) (verifyResponseCode(400, "perusteenDiaarinumero"))
      }
    }

    describe("Hetun ollessa") {
      describe("muodoltaan virheellinen") {
        it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "010101-123123")))
          (verifyResponseCode(400, "Virheellinen muoto hetulla: 010101-123123")))
      }
      describe("muodoltaan oikea, mutta väärä tarkistusmerkki") {
        it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "010101-123P")))
          (verifyResponseCode(400, "Virheellinen tarkistusmerkki hetussa: 010101-123P")))
      }
      describe("päivämäärältään tulevaisuudessa") {
        it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "141299A903C")))
          (verifyResponseCode(400, "Syntymäpäivä hetussa: 141299A903C on tulevaisuudessa")))
      }
      describe("päivämäärältään virheellinen") {
        it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "300215-123T")))
          (verifyResponseCode(400, "Virheellinen syntymäpäivä hetulla: 300215-123T")))
      }
      describe("validi") {
        it("palautetaan HTTP 200" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "010101-123N")))
          (verifyResponseCode(200)))
      }
    }

    describe("Opiskeluoikeuden päivämäärät") {
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200" ) (putOpiskeluOikeusAjax(Map(
          "alkamispäivä" -> "2015-08-01",
          "päättymispäivä" -> "2016-05-31",
          "arvioituPäättymispäivä" -> "2018-05-31"
        ))(verifyResponseCode(200)))
      }
      describe("Päivämääräformaatti virheellinen") {
        it("palautetaan HTTP 400" ) (putOpiskeluOikeusAjax(Map(
          "alkamispäivä" -> "2015.01-12"
        ))(verifyResponseCode(400, "Virheellinen päivämäärä: 2015.01-12")))
      }
      describe("Päivämäärä virheellinen") {
        it("palautetaan HTTP 400" ) (putOpiskeluOikeusAjax(Map(
          "alkamispäivä" -> "2015-01-32"
        ))(verifyResponseCode(400, "Virheellinen päivämäärä: 2015-01-32")))
      }
      describe("Väärä päivämääräjärjestys") {
        it("alkamispäivä > päättymispäivä" ) (putOpiskeluOikeusAjax(Map(
          "alkamispäivä" -> "2015-08-01",
          "päättymispäivä" -> "2014-05-31"
        ))(verifyResponseCode(400, "alkamispäivä (2015-08-01) oltava sama tai aiempi kuin päättymispäivä(2014-05-31)")))

        it("alkamispäivä > arvioituPäättymispäivä" ) (putOpiskeluOikeusAjax(Map(
          "alkamispäivä" -> "2015-08-01",
          "arvioituPäättymispäivä" -> "2014-05-31"
        ))(verifyResponseCode(400, "alkamispäivä (2015-08-01) oltava sama tai aiempi kuin arvioituPäättymispäivä(2014-05-31)")))
      }
    }

    describe("Suorituksen päivämäärät") {
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200" ) (putOpiskeluOikeusAjax(Map("suoritus" -> Map(
          "alkamispäivä" -> "2015-08-01",
          "arviointi" -> List(Map(
            "päivä" -> "2016-05-31",
            "arvosana" -> Map("koodiarvo" -> "2", "koodistoUri" -> "arviointiasteikkoammatillinent1k3"))),
            "vahvistus" -> Map("päivä" -> "2016-05-31")
          )))(verifyResponseCode(200)))
      }

      describe("alkamispäivä > arviointi.päivä") {
        it("palautetaan HTTP 200" ) (putOpiskeluOikeusAjax(Map("suoritus" -> Map(
          "alkamispäivä" -> "2017-08-01",
          "arviointi" -> List(Map(
            "päivä" -> "2016-05-31",
            "arvosana" -> Map("koodiarvo" -> "2", "koodistoUri" -> "arviointiasteikkoammatillinent1k3"))
          )
        )))(verifyResponseCode(400, "suoritus.alkamispäivä (2017-08-01) oltava sama tai aiempi kuin suoritus.arviointi.päivä(2016-05-31)")))
      }

      describe("arviointi.päivä > vahvistus.päivä") {
        it("palautetaan HTTP 200" ) (putOpiskeluOikeusAjax(Map("suoritus" -> Map(
          "arviointi" -> List(Map(
            "päivä" -> "2016-05-31",
            "arvosana" -> Map("koodiarvo" -> "2", "koodistoUri" -> "arviointiasteikkoammatillinent1k3"))
          ),
          "vahvistus" -> Map("päivä" -> "2016-05-30")
        )))(verifyResponseCode(400, "suoritus.arviointi.päivä (2016-05-31) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2016-05-30)")))
      }
    }

    describe("opiskeluoikeusjaksot"){
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200") (putOpiskeluOikeusAjax(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "opiskeluoikeudentila")),
          Map( "alku" -> "2016-06-01", "tila" -> Map("koodiarvo" -> "paattynyt", "koodistoUri" -> "opiskeluoikeudentila"))
        )))) (verifyResponseCode(200)))
      }
      describe("alku > loppu") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2016-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila"))
        )))) (verifyResponseCode(400, "opiskeluoikeudenTila.opiskeluoikeusjaksot.alku (2016-08-01) oltava sama tai aiempi kuin opiskeluoikeudenTila.opiskeluoikeusjaksot.loppu(2015-12-31)")))
      }
      describe("ei-viimeiseltä jaksolta puuttuu loppupäivä") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map("alku" -> "2015-08-01", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila")),
          Map("alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "opiskeluoikeudentila"))
        )))) (verifyResponseCode(400, "opiskeluoikeudenTila.opiskeluoikeusjaksot: ei-viimeiseltä jaksolta puuttuu loppupäivä"))) }
      describe("jaksot ovat päällekkäiset") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2016-01-01", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "opiskeluoikeudentila"))
        ))))(verifyResponseCode(400, "opiskeluoikeudenTila.opiskeluoikeusjaksot: jaksot eivät muodosta jatkumoa")))
      }
      describe("jaksojen väliin jää tyhjää") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-10-01", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "opiskeluoikeudentila"))
        ))))(verifyResponseCode(400, "opiskeluoikeudenTila.opiskeluoikeusjaksot: jaksot eivät muodosta jatkumoa")))
      }
    }

    describe("Läsnäolojaksot") {
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200") (putOpiskeluOikeusAjax(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
            Map( "alku" -> "2015-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
            Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila")),
            Map( "alku" -> "2016-06-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseCode(200)))
      }
      describe("alku > loppu") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2016-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseCode(400, "läsnäolotiedot.läsnäolojaksot.alku (2016-08-01) oltava sama tai aiempi kuin läsnäolotiedot.läsnäolojaksot.loppu(2015-12-31)")))
      }
      describe("ei-viimeiseltä jaksolta puuttuu loppupäivä") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseCode(400, "läsnäolotiedot.läsnäolojaksot: ei-viimeiseltä jaksolta puuttuu loppupäivä")))
      }
      describe("jaksot ovat päällekkäiset") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2016-01-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseCode(400, "läsnäolotiedot.läsnäolojaksot: jaksot eivät muodosta jatkumoa")))
      }
      describe("jaksojen väliin jää tyhjää") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-10-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseCode(400, "läsnäolotiedot.läsnäolojaksot: jaksot eivät muodosta jatkumoa")))
      }
    }


    describe("Tutkinnon tietojen muuttaminen") {
      describe("Osaamisala ja suoritustapa ok") {
        it("palautetaan HTTP 200") (putOpiskeluOikeusAjax(Map("suoritus" -> Map("koulutusmoduulitoteutus" -> Map(
          "suoritustapa" -> Map("tunniste" -> Map("koodiarvo" -> "ops", "koodistoUri" -> "suoritustapa")),
          "osaamisala" -> List(Map("koodiarvo" -> "1527", "koodistoUri" -> "osaamisala"))
        ))))(verifyResponseCode(200)))
      }
      describe("Suoritustapa virheellinen") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("suoritus" -> Map("koulutusmoduulitoteutus" -> Map(
          "suoritustapa" -> Map("tunniste" -> Map("koodiarvo" -> "blahblahtest", "koodistoUri" -> "suoritustapa")),
          "osaamisala" -> List(Map("koodiarvo" -> "1527", "koodistoUri" -> "osaamisala"))
        ))))(verifyResponseCode(400, "Koodia suoritustapa/blahblahtest ei löydy koodistosta")))
      }
      describe("Osaamisala ei löydy tutkintorakenteesta") {
        it("palautetaan HTTP 400") (putOpiskeluOikeusAjax(Map("suoritus" -> Map("koulutusmoduulitoteutus" -> Map(
          "suoritustapa" -> Map("tunniste" -> Map("koodiarvo" -> "ops", "koodistoUri" -> "suoritustapa")),
          "osaamisala" -> List(Map("koodiarvo" -> "3053", "koodistoUri" -> "osaamisala"))
        )))) (verifyResponseCode(400, "Osaamisala 3053 ei löydy tutkintorakenteesta perusteelle 39/011/2014")))
      }
      describe("Osaamisala virheellinen") {
        it("palautetaan HTTP 400")(putOpiskeluOikeusAjax(Map("suoritus" -> Map("koulutusmoduulitoteutus" -> Map(
          "suoritustapa" -> Map("tunniste" -> Map("koodiarvo" -> "ops", "koodistoUri" -> "suoritustapa")),
          "osaamisala" -> List(Map("koodiarvo" -> "0", "koodistoUri" -> "osaamisala"))
        ))))(verifyResponseCode(400, "Koodia osaamisala/0 ei löydy koodistosta")))
      }
    }

    describe("Arvioinnin antaminen tutkinnon osalle") {
      describe("Tutkinnon osa ja arviointi ok") {
        it("palautetaan HTTP 200") (putTutkinnonOsaSuoritusAjax(Map()) (verifyResponseCode(200)))
      }

      describe("Tutkinnon osa ei kuulu tutkintorakenteeseen") {
        it("palautetaan HTTP 400") (putTutkinnonOsaSuoritusAjax(Map("koulutusmoduulitoteutus" -> Map("koulutusmoduuli" -> Map(
          "tunniste" -> Map("koodiarvo" -> "103135", "nimi" -> "Kaapelitelevisio- ja antennijärjestelmät", "koodistoUri" -> "tutkinnonosat", "koodistoVersio" -> 1)
        ))))(verifyResponseCode(400, "Tutkinnon osa tutkinnonosat/103135 ei löydy tutkintorakenteesta perusteelle 39/011/2014 - suoritustapa naytto"))) }

      describe("Tutkinnon osaa ei ei löydy koodistosta") {
        it("palautetaan HTTP 400") (putTutkinnonOsaSuoritusAjax(Map("koulutusmoduulitoteutus" -> Map("koulutusmoduuli" -> Map(
            "tunniste" -> Map("koodiarvo" -> "9923123", "nimi" -> "Väärää tietoa", "koodistoUri" -> "tutkinnonosat", "koodistoVersio" -> 1)
        )))) (verifyResponseCode(400, "Koodia tutkinnonosat/9923123 ei löydy koodistosta"))) }

      describe("Arviointiasteikko on tuntematon") {
        it("palautetaan HTTP 400") (putTutkinnonOsaSuoritusAjax(Map(
          "arviointi" -> List(Map("arvosana" -> Map("koodiarvo" -> "2", "koodistoUri" -> "vääräasteikko")))
        ))(verifyResponseCode(400, "not found in enum")))
      }
      describe("Arvosana ei kuulu perusteiden mukaiseen arviointiasteikkoon") {
        it("palautetaan HTTP 400") (putTutkinnonOsaSuoritusAjax(Map(
          "arviointi" -> List(Map("arvosana" -> Map("koodiarvo" -> "x", "koodistoUri" -> "arviointiasteikkoammatillinent1k3")))
        ))(verifyResponseCode(400, "Koodia arviointiasteikkoammatillinent1k3/x ei löydy koodistosta")))
      }
    }

    describe("Kyselyrajapinta") {
      describe("Kun haku osuu") {
        it("palautetaan hakutulokset") {
          putOpiskeluOikeusAjax(Map("päättymispäivä"-> "2016-01-09")) {
            putOpiskeluOikeusAjax(Map("päättymispäivä"-> "2013-01-09"), toJValue(Map(
              "etunimet"->"Teija",
              "sukunimi"->"Tekijä",
              "kutsumanimi"->"Teija",
              "hetu"->"150995-914X"
            ))) {
              authGet ("api/oppija?opiskeluoikeusPäättynytViimeistään=2016-12-31&opiskeluoikeusPäättynytAikaisintaan=2016-01-01") {
                verifyResponseCode(200)
                val oppijat: List[TorOppija] = Json.read[List[TorOppija]](response.body)
                oppijat.length should equal(1)
                oppijat(0).opiskeluoikeudet(0).päättymispäivä should equal(Some(LocalDate.parse("2016-01-09")))
              }
            }
          }
        }
      }

      describe("Kun haku ei osu") {
        it("palautetaan tyhjä lista") {
          putOpiskeluOikeusAjax(Map("päättymispäivä"-> "2016-01-09")) {
            authGet ("api/oppija?opiskeluoikeusPäättynytViimeistään=2014-12-31&opiskeluoikeusPäättynytAikaisintaan=2014-01-01") {
              verifyResponseCode(200)
              val oppijat: List[TorOppija] = Json.read[List[TorOppija]](response.body)
              oppijat.length should equal(0)
            }
          }
        }
      }

      describe("Kun haetaan ei tuetulla parametrilla") {
        it("palautetaan HTTP 400") {
          authGet("api/oppija?eiTuettu=kyllä") {
            verifyResponseCode(400, "Unsupported query parameter: eiTuettu")
          }
        }
      }

      describe("Kun haetaan ilman parametreja") {
        it("palautetaan kaikki oppijat") {
          putOpiskeluOikeusAjax(Map("päättymispäivä"-> "2016-01-09")) {
            putOpiskeluOikeusAjax(Map("päättymispäivä"-> "2013-01-09"), toJValue(Map(
              "etunimet"->"Teija",
              "sukunimi"->"Tekijä",
              "kutsumanimi"->"Teija",
              "hetu"->"150995-914X"
            ))) {
              authGet ("api/oppija") {
                verifyResponseCode(200)
                val oppijat: List[TorOppija] = Json.read[List[TorOppija]](response.body)
                oppijat.length should be >= 2
              }
            }
          }
        }
      }
    }
  }
}
