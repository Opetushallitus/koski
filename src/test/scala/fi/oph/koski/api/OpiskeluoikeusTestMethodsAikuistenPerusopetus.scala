package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsAikuistenPerusopetus extends PutOpiskeluoikeusTestMethods[AikuistenPerusopetuksenOpiskeluoikeus]{
  val vahvistus = Some(HenkilövahvistusPaikkakunnalla(date(2016, 6, 4), jyväskylä, jyväskylänNormaalikoulu, List(Organisaatiohenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))
  def tag = implicitly[reflect.runtime.universe.TypeTag[AikuistenPerusopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = opiskeluoikeusWithPerusteenDiaarinumero(Some("19/011/2015"))
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) : AikuistenPerusopetuksenOpiskeluoikeus
}
