package fi.oph.koski.perftest

import fi.oph.koski.util.EnvVariables
import fi.oph.koski.schema.Henkilö

import scala.util.Random

class RandomValpasOppijaOidFromS3 extends EnvVariables {
  private val s3Key = optEnv("VALPAS_OPPIJAOIDIT_S3_KEY").filter(_.nonEmpty).getOrElse(ValpasPerftestS3.defaultOppijaOiditCsv)
  private val oids = Random.shuffle(ValpasPerftestS3.getLines(s3Key))
  private var index = 0

  private def nextFrom(list: List[Henkilö.Oid]) = this.synchronized {
    index = index + 1
    list.apply(index % list.length)
  }

  def next = nextFrom(oids)
}

