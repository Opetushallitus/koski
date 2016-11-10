package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData.helsinki
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

class OppijaValidationAmmatillinenSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  describe("Ammatillisen koulutuksen opiskeluoikeuden lisääminen") {
    describe("Valideilla tiedoilla") {
      it("palautetaan HTTP 200") {
        putOpiskeluOikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(200)
        }
      }
    }

    describe("Kun tutkintosuoritus puuttuu") {
      it("palautetaan HTTP 400 virhe" ) {
        putOpiskeluOikeus(defaultOpiskeluoikeus.copy(suoritukset = Nil)) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*array is too short.*".r)))
      }
    }

    describe("Tutkinnon perusteet ja rakenne") {
      describe("Osaamisala ja suoritustapa") {
        describe("Osaamisala ja suoritustapa ok") {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Some(Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa")),
            osaamisala = Some(List(Koodistokoodiviite("1527", "osaamisala"))))

          it("palautetaan HTTP 200") (putTutkintoSuoritus(suoritus)(verifyResponseStatus(200)))
        }
        describe("Suoritustapa virheellinen") {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Some(Koodistokoodiviite("blahblahtest", "ammatillisentutkinnonsuoritustapa")),
            osaamisala = Some(List(Koodistokoodiviite("1527", "osaamisala"))))

          it("palautetaan HTTP 400") (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia ammatillisentutkinnonsuoritustapa/blahblahtest ei löydy koodistosta"))))
        }
        describe("Osaamisala ei löydy tutkintorakenteesta") {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Some(Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa")),
            osaamisala = Some(List(Koodistokoodiviite("3053", "osaamisala"))))

          it("palautetaan HTTP 400") (putTutkintoSuoritus(suoritus) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala 3053 ei löydy tutkintorakenteesta perusteelle 39/011/2014"))))
        }
        describe("Osaamisala virheellinen") {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Some(Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa")),
            osaamisala = Some(List(Koodistokoodiviite("0", "osaamisala"))))

          it("palautetaan HTTP 400")(putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia osaamisala/0 ei löydy koodistosta"))))
        }
      }

      describe("Tutkinnon osat ja arvionnit") {
        val johtaminenJaHenkilöstönKehittäminen: ValtakunnallinenTutkinnonOsa = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None)

        describe("OPS-perusteinen tutkinnonosa") {
          describe("Tutkinnon osa ja arviointi ok") {
            it("palautetaan HTTP 200") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, tutkinnonSuoritustapaNäyttönä) (verifyResponseStatus(200)))
          }

          describe("Tutkinnon osa ei kuulu tutkintorakenteeseen") {
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa tutkinnonosat/104052 ei löydy tutkintorakenteesta perusteelle 39/011/2014 - suoritustapa naytto"))))
          }

          describe("Tutkinnon osaa ei ei löydy koodistosta") {
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
              koulutusmoduuli = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite("9923123", "tutkinnonosat"), true, None)), tutkinnonSuoritustapaNäyttönä)
              (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia tutkinnonosat/9923123 ei löydy koodistosta"))))
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
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*numeric instance is lower than the required minimum.*".r)))
            )
          }
        }

        describe("Tuntematon tutkinnonosa") {
          it("palautetaan HTTP 400 virhe" ) {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(tyyppi = Koodistokoodiviite(koodiarvo = "tuntematon", koodistoUri = "suorituksentyyppi"))
            putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*instance value ..tuntematon.. not found in enum.*".r))
            )
          }
        }

        describe("Tutkinnon osa toisesta tutkinnosta") {
          val autoalanTyönjohdonErikoisammattitutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", "koulutus"), Some("40/011/2001"))

          def osanSuoritusToisestaTutkinnosta(tutkinto: AmmatillinenTutkintoKoulutus, tutkinnonOsa: ValtakunnallinenTutkinnonOsa): AmmatillisenTutkinnonOsanSuoritus = tutkinnonOsaSuoritus.copy(
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
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia koulutus/123456 ei löydy koodistosta"))))
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
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla Boom boom kah"))))
          }
        }

        describe("Suoritustapa puuttuu") {
          it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, None) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu("Tutkinnolta puuttuu suoritustapa. Tutkinnon osasuorituksia ei hyväksytä."))
          })
        }

        describe("Suorituksen tila") {
          def copySuoritus(t: Koodistokoodiviite, a: Option[List[AmmatillinenArviointi]], v: Option[Henkilövahvistus], ap: Option[LocalDate] = None): AmmatillisenTutkinnonOsanSuoritus = {
            val alkamispäivä = ap.orElse(tutkinnonOsaSuoritus.alkamispäivä)
            tutkinnonOsaSuoritus.copy(tila = t, arviointi = a, vahvistus = v, alkamispäivä = alkamispäivä)
          }

          def put(suoritus: AmmatillisenTutkinnonOsanSuoritus)(f: => Unit) = {
            putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(f)
          }


          def testKesken(tila: Koodistokoodiviite): Unit = {
            describe("Arviointi puuttuu") {
              it("palautetaan HTTP 200") (put(copySuoritus(tila, None, None)) (
                verifyResponseStatus(200)
              ))
            }
            describe("Arviointi annettu") {
              it("palautetaan HTTP 200") (put(copySuoritus(tila, arviointiHyvä(), None)) (
                verifyResponseStatus(200)
              ))
            }
            describe("Vahvistus annettu") {
              it("palautetaan HTTP 400") (put(copySuoritus(tila, arviointiHyvä(), vahvistus(LocalDate.parse("2016-08-08")))) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusVäärässäTilassa("Suorituksella tutkinnonosat/100023 on vahvistus, vaikka suorituksen tila on " + tila.koodiarvo))
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
              it("palautetaan HTTP 200") (put(copySuoritus(tilaValmis, arviointiHyvä(), vahvistus(LocalDate.parse("2016-08-08")))) (
                verifyResponseStatus(200)
              ))
            }
            describe("Vahvistus annettu, mutta arviointi puuttuu") {
              it("palautetaan HTTP 200") (put(copySuoritus(tilaValmis, None, vahvistus(LocalDate.parse("2016-08-08")))) (
                verifyResponseStatus(200)
              ))
            }

            describe("Vahvistus puuttuu") {
              it("palautetaan HTTP 400") (put(copySuoritus(tilaValmis, arviointiHyvä(), None)) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta tutkinnonosat/100023 puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
              ))
            }

            describe("Vahvistuksen myöntäjähenkilö puuttuu") {
              it("palautetaan HTTP 400") (put(copySuoritus(tilaValmis, arviointiHyvä(), Some(Henkilövahvistus(LocalDate.parse("2016-08-08"), helsinki, stadinOpisto, Nil)))) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*array is too short.*".r))
              ))
            }

          }

          describe("Arviointi") {
            describe("Arviointiasteikko on tuntematon") {
              it("palautetaan HTTP 400") (put(copySuoritus(tutkinnonOsaSuoritus.tila, Some(List(AmmatillinenArviointi(Koodistokoodiviite("2", "vääräasteikko"), date(2015, 5, 1)))), None))
                (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*not found in enum.*".r))))
            }

            describe("Arvosana ei kuulu perusteiden mukaiseen arviointiasteikkoon") {
              it("palautetaan HTTP 400") (put(copySuoritus(tutkinnonOsaSuoritus.tila, Some(List(AmmatillinenArviointi(Koodistokoodiviite("x", "arviointiasteikkoammatillinent1k3"), date(2015, 5, 1)))), None))
                (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia arviointiasteikkoammatillinent1k3/x ei löydy koodistosta"))))
            }
          }

          describe("Suorituksen päivämäärät") {
            def päivämäärillä(alkamispäivä: String, arviointipäivä: String, vahvistuspäivä: String) = {
              copySuoritus(tilaValmis, arviointiHyvä(LocalDate.parse(arviointipäivä)), vahvistus(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)))
            }

            describe("Päivämäärät kunnossa") {
              it("palautetaan HTTP 200" ) (put(päivämäärillä("2015-08-01", "2016-05-30", "2016-06-01"))(
                verifyResponseStatus(200)))
            }

            describe("Päivämäärät tulevaisuudessa") {
              it("palautetaan HTTP 200" ) (put(päivämäärillä("2115-08-01", "2116-05-30", "2116-06-01"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.tulevaisuudessa("Päivämäärä suoritus.alkamispäivä (2115-08-01) on tulevaisuudessa"),
                                          KoskiErrorCategory.badRequest.validation.date.tulevaisuudessa("Päivämäärä suoritus.arviointi.päivä (2116-05-30) on tulevaisuudessa"),
                                          KoskiErrorCategory.badRequest.validation.date.tulevaisuudessa("Päivämäärä suoritus.vahvistus.päivä (2116-06-01) on tulevaisuudessa")
                )))
            }

            describe("alkamispäivä > arviointi.päivä") {
              it("palautetaan HTTP 400" ) (put(päivämäärillä("2016-08-01", "2015-05-31", "2015-05-31"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua("suoritus.alkamispäivä (2016-08-01) oltava sama tai aiempi kuin suoritus.arviointi.päivä(2015-05-31)"))))
            }

            describe("arviointi.päivä > vahvistus.päivä") {
              it("palautetaan HTTP 400" ) (put(päivämäärillä("2015-08-01", "2016-05-31", "2016-05-30"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua("suoritus.arviointi.päivä (2016-05-31) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2016-05-30)"))))
            }
          }

          describe("Kun tutkinto on VALMIS-tilassa ja sillä on osa, joka on KESKEN-tilassa") {
            val opiskeluOikeus = defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(
              suoritustapa = tutkinnonSuoritustapaNäyttönä,
              tila = tilaValmis,
              vahvistus = vahvistus(LocalDate.parse("2016-10-08")),
              osasuoritukset = Some(List(tutkinnonOsaSuoritus))
            )))

            it("palautetaan HTTP 400") (putOpiskeluOikeus(opiskeluOikeus) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Suorituksella koulutus/351301 on keskeneräinen osasuoritus tutkinnonosat/100023 vaikka suorituksen tila on VALMIS"))))
          }
        }
      }
    }

    describe("Tutkinnon tila ja arviointi") {
      def copySuoritus(t: Koodistokoodiviite, v: Option[Henkilövahvistus], ap: Option[LocalDate] = None) = {
        val alkamispäivä = ap.orElse(tutkinnonOsaSuoritus.alkamispäivä)
        autoalanPerustutkinnonSuoritus().copy(tila = t, vahvistus = v, alkamispäivä = alkamispäivä)
      }

      def put(s: AmmatillisenTutkinnonSuoritus)(f: => Unit) = {
        putOpiskeluOikeus(defaultOpiskeluoikeus.copy(suoritukset = List(s)))(f)
      }

      def testKesken(tila: Koodistokoodiviite): Unit = {
        describe("Vahvistus puuttuu") {
          it("palautetaan HTTP 200") (put(copySuoritus(tila, None, None)) (
            verifyResponseStatus(200)
          ))
        }
        describe("Vahvistus annettu") {
          it("palautetaan HTTP 400") (put(copySuoritus(tila, vahvistus(LocalDate.parse("2016-08-08")))) (
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusVäärässäTilassa("Suorituksella koulutus/351301 on vahvistus, vaikka suorituksen tila on " + tila.koodiarvo))
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
        describe("Suorituksella on vahvistus") {
          it("palautetaan HTTP 200") (put(copySuoritus(tilaValmis, vahvistus(LocalDate.parse("2016-08-08")))) (
            verifyResponseStatus(200)
          ))
        }

        describe("Vahvistus puuttuu") {
          it("palautetaan HTTP 400") (put(copySuoritus(tilaValmis, None)) (
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/351301 puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
          ))
        }

        describe("Vahvistuksen myöntäjähenkilö puuttuu") {
          it("palautetaan HTTP 400") (put(copySuoritus(tilaValmis, Some(Henkilövahvistus(LocalDate.parse("2016-08-08"), helsinki, stadinOpisto, Nil)))) (
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*array is too short.*".r))
          ))
        }

      }

      describe("Suorituksen päivämäärät") {
        def päivämäärillä(alkamispäivä: String, vahvistuspäivä: String) = {
          copySuoritus(tilaValmis, vahvistus(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)))
        }

        describe("Päivämäärät kunnossa") {
          it("palautetaan HTTP 200" ) (put(päivämäärillä("2015-08-01", "2016-06-01"))(
            verifyResponseStatus(200)))
        }

        describe("alkamispäivä > vahvistus.päivä") {
          it("palautetaan HTTP 400" ) (put(päivämäärillä("2016-08-01", "2015-05-31"))(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua("suoritus.alkamispäivä (2016-08-01) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2015-05-31)"))))
        }
      }
    }

    describe("Oppisopimus") {
      def toteutusOppisopimuksella(yTunnus: String): AmmatillisenTutkinnonSuoritus = {
        autoalanPerustutkinnonSuoritus().copy(järjestämismuoto = Some(OppisopimuksellinenJärjestämismuoto(Koodistokoodiviite("20", "jarjestamismuoto"), Oppisopimus(Yritys("Reaktor", yTunnus)))))
      }

      describe("Kun ok") {
        it("palautetaan HTTP 200") (
          putOpiskeluOikeus(defaultOpiskeluoikeus.copy(suoritukset = List(toteutusOppisopimuksella("1629284-5"))))
            (verifyResponseStatus(200))
        )
      }

      describe("Virheellinen y-tunnus") {
        it("palautetaan HTTP 400") (
          putOpiskeluOikeus(defaultOpiskeluoikeus.copy(suoritukset = List(toteutusOppisopimuksella("1629284x5"))))
            (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*ECMA 262 regex.*".r)))
        )
      }
    }
  }

  def vahvistus(date: LocalDate) = {
    Some(Henkilövahvistus(date, helsinki, stadinOpisto, List(Organisaatiohenkilö("Teppo Testaaja", "rehtori", stadinOpisto))))
  }

  def arviointiHyvä(päivä: LocalDate = date(2015, 1, 1)): Some[List[AmmatillinenArviointi]] = Some(List(AmmatillinenArviointi(Koodistokoodiviite("2", "arviointiasteikkoammatillinent1k3"), päivä)))

  lazy val stadinOpisto: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.stadinAmmattiopisto)

  lazy val laajuus = LaajuusOsaamispisteissä(11)

  lazy val tutkinnonOsa: ValtakunnallinenTutkinnonOsa = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100023", "tutkinnonosat"), true, Some(laajuus))

  lazy val tutkinnonSuoritustapaNäyttönä = Some(Koodistokoodiviite("naytto", "ammatillisentutkinnonsuoritustapa"))

  lazy val tutkinnonOsaSuoritus = AmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = tutkinnonOsa,
    tila = tilaKesken,
    toimipiste = Some(OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
    arviointi = arviointiHyvä()
  )

  lazy val paikallinenTutkinnonOsa = PaikallinenTutkinnonOsa(
    PaikallinenKoodi("1", "paikallinen osa"), "Paikallinen tutkinnon osa", false, Some(laajuus)
  )

  lazy val paikallinenTutkinnonOsaSuoritus = AmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = paikallinenTutkinnonOsa,
    tila = tilaKesken,
    toimipiste = Some(OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
    arviointi = arviointiHyvä()
  )

  def putTutkinnonOsaSuoritus[A](tutkinnonOsaSuoritus: AmmatillisenTutkinnonOsanSuoritus, tutkinnonSuoritustapa: Option[Koodistokoodiviite])(f: => A) = {
    val s = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapa, osasuoritukset = Some(List(tutkinnonOsaSuoritus)))

    putTutkintoSuoritus(s)(f)
  }

  def putTutkintoSuoritus[A](suoritus: AmmatillisenTutkinnonSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    val opiskeluOikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))

    putOppija(makeOppija(henkilö, List(Json.toJValue(opiskeluOikeus))), headers)(f)
  }

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(koulutusmoduuli = autoalanPerustutkinnonSuoritus().koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  override def vääräntyyppisenPerusteenDiaarinumero: String = "60/011/2015"
}
