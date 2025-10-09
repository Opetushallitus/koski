package fi.oph.koski.todistus

import fi.oph.koski.config.{KoskiApplication, KoskiInstance}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.annotation.RedundantData
import fi.oph.koski.todistus.TodistusLanguage.TodistusLanguage
import fi.oph.koski.todistus.TodistusState.TodistusState

import java.io.OutputStream
import java.time.{LocalDateTime}
import java.util.UUID

class TodistusService(application: KoskiApplication) extends Logging {
  val workerId: String = application.ecsMetadata.taskARN.getOrElse("local")

  private val resultRepository = new TodistusResultRepository(application.config)
  private val todistusRepository: TodistusRepository = application.todistusRepository

  def currentStatus(req: TodistusIdRequest)(implicit user: KoskiSpecificSession): Either[HttpStatus, Todistus] = {
    todistusRepository
      .get(UUID.fromString(req.id))
      .toRight(KoskiErrorCategory.notFound())
  }

  def initiateGenerating(req: TodistusGenerateRequest)(implicit user: KoskiSpecificSession): Either[HttpStatus, Unit] = {
    // TODO: TOR-2400: Toteuta
    Left(KoskiErrorCategory.notImplemented())
  }

  def downloadCertificate(req: Todistus, output: OutputStream)(implicit user: KoskiSpecificSession): Unit = {
    // TODO: TOR-2400: Hae tiedosto, tai ehkä ennemmin presigned s3 URL?
    // resultRepository.fooBar...
  }

  def cancelAllTasks(reason: String): Boolean = todistusRepository.setRunningTasksFailed(reason)

  def hasNext: Boolean = todistusRepository.numberOfQueuedTasks > 0

  def cleanup(koskiInstances: Seq[KoskiInstance]): Unit = {
    val instanceArns = koskiInstances.map(_.taskArn)

    todistusRepository
      .findOrphaned(instanceArns)
      .foreach { todistus =>
        // TODO: TOR-2400: Uudelleenkäynnistä, jos ei ole jo yritetty turhan monta kertaa
      }
  }


  def runNext(): Unit = {
    todistusRepository.takeNext.foreach { todistus =>
      logStart(todistus)

      // TODO: TOR-2400: Toteuta
    }
  }

  private def logStart(todistus: Todistus): Unit = {
    logger.info(s"Starting new for ${todistus.oppijaOid}/${todistus.opiskeluoikeusOid} as user ${todistus.userOid}")
    // TODO: TOR-2400: metriikat
  }

}

object TodistusState {
  type TodistusState = String

  val QUEUED = "QUEUED"
  val GENERATING_RAW_PDF = "GENERATING_RAW_PDF"
  val SAVING_RAW_PDF = "SAVING_RAW_PDF"
  val CONSTRUCTING_STAMP_REQUEST = "CONSTRUCTING_STAMP_REQUEST"
  val WAITING_STAMP_RESPONSE = "WAITING_STAMP_RESPONSE"
  val STAMPING_PDF = "STAMPING_PDF"
  val SAVING_STAMPED_PDF = "SAVING_STAMPED_PDF"
  val COMPLETED = "COMPLETED"
  val ERROR = "ERROR"

  val runningStates: Set[String] = Set(GENERATING_RAW_PDF, SAVING_RAW_PDF, CONSTRUCTING_STAMP_REQUEST, WAITING_STAMP_RESPONSE, STAMPING_PDF, SAVING_STAMPED_PDF)

  val * : Set[String] = Set(QUEUED, GENERATING_RAW_PDF, SAVING_RAW_PDF, CONSTRUCTING_STAMP_REQUEST, WAITING_STAMP_RESPONSE, STAMPING_PDF, SAVING_STAMPED_PDF, COMPLETED, ERROR)
}

object TodistusLanguage {
  type TodistusLanguage = String

  val FI = "fi"
  val SV = "sv"
  val EN = "en"
}

case class Todistus(
  id: UUID,
  userOid: String, // käyttäjä, joka tämän käynnisti (voi olla eri kuin oppija_oid, jos esim. viranhaltija luo todistusta oppijalle)
  oppijaOid: String,
  opiskeluoikeusOid: String,
  language: TodistusLanguage, // pyydetty todistuksen kieli (fi/sv/en)
  opiskeluoikeusVersionumero: Option[Int], // oo-versio, mistä pdf on luotu. Voi käyttää, kun tarkistetaan, pitääkö todistus luoda uudestaan.
  oppijaHenkilotiedotHash: Option[String],  // hash todistuksella näkyvistä oppijan henkilötiedoista (etunimet, sukunimi, syntymäaika jne.). Voi käyttää, kun tarkistetaan, pitääkö todistus luoda uudestaan.
  state: TodistusState = TodistusState.QUEUED,
  createdAt: LocalDateTime = LocalDateTime.now(),
  startedAt: Option[LocalDateTime] = None,
  completedAt: Option[LocalDateTime] = None,
  @RedundantData // Piilotetaan loppukäyttäjiltä
  worker: Option[String] = None,
  @RedundantData // Piilotetaan loppukäyttäjiltä
  rawS3ObjectKey: Option[String] = None,
  @RedundantData // Piilotetaan loppukäyttäjiltä
  signedS3ObjectKey: Option[String] = None,
  error: Option[String] = None
)
