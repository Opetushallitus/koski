package fi.oph.koski.perftest
import java.time.LocalDate
import java.util.UUID

import fi.oph.koski.documentation._
import fi.oph.koski.perftest.OppilaitosImuri.{ammatillisetOppilaitokset, lukiot, peruskoulut}
import fi.oph.koski.schema.{Koodistokoodiviite, _}

import scala.util.Random

object MixedOpiskeluoikeusInserterUpdater extends App {
  PerfTestRunner.executeTest(MixedOpiskeluoikeusInserterUpdaterScenario)
}

object MixedOpiskeluoikeusInserterUpdaterScenario extends FixtureDataInserterScenario {
  def opiskeluoikeudet(x: Int) = MixedOpiskeluoikeusInserterScenario.opiskeluoikeudet(x).flatMap(oo => List(oo, muokkaa(oo)))

  private def muokkaa(oo: KoskeenTallennettavaOpiskeluoikeus) = {
    import mojave._


    val suorituksetT = traversal[KoskeenTallennettavaOpiskeluoikeus]
      .field[List[PäätasonSuoritus]]("suoritukset")
      .items

    val vahvistusPäiväT = suorituksetT
      .field[Option[Vahvistus]]("vahvistus")
      .items.field[LocalDate]("päivä")

    val osasuoritustenVahvistusT = suorituksetT
      .field[Option[List[Suoritus]]]("osasuoritukset").items.items
      .filter(_.vahvistus.isDefined)
      .field[Option[Vahvistus]]("vahvistus")

    val vahvistusSiirretty = vahvistusPäiväT
      .modify(oo)(_.minusDays(1))


    osasuoritustenVahvistusT.set(vahvistusSiirretty)(None)
  }
}