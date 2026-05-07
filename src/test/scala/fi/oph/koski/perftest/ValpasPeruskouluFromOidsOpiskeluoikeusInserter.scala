package fi.oph.koski.perftest

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{OidHenkilö, Oppija}

import java.io.PrintWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters._

object ValpasPeruskouluFromOidsOpiskeluoikeusInserter extends App with Logging {
  PerfTestRunner.executeTest(ValpasFromOidsOpiskeluoikeusInserterScenario)

  val outputFile = ValpasPerftestS3.defaultOrganisaatioJaOppijaOiditCsv
  ValpasFromOidsOpiskeluoikeusInserterScenario.writeCsv(outputFile)
  logger.info(s"Tulokset kirjoitettiin tiedostoon: $outputFile")
}

object ValpasFromOidsOpiskeluoikeusInserterScenario extends ValpasOpiskeluoikeusInserterScenario with FixtureOidDataInserterScenario {
  override val luokka = "9A"

  private val insertedOppijatByOppilaitos = new ConcurrentHashMap[String, ConcurrentLinkedQueue[String]]()

  override def operation(x: Int) = {
    val opiskeluoikeus = opiskeluoikeudet(x)
    val henkilö = OidHenkilö(randomOid.next)
    opiskeluoikeus.foreach { oo =>
      oo.oppilaitos.foreach { oppilaitos =>
        insertedOppijatByOppilaitos
          .computeIfAbsent(oppilaitos.oid, _ => new ConcurrentLinkedQueue[String]())
          .add(henkilö.oid)
      }
    }
    opiskeluoikeus.map { oikeus =>
      val oppija = Oppija(henkilö, List(oikeus))
      val body = JsonSerializer.writeWithRoot(oppija).getBytes("utf-8")
      Operation("PUT", "api/oppija", body = body, gzip = true, responseCodes = responseCodes)
    }
  }

  def writeCsv(filename: String): Unit = {
    val writer = new PrintWriter(filename)
    writer.println("oppilaitos_oid,oppija_oidit")
    insertedOppijatByOppilaitos.entrySet().asScala.foreach { entry =>
      writer.println(s"${entry.getKey},${entry.getValue.asScala.mkString(" ")}")
    }
    writer.close()
  }
}
