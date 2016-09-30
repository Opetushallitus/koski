package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetuksenExampleData.opiskeluoikeusLäsnä
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.schema._

object ExamplesEsiopetus {
  val esioppilas = Oppija(
    exampleHenkilö,
    List(EsiopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2015, 8, 15)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      suoritukset = List(
          EsiopetuksenSuoritus(
            tila = tilaValmis,
            toimipiste = jyväskylänNormaalikoulu,
            suorituskieli = suomenKieli,
            vahvistus = vahvistus()
          )
      ),
      tila = PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2015, 8, 15), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      ),
      läsnäolotiedot = None
    ))
  )

  val examples = List(Example("esiopetus valmis", "Oppija on suorittanut peruskoulun esiopetuksen", esioppilas))
}

