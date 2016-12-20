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
      alkamispäivä = Some(date(2006, 8, 13)),
      päättymispäivä = Some(date(2007, 6, 3)),
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      suoritukset = List(
          EsiopetuksenSuoritus(
            tila = tilaValmis,
            toimipiste = jyväskylänNormaalikoulu,
            suorituskieli = suomenKieli,
            vahvistus = vahvistusPaikkakunnalla(date(2007, 6, 3))
          )
      ),
      tila = PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2006, 8, 13), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2007, 6, 3), opiskeluoikeusValmistunut)
        )
      )
    ))
  )

  val examples = List(Example("esiopetus valmis", "Oppija on suorittanut peruskoulun esiopetuksen", esioppilas))
}

