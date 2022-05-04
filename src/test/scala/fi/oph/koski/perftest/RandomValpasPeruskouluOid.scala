package fi.oph.koski.perftest

import fi.oph.koski.util.EnvVariables
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö

import scala.io.Source
import scala.util.Random

class RandomValpasPeruskouluOid extends EnvVariables {
  private def organisaatioListaTiedosto: String = "src/test/resources/" + requiredEnv("KOSKI_VALPAS_ORGANISAATIOT_FILENAME")
  private def randomLines(filename: String) = Random.shuffle(Source.fromFile(filename).getLines().toList)
  private val oids = randomLines(organisaatioListaTiedosto)
  private var index = 0;
  private def nextFrom(list: List[String]) = this.synchronized {
    index = index + 1
    PeruskouluJaOppijat(list.apply(index % list.length))
  }

  def next = nextFrom(oids)
}

case class PeruskouluJaOppijat(
  oppilaitos: Organisaatio.Oid,
  oppijat: List[ValpasHenkilö.Oid],
)

object PeruskouluJaOppijat {
  def apply(source: String): PeruskouluJaOppijat = {
    val oppilaitos :: oppijat = source.split(" ").toList
    PeruskouluJaOppijat(oppilaitos, oppijat)
  }
}
