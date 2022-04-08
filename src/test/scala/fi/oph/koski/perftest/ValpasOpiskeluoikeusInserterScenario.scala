package fi.oph.koski.perftest

import java.time.LocalDate.{of => date}
import java.util.UUID

import fi.oph.koski.documentation.ExampleData.{suomenKieli, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData.{kaikkiAineet, perusopetuksenDiaarinumero, perusopetus, suoritustapaKoulutus}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.schema.{Koodistokoodiviite, LähdejärjestelmäId, NuortenPerusopetuksenOppimääränSuoritus, Oppilaitos, PerusopetuksenLuokkaAste, PerusopetuksenVuosiluokanSuoritus}

import scala.util.Random

abstract class ValpasOpiskeluoikeusInserterScenario {
  def lähdejärjestelmät = List("primus", "winnova", "helmi", "winha", "peppi", "studentaplus", "rediteq")
  def lähdejärjestelmäId = Some(LähdejärjestelmäId(Some(UUID.randomUUID().toString), Koodistokoodiviite(lähdejärjestelmät(Random.nextInt(lähdejärjestelmät.length)), "lahdejarjestelma")))
  val alkamispäivä = date(2021, 8, 15)
  val valmistumispäivä = date(2022, 6, 4)
  val peruskoulut = new RandomValpasPeruskouluOid()
  val luokka = "9A"


  def opiskeluoikeudet(x: Int) = {
    val peruskoulu = Oppilaitos(peruskoulut.next, None, None)

    val yhdeksännenLuokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
      koulutusmoduuli = PerusopetuksenLuokkaAste(9, perusopetuksenDiaarinumero),
      luokka = luokka,
      alkamispäivä = Some(alkamispäivä),
      vahvistus = vahvistusPaikkakunnalla(valmistumispäivä),
      toimipiste = peruskoulu,
      suorituskieli = suomenKieli
    )
    val perusopetuksenOppimäärä = NuortenPerusopetuksenOppimääränSuoritus(
      koulutusmoduuli = perusopetus,
      toimipiste = jyväskylänNormaalikoulu,
      vahvistus = vahvistusPaikkakunnalla(valmistumispäivä),
      suoritustapa = suoritustapaKoulutus,
      osasuoritukset = kaikkiAineet,
      suorituskieli = suomenKieli
    )

    val perusopetuksenOpiskeluoikeus =
      PerusopetusExampleData.opiskeluoikeus(
        peruskoulu,
        List(yhdeksännenLuokanSuoritus, perusopetuksenOppimäärä),
        alkamispäivä,
        Some(valmistumispäivä)
      )
    List(perusopetuksenOpiskeluoikeus)
  }

}
