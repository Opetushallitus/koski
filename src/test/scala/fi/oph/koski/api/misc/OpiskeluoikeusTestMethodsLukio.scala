package fi.oph.koski.api.misc

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData.nuortenOpetussuunnitelma
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

trait OpiskeluoikeusTestMethodsLukio extends PutOpiskeluoikeusTestMethods[LukionOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[LukionOpiskeluoikeus]]
}

trait OpiskeluoikeusTestMethodsLukio2015 extends OpiskeluoikeusTestMethodsLukio {
  override def defaultOpiskeluoikeus = TestMethodsLukio.lukionOpiskeluoikeus
}

object TestMethodsLukio {
  val vahvistus = Some(HenkilövahvistusPaikkakunnalla(päivä = date(2016, 6, 4), jyväskylä, myöntäjäOrganisaatio = jyväskylänNormaalikoulu, myöntäjäHenkilöt = List(Organisaatiohenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  val päättötodistusSuoritus = LukionOppimääränSuoritus2015(
    koulutusmoduuli = LukionOppimäärä(perusteenDiaarinumero = Some("60/011/2015")),
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = None,
    osasuoritukset = None
  )

  def lukionOpiskeluoikeus = LukionOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(päättötodistusSuoritus),
    tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))
  )
}
