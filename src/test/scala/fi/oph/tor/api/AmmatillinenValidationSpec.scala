package fi.oph.tor.api

import java.time.LocalDate

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.schema._
import org.scalatest.FunSpec

class AmmatillinenValidationSpec extends FunSpec with OpiskeluOikeusTestMethods {
  describe("Ammatillisen koulutuksen opiskeluoikeuden lisääminen") {
    describe("Valideilla tiedoilla") {
      it("palautetaan HTTP 200") {
        putOpiskeluOikeus(opiskeluoikeus()) {
          verifyResponseStatus(200)
        }
      }
    }

    describe("Kun tutkintosuoritus puuttuu") {
      it("palautetaan HTTP 400 virhe" ) {
        putOpiskeluOikeus(opiskeluoikeus().copy(suoritukset = Nil)) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*array is too short.*".r)))
      }
    }

    describe("Tutkinnon perusteet ja rakenne") {
      describe("Kun yritetään lisätä opinto-oikeus tuntemattomaan tutkinnon perusteeseen") {
        it("palautetaan HTTP 400 virhe" ) {
          val suoritus: AmmatillisenTutkinnonSuoritus = tutkintoSuoritus.copy(koulutusmoduuli = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", "koulutus"), Some("39/xxx/2014")))
          putTutkintoSuoritus(suoritus) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla 39/xxx/2014")))
        }
      }

      describe("Kun yritetään lisätä opinto-oikeus ilman tutkinnon perusteen diaarinumeroa") {
        it("palautetaan HTTP 200" ) {
          val suoritus: AmmatillisenTutkinnonSuoritus = tutkintoSuoritus.copy(koulutusmoduuli = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", "koulutus"), None))
          putTutkintoSuoritus(suoritus) (verifyResponseStatus(200))
        }
      }

      describe("Kun yritetään lisätä opinto-oikeus tyhjällä diaarinumerolla") {
        it("palautetaan HTTP 400 virhe" ) {
          val suoritus = tutkintoSuoritus.copy(koulutusmoduuli = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", "koulutus"), Some("")))

          putTutkintoSuoritus(suoritus) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*perusteenDiaarinumero.*".r)))
        }
      }

      describe("Osaamisala ja suoritustapa") {
        describe("Osaamisala ja suoritustapa ok") {
          val suoritus = tutkintoSuoritus.copy(
            suoritustapa = Some(Suoritustapa(Koodistokoodiviite("ops", "suoritustapa"))),
            osaamisala = Some(List(Koodistokoodiviite("1527", "osaamisala"))))

          it("palautetaan HTTP 200") (putTutkintoSuoritus(suoritus)(verifyResponseStatus(200)))
        }
        describe("Suoritustapa virheellinen") {
          val suoritus = tutkintoSuoritus.copy(
            suoritustapa = Some(Suoritustapa(Koodistokoodiviite("blahblahtest", "suoritustapa"))),
            osaamisala = Some(List(Koodistokoodiviite("1527", "osaamisala"))))

          it("palautetaan HTTP 400") (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia suoritustapa/blahblahtest ei löydy koodistosta"))))
        }
        describe("Osaamisala ei löydy tutkintorakenteesta") {
          val suoritus = tutkintoSuoritus.copy(
            suoritustapa = Some(Suoritustapa(Koodistokoodiviite("ops", "suoritustapa"))),
            osaamisala = Some(List(Koodistokoodiviite("3053", "osaamisala"))))

          it("palautetaan HTTP 400") (putTutkintoSuoritus(suoritus) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala 3053 ei löydy tutkintorakenteesta perusteelle 39/011/2014"))))
        }
        describe("Osaamisala virheellinen") {
          val suoritus = tutkintoSuoritus.copy(
            suoritustapa = Some(Suoritustapa(Koodistokoodiviite("ops", "suoritustapa"))),
            osaamisala = Some(List(Koodistokoodiviite("0", "osaamisala"))))

          it("palautetaan HTTP 400")(putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia osaamisala/0 ei löydy koodistosta"))))
        }
      }

      describe("Tutkinnon osat ja arvionnit") {
        val johtaminenJaHenkilöstönKehittäminen: OpsTutkinnonosa = OpsTutkinnonosa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None, None, None)

        describe("OPS-perusteinen tutkinnonosa") {
          describe("Tutkinnon osa ja arviointi ok") {
            it("palautetaan HTTP 200") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, tutkinnonSuoritustapaNäyttönä) (verifyResponseStatus(200)))
          }

          describe("Tutkinnon osa ei kuulu tutkintorakenteeseen") {
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa tutkinnonosat/104052 ei löydy tutkintorakenteesta perusteelle 39/011/2014 - suoritustapa naytto"))))
          }

          describe("Tutkinnon osaa ei ei löydy koodistosta") {
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
              koulutusmoduuli = OpsTutkinnonosa(Koodistokoodiviite("9923123", "tutkinnonosat"), true, None, None, None)), tutkinnonSuoritustapaNäyttönä)
              (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia tutkinnonosat/9923123 ei löydy koodistosta"))))
          }
        }

        describe("Paikallinen tutkinnonosa") {
          describe("Tutkinnon osa ja arviointi ok") {
            val suoritus = paikallinenTutkinnonOsaSuoritus
            it("palautetaan HTTP 200") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (verifyResponseStatus(200)))
          }

          describe("Laajuus negatiivinen") {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(koulutusmoduuli = paikallinenTutkinnonOsa.copy(laajuus = Some(laajuus.copy(arvo = -1))))
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (
              verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*numeric instance is lower than the required minimum.*".r)))
            )
          }
        }

        describe("Tuntematon tutkinnonosa") {
          it("palautetaan HTTP 400 virhe" ) {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(tyyppi = Koodistokoodiviite(koodiarvo = "tuntematon", koodistoUri = "suorituksentyyppi"))
            putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (
              verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*instance value ..tuntematon.. not found in enum.*".r))
            )
          }
        }

        describe("Tutkinnon osa toisesta tutkinnosta") {
          val autoalanTyönjohdonErikoisammattitutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", "koulutus"), Some("40/011/2001"))

          def osanSuoritusToisestaTutkinnosta(tutkinto: AmmatillinenTutkintoKoulutus, tutkinnonOsa: OpsTutkinnonosa): AmmatillisenTutkinnonosanSuoritus = tutkinnonOsaSuoritus.copy(
            tutkinto = Some(tutkinto),
            koulutusmoduuli = tutkinnonOsa
          )

          describe("Kun tutkinto löytyy ja osa kuuluu sen rakenteeseen") {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanTyönjohdonErikoisammattitutkinto, johtaminenJaHenkilöstönKehittäminen)
            it("palautetaan HTTP 200") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(200)))
          }

          describe("Kun tutkintoa ei löydy") {
            val suoritus = osanSuoritusToisestaTutkinnosta(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("123456", "koulutus"), Some("40/011/2001")), johtaminenJaHenkilöstönKehittäminen)
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia koulutus/123456 ei löydy koodistosta"))))
          }

          describe("Kun osa ei kuulu annetun tutkinnon rakenteeseen") {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanPerustutkinto, johtaminenJaHenkilöstönKehittäminen)
            it("palautetaan HTTP 200 (ei validoida rakennetta tässä)") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(200)))
          }

          describe("Kun tutkinnolla ei ole diaarinumeroa") {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = None), johtaminenJaHenkilöstönKehittäminen)
            it("palautetaan HTTP 200 (diaarinumeroa ei vaadita)") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(200)))
          }

          describe("Kun tutkinnon diaarinumero on virheellinen") {
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(osanSuoritusToisestaTutkinnosta(
              autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = Some("Boom boom kah")),
              johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla Boom boom kah"))))
          }
        }

        describe("Suoritustapa puuttuu") {
          it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, None) {
            verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu("Tutkinnolta puuttuu suoritustapa. Tutkinnon osasuorituksia ei hyväksytä."))
          })
        }

        describe("Suorituksen tila") {
          testSuorituksenTila[AmmatillisenTutkinnonosanSuoritus](tutkinnonOsaSuoritus, "tutkinnonosat/100023", { suoritus => { f => putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(f)} })

          describe("Kun tutkinto on VALMIS-tilassa ja sillä on osa, joka on KESKEN-tilassa") {
            val opiskeluOikeus = opiskeluoikeus().copy(suoritukset = List(tutkintoSuoritus.copy(
              suoritustapa = tutkinnonSuoritustapaNäyttönä, tila = tilaValmis, vahvistus = vahvistus(LocalDate.parse("2016-08-08")),osasuoritukset = Some(List(tutkinnonOsaSuoritus))
            )))

            it("palautetaan HTTP 400") (putOpiskeluOikeus(opiskeluOikeus) (
              verifyResponseStatus(400, TorErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Suorituksella koulutus/351301 on keskeneräinen osasuoritus tutkinnonosat/100023 vaikka suorituksen tila on VALMIS"))))
          }
        }
      }
    }

    describe("Tutkinnon tila ja arviointi") {
      testSuorituksenTila[AmmatillisenTutkinnonSuoritus](tutkintoSuoritus, "koulutus/351301", { suoritus => { f => {
        putOpiskeluOikeus(opiskeluoikeus().copy(suoritukset = List(suoritus)))(f)
      }}})
    }

    describe("Oppisopimus") {
      def toteutusOppisopimuksella(yTunnus: String): AmmatillisenTutkinnonSuoritus = {
        tutkintoSuoritus.copy(järjestämismuoto = Some(OppisopimuksellinenJärjestämismuoto(Koodistokoodiviite("20", "jarjestamismuoto"), Oppisopimus(Yritys("Reaktor", yTunnus)))))
      }

      describe("Kun ok") {
        it("palautetaan HTTP 200") (
          putOpiskeluOikeus(opiskeluoikeus(toteutusOppisopimuksella("1629284-5")))
            (verifyResponseStatus(200))
        )
      }

      describe("Virheellinen y-tunnus") {
        it("palautetaan HTTP 400") (
          putOpiskeluOikeus(opiskeluoikeus(toteutusOppisopimuksella("1629284x5")))
            (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*ECMA 262 regex.*".r)))
        )
      }
    }
  }

  private def testSuorituksenTila[T <: Suoritus](suoritus: T, desc: String, put: (T => ((=> Unit) => Unit))): Unit = {
    def copySuoritus(suoritus: T, t: Koodistokoodiviite, a: Option[List[AmmatillinenArviointi]], v: Option[Vahvistus], ap: Option[LocalDate] = None): T = {
      val alkamispäivä = ap.orElse(suoritus.alkamispäivä)
      (suoritus match {
        case s: AmmatillisenTutkinnonSuoritus => s.copy(tila = t, arviointi = a, vahvistus = v, alkamispäivä = alkamispäivä)
        case s: AmmatillisenTutkinnonosanSuoritus => s.copy(tila = t, arviointi = a, vahvistus = v, alkamispäivä = alkamispäivä)
      }).asInstanceOf[T]
    }


    def testKesken(tila: Koodistokoodiviite): Unit = {
      describe("Arviointi puuttuu") {
        it("palautetaan HTTP 200") (put(copySuoritus(suoritus, tila, None, None)) (
          verifyResponseStatus(200)
        ))
      }
      describe("Arviointi annettu") {
        it("palautetaan HTTP 200") (put(copySuoritus(suoritus, tila, arviointiHyvä(), None)) (
          verifyResponseStatus(200)
        ))
      }
      describe("Vahvistus annettu") {
        it("palautetaan HTTP 400") (put(copySuoritus(suoritus, tila, arviointiHyvä(), vahvistus(LocalDate.parse("2016-08-08")))) (
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.tila.vahvistusVäärässäTilassa("Suorituksella " + desc + " on vahvistus, vaikka suorituksen tila on " + tila.koodiarvo))
        ))
      }
    }
    describe("Kun suorituksen tila on KESKEN") {
      testKesken(tilaKesken)
    }

    describe("Kun suorituksen tila on KESKEYTYNYT") {
      testKesken(tilaKesken)
    }

    describe("Kun suorituksen tila on VALMIS") {
      describe("Suorituksella arviointi ja vahvistus") {
        it("palautetaan HTTP 200") (put(copySuoritus(suoritus, tilaValmis, arviointiHyvä(), vahvistus(LocalDate.parse("2016-08-08")))) (
          verifyResponseStatus(200)
        ))
      }
      describe("Vahvistus annettu, mutta arviointi puuttuu") {
        it("palautetaan HTTP 200") (put(copySuoritus(suoritus, tilaValmis, None, vahvistus(LocalDate.parse("2016-08-08")))) (
          verifyResponseStatus(200)
        ))
      }

      describe("Vahvistus puuttuu") {
        it("palautetaan HTTP 400") (put(copySuoritus(suoritus, tilaValmis, arviointiHyvä(), None)) (
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta " + desc + " puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
        ))
      }

      describe("Vahvistuksen myöntäjähenkilö puuttuu") {
        it("palautetaan HTTP 400") (put(copySuoritus(suoritus, tilaValmis, arviointiHyvä(), Some(Vahvistus(LocalDate.parse("2016-08-08"), stadinOpisto, Nil)))) (
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*array is too short.*".r))
        ))
      }

    }

    describe("Arviointi") {
      describe("Arviointiasteikko on tuntematon") {
        it("palautetaan HTTP 400") (put(copySuoritus(suoritus, suoritus.tila, Some(List(AmmatillinenArviointi(Koodistokoodiviite("2", "vääräasteikko"), None))), None))
          (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*not found in enum.*".r))))
      }

      describe("Arvosana ei kuulu perusteiden mukaiseen arviointiasteikkoon") {
        it("palautetaan HTTP 400") (put(copySuoritus(suoritus, suoritus.tila, Some(List(AmmatillinenArviointi(Koodistokoodiviite("x", "arviointiasteikkoammatillinent1k3"), None))), None))
          (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia arviointiasteikkoammatillinent1k3/x ei löydy koodistosta"))))
      }
    }

    describe("Suorituksen päivämäärät") {
      def päivämäärillä(alkamispäivä: String, arviointipäivä: String, vahvistuspäivä: String) = {
        copySuoritus(suoritus, tilaValmis, arviointiHyvä(Some(LocalDate.parse(arviointipäivä))), vahvistus(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)))
      }

      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200" ) (put(päivämäärillä("2015-08-01", "2016-05-30", "2016-06-01"))(
          verifyResponseStatus(200)))
      }

      describe("alkamispäivä > arviointi.päivä") {
        it("palautetaan HTTP 400" ) (put(päivämäärillä("2017-08-01", "2016-05-31", "2016-05-31"))(
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.loppuEnnenAlkua("suoritus.alkamispäivä (2017-08-01) oltava sama tai aiempi kuin suoritus.arviointi.päivä(2016-05-31)"))))
      }

      describe("arviointi.päivä > vahvistus.päivä") {
        it("palautetaan HTTP 400" ) (put(päivämäärillä("2015-08-01", "2016-05-31", "2016-05-30"))(
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.loppuEnnenAlkua("suoritus.arviointi.päivä (2016-05-31) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2016-05-30)"))))
      }
    }
  }
}
