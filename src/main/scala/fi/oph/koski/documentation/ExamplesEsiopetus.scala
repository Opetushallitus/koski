package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesEsiopetus {
  val esioppilas = Oppija(
    exampleHenkilö,
    List(EsiopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2006, 8, 13)),
      päättymispäivä = Some(date(2007, 6, 3)),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      koulutustoimija = None,
      suoritukset = List(
          EsiopetuksenSuoritus(
            koulutusmoduuli = Esiopetus(kuvaus = Some("Kaksikielinen esiopetus (suomi-portugali)"), perusteenDiaarinumero = Some("102/011/2014")),
            toimipiste = jyväskylänNormaalikoulu,
            suorituskieli = suomenKieli,
            muutSuorituskielet = Some(List(ruotsinKieli)),
            vahvistus = vahvistusPaikkakunnalla(date(2007, 6, 3))
          )
      ),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2006, 8, 13), opiskeluoikeusLäsnä),
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2007, 6, 3), opiskeluoikeusValmistunut)
        )
      ),
      lisätiedot = Some(EsiopetuksenOpiskeluoikeudenLisätiedot(pidennettyOppivelvollisuus = Some(Päätösjakso(date(2008, 8, 15), Some(date(2016, 6, 4))))))
    ))
  )

  val examples = List(Example("esiopetus valmis", "Oppija on suorittanut peruskoulun esiopetuksen", esioppilas))
}

