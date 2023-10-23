package fi.oph.koski.api.misc

import fi.oph.koski.documentation.ExampleData.{suomenKieli, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.schema.{NuortenPerusopetuksenOppiaineenOppimääränSuoritus, PerusopetuksenPäätasonSuoritus}
import fi.oph.koski.{DirtiesFixtures, KoskiHttpSpec}
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JObject}
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate

class PerusopetusOmattiedotSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsPerusopetus with DirtiesFixtures {
  implicit val formats = DefaultFormats
  val suoritustenLukumäärä = PerusopetusExampleData.kaikkiAineet.get.length

  "Perusopetuksen oppimäärän suoritus" - {
    "kun suoritus on valmis" - {
      "palautetaan osasuoritukset" in {
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = None), kahdeksannenLuokanSuoritus)), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = None))), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(
          yhdeksännenLuokanSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(4)))),
          päättötodistusSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(4))))
        ))
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(0)
        }
      }
    }
    "kun suoritus on valmistut vähintään 5 päivää sitten" - {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(
        yhdeksännenLuokanSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(5)))),
        päättötodistusSuoritus.copy(vahvistus = vahvistus.map(_.copy(päivä = LocalDate.now().minusDays(5))))
      ))
      "palautetaan arvosanat" in {
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(kahdeksannenLuokanSuoritus)), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(kahdeksannenLuokanSuoritus.copy(vahvistus = None))), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineenOppimääränSuoritus)), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(1)
        }
      }
    }
    "kun suoritus on kesken" - {
      "piilotetaan arvosanat" in {
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineenOppimääränSuoritus.copy(vahvistus = None))), henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
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
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus = opiskeluoikeus, henkilö = defaultHenkilö.copy(hetu = "251014-5651")) {
          verifyResponseStatusOk()
        }
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders("251014-5651")) {
          arvioinnit.length should equal(1)
        }
      }
    }
  }

  "Pakollisten oppiainenden laajuuksien piilottaminen" - {
    def verify[A](päätasonSuoritus: PerusopetuksenPäätasonSuoritus, hetu: String)(verifyResponse: => A): A = {
      val pakollinenOppiaine = suoritus(oppiaine("GE").copy(pakollinen = true, laajuus = vuosiviikkotuntia(8))).copy(arviointi = arviointi(9))
      val valinnainenOppiaine = suoritus(oppiaine("LI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(8))).copy(arviointi = arviointi(8))
      val opiskeluoikeus = defaultOpiskeluoikeus.withSuoritukset(List(yhdeksännenLuokanSuoritus, päätasonSuoritus.withOsasuoritukset(Some(List(pakollinenOppiaine, valinnainenOppiaine)))))
      val henkilö = defaultHenkilö.copy(hetu = hetu)

      setupOppijaWithOpiskeluoikeus(opiskeluoikeus, henkilö) {
        verifyResponseStatusOk()
        get("api/omattiedot/editor", headers = kansalainenLoginHeaders(hetu)) {
          verifyResponse
        }
      }
    }
    "nuorten perusopetuksen vuosiluokan suoritus" - {
      "ilman vuosiluokan vahvistusta laajuudet piilotetaan pakollisilta oppiaineilta" in {
        verify(seitsemännenLuokanSuoritus.copy(vahvistus = None), hetu = "090401A8955") {
          laajuudet.length should equal(1)
        }
      }
      "jos vuosiluokan vahvistus on ennen leikkuripäivää, laajuudet piilotetaan pakollisilta oppiaineilta" in {
        verify(seitsemännenLuokanSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 7, 31))), hetu = "090401A8955") {
          laajuudet.length should equal(1)
        }
      }
      "jos vuosiluokka on vahvistettu leikkuripäivänä tai sen jälkeen, kaikki laajuudet näytetään" in {
        verify(seitsemännenLuokanSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 1))), hetu = "090401A8955") {
          laajuudet.length should equal(2)
        }
      }
    }
    "nuorten perusopetuksen oppimäärän suoritus" - {
      "ilman oppimäärän vahvistusta laajuudet piilotetaan pakollisilta oppiaineilta" in {
        verify(päättötodistusSuoritus.copy(vahvistus = None), hetu = "090401A187A") {
          laajuudet.length should equal(0) // Keskeneräisestä oppimäärän suorituksesta piilotetaan kaikki osasuoritukset (suorituksen ollessa ainut)
        }
      }
      "jos vuosiluokan vahvistus on ennen leikkuripäivää, laajuudet piilotetaan pakollisilta oppiaineilta" in {
        verify(päättötodistusSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 7, 31))), hetu = "090401A187A") {
          laajuudet.length should equal(1)
        }
      }
      "jos vuosiluokka on vahvistettu leikkuripäivänä tai sen jälkeen, kaikki laajuudet näytetään" in {
        verify(päättötodistusSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 1))), hetu = "090401A187A") {
          laajuudet.length should equal(2)
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

  def laajuudet = JsonMethods.parse(body).filter { json =>
    (json \ "key").extractOpt[String].contains("laajuus") && (json \ "model" \ "value").toOption.isDefined
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
