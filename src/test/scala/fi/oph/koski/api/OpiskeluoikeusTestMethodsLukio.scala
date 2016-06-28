package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData.nuortenOpetussuunnitelma
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsLukio extends PutOpiskeluOikeusTestMethods[LukionOpiskeluoikeus]{
  val vahvistus = Some(Henkilövahvistus(päivä = date(2016, 6, 4), jyväskylä, myöntäjäOrganisaatio = jyväskylänNormaalikoulu, myöntäjäHenkilöt = List(Organisaatiohenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  override def defaultOpiskeluoikeus = LukionOpiskeluoikeus(
    oppilaitos = jyväskylänNormaalikoulu,
    suoritukset = List(päättötodistusSuoritus),
    alkamispäivä = Some(longTimeAgo),
    tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä)))
  )

  val päättötodistusSuoritus = LukionOppimääränSuoritus(
    koulutusmoduuli = LukionOppimäärä(perusteenDiaarinumero = Some("60/011/2015")),
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = None,
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistus,
    osasuoritukset = None
  )
}
