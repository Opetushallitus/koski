package fi.oph.koski.perftest

import java.util.UUID
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExamplesLukio, PerusopetusExampleData}
import fi.oph.koski.perftest.OppilaitosImuri.{ammatillisetOppilaitokset, lukiot, peruskoulut}
import fi.oph.koski.schema.{Koodistokoodiviite, LähdejärjestelmäId, Oppilaitos}
import java.time.LocalDate

import scala.util.Random

object MixedOpiskeluoikeusInserter extends App {
  PerfTestRunner.executeTest(MixedOpiskeluoikeusInserterScenario)
}

object MixedOpiskeluoikeusInserterScenario extends FixtureDataInserterScenario {
  def lähdejärjestelmät = List("primus", "winnova", "helmi", "winha", "peppi", "studentaplus", "rediteq")
  def lähdejärjestelmäId = Some(LähdejärjestelmäId(Some(UUID.randomUUID().toString), Koodistokoodiviite(lähdejärjestelmät(Random.nextInt(lähdejärjestelmät.length)), "lahdejarjestelma")))

  def opiskeluoikeudet(x: Int) = {
    val peruskoulu = Oppilaitos(peruskoulut(Random.nextInt(peruskoulut.length)).oid, None, None)
    val lukio = Oppilaitos(lukiot(Random.nextInt(lukiot.length)).oid, None, None)
    val ammattikoulu = Oppilaitos(ammatillisetOppilaitokset(Random.nextInt(ammatillisetOppilaitokset.length)).oid, None, None)

    val perusopetuksenOpiskeluoikeus = PerusopetusExampleData.päättötodistusOpiskeluoikeus(peruskoulu, peruskoulu.toOidOrganisaatio).copy(lähdejärjestelmänId = lähdejärjestelmäId)

    val lukioTaiAmmattikouluOpiskeluoikeus = if(x % 3 == 0) {
      AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis(ammattikoulu, ammattikoulu.toOidOrganisaatio, tuotantoEPerusteidenMukainenKelvollinenValmistumispäivä).copy(lähdejärjestelmänId = lähdejärjestelmäId)
    } else {
      ExamplesLukio.päättötodistus(lukio, lukio.toOidOrganisaatio).copy(lähdejärjestelmänId = lähdejärjestelmäId)
    }

    List(perusopetuksenOpiskeluoikeus, lukioTaiAmmattikouluOpiskeluoikeus)
  }

  private val tuotantoEPerusteidenMukainenKelvollinenValmistumispäivä = LocalDate.of(2016, 8, 31)
}
