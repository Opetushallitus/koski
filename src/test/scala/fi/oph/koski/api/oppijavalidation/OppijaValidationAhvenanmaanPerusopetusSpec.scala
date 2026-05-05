package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

class OppijaValidationAhvenanmaanPerusopetusSpec
  extends AnyFreeSpec
  with KoskiHttpSpec
  with PutOpiskeluoikeusTestMethods[AhvenanmaanPerusopetuksenOpiskeluoikeus] {

  def tag = implicitly[reflect.runtime.universe.TypeTag[AhvenanmaanPerusopetuksenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = AhvenanmaanPerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    tila = AhvenanmaanPerusopetuksenOpiskeluoikeudenTila(
      List(AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä))
    ),
    suoritukset = List(
      AhvenanmaanPerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = AhvenanmaanPerusopetuksenLuokkaAste(
          tunniste = Koodistokoodiviite("9", "perusopetuksenluokkaaste"),
          perusteenDiaarinumero = Some("104/011/2014")
        ),
        luokka = "9A",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli
      )
    )
  )

  "Ahvenanmaan perusopetuksen opiskeluoikeus" - {
    "ei voi tallentaa koska opiskeluoikeudentyyppi-koodiarvoa ei ole rekisteröity" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatus(
          400,
          ErrorMatcher.regex(
            KoskiErrorCategory.badRequest.validation.jsonSchema,
            """.*opiskeluoikeudentyyppi/ahvenanmaanperusopetus ei löydy koodistosta.*""".r
          )
        )
      }
    }
  }
}
