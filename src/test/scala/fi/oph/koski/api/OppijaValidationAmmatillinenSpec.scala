package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData.helsinki
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

class OppijaValidationAmmatillinenSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  "Ammatillisen koulutuksen opiskeluoikeuden lisääminen" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(200)
        }
      }
    }

    "Kun tutkintosuoritus puuttuu" - {
      "palautetaan HTTP 400 virhe"  in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = Nil)) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lessThanMinimumNumberOfItems.*".r)))
      }
    }

    "Tutkinnon perusteet ja rakenne" - {
      "Osaamisala ja suoritustapa" - {
        "Osaamisala ja suoritustapa ok" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Koodistokoodiviite("1527", "osaamisala"))))

          "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(200)))
        }
        "Suoritustapa virheellinen" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("blahblahtest", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Koodistokoodiviite("1527", "osaamisala"))))

          "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia ammatillisentutkinnonsuoritustapa/blahblahtest ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
        }
        "Osaamisala ei löydy tutkintorakenteesta" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Koodistokoodiviite("3053", "osaamisala"))))

          "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala 3053 ei löydy tutkintorakenteesta perusteelle 39/011/2014"))))
        }
        "Osaamisala virheellinen" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Koodistokoodiviite("0", "osaamisala"))))

          "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia osaamisala/0 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
        }
      }

      "Tutkinnon osat ja arvionnit" - {
        val johtaminenJaHenkilöstönKehittäminen = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None)

        "Valtakunnallinen tutkinnonosa" - {
          "Tutkinnon osa ja arviointi ok" - {
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, tutkinnonSuoritustapaNäyttönä) (verifyResponseStatus(200)))
          }

          "Tutkinnon osa ei kuulu tutkintorakenteeseen" - {
            "Pakolliset ja ammatilliset tutkinnon osat" - {
              "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa tutkinnonosat/104052 ei löydy tutkintorakenteesta perusteelle 39/011/2014 - suoritustapa naytto"))))
            }
            "Vapaavalintaiset tutkinnon osat" - {
              "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
                  koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen, tutkinnonOsanRyhmä = vapaavalintaisetTutkinnonOsat
                ), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(200)))
            }
          }

          "Tutkinnon osaa ei ei löydy koodistosta" - {
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
              koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("9923123", "tutkinnonosat"), true, None)), tutkinnonSuoritustapaNäyttönä)
              (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia tutkinnonosat/9923123 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
          }

          "Sama tutkinnon osa kahteen kertaan" - {
            val suoritus = autoalanPerustutkinnonSuoritus().copy(
              osasuoritukset = Some(List(
                tutkinnonOsaSuoritus, tutkinnonOsaSuoritus
              ))
            )

            "Palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Tutkinnon osa tutkinnonosat/100023 esiintyy useammin kuin kerran ryhmässä ammatillisentutkinnonosanryhma/1")))
            )
          }

          "Tutkinnon osan osat" - {
            "Sama osa kahteen kertaan" - {
              val osanOsa = AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
                AmmatillisenTutkinnonOsaaPienempiKokonaisuus(PaikallinenKoodi("htm", "Hoitotarpeen määrittäminen"), "Hoitotarpeen määrittäminen"),
                arviointi = Some(List(arviointiHyväksytty))
              )

              "Palautetaan HTTP 200" in (
                putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(osasuoritukset = Some(List(
                  osanOsa, osanOsa
                ))), tutkinnonSuoritustapaNäyttönä) (verifyResponseStatus(200))
              )
            }
          }
        }

        "Paikallinen tutkinnonosa" - {
          "Tutkinnon osa ja arviointi ok" - {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat)
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (verifyResponseStatus(200)))
          }

          "Laajuus negatiivinen" - {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(koulutusmoduuli = paikallinenTutkinnonOsa.copy(laajuus = Some(laajuus.copy(arvo = -1))))
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (
              verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*exclusiveMinimumValue.*".r)))
            )
          }
        }

        "Tuntematon tutkinnonosa" - {
          "palautetaan HTTP 400 virhe"  in {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(tyyppi = Koodistokoodiviite(koodiarvo = "tuntematon", koodistoUri = "suorituksentyyppi"))
            putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (
              verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*101053, 101054, 101055, 101056.*".r))
            )
          }
        }

        "Tutkinnon osa toisesta tutkinnosta" - {
          val autoalanTyönjohdonErikoisammattitutkinto = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", "koulutus"), Some("40/011/2001"))

          def osanSuoritusToisestaTutkinnosta(tutkinto: AmmatillinenTutkintoKoulutus, tutkinnonOsa: MuuKuinYhteinenTutkinnonOsa): AmmatillisenTutkinnonOsanSuoritus = tutkinnonOsaSuoritus.copy(
            tutkinto = Some(tutkinto),
            koulutusmoduuli = tutkinnonOsa
          )

          "Kun tutkinto löytyy ja osa kuuluu sen rakenteeseen" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanTyönjohdonErikoisammattitutkinto, johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(200)))
          }

          "Kun tutkintoa ei löydy" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("123456", "koulutus"), Some("40/011/2001")), johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia koulutus/123456 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
          }

          "Kun osa ei kuulu annetun tutkinnon rakenteeseen" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(parturikampaaja, johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 200 (ei validoida rakennetta tässä)" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(200)))
          }

          "Kun tutkinnolla ei ole diaarinumeroa" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = None), johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 400 (diaarinumero vaaditaan)" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu())))
          }

          "Kun tutkinnon diaarinumero on virheellinen" - {
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(osanSuoritusToisestaTutkinnosta(
              autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = Some("Boom boom kah")),
              johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla Boom boom kah"))))
          }

          "Kun tutkinnon osalle ilmoitetaan tutkintotieto, joka on sama kuin päätason tutkinto" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanPerustutkinto, johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.samaTutkintokoodi("Tutkinnon osalle tutkinnonosat/104052 on merkitty tutkinto, jossa on sama tutkintokoodi koulutus/351301 kuin tutkinnon suorituksessa"))))
          }
        }

        "Suorituksen tila" - {
          def copySuoritus(a: Option[List[AmmatillinenArviointi]], v: Option[HenkilövahvistusValinnaisellaTittelillä], ap: Option[LocalDate] = None): AmmatillisenTutkinnonOsanSuoritus = {
            val alkamispäivä = ap.orElse(tutkinnonOsaSuoritus.alkamispäivä)
            tutkinnonOsaSuoritus.copy(arviointi = a, vahvistus = v, alkamispäivä = alkamispäivä)
          }

          def put(suoritus: AmmatillisenTutkinnonOsanSuoritus)(f: => Unit) = {
            putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(f)
          }


          "Arviointi ja vahvistus puuttuu" - {
            "palautetaan HTTP 200" in (put(copySuoritus(None, None)) (
              verifyResponseStatus(200)
            ))
          }

          "Arviointi annettu" - {
            "palautetaan HTTP 200" in (put(copySuoritus(arviointiHyvä(), None)) (
              verifyResponseStatus(200)
            ))
          }

          "Suorituksella arviointi ja vahvistus" - {
            "palautetaan HTTP 200" in (put(copySuoritus(arviointiHyvä(), vahvistusValinnaisellaTittelillä(LocalDate.parse("2016-08-08")))) (
              verifyResponseStatus(200)
            ))
          }

          "Vahvistus annettu, mutta arviointi puuttuu" - {
            "palautetaan HTTP 400" in (put(copySuoritus(None, vahvistusValinnaisellaTittelillä(LocalDate.parse("2016-08-08")))) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusIlmanArviointia("Suorituksella tutkinnonosat/100023 on vahvistus, vaikka arviointi puuttuu"))
            ))
          }

          "Vahvistuksen myöntäjähenkilö puuttuu" - {
            "palautetaan HTTP 400" in (put(copySuoritus(arviointiHyvä(), Some(HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(LocalDate.parse("2016-08-08"), Some(helsinki), stadinOpisto, Nil)))) (
              verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lessThanMinimumNumberOfItems.*".r))
            ))
          }

          "Arviointi" - {
            "Arviointiasteikko on tuntematon" - {
              "palautetaan HTTP 400" in (put(copySuoritus(Some(List(AmmatillinenArviointi(Koodistokoodiviite("2", "vääräasteikko"), date(2015, 5, 1)))), None))
                (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*arviointiasteikkoammatillinenhyvaksyttyhylatty.*enumValueMismatch.*".r))))
            }

            "Arvosana ei kuulu perusteiden mukaiseen arviointiasteikkoon" - {
              "palautetaan HTTP 400" in (put(copySuoritus(Some(List(AmmatillinenArviointi(Koodistokoodiviite("x", "arviointiasteikkoammatillinent1k3"), date(2015, 5, 1)))), None))
                (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia arviointiasteikkoammatillinent1k3/x ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
            }
          }

          "Suorituksen päivämäärät" - {
            def päivämäärillä(alkamispäivä: String, arviointipäivä: String, vahvistuspäivä: String) = {
              copySuoritus(arviointiHyvä(LocalDate.parse(arviointipäivä)), vahvistusValinnaisellaTittelillä(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)))
            }

            "Päivämäärät kunnossa" - {
              "palautetaan HTTP 200"  in (put(päivämäärillä("2015-08-01", "2016-05-30", "2016-06-01"))(
                verifyResponseStatus(200)))
            }

            "Päivämäärät tulevaisuudessa" - {
              "palautetaan HTTP 200"  in (put(päivämäärillä("2115-08-01", "2116-05-30", "2116-06-01"))(
                verifyResponseStatus(200)))
            }

            "alkamispäivä > arviointi.päivä" - {
              "palautetaan HTTP 400"  in (put(päivämäärillä("2016-08-01", "2015-05-31", "2015-05-31"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.arviointiEnnenAlkamispäivää("suoritus.alkamispäivä (2016-08-01) oltava sama tai aiempi kuin suoritus.arviointi.päivä(2015-05-31)"))))
            }

            "arviointi.päivä > vahvistus.päivä" - {
              "palautetaan HTTP 400"  in (put(päivämäärillä("2015-08-01", "2016-05-31", "2016-05-30"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenArviointia("suoritus.arviointi.päivä (2016-05-31) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2016-05-30)"))))
            }

            "osasuoritus.vahvistus.päivä > suoritus.vahvistus.päivä" - {
              "palautetaan HTTP 400"  in {
                val suoritus: AmmatillisenTutkinnonSuoritus = withTutkinnonOsaSuoritus(päivämäärillä("2015-08-01", "2017-05-30", vahvistuspäivä = "2017-06-01"), tutkinnonSuoritustapaNäyttönä)
                putTutkintoSuoritus(suoritus.copy(vahvistus = vahvistus(date(2017, 5, 31)))) {
                  verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.suorituksenVahvistusEnnenSuorituksenOsanVahvistusta("osasuoritus.vahvistus.päivä (2017-06-01) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2017-05-31)"))
                }
              }
            }
          }

          "Kun tutkinnolla on vahvistus, mutta tutkinnon osalta puuttuu arviointi" - {
            val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(
              suoritustapa = tutkinnonSuoritustapaNäyttönä,
              vahvistus = vahvistus(LocalDate.parse("2016-10-08")),
              osasuoritukset = Some(List(tutkinnonOsaSuoritus.copy(arviointi = None)))
            )))

            "palautetaan HTTP 400" in (putOpiskeluoikeus(opiskeluoikeus) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella koulutus/351301 on keskeneräinen osasuoritus tutkinnonosat/100023"))))
          }
        }
      }
    }

    "Tutkinnon tila ja arviointi" - {
      def copySuoritus(v: Option[HenkilövahvistusValinnaisellaPaikkakunnalla], ap: Option[LocalDate] = None) = {
        val alkamispäivä = ap.orElse(tutkinnonOsaSuoritus.alkamispäivä)
        autoalanPerustutkinnonSuoritus().copy(vahvistus = v, alkamispäivä = alkamispäivä)
      }

      def put(s: AmmatillisenTutkinnonSuoritus)(f: => Unit) = {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(s)))(f)
      }

      "Vahvistus puuttuu, opiskeluoikeus voimassa" - {
        "palautetaan HTTP 200" in (put(copySuoritus(None, None)) (
          verifyResponseStatus(200)
        ))
      }

      "Vahvistus puuttuu, opiskeluoikeus valmistunut" - {
        "palautetaan HTTP 400" in (putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)).copy(suoritukset = List(copySuoritus(v = None)))) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/351301 puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut"))
        ))
      }

      "Suorituksella on vahvistus" - {
        "palautetaan HTTP 200" in (put(copySuoritus(vahvistus(LocalDate.parse("2016-08-08")))) (
          verifyResponseStatus(200)
        ))
      }

      "Vahvistuksen myöntäjähenkilö puuttuu" - {
        "palautetaan HTTP 400" in (put(copySuoritus(Some(HenkilövahvistusValinnaisellaPaikkakunnalla(LocalDate.parse("2016-08-08"), Some(helsinki), stadinOpisto, Nil)))) (
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lessThanMinimumNumberOfItems.*".r))
        ))
      }

      "Suorituksen päivämäärät" - {
        def päivämäärillä(alkamispäivä: String, vahvistuspäivä: String) = {
          copySuoritus(vahvistus(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)))
        }

        "Päivämäärät kunnossa" - {
          "palautetaan HTTP 200"  in (put(päivämäärillä("2015-08-01", "2016-06-01"))(
            verifyResponseStatus(200)))
        }

        "alkamispäivä > vahvistus.päivä" - {
          "palautetaan HTTP 400"  in (put(päivämäärillä("2016-08-01", "2015-05-31"))(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenAlkamispäivää("suoritus.alkamispäivä (2016-08-01) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2015-05-31)"))))
        }
      }
    }

    "Ammatillinen perustutkinto opetussuunnitelman mukaisesti" - {
      "Tutkinnonosan ryhmä on määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, osasuoritukset = Some(List(tutkinnonOsaSuoritus)))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(200)))
      }

      "Tutkinnonosan ryhmää ei ole määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, osasuoritukset = Some(List(tutkinnonOsaSuoritus.copy(tutkinnonOsanRyhmä = None))))
        "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tutkinnonOsanRyhmäPuuttuu("Tutkinnonosalta tutkinnonosat/100023 puuttuu tutkinnonosan ryhmä, joka on pakollinen ammatillisen perustutkinnon tutkinnonosille, kun suoritustapa on opetussuunnitelman mukainen."))))
      }
    }

    "Ammatillinen perustutkinto näyttönä" - {
      "Tutkinnonosan ryhmä on määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osasuoritukset = Some(List(tutkinnonOsaSuoritus)))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(200)))
      }

      "Tutkinnonosan ryhmää ei ole määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osasuoritukset = Some(List(tutkinnonOsaSuoritus.copy(tutkinnonOsanRyhmä = None))))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(200)))
      }
    }

    "Ammatti- tai erikoisammattitutkinto" - {
      val tutkinnonOsanSuoritus = tutkinnonOsaSuoritus.copy(
        koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None)
      )

      def erikoisammattitutkintoSuoritus(osasuoritus: AmmatillisenTutkinnonOsanSuoritus) = autoalanErikoisammattitutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osasuoritukset = Some(List(osasuoritus)))

      "Tutkinnonosan ryhmää ei ole määritetty" - {
        val suoritus = erikoisammattitutkintoSuoritus(tutkinnonOsanSuoritus.copy(tutkinnonOsanRyhmä = None))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(200)))
      }

      "Tutkinnonosan ryhmä on määritetty" - {
        val suoritus = erikoisammattitutkintoSuoritus(tutkinnonOsanSuoritus)
        "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.koulutustyyppiEiSalliTutkinnonOsienRyhmittelyä("Tutkinnonosalle tutkinnonosat/104052 on määritetty tutkinnonosan ryhmä, vaikka kyseessä ei ole ammatillinen perustutkinto."))))
      }

      "Tutkinnonosan ryhmä on määritetty ja diaarinumero puuttuu" - {
        val suoritus = erikoisammattitutkintoSuoritus(tutkinnonOsanSuoritus)
        "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus.copy(koulutusmoduuli = suoritus.koulutusmoduuli.copy(perusteenDiaarinumero = None)))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu())))
      }

      "Suoritustapana OPS" - {
        val suoritus = erikoisammattitutkintoSuoritus(tutkinnonOsanSuoritus.copy(tutkinnonOsanRyhmä = None)).copy(suoritustapa = suoritustapaOps)
        "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaaEiLöydyRakenteesta("Suoritustapaa ei löydy tutkinnon rakenteesta"))))
      }
    }

    "Oppisopimus" - {
      def toteutusOppisopimuksella(yTunnus: String): AmmatillisenTutkinnonSuoritus = {
        autoalanPerustutkinnonSuoritus().copy(järjestämismuodot = Some(List(Järjestämismuotojakso(date(2012, 1, 1), None, OppisopimuksellinenJärjestämismuoto(Koodistokoodiviite("20", "jarjestamismuoto"), Oppisopimus(Yritys("Reaktor", yTunnus)))))))
      }

      "Kun ok" - {
        "palautetaan HTTP 200" in (
          putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(toteutusOppisopimuksella("1629284-5"))))
            (verifyResponseStatus(200))
        )
      }

      "Virheellinen y-tunnus" - {
        "palautetaan HTTP 400" in (
          putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(toteutusOppisopimuksella("1629284x5"))))
            (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*regularExpressionMismatch.*".r)))
        )
      }
    }
  }

  def vahvistus(date: LocalDate) = {
    Some(HenkilövahvistusValinnaisellaPaikkakunnalla(date, Some(helsinki), stadinOpisto, List(Organisaatiohenkilö("Teppo Testaaja", "rehtori", stadinOpisto))))
  }


  def vahvistusValinnaisellaTittelillä(date: LocalDate) = {
    Some(HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(date, Some(helsinki), stadinOpisto, List(OrganisaatiohenkilöValinnaisellaTittelillä("Teppo Testaaja", Some("rehtori"), stadinOpisto))))
  }

  def arviointiHyvä(päivä: LocalDate = date(2015, 1, 1)): Some[List[AmmatillinenArviointi]] = Some(List(AmmatillinenArviointi(Koodistokoodiviite("2", "arviointiasteikkoammatillinent1k3"), päivä)))

  lazy val stadinOpisto: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.stadinAmmattiopisto)

  lazy val laajuus = LaajuusOsaamispisteissä(11)

  lazy val tutkinnonOsa: MuuValtakunnallinenTutkinnonOsa = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100023", "tutkinnonosat"), true, Some(laajuus))

  lazy val tutkinnonSuoritustapaNäyttönä = Koodistokoodiviite("naytto", "ammatillisentutkinnonsuoritustapa")
  lazy val tutkinnonSuoritustapaOps = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa")

  lazy val tutkinnonOsaSuoritus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = tutkinnonOsa,
    toimipiste = Some(OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
    arviointi = arviointiHyvä(),
    tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat
  )

  lazy val paikallinenTutkinnonOsa = PaikallinenTutkinnonOsa(
    PaikallinenKoodi("1", "paikallinen osa"), "Paikallinen tutkinnon osa", false, Some(laajuus)
  )

  lazy val paikallinenTutkinnonOsaSuoritus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = paikallinenTutkinnonOsa,
    toimipiste = Some(OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
    arviointi = arviointiHyvä()
  )

  def putTutkinnonOsaSuoritus[A](tutkinnonOsaSuoritus: AmmatillisenTutkinnonOsanSuoritus, tutkinnonSuoritustapa: Koodistokoodiviite)(f: => A) = {
    putTutkintoSuoritus(withTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, tutkinnonSuoritustapa))(f)
  }

  def withTutkinnonOsaSuoritus(tutkinnonOsaSuoritus: AmmatillisenTutkinnonOsanSuoritus, tutkinnonSuoritustapa: Koodistokoodiviite): AmmatillisenTutkinnonSuoritus =
    autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapa, osasuoritukset = Some(List(tutkinnonOsaSuoritus)))

  def putTutkintoSuoritus[A](suoritus: AmmatillisenTutkinnonSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))

    putOppija(makeOppija(henkilö, List(JsonSerializer.serializeWithRoot(opiskeluoikeus))), headers)(f)
  }

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(koulutusmoduuli = autoalanPerustutkinnonSuoritus().koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  override def vääräntyyppisenPerusteenDiaarinumero: String = "60/011/2015"
  def eperusteistaLöytymätönValidiDiaarinumero: String = "13/011/2009"
}
