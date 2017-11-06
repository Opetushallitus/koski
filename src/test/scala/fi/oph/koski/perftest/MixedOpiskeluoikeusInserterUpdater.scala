package fi.oph.koski.perftest
import java.time.LocalDate
import java.util.UUID

import fi.oph.koski.documentation._
import fi.oph.koski.perftest.OppilaitosImuri.{ammatillisetOppilaitokset, lukiot, peruskoulut}
import fi.oph.koski.schema.{Koodistokoodiviite, _}

import scala.util.Random

object MixedOpiskeluoikeusInserterUpdater extends App {
  PerfTestRunner.executeTest(MixedOpiskeluoikeusInserterScenarioUpdater)
}

object MixedOpiskeluoikeusInserterScenarioUpdater extends FixtureDataInserterScenario {
  def lähdejärjestelmät = List("primus", "winnova", "helmi", "winha", "peppi", "studentaplus", "rediteq")
  def lähdejärjestelmäId = Some(LähdejärjestelmäId(Some(UUID.randomUUID().toString), Koodistokoodiviite(lähdejärjestelmät(Random.nextInt(lähdejärjestelmät.length)), "lahdejarjestelma")))

  def opiskeluoikeudet(x: Int) = {
    val peruskoulu = Oppilaitos(peruskoulut(Random.nextInt(peruskoulut.length)).oid, None, None)
    val lukio = Oppilaitos(lukiot(Random.nextInt(lukiot.length)).oid, None, None)
    val ammattikoulu = Oppilaitos(ammatillisetOppilaitokset(Random.nextInt(ammatillisetOppilaitokset.length)).oid, None, None)

    val perusopetuksenOpiskeluoikeus = PerusopetusExampleData.päättötodistusOpiskeluoikeus(peruskoulu, peruskoulu.toOidOrganisaatio).copy(lähdejärjestelmänId = lähdejärjestelmäId)

    val lukioTaiAmmattikouluOpiskeluoikeus = if(x % 3 == 0) {
      AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis(ammattikoulu, ammattikoulu.toOidOrganisaatio).copy(lähdejärjestelmänId = lähdejärjestelmäId)
    } else {
      ExamplesLukio.päättötodistus(lukio, lukio.toOidOrganisaatio).copy(lähdejärjestelmänId = lähdejärjestelmäId)
    }

    List(
      perusopetuksenOpiskeluoikeus,
      muokkaa(perusopetuksenOpiskeluoikeus),
      lukioTaiAmmattikouluOpiskeluoikeus,
      muokkaa(lukioTaiAmmattikouluOpiskeluoikeus)
    )
  }

  private def muokkaa(perusopetuksenOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
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
      .modify(perusopetuksenOpiskeluoikeus)(_.minusDays(1))


    osasuoritustenVahvistusT.set(vahvistusSiirretty)(None)
  }
}