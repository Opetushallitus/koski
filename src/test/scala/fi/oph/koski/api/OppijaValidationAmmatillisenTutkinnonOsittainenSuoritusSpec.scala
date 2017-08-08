package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData.{helsinki, longTimeAgo, opiskeluoikeusLäsnä}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

class OppijaValidationAmmatillisenTutkinnonOsittainenSuoritusSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with LocalJettyHttpSpecification with PutOpiskeluoikeusTestMethods[AmmatillinenOpiskeluoikeus] {
  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo)

  def makeOpiskeluoikeus(alkamispäivä: LocalDate = longTimeAgo) = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(alkamispäivä),
    tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, None))),
    oppilaitos = Some(Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto)),
    suoritukset = List(osittainenSuoritusKesken)
  )

  def valmisTutkinnonOsa = tutkinnonOsaSuoritus
  def osittainenSuoritusKesken = ammatillisenTutkinnonOsittainenSuoritus.copy(tila = tilaKesken)

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
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = Nil)) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*lessThanMinimumNumberOfItems.*".r)))
      }
    }

    "Tutkinnon perusteet ja rakenne" - {
      "Tutkinnon osat ja arvionnit" - {
        val johtaminenJaHenkilöstönKehittäminen = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None)

        "OPS-perusteinen tutkinnonosa" - {
          "Tutkinnon osa ja arviointi ok" - {
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus) (verifyResponseStatus(200)))
          }

          "Tutkinnon osa ei kuulu tutkintorakenteeseen" - {
            "palautetaan HTTP 200 (osittaisissa suorituksissa ei validoida rakennetta)" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen))(
              verifyResponseStatus(200)))
          }

          "Tutkinnon osaa ei ei löydy koodistosta" - {
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
              koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("9923123", "tutkinnonosat"), true, None)))
              (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(""".*"message":"Koodia tutkinnonosat/9923123 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
          }
        }

        "Paikallinen tutkinnonosa" - {
          "Tutkinnon osa ja arviointi ok" - {
            val suoritus = paikallinenTutkinnonOsaSuoritus
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(suoritus) (verifyResponseStatus(200)))
          }

          "Laajuus negatiivinen" - {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(koulutusmoduuli = paikallinenTutkinnonOsa.copy(laajuus = Some(laajuus.copy(arvo = -1))))
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(suoritus) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*exclusiveMinimumValue.*".r)))
            )
          }
        }

        "Tuntematon tutkinnonosa" - {
          "palautetaan HTTP 400 virhe"  in {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(tyyppi = Koodistokoodiviite(koodiarvo = "tuntematon", koodistoUri = "suorituksentyyppi"))
            putTutkinnonOsaSuoritus(suoritus) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*101053, 101054, 101055, 101056.*".r))
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
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(suoritus)(
              verifyResponseStatus(200)))
          }

          "Kun tutkintoa ei löydy" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("123456", "koulutus"), Some("40/011/2001")), johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(suoritus)(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(""".*"message":"Koodia koulutus/123456 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
          }

          "Kun osa ei kuulu annetun tutkinnon rakenteeseen" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanPerustutkinto, johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 200 (ei validoida rakennetta tässä)" in (putTutkinnonOsaSuoritus(suoritus)(
              verifyResponseStatus(200)))
          }

          "Kun tutkinnolla ei ole diaarinumeroa" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = None), johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 200 (diaarinumeroa ei vaadita)" in (putTutkinnonOsaSuoritus(suoritus)(
                verifyResponseStatus(200)))
          }

          "Kun tutkinnon diaarinumero on virheellinen" - {
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(osanSuoritusToisestaTutkinnosta(
              autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = Some("Boom boom kah")),
              johtaminenJaHenkilöstönKehittäminen))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla Boom boom kah"))))
          }
        }

        "Suorituksen tila" - {
          def copySuoritus(t: Koodistokoodiviite, a: Option[List[AmmatillinenArviointi]], v: Option[HenkilövahvistusValinnaisellaTittelillä], ap: Option[LocalDate] = None): AmmatillisenTutkinnonOsanSuoritus = {
            val alkamispäivä = ap.orElse(tutkinnonOsaSuoritus.alkamispäivä)
            tutkinnonOsaSuoritus.copy(tila = t, arviointi = a, vahvistus = v, alkamispäivä = alkamispäivä)
          }

          def put(suoritus: AmmatillisenTutkinnonOsanSuoritus)(f: => Unit) = {
            putTutkinnonOsaSuoritus(suoritus)(f)
          }


          def testKesken(tila: Koodistokoodiviite): Unit = {
            "Arviointi puuttuu" - {
              "palautetaan HTTP 200" in (put(copySuoritus(tila, None, None)) (
                verifyResponseStatus(200)
              ))
            }
            "Arviointi annettu" - {
              "palautetaan HTTP 200" in (put(copySuoritus(tila, arviointiHyvä(), None)) (
                verifyResponseStatus(200)
              ))
            }
            "Vahvistus annettu" - {
              "palautetaan HTTP 400" in (put(copySuoritus(tila, arviointiHyvä(), vahvistusValinnaisellaTittelillä(LocalDate.parse("2016-08-08")))) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusVäärässäTilassa("Suorituksella tutkinnonosat/100023 on vahvistus, vaikka suorituksen tila on " + tila.koodiarvo))
              ))
            }
          }
          "Kun suorituksen tila on KESKEN" - {
            testKesken(tilaKesken)
          }

          "Kun suorituksen tila on KESKEYTYNYT" - {
            testKesken(tilaKesken)
          }

          "Kun suorituksen tila on VALMIS" - {
            "Suorituksella arviointi ja vahvistus" - {
              "palautetaan HTTP 200" in (put(copySuoritus(tilaValmis, arviointiHyvä(), vahvistusValinnaisellaTittelillä(LocalDate.parse("2016-08-08")))) (
                verifyResponseStatus(200)
              ))
            }
            "Vahvistus annettu, mutta arviointi puuttuu" - {
              "palautetaan HTTP 400" in (put(copySuoritus(tilaValmis, None, vahvistusValinnaisellaTittelillä(LocalDate.parse("2016-08-08")))) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.arviointiPuuttuu("Suoritukselta tutkinnonosat/100023 puuttuu arviointi, vaikka suorituksen tila on VALMIS"))
              ))
            }

            "Vahvistus puuttuu" - {
              "palautetaan HTTP 400" in (put(copySuoritus(tilaValmis, arviointiHyvä(), None)) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta tutkinnonosat/100023 puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
              ))
            }
            "Vahvistuksen myöntäjähenkilö puuttuu" - {
              "palautetaan HTTP 400" in (put(copySuoritus(tilaValmis, arviointiHyvä(), Some(HenkilövahvistusValinnaisellaTittelilläJaPaikkakunnalla(LocalDate.parse("2016-08-08"), helsinki, stadinOpisto, Nil)))) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*lessThanMinimumNumberOfItems.*".r))
              ))
            }

          }

          "Arviointi" - {
            "Arviointiasteikko on tuntematon" - {
              "palautetaan HTTP 400" in (put(copySuoritus(tutkinnonOsaSuoritus.tila, Some(List(AmmatillinenArviointi(Koodistokoodiviite("2", "vääräasteikko"), date(2015, 5, 1)))), None))
                (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*arviointiasteikkoammatillinenhyvaksyttyhylatty.*enumValueMismatch.*".r))))
            }

            "Arvosana ei kuulu perusteiden mukaiseen arviointiasteikkoon" - {
              "palautetaan HTTP 400" in (put(copySuoritus(tutkinnonOsaSuoritus.tila, Some(List(AmmatillinenArviointi(Koodistokoodiviite("x", "arviointiasteikkoammatillinent1k3"), date(2015, 5, 1)))), None))
                (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(""".*"message":"Koodia arviointiasteikkoammatillinent1k3/x ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
            }
          }

          "Suorituksen päivämäärät" - {
            def päivämäärillä(alkamispäivä: String, arviointipäivä: String, vahvistuspäivä: String) = {
              copySuoritus(tilaValmis, arviointiHyvä(LocalDate.parse(arviointipäivä)), vahvistusValinnaisellaTittelillä(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)))
            }

            "Päivämäärät kunnossa" - {
              "palautetaan HTTP 200"  in (put(päivämäärillä("2015-08-01", "2016-05-30", "2016-06-01"))(
                verifyResponseStatus(200)))
            }

            "Päivämäärät tulevaisuudessa" - {
              "palautetaan HTTP 200"  in (put(päivämäärillä("2115-08-01", "2116-05-30", "2116-06-01"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.arviointipäiväTulevaisuudessa("Päivämäärä suoritus.arviointi.päivä (2116-05-30) on tulevaisuudessa"),
                                          KoskiErrorCategory.badRequest.validation.date.vahvistuspäiväTulevaisuudessa("Päivämäärä suoritus.vahvistus.päivä (2116-06-01) on tulevaisuudessa")
                )))
            }

            "alkamispäivä > arviointi.päivä" - {
              "palautetaan HTTP 400"  in (put(päivämäärillä("2016-08-01", "2015-05-31", "2015-05-31"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.arviointiEnnenAlkamispäivää("suoritus.alkamispäivä (2016-08-01) oltava sama tai aiempi kuin suoritus.arviointi.päivä(2015-05-31)"))))
            }

            "arviointi.päivä > vahvistus.päivä" - {
              "palautetaan HTTP 400"  in (put(päivämäärillä("2015-08-01", "2016-05-31", "2016-05-30"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenArviointia("suoritus.arviointi.päivä (2016-05-31) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2016-05-30)"))))
            }
          }

          "Kun tutkinto on VALMIS-tilassa ja sillä on osa, joka on KESKEN-tilassa" - {
            val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(
              suoritustapa = tutkinnonSuoritustapaNäyttönä,
              tila = tilaValmis,
              vahvistus = vahvistus(LocalDate.parse("2016-10-08")),
              osasuoritukset = Some(List(tutkinnonOsaSuoritus))
            )))

            "palautetaan HTTP 400" in (putOpiskeluoikeus(opiskeluoikeus) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Suorituksella koulutus/351301 on keskeneräinen osasuoritus tutkinnonosat/100023 vaikka suorituksen tila on VALMIS"))))
          }
        }
      }
    }

    "Tutkinnon tila ja arviointi" - {
      def copySuoritus(t: Koodistokoodiviite, ap: Option[LocalDate] = None) = {
        val alkamispäivä = ap.orElse(tutkinnonOsaSuoritus.alkamispäivä)
        osittainenSuoritusKesken.copy(tila = t, alkamispäivä = alkamispäivä)
      }

      def put(s: AmmatillisenTutkinnonOsittainenSuoritus)(f: => Unit) = {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(s)))(f)
      }

      def testKesken(tila: Koodistokoodiviite): Unit = {
        "palautetaan HTTP 200" in (put(copySuoritus(tila, None)) (
          verifyResponseStatus(200)
        ))
      }

      "Kun suorituksen tila on KESKEN" - {
        testKesken(tilaKesken)
      }

      "Kun suorituksen tila on KESKEYTYNYT" - {
        testKesken(tilaKeskeytynyt)
      }

      "Kun suorituksen tila on VALMIS" - {
        "palautetaan HTTP 200" in (put(copySuoritus(tilaValmis)) (
          verifyResponseStatus(200)
        ))
      }

      "Suorituksen päivämäärät" - {
        def päivämäärillä(alkamispäivä: String, vahvistuspäivä: String) = {
          copySuoritus(tilaValmis, Some(LocalDate.parse(alkamispäivä)))
        }

        "Päivämäärät kunnossa" - {
          "palautetaan HTTP 200"  in (put(päivämäärillä("2015-08-01", "2016-06-01"))(
            verifyResponseStatus(200)))
        }
      }
    }
  }

  private def vahvistus(date: LocalDate) = {
    Some(HenkilövahvistusPaikkakunnalla(date, helsinki, stadinOpisto, List(Organisaatiohenkilö("Teppo Testaaja", "rehtori", stadinOpisto))))
  }


  private def vahvistusValinnaisellaTittelillä(date: LocalDate) = {
    Some(HenkilövahvistusValinnaisellaTittelilläJaPaikkakunnalla(date, helsinki, stadinOpisto, List(OrganisaatiohenkilöValinnaisellaTittelillä("Teppo Testaaja", Some("rehtori"), stadinOpisto))))
  }

  private def arviointiHyvä(päivä: LocalDate = date(2015, 1, 1)): Some[List[AmmatillinenArviointi]] = Some(List(AmmatillinenArviointi(Koodistokoodiviite("2", "arviointiasteikkoammatillinent1k3"), päivä)))

  private lazy val stadinOpisto: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.stadinAmmattiopisto)

  private lazy val laajuus = LaajuusOsaamispisteissä(11)

  private lazy val tutkinnonOsa: MuuValtakunnallinenTutkinnonOsa = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100023", "tutkinnonosat"), true, Some(laajuus))

  private lazy val tutkinnonSuoritustapaNäyttönä = Some(Koodistokoodiviite("naytto", "ammatillisentutkinnonsuoritustapa"))

  private lazy val tutkinnonOsaSuoritus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = tutkinnonOsa,
    tila = tilaKesken,
    toimipiste = Some(OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
    arviointi = arviointiHyvä()
  )

  private lazy val paikallinenTutkinnonOsa = PaikallinenTutkinnonOsa(
    PaikallinenKoodi("1", "paikallinen osa"), "Paikallinen tutkinnon osa", false, Some(laajuus)
  )

  private lazy val paikallinenTutkinnonOsaSuoritus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = paikallinenTutkinnonOsa,
    tila = tilaKesken,
    toimipiste = Some(OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
    arviointi = arviointiHyvä()
  )

  private def putTutkinnonOsaSuoritus[A](tutkinnonOsaSuoritus: AmmatillisenTutkinnonOsanSuoritus)(f: => A) = {
    val s = osittainenSuoritusKesken.copy(osasuoritukset = Some(List(tutkinnonOsaSuoritus)))

    putTutkintoSuoritus(s)(f)
  }

  private def putTutkintoSuoritus[A](suoritus: AmmatillisenTutkinnonOsittainenSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))

    putOppija(makeOppija(henkilö, List(Json.toJValue(opiskeluoikeus))), headers)(f)
  }

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(osittainenSuoritusKesken.copy(koulutusmoduuli = osittainenSuoritusKesken.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  override def vääräntyyppisenPerusteenDiaarinumero: String = "60/011/2015"
  def eperusteistaLöytymätönValidiDiaarinumero: String = "13/011/2009"
}
