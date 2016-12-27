package fi.oph.koski.perftest

import fi.oph.koski.documentation.ExamplesLukio
import fi.oph.koski.json.Json
import fi.oph.koski.organisaatio.{OrganisaatioHakuTulos, OrganisaatioPalveluOrganisaatio}
import fi.oph.koski.schema.{Koodistokoodiviite, Oppilaitos}

import scala.util.Random

object LukioFixtureDataInserter extends FixtureDataInserter {
  lazy val lukiot: List[OrganisaatioPalveluOrganisaatio] = Json.fromJValue[OrganisaatioHakuTulos](Json.readFile("ignore/lukiot.json")).organisaatiot

  lazy val opiskeluoikeudet = lukiot.map { org =>
    val oppilaitos = Oppilaitos(org.oid, org.oppilaitosKoodi.map(Koodistokoodiviite(_, "oppilaitosnumero")))
    ExamplesLukio.päättötodistus(oppilaitos = oppilaitos)
  }

  def opiskeluoikeudet(x: Int) = {
    Random.shuffle(opiskeluoikeudet)
  }
}
