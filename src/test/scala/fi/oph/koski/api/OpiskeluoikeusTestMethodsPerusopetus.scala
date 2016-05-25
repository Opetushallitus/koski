package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData.jyväskylä
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsPerusopetus extends OpiskeluOikeusTestMethods[PerusopetuksenOpiskeluoikeus]{
  val vahvistus: Some[Vahvistus] = Some(Vahvistus(date(2016, 6, 4), jyväskylä, jyväskylänNormaalikoulu, List(OrganisaatioHenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  override def defaultOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    id = None, versionumero = None, lähdejärjestelmänId = None, alkamispäivä = None, päättymispäivä = None,
    oppilaitos = jyväskylänNormaalikoulu, koulutustoimija = None, tavoite = PerusopetusExampleData.tavoiteKokoOppimäärä,
    suoritukset = List(päättötodistusSuoritus),
    tila = None, läsnäolotiedot = None
  )

  val päättötodistusSuoritus = PerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = Perusopetus(perusteenDiaarinumero = Some("104/011/2014")),
    suoritustapa = PerusopetusExampleData.suoritustapaKoulutus,
    oppimäärä = PerusopetusExampleData.perusopetuksenOppimäärä,
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistus
  )
}
