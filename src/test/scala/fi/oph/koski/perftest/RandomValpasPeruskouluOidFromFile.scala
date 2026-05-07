package fi.oph.koski.perftest

import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.EnvVariables

import scala.io.Source
import scala.util.Random

class RandomValpasPeruskouluOidFromFile extends EnvVariables {
  private val filename = env("KOSKI_VALPAS_ORGANISAATIOT_FILENAME", "valpas_local_peruskoulujen_oidit.txt")
  private val oids = Random.shuffle(Source.fromFile(filename).getLines().toList.filter(_.nonEmpty))
  private var index = 0

  private def nextFrom(list: List[Organisaatio.Oid]) = this.synchronized {
    index = index + 1
    list.apply(index % list.length)
  }

  def next: Organisaatio.Oid = nextFrom(oids)
}
