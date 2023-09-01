package fi.oph.koski.api.misc

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

trait OpiskeluoikeusTestMethodsPerusopetus extends PutOpiskeluoikeusTestMethods[PerusopetuksenOpiskeluoikeus]{
  def tag = implicitly[reflect.runtime.universe.TypeTag[PerusopetuksenOpiskeluoikeus]]
  val vahvistus = Some(HenkilövahvistusPaikkakunnalla(date(2016, 6, 4), jyväskylä, jyväskylänNormaalikoulu, List(Organisaatiohenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  override def defaultOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(vuosiluokkasuoritus, päättötodistusSuoritus),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(NuortenPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä)))
  )

  val päättötodistusSuoritus = PerusopetusExampleData.perusopetuksenOppimääränSuoritus

  val vuosiluokkasuoritus = PerusopetusExampleData.yhdeksännenLuokanSuoritus

}
