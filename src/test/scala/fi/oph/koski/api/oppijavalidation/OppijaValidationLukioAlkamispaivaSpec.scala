package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.TestMethodsLukio.päättötodistusSuoritus
import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethodsLukio2015, PutOpiskeluoikeusTestMethods}
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesLukio.aikuistenOpsinPerusteet2015
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.ressunLukio
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationLukioAlkamispäiväSpec
  extends AnyFreeSpec
    with PutOpiskeluoikeusTestMethods[LukionOpiskeluoikeus]
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethodsLukio2015
    with BeforeAndAfterEach
{
  override protected def beforeEach() {
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
  }

  "Alkamispäivä 1.8.2021 jälkeen" - {
    val alkamispäivä = date(2021, 8, 1)
    val aiempiAlkamispäivä = date(2021, 1, 1)

    "Lukion oppimäärässä" - {
      "Sallitaan 2005 tai myöhemmin syntyneelle, jos on aiempi lukion opiskeluoikeus" in {
        setupOppijaWithOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(aiempiAlkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))
          ).withLisääPuuttuvaMaksuttomuustieto,
          KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
        ) {
           verifyResponseStatusOk()
        }

        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            oppilaitos = Some(ressunLukio),
            suoritukset = List(päättötodistusSuoritus.copy(
              toimipiste = ressunLukio
            )),
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))
          ).withLisääPuuttuvaMaksuttomuustieto,
          KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatusOk()
        }
      }
      "Sallitaan 2004 tai aiemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))))) {
          verifyResponseStatusOk()
        }
      }
      "Ei sallita 2005 tai myöhemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia" in {
        setupOppijaWithOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))
          ).withLisääPuuttuvaMaksuttomuustieto,
          KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.liianVanhaOpetussuunnitelma(
            "Uusi lukion opiskelija ei voi aloittaa vanhojen opetussuunnitelman perusteiden mukaisia opintoja 1.8.2021 tai myöhemmin. Käytä lukion opetussuunnitelman perusteen diaarinumeroa OPH-2263-2019. Jos tosiasiassa oppija on aloittanut vanhojen perusteiden mukaiset lukio-opinnot ennen 1.8.2021, häneltä puuttuu KOSKI-tietovarannosta tämä opiskeluoikeus"
          ))
        }
      }
      "Ei sallita 2005 tai myöhemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia, paitsi se, jota ollaan parhaillaan muokkaamassa" in {
        val luodunOpiskeluoikeudenOid = setupOppijaWithOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(aiempiAlkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))
          ).withLisääPuuttuvaMaksuttomuustieto,
          KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatusOk()
          readPutOppijaResponse.opiskeluoikeudet.head.oid
        }

        setupOppijaWithOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            oid = Some(luodunOpiskeluoikeudenOid),
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))
          ).withLisääPuuttuvaMaksuttomuustieto,
          KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.liianVanhaOpetussuunnitelma(
            "Uusi lukion opiskelija ei voi aloittaa vanhojen opetussuunnitelman perusteiden mukaisia opintoja 1.8.2021 tai myöhemmin. Käytä lukion opetussuunnitelman perusteen diaarinumeroa OPH-2263-2019. Jos tosiasiassa oppija on aloittanut vanhojen perusteiden mukaiset lukio-opinnot ennen 1.8.2021, häneltä puuttuu KOSKI-tietovarannosta tämä opiskeluoikeus"
          ))
        }
      }
      "Sallitaan jos opiskelee aikuisten opsilla" - {
        "Oppimäärä" in {
          setupOppijaWithOpiskeluoikeus(
            defaultOpiskeluoikeus.copy(
              tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
              suoritukset = List(päättötodistusSuoritus.copy(oppimäärä = aikuistenOpetussuunnitelma))
            ).withLisääPuuttuvaMaksuttomuustieto,
            KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
          ) {
            verifyResponseStatusOk()
          }
        }
        "Oppiaineen oppimäärä" in {
          setupOppijaWithOpiskeluoikeus(
            defaultOpiskeluoikeus.copy(
              tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
              suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi.copy(
                koulutusmoduuli = lukionOppiaine("YH", diaarinumero = Some(aikuistenOpsinPerusteet2015))
              ))
            ),
            KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
          ) {
            verifyResponseStatusOk()
          }
        }
      }
    }
    "Lukion oppiaineen oppimäärässä" - {
      "Sallitaan 2005 tai myöhemmin syntyneelle, jos on aiempi lukion opiskeluoikeus" in {
        setupOppijaWithOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(
              LukionOpiskeluoikeusjakso(aiempiAlkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
              LukionOpiskeluoikeusjakso(aiempiAlkamispäivä.plusDays(7), katsotaanEronneeksi, Some(valtionosuusRahoitteinen))
            )),
          ).withLisääPuuttuvaMaksuttomuustieto,
          KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatusOk()
        }

        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
            suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi)
          ),
          KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatusOk()
        }
      }
      "Sallitaan 2004 tai aiemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(
          tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
          suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi)
        )) {
          verifyResponseStatusOk()
        }
      }
      "Ei sallita 2005 tai myöhemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia" in {
        setupOppijaWithOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
            suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi)
          ),
          KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.liianVanhaOpetussuunnitelma(
            "Uusi lukion opiskelija ei voi aloittaa vanhojen opetussuunnitelman perusteiden mukaisia opintoja 1.8.2021 tai myöhemmin. Käytä lukion opetussuunnitelman perusteen diaarinumeroa OPH-2263-2019. Jos tosiasiassa oppija on aloittanut vanhojen perusteiden mukaiset lukio-opinnot ennen 1.8.2021, häneltä puuttuu KOSKI-tietovarannosta tämä opiskeluoikeus"
          ))
        }
      }
      "Sallitaan, jos on ulkomainen vaihto-opiskelija" in {
        setupOppijaWithOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
            suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi),
            lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(ulkomainenVaihtoopiskelija = true))
          ),
          KoskiSpecificMockOppijat.vuonna2005SyntynytUlkomainenVaihtoopiskelija
        ) {
          verifyResponseStatusOk()
        }
      }
    }
  }
}
