package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesLukioonValmistavaKoulutus.{lukioonValmistavanKoulutuksenSuoritus, lukioonValmistavanKoulutuksenSuoritus2019}
import fi.oph.koski.documentation.LukioExampleData.{laajuus}
import fi.oph.koski.documentation.{ExamplesLukioonValmistavaKoulutus, LukioExampleData}
import fi.oph.koski.http.{KoskiErrorCategory}
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import scala.reflect.runtime.universe.TypeTag

class OppijaValidationLukioonValmistavaSpec extends TutkinnonPerusteetTest[LukioonValmistavanKoulutuksenOpiskeluoikeus] with KoskiHttpSpec {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    lukioonValmistavanKoulutuksenSuoritus.copy(koulutusmoduuli = lukioonValmistavanKoulutuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"
  override def tag: TypeTag[LukioonValmistavanKoulutuksenOpiskeluoikeus] = implicitly[TypeTag[LukioonValmistavanKoulutuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: LukioonValmistavanKoulutuksenOpiskeluoikeus = ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenOpiskeluoikeus

  "Opintojen päätason suoritus" - {
    "laajuuden yksikkö ei voi olla '4' jos diaarinumero on OPH-4958-2020" in {
      val opiskeluoikeus = ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenOpiskeluoikeus2019.copy(
        suoritukset = List(lukioonValmistavanKoulutuksenSuoritus2019.copy(
          koulutusmoduuli = lukioonValmistavanKoulutuksenSuoritus2019.koulutusmoduuli.copy(
            laajuus = Some(laajuus(2.0, yksikkö = laajuusKursseissa.koodiarvo)),
            perusteenDiaarinumero = Some("OPH-4958-2020")
          )
        ))
      )

      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.lukioonValmistavallaKoulutuksellaVääräLaajuudenArvo())
      }
    }
  }

  "Opintojen rahoitus" - {
    "lasna -tilalta vaaditaan opintojen rahoitus" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta lasna puuttuu rahoitusmuoto"))
      }
    }
    "valmistunut -tila vaaditaan opintojen rahoitus" in {
      val tila = LukionOpiskeluoikeudenTila(List(
        LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(date(2016, 8, 1), opiskeluoikeusValmistunut))
      )
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila)) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta valmistunut puuttuu rahoitusmuoto"))
      }
    }
  }

  "Opintojen osasuoritukset" - {
    "sallitaan vain luva2019-koodiston mukaisia osasuorituksia 1.8.2021 ja jälkeen" - {
      "estetään luva2015-opsin mukainen kurssisuoritus" in {
        val opiskeluoikeus = ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenOpiskeluoikeus2019.copy(
          suoritukset = List(lukioonValmistavanKoulutuksenSuoritus2019.copy(
            osasuoritukset = Some(List(
              LukioonValmistavanKoulutuksenOppiaineenSuoritus(
                LukioonValmistavaÄidinkieliJaKirjallisuus(Koodistokoodiviite("LVAIK", "oppiaineetluva"), kieli = Koodistokoodiviite(koodiarvo = "AI7", koodistoUri = "oppiaineaidinkielijakirjallisuus")),
                arviointi = arviointiPäivämäärällä("S", LocalDate.of(2021, 8, 1)),
                osasuoritukset = Some(List(
                  ExamplesLukioonValmistavaKoulutus.luvaKurssinSuoritus(ExamplesLukioonValmistavaKoulutus.valtakunnallinenLuvaKurssi("LVS1").copy(
                    laajuus = Some(laajuus(2.0, yksikkö = laajuusOpintopisteissä.koodiarvo)))).copy(
                    alkamispäivä = Some(LocalDate.of(2021, 8, 1)),
                    arviointi = LukioExampleData.sanallinenArviointi("S", None, LocalDate.of(2021, 8, 1))
                  )
                ))
              )
            ))
          ))
        )

        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.lukioonValmistavassaVanhanOpsinKurssiSuorituksia("Lukioon valmistavan koulutuksen kurssilla lukioonvalmistavankoulutuksenkurssit2015/LVS1 on vanhan opetussuunniteleman mukainen koodi. 1.8.2021 jälkeen alkaneiden kurssien tulee käyttää vuoden 2021 opetussuunnitelmaa."))
        }
      }
      "sallitaan luva2019-opsin mukainen kurssisuoritus" in {
        val opiskeluoikeus = ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenOpiskeluoikeus2019.copy(
          suoritukset = List(lukioonValmistavanKoulutuksenSuoritus2019.copy(
            osasuoritukset = Some(List(
              LukioonValmistavanKoulutuksenOppiaineenSuoritus(
                LukioonValmistavaÄidinkieliJaKirjallisuus(Koodistokoodiviite("LVAIK", "oppiaineetluva"), kieli = Koodistokoodiviite(koodiarvo = "AI7", koodistoUri = "oppiaineaidinkielijakirjallisuus")),
                arviointi = arviointiPäivämäärällä("S", LocalDate.of(2021, 8, 1)),
                osasuoritukset = Some(List(
                  ExamplesLukioonValmistavaKoulutus.luvaKurssinSuoritus(ExamplesLukioonValmistavaKoulutus.valtakunnallinenLuvaKurssi2019("LVS1").copy(
                    laajuus = Some(laajuus(2.0, yksikkö = laajuusOpintopisteissä.koodiarvo)))).copy(
                    alkamispäivä = Some(LocalDate.of(2021, 8, 1)),
                    arviointi = LukioExampleData.sanallinenArviointi("S", None, LocalDate.of(2021, 8, 1))
                  )
                ))
              )
            ))
          ))
        )

        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }

      "ei sallita laajuutta kursseissa jos diaarinumero 'OPH-4958-2020'" in {
        val opiskeluoikeus = ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenOpiskeluoikeus2019.copy(
          suoritukset = List(lukioonValmistavanKoulutuksenSuoritus2019.copy(
            koulutusmoduuli = lukioonValmistavanKoulutuksenSuoritus2019.koulutusmoduuli.copy(
              laajuus = Some(laajuus(2.0, yksikkö = laajuusOpintopisteissä.koodiarvo)),
              perusteenDiaarinumero = Some("OPH-4958-2020")
            ),
            osasuoritukset = Some(List(
              LukioonValmistavanKoulutuksenOppiaineenSuoritus(
                LukioonValmistavaÄidinkieliJaKirjallisuus(Koodistokoodiviite("LVAIK", "oppiaineetluva"), kieli = Koodistokoodiviite(koodiarvo = "AI7", koodistoUri = "oppiaineaidinkielijakirjallisuus")),
                arviointi = arviointiPäivämäärällä("S", LocalDate.of(2021, 8, 1)),
                osasuoritukset = Some(List(
                  ExamplesLukioonValmistavaKoulutus.luvaKurssinSuoritus(ExamplesLukioonValmistavaKoulutus.valtakunnallinenLuvaKurssi2019("LVS1").copy(
                    laajuus = Some(laajuus(2.0, yksikkö = laajuusKursseissa.koodiarvo))
                  ))
                ))
              )
            ))
          ))
        )

        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.lukioonValmistavallaKoulutuksellaVääräLaajuudenArvo())
        }
      }

      "ei sallita laajuutta opintopisteissä jos diaarinumero '56/011/2015'" in {
        val opiskeluoikeus = ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenOpiskeluoikeus2019.copy(
          suoritukset = List(lukioonValmistavanKoulutuksenSuoritus2019.copy(
            koulutusmoduuli = lukioonValmistavanKoulutuksenSuoritus2019.koulutusmoduuli.copy(
              laajuus = Some(laajuus(2.0, yksikkö = laajuusOpintopisteissä.koodiarvo)),
              perusteenDiaarinumero = Some("56/011/2015")
            ),
            osasuoritukset = Some(List(
              LukioonValmistavanKoulutuksenOppiaineenSuoritus(
                LukioonValmistavaÄidinkieliJaKirjallisuus(Koodistokoodiviite("LVAIK", "oppiaineetluva"), kieli = Koodistokoodiviite(koodiarvo = "AI7", koodistoUri = "oppiaineaidinkielijakirjallisuus")),
                arviointi = arviointiPäivämäärällä("S", LocalDate.of(2021, 8, 1)),
                osasuoritukset = Some(List(
                  ExamplesLukioonValmistavaKoulutus.luvaKurssinSuoritus(ExamplesLukioonValmistavaKoulutus.valtakunnallinenLuvaKurssi2019("LVS1").copy(
                    laajuus = Some(laajuus(2.0, yksikkö = laajuusOpintopisteissä.koodiarvo))
                  ))
                ))
              )
            ))
          ))
        )

        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.lukioonValmistavallaKoulutuksellaVääräLaajuudenArvo(
            "Lukioon valmistavan koulutuksen suorituksella voi olla laajuuden koodiyksikkönä vain '4', jos suorituksen diaarinumero on '56/011/2015'"
          ))
        }
      }
    }
  }

  def arviointiPäivämäärällä(arvosana: String, päivä: LocalDate): Some[List[LukionOppiaineenArviointi]] = {
    Some(List(LukionOppiaineenArviointi(arvosana, Some(päivä))))
  }
}
