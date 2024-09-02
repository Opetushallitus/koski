package fi.oph.koski.api.oppijavalidation

import com.typesafe.config.Config
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.AmmatillinenOldExamples.muunAmmatillisenTutkinnonOsanSuoritus
import fi.oph.koski.documentation.AmmatillinenReforminMukainenPerustutkintoExample.{jatkoOpintovalmiuksiaTukevienOpintojenSuoritus, korkeakouluopintoSuoritus}
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.{AmmattitutkintoExample, ExampleData, ExamplesValma}
import fi.oph.koski.eperusteetvalidation.{EPerusteetFiller, EPerusteetLops2019Validator, EPerusteisiinPerustuvaValidator}
import fi.oph.koski.fixture.AmmatillinenOpiskeluoikeusTestData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers.{jyväskylänNormaalikoulunPalvelukäyttäjä, stadinAmmattiopistoPalvelukäyttäjä}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationAmmatillinenSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  "Ammatillisen koulutuksen opiskeluoikeuden lisääminen" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }
    }

    "Kun tutkintosuoritus puuttuu" - {
      "palautetaan HTTP 400 virhe"  in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = Nil)) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lessThanMinimumNumberOfItems.*".r)))
      }
    }

    "Tutkinnon perusteet ja rakenne" - {
      "Osaamisala ja suoritustapa" - {
        "Osaamisala ja suoritustapa ok" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1527", "osaamisala")))))

          "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
        }
        "Suoritustapa virheellinen" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("blahblahtest", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1527", "osaamisala")))))

          "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia ammatillisentutkinnonsuoritustapa/blahblahtest ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
        }
        "Osaamisala ei löydy tutkintorakenteesta" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("3053", "osaamisala")))))

          "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala 3053 ei löydy tutkintorakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle 39/011/2014 (612)"))))
        }
        "Osaamisala virheellinen" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa"),
            osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("0", "osaamisala")))))

          "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia osaamisala/0 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
        }
        "Tutkintonimikettä ei löydy tutkintorakenteesta" - {
          val suoritus = autoalanPerustutkinnonSuoritus().copy(
            suoritustapa = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa"),
            tutkintonimike = Some(List(Koodistokoodiviite("20013", "tutkintonimikkeet")))
          )

          "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkintonimike("Tutkintonimikkeitä Vaatturi (AT)(20013) ei löydy tutkintorakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle 39/011/2014 (612)"))))
        }
      }

      "Tutkinnon osat ja arvionnit" - {
        val johtaminenJaHenkilöstönKehittäminen = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None)

        "Valtakunnallinen tutkinnonosa" - {
          "Tutkinnon osa ja arviointi ok" - {
            "palautetaan HTTP 200" in (setupTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, tutkinnonSuoritustapaOps) (verifyResponseStatusOk()))
          }

          "Tutkinnon osa ei kuulu tutkintorakenteeseen" - {
            "Pakolliset ja ammatilliset tutkinnon osat" - {
              "palautetaan HTTP 400" in (setupTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa tutkinnonosat/104052 ei löydy tutkintorakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle 39/011/2014 (612) - suoritustapa naytto"))))
            }
            "Vapaavalintaiset tutkinnon osat" - {
              "palautetaan HTTP 200" in (setupTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
                  koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen, tutkinnonOsanRyhmä = vapaavalintaisetTutkinnonOsat
                ), tutkinnonSuoritustapaOps)(
                verifyResponseStatusOk()))
            }
          }

          "Tutkinnon osaa ei ei löydy koodistosta" - {
            "palautetaan HTTP 400" in (setupTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
              koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("9923123", "tutkinnonosat"), true, None)), tutkinnonSuoritustapaNäyttönä)
              (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia tutkinnonosat/9923123 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
          }

          "Sama pakollinen tutkinnon osa kahteen kertaan" - {
            val suoritus = autoalanPerustutkinnonSuoritus().copy(
              osasuoritukset = Some(List(
                tutkinnonOsaSuoritus, tutkinnonOsaSuoritus
              ))
            )

            "palautetaan HTTP 200" in setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk())
          }

          "Sama valinnainen tutkinnon osa kahteen kertaan" - {
            val valinnainenTutkinnonosa = tutkinnonOsaSuoritus.copy(koulutusmoduuli = tutkinnonOsa.copy(pakollinen = false))
            val suoritus = autoalanPerustutkinnonSuoritus().copy(
              osasuoritukset = Some(List(
                valinnainenTutkinnonosa, valinnainenTutkinnonosa
              ))
            )
            "palautetaan HTTP 200" in setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk())
          }



          "Tutkinnon osan osat" - {
            "Sama osa kahteen kertaan" - {
              "Palautetaan HTTP 200" in (
                setupTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(osasuoritukset = Some(List(
                  osanOsa, osanOsa
                ))), tutkinnonSuoritustapaOps) (verifyResponseStatusOk())
              )
            }
          }

          "Yhteiset tutkinnon osat" - {
            "Osan laajuus ei vastaa osan osien yhteislaajuutta" - {
              "Palautetaan HTTP 400" in (
              setupTutkinnonOsaSuoritus(yhtTutkinnonOsanSuoritus, tutkinnonSuoritustapaOps) (
                verifyResponseStatus(400,
                  KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Yhteisillä tutkinnon osilla 'Viestintä- ja vuorovaikutusosaaminen' on eri laajuus kun tutkinnon osien osa-alueiden yhteenlaskettu summa")))
                )
            }

            "Arvioidun osan laajuus ei perusteen mukainen" - {
              "Palautetaan HTTP 400" in (
                setupTutkinnonOsaSuoritus(yhtTutkinnonOsanSuoritus.copy(
                  koulutusmoduuli = yhtTutkinnonOsanSuoritus.koulutusmoduuli.copy(laajuus = Some(LaajuusOsaamispisteissä(10)))
                ), tutkinnonSuoritustapaOps)(
                  verifyResponseStatus(400,
                    KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta
                    ("Arvioidun suorituksen 'Viestintä- ja vuorovaikutusosaaminen' laajuus oltava perusteen mukaan vähintään 11 (oli 10.0)")))
                )
            }

            "Osa-alue ei kuulu osaan" - {
              val suoritus = ajoneuvoalanPerustutkinnonSuoritus().copy(
                osasuoritukset = Some(List(
                  yhteisenTutkinnonOsanSuoritus("106727", "Viestintä- ja vuorovaikutusosaaminen", k3, 3).copy(
                    osasuoritukset = Some(List(
                      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("MLMA", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(4)))),
                    )),
                    arviointi = None,
                    vahvistus = None,
                  )))
              )
              "Palautetaan HTTP 400" in (
                setupOppijaWithOpiskeluoikeus(henkilö = KoskiSpecificMockOppijat.tyhjä, opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
                  verifyResponseStatus(400,
                    KoskiErrorCategory.badRequest.validation.rakenne(
                      "Osa-alue 'Matematiikka ja matematiikan soveltaminen' (MLMA) ei kuulu perusteen mukaan tutkinnon osaan 'Viestintä- ja vuorovaikutusosaaminen'")
                  )
                })
            }

            "Sallitaan paikallinen osa-alue" - {
              val suoritus = ajoneuvoalanPerustutkinnonSuoritus().copy(
                osasuoritukset = Some(List(
                  yhteisenTutkinnonOsanSuoritus("106727", "Viestintä- ja vuorovaikutusosaaminen", k3, 5).copy(
                    osasuoritukset = Some(List(
                      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = PaikallinenAmmatillisenTutkinnonOsanOsaAlue(PaikallinenKoodi("paikallinen", "paikallinen"), "paikallinen", pakollinen = true, Some(LaajuusOsaamispisteissä(5)))),
                    )),
                    arviointi = None,
                    vahvistus = None,
                  )))
              )
              "Palautetaan HTTP 200" in (
                setupOppijaWithOpiskeluoikeus(henkilö = KoskiSpecificMockOppijat.tyhjä, opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
                  verifyResponseStatusOk()
                })
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
                setupTutkintoSuoritus(suoritus)(
                  verifyResponseStatus(400, HttpStatus.fold(KoskiErrorCategory.badRequest.validation.rakenne.yhteiselläOsuudellaEiOsasuorituksia("Arvioidulla yhteisellä tutkinnon osalla 'Viestintä- ja vuorovaikutusosaaminen' ei ole osa-alueita"),
                  )))
                )
            }

            "Osa-alueella ei osasuorituksia, suoritustapa ops" - {
              "Palautetaan HTTP 200" in (
                setupTutkinnonOsaSuoritus(yhtTutkinnonOsanSuoritus.copy(osasuoritukset = Some(List())), tutkinnonSuoritustapaOps) (
                  verifyResponseStatusOk()
                ))
            }


            "Osa-alueen laajuus ei perusteen mukainen, VVAI22 pakollinen + valinnainen" - {
              val suoritus = ajoneuvoalanPerustutkinnonSuoritus().copy(
                osasuoritukset = Some(List(
                  yhteisenTutkinnonOsanSuoritus("106727", "Viestintä- ja vuorovaikutusosaaminen", k3, 3).copy(
                    osasuoritukset = Some(List(
                      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVAI22", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))),
                      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVAI22", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = false, Some(LaajuusOsaamispisteissä(2))), arviointi = Some(List(arviointiKiitettävä)))
                    )),
                    arviointi = None,
                    vahvistus = None,
                  )))
              )
              "Palautetaan HTTP 400" in (
                setupOppijaWithOpiskeluoikeus(henkilö = KoskiSpecificMockOppijat.tyhjä, opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
                  verifyResponseStatus(400, List(
                    ErrorMatcher.exact(
                      KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta,
                      "Osa-alueen 'Viestintä ja vuorovaikutus äidinkielellä, englanti' (VVAI22) pakollisen osan laajuus oltava perusteen mukaan 4 (oli 3.0)"),
                    ErrorMatcher.exact(
                      KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta,
                      "Osa-alueen 'Viestintä ja vuorovaikutus äidinkielellä, englanti' (VVAI22) valinnaisen osan laajuus oltava perusteen mukaan 3 (oli 2.0)")
                  ))
                })
            }

            "Osa-alueen laajuus ei perusteen mukainen, MLMA" - {
              val suoritus = ajoneuvoalanPerustutkinnonSuoritus().copy(
                osasuoritukset = Some(List(
                  yhteisenTutkinnonOsanSuoritus("106728", "Matemaattis-luonnontieteellinen osaaminen", k3, 3).copy(
                    osasuoritukset = Some(List(
                      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("MLMA", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(5)))),
                    )),
                    arviointi = None,
                    vahvistus = None,
                  )))
              )
              "Palautetaan HTTP 400" in (
                setupOppijaWithOpiskeluoikeus(henkilö = KoskiSpecificMockOppijat.tyhjä, opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
                  verifyResponseStatus(400,
                    KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta(
                      "Osa-alueen 'Matematiikka ja matematiikan soveltaminen' (MLMA) pakollisen osan laajuus oltava perusteen mukaan 4 (oli 5.0)")
                  )
                })
            }

            "Osa-alueiden yhteenlaskettu laajuus" - {
              "On alle 35" - {
                val yhtSuoritus = yhteisenTutkinnonOsanSuoritus("400012", "Viestintä- ja vuorovaikutusosaaminen", k3, 11).copy(
                  osasuoritukset = Some(List(
                    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVAI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("FI", "kielivalikoima"), laajuus = Some(LaajuusOsaamispisteissä(4))), arviointi = Some(List(arviointiKiitettävä))),
                    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVTK", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("SV", "kielivalikoima"), laajuus = Some(LaajuusOsaamispisteissä(1))), arviointi = Some(List(arviointiKiitettävä))),
                    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVVK", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("EN", "kielivalikoima"), laajuus = Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))),
                    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("VVTD", "ammatillisenoppiaineet"), pakollinen = true, laajuus = Some(LaajuusOsaamispisteissä(2))), arviointi = Some(List(arviointiKiitettävä))),
                    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("VVTL", "ammatillisenoppiaineet"), pakollinen = true, laajuus = Some(LaajuusOsaamispisteissä(1))), arviointi = Some(List(arviointiKiitettävä))),
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
                  setupTutkintoSuoritus(suoritus)(
                    verifyResponseStatus(400, HttpStatus.fold(KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Valmiiksi merkityn suorituksen koulutus/351741 yhteisten tutkinnon osien laajuuden tulee olla vähintään 35"))))
                  )
              }
              "On 35" - {
                val yhtSuoritukset = List(
                  yhteisenTutkinnonOsanSuoritus("400012", "Viestintä- ja vuorovaikutusosaaminen", k3, 4).copy(
                    vahvistus = None,
                    arviointi = None,
                    osasuoritukset = Some(List(
                      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVAI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("FI", "kielivalikoima"), laajuus = Some(LaajuusOsaamispisteissä(4))), arviointi = Some(List(arviointiKiitettävä))),
                    ))
                  ),
                  yhteisenTutkinnonOsanSuoritus("400013", "Matemaattis-luonnontieteellinen osaaminen", k3, 30).copy(
                    vahvistus = None,
                    arviointi = None,
                    osasuoritukset = Some(List(
                      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = PaikallinenAmmatillisenTutkinnonOsanOsaAlue(PaikallinenKoodi("MA", "Matematiikka"), "Matematiikan opinnot", pakollinen = true, Some(LaajuusOsaamispisteissä(30))), arviointi = Some(List(arviointiKiitettävä))),
                    ))
                  ))
                val reformiSuoritus = puuteollisuudenPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaReformi,
                  osasuoritukset = Some(yhtSuoritukset))
                val suoritus = reformiSuoritus.copy(
                  osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018, 1, 1), None, osaamisenHankkimistapaOppilaitos))),
                  vahvistus = None,
                  keskiarvo = None
                )
                "Palautetaan HTTP 200" in (
                  setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk())
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
                setupTutkintoSuoritus(suoritus) (
                  verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Suorituksella koulutus/351301 on useampi yhteinen osasuoritus samalla koodilla")))
                )
            }

            "Reformi-muotoisella tutkinnolla väärän koodin yhteisiä osuuksia" - {
              val yhtSuoritukset = List(
                yhteisenTutkinnonOsanSuoritus("101053", "Viestintä- ja vuorovaikutusosaaminen", k3, 4).copy(
                  arviointi = None,
                  vahvistus = None,
                  osasuoritukset = Some(List(
                    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVAI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("FI", "kielivalikoima"), laajuus = Some(LaajuusOsaamispisteissä(4))), arviointi = Some(List(arviointiKiitettävä))),
                  ))
                ))
              val reformiSuoritus = virheellinenPuuteollisuudenPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaReformi,
                osasuoritukset = Some(yhtSuoritukset))
              val suoritus = reformiSuoritus.copy(
                osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018, 1, 1), None, osaamisenHankkimistapaOppilaitos))),
                vahvistus = None,
                keskiarvo = None
              )
              "Palautetaan HTTP 400" in (
                setupTutkintoSuoritus(suoritus) (
                  verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääränKoodinYhteinenOsasuoritus("Suorituksella koulutus/351741 on Ops-muotoiselle tutkinnolle tarkoitettu yhteinen osasuoritus")))
                )
            }
          }
        }

        "Paikallinen tutkinnonosa" - {
          "Tutkinnon osa ja arviointi ok" - {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat)
            "palautetaan HTTP 200" in (setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps) (verifyResponseStatusOk()))
          }

          "Paikallinen tutkinnon osa ja koodisto uri" - {
            lazy val paikallinenTunniste = PaikallinenKoodi("1", "paikallinen osa", Some("jokukoodistouri"))
            lazy val paikallinenTutkinnonOsaUrilla = PaikallinenTutkinnonOsa(
              paikallinenTunniste, "Paikallinen tutkinnon osa koodistoUrilla", false, Some(laajuus)
            )
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(koulutusmoduuli = paikallinenTutkinnonOsaUrilla, tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat)
            "palautetaan HTTP 200 mutta paikallinen koodisto uri pudotetaan pois" in {
              setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps){
                verifyResponseStatusOk()

                val oo = getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
                oo.suoritukset.head.osasuoritukset.get.map(_.koulutusmoduuli).exists {
                  case p: PaikallinenTutkinnonOsa =>
                    p.tunniste == paikallinenTunniste.copy(koodistoUri = None)
                  case _ => false
                } shouldBe true
              }
            }
          }

          "Laajuus negatiivinen" - {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(koulutusmoduuli = paikallinenTutkinnonOsa.copy(laajuus = Some(laajuus.copy(arvo = -1))))
            "palautetaan HTTP 400" in (setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (
              verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*exclusiveMinimumValue.*".r)))
            )
          }
        }

        "Tuntematon tutkinnonosa" - {
          "palautetaan HTTP 400 virhe"  in {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(tyyppi = Koodistokoodiviite(koodiarvo = "tuntematon", koodistoUri = "suorituksentyyppi"))
            setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (
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
            "palautetaan HTTP 200" in (setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps)(
              verifyResponseStatusOk()))
          }
        }

        "Tutkinnon osa toisesta tutkinnosta" - {
          val autoalanTyönjohdonErikoisammattitutkinto = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("457305", "koulutus"), Some("40/011/2001"))

          def osanSuoritusToisestaTutkinnosta(tutkinto: AmmatillinenTutkintoKoulutus, tutkinnonOsa: MuuKuinYhteinenTutkinnonOsa): AmmatillisenTutkinnonOsanSuoritus = tutkinnonOsaSuoritus.copy(
            tutkinto = Some(tutkinto),
            koulutusmoduuli = tutkinnonOsa
          )

          "Kun tutkinto löytyy ja osa kuuluu sen rakenteeseen" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanTyönjohdonErikoisammattitutkinto, johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 200" in (setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps)(
              verifyResponseStatusOk()))
          }

          "Kun tutkintoa ei löydy" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("123456", "koulutus"), Some("40/011/2001")), johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 400" in (setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia koulutus/123456 ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
          }

          "Kun osa ei kuulu annetun tutkinnon rakenteeseen" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(parturikampaaja, johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 200 (ei validoida rakennetta tässä)" in (setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps)(
              verifyResponseStatusOk()))
          }

          "Kun tutkinnolla ei ole diaarinumeroa" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = None), johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 400 (diaarinumero vaaditaan)" in (setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu())))
          }

          "Kun tutkinnon diaarinumero on virheellinen" - {
            "palautetaan HTTP 400" in (setupTutkinnonOsaSuoritus(osanSuoritusToisestaTutkinnosta(
              autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = Some("Boom boom kah")),
              johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari(s"Opiskeluoikeuden voimassaoloaikana voimassaolevaa tutkinnon perustetta ei löydy diaarinumerolla Boom boom kah"))))
          }

          "Kun tutkinnon diaarinumero on muodoltaan virheellinen" - {
            "palautetaan HTTP 400" in (setupTutkinnonOsaSuoritus(osanSuoritusToisestaTutkinnosta(
              autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = Some("Lorem ipsum dolor sit amet, consectetur adipiscing elit")),
              johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Diaarinumeron muoto on virheellinen: Lorem ipsum dolor sit amet, co"))))
          }

          "Kun tutkinnon osalle ilmoitetaan tutkintotieto, joka on sama kuin päätason tutkinto" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanPerustutkinto, johtaminenJaHenkilöstönKehittäminen)
            "palautetaan HTTP 400" in (setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.samaTutkintokoodi("Tutkinnon osalle tutkinnonosat/104052 on merkitty tutkinto, jossa on sama diaarinumero 39/011/2014 kuin tutkinnon suorituksessa"))))
          }

          "Kun tunnustettu osa ei kuulu annetun tutkinnon rakenteeseen eikä sen peruste ole voimassa" - {
            val suoritus = osanSuoritusToisestaTutkinnosta(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("331101", "koulutus"), Some("1000/011/2014")), johtaminenJaHenkilöstönKehittäminen) match {
              case m: MuunAmmatillisenTutkinnonOsanSuoritus => m.copy(tunnustettu = Some(tunnustettu))
            }
            "palautetaan HTTP 200 (ei validoida rakennetta tässä)" in (setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaOps)(
              verifyResponseStatusOk()))
          }

        }

        "Tunnisteen koodiarvon validointi" - {

          "Tunnisteen koodiarvo ei löydy rakenteen koulutuksista" - {
            val suoritus =  autoalanPerustutkinnonSuoritus().copy(koulutusmoduuli = autoalanPerustutkinto.copy(tunniste = autoalanPerustutkinto.tunniste.copy(koodiarvo = "361902")))
            "palautetaan HTTP 400" in (setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))))(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tunnisteenKoodiarvoaEiLöydyRakenteesta("Tunnisteen koodiarvoa 361902 ei löytynyt opiskeluoikeuden voimassaoloaikana voimassaolleen rakenteen 39/011/2014 mahdollisista koulutuksista. Tarkista tutkintokoodit ePerusteista."))
            )
          }

          "Löydetyssä rakenteessa ei ole yhtään koulutusta"  - {
            val suoritus =  autoalanPerustutkinnonSuoritus().copy(koulutusmoduuli = autoalanPerustutkinto.copy(perusteenDiaarinumero = Some("mock-empty-koulutukset")))
            "palautetaan HTTP 200" in (setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus)), defaultHenkilö.copy(hetu = "120950-0351")))(
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

          def setup(suoritus: AmmatillisenTutkinnonOsanSuoritus)(f: => Unit) = setupTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä, tilanHenkilö)(f)
          def setupOsasuoritukset(suoritukset: List[AmmatillisenTutkinnonOsanSuoritus])(f: => Unit) = setupTutkinnonOsaSuoritukset(suoritukset, tutkinnonSuoritustapaNäyttönä)(f)

          "Arviointi ja vahvistus puuttuu" - {
            "palautetaan HTTP 200" in (setup(copySuoritus(None, None)) (
              verifyResponseStatusOk()
            ))
          }

          "Arviointi annettu" - {
            "palautetaan HTTP 200" in (setup(copySuoritus(arviointiHyvä(), None)) (
              verifyResponseStatusOk()
            ))
          }

          "Suorituksella arviointi ja vahvistus" - {
            "palautetaan HTTP 200" in (setup(copySuoritus(arviointiHyvä(), vahvistusValinnaisellaTittelillä(LocalDate.parse("2016-08-08")))) (
              verifyResponseStatusOk()
            ))
          }

          "Vahvistus annettu, mutta arviointi puuttuu" - {
            "palautetaan HTTP 400" in (setup(copySuoritus(None, vahvistusValinnaisellaTittelillä(LocalDate.parse("2016-08-08")))) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusIlmanArviointia("Suorituksella tutkinnonosat/100023 on vahvistus, vaikka arviointi puuttuu"))
            ))
          }

          "Vahvistuksen myöntäjähenkilö puuttuu" - {
            "palautetaan HTTP 400" in (setup(copySuoritus(arviointiHyvä(), Some(HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(LocalDate.parse("2016-08-08"), Some(helsinki), stadinOpisto, Nil)))) (
              verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lessThanMinimumNumberOfItems.*".r))
            ))
          }

          "Arviointi" - {
            "Arviointiasteikko on tuntematon" - {
              "palautetaan HTTP 400" in (setup(copySuoritus(Some(List(AmmatillinenArviointi(Koodistokoodiviite("2", "vääräasteikko"), date(2015, 5, 1)))), None))
                (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*arviointiasteikkoammatillinenhyvaksyttyhylatty.*enumValueMismatch.*".r))))
            }

            "Arvosana ei kuulu perusteiden mukaiseen arviointiasteikkoon" - {
              "palautetaan HTTP 400" in (setup(copySuoritus(Some(List(AmmatillinenArviointi(Koodistokoodiviite("x", "arviointiasteikkoammatillinent1k3"), date(2015, 5, 1)))), None))
                (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, """.*"message":"Koodia arviointiasteikkoammatillinent1k3/x ei löydy koodistosta","errorType":"tuntematonKoodi".*""".r))))
            }

            "Useita arviointiasteikoita käytetty" - {
              "palautetaan HTTP 400" in (setupOsasuoritukset(List(copySuoritus(arviointiHyvä(), None), copySuoritus(arviointiHyvä(arvosana = arvosanaViisi), None))) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.useitaArviointiasteikoita("Suoritus käyttää useampaa kuin yhtä numeerista arviointiasteikkoa: arviointiasteikkoammatillinen15, arviointiasteikkoammatillinent1k3"))
              ))
            }

            "Useita arviointiasteikoita käytetty, näyttö" - {
              val näytöllinenSuoritus = copySuoritus(arviointiHyvä(arvosana = arvosanaViisi), None).copy(näyttö = Some(näyttö(date(2016, 2, 1), "Näyttö", "Näyttöpaikka, Näyttölä", Some(näytönArviointi))))
              "palautetaan HTTP 400" in (setupOsasuoritukset(List(copySuoritus(arviointiHyvä(), None), näytöllinenSuoritus)) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.useitaArviointiasteikoita("Suoritus käyttää useampaa kuin yhtä numeerista arviointiasteikkoa: arviointiasteikkoammatillinen15, arviointiasteikkoammatillinent1k3"))
              ))
            }

            "Useita arviointiasteikoita käytetty, näytön arvioinnin arviointikohteet" - {
              val arviointikohteet = näytönArviointi.arviointikohteet.toList.flatten
              val arviointi = näytönArviointi.copy(arviointikohteet = Some(arviointikohteet.head.copy(arvosana = arvosanaViisi) :: arviointikohteet.tail))
              val näytöllinenSuoritus = copySuoritus(arviointiHyvä(), None).copy(näyttö = Some(näyttö(date(2016, 2, 1), "Näyttö", "Näyttöpaikka, Näyttölä", Some(arviointi))))
              "palautetaan HTTP 400" in (setupOsasuoritukset(List(copySuoritus(arviointiHyvä(), None), näytöllinenSuoritus)) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.useitaArviointiasteikoita("Suoritus käyttää useampaa kuin yhtä numeerista arviointiasteikkoa: arviointiasteikkoammatillinen15, arviointiasteikkoammatillinent1k3"))
              ))
            }

            "Useita arviointiasteikoita käytetty, tutkinnon osien osat" - {
              val suoritusOsienOsat = tutkinnonOsaSuoritus.copy(osasuoritukset = Some(List(osanOsa.copy(arviointi = arviointiHyvä()))))
              "palautetaan HTTP 400" in (setupOsasuoritukset(List(copySuoritus(arviointiHyvä(arvosana = arvosanaViisi), None), suoritusOsienOsat)) (
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.useitaArviointiasteikoita("Suoritus käyttää useampaa kuin yhtä numeerista arviointiasteikkoa: arviointiasteikkoammatillinen15, arviointiasteikkoammatillinent1k3"))
              ))
            }
          }

          "Suorituksen päivämäärät" - {
            def päivämäärillä(alkamispäivä: String, arviointipäivä: String, vahvistuspäivä: String) = {
              copySuoritus(arviointiHyvä(LocalDate.parse(arviointipäivä)), vahvistusValinnaisellaTittelillä(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)))
            }

            "Päivämäärät kunnossa" - {
              "palautetaan HTTP 200"  in (setup(päivämäärillä("2015-08-01", "2016-05-30", "2016-06-01"))(
                verifyResponseStatusOk()))
            }

            "Päivämäärät tulevaisuudessa" - {
              "palautetaan HTTP 200"  in (setup(päivämäärillä("2115-08-01", "2116-05-30", "2116-06-01"))(
                verifyResponseStatusOk()))
            }

            "alkamispäivä > arviointi.päivä" - {
              "palautetaan HTTP 400"  in (setup(päivämäärillä("2016-08-01", "2015-05-31", "2015-05-31"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.arviointiEnnenAlkamispäivää("suoritus.alkamispäivä (2016-08-01) oltava sama tai aiempi kuin suoritus.arviointi.päivä (2015-05-31)"))))
            }

            "arviointi.päivä > vahvistus.päivä" - {
              "palautetaan HTTP 400"  in (setup(päivämäärillä("2015-08-01", "2016-05-31", "2016-05-30"))(
                verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenArviointia("suoritus.arviointi.päivä (2016-05-31) oltava sama tai aiempi kuin suoritus.vahvistus.päivä (2016-05-30)"))))
            }

            "osasuoritus.vahvistus.päivä > suoritus.vahvistus.päivä" - {
              "palautetaan HTTP 400"  in {
                val suoritus: AmmatillisenTutkinnonSuoritus = withTutkinnonOsaSuoritus(päivämäärillä("2015-08-01", "2017-05-30", vahvistuspäivä = "2017-06-01"), tutkinnonSuoritustapaNäyttönä)
                setupTutkintoSuoritus(suoritus.copy(vahvistus = vahvistus(date(2017, 5, 31)))) {
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

            "ja tutkinnon osalta puuttuu arviointi, palautetaan HTTP 400" in (setupOppijaWithOpiskeluoikeus(opiskeluoikeus) (
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella koulutus/351301 on keskeneräinen osasuoritus tutkinnonosat/100023"))))

            "tutkinnolla ei osasuorituksia, palautetaan HTTP 400" in (setupOppijaWithOpiskeluoikeus(tyhjilläOsasuorituksilla)(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia("Suoritus koulutus/351301 on merkitty valmiiksi, mutta sillä ei ole ammatillisen tutkinnon osan suoritusta tai opiskeluoikeudelta puuttuu linkitys"))))

            "Opiskeluoikeus ostettu" - {

              "Opiskeluoikeus valmis ennen vuotta 2019" - {
                val valmisTila = AmmatillinenOpiskeluoikeusjakso(date(2018, 12, 31), ExampleData.opiskeluoikeusValmistunut, Some(valtionosuusRahoitteinen))
                val valmisOpiskeluoikeus = tyhjilläOsasuorituksilla.copy(tila = AmmatillinenOpiskeluoikeudenTila(tyhjilläOsasuorituksilla.tila.opiskeluoikeusjaksot :+ valmisTila), ostettu = true)
                "palautetaan HTTP 200" in (setupOppijaWithOpiskeluoikeus(valmisOpiskeluoikeus, defaultHenkilö.copy(hetu = "150435-0429"))(
                  verifyResponseStatusOk()))
              }

              "Opiskeluoikeus valmis vuoden 2018 jälkeen" - {
                val valmisTila = AmmatillinenOpiskeluoikeusjakso(date(2019, 1, 1), ExampleData.opiskeluoikeusValmistunut, Some(valtionosuusRahoitteinen))
                val valmisOpiskeluoikeus = opiskeluoikeus.copy(tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeus.tila.opiskeluoikeusjaksot :+ valmisTila))
                "palautetaan HTTP 400" in (setupOppijaWithOpiskeluoikeus(valmisOpiskeluoikeus.copy(suoritukset = List(eiOsasuorituksia), ostettu = true))(
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
            setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
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

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus, defaultHenkilö.copy(hetu = "160337-625E")) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.useampiPäätasonSuoritus())
          }
        }

        "Sallitaan kaksi päätason suoritusta, kun yhdistelmänä 'ammatillinentutkinto', jossa suoritustapa näyttö, ja 'nayttotutkintoonvalmistavakoulutus'" in {
          val opiskeluoikeus = ammatillinenOpiskeluoikeusNäyttötutkinnonJaNäyttöönValmistavanSuorituksilla()

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
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
          val tallennettuna = setupOppijaWithAndGetOpiskeluoikeus(opiskeluoikeus, opiskelija).withSuoritukset(
            List(autoalanPerustutkinnonSuoritus().copy(
              koulutusmoduuli = autoalanPerustutkinto.copy(
                tunniste = Koodistokoodiviite("457305", "koulutus")
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
          val tallennettuna = setupOppijaWithAndGetOpiskeluoikeus(opiskeluoikeus, henkilö = opiskelija).withSuoritukset(
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
          val tallennettuna = setupOppijaWithAndGetOpiskeluoikeus(opiskeluoikeus, henkilö = opiskelija).withSuoritukset(
            List(näyttötutkinnonSuoritus, näyttötutkintoonValmistavaSuoritus)
          )

          putOpiskeluoikeus(tallennettuna, henkilö = opiskelija) {
            verifyResponseStatusOk()
          }
        }
      }

      "Tutkinnon rakenteen vanheneminen" - {

        "Ei sallita siirtoa perusteen voimassaolon jälkeen alkaneelle keskeneräiselle opiskeluoikeudelle" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 331101, diaariNumero = "1000/011/2014", alkamispäivä = LocalDate.of(2022, 1, 1))
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))
          mockKoskiValidator(KoskiApplicationForTests.config).updateFieldsAndValidateAsJson(oppija).left.get should equal (KoskiErrorCategory.badRequest.validation.rakenne.perusteEiVoimassa())
        }

        "Sallitaan siirto ja täydennetään perusteen nimi oikein perusteen siirtymäajalla päättyneelle opiskeluoikeudelle" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "3000/011/2014",
            alkamispäivä = LocalDate.of(2018, 1, 1),
            päättymispäivä = LocalDate.of(2019, 7, 31)
          )
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))

          val validatedOppija = mockKoskiValidator(KoskiApplicationForTests.config).updateFieldsAndValidateAsJson(oppija)

          validatedOppija.isRight should equal (true)

          validatedOppija.right.get.opiskeluoikeudet(0).suoritukset(0).koulutusmoduuli.asInstanceOf[PerusteenNimellinen].perusteenNimi.get.get("fi") should be("Liiketalouden perustutkinto - päättymisajan testi 4")
        }

        "Ei sallita siirtoa perusteen siirtymäajan jälkeen päättyneelle opiskeluoikeudelle" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "3000/011/2014",
            alkamispäivä = LocalDate.of(2018, 1, 1),
            päättymispäivä = LocalDate.of(2019, 8, 1)
          )
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))

          mockKoskiValidator(KoskiApplicationForTests.config).updateFieldsAndValidateAsJson(oppija).left.get should equal (KoskiErrorCategory.badRequest.validation.rakenne.perusteEiVoimassa())
        }

        "Sallitaan siirto ja täydennetään perusteen nimi oikein perusteen voimassaoloaikana päättyneelle opiskeluoikeudelle, vaikka samalla diaarinumerolla löytyy luontipäivältään uudempi mutta päättynyt peruste" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "2000/011/2014",
            alkamispäivä = LocalDate.of(2018, 1, 1),
            päättymispäivä = LocalDate.of(2018, 7, 31)
          )
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))

          val validatedOppija = mockKoskiValidator(KoskiApplicationForTests.config).updateFieldsAndValidateAsJson(oppija)

          validatedOppija.isRight should equal (true)

          validatedOppija.right.get.opiskeluoikeudet(0).suoritukset(0).koulutusmoduuli.asInstanceOf[PerusteenNimellinen].perusteenNimi.get.get("fi") should be("Liiketalouden perustutkinto - päättymisajan testi 3")
        }

        "Sallitaan siirto ja läpäistään validaatio, vaikka samalla diaarinumerolla löytyy monta perustetta jotka ovat voimassa mutta kaikkiin ei validoidu" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "2000/011/2014",
            alkamispäivä = LocalDate.of(2016, 1, 1),
            päättymispäivä = LocalDate.of(2016, 8, 1)
          )
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))

          val validatedOppija = mockKoskiValidator(KoskiApplicationForTests.config).updateFieldsAndValidateAsJson(oppija)

          validatedOppija.isRight should equal (true)

          // Validoituu perusteeseen "Liiketalouden perustutkinto - päättymisajan testi 3", mutta ei perusteeseen "Liiketalouden perustutkinto - päättymisajan testi 2".
          // Kuitenkin perusteen nimi valitaan myöhemmän luotu-timestampin sisältävältä perusteelta "Liiketalouden perustutkinto - päättymisajan testi 2".
          validatedOppija.right.get.opiskeluoikeudet(0).suoritukset(0).koulutusmoduuli.asInstanceOf[PerusteenNimellinen].perusteenNimi.get.get("fi") should be("Liiketalouden perustutkinto - päättymisajan testi 2")
        }

        "Ei sallita siirtoa perusteen voimassaolon jälkeen päättyneelle opiskeluoikeudelle" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101, diaariNumero = "1000/011/2014",
            alkamispäivä = LocalDate.of(2017, 1, 1),
            päättymispäivä = LocalDate.of(2018, 8, 1)
          )
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))
          mockKoskiValidator(KoskiApplicationForTests.config).updateFieldsAndValidateAsJson(oppija).left.get should equal (KoskiErrorCategory.badRequest.validation.rakenne.perusteEiVoimassa())
        }

        "Ei validoida perusteen voimassaoloa tai rakennetta, jos diaarinumero löytyy koodistosta" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101, diaariNumero = "13/011/2009",
            alkamispäivä = LocalDate.of(2017, 1, 1),
            päättymispäivä = LocalDate.of(2099, 8, 1)
          )
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))

          val validatedOppija = mockKoskiValidator(KoskiApplicationForTests.config).updateFieldsAndValidateAsJson(oppija)
          validatedOppija.isRight should equal (true)
        }

        "Sallitaan siirto ja läpäistään validaatio tulevaisuudessa alkavalla opiskeluoikeudella, jos peruste on voimassa opiskeluoikeuden alkamisen päivänä" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "4000/011/2014",
            alkamispäivä = LocalDate.of(2066, 5, 12)
          )
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))
          val validatedOppija = mockKoskiValidator(KoskiApplicationForTests.config).updateFieldsAndValidateAsJson(oppija)
          validatedOppija.isRight should equal (true)
          validatedOppija.right.get.opiskeluoikeudet(0).suoritukset(0).koulutusmoduuli.asInstanceOf[PerusteenNimellinen].perusteenNimi.get.get("fi") should be("Liiketalouden perustutkinto - päättymisajan testi 5")
        }

        "Ei sallita siirtoa tulevaisuudessa alkavalle opiskeluoikeudelle, jos peruste ei ole voimassa opiskeluoikeuden alkamisen päivänä" in {
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "4000/011/2014",
            alkamispäivä = LocalDate.of(2066, 5, 11)
          )
          implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
          implicit val accessType = AccessType.write
          val oppija = Oppija(defaultHenkilö, List(opiskeluoikeus))
          mockKoskiValidator(KoskiApplicationForTests.config).updateFieldsAndValidateAsJson(oppija).left.get should equal (KoskiErrorCategory.badRequest.validation.rakenne.perusteEiVoimassa())
        }
      }
    }

    "Tutkinnon tila ja arviointi" - {
      def copySuoritus(v: Option[HenkilövahvistusValinnaisellaPaikkakunnalla], ap: Option[LocalDate] = None, keskiarvo: Option[Double] = None) = {
        val alkamispäivä = ap.orElse(tutkinnonOsaSuoritus.alkamispäivä)
        val suoritus = autoalanPerustutkinnonSuoritus().copy(vahvistus = v, alkamispäivä = alkamispäivä, keskiarvo = keskiarvo)
        v.map(_ => suoritus.copy(osasuoritukset = Some(List(muunAmmatillisenTutkinnonOsanSuoritus)))).getOrElse(suoritus)
      }

      def setup(s: AmmatillisenTutkinnonSuoritus)(f: => Unit) = {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(s)))(f)
      }

      "Vahvistus puuttuu, opiskeluoikeus voimassa" - {
        "palautetaan HTTP 200" in (setup(copySuoritus(None, None)) (
          verifyResponseStatusOk()
        ))
      }

      "Vahvistus puuttuu, opiskeluoikeus valmistunut" - {
        "palautetaan HTTP 400" in (putOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)).copy(suoritukset = List(copySuoritus(v = None)))) (
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/351301 puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut"))
        ))
      }

      "Suorituksella on vahvistus" - {
        "palautetaan HTTP 200" in (setup(copySuoritus(vahvistus(LocalDate.parse("2016-08-08")), keskiarvo = Some(4.0))) (
          verifyResponseStatusOk()
        ))
      }

      "Vahvistuksen myöntäjähenkilö puuttuu" - {
        "palautetaan HTTP 400" in (setup(copySuoritus(Some(HenkilövahvistusValinnaisellaPaikkakunnalla(LocalDate.parse("2016-08-08"), Some(helsinki), stadinOpisto, Nil)))) (
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lessThanMinimumNumberOfItems.*".r))
        ))
      }

      "Suorituksen päivämäärät" - {
        def päivämäärillä(alkamispäivä: String, vahvistuspäivä: String) = {
          copySuoritus(vahvistus(LocalDate.parse(vahvistuspäivä)), Some(LocalDate.parse(alkamispäivä)), keskiarvo = Some(4.0))
        }

        "Päivämäärät kunnossa" - {
          "palautetaan HTTP 200"  in (setup(päivämäärillä("2015-08-01", "2016-06-01"))(
            verifyResponseStatusOk()))
        }

        "alkamispäivä > vahvistus.päivä" - {
          "palautetaan HTTP 400"  in (setup(päivämäärillä("2016-08-01", "2015-05-31"))(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenAlkamispäivää("suoritus.alkamispäivä (2016-08-01) oltava sama tai aiempi kuin suoritus.vahvistus.päivä (2015-05-31)"))))
        }
      }

      "Keskiarvon asettaminen" - {
        val keskeneräinenSuoritusKeskiarvolla = autoalanPerustutkinnonSuoritus().copy(
          osasuoritukset = Some(List(tutkinnonOsaSuoritus)),
          keskiarvo = Some(4.0))

        "estetään jos suoritus on kesken" - {
          "palautetaan HTTP 400" in (setupTutkintoSuoritus(keskeneräinenSuoritusKeskiarvolla)(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.keskiarvoaEiSallitaKeskeneräiselleSuoritukselle("Suoritukselle ei voi asettaa keskiarvoa ellei suoritus ole päättynyt"))))
        }

        val valmisSuoritusKeskiarvolla = keskeneräinenSuoritusKeskiarvolla.copy(
          vahvistus = vahvistus(date(2017, 5, 31)),
          keskiarvo = Some(4.0))

        "sallitaan jos suoritus on valmis" - {
          "palautetaan HTTP 200" in (setupTutkintoSuoritus(valmisSuoritusKeskiarvolla)(
            verifyResponseStatusOk()))
        }

        val osasuoritusJaKeskiarvo = valmisSuoritusKeskiarvolla.copy(
          vahvistus = None,
          osasuoritukset = Some(List(tutkinnonOsaSuoritus))
        )

        val opiskeluoikeus = lisääTila(defaultOpiskeluoikeus.copy(suoritukset = List(osasuoritusJaKeskiarvo)), LocalDate.now().minusYears(1), opiskeluoikeusKatsotaanEronneeksi)

        "sallitaan jos tila on katsotaan eronneeksi mutta tutkinnon osa löytyy" - {
          "palautetaan HTTP 200" in (setupOppijaWithOpiskeluoikeus(opiskeluoikeus)(
            verifyResponseStatusOk()
          ))
        }

        "vaaditaan jos osittainen tutkinto valmis 1.1.2022 tai jälkeen" - {
          "palautetaan HTTP 400" in setupAmmatillinenPäätasonSuoritus(ammatillisenTutkinnonOsittainenAutoalanSuoritus.copy(keskiarvo = None, vahvistus = vahvistus(date(2022, 1, 1))))(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.valmiillaSuorituksellaPitääOllaKeskiarvo("Suorituksella pitää olla keskiarvo kun suoritus on valmis")))
        }

        "ei vaadita jos osittainen tutkinto valmis ennen 1.1.2022" - {
          "palautetaan HTTP 200" in setupAmmatillinenPäätasonSuoritus(ammatillisenTutkinnonOsittainenAutoalanSuoritus.copy(keskiarvo = None, vahvistus = vahvistus(date(2021, 12, 31))))(
            verifyResponseStatus(200))
        }

        "vaaditaan jos koko tutkinto valmis 15.1.2018 tai jälkeen" - {
          "palautetaan HTTP 400" in setupAmmatillinenPäätasonSuoritus(valmisSuoritusKeskiarvolla.copy(keskiarvo = None, vahvistus = vahvistus(date(2018, 1, 15))))(
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.valmiillaSuorituksellaPitääOllaKeskiarvo("Suorituksella pitää olla keskiarvo kun suoritus on valmis")))
        }

        "ei vaadita jos koko tutkinto valmis ennen 15.1.2018" - {
          "palautetaan HTTP 200" in setupAmmatillinenPäätasonSuoritus(valmisSuoritusKeskiarvolla.copy(keskiarvo = None, vahvistus = vahvistus(date(2018, 1, 14))))(
            verifyResponseStatus(200)
          )
        }
      }
    }

    "Ammatillinen perustutkinto opetussuunnitelman mukaisesti" - {
      "Tutkinnonosan ryhmä on määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, osasuoritukset = Some(List(tutkinnonOsaSuoritus)))
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Tutkinnonosan ryhmää ei ole määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, osasuoritukset = Some(List(tutkinnonOsaSuoritus.copy(tutkinnonOsanRyhmä = None))))
        "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tutkinnonOsanRyhmäPuuttuu("Tutkinnonosalta tutkinnonosat/100023 puuttuu tutkinnonosan ryhmä, joka on pakollinen ammatillisen perustutkinnon tutkinnonosille."))))
      }

      "Syötetään osaamisen hankkimistapa" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, osaamisenHankkimistapaOppilaitos))))
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Syötetään deprekoitu osaamisen hankkimistapa" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, deprekoituOsaamisenHankkimistapaOppilaitos))))
        "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.deprekoituOsaamisenHankkimistapa())))
      }

      "Syötetään koulutussopimus" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(koulutussopimukset = Some(List(koulutussopimusjakso)))
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Syötetään keskiarvo" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, vahvistus = vahvistus(date(2016, 9, 1)), osasuoritukset = Some(List(tutkinnonOsaSuoritus)), keskiarvo = Option(2.1f))
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Syötetään tieto siitä, että keskiarvo sisältää mukautettuja arvosanoja" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaOps, vahvistus = vahvistus(date(2016, 9, 1)), osasuoritukset = Some(List(tutkinnonOsaSuoritus)), keskiarvo = Option(2.1f), keskiarvoSisältääMukautettujaArvosanoja = Some(true))
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "suoritus.vahvistus.päivä > päättymispäivä" - {
        "palautetaan HTTP 400" in setupOppijaWithOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)).copy(
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
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk()))
      }

      "Tutkinnonosan ryhmää ei ole määritetty" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osasuoritukset = Some(List(tutkinnonOsaSuoritus.copy(tutkinnonOsanRyhmä = None))))
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk()))
      }

      "Syötetään osaamisen hankkimistapa" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, osaamisenHankkimistapaOppilaitos))))
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk()))
      }

      "Syötetään koulutussopimus" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, koulutussopimukset = Some(List(koulutussopimusjakso)))
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk()))
      }

      "Yritetty antaa keskiarvo" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaNäyttönä, osasuoritukset = Some(List(tutkinnonOsaSuoritus)), keskiarvo = Option(2.1f))
        "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*onlyWhenMismatch.*".r))))
      }

      "suoritus.vahvistus.päivä > päättymispäivä" - {
        val suoritus = autoalanPerustutkinnonSuoritus().copy(vahvistus = vahvistus(date(2017, 5, 31)), suoritustapa = suoritustapaNäyttö, osasuoritukset = Some(List(muunAmmatillisenTutkinnonOsanSuoritus)))
        "palautetaan HTTP 200" in setupOppijaWithOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 5, 31)).copy(suoritukset = List(suoritus)), opiskelija)(
          verifyResponseStatusOk()
        )
      }
    }

    "Reformin mukainen tutkinto" - {
      val opiskelija = defaultHenkilö.copy(hetu = "090373-474B")

      def reformiSuoritus = autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapaReformi)
      "Syötetään osaamisen hankkimistapa" - {
        val suoritus = reformiSuoritus.copy(osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, osaamisenHankkimistapaOppilaitos))))
        "palautetaan HTTP 200" in setupTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk())
      }

      "Syötetään koulutussopimus" - {
        val suoritus = reformiSuoritus.copy(koulutussopimukset = Some(List(koulutussopimusjakso)))
        "palautetaan HTTP 200" in setupTutkintoSuoritus(suoritus, opiskelija)(verifyResponseStatusOk())
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
          "palautetaan HTTP 200" in {
            mitätöiOppijanKaikkiOpiskeluoikeudet(opiskelija)
            putOppija(oppija(LocalDate.of(2018, 1, 1), suoritus()))(verifyResponseStatusOk())
          }
        }
        "Alkamispäivä 2019" - {
          "palautetaan HTTP 400" in {
            mitätöiOppijanKaikkiOpiskeluoikeudet(opiskelija)
            putOppija(oppija(LocalDate.of(2019, 1, 1), suoritus()))(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaaEiLöydyRakenteesta("Suoritustapaa ei löydy tutkinnon rakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle 39/011/2014 (612)")))
          }
        }
        "Alkamispäivä 2018, rakenne ei validi" - {
          val johtaminenJaHenkilöstönKehittäminen = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None)
          "palautetaan HTTP 400" in {
            mitätöiOppijanKaikkiOpiskeluoikeudet(opiskelija)
            putOppija(oppija(LocalDate.of(2018, 1, 1), suoritus(tutkinnonOsaSuoritus.copy(koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen))))(
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa tutkinnonosat/104052 ei löydy tutkintorakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle 39/011/2014 (612) - suoritustapa ops")))
          }
        }
      }

      "Korkeakouluopinnot" - {
        "Ei tarvitse arviointia" in {
          val suoritusIlmanArviointeja = korkeakouluopintoSuoritus.copy(osasuoritukset = korkeakouluopintoSuoritus.osasuoritukset.map(_.map(_.copy(arviointi = None))))
          setupTutkintoSuoritus(reformiSuoritus.copy(
            osasuoritukset = Some(List(suoritusIlmanArviointeja))
          ), opiskelija)(verifyResponseStatusOk())
        }
      }

      "Jatko-opintovalmiuksia tukevat opinnot" - {
        "Ei tarvitse arviointia" in {
          val suoritusIlmanArviointeja = jatkoOpintovalmiuksiaTukevienOpintojenSuoritus.copy(osasuoritukset = jatkoOpintovalmiuksiaTukevienOpintojenSuoritus.osasuoritukset.map(_.collect  { case l: LukioOpintojenSuoritus => l.copy(arviointi = None) }))
          setupTutkintoSuoritus(reformiSuoritus.copy(
            osasuoritukset = Some(List(suoritusIlmanArviointeja))
          ), opiskelija)(verifyResponseStatusOk())
        }
      }
    }

    "Valma" - {
      "suoritus.vahvistus.päivä > päättymispäivä" - {
        val suoritus = autoalanPerustutkinnonSuoritusValma().copy(vahvistus = vahvistus(date(2017, 5, 31)), osasuoritukset = Some(List(ExamplesValma.valmaKoulutukseenOrientoitumine)))
        "palautetaan HTTP 200" in setupOppijaWithOpiskeluoikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2018, 1, 1)).copy(suoritukset = List(suoritus)))(
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
        "palautetaan HTTP 200" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatusOk()))
      }

      "Tutkinnonosan ryhmä on määritetty" - {
        val suoritus = erikoisammattitutkintoSuoritus(tutkinnonOsanSuoritus)
        "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.koulutustyyppiEiSalliTutkinnonOsienRyhmittelyä("Tutkinnonosalle tutkinnonosat/104052 on määritetty tutkinnonosan ryhmä, vaikka kyseessä ei ole ammatillinen perustutkinto."))))
      }

      "Tutkinnonosan ryhmä on määritetty ja diaarinumero puuttuu" - {
        val suoritus = erikoisammattitutkintoSuoritus(tutkinnonOsanSuoritus)
        "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus.copy(koulutusmoduuli = suoritus.koulutusmoduuli.copy(perusteenDiaarinumero = None)))(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu())))
      }

      "Suoritustapana OPS" - {
        val suoritus = erikoisammattitutkintoSuoritus(tutkinnonOsanSuoritus.copy(tutkinnonOsanRyhmä = None)).copy(suoritustapa = suoritustapaOps)
        "palautetaan HTTP 400" in (setupTutkintoSuoritus(suoritus)(verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaaEiLöydyRakenteesta("Suoritustapaa ei löydy tutkinnon rakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle 40/011/2001 (1013059)"))))
      }
    }

    "Oppisopimus" - {
      def toteutusOppisopimuksella(yTunnus: String): AmmatillisenTutkinnonSuoritus = {
        autoalanPerustutkinnonSuoritus().copy(järjestämismuodot = Some(List(Järjestämismuotojakso(date(2012, 1, 1), None, OppisopimuksellinenJärjestämismuoto(Koodistokoodiviite("20", "jarjestamismuoto"), Oppisopimus(Yritys("Reaktor", yTunnus)))))))
      }

      "Kun ok" - {
        "palautetaan HTTP 200" in (
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(toteutusOppisopimuksella("1629284-5"))))
            (verifyResponseStatusOk())
        )
      }

      "Virheellinen y-tunnus" - {
        "palautetaan HTTP 400" in (
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(toteutusOppisopimuksella("1629284x5"))))
            (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*regularExpressionMismatch.*".r)))
        )
      }
    }

    "Muu ammatillinen" - {
      "Tutkinnon osaa pienempi kokonaisuus" - {
        "Paikallinen tutkinnon osaa pienempi kokonaisuus" - {
          val suoritus = kiinteistösihteerinTutkinnonOsaaPienempiMuuAmmatillinenKokonaisuus()
          "palautetaan HTTP 200" in setupTutkinnonOsaaPienempienKokonaisuuksienSuoritus(suoritus)(verifyResponseStatusOk())
        }
      }

      "Muu ammatillinen koulutus" - {
        "Paikallinen muu ammatillinen koulutus" - {
          val suoritus = kiinteistösihteerinMuuAmmatillinenKoulutus()
          "palautetaan HTTP 200" in setupMuuAmmatillinenKoulutusSuoritus(suoritus)(verifyResponseStatusOk())
        }

        "Ammatilliseen tehtävään valmistava koulutus" - {
          val suoritus = ansioJaLiikenneLentäjänMuuAmmatillinenKoulutus()
          "palautetaan HTTP 200" in setupMuuAmmatillinenKoulutusSuoritus(suoritus)(verifyResponseStatusOk())
        }
      }

      "Opintojen rahoitus" - {
        "lasna -tilalta vaaditaan opintojen rahoitus" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta lasna puuttuu rahoitusmuoto"))
          }
        }
        "loma -tilalta vaaditaan opintojen rahoitus" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLoma))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta loma puuttuu rahoitusmuoto"))
          }
        }
        "valmistunut -tilalta vaaditaan opintojen rahoitus" in {
          val tila = AmmatillinenOpiskeluoikeudenTila(List(
            AmmatillinenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
            AmmatillinenOpiskeluoikeusjakso(date(2016, 1, 1), opiskeluoikeusValmistunut)
          ))
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila, suoritukset = List(AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus))) {
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

        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.yhteinenTutkinnonOsaVVAI22())
        }
      }
      "Koodin VVAI22 saa tallentaa jos perusteen voimaantulon päivä on 1.8.2022 tai sen jälkeen" in {
        val suoritus = ajoneuvoalanPerustutkinnonSuoritus().copy(
          osasuoritukset = Some(List(yhtTutkinnonOsanSuoritusVVAI22("106727")))
        )

        setupOppijaWithOpiskeluoikeus(henkilö = KoskiSpecificMockOppijat.tyhjä, opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
          verifyResponseStatusOk()
        }
      }
    }

    "Duplikaattiopiskeluoikeuksien tunnistus" - {
      def testDuplicates(opiskeluoikeus: AmmatillinenOpiskeluoikeus): Unit = {
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus) {
          verifyResponseStatusOk()
        }
        postOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus) {
          verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
        }
      }

      val lähdejärjestelmänId1 = Some(primusLähdejärjestelmäId("primus-yksi"))
      val lähdejärjestelmänId2 = Some(primusLähdejärjestelmäId("primus-kaksi"))

      def setupOppijaWithOpiskeluoikeusAsPalvelukäyttäjä(oo: KoskeenTallennettavaOpiskeluoikeus)(f: => Unit): Unit = {
        setupOppijaWithOpiskeluoikeus(oo, defaultHenkilö, headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä) ++ jsonContent) {
          f
        }
      }

      def testConflictExists(opiskeluoikeus1: AmmatillinenOpiskeluoikeus, opiskeluoikeus2: AmmatillinenOpiskeluoikeus): Unit = {
        setupOppijaWithOpiskeluoikeusAsPalvelukäyttäjä(opiskeluoikeus1) {
          verifyResponseStatusOk()
        }
        postOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus2, headers = authHeaders(stadinAmmattiopistoPalvelukäyttäjä)  ++ jsonContent) {
          verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
        }
      }

      "Tutkinnon suoritus" - {
        "Duplikaatin tallennus ei onnistu, jos edellisen opiskeluoikeuden suoritus on kesken" in {
          resetFixtures()
          testDuplicates(defaultOpiskeluoikeus)
        }


        "Duplikaatin tallennus ei onnistu jos identtinen paitsi lähdejärjestelmän id" in {
          testConflictExists(
            defaultOpiskeluoikeus.copy(lähdejärjestelmänId = lähdejärjestelmänId1),
            defaultOpiskeluoikeus.copy(lähdejärjestelmänId = lähdejärjestelmänId2)
          )
        }

        "Duplikaatin tallennus ei onnistu, jos edellisen opiskeluoikeuden suoritus on päättynyt, mutta päivämäärät ovat päällekkäin" in {
          resetFixtures()
          testDuplicates(AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "3000/011/2014",
            alkamispäivä = LocalDate.of(2018, 1, 1),
            päättymispäivä = LocalDate.of(2019, 7, 31)
          ))
        }

        "Duplikaatin tallennus onnistuu, jos edellisen opiskeluoikeuden suoritus on päättynyt ja päivämäärät ovat erillään" in {
          resetFixtures()
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "3000/011/2014",
            alkamispäivä = LocalDate.of(2018, 1, 1),
            päättymispäivä = LocalDate.of(2018, 1, 31)
          )

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus) {
            verifyResponseStatusOk()
          }

          val toinenOpiskeluoiekus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "3000/011/2014",
            alkamispäivä = LocalDate.of(2018, 2, 1),
            päättymispäivä = LocalDate.of(2019, 7, 31)
          )

          postOpiskeluoikeus(opiskeluoikeus = toinenOpiskeluoiekus) {
            verifyResponseStatusOk()
          }
        }
      }

      "Tutkinnon osittainen suoritus" - {
        "Duplikaatin tallennus ei onnistu, kun edellisen opiskeluoikeuden suoritus on kesken" in {
          resetFixtures()
          testDuplicates(defaultOpiskeluoikeus.copy(suoritukset = List(ammatillisenTutkinnonOsittainenSuoritus)))
        }

        "Duplikaatin tallennus onnistuu, kun edellisen opiskeluoikeuden suoritus on päättynyt ja päivämäärät ovatkin päällekkäiset" in {
          resetFixtures()
          val opiskeluoikeus = AmmatillinenOpiskeluoikeusTestData.päättynytOpiskeluoikeus(
            MockOrganisaatiot.stadinAmmattiopisto,
            koulutusKoodi = 331101,
            diaariNumero = "3000/011/2014",
            alkamispäivä = LocalDate.of(2018, 1, 1),
            päättymispäivä = LocalDate.of(2019, 7, 31)
          ).copy(suoritukset = List(ammatillisenTutkinnonOsittainenSuoritus))

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus) {
            verifyResponseStatusOk()
          }
          postOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus) {
            verifyResponseStatusOk()
          }
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

  lazy val tutkinnonOsa: MuuValtakunnallinenTutkinnonOsa = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100023", "tutkinnonosat"), true, Some(LaajuusOsaamispisteissä(30)))
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

  def yhtTutkinnonOsanSuoritusVVAI22(koodiArvo: String = "101053") = yhteisenTutkinnonOsanSuoritus(koodiArvo, "Viestintä- ja vuorovaikutusosaaminen", k3, 4).copy(
    osasuoritukset = Some(List(
      YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVAI22", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(4))), arviointi = Some(List(arviointiKiitettävä)))
    )),
    vahvistus = None,
    arviointi = None,
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

  def setupTutkinnonOsaSuoritus[A](tutkinnonOsaSuoritus: AmmatillisenTutkinnonOsanSuoritus, tutkinnonSuoritustapa: Koodistokoodiviite, henkilö: Henkilö = defaultHenkilö)(f: => A) = {
    setupTutkintoSuoritus(withTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, tutkinnonSuoritustapa), henkilö)(f)
  }

  def setupTutkinnonOsaSuoritukset[A](tutkinnonOsaSuoritukset: List[AmmatillisenTutkinnonOsanSuoritus], tutkinnonSuoritustapa: Koodistokoodiviite)(f: => A) = {
    setupTutkintoSuoritus(withOsasuoritukset(tutkinnonOsaSuoritukset, tutkinnonSuoritustapa))(f)
  }

  def withTutkinnonOsaSuoritus(tutkinnonOsaSuoritus: AmmatillisenTutkinnonOsanSuoritus, tutkinnonSuoritustapa: Koodistokoodiviite): AmmatillisenTutkinnonSuoritus =
    withOsasuoritukset(List(tutkinnonOsaSuoritus), tutkinnonSuoritustapa)

  def withOsasuoritukset(osasuoritukset: List[AmmatillisenTutkinnonOsanSuoritus], tutkinnonSuoritustapa: Koodistokoodiviite): AmmatillisenTutkinnonSuoritus =
    autoalanPerustutkinnonSuoritus().copy(suoritustapa = tutkinnonSuoritustapa, osasuoritukset = Some(osasuoritukset))

  def setupTutkintoSuoritus[A](suoritus: AmmatillisenTutkinnonSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    setupAmmatillinenPäätasonSuoritus(suoritus, henkilö, headers)(f)
  }

  def setupTutkinnonOsaaPienempienKokonaisuuksienSuoritus[A](suoritus: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    setupAmmatillinenPäätasonSuoritus(suoritus, henkilö, headers)(f)
  }

  def setupMuuAmmatillinenKoulutusSuoritus[A](suoritus: MuunAmmatillisenKoulutuksenSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    setupAmmatillinenPäätasonSuoritus(suoritus, henkilö, headers)(f)
  }

  def setupAmmatillinenPäätasonSuoritus[A](suoritus: AmmatillinenPäätasonSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))

    setupOppijaWithOpiskeluoikeus(opiskeluoikeus, henkilö, headers)(f)
  }

  private def mockKoskiValidator(config: Config) = {
    new KoskiValidator(
      KoskiApplicationForTests.organisaatioRepository,
      KoskiApplicationForTests.possu,
      KoskiApplicationForTests.henkilöRepository,
      new EPerusteisiinPerustuvaValidator(
        KoskiApplicationForTests.ePerusteet,
        KoskiApplicationForTests.tutkintoRepository,
        KoskiApplicationForTests.koodistoViitePalvelu,
        config
      ),
      new EPerusteetLops2019Validator(KoskiApplicationForTests.config, KoskiApplicationForTests.ePerusteet),
      new EPerusteetFiller(
        KoskiApplicationForTests.ePerusteet,
        KoskiApplicationForTests.tutkintoRepository,
        KoskiApplicationForTests.koodistoViitePalvelu
      ),
      KoskiApplicationForTests.validatingAndResolvingExtractor,
      KoskiApplicationForTests.suostumuksenPeruutusService,
      KoskiApplicationForTests.koodistoViitePalvelu,
      config,
      KoskiApplicationForTests.validationContext,
    )
  }

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(koulutusmoduuli = autoalanPerustutkinnonSuoritus().koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  override def vääräntyyppisenPerusteenDiaarinumero: String = "60/011/2015"
  override def vääräntyyppisenPerusteenId: Long = 1372910
  def eperusteistaLöytymätönValidiDiaarinumero: String = "13/011/2009"
}
