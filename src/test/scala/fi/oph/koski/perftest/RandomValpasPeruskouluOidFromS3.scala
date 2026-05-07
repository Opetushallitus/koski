package fi.oph.koski.perftest

import fi.oph.koski.util.EnvVariables
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö

import scala.util.Random

class RandomValpasPeruskouluOidFromS3 extends EnvVariables {
  private val s3Key = env("KOSKI_VALPAS_ORGANISAATIOT_S3_KEY", ValpasPerftestS3.defaultOrganisaatioJaOppijaOiditCsv)
  private val oids = Random.shuffle(ValpasPerftestS3.getLines(s3Key))
  private var index = 0

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
    val Array(oppilaitosOid, oppijatStr) = source.split(",", 2)
    PeruskouluJaOppijat(oppilaitosOid, oppijatStr.split(" ").toList)
  }
}
