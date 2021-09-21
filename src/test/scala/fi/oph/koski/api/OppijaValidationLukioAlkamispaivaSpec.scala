package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import org.scalatest.{BeforeAndAfterEach, FreeSpec}

class OppijaValidationLukioAlkamispäiväSpec
  extends FreeSpec
    with PutOpiskeluoikeusTestMethods[LukionOpiskeluoikeus]
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethodsLukio2015
    with BeforeAndAfterEach
{
  override protected def beforeEach() {
    super.beforeEach()
    resetFixtures()
  }

  override protected def afterEach(): Unit = {
    resetFixtures()
    super.afterEach()
  }

  "Alkamispäivä 1.8.2021 jälkeen" - {
    val alkamispäivä = date(2021, 8, 1)
    val aiempiAlkamispäivä = date(2021, 1, 1)

    "Lukion oppimäärässä" - {
      "Sallitaan 2004 tai myöhemmin syntyneelle, jos on aiempi lukion opiskeluoikeus" in {

        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(aiempiAlkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))),
          KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatusOk()
        }

        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))),
          KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatusOk()
        }
      }
      "Sallitaan 2003 tai aiemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))))) {
          verifyResponseStatusOk()
        }
      }
      "Ei sallita 2004 tai myöhemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia" in {
        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))),
          KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.liianVanhaOpetussuunnitelma(
            "Uusi lukion opiskelija ei voi aloittaa vanhojen opetussuunnitelman perusteiden mukaisia opintoja 1.8.2021 tai myöhemmin. Käytä lukion opetussuunnitelman perusteen diaarinumeroa OPH-2263-2019. Jos tosiasiassa oppija on aloittanut vanhojen perusteiden mukaiset lukio-opinnot ennen 1.8.2021, häneltä puuttuu KOSKI-tietovarannosta tämä opiskeluoikeus"
          ))
        }
      }
      "Ei sallita 2004 tai myöhemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia, paitsi se, jota ollaan parhaillaan muokkaamassa" in {
        val luodunOpiskeluoikeudenOid = putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(aiempiAlkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))),
          KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatusOk()
          readPutOppijaResponse.opiskeluoikeudet.head.oid
        }

        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            oid = Some(luodunOpiskeluoikeudenOid),
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))
          ),
          KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.liianVanhaOpetussuunnitelma(
            "Uusi lukion opiskelija ei voi aloittaa vanhojen opetussuunnitelman perusteiden mukaisia opintoja 1.8.2021 tai myöhemmin. Käytä lukion opetussuunnitelman perusteen diaarinumeroa OPH-2263-2019. Jos tosiasiassa oppija on aloittanut vanhojen perusteiden mukaiset lukio-opinnot ennen 1.8.2021, häneltä puuttuu KOSKI-tietovarannosta tämä opiskeluoikeus"
          ))
        }
      }
    }
    "Lukion oppiaineen oppimäärässä" - {
      "Sallitaan 2004 tai myöhemmin syntyneelle, jos on aiempi lukion opiskeluoikeus" in {

        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(aiempiAlkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))),
          KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatusOk()
        }

        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
            suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi)
          ),
          KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatusOk()
        }
      }
      "Sallitaan 2003 tai aiemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
          tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
          suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi)
        )) {
          verifyResponseStatusOk()
        }
      }
      "Ei sallita 2004 tai myöhemmin syntyneelle, jos ei ole aiempia lukion opiskeluoikeuksia" in {
        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
            suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi)
          ),
          KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.liianVanhaOpetussuunnitelma(
            "Uusi lukion opiskelija ei voi aloittaa vanhojen opetussuunnitelman perusteiden mukaisia opintoja 1.8.2021 tai myöhemmin. Käytä lukion opetussuunnitelman perusteen diaarinumeroa OPH-2263-2019. Jos tosiasiassa oppija on aloittanut vanhojen perusteiden mukaiset lukio-opinnot ennen 1.8.2021, häneltä puuttuu KOSKI-tietovarannosta tämä opiskeluoikeus"
          ))
        }
      }
      "Sallitaan, jos on ulkomainen vaihto-opiskelija" in {
        putOpiskeluoikeus(
          defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
            suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi),
            lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(ulkomainenVaihtoopiskelija = true))
          ),
          KoskiSpecificMockOppijat.vuonna2004SyntynytUlkomainenVaihtoopiskelija
        ) {
          verifyResponseStatusOk()
        }
      }
    }
  }
}
