package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData.jyväskylä
import fi.oph.koski.documentation.LukioExampleData
import fi.oph.koski.documentation.LukioExampleData.nuortenOpetussuunnitelma
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._

trait OpiskeluoikeusTestMethodsLukio extends PutOpiskeluOikeusTestMethods[LukionOpiskeluoikeus]{
  val vahvistus = Some(Henkilövahvistus(päivä = date(2016, 6, 4), jyväskylä, myöntäjäOrganisaatio = jyväskylänNormaalikoulu, myöntäjäHenkilöt = List(OrganisaatioHenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  override def defaultOpiskeluoikeus = LukionOpiskeluoikeus(
    id = None, versionumero = None, lähdejärjestelmänId = None, alkamispäivä = None, päättymispäivä = None,
    oppilaitos = jyväskylänNormaalikoulu, koulutustoimija = None,
    suoritukset = List(päättötodistusSuoritus),
    tila = None, läsnäolotiedot = None
  )

  val päättötodistusSuoritus = LukionOppimääränSuoritus(
    koulutusmoduuli = LukionOppimäärä(perusteenDiaarinumero = Some("60/011/2015")),
    oppimäärä = nuortenOpetussuunnitelma,
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistus,
    osasuoritukset = None
  )
}
