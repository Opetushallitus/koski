package fi.oph.koski.perftest

import java.nio.file.Paths

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.common.koskiuser.UserWithPassword

object RemoteYtrKoesuoritusFetcher extends App {
  // 1) Check from ytr-registry values of the field "copyOfExamPaper" and create a comma separated list with the different values
  // 2) Run this scenario with -DHETU=<ytr-oppija-hetu> -DKOESUORITUKSET=<comma-separated-list-of-copyOfExamPapers>
  PerfTestRunner.executeTest(RemoteYtrFetchKoesuoritusScenario)
}

object RemoteYtrFetchKoesuoritusScenario extends PerfTestScenario {
  private val hetu = requiredEnv("HETU")
  private val pdfs = requiredEnv("KOESUORITUKSET").split(",").toList
  private val kansalainenAuthHeaders = kansalainenLoginHeaders(hetu).toMap

  override def operation(round: Int): List[Operation] = {
    List(Operation(uri = s"koesuoritus/${pdfs(round % pdfs.length)}"))
  }

  override def authHeaders(user: UserWithPassword = defaultUser): Headers = kansalainenAuthHeaders
  override def readBody: Boolean = true
}
