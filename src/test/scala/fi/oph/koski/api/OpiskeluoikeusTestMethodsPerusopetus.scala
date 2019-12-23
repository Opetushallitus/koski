package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsPerusopetus extends PutOpiskeluoikeusTestMethods[PerusopetuksenOpiskeluoikeus]{
  def tag = implicitly[reflect.runtime.universe.TypeTag[PerusopetuksenOpiskeluoikeus]]
  val vahvistus = Some(HenkilövahvistusPaikkakunnalla(date(2016, 6, 4), jyväskylä, jyväskylänNormaalikoulu, List(Organisaatiohenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  override def defaultOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(päättötodistusSuoritus),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(NuortenPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä)))
  )

  val päättötodistusSuoritus = PerusopetusExampleData.perusopetuksenOppimääränSuoritus

  val vuosiluokkasuoritus = PerusopetusExampleData.yhdeksännenLuokanSuoritus

}
