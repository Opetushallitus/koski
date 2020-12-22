package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.schema._
import org.scalatest.FreeSpec

class OppijaValidationVapaaSivistystyöSpec extends FreeSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with LocalJettyHttpSpecification {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "Laajuudet" - {
    "Osaamiskokonaisuuden laajuus" - {
      "Osaamiskokonaisuuden laajuus lasketaan opintokokonaisuuksien laajuuksista" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus.copy(
          osasuoritukset = Some(List(
            osaamiskokonaisuudenSuoritus("1002", List(
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("A01", "Arjen rahankäyttö", "Arjen rahankäyttö", 2.0)
              ),
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("M01", "Mielen liikkeet", "Mielen liikkeet ja niiden havaitseminen", 2.5),
                vstArviointi("Hyväksytty", date(2021, 11, 2))
              )
            ))
          ))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(4.5)
      }

      "Valinnaisten suuntautumisopintojen laajuus lasketaan opintokokonaisuuksien laajuuksista" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus.copy(
          osasuoritukset = Some(List(
            suuntautumisopintojenSuoritus(List(
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("ATX01", "Tietokoneen huolto", "Nykyaikaisen tietokoneen tyypilliset huoltotoimenpiteet", 5.0),
                vstArviointi("Hyväksytty", date(2021, 11, 12))
              ),
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("VT02", "Valaisintekniikka", "Valaisinlähteet ja niiden toiminta", 10.0)
              ),
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("TAI01", "Taide työkaluna", "Taiteen käyttö työkaluna", 30.0)
              )
            ))
          ))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(45.0)
      }

      "Jos osaamiskokonaisuudella ei ole opintokokonaisuuksia, sille asetettu laajuus poistetaan" in {
        val oo = defaultOpiskeluoikeus.copy(
          suoritukset = List(suoritus.copy(
            vahvistus = None,
            osasuoritukset = Some(List(tyhjäOsaamiskokonaisuudenSuoritus("1003", Some(laajuus(5.0)))))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
      }

      "Jos valinnaisilla suuntautumisopinnoilla ei ole opintokokonaisuuksia, sille asetettu laajuus poistetaan" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus.copy(
          osasuoritukset = Some(List(tyhjäSuuntautumisopintojenSuoritus(Some(laajuus(5.0)))))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
      }
    }
  }

  private def putAndGetOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus): Opiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeus
}
