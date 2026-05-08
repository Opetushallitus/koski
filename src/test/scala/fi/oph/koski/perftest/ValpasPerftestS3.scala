package fi.oph.koski.perftest

import fi.oph.koski.util.EnvVariables
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

object ValpasPerftestS3 extends EnvVariables {
  val bucket = "valpas-perf-test-oppija-oids-qa"
  val defaultOppijaOiditCsv = "valpas_qa_oppija_oidit.csv"
  val defaultOrganisaatioJaOppijaOiditCsv = "valpas_qa_peruskoulujen_ja_oppijoiden_oidit.csv"

  private val credentialsProvider = {
    val builder = DefaultCredentialsProvider.builder()
    if (optEnv("CI").isEmpty) builder.profileName(env("AWS_PROFILE", "oph-koski-qa"))
    builder.build()
  }

  private val client = S3Client.builder
    .region(Region.EU_WEST_1)
    .credentialsProvider(credentialsProvider)
    .build

  def getLines(key: String): List[String] = {
    val request = GetObjectRequest.builder.bucket(bucket).key(key).build
    val bytes = client.getObjectAsBytes(request)
    bytes.asUtf8String().split("\n").toList.map(_.trim).filter(_.nonEmpty).drop(1)
  }
}
