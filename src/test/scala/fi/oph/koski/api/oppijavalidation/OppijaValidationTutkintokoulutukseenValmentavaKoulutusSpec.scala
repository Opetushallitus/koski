package fi.oph.koski.api.oppijavalidation

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.documentation.ExamplesTutkintokoulutukseenValmentavaKoulutus._
import fi.oph.koski.documentation.{ExampleData, LukioExampleData}
import fi.oph.koski.eperusteetvalidation.{EPerusteetFiller, EPerusteetLops2019Validator, EPerusteisiinPerustuvaValidator}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoTallentaja
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationTutkintokoulutukseenValmentavaKoulutusSpec extends TutkinnonPerusteetTest[TutkintokoulutukseenValmentavanOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[TutkintokoulutukseenValmentavanOpiskeluoikeus]]

  override val defaultHenkilö: UusiHenkilö = asUusiOppija(KoskiSpecificMockOppijat.tuva)

  val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
  val accessType = AccessType.write

  "Tutkintokoulutukseen valmentava koulutus" - {
    resetFixtures()

    "Opiskeluoikeus" - {
      def ilmanRahoitusta(oo: TutkintokoulutukseenValmentavanOpiskeluoikeus, tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila) = {
        val viimeisinJakso = tila.opiskeluoikeusjaksot.last.copy(opintojenRahoitus = None)
        oo.copy(tila = tila.copy(
          opiskeluoikeusjaksot = tila.opiskeluoikeusjaksot.dropRight(1):+viimeisinJakso
        ))
      }

      "jaksolla pitää olla rahoitusmuoto kun tila on läsnä" in {
        setupOppijaWithOpiskeluoikeus(ilmanRahoitusta(tuvaOpiskeluOikeusEiValmistunut, tuvaTilaLäsnä), henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto())
        }
      }

      "jaksolla pitää olla rahoitusmuoto kun tila on valmistunut" in {
        setupOppijaWithOpiskeluoikeus(ilmanRahoitusta(tuvaOpiskeluOikeusValmistunut, tuvaTilaValmistunut), henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto())
        }
      }

      "jaksolla pitää olla rahoitusmuoto kun tila on loma" in {
        setupOppijaWithOpiskeluoikeus(ilmanRahoitusta(tuvaOpiskeluOikeusLoma, tuvaTilaLoma).copy(
          järjestämislupa = Koodistokoodiviite("ammatillinen", "tuvajarjestamislupa")
        ), henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto())
        }
      }
    }

    "Suoritukset" - {
      "valmistuneen päätason suorituksen kesto ja osasuoritukset vaatimusten mukaiset" in {
        setupOppijaWithOpiskeluoikeus(tuvaOpiskeluOikeusValmistunut, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "keskeneräisen päätason suorituksen kesto ja osasuoritukset vaatimusten mukaiset" in {
        setupOppijaWithOpiskeluoikeus(tuvaOpiskeluOikeusEiValmistunut, henkilö = tuvaHenkilöEiValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "suoritusten laajuus sallitaan kun se vastaa osasuoritusten summaa" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = None).copy( // laajuus lasketaan ja täytetään automaattisesti
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenValinnaisenOsanSuoritus(
                  arviointiPäivä = Some(date(2021, 9, 1)),
                  laajuus = Some(8)
                ).copy(
                  osasuoritukset = Some(
                    List(
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 1",
                        paikallinenKoodi = "ohj1",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ),
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 2",
                        paikallinenKoodi = "ohj2",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ),
                    )
                  )
                )
              )
            )
          )))


        val tuva = setupOppijaWithAndGetOpiskeluoikeus(oo, tuvaHenkilöValmis)
        tuva.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe 12.0
        tuva.suoritukset.head.osasuoritusLista.last.koulutusmoduuli.laajuusArvo(0.0) shouldBe 8.0
      }

      "suoritusten laajuuteen lasketaan mukaan myös muut kuin hyväksytyt suoritukset ennen rajapäivää" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = None).copy( // laajuus lasketaan ja täytetään automaattisesti
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ).copy(arviointi = tuvaSanallinenArviointiHylätty(Some(date(2021, 9, 1)))),
                tuvaKoulutuksenValinnaisenOsanSuoritus(
                  arviointiPäivä = Some(date(2021, 9, 1)),
                  laajuus = Some(8)
                ).copy(
                  osasuoritukset = Some(
                    List(
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 1",
                        paikallinenKoodi = "ohj1",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ),
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 2",
                        paikallinenKoodi = "ohj2",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ).copy(arviointi = tuvaSanallinenArviointiHylätty(Some(date(2021, 12, 1)))),
                    )
                  )
                )
              )
            )
          )))

        val validator = mockKoskiValidator(LocalDate.now.plusDays(1))
        val res = validator.updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType).right.get
        res.opiskeluoikeudet.size shouldBe 1

        val tuva = res.opiskeluoikeudet.head
        tuva.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe 12.0
        tuva.suoritukset.head.osasuoritusLista.last.koulutusmoduuli.laajuusArvo(0.0) shouldBe 8.0
      }

      "suoritusten laajuuden validointi tuottaa virheen ennen rajapäivää jos osasuorituksen laajuuteen ei ole sisällytetty hylättyjä suorituksia" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = None).copy( // laajuus lasketaan ja täytetään automaattisesti
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ).copy(arviointi = tuvaSanallinenArviointiHylätty(Some(date(2021, 9, 1)))),
                tuvaKoulutuksenValinnaisenOsanSuoritus(
                  arviointiPäivä = Some(date(2021, 9, 1)),
                  laajuus = Some(4) // Tämä laajuus pitäisi olla 8 kun lasketaan mukaan hylätyt aliosasuoritukset
                ).copy(
                  osasuoritukset = Some(
                    List(
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 1",
                        paikallinenKoodi = "ohj1",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ),
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 2",
                        paikallinenKoodi = "ohj2",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ).copy(arviointi = tuvaSanallinenArviointiHylätty(Some(date(2021, 12, 1)))),
                    )
                  )
                )
              )
            )
          )))

        val validator = mockKoskiValidator(LocalDate.now.plusDays(1))
        val res = validator.updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType).left.get
        res shouldBe KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Suorituksen koulutuksenosattuva/104 osasuoritusten laajuuksien summa 8.0 ei vastaa suorituksen laajuutta 4.0")
      }

      "suoritusten laajuuteen ei lasketa mukaan kuin hyväksytyt suoritukset rajapäivän jälkeen" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = None).copy( // laajuus lasketaan ja täytetään automaattisesti
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ).copy(arviointi = tuvaSanallinenArviointiHylätty(Some(date(2021, 9, 1)))),
                tuvaKoulutuksenValinnaisenOsanSuoritus(
                  arviointiPäivä = Some(date(2021, 9, 1)),
                  laajuus = Some(4)
                ).copy(
                  osasuoritukset = Some(
                    List(
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 1",
                        paikallinenKoodi = "ohj1",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ),
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 2",
                        paikallinenKoodi = "ohj2",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ).copy(arviointi = tuvaSanallinenArviointiHylätty(Some(date(2021, 12, 1)))),
                    )
                  )
                )
              )
            )
          )))

        val validator = mockKoskiValidator(LocalDate.now.minusDays(1))
        val res = validator.updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType).right.get
        res.opiskeluoikeudet.size shouldBe 1

        val tuva = res.opiskeluoikeudet.head
        tuva.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe 6.0
        tuva.suoritukset.head.osasuoritusLista.last.koulutusmoduuli.laajuusArvo(0.0) shouldBe 4.0
      }

      "suoritusten laajuus sallitaan ilman osasuorituksia" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = None).copy( // laajuus lasketaan ja täytetään automaattisesti
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenValinnaisenOsanSuoritus(
                  arviointiPäivä = Some(date(2021, 9, 1)),
                  laajuus = Some(6)
                )
              )
            )
          ))
        )

        val tuva = setupOppijaWithAndGetOpiskeluoikeus(oo, tuvaHenkilöValmis)
        tuva.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe 10.0
        tuva.suoritukset.head.osasuoritusLista.last.koulutusmoduuli.laajuusArvo(0.0) shouldBe 6.0
      }

      "suoritusten laajuus validoidaan jos osasuorituksia" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = None).copy( // laajuus validoidaan jos syötetty myös osasuorituksille
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenValinnaisenOsanSuoritus(
                  arviointiPäivä = Some(date(2021, 9, 1)),
                  laajuus = Some(1) // should throw
                ).copy(
                  osasuoritukset = Some(List(
                    tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                      kurssinNimi = "Ohjelmointi 1",
                      paikallinenKoodi = "ohj1",
                      paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                      laajuusViikoissa = 1
                    ),
                    tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                      kurssinNimi = "Ohjelmointi 2",
                      paikallinenKoodi = "ohj2",
                      paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                      laajuusViikoissa = 1
                    ),
                  )))
            )
          ))
        ))

        setupOppijaWithOpiskeluoikeus(
          oo,
          henkilö = tuvaHenkilöValmis,
          headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.tuvaPäätasonSuoritusVääräLaajuus(), KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Suorituksen koulutuksenosattuva/104 osasuoritusten laajuuksien summa 2.0 ei vastaa suorituksen laajuutta 1.0"))
        }
      }

      "valmistuneen päätason suorituksen laajuus liian pieni (ja osasuorituksia puuttuu)" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(2)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        val resEnnen = mockKoskiValidator(LocalDate.now.plusDays(1)).updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType).left.get
        resEnnen shouldBe KoskiErrorCategory.badRequest.validation.laajuudet.tuvaPäätasonSuoritusVääräLaajuus()

        val resJälkeen = mockKoskiValidator(LocalDate.now.minusDays(1)).updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType)
        resJälkeen.isRight shouldBe true
      }

      "valmistuneen päätason suorituksen laajuus liian suuri" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(201)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(20)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(179)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        val resEnnen = mockKoskiValidator(LocalDate.now.plusDays(1)).updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType).left.get
        resEnnen shouldBe KoskiErrorCategory.badRequest.validation.laajuudet.tuvaPäätasonSuoritusVääräLaajuus()

        val resJälkeen = mockKoskiValidator(LocalDate.now.minusDays(1)).updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType).left.get
        resJälkeen shouldBe KoskiErrorCategory.badRequest.validation.laajuudet.tuvaOsaSuoritusVääräLaajuus("Tutkintokoulutukseen valmentavan koulutuksen arjen ja yhteiskunnallisen osallisuuden taitojen osasuorituksen laajuus on oltava enintään 20 viikkoa.")
      }

      "valmistuneen päätason suorituksen osasuorituksen laajuus liian pieni" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(4)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(1)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(1)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(
            expectedStatus = 400,
            KoskiErrorCategory.badRequest.validation.laajuudet.tuvaOsaSuoritusVääräLaajuus(
              "Tutkintokoulutukseen valmentavan koulutuksen opiskelu- ja urasuunnittelutaitojen osasuorituksen laajuus on oltava vähintään 2 ja enintään 10 viikkoa."
            )
          )
        }
      }

      "valmistuneen päätason suorituksesta puuttuu opiskelu ja urasuunnittelutaitojen osasuoritus" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(4)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuvaOpiskeluJaUrasuunnittelutaitojenOsasuoritusPuuttuu())
        }
      }

      "valmistuneen päätason suorituksella on hylätty opiskelu ja urasuunnittelutaitojen osasuoritus" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(4)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ).copy(arviointi = tuvaSanallinenArviointiHylätty(Some(date(2021, 9, 1)))),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        val validator = mockKoskiValidator(LocalDate.now.minusDays(1))
        val res = validator.updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType).left.get
        res shouldBe KoskiErrorCategory.badRequest.validation.rakenne.tuvaOpiskeluJaUrasuunnittelutaitojenOsasuoritusPuuttuu()
      }

      "valmistuneen päätason suorituksella on ensin hylätyn ja sitten hyväksytyn arvosanan opiskelu ja urasuunnittelutaitojen osasuoritus" in {
        val osasuoritus = tuvaKoulutuksenMuunOsanSuoritus(
          koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
          koodistoviite = "tutkintokoulutukseenvalmentava",
          arviointiPäivä = Some(date(2021, 9, 1))
        )
        val hylättyArviointi = tuvaSanallinenArviointiHylätty(Some(date(2021, 8, 15))).get
        val molemmatArvioinnit = Some(hylättyArviointi ++ osasuoritus.arviointi.get)

        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(4)).copy(
            osasuoritukset = Some(
              List(
                osasuoritus.copy(arviointi = molemmatArvioinnit),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        val validator = mockKoskiValidator(LocalDate.now.minusDays(1))
        val res = validator.updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType)
        res.isRight shouldBe true
      }

      "valmistuneen päätason suorituksesta puuttuu riittävä määrä eri osasuorituksia" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(4)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        val resEnnen = mockKoskiValidator(LocalDate.now.plusDays(1)).updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType).left.get
        resEnnen shouldBe KoskiErrorCategory.badRequest.validation.rakenne.tuvaOsasuorituksiaLiianVähän()

        val resJälkeen = mockKoskiValidator(LocalDate.now.minusDays(1)).updateFieldsAndValidateAsJson(Oppija(tuvaHenkilöValmis, List(oo)))(session, accessType)
        resJälkeen.isRight shouldBe true
      }

      "koulutustyyppi täydennetään automaattisesti perusteista" in {

        val defaultKoulutustyyppi = defaultOpiskeluoikeus.suoritukset.head.koulutusmoduuli match {
          case k: TutkintokoulutukseenValmentavanKoulutus => k.koulutustyyppi.map(_.koodiarvo)
        }
        defaultKoulutustyyppi shouldBe None

        val tuva = setupOppijaWithAndGetOpiskeluoikeus(defaultOpiskeluoikeus, tuvaHenkilöValmis)
        val täydennettyKoulutustyyppi = tuva.suoritukset.head.koulutusmoduuli match {
          case k: TutkintokoulutukseenValmentavanKoulutus => k.koulutustyyppi.map(_.koodiarvo)
        }
        täydennettyKoulutustyyppi shouldBe Some("40")
      }
    }

    "Katsotaan eronneeksi tilaan päättyneellä opiskeluoikeudella ei saa olla arvioimattomia osasuorituksia" in {
      val oo = tuvaOpiskeluOikeusValmistunut.copy(
        tila = tuvaTilaKatsotaanEronneeksi,
        suoritukset = List(
          tuvaPäätasonSuoritus(laajuus = Some(12)).copy(
            vahvistus = None,
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaAmmatillisenKoulutuksenOpinnot(laajuus = Some(1)),
                  arviointiPäivä = Some(date(2021, 10, 1)),
                  koodistoviite = "tuvaammatillinenkoulutus"
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaLukiokoulutuksenOpinnot(laajuus = Some(1)),
                  arviointiPäivä = None,
                  koodistoviite = "tuvalukiokoulutus"
                ).copy(
                  tunnustettu = Some(
                    OsaamisenTunnustaminen(
                      osaaminen = Some(
                        LukioExampleData.kurssisuoritus(
                          LukioExampleData.valtakunnallinenKurssi("ENA1")
                        ).copy(arviointi = LukioExampleData.numeerinenArviointi(8))
                      ),
                      selite = finnish("Tunnustettu lukion kurssi")
                    )
                  )
                )
              )
            )
          )
        )
      )

      setupOppijaWithOpiskeluoikeus(oo, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.tila.eronneeksiKatsotunOpiskeluoikeudenArvioinnit(
            "Katsotaan eronneeksi -tilaan päättyvällä opiskeluoikeudella ei saa olla osasuorituksia, joista puuttuu arviointi"
          ))
      }
    }

    "Opiskeluoikeudet" - {
      "opiskeluoikeuden järjestämislupa ei saa muuttua opiskeluoikeuden luonnin jälkeen" in {
        val tallennettuTuvaOpiskeluOikeusEiValmistunut = setupOppijaWithAndGetOpiskeluoikeus(tuvaOpiskeluOikeusEiValmistunut, tuvaHenkilöEiValmis)

        putOpiskeluoikeus(
          tallennettuTuvaOpiskeluOikeusEiValmistunut
            .copy(järjestämislupa = Koodistokoodiviite("ammatillinen", "tuvajarjestamislupa"), lisätiedot = None),
          henkilö = tuvaHenkilöEiValmis,
          headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent
        ) {
          verifyResponseStatus(
            400,
            KoskiErrorCategory
              .badRequest(
                "Olemassaolevan tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeuden järjestämislupaa ei saa muuttaa."
              )
          )
        }
      }
    }

    "Opiskeluoikeuden tila" - {
      """Opiskeluoikeuden tilaa "eronnut" ei saa käyttää""" in {
        // Tämä tilan tarkistus on tehty validaationa eikä tietomalliin siksi, että tuotantoon ehti livahtaa väärää dataa.
        setupOppijaWithOpiskeluoikeus(
          tuvaOpiskeluOikeusValmistunut.copy(tila = TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
            opiskeluoikeusjaksot = List(
              tuvaOpiskeluOikeusjakso(date(2021, 8, 1), "lasna"),
              tuvaOpiskeluOikeusjakso(date(2022, 8, 1), "eronnut")
            )
          )),
          henkilö = tuvaHenkilöValmis,
          headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent
        ) {
          verifyResponseStatus(
            400,
            KoskiErrorCategory
              .badRequest.validation.tila.tuvaSuorituksenOpiskeluoikeidenTilaVääräKoodiarvo(
              """Opiskeluoikeuden tila "Eronnut" ei ole sallittu tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeudessa. Käytä tilaa "Katsotaan eronneeksi"."""
            )
          )
        }
      }

      """Opiskeluoikeuden tila "loma" sallitaan, kun opiskeluoikeuden järjestämislupa on ammatillisen koulutuksen järjestämisluvan piirissä""" in {
        setupOppijaWithOpiskeluoikeus(
          tuvaOpiskeluOikeusLoma.copy(
            järjestämislupa = Koodistokoodiviite("ammatillinen", "tuvajarjestamislupa"),
            lisätiedot = Some(TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
              maksuttomuus = Some(
                List(
                  Maksuttomuus(
                    alku = date(2021, 8, 1),
                    loppu = None,
                    maksuton = true
                  )
                )
              )
            ))),
          henkilö = tuvaHenkilöLoma,
          headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent
        ) {
          verifyResponseStatusOk()
        }
      }
      """Opiskeluoikeuden tila ei saa olla "loma", kun opiskeluoikeuden järjestämislupa on jokin muu kuin ammatillisen koulutuksen järjestämisluvan piirissä""" in {
        setupOppijaWithOpiskeluoikeus(
          tuvaOpiskeluOikeusLoma.copy(
            lisätiedot = Some(
              TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot(
                maksuttomuus = Some(
                  List(
                    Maksuttomuus(
                      alku = date(2021, 8, 1),
                      loppu = None,
                      maksuton = true
                    )
                  )
                )
              ))),
          henkilö = tuvaHenkilöLoma,
          headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent
        ) {
          verifyResponseStatus(
            400,
            KoskiErrorCategory
              .badRequest.validation.tila.tuvaSuorituksenOpiskeluoikeidenTilaVääräKoodiarvo(
              """Tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeuden tila ei voi olla "loma", jos opiskeluoikeuden järjestämislupa ei ole ammatillisen koulutuksen järjestämisluvan piirissä."""
            )
          )
        }
      }
    }

    "Deserialisointi osaa päätellä skeeman ja täydentää optionaaliset @DefaultValuella annotoidut kentät, jos niiltä puuttuu arvo" in {
      val json = JsonFiles.readFile("src/test/resources/opiskeluoikeus_puuttuvilla_defaultvalue_propertyilla.json")
      val ooVersiot = putOppija(json) {
        verifyResponseStatusOk()
        JsonSerializer.parse[HenkilönOpiskeluoikeusVersiot](response.body)
      }
      val oo = getOpiskeluoikeus(ooVersiot.opiskeluoikeudet.last.oid)
      val lisätiedot = oo.lisätiedot.get.asInstanceOf[TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot]
      lisätiedot.pidennettyPäättymispäivä should equal(Some(false))
      lisätiedot.koulutusvienti should equal(Some(false))
    }

    "Erityisen tuen ja vammaisuuden jaksot perusopetuksen järjestämisluvalla" - {
      val alku = LocalDate.of(2021, 8, 5)

      val opiskeluoikeus = tuvaOpiskeluOikeusEiValmistunut
      val lisätiedot = opiskeluoikeus.lisätiedot.get.asInstanceOf[TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot].copy(
        maksuttomuus = None
      )

      "Validointi onnistuu, kun opiskeluoikeus sisältää erityisen tuen päätöksen mutta ei vammaisuusjaksoja" in {
        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(List(
                TuvaErityisenTuenPäätös(
                  alku = Some(alku),
                  loppu = None
                ),
              )),
              vammainen = None,
              vaikeastiVammainen = None
            )
          )
        )

        validate(oo).isRight should equal(true)
      }

      "Validointi onnistu, kun vammaisuusjaksoja on lomittain ja ne kaikki osuvat johonkin lomittaiseen erityisen tuen päätökseen" in {
        val jakso1 = Aikajakso(alku.plusDays(0), Some(alku.plusDays(4)))
        val jakso2 = Aikajakso(alku.plusDays(3), Some(alku.plusDays(7)))
        val jakso3 = Aikajakso(alku.plusDays(6), Some(alku.plusDays(10)))

        val erityisenTuenPäätökset = List(jakso1, jakso2, jakso3).map(j => TuvaErityisenTuenPäätös.apply(Some(j.alku), j.loppu))

        val jakso4 = Aikajakso(alku.plusDays(1), Some(alku.plusDays(5)))
        val jakso5 = Aikajakso(alku.plusDays(9), Some(alku.plusDays(10)))

        val vammainenJaksot = List(jakso4, jakso5)

        val jakso6 = Aikajakso(alku.plusDays(6), Some(alku.plusDays(8)))
        val jakso7 = Aikajakso(alku.plusDays(8), Some(alku.plusDays(8)))

        val vaikeastiVammainenJaksot = List(jakso6, jakso7)

        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(erityisenTuenPäätökset),
              vammainen = Some(vammainenJaksot),
              vaikeastiVammainen = Some(vaikeastiVammainenJaksot)
            )
          )
        )

        validate(oo).isRight should equal(true)
      }

      "Validointi ei onnistu, kun opiskeluoikeus sisältää osittain päällekäiset eri vammaisuuden jaksot" in {
        val jakso1 = Aikajakso(alku, None)
        val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(12)))

        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(List(
                TuvaErityisenTuenPäätös(
                  alku = Some(alku),
                  loppu = None
                ),
              )),
              vammainen = Some(List(
                jakso1
              )),
              vaikeastiVammainen = Some(List(
                jakso2
              ))
            )
          )
        )

        validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Vaikeasti vammaisuuden ja muun kuin vaikeasti vammaisuuden aikajaksot eivät voi olla voimassa samana päivänä"))
      }

      "Validointi onnistuu ennen rajapäivää, vaikka opiskeluoikeus sisältää osittain päällekäiset eri vammaisuuden jaksot" in {
        val jakso1 = Aikajakso(alku, None)
        val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(12)))

        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(List(
                TuvaErityisenTuenPäätös(
                  alku = Some(alku),
                  loppu = None
                ),
              )),
              vammainen = Some(List(
                jakso1
              )),
              vaikeastiVammainen = Some(List(
                jakso2
              ))
            )
          )
        )

        validate(oo, 1).isRight should be(true)
      }

      "Validointi ei onnistu, kun vammaisuusjaksoja on osittain erityisen tuen päätösten ulkopuolella" in {
        val jakso1 = Aikajakso(alku, None)
        val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(12)))

        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(List(
                TuvaErityisenTuenPäätös(
                  alku = Some(jakso2.alku),
                  loppu = jakso2.loppu
                ),
              )),
              vammainen = Some(List(
                jakso1
              ))
            )
          )
        )

        validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Vammaisuusjaksot sisältävät päiviä, joina ei ole voimassaolevaa erityisen tuen jaksoa"))
      }

      "Ei voi tallentaa väärällä perusteen diaarinumerolla" in {
        val oo = opiskeluoikeus.copy(
          suoritukset = List(TutkintokoulutukseenValmentavanKoulutuksenSuoritus(
            toimipiste = stadinAmmattiopisto,
            koulutusmoduuli = TutkintokoulutukseenValmentavanKoulutus(
              perusteenDiaarinumero = Some("OPH-5410-2021"), // ajoneuvoalan perustutkinto
              laajuus = None
            ),
            vahvistus = None,
            suorituskieli = ExampleData.suomenKieli,
            osasuoritukset = None
          ))
        )
        validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi("Suoritukselle TutkintokoulutukseenValmentavanKoulutus ei voi käyttää opiskeluoikeuden voimassaoloaikana voimassaollutta perustetta OPH-5410-2021 (7614470), jonka koulutustyyppi on 1(Ammatillinen perustutkinto). Tälle suoritukselle hyväksytyt perusteen koulutustyypit ovat 40(Tutkintokoulutukseen valmentava koulutus TUVA))."))
      }

      def validate(oo: Opiskeluoikeus, voimaanastumispäivänOffsetTästäPäivästä: Long = 0): Either[HttpStatus, Oppija] = {
        val oppija = Oppija(defaultHenkilö, List(oo))

        implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
        implicit val accessType = AccessType.write

        val config = KoskiApplicationForTests.config.withValue("validaatiot.pidennetynOppivelvollisuudenYmsValidaatiotAstuvatVoimaan", fromAnyRef(LocalDate.now().plusDays(voimaanastumispäivänOffsetTästäPäivästä).toString))

        mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija)
      }

      def mockKoskiValidator(config: Config) = {
        new KoskiValidator(
          KoskiApplicationForTests.organisaatioRepository,
          KoskiApplicationForTests.possu,
          KoskiApplicationForTests.henkilöRepository,
          KoskiApplicationForTests.ePerusteetValidator,
          KoskiApplicationForTests.ePerusteetLops2019Validator,
          KoskiApplicationForTests.ePerusteetFiller,
          KoskiApplicationForTests.validatingAndResolvingExtractor,
          KoskiApplicationForTests.suostumuksenPeruutusService,
          KoskiApplicationForTests.koodistoViitePalvelu,
          config,
          KoskiApplicationForTests.validationContext,
        )
      }

    }
  }

  override def defaultOpiskeluoikeus: TutkintokoulutukseenValmentavanOpiskeluoikeus = tuvaOpiskeluOikeusValmistunut

  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): TutkintokoulutukseenValmentavanOpiskeluoikeus = {
    tuvaOpiskeluOikeusValmistunut.withSuoritukset(
      tuvaOpiskeluOikeusValmistunut.suoritukset.map {
        case s: TutkintokoulutukseenValmentavanKoulutuksenSuoritus =>
          s.withKoulutusmoduuli(s.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
      }
    ) match {
      case t: TutkintokoulutukseenValmentavanOpiskeluoikeus => t
    }
  }

  override def eperusteistaLöytymätönValidiDiaarinumero: String = "6/011/2015"

  private def mockKoskiValidator(rajapäivä: LocalDate) = {
    val config = KoskiApplicationForTests.config.withValue("validaatiot.tuvaLaajuusValidaatioMuutoksetAstuvatVoimaan", fromAnyRef(rajapäivä.toString))
    new KoskiValidator(
      KoskiApplicationForTests.organisaatioRepository,
      KoskiApplicationForTests.possu,
      KoskiApplicationForTests.henkilöRepository,
      new EPerusteisiinPerustuvaValidator(
        KoskiApplicationForTests.ePerusteet,
        KoskiApplicationForTests.tutkintoRepository,
        KoskiApplicationForTests.koodistoViitePalvelu
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
}
