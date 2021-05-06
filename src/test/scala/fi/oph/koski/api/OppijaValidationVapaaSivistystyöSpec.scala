package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import org.scalatest.FreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationVapaaSivistystyöSpec extends FreeSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "Laajuudet" - {
    "Osaamiskokonaisuuden laajuus" - {
      "Osaamiskokonaisuuden laajuus lasketaan opintokokonaisuuksien laajuuksista" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritusKOPS.copy(
          osasuoritukset = Some(List(
            osaamiskokonaisuudenSuoritus("1002", List(
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("A01", "Arjen rahankäyttö", "Arjen rahankäyttö", 2.0)
              ),
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("M01", "Mielen liikkeet", "Mielen liikkeet ja niiden havaitseminen", 51),
                vstArviointi("Hyväksytty", date(2021, 11, 2))
              )
            ))
          ))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(53.0)
      }

      "Valinnaisten suuntautumisopintojen laajuus lasketaan opintokokonaisuuksien laajuuksista" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritusKOPS.copy(
          osasuoritukset = Some(List(
            suuntautumisopintojenSuoritus(List(
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("ATX01", "Tietokoneen huolto", "Nykyaikaisen tietokoneen tyypilliset huoltotoimenpiteet", 3.0),
                vstArviointi("Hyväksytty", date(2021, 11, 12))
              ),
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("VT02", "Valaisintekniikka", "Valaisinlähteet ja niiden toiminta", 10.0)
              ),
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("TAI01", "Taide työkaluna", "Taiteen käyttö työkaluna", 40.0)
              )
            ))
          ))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(53.0)
      }

      "Jos osaamiskokonaisuudella ei ole opintokokonaisuuksia, sille asetettu laajuus poistetaan" in {
        val oo = defaultOpiskeluoikeus.copy(
          suoritukset = List(suoritusKOPS.copy(
            vahvistus = None,
            osasuoritukset = Some(List(tyhjäOsaamiskokonaisuudenSuoritus("1003", Some(laajuus(5.0)))))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
      }

      "Jos valinnaisilla suuntautumisopinnoilla ei ole opintokokonaisuuksia, sille asetettu laajuus poistetaan" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritusKOPS.copy(
          vahvistus = None,
          osasuoritukset = Some(List(tyhjäSuuntautumisopintojenSuoritus(Some(laajuus(5.0)))))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
      }

      "Jos päätason suorituksella on väärä yhteenlaskettu laajuus, päätason suoritusta ei voida merkitä valmiiksi" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritusKOPS.copy(
          osasuoritukset = Some(List(
            osaamiskokonaisuudenSuoritus("1002", List(
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("A01", "Arjen rahankäyttö", "Arjen rahankäyttö", 4.0)
              ),
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("M01", "Mielen liikkeet", "Mielen liikkeet ja niiden havaitseminen", 49),
                vstArviointi("Hylätty", date(2021, 11, 2))
              )
            ))
          ))
        )))

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVahvistetunPäätasonSuorituksenLaajuus("Päätason suoritus koulutus/999909 on vahvistettu, mutta sillä ei ole 53 opintopisteen edestä hyväksytyksi arvioituja suorituksia"))
        }
      }

      "Jos päätason suorituksella on osaamiskokonaisuuksia, joiden laajuus on alle 4, päätason suoritusta ei voida merkitä valmiiksi" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritusKOPS.copy(
          osasuoritukset = Some(List(
            osaamiskokonaisuudenSuoritus("1002", List(
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("M01", "Mielen liikkeet", "Mielen liikkeet ja niiden havaitseminen", 51),
                vstArviointi("Hyväksytty", date(2021, 11, 2))
              )
            )),
            osaamiskokonaisuudenSuoritus("1003", List(
              opintokokonaisuudenSuoritus(
                opintokokonaisuus("A01", "Arjen rahankäyttö", "Arjen rahankäyttö", 2.0),
                vstArviointi("Hyväksytty", date(2021, 11, 2))
              )
            ))
          ))
        )))

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVahvistetunPäätasonSuorituksenLaajuus("Päätason suoritus koulutus/999909 on vahvistettu, mutta sillä on hyväksytyksi arvioituja osaamiskokonaisuuksia, joiden laajuus on alle 4 opintopistettä"))
        }
      }
    }
  }

  private def putAndGetOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus): Opiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusKOPS
  def KOTOOPiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusKOTO
}
