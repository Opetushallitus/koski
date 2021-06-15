package fi.oph.koski.perftest

import fi.oph.koski.util.EnvVariables
import fi.oph.koski.schema.Henkilö

import scala.io.Source
import scala.util.Random

class RandomValpasOppijaOid extends EnvVariables {
  private def oppijaOiditTiedosto: String = "src/test/resources/" + requiredEnv("KOSKI_VALPAS_OPPIJAOIDIT_FILENAME")
  private def randomLines(filename: String) = Random.shuffle(Source.fromFile(filename).getLines().toList)
  private val oids = randomLines(oppijaOiditTiedosto)
  private var index = 0;
  private def nextFrom(list: List[Henkilö.Oid]) = this.synchronized {
    index = index + 1
    list.apply(index % list.length)
  }

  def next = nextFrom(oids)
}

