package fi.oph.koski.api

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.AmmatillinenOldExamples.muunAmmatillisenTutkinnonOsanSuoritus
import fi.oph.koski.documentation.AmmatillinenReforminMukainenPerustutkintoExample.{jatkoOpintovalmiuksiaTukevienOpintojenSuoritus, korkeakouluopintoSuoritus}
import fi.oph.koski.documentation.ExampleData.{helsinki, _}
import fi.oph.koski.documentation.{AmmatillinenExampleData, AmmattitutkintoExample, ExampleData, ExamplesValma}
import fi.oph.koski.fixture.AmmatillinenOpiskeluoikeusTestData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationAmmatillinenSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  "Ammatillisen koulutuksen opiskeluoikeuden lisääminen" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatusOk()
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
            osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1527", "osaamisala")))))

          "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
        }
        "Suoritustapa virheellinen" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("blahblahtest", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1527", "osaamisala")))))

          "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia ammatillisentutkinnonsuoritustapa/blahblahtest ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
        }
        "Osaamisala ei löydy tutkintorakenteesta" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("3053", "osaamisala")))))

          "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala 3053 ei löydy tutkintorakenteesta perusteelle 39/011/2014"))))
        }
        "Osaamisala virheellinen" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("0", "osaamisala")))))

          "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia osaamisala/0 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
        }
      }

      "Tutkinnon osat ja arvionnit" - {
        val johtaminenJaHenkilöstönKehittäminen = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None)

        "Valtakunnallinen tutkinnonosa" - {
          "Tutkinnon osa ja arviointi ok" - {
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, tutkinnonSuoritustapaOps) (verifyResponseStatusOk()))
          }

          "Tutkinnon osa ei kuulu tutkintorakenteeseen" - {
            "Pakolliset ja ammatilliset tutkinnon osat" - {
              "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa tutkinnonosat/104052 ei löydy tutkintorakenteesta perusteelle 39/011/2014 - suoritustapa naytto"))))
            }
            "Vapaavalintaiset tutkinnon osat" - {
              "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
                  koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen, tutkinnonOsanRyhmä = vapaavalintaisetTutkinnonOsat
                ), tutkinnonSuoritustapaOps)(
                verifyResponseStatusOk()))
            }
          }

          "Tutkinnon osaa ei ei löydy koodistosta" - {
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
              koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("9923123", "tutkinnonosat"), true, None)), tutkinnonSuoritustapaNäyttönä)
              (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia tutkinnonosat/9923123 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
          }

          "Sama pakollinen tutkinnon osa kahteen kertaan" - {
            val suoritus = autoalanPerustutkinnonSuoritus().copy(
              osasuoritukset = Some(List(
                tutkinnonOsaSuoritus, tutkinnonOsaSuoritus
              ))
            )

            "palautetaan HTTP 200" in putTutkintoSuoritus(suoritus)(verifyResponseStatusOk())
          }

          "Sama valinnainen tutkinnon osa kahteen kertaan" - {
            val valinnainenTutkinnonosa = tutkinnonOsaSuoritus.copy(koulutusmoduuli = tutkinnonOsa.copy(pakollinen = false))
            val suoritus = autoalanPerustutkinnonSuoritus().copy(
              osasuoritukset = Some(List(
                valinnainenTutkinnonosa, valinnainenTutkinnonosa
              ))
            )
            "palautetaan HTTP 200" in putTutkintoSuoritus(suoritus)(verifyResponseStatusOk())
          }



          "Tutkinnon osan osat" - {
            "Sama osa kahteen kertaan" - {
              "Palautetaan HTTP 200" in (
                putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(osasuoritukset = Some(List(
                  osanOsa, osanOsa
                ))), tutkinnonSuoritustapaOps) (verifyResponseStatusOk())
              )
            }
          }

          "Yhteiset tutkinnon osat" - {
            "Osan laajuus ei vastaa osan osien yhteislaajuutta" - {
              "Palautetaan HTTP 400" in (
              putTutkinnonOsaSuoritus(yhtTutkinnonOsanSuoritus, tutkinnonSuoritustapaOps) (
                verifyResponseStatus(400,
                  KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Yhteisillä tutkinnon osilla 'Viestintä- ja vuorovaikutusosaaminen' on eri laajuus kun tutkinnon osien osa-alueiden yhteenlaskettu summa")))
                )
            }

            "Osa-alueella ei osasuorituksia, suoritustapa reformi" - {
              val yhtSuoritus = yhteisenTutkinnonOsanSuoritus("400012", "Viestintä- ja vuorovaikutusosaaminen", k3, 35).copy(
                osasuoritukset = Some(List())
              )
              val reformiSuoritus = puuteollisuudenPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaReformi,
                osasuoritukset = Some(List(yhtSuoritus)))
              val suoritus = reformiSuoritus.copy(
                osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018, 1, 1), None, osaamisenHankkimistapaOppilaitos))),
                vahvistus = vahvistus(date(2018, 1, 1)),
                keskiarvo = Some(4.0)
              )

              "Palautetaan HTTP 400" in (
                putTutkintoSuoritus(suoritus)(
                  verifyResponseStatus(400, HttpStatus.fold(KoskiErrorCategory.badRequest.validation.rakenne.yhteiselläOsuudellaEiOsasuorituksia("Arvioidulla yhteisellä tutkinnon osalla 'Viestintä- ja vuorovaikutusosaaminen' ei ole osa-alueita"),
                  )))
                )
            }

            "Osa-alueella ei osasuorituksia, suoritustapa ops" - {
              "Palautetaan HTTP 200" in (
                putTutkinnonOsaSuoritus(yhtTutkinnonOsanSuoritus.copy(osasuoritukset = Some(List())), tutkinnonSuoritustapaOps) (
                  verifyResponseStatusOk()
                ))
            }

            "Osa-alueiden yhteenlaskettu laajuus" - {
              "On alle 35" - {
                val yhtSuoritus = yhteisenTutkinnonOsanSuoritus("400012", "Viestintä- ja vuorovaikutusosaaminen", k3, 8).copy(
                  osasuoritukset = Some(List(
                    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(5))), arviointi = Some(List(arviointiKiitettävä))),
                    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = false, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))),
                  ))
                )
                val reformiSuoritus = puuteollisuudenPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaReformi,
                  osasuoritukset = Some(List(yhtSuoritus)))
                val suoritus = reformiSuoritus.copy(
                  osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018, 1, 1), None, osaamisenHankkimistapaOppilaitos))),
                  vahvistus = vahvistus(date(2018, 1, 1)),
                  keskiarvo = Some(4.0)
                )
                "Palautetaan HTTP 400" in (
                  putTutkintoSuoritus(suoritus)(
                    verifyResponseStatus(400, HttpStatus.fold(KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Valmiiksi merkityn suorituksen koulutus/351741 yhteisten tutkinnon osien laajuuden tulee olla vähintään 35"))))
                  )
              }
              "On 35" - {
                val yhtSuoritukset = List(
                  yhteisenTutkinnonOsanSuoritus("400012", "Viestintä- ja vuorovaikutusosaaminen", k3, 5).copy(
                    osasuoritukset = Some(List(
                      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(5))), arviointi = Some(List(arviointiKiitettävä))),
                    ))
                  ),
                  yhteisenTutkinnonOsanSuoritus("400013", "Matemaattis-luonnontieteellinen osaaminen", k3, 30).copy(
                    osasuoritukset = Some(List(
                      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = PaikallinenAmmatillisenTutkinnonOsanOsaAlue(PaikallinenKoodi("MA", "Matematiikka"), "Matematiikan opinnot", pakollinen = true, Some(LaajuusOsaamispisteissä(30))), arviointi = Some(List(arviointiKiitettävä))),
                    ))
                  ))
                val reformiSuoritus = puuteollisuudenPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaReformi,
                  osasuoritukset = Some(yhtSuoritukset))
                val suoritus = reformiSuoritus.copy(
                  osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018, 1, 1), None, osaamisenHankkimistapaOppilaitos))),
                  vahvistus = vahvistus(date(2018, 1, 1)),
                  keskiarvo = Some(4.0)
                )
                "Palautetaan HTTP 200" in (
                  putTutkintoSuoritus(suoritus)(verifyResponseStatusOk())
                  )
              }
            }

            "Samoja yhteisiä osuuksia" - {
              val yhtOsanSuoritus = yhtTutkinnonOsanSuoritus.copy(koulutusmoduuli = yhtTutkinnonOsanSuoritus.koulutusmoduuli.copy(laajuus = Some(LaajuusOsaamispisteissä(13.0))))
              val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaOps,
                osasuoritukset = Some(List(yhtOsanSuoritus, yhtOsanSuoritus)),
                vahvistus = vahvistus(date(2018,1,1)),
                keskiarvo = Some(4.0)
              )
              "Palautetaan HTTP 400" in (
                putTutkintoSuoritus(suoritus) (
                  verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Suorituksella koulutus/351301 on useampi yhteinen osasuoritus samalla koodilla")))
                )
            }

            "Reformi-muotoisella tutkinnolla väärän koodin yhteisiä osuuksia" - {
              val yhtSuoritukset = List(
                yhteisenTutkinnonOsanSuoritus("101053", "Viestintä- ja vuorovaikutusosaaminen", k3, 35).copy(
                  osasuoritukset = Some(List(
                    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(35))), arviointi = Some(List(arviointiKiitettävä))),
                  ))
                ))
              val reformiSuoritus = virheellinenPuuteollisuudenPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaReformi,
                osasuoritukset = Some(yhtSuoritukset))
              val suoritus = reformiSuoritus.copy(
                osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018, 1, 1), None, osaamisenHankkimistapaOppilaitos))),
                vahvistus = vahvistus(date(2018, 1, 1)),
                keskiarvo = Some(4.0)
              )
              "Palautetaan HTTP 400" in (
                putTutkintoSuoritus(suoritus) (
                  verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääränKoodinYhteinenOsasuoritus("Suorituksella koulutus/351741 on Ops-muotoiselle tutkinnolle tarkoitettu yhteinen osasuoritus")))
                )
            }
          }
        }

        "Paikallinen tutkinnonosa" - {
          "Tutkinnon osa ja arviointi ok" - {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat)
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps) (verifyResponseStatusOk()))
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

        "Tutkinnon osa saman tutkinnon uudesta perusteesta" - {
          "Kun tutkinto löytyy ja osa kuuluu sen rakenteeseen" - {
            val autoalanPerustutkintoUusiPeruste = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", "koulutus"), Some("OPH-2762-2017"))
            val tutkinnonOsaUudestaPerusteesta = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("400010", "tutkinnonosat"), pakollinen = true, None)
            val suoritus = tutkinnonOsaSuoritus.copy(
              tutkinto = Some(autoalanPerustutkintoUusiPeruste),
              koulutusmoduuli = tutkinnonOsaUudestaPerusteesta
            )
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps)(
              verifyResponseStatusOk()))
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
            "palautetaan HTTP 200" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps)(
              verifyResponseStatusOk()))
          }

          "Kun tutkintoa ei löydy" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("123456", "koulutus"), Some("40/011/2001")), johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia koulutus/123456 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
          }

          "Kun osa ei kuulu annetun tutkinnon rakenteeseen" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(parturikampaaja, johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 200 (ei validoida rakennetta tässä)" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps)(
              verifyResponseStatusOk()))
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

          "Kun tutkinnon diaarinumero on muodoltaan virheellinen" - {
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(osanSuoritusToisestaTutkinnosta(
              autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = Some("Lorem ipsum dolor sit amet, consectetur adipiscing elit")),
              johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Diaarinumeron muoto on virheellinen: Lorem ipsum dolor sit amet, co"))))
          }

          "Kun tutkinnon osalle ilmoitetaan tutkintotieto, joka on sama kuin päätason tutkinto" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanPerustutkinto, johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 400" in (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.samaTutkintokoodi("Tutkinnon osalle tutkinnonosat/104052 on merkitty tutkinto, jossa on sama diaarinumero 39/011/2014 kuin tutkinnon suorituksessa"))))
          }
        }

        "Tunnisteen koodiarvon validointi" - {

          "Tunnisteen koodiarvo ei löydy rakenteen koulutuksista" - {
            val suoritus =  autoalanPerustutkinnonSuoritus().copy(koulutusmoduuli = autoalanPerustutkinto.copy(tunniste = autoalanPerustutkinto.tunniste.copy(koodiarvo = "361902")))
            "palautetaan HTTP 400" in (putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))))(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tunnisteenKoodiarvoaEiLöydyRakenteesta("Tunnisteen koodiarvoa 361902 ei löytynyt rakenteen 39/011/2014 mahdollisista koulutuksista. Tarkista tutkintokoodit ePerusteista."))
            )
          }

          "Löydetyssä rakenteessa ei ole yhtään koulutusta"  - {
            val suoritus =  autoalanPerustutkinnonSuoritus().copy(koulutusmoduuli = autoalanPerustutkinto.copy(perusteenDiaarinumero = Some("mock-empty-koulutukset")))
            "palautetaan HTTP 200" in (putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus)), defaultHenkilö.copy(hetu = "120950-0351")))(
              verifyResponseStatusOk()
            )
          }
        }

        "Suorituksen tila" - {
          def copySuoritus(a: Option[List[AmmatillinenArviointi]], v: Option[HenkilövahvistusValinnaisellaTittelillä], ap: Option[LocalDate] = None): MuunAmmatillisenTutkinnonOsanSuoritus = {
            val alkamispäivä = ap.orElse(tutkinnonOsaSuoritus.alkamispäivä)
            tutkinnonOsaSuoritus.copy(arviointi = a, vahvistus = v, alkamispäivä = alkamispäivä)
          }

          def tilanHenkilö = defaultHenkilö.copy(hetu = "240252-7302")

          def put(suoritus: AmmatillisenTutkinnonOsanSuoritus)(f: => Unit) = putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä, tilanHenkilö)(f)
          def putOsasuoritukset(suoritukset: List[AmmatillisenTutkinnonOsanSuoritus])(f: => Unit) = putTutkinnonOsaSuoritukset(suoritukset, tutkinnonSuoritustapaNäyttönä)(f)

          "Arviointi ja vahvistus puuttuu" - {
            "palautetaan HTTP 200" in (put(copySuoritus(None, None)) (
              verifyResponseStatusOk()
            ))
          }

          "Arviointi annettu" - {
            "palautetaan HTTP 200" in (put(copySuoritus(arviointiHyvä(), None)) (
              verifyResponseStatusOk()
            ))
          }

          "Suorituksella arviointi ja vahvistus" - {
            "palautetaan HTTP 200" in (put(copySuoritus(arviointiHyvä(), vahvistusValinnaisellaTittelillä(LocalDate.parse("2016-08-08")))) (
              verifyResponseStatusOk()
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

            "Useita arviointiasteikoita käytetty" - {
              "palautetaan HTTP 400" in (putOsasuoritukset(List(copySuoritus(arviointiHyvä(), None), copySuoritus(arviointiHyvä(arvosana = arvosanaViisi), None))) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.useitaArviointiasteikoita("Suoritus käyttää useampaa kuin yhtä numeerista arviointiasteikkoa: arviointiasteikkoammatillinen15, arviointiasteikkoammatillinent1k3"))
              ))
            }

            "Useita arviointiasteikoita käytetty, näyttö" - {
              val näytöllinenSuoritus = copySuoritus(arviointiHyvä(arvosana = arvosanaViisi), None).copy(näyttö = Some(näyttö(date(2016, 2, 1), "Näyttö", "Näyttöpaikka, Näyttölä", Some(näytönArviointi))))
              "palautetaan HTTP 400" in (putOsasuoritukset(List(copySuoritus(arviointiHyvä(), None), näytöllinenSuoritus)) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.useitaArviointiasteikoita("Suoritus käyttää useampaa kuin yhtä numeerista arviointiasteikkoa: arviointiasteikkoammatillinen15, arviointiasteikkoammatillinent1k3"))
              ))
            }

            "Useita arviointiasteikoita käytetty, näytön arvioinnin arviointikohteet" - {
              val arviointikohteet = näytönArviointi.arviointikohteet.toList.flatten
              val arviointi = näytönArviointi.copy(arviointikohteet = Some(arviointikohteet.head.copy(arvosana = arvosanaViisi) :: arviointikohteet.tail))
              val näytöllinenSuoritus = copySuoritus(arviointiHyvä(), None).copy(näyttö = Some(näyttö(date(2016, 2, 1), "Näyttö", "Näyttöpaikka, Näyttölä", Some(arviointi))))
              "palautetaan HTTP 400" in (putOsasuoritukset(List(copySuoritus(arviointiHyvä(), None), näytöllinenSuoritus)) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.useitaArviointiasteikoita("Suoritus käyttää useampaa kuin yhtä numeerista arviointiasteikkoa: arviointiasteikkoammatillinen15, arviointiasteikkoammatillinent1k3"))
              ))
            }

            "Useita arviointiasteikoita käytetty, tutkinnon osien osat" - {
              val suoritusOsienOsat = tutkinnonOsaSuoritus.copy(osasuoritukset = Some(List(osanOsa.copy(arviointi = arviointiHyvä()))))
              "palautetaan HTTP 400" in (putOsasuoritukset(List(copySuoritus(arviointiHyvä(arvosana = arvosanaViisi), None), suoritusOsienOsat)) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.useitaArviointiasteikoita("Suoritus käyttää useampaa kuin yhtä numeerista arviointiasteikkoa: arviointiasteikkoammatillinen15, arviointiasteikkoammatillinent1k3"))
              ))
            }
          }

          "Suorituksen päivämäärät" - {
            def päivämäärillä(alkamispäivä: String, arviointipäivä: String, vahvistuspäivä: String) = {
              copySuoritus(arviointiHyvä(LocalDate.parse(arviointipäivä)), vahvistusValinnaisellaTittelillä(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)))
            }

            "Päivämäärät kunnossa" - {
              "palautetaan HTTP 200"  in (put(päivämäärillä("2015-08-01", "2016-05-30", "2016-06-01"))(
                verifyResponseStatusOk()))
            }

            "Päivämäärät tulevaisuudessa" - {
              "palautetaan HTTP 200"  in (put(päivämäärillä("2115-08-01", "2116-05-30", "2116-06-01"))(
                verifyResponseStatusOk()))
            }

            "alkamispäivä > arviointi.päivä" - {
              "palautetaan HTTP 400"  in (put(päivämäärillä("2016-08-01", "2015-05-31", "2015-05-31"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.arviointiEnnenAlkamispäivää("suoritus.alkamispäivä (2016-08-01) oltava sama tai aiempi kuin suoritus.arviointi.päivä (2015-05-31)"))))
            }

            "arviointi.päivä > vahvistus.päivä" - {
              "palautetaan HTTP 400"  in (put(päivämäärillä("2015-08-01", "2016-05-31", "2016-05-30"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenArviointia("suoritus.arviointi.päivä (2016-05-31) oltava sama tai aiempi kuin suoritus.vahvistus.päivä (2016-05-30)"))))
            }

            "osasuoritus.vahvistus.päivä > suoritus.vahvistus.päivä" - {
              "palautetaan HTTP 400"  in {
                val suoritus: AmmatillisenTutkinnonSuoritus = withTutkinnonOsaSuoritus(päivämäärillä("2015-08-01", "2017-05-30", vahvistuspäivä = "2017-06-01"), tutkinnonSuoritustapaNäyttönä)
                putTutkintoSuoritus(suoritus.copy(vahvistus = vahvistus(date(2017, 5, 31)))) {
                  verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.suorituksenVahvistusEnnenSuorituksenOsanVahvistusta("osasuoritus.vahvistus.päivä (2017-06-01) oltava sama tai aiempi kuin suoritus.vahvistus.päivä (2017-05-31)"))
                }
              }
            }
          }

          "Kun tutkinnolla on vahvistus" - {
            val suoritus = autoalanPerustutkinnonSuoritus().copy(
              suoritustapa = tutkinnonSuoritustapaNäyttönä,
              vahvistus = vahvistus(LocalDate.parse("2016-10-08")),
              osasuoritukset = Some(List(tutkinnonOsaSuoritus.copy(arviointi = None)))
            )
            val eiOsasuorituksia = suoritus.copy(osasuoritukset = Some(List()))
            val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))
            val tyhjilläOsasuorituksilla = opiskeluoikeus.copy(ostettu = false, suoritukset = List(eiOsasuorituksia))

            "ja tutkinnon osalta puuttuu arviointi, palautetaan HTTP 400" in (putOpiskeluoikeus(opiskeluoikeus) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella koulutus/351301 on keskeneräinen osasuoritus tutkinnonosat/100023"))))

            "tutkinnolla ei osasuorituksia, palautetaan HTTP 400" in (putOpiskeluoikeus(tyhjilläOsasuorituksilla)(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia("Suoritus koulutus/351301 on merkitty valmiiksi, mutta sillä ei ole ammatillisen tutkinnon osan suoritusta tai opiskeluoikeudelta puuttuu linkitys"))))

            "Opiskeluoikeus ostettu" - {

              "Opiskeluoikeus valmis ennen vuotta 2019" - {
                val valmisTila = AmmatillinenOpiskeluoikeusjakso(date(2018, 12, 31), ExampleData.opiskeluoikeusValmistunut, Some(valtionosuusRahoitteinen))
                val valmisOpiskeluoikeus = tyhjilläOsasuorituksilla.copy(tila = AmmatillinenOpiskeluoikeudenTila(tyhjilläOsasuorituksilla.tila.opiskeluoikeusjaksot :+ valmisTila), ostettu = true)
                "palautetaan HTTP 200" in (putOpiskeluoikeus(valmisOpiskeluoikeus, defaultHenkilö.copy(hetu = "150435-0429"))(
                  verifyResponseStatusOk()))
              }

              "Opiskeluoikeus valmis vuoden 2018 jälkeen" - {
                val valmisTila = AmmatillinenOpiskeluoikeusjakso(date(2019, 1, 1), ExampleData.opiskeluoikeusValmistunut, Some(valtionosuusRahoitteinen))
                val valmisOpiskeluoikeus = opiskeluoikeus.copy(tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeus.tila.opiskeluoikeusjaksot :+ valmisTila))
                "palautetaan HTTP 400" in (putOpiskeluoikeus(valmisOpiskeluoikeus.copy(suoritukset = List(eiOsasuorituksia), ostettu = true))(
                  verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia("Suoritus koulutus/351301 on merkitty valmiiksi, mutta sillä ei ole ammatillisen tutkinnon osan suoritusta tai opiskeluoikeudelta puuttuu linkitys"))))
              }
            }
          }

          "Kun suorituksen tila 'vahvistettu', opiskeluoikeuden tila ei voi olla 'eronnut' tai 'katsotaan eronneeksi'" in {
            val opiskeluoikeus = defaultOpiskeluoikeus.copy(
              tila = AmmatillinenOpiskeluoikeudenTila(List(
                AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2016, 1, 1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
                AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2017, 1, 1), opiskeluoikeusEronnut)
              )),
              suoritukset = List(autoalanPerustutkinnonSuoritus().copy(
                vahvistus = vahvistus(date(2017, 1, 1)),
                keskiarvo = Some(4.0),
                osasuoritukset = Some(List(muunAmmatillisenTutkinnonOsanSuoritus))
              )))
            putOpiskeluoikeus(opiskeluoikeus) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus())
            }
          }
        }
      }

      "Useampi päätason suoritus" - {
        "Ei sallita kahta päätason suoritusta tyyppiä 'ammatillinentutkinto'" in {
          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            suoritukset = List(autoalanPerustutkinnonSuoritus(), autoalanErikoisammattitutkinnonSuoritus())
          )

          putOpiskeluoikeus(opiskeluoikeus, defaultHenkilö.copy(hetu = "160337-625E")) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.useampiPäätasonSuoritus())
          }
        }

        "Sallitaan kaksi päätason suoritusta, kun yhdistelmänä 'ammatillinentutkinto', jossa suoritustapa näyttö, ja 'nayttotutkintoonvalmistavakoulutus'" in {
          val opiskeluoikeus = ammatillinenOpiskeluoikeusNäyttötutkinnonJaNäyttöönValmistavanSuorituksilla()

          putOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatusOk()
          }
        }
      }

      "Tutkintokoodin ja suoritustavan vaihtaminen" - {
        "Tutkintokoodia ei voi vaihtaa opiskeluoikeuden luonnin jälkeen" in {
          val opiskelija = defaultHenkilö.copy(hetu = "270550-879P")

          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            suoritukset = List(autoalanPerustutkinnonSuoritus().copy(
              koulutusmoduuli = autoalanPerustutkinto.copy(
                tunniste = Koodistokoodiviite("351301", "koulutus")
              )
            ))
          )
          val tallennettuna = putAndGetOpiskeluoikeus(opiskeluoikeus, opiskelija).withSuoritukset(
            List(autoalanPerustutkinnonSuoritus().copy(
              koulutusmoduuli = autoalanPerustutkinto.copy(
                tunniste = Koodistokoodiviite("357305", "koulutus")
              )
            ))
          )

          putOpiskeluoikeus(tallennettuna, opiskelija) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.muutettuSuoritustapaaTaiTutkintokoodia())
          }
        }

        "Suoritustapaa ei voi vaihtaa opiskeluoikeuden luonnin jälkeen" in {
          val opiskelija = defaultHenkilö.copy(hetu = "170794-450C")

          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            suoritukset = List(autoalanPerustutkinnonSuoritus().copy(
              suoritustapa = suoritustapaNäyttö
            ))
          )
          val tallennettuna = putAndGetOpiskeluoikeus(opiskeluoikeus, henkilö = opiskelija).withSuoritukset(
            List(autoalanPerustutkinnonSuoritus().copy(
              suoritustapa = suoritustapaOps
            ))
          )

          putOpiskeluoikeus(tallennettuna, henkilö = opiskelija) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.muutettuSuoritustapaaTaiTutkintokoodia())
          }
        }

        "'nayttotutkintoonvalmistavakoulutus'-tyypin koulutukselle voidaan lisätä kaveriksi 'ammatillinentutkinto' ja tätä ei lasketa suoritustavan/tutkintokoodin muuttamiseksi" in {
          val opiskelija = defaultHenkilö.copy(hetu = "010914-406L")

          val näyttötutkinnonSuoritus = AmmatillisenTutkinnonSuoritus(
            koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
            suoritustapa = suoritustapaNäyttö,
            suorituskieli = suomenKieli,
            toimipiste = stadinToimipiste,
          )
          val näyttötutkintoonValmistavaSuoritus = AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(alkamispäivä = Some(date(2015, 1, 1)), vahvistus = None)


          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            suoritukset = List(näyttötutkintoonValmistavaSuoritus)
          )
          val tallennettuna = putAndGetOpiskeluoikeus(opiskeluoikeus, henkilö = opiskelija).withSuoritukset(
            List(näyttötutkinnonSuoritus, näyttötutkintoonValmistavaSuoritus)
          )

          putOpiskeluoikeus(tallennettuna, henkilö = opiskelija) {
            verifyResponseStatusOk()
          }
        }
      }

      "Vanhentunut tutkinnon rakenne" - {
        "Sallitaan siirto, jos eperusteissa rakenteen siirtymäaika on päättynyt kun validaatio on voimassa" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 331101, diaariNumero = "59/011/2014")
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))
          val config = KoskiApplicationForTests.config.withValue("validaatiot.ammatillisenPerusteidenVoimassaoloTarkastusAstuuVoimaan", fromAnyRef(LocalDate.now().toString))
          mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija).isRight should equal (true)
        }
        "Sallitaan siirto, jos eperusteissa rakenteen voimassaolo on päättynyt ja siirtymäaikaa ei ole määritelty kun validaatio on voimassa" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 331101, diaariNumero = "1000/011/2014", versio = Some(11))
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))
          val config = KoskiApplicationForTests.config.withValue("validaatiot.ammatillisenPerusteidenVoimassaoloTarkastusAstuuVoimaan", fromAnyRef(LocalDate.now().toString))
          mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija).isRight should equal (true)
        }

        /*
        FIXME: Väliaikaisesti otettu pois käytöstä perusteen voimassaolon validointi
        "Ei sallita siirtoa, jos eperusteissa rakenteen siirtymäaika on päättynyt kun validaatio on voimassa" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 331101, diaariNumero = "59/011/2014")
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))
          val config = KoskiApplicationForTests.config.withValue("validaatiot.ammatillisenPerusteidenVoimassaoloTarkastusAstuuVoimaan", fromAnyRef(LocalDate.now().toString))
          mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija).left.get should equal (KoskiErrorCategory.badRequest.validation.rakenne.siirtymäaikaPäättynyt())
        }

        "Ei sallita siirtoa, jos eperusteissa rakenteen voimassaolo on päättynyt ja siirtymäaikaa ei ole määritelty kun validaatio on voimassa" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 331101, diaariNumero = "1000/011/2014")
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))
          val config = KoskiApplicationForTests.config.withValue("validaatiot.ammatillisenPerusteidenVoimassaoloTarkastusAstuuVoimaan", fromAnyRef(LocalDate.now().toString))
          mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija).left.get should equal (KoskiErrorCategory.badRequest.validation.rakenne.perusteenVoimassaoloPäättynyt())
        }*/

        "Sallitaan siirto, kun validaatio ei vielä voimassa" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 331101, diaariNumero = "59/011/2014", versio = Some(11))
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))
          val config = KoskiApplicationForTests.config.withValue("validaatiot.ammatillisenPerusteidenVoimassaoloTarkastusAstuuVoimaan", fromAnyRef(LocalDate.now().plusDays(1).toString))
          mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija).isRight should equal (true)
        }
      }
    }

    "Tutkinnon tila ja arviointi" - {
      def copySuoritus(v: Option[HenkilövahvistusValinnaisellaPaikkakunnalla], ap: Option[LocalDate] = None, keskiarvo: Option[Double] = None) = {
        val alkamispäivä = ap.orElse(tutkinnonOsaSuoritus.alkamispäivä)
        val suoritus = autoalanPerustutkinnonSuoritus().copy(vahvistus = v, alkamispäivä = alkamispäivä, keskiarvo = keskiarvo)
        v.map(_ => suoritus.copy(osasuoritukset = Some(List(muunAmmatillisenTutkinnonOsanSuoritus)))).getOrElse(suoritus)
      }

      def put(s: AmmatillisenTutkinnonSuoritus)(f: => Unit) = {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(s)))(f)
      }

      "Vahvistus puuttuu, opiskeluoikeus voimassa" - {
        "palautetaan HTTP 200" in (put(copySuoritus(None, None)) (
          verifyResponseStatusOk()
        ))
      }

      "Vahvistus puuttuu, opiskeluoikeus valmistunut" - {
        "palautetaan HTTP 400" in (putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)).copy(suoritukset = List(copySuoritus(v = None)))) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/351301 puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut"))
        ))
      }

      "Suorituksella on vahvistus" - {
        "palautetaan HTTP 200" in (put(copySuoritus(vahvistus(LocalDate.parse("2016-08-08")), keskiarvo = Some(4.0))) (
          verifyResponseStatusOk()
        ))
      }

      "Vahvistuksen myöntäjähenkilö puuttuu" - {
        "palautetaan HTTP 400" in (put(copySuoritus(Some(HenkilövahvistusValinnaisellaPaikkakunnalla(LocalDate.parse("2016-08-08"), Some(helsinki), stadinOpisto, Nil)))) (
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lessThanMinimumNumberOfItems.*".r))
        ))
      }

      "Suorituksen päivämäärät" - {
        def päivämäärillä(alkamispäivä: String, vahvistuspäivä: String) = {
          copySuoritus(vahvistus(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)), keskiarvo = Some(4.0))
        }

        "Päivämäärät kunnossa" - {
          "palautetaan HTTP 200"  in (put(päivämäärillä("2015-08-01", "2016-06-01"))(
            verifyResponseStatusOk()))
        }

        "alkamispäivä > vahvistus.päivä" - {
          "palautetaan HTTP 400"  in (put(päivämäärillä("2016-08-01", "2015-05-31"))(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenAlkamispäivää("suoritus.alkamispäivä (2016-08-01) oltava sama tai aiempi kuin suoritus.vahvistus.päivä (2015-05-31)"))))
        }
      }

      "Keskiarvon asettaminen" - {
        val keskeneräinenSuoritusKeskiarvolla = autoalanPerustutkinnonSuoritus().copy(keskiarvo = Some(4.0))
        "estetään jos suoritus on kesken" - {
          "palautetaan HTTP 400" in (putTutkintoSuoritus(keskeneräinenSuoritusKeskiarvolla)(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.keskiarvoaEiSallitaKeskeneräiselleSuoritukselle("Suoritukselle ei voi asettaa keskiarvoa ellei suoritus ole valmis"))))
        }
        val valmisSuoritusKeskiarvolla = keskeneräinenSuoritusKeskiarvolla.copy(
          vahvistus = vahvistus(date(2017, 5, 31)),
          osasuoritukset = Some(List(tutkinnonOsaSuoritus)))
        "sallitaan jos suoritus on valmis" - {
          "palautetaan HTTP 200" in (putTutkintoSuoritus(valmisSuoritusKeskiarvolla)(
            verifyResponseStatusOk()))
        }
        val valmisSuoritusIlmanKeskiarvoa = autoalanPerustutkinnonSuoritus().copy(
          vahvistus = vahvistus(date(2017, 5, 31)),
          keskiarvo = None,
          osasuoritukset = Some(List(tutkinnonOsaSuoritus)))
        "vaaditaan jos suoritus on valmis" - {
          "palautetaan HTTP 400" in (putTutkintoSuoritus(valmisSuoritusIlmanKeskiarvoa)(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.valmiillaSuorituksellaPitääOllaKeskiarvo("Suorituksella pitää olla keskiarvo kun suoritus on valmis"))))
        }
      }
    }

    "Ammatillinen perustutkinto opetussuunnitelman mukaisesti" - {
      "Tutkinnonosan ryhmä on määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, osasuoritukset = Some(List(tutkinnonOsaSuoritus)))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Tutkinnonosan ryhmää ei ole määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, osasuoritukset = Some(List(tutkinnonOsaSuoritus.copy(tutkinnonOsanRyhmä = None))))
        // !Väliaikainen! Solenovo ei osannut ajoissa korjata datojaan. Poistetaan mahd pian. Muistutus kalenterissa 28.5.
        // "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tutkinnonOsanRyhmäPuuttuu("Tutkinnonosalta tutkinnonosat/100023 puuttuu tutkinnonosan ryhmä, joka on pakollinen ammatillisen perustutkinnon tutkinnonosille."))))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Syötetään osaamisen hankkimistapa" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, osaamisenHankkimistapaOppilaitos))))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Syötetään deprekoitu osaamisen hankkimistapa" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, deprekoituOsaamisenHankkimistapaOppilaitos))))
        "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.deprekoituOsaamisenHankkimistapa())))
      }

      "Syötetään koulutussopimus" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(koulutussopimukset = Some(List(koulutussopimusjakso)))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Syötetään keskiarvo" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, vahvistus = vahvistus(date(2016, 9, 1)), osasuoritukset = Some(List(tutkinnonOsaSuoritus)), keskiarvo = Option(2.1f))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Syötetään tieto siitä, että keskiarvo sisältää mukautettuja arvosanoja" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, vahvistus = vahvistus(date(2016, 9, 1)), osasuoritukset = Some(List(tutkinnonOsaSuoritus)), keskiarvo = Option(2.1f), keskiarvoSisältääMukautettujaArvosanoja = Some(true))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "suoritus.vahvistus.päivä > päättymispäivä" - {
        "palautetaan HTTP 400" in putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)).copy(
          suoritukset = List(autoalanPerustutkinnonSuoritus().copy(
            keskiarvo = Some(4.0),
            vahvistus = vahvistus(date(2017, 5, 31)),
            osasuoritukset = Some(List(muunAmmatillisenTutkinnonOsanSuoritus))
          ))))(
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.päättymispäiväEnnenVahvistusta("suoritus.vahvistus.päivä (2017-05-31) oltava sama tai aiempi kuin päättymispäivä (2016-05-31)"))
        )
      }
    }

    "Ammatillinen perustutkinto näyttönä" - {
      val opiskelija = defaultHenkilö.copy(hetu = "030301-403L")

      "Tutkinnonosan ryhmä on määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osasuoritukset = Some(List(tutkinnonOsaSuoritus)))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk()))
      }

      "Tutkinnonosan ryhmää ei ole määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osasuoritukset = Some(List(tutkinnonOsaSuoritus.copy(tutkinnonOsanRyhmä = None))))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk()))
      }

      "Syötetään osaamisen hankkimistapa" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, osaamisenHankkimistapaOppilaitos))))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk()))
      }

      "Syötetään koulutussopimus" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, koulutussopimukset = Some(List(koulutussopimusjakso)))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk()))
      }

      "Yritetty antaa keskiarvo" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osasuoritukset = Some(List(tutkinnonOsaSuoritus)), keskiarvo = Option(2.1f))
        "palautetaan HTTP 400" in (putTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*onlyWhenMismatch.*".r))))
      }

      "suoritus.vahvistus.päivä > päättymispäivä" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(vahvistus = vahvistus(date(2017, 5, 31)), suoritustapa = suoritustapaNäyttö, osasuoritukset = Some(List(muunAmmatillisenTutkinnonOsanSuoritus)))
        "palautetaan HTTP 200" in putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)).copy(suoritukset = List(suoritus)), opiskelija)(
          verifyResponseStatusOk()
        )
      }
    }

    "Reformin mukainen tutkinto" - {
      val opiskelija = defaultHenkilö.copy(hetu = "090373-474B")

      def reformiSuoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaReformi)
      "Syötetään osaamisen hankkimistapa" - {
        val suoritus = reformiSuoritus.copy(osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, osaamisenHankkimistapaOppilaitos))))
        "palautetaan HTTP 200" in putTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk())
      }

      "Syötetään koulutussopimus" - {
        val suoritus = reformiSuoritus.copy(koulutussopimukset = Some(List(koulutussopimusjakso)))
        "palautetaan HTTP 200" in putTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk())
      }

      "Osasuoritukset vanhojen perusteiden mukaan (siirtymäaika 2018)" - {
        def suoritus(osasuoritus: AmmatillisenTutkinnonOsanSuoritus = tutkinnonOsaSuoritus) = reformiSuoritus.copy(
          osasuoritukset = Some(List(osasuoritus)),
          alkamispäivä = Some(date(2020, 1, 1))
        )
        def oppija(alkamispäivä: LocalDate, suoritus: AmmatillisenTutkinnonSuoritus) = {
          val opiskeluoikeus = makeOpiskeluoikeus(alkamispäivä).copy(suoritukset = List(suoritus))
          makeOppija(opiskelija, List(JsonSerializer.serializeWithRoot(opiskeluoikeus)))
        }

        "Alkamispäivä 2018, rakenne validi" - {
          "palautetaan HTTP 200" in putOppija(oppija(LocalDate.of(2018, 1, 1), suoritus()))(verifyResponseStatusOk())
        }
        "Alkamispäivä 2019" - {
          "palautetaan HTTP 400" in putOppija(oppija(LocalDate.of(2019, 1, 1), suoritus()))(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaaEiLöydyRakenteesta("Suoritustapaa ei löydy tutkinnon rakenteesta")))
        }
        "Alkamispäivä 2018, rakenne ei validi" - {
          val johtaminenJaHenkilöstönKehittäminen = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None)
          "palautetaan HTTP 400" in putOppija(oppija(LocalDate.of(2018, 1, 1), suoritus(tutkinnonOsaSuoritus.copy(koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen))))(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa tutkinnonosat/104052 ei löydy tutkintorakenteesta perusteelle 39/011/2014 - suoritustapa ops")))
        }
      }

      "Korkeakouluopinnot" - {
        "Ei tarvitse arviointia" in {
          val suoritusIlmanArviointeja = korkeakouluopintoSuoritus.copy(osasuoritukset = korkeakouluopintoSuoritus.osasuoritukset.map(_.map(_.copy(arviointi = None))))
          putTutkintoSuoritus(reformiSuoritus.copy(
            osasuoritukset = Some(List(suoritusIlmanArviointeja))
          ), opiskelija)(verifyResponseStatusOk())
        }
      }

      "Jatko-opintovalmiuksia tukevat opinnot" - {
        "Ei tarvitse arviointia" in {
          val suoritusIlmanArviointeja = jatkoOpintovalmiuksiaTukevienOpintojenSuoritus.copy(osasuoritukset = jatkoOpintovalmiuksiaTukevienOpintojenSuoritus.osasuoritukset.map(_.collect  { case l: LukioOpintojenSuoritus => l.copy(arviointi = None) }))
          putTutkintoSuoritus(reformiSuoritus.copy(
            osasuoritukset = Some(List(suoritusIlmanArviointeja))
          ), opiskelija)(verifyResponseStatusOk())
        }
      }
    }

    "Valma" - {
      "suoritus.vahvistus.päivä > päättymispäivä" - {
        val suoritus = autoalanPerustutkinnonSuoritusValma().copy(vahvistus = vahvistus(date(2017, 5, 31)), osasuoritukset = Some(List(ExamplesValma.valmaKoulutukseenOrientoitumine)))
        "palautetaan HTTP 200" in putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)).copy(suoritukset = List(suoritus)))(
          verifyResponseStatusOk()
        )
      }
    }

    "Ammatti- tai erikoisammattitutkinto" - {
      val tutkinnonOsanSuoritus = tutkinnonOsaSuoritus.copy(
        koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None)
      )

      def erikoisammattitutkintoSuoritus(osasuoritus: AmmatillisenTutkinnonOsanSuoritus) = autoalanErikoisammattitutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osasuoritukset = Some(List(osasuoritus)))

      "Tutkinnonosan ryhmää ei ole määritetty" - {
        val suoritus = erikoisammattitutkintoSuoritus(tutkinnonOsanSuoritus.copy(tutkinnonOsanRyhmä = None))
        "palautetaan HTTP 200" in (putTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
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
            (verifyResponseStatusOk())
        )
      }

      "Virheellinen y-tunnus" - {
        "palautetaan HTTP 400" in (
          putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(toteutusOppisopimuksella("1629284x5"))))
            (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*regularExpressionMismatch.*".r)))
        )
      }
    }

    "Muu ammatillinen" - {
      "Tutkinnon osaa pienempi kokonaisuus" - {
        "Paikallinen tutkinnon osaa pienempi kokonaisuus" - {
          val suoritus = kiinteistösihteerinTutkinnonOsaaPienempiMuuAmmatillinenKokonaisuus()
          "palautetaan HTTP 200" in putTutkinnonOsaaPienempienKokonaisuuksienSuoritus(suoritus)(verifyResponseStatusOk())
        }
      }

      "Muu ammatillinen koulutus" - {
        "Paikallinen muu ammatillinen koulutus" - {
          val suoritus = kiinteistösihteerinMuuAmmatillinenKoulutus()
          "palautetaan HTTP 200" in putMuuAmmatillinenKoulutusSuoritus(suoritus)(verifyResponseStatusOk())
        }

        "Ammatilliseen tehtävään valmistava koulutus" - {
          val suoritus = ansioJaLiikenneLentäjänMuuAmmatillinenKoulutus()
          "palautetaan HTTP 200" in putMuuAmmatillinenKoulutusSuoritus(suoritus)(verifyResponseStatusOk())
        }
      }

      "Opintojen rahoitus" - {
        "lasna -tilalta vaaditaan opintojen rahoitus" in {
          putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta lasna puuttuu rahoitusmuoto"))
          }
        }
        "loma -tilalta vaaditaan opintojen rahoitus" in {
          putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLoma))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta loma puuttuu rahoitusmuoto"))
          }
        }
        "valmistunut -tilalta vaaditaan opintojen rahoitus" in {
          val tila = AmmatillinenOpiskeluoikeudenTila(List(
            AmmatillinenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
            AmmatillinenOpiskeluoikeusjakso(date(2016, 1, 1), opiskeluoikeusValmistunut)
          ))
          putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila, suoritukset = List(AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta valmistunut puuttuu rahoitusmuoto"))
          }
        }
      }
    }

    "Yhteisen tutkinnon osan osa-alueen viestintä- ja vuorovaikutus kielivalinnalla suoritus VVAI22" - {
      "Koodia VVAI22 ei saa tallentaa jos perusteen voimaantulon päivä on ennen 1.8.2022" in {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(
          osasuoritukset = Some(List(yhtTutkinnonOsanSuoritusVVAI22()))
        )

        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.yhteinenTutkinnonOsaVVAI22())
        }
      }
      "Koodin VVAI22 saa tallentaa jos perusteen voimaantulon päivä on 1.8.2022 tai sen jälkeen" in {
        val suoritus = ajoneuvoalanPerustutkinnonSuoritus().copy(
          osasuoritukset = Some(List(yhtTutkinnonOsanSuoritusVVAI22("106727")))
        )

        putOpiskeluoikeus(henkilö = KoskiSpecificMockOppijat.tyhjä, opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
          verifyResponseStatusOk()
        }
      }
    }
  }

  def vahvistus(date: LocalDate) = {
    Some(HenkilövahvistusValinnaisellaPaikkakunnalla(date, Some(helsinki), stadinOpisto, List(Organisaatiohenkilö("Teppo Testaaja", "rehtori", stadinOpisto))))
  }


  def vahvistusValinnaisellaTittelillä(date: LocalDate) = {
    Some(HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(date, Some(helsinki), stadinOpisto, List(OrganisaatiohenkilöValinnaisellaTittelillä("Teppo Testaaja", Some("rehtori"), stadinOpisto))))
  }

  def arviointiHyvä(päivä: LocalDate = date(2015, 1, 1), arvosana: Koodistokoodiviite = hyvä1k3): Some[List[AmmatillinenArviointi]] = Some(List(AmmatillinenArviointi(arvosana, päivä)))

  lazy val hyvä1k3 = Koodistokoodiviite("2", "arviointiasteikkoammatillinent1k3")

  lazy val stadinOpisto: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.stadinAmmattiopisto)

  lazy val laajuus = LaajuusOsaamispisteissä(11)

  lazy val tutkinnonOsa: MuuValtakunnallinenTutkinnonOsa = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100023", "tutkinnonosat"), true, Some(laajuus))
  lazy val yhteinenTutkinnonOsa: YhteinenTutkinnonOsa = YhteinenTutkinnonOsa(Koodistokoodiviite("100023", "tutkinnonosat"), true, Some(laajuus))

  lazy val tutkinnonSuoritustapaNäyttönä = Koodistokoodiviite("naytto", "ammatillisentutkinnonsuoritustapa")
  lazy val tutkinnonSuoritustapaOps = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa")
  lazy val tutkinnonSuoritustapaReformi = Koodistokoodiviite("reformi", "ammatillisentutkinnonsuoritustapa")

  lazy val tutkinnonOsaSuoritus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = tutkinnonOsa,
    toimipiste = Some(OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka"))),
    arviointi = arviointiHyvä(),
    tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat
  )

  lazy val yhtTutkinnonOsanSuoritus = yhteisenTutkinnonOsanSuoritus("101053", "Viestintä- ja vuorovaikutusosaaminen", k3, 11).copy(
    osasuoritukset = Some(List(
      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(5))), arviointi = Some(List(arviointiKiitettävä))),
      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = false, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))),
      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(Koodistokoodiviite("TK1", "ammatillisenoppiaineet"), Koodistokoodiviite("SV", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(1))), arviointi = Some(List(arviointiKiitettävä))),
      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(Koodistokoodiviite("VK", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(2))), arviointi = Some(List(arviointiKiitettävä))),
      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVTK", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(2))), arviointi = Some(List(arviointiKiitettävä)))
    )),
    arviointi = arviointiHyvä(),
  )

  def yhtTutkinnonOsanSuoritusVVAI22(koodiArvo: String = "101053") = yhteisenTutkinnonOsanSuoritus(koodiArvo, "Viestintä- ja vuorovaikutusosaaminen", k3, 2).copy(
    osasuoritukset = Some(List(
      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVAI22", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(2))), arviointi = Some(List(arviointiKiitettävä)))
    )),
    arviointi = arviointiHyvä(),
  )

  lazy val paikallinenTutkinnonOsa = PaikallinenTutkinnonOsa(
    PaikallinenKoodi("1", "paikallinen osa"), "Paikallinen tutkinnon osa", false, Some(laajuus)
  )

  lazy val paikallinenTutkinnonOsaSuoritus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = paikallinenTutkinnonOsa,
    toimipiste = Some(OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka"))),
    arviointi = arviointiHyvä()
  )

  lazy val osanOsa = AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
    AmmatillisenTutkinnonOsaaPienempiKokonaisuus(PaikallinenKoodi("htm", "Hoitotarpeen määrittäminen"), "Hoitotarpeen määrittäminen"),
    arviointi = Some(List(arviointiHyväksytty))
  )

  def putTutkinnonOsaSuoritus[A](tutkinnonOsaSuoritus: AmmatillisenTutkinnonOsanSuoritus, tutkinnonSuoritustapa: Koodistokoodiviite, henkilö: Henkilö = defaultHenkilö)(f: => A) = {
    putTutkintoSuoritus(withTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, tutkinnonSuoritustapa), henkilö)(f)
  }

  def putTutkinnonOsaSuoritukset[A](tutkinnonOsaSuoritukset: List[AmmatillisenTutkinnonOsanSuoritus], tutkinnonSuoritustapa: Koodistokoodiviite)(f: => A) = {
    putTutkintoSuoritus(withOsasuoritukset(tutkinnonOsaSuoritukset, tutkinnonSuoritustapa))(f)
  }

  def withTutkinnonOsaSuoritus(tutkinnonOsaSuoritus: AmmatillisenTutkinnonOsanSuoritus, tutkinnonSuoritustapa: Koodistokoodiviite): AmmatillisenTutkinnonSuoritus =
    withOsasuoritukset(List(tutkinnonOsaSuoritus), tutkinnonSuoritustapa)

  def withOsasuoritukset(osasuoritukset: List[AmmatillisenTutkinnonOsanSuoritus], tutkinnonSuoritustapa: Koodistokoodiviite): AmmatillisenTutkinnonSuoritus =
    autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapa, osasuoritukset = Some(osasuoritukset))

  def putTutkintoSuoritus[A](suoritus: AmmatillisenTutkinnonSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putAmmatillinenPäätasonSuoritus(suoritus, henkilö, headers)(f)
  }

  def putTutkinnonOsaaPienempienKokonaisuuksienSuoritus[A](suoritus: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putAmmatillinenPäätasonSuoritus(suoritus, henkilö, headers)(f)
  }

  def putMuuAmmatillinenKoulutusSuoritus[A](suoritus: MuunAmmatillisenKoulutuksenSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putAmmatillinenPäätasonSuoritus(suoritus, henkilö, headers)(f)
  }

  def putAmmatillinenPäätasonSuoritus[A](suoritus: AmmatillinenPäätasonSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))

    putOppija(makeOppija(henkilö, List(JsonSerializer.serializeWithRoot(opiskeluoikeus))), headers)(f)
  }

  private def putAndGetOpiskeluoikeus(oo: AmmatillinenOpiskeluoikeus, henkilö: Henkilö = defaultHenkilö): Opiskeluoikeus = putOpiskeluoikeus(oo, henkilö) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }

  private def mockKoskiValidator(config: Config) = {
    new KoskiValidator(
      KoskiApplicationForTests.tutkintoRepository,
      KoskiApplicationForTests.koodistoViitePalvelu,
      KoskiApplicationForTests.organisaatioRepository,
      KoskiApplicationForTests.possu,
      KoskiApplicationForTests.henkilöRepository,
      KoskiApplicationForTests.ePerusteet,
      KoskiApplicationForTests.validatingAndResolvingExtractor,
      KoskiApplicationForTests.suostumuksenPeruutusService,
      config
    )
  }

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(koulutusmoduuli = autoalanPerustutkinnonSuoritus().koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  override def vääräntyyppisenPerusteenDiaarinumero: String = "60/011/2015"
  def eperusteistaLöytymätönValidiDiaarinumero: String = "13/011/2009"
}
