package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsPerusopetus extends PutOpiskeluOikeusTestMethods[PerusopetuksenOpiskeluoikeus]{
  val vahvistus = Some(Henkilövahvistus(date(2016, 6, 4), jyväskylä, jyväskylänNormaalikoulu, List(Organisaatiohenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  override def defaultOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = jyväskylänNormaalikoulu,
    suoritukset = List(päättötodistusSuoritus),
    alkamispäivä = Some(longTimeAgo),
    tila = PerusopetuksenOpiskeluoikeudenTila(List(PerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä)))
  )

  val päättötodistusSuoritus = PerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = Perusopetus(perusteenDiaarinumero = Some("104/011/2014")),
    suoritustapa = PerusopetusExampleData.suoritustapaKoulutus,
    oppimäärä = PerusopetusExampleData.perusopetuksenOppimäärä,
    suorituskieli = None,
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistus
  )
}
