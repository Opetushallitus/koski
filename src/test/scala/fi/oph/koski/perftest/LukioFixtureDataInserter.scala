package fi.oph.koski.perftest

import fi.oph.koski.documentation.ExamplesLukio
import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.organisaatio.{OrganisaatioHakuTulos, OrganisaatioPalveluOrganisaatio}
import fi.oph.koski.schema.{Koodistokoodiviite, Oppilaitos}

import scala.util.Random

object LukioFixtureDataInserter extends App {
  PerfTestRunner.executeTest(LukioFixtureDataInserterScenario)
}

object LukioFixtureDataInserterScenario extends FixtureDataInserterScenario {
  lazy val lukiot: List[OrganisaatioPalveluOrganisaatio] = JsonSerializer.extract[OrganisaatioHakuTulos](JsonFiles.readFile("ignore/lukiot.json"), ignoreExtras = true).organisaatiot

  lazy val opiskeluoikeudet = lukiot.map { org =>
    val oppilaitos = Oppilaitos(org.oid, org.oppilaitosKoodi.map(Koodistokoodiviite(_, "oppilaitosnumero")))
    ExamplesLukio.päättötodistus(oppilaitos = oppilaitos)
  }

  def opiskeluoikeudet(x: Int) = {
    Random.shuffle(opiskeluoikeudet)
  }
}
