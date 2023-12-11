package fi.oph.koski.perftest

import java.nio.file.Paths

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.UserWithPassword

object LocalYtrKoesuoritusFetcher extends App {
  // 1) Check values of "copyOfExamPaper"-fields in src/main/resources/mockdata/ytr/080698-703Y.json
  // 2) Add files to a directory that match those values
  // 3) Run Koski with -DPDF_DIR=/path/to/pdf-files
  // 4) Run this scenario with -DPDF_DIR=/path/to/pdf-files -DKOSKI_BASE_URL=http://localhost:7021/koski
  PerfTestRunner.executeTest(LocalYtrFetchKoesuoritusScenario)
}

object LocalYtrFetchKoesuoritusScenario extends PerfTestScenario {
  private val pdfDir = Paths.get(requiredEnv("PDF_DIR"))
  private val Some(hetu) = KoskiSpecificMockOppijat.ylioppilasLukiolainen.hetu
  private lazy val kansalainenAuthHeaders = kansalainenLoginHeaders(hetu).toMap

  override def operation(round: Int): List[Operation] = {
    List(Operation(uri = s"koesuoritus/${files(round % files.length)}"))
  }

  override def authHeaders(user: UserWithPassword = defaultUser): Headers = kansalainenAuthHeaders
  override def readBody: Boolean = true

  private def files =
    pdfDir.toFile.listFiles.filter(_.getName.toLowerCase.endsWith(".pdf")).toList.map(_.getName)
}
