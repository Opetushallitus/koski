package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.AmmatillinenOsittainenUseistaTutkinnoista
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritusSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[AmmatillinenOpiskeluoikeus] with BeforeAndAfterAll with KoskiHttpSpec {

  def tag = implicitly[reflect.runtime.universe.TypeTag[AmmatillinenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo)

  def makeOpiskeluoikeus(
    alkamispäivä: LocalDate = longTimeAgo,
    oppilaitos: Oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
    suoritus: AmmatillinenPäätasonSuoritus = osittainenSuoritusKesken,
    tila: Option[Koodistokoodiviite] = None
  ) = AmmatillinenOpiskeluoikeus(
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))
      ) ++ tila.map(t => AmmatillinenOpiskeluoikeusjakso(date(2023, 12, 31), t, Some(valtionosuusRahoitteinen))).toList
    ),
    oppilaitos = Some(oppilaitos),
    suoritukset = List(suoritus)
  )

  def osittainenSuoritusKesken: AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus = ammatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
    .copy(vahvistus = None, keskiarvo = None)

  "Ammatillisen koulutuksen opiskeluoikeuden lisääminen" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }
    }

    // Kopioitu soveltaen OppijaValidationAmmatillisenTutkinnonOsittainenSuoritusSpecistä
    "Tutkinnon tila ja arviointi" - {
      def copySuoritus(ap: Option[LocalDate] = None, vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None, keskiarvo: Option[Double] = None) = {
        val alkamispäivä = ap.orElse(Some(longTimeAgo))
        osittainenSuoritusKesken.copy(alkamispäivä = alkamispäivä, vahvistus = vahvistus, keskiarvo = keskiarvo)
      }

      def setupOppijWith(s: AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus)(f: => Unit) = {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(s)))(f)
      }

      "Kun vahvistus puuttuu" - {
        "palautetaan HTTP 200" in (setupOppijWith(copySuoritus()) (
          verifyResponseStatusOk()
        ))
      }

      "Kun vahvistus on annettu" - {
        "palautetaan HTTP 200" in (setupOppijWith(copySuoritus(vahvistus = vahvistus(LocalDate.now), keskiarvo = Some(4.0))) (
          verifyResponseStatusOk()
        ))
      }

      "Suorituksen päivämäärät" - {
        def päivämäärillä(alkamispäivä: String, vahvistuspäivä: String) = {
          copySuoritus(ap = Some(LocalDate.parse(alkamispäivä)), vahvistus = vahvistus(LocalDate.parse(vahvistuspäivä)), keskiarvo = Some(4.0))
        }

        "Päivämäärät kunnossa" - {
          "palautetaan HTTP 200"  in (setupOppijWith(päivämäärillä("2015-08-01", "2016-06-01"))(
            verifyResponseStatusOk()))
        }
      }
    }

    "Osaamisaloilla" - {
      "Voi syöttää ilman osaamisalaa" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatusOk()
        }
      }

      "Voi syöttää osaamisalan, kun ei ole osasuorituksia" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1008", Some(finnish("Ajoneuvotekniikan osaamisala")), "osaamisala", None)))),
          osasuoritukset = None,
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatusOk()
        }
      }

      "Voi syöttää osaamisalan, kun löytyy jokin osaamisalaa vastaava tutkinto osasuorituksista" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1008", Some(finnish("Ajoneuvotekniikan osaamisala")), "osaamisala", None)))),
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 25).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                Some("OPH-5410-2021")
              )
            ),
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "100431", "Kestävällä tavalla toimiminen", 40).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("361902", Some(finnish("Luonto- ja ympäristöalan perustutkinto")), "koulutus", None),
                Some("62/011/2014")
              )
            )
          ))
        )

        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatusOk()
        }
      }

      "Ei voi syöttää osaamisalaa, kun osaamisalaa vastaavaa tutkintoa ei löydy osasuorituksista" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1008", Some(finnish("Ajoneuvotekniikan osaamisala")), "osaamisala", None)))),
          tutkintonimike = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "100431", "Kestävällä tavalla toimiminen", 40).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("361902", Some(finnish("Luonto- ja ympäristöalan perustutkinto")), "koulutus", None),
                Some("62/011/2014")
              )
            )
          ))
        )

        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisalaa Ajoneuvotekniikan osaamisala(1008) ei löydy opiskeluoikeudelle määritellystä perusteesta."))
        }
      }
    }

    "Tutkintonimikkeillä" - {
      "Voi syöttää ilman tutkintonimikkeitä" in {
        val suoritus = osittainenSuoritusKesken.copy(
          tutkintonimike = None
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatusOk()
        }
      }

      "Voi syöttää tutkintonimikkeen, kun ei ole osasuorituksia" in {
        val suoritus = osittainenSuoritusKesken.copy(
          tutkintonimike = Some(List(Koodistokoodiviite("10025", Some(finnish("Automaalari")), "tutkintonimikkeet", None))),
          osasuoritukset = None
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatusOk()
        }
      }

      "Voi syöttää tutkintonimikkeen, kun nimikettä vastaava tutkinto löytyy" in {
        val suoritus = osittainenSuoritusKesken.copy(
          tutkintonimike = Some(List(Koodistokoodiviite("10025", Some(finnish("Automaalari")), "tutkintonimikkeet", None))),
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 25).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                Some("OPH-5410-2021")
              )
            ),
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "100431", "Kestävällä tavalla toimiminen", 40).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("361902", Some(finnish("Luonto- ja ympäristöalan perustutkinto")), "koulutus", None),
                Some("62/011/2014")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatusOk()
        }
      }

      "Ei voi syöttää tutkintonimikettä, jota vastaavaa tutkintoa ei löydy" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = Some(List(Koodistokoodiviite("10025", Some(finnish("Automaalari")), "tutkintonimikkeet", None))),
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "100431", "Kestävällä tavalla toimiminen", 40).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("361902", Some(finnish("Luonto- ja ympäristöalan perustutkinto")), "koulutus", None),
                Some("62/011/2014")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkintonimike("Tutkintonimikettä Automaalari(10025) ei löydy opiskeluoikeudelle määritellystä perusteesta."))
        }
      }
    }

    "Tutkinnon osan tutkinto" - {
      "Tutkinnon osan tutkinnon perusteen nimi täydennetään diaarinumeron perusteella" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 25).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("OPH-5410-2021"),
                perusteenNimi = None
              )
            )
          ))
        )

        val oo = setupOppijaWithAndGetOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus))
        oo.suoritukset.head match {
          case s: AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus =>
            s.osasuoritusLista.size shouldBe 1
            s.osasuoritusLista.head.tutkinto.perusteenNimi.get.get("fi") shouldBe "Ajoneuvoalan perustutkinto"
        }
      }

      "Tutkinnon osan tutkintoa ei voi siirtää ilman diaarinumeroa" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 25).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = None
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu())
        }
      }

      "Tutkinnon osan tutkintoa ei voi siirtää vääränmuotoisella diaarinumerolla" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 25).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("liian pitkä diaarinumero lorem ipsum")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Diaarinumeron muoto on virheellinen: liian pitkä diaarinumero lorem"))
        }
      }

      "Tutkinnon osan tutkintoa ei voi siirtää vääräntyyppisellä perusteella" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 25).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("OPH-2263-2019") // Lukion perusteen diaari
              )
            )
          )
          )
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi("Suoritukselle AmmatillinenTutkintoKoulutus ei voi käyttää opiskeluoikeuden voimassaoloaikana voimassaollutta perustetta OPH-2263-2019 (6828810), jonka koulutustyyppi on 2(Lukiokoulutus). Tälle suoritukselle hyväksytyt perusteen koulutustyypit ovat 1(Ammatillinen perustutkinto), 4(Ammatillinen perustutkinto (vaativa erityinen tuki)), 13(Ammatillinen perustutkinto näyttötutkintona), 11(Ammattitutkinto), 12(Erikoisammattitutkinto)."))
        }
      }

      "Ei voi syöttää väärää tutkinnon osaa" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            // Tutkinnon osa väärästä perusteesta
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "101050", "Yritystoiminnan suunnittelu", 15).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("OPH-5410-2021")
              )
            )
          )
          )
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa Yritystoiminnan suunnittelu(101050) ei löydy tutkintorakenteesta perusteelle OPH-5410-2021 (7614470)"))
        }
      }

      "Ei voi syöttää tutkinnon osaa väärällä laajuudella" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            // Tutkinnon osalla liian pieni laajuus
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 1).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("OPH-5410-2021")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta("Arvioidun suorituksen 'Ajoneuvon huoltotyöt' laajuus oltava perusteen mukaan vähintään 25 (oli 1.0)"))
        }
      }

      "Ei voi syöttää väärää tutkinnon osan osa-aluetta" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            yhteisenOsittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, yhteisetTutkinnonOsat, "106728", "Matemaattis-luonnontieteellinen osaaminen", 12).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("OPH-5410-2021")
              ),
              osasuoritukset = Some(List(
                YhteisenTutkinnonOsanOsaAlueenSuoritus(
                  koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(
                    Koodistokoodiviite("VVAI22", "ammatillisenoppiaineet"),
                    kieli = Koodistokoodiviite("FI", "kielivalikoima"),
                    pakollinen = true,
                    laajuus = Some(LaajuusOsaamispisteissä(12))
                  ), arviointi = Some(List(arviointiKiitettävä))
                )
              ))
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne("Osa-alue 'Viestintä ja vuorovaikutus äidinkielellä' (VVAI22) ei kuulu perusteen mukaan tutkinnon osaan 'Matemaattis-luonnontieteellinen osaaminen'"))
        }
      }

      "Ei voi syöttää tutkinnon osan osa-aluetta liian pienellä laajuudella" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            yhteisenOsittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, yhteisetTutkinnonOsat, "106728", "Matemaattis-luonnontieteellinen osaaminen", 12).copy(
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("OPH-5410-2021")
              ),
              osasuoritukset = Some(List(
                YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("MLFK", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(1))), arviointi = Some(List(arviointiKiitettävä)))
              ))
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta("Osa-alueen 'Fysikaaliset ja kemialliset ilmiöt ja niiden soveltaminen' (MLFK) pakollisen osan laajuus oltava perusteen mukaan 2 (oli 1.0)"))
        }
      }
    }

    "Tunnustettu tutkinnon osan tutkinto" - {
      "Voi syöttää tunnustetun tutkinnon osan" in {
        val suoritus = osittainenSuoritusKesken.copy(
          tutkintonimike = None,
          osaamisala = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 25).copy(
              tunnustettu = Some(OsaamisenTunnustaminen(
                osaaminen = None,
                selite = finnish("Aiemmin hankittu osaaminen")
              )),
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("OPH-5410-2021")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(200)
        }
      }

      "Ei voi syöttää tunnustettua tutkinnon osaa liian pienellä laajuudella" in {
        val suoritus = osittainenSuoritusKesken.copy(
          tutkintonimike = None,
          osaamisala = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 1).copy(
              tunnustettu = Some(OsaamisenTunnustaminen(
                osaaminen = None,
                selite = finnish("Aiemmin hankittu osaaminen")
              )),
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("OPH-5410-2021")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta("Arvioidun suorituksen 'Ajoneuvon huoltotyöt' laajuus oltava perusteen mukaan vähintään 25 (oli 1.0)"))
        }
      }

      "Ei voi syöttää tutkintoon kuulumatonta tunnustettua tutkinnon osaa" in {
        val suoritus = osittainenSuoritusKesken.copy(
          tutkintonimike = None,
          osaamisala = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "101050", "Yritystoiminnan suunnittelu", 15).copy(
              tunnustettu = Some(OsaamisenTunnustaminen(
                osaaminen = None,
                selite = finnish("Aiemmin hankittu osaaminen")
              )),
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("351301", Some(finnish("Ajoneuvoalan perustutkinto")), "koulutus", None),
                perusteenDiaarinumero = Some("OPH-5410-2021")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa Yritystoiminnan suunnittelu(101050) ei löydy tutkintorakenteesta perusteelle OPH-5410-2021 (7614470)"))
        }
      }

      "Voi syöttää tunnustetun tutkinnon osan vanhentuneesta perusteesta" in {
        val suoritus = osittainenSuoritusKesken.copy(
          tutkintonimike = None,
          osaamisala = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "101050", "Yritystoiminnan suunnittelu", 15).copy(
              tunnustettu = Some(OsaamisenTunnustaminen(
                osaaminen = None,
                selite = finnish("Aiemmin hankittu osaaminen")
              )),
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("331101", Some(finnish("Liiketoiminnan perustutkinto")), "koulutus", None),
                Some("59/011/2014")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(200)
        }
      }


      "Ei voi syöttää tunnustetun tutkinnon osaa liian pienellä laajuudella" in {
        val suoritus = osittainenSuoritusKesken.copy(
          tutkintonimike = None,
          osaamisala = None,
          osasuoritukset = Some(List(
            // Liian pieni laajuus läpäisee validaation näyttö-suoritustavan mukaisen rakenteen perusteella, jossa laajuutta ei ole rajoitettu
            // Emme tiedä minkä suoritustavan mukaan tutkinnon osa on suoritettu, joten riittää kun läpäisemme validaation millä tahansa rakenteella
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "101050", "Yritystoiminnan suunnittelu", 1).copy(
              tunnustettu = Some(OsaamisenTunnustaminen(
                osaaminen = None,
                selite = finnish("Aiemmin hankittu osaaminen")
              )),
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("331101", Some(finnish("Liiketoiminnan perustutkinto")), "koulutus", None),
                Some("59/011/2014")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400)
        }
      }

      "Ei voi syöttää tutkintoon kuulumatonta tutkinnon osaa vanhentuneelle perusteelle" in {
        val suoritus = osittainenSuoritusKesken.copy(
          tutkintonimike = None,
          osaamisala = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, ammatillisetTutkinnonOsat, "106945", "Ajoneuvon huoltotyöt", 25).copy(
              tunnustettu = Some(OsaamisenTunnustaminen(
                osaaminen = None,
                selite = finnish("Aiemmin hankittu osaaminen")
              )),
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("361902", Some(finnish("Luonto- ja ympäristöalan perustutkinto")), "koulutus", None),
                Some("62/011/2014")
              )
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa Ajoneuvon huoltotyöt(106945) ei löydy tutkintorakenteesta perusteelle 62/011/2014 (33827)"))
        }
      }

      "Vanhentuneen perusteen mukainen tunnustetun tutkinnon osan osa-alue läpäisee validaation ilman virheitä" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            yhteisenOsittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, yhteisetTutkinnonOsat, "101054", "Matemaattis-luonnontieteellinen osaaminen", 12).copy(
              tunnustettu = Some(OsaamisenTunnustaminen(
                osaaminen = None,
                selite = finnish("Aiemmin hankittu osaaminen")
              )),
              tutkinto = AmmatillinenTutkintoKoulutus(
                Koodistokoodiviite("361902", Some(finnish("Luonto- ja ympäristöalan perustutkinto")), "koulutus", None),
                Some("62/011/2014")
              ),
              osasuoritukset = Some(List(
                // On oikeasti väärä osa-alue, mutta vanhentuneelle perusteelle tämä ei aiheuta validaatiovirhettä
                YhteisenTutkinnonOsanOsaAlueenSuoritus(
                  koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(
                    Koodistokoodiviite("AI", "ammatillisenoppiaineet"),
                    kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"),
                    pakollinen = true,
                    laajuus = Some(LaajuusOsaamispisteissä(12))
                  ), arviointi = Some(List(arviointiKiitettävä))
                )
              ))
            )
          ))
        )
        setupOppijaWithOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus)) {
          verifyResponseStatusOk()
        }
      }
    }

    "Tutkinnon osan ryhmä" - {
      "Täydennetään yhteisen tutkinnon osan ryhmä automaattisesti tallennuksessa" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            yhteisenOsittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, None, "106728", "Matemaattis-luonnontieteellinen osaaminen", 12)
          ))
        )

        val oo = setupOppijaWithAndGetOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus))
        oo.suoritukset.length shouldBe 1
        oo.suoritukset.foreach {
          case s: AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus =>
            s.osasuoritusLista.length shouldBe 1
            s.osasuoritusLista.foreach {
              case os: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =>
                os.tutkinnonOsanRyhmä shouldBe yhteisetTutkinnonOsat
                os.tutkinnonOsanRyhmä.flatMap(_.nimi).isDefined shouldBe (true)
            }
        }
      }

      "Ei täydennetä muun tutkinnon osan ryhmää automaattisesti tallennuksessa ja sallitaan tyhjä tutkinnon osan ryhmä" in {
        val suoritus = osittainenSuoritusKesken.copy(
          osaamisala = None,
          tutkintonimike = None,
          osasuoritukset = Some(List(
            osittaisenTutkinnonTutkinnonOsanUseastaTutkinnostaSuoritus(h2, None, "106945", "Ajoneuvon huoltotyöt", 25)
          ))
        )

        val oo = setupOppijaWithAndGetOpiskeluoikeus(makeOpiskeluoikeus(suoritus = suoritus))
        oo.suoritukset.length shouldBe 1
        oo.suoritukset.foreach {
          case s: AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus =>
            s.osasuoritusLista.length shouldBe 1
            s.osasuoritusLista.foreach {
              case os: MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =>
                os.tutkinnonOsanRyhmä shouldBe None
            }
        }
      }
    }

    "Duplikaattiopiskeluoikeus" - {
      "Ei voi lisätä vastaavaa opiskeluoikeutta samaan oppilaitokseen" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus, defaultHenkilö) {
          verifyResponseStatusOk()
        }

        postOppija(makeOppija(defaultHenkilö, List(defaultOpiskeluoikeus))) {
          verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
        }
      }

      "Vastaavaan opiskeluoikeuden voi lisätä samaan oppilaitokseen, kun se ei ole ajallisesti päällekkäin" in {
        setupOppijaWithOpiskeluoikeus(AmmatillinenOsittainenUseistaTutkinnoista.valmisUseastaTutkinnostaOpiskeluoikeus) {
          verifyResponseStatusOk()
        }

        val myöhempiOpiskeluoikeus = AmmatillinenOsittainenUseistaTutkinnoista.valmisUseastaTutkinnostaOpiskeluoikeus.copy(
          arvioituPäättymispäivä = Some(date(2024, 12, 31)),
          tila = AmmatillinenOpiskeluoikeudenTila(List(
            AmmatillinenOpiskeluoikeusjakso(date(2024, 6, 5), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
            AmmatillinenOpiskeluoikeusjakso(date(2024, 12, 31), opiskeluoikeusValmistunut, Some(valtionosuusRahoitteinen))
          ))
        )

        postOppija(makeOppija(defaultHenkilö, List(myöhempiOpiskeluoikeus))) {
          verifyResponseStatusOk()
        }
      }
    }
  }
}
