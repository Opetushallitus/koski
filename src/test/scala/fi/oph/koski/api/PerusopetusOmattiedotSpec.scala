package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData.{suomenKieli, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData.{arviointi, kahdeksannenLuokanSuoritus, perusopetuksenDiaarinumero, suoritustapaErityinenTutkinto}
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenOppimääränSuoritus
import org.json4s.{DefaultFormats, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest.FreeSpec

class PerusopetusOmattiedotSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus {
  implicit val formats = DefaultFormats
  val suoritustenLukumäärä = PerusopetusExampleData.kaikkiAineet.get.length

  "Perusopetuksen oppimäärän suoritus" - {
    "kun suoritus on valmis" - {
      "palautetaan osasuoritukset" in {
        putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          osasuoritukset.length should equal(suoritustenLukumäärä)
        }
      }
      "palautetaan arvosanat" in {
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(suoritustenLukumäärä)
        }
      }
    }
    "kun suoritus on kesken" - {
      "ja suoritus ei ole opiskeluoikeuden ainoa päätason suoritus" - {
        "piilotetaan perusopetuksen oppimäärän suoritus" in {
          resetFixtures
          putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = None), kahdeksannenLuokanSuoritus)), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
            verifyResponseStatusOk()
          }
          get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
            päätasonSuoritukset.length should equal(1)
            (päätasonSuoritukset.head \ "value" \ "classes").extract[List[String]] should not contain "perusopetuksenoppimaaransuoritus"
            osasuoritukset.length should equal(suoritustenLukumäärä)
          }
        }
      }
      "ja suoritus on opiskeluoikeuden ainoa päätason suoritus" - {
        "piilotetaan perusopetuksen oppimäärän osasuoritukset" in {
          resetFixtures
          putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = None))), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
            verifyResponseStatusOk()
          }
          get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
            päätasonSuoritukset.length should equal(1)
            (päätasonSuoritukset.head \ "value" \ "classes").extract[List[String]] should contain("perusopetuksenoppimaaransuoritus")
            osasuoritukset.length should equal(0)
          }
        }
      }
    }
    "kun suoritus on valmistunut alle 5 päivää sitten" - {
      "piilotetaan arvosanat" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(4))))))
        putOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(0)
        }
      }
    }
    "kun suoritus on valmistut vähintään 5 päivää sitten" - {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(5))))))
      "palautetaan arvosanat" in {
        putOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(suoritustenLukumäärä)
        }
      }
    }
  }
  "Perusopetuksen vuosiluokan suoritus" - {
    "kun suoritus on valmis" - {
      "palautetaan arvosanat" in {
        resetFixtures
        putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(kahdeksannenLuokanSuoritus)), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(suoritustenLukumäärä)
          käyttäytymisenArviointi.length should equal(1)
        }
      }
    }
    "kun suoritus on kesken" - {
      "piilotetaan arvosanat" in {
        putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(kahdeksannenLuokanSuoritus.copy(vahvistus = None))), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(0)
          käyttäytymisenArviointi.length should equal(0)
        }
      }
    }
    "kun suoritus on valmistunut alle 5 päivää sitten" - {
      "piilotetaan arvosanat" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(kahdeksannenLuokanSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(4))))))
        putOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(0)
          käyttäytymisenArviointi.length should equal(0)
        }
      }
    }
    "kun suoritus on valmistut vähintään 5 päivää sitten" - {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(kahdeksannenLuokanSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(5))))))
      "palautetaan arvosanat" in {
        putOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(suoritustenLukumäärä)
          käyttäytymisenArviointi.length should equal(1)
        }
      }
    }
  }
  "Perusopetuksen oppiaineen oppimäärän suoritus" - {

    val oppiaineenOppimääränSuoritus =
      NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
        koulutusmoduuli = PerusopetusExampleData.äidinkieli("AI1", diaarinumero = Some(perusopetuksenDiaarinumero)),
        toimipiste = jyväskylänNormaalikoulu,
        arviointi = arviointi(9),
        suoritustapa = suoritustapaErityinenTutkinto,
        vahvistus = vahvistusPaikkakunnalla(),
        suorituskieli = suomenKieli
      )
    "kun suoritus on valmis" - {
      "palautetaan arvosanat" in {
        resetFixtures
        putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineenOppimääränSuoritus)), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(1)
        }
      }
    }
    "kun suoritus on kesken" - {
      "piilotetaan arvosanat" in {
        putOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineenOppimääränSuoritus.copy(vahvistus = None))), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(0)
        }
      }
    }
    "kun suoritus on valmistunut alle 5 päivää sitten" - {
      "piilotetaan arvosanat" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineenOppimääränSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(4))))))
        putOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(0)
        }
      }
    }
    "kun suoritus on valmistut vähintään 5 päivää sitten" - {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineenOppimääränSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(5))))))
      "palautetaan arvosanat" in {
        putOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(1)
        }
      }
    }
  }

  def käyttäytymisenArviointi = JsonMethods.parse(body).filter { json =>
    (json \ "key").extractOpt[String].contains("käyttäytymisenArvio") && (json \ "model" \ "value").toOption.isDefined
  }

  def arvioinnit = JsonMethods.parse(body).filter { json =>
    (json \ "key").extractOpt[String].contains("arviointi") && (json \ "model" \ "value").toOption.isDefined
  }

  def päätasonSuoritukset = JsonMethods.parse(body)
    .filter(json => (json \ "key").extractOpt[String].contains("suoritukset") && (json \ "model" \ "value").toOption.isDefined)
    .map(json => json \ "model" \ "value")
    .flatMap(json => json.extract[List[JObject]])

  def osasuoritukset = päätasonSuoritukset.flatMap(suoritusJson =>
    suoritusJson
      .filter(json => (json \ "key").extractOpt[String].contains("osasuoritukset"))
      .map(json => json \ "model" \ "value")
      .flatMap(json => json.extract[List[JObject]])
  )
}
