package fi.oph.koski.todistus

import fi.oph.koski.config.{KoskiApplication, KoskiInstance}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, YleisenKielitutkinnonSuoritus}
import fi.oph.koski.schema.annotation.RedundantData
import fi.oph.koski.todistus.TodistusLanguage.TodistusLanguage
import fi.oph.koski.todistus.TodistusState.TodistusState

import java.io.OutputStream
import java.time.LocalDateTime
import java.util.UUID

class TodistusService(application: KoskiApplication) extends Logging {
  val workerId: String = application.ecsMetadata.taskARN.getOrElse("local")

  private val resultRepository = new TodistusResultRepository(application.config)
  private val todistusRepository: TodistusJobRepository = application.todistusRepository

  def currentStatus(req: TodistusIdRequest)(implicit user: KoskiSpecificSession): Either[HttpStatus, TodistusJob] = {
    todistusRepository
      .get(UUID.fromString(req.id))
      .toRight(KoskiErrorCategory.notFound())
  }

  def initiateGenerating(req: TodistusGenerateRequest)(implicit user: KoskiSpecificSession): Either[HttpStatus, TodistusJob] = {
    // TODO: TOR-2400: Toteuta hashin ja oo-versionumeron tarkistus yms., ettei pyyntöä tehdä, jos on jo tehty todistus tai todistuksen teko käynnissä
    val oppijanHenkilötiedotHash = "TODO"

    for {
      pyydetynOppijanOidit <- tarkistaOikeudetJaHaePyydetynOppijanKaikkiOidit(req, user)
      yleisenKielitutkinnonVahvistettuOpiskeluoikeus <- pyydetynOppijanKielitutkinnonVahvistettuOpiskeluoikeus(req, pyydetynOppijanOidit)
      job = TodistusJob(req, oppijanHenkilötiedotHash, yleisenKielitutkinnonVahvistettuOpiskeluoikeus)
      result <- todistusRepository.add(job).toRight(KoskiErrorCategory.badRequest("Todistuspyynnön luonti epäonnistui"))
    } yield result
  }

  private def tarkistaOikeudetJaHaePyydetynOppijanKaikkiOidit(req: TodistusGenerateRequest, user: KoskiSpecificSession): Either[HttpStatus, List[TodistusState]] = {
    for {
      huollettavat <- user.huollettavat.map(_.flatMap(_.oid).toList)
      kaikkiPääOppijaOiditJoihinOikeus = user.oid :: huollettavat
      henkilöt = kaikkiPääOppijaOiditJoihinOikeus.flatMap(oid => application.henkilöRepository.findByOid(oid).toList)
      kaikkiHenkilöOiditJoihinOikeus = henkilöt.flatMap(h => h.oid :: h.linkitetytOidit).distinct
      pyydetynOppijanHenkilö <- application.henkilöRepository.findByOid(req.oppijaOid).toRight(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
      pyydetynOppijanOidit = (pyydetynOppijanHenkilö.oid :: pyydetynOppijanHenkilö.linkitetytOidit).distinct
      _ <- Either.cond(
        kaikkiHenkilöOiditJoihinOikeus.exists(oid => pyydetynOppijanOidit.contains(oid)),
        (),
        KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia()
      )
    } yield pyydetynOppijanOidit
  }

  private def pyydetynOppijanKielitutkinnonVahvistettuOpiskeluoikeus(req: TodistusGenerateRequest, pyydetynOppijanOidit: List[String])(implicit user: KoskiSpecificSession): Either[HttpStatus, KielitutkinnonOpiskeluoikeus] = {
    for {
      rawOpiskeluoikeus <- application.possu.findByOidIlmanKäyttöoikeustarkistusta(req.opiskeluoikeusOid)
      _ <- Either.cond(
        !rawOpiskeluoikeus.mitätöity && pyydetynOppijanOidit.contains(rawOpiskeluoikeus.oppijaOid),
        (),
        KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
      )
      opiskeluoikeus = rawOpiskeluoikeus.toOpiskeluoikeusUnsafe
      yleisenKielitutkinnonVahvistettuOpiskeluoikeus <-
        opiskeluoikeus match {
          case ktOo: KielitutkinnonOpiskeluoikeus if ktOo.suoritukset.exists {
            case s: YleisenKielitutkinnonSuoritus if s.vahvistus.isDefined => true
            case _ => false
          } => Right(ktOo)
          case _ => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
        }
    } yield yleisenKielitutkinnonVahvistettuOpiskeluoikeus
  }

  def downloadCertificate(req: TodistusJob, output: OutputStream)(implicit user: KoskiSpecificSession): Unit = {
    // TODO: TOR-2400: Hae tiedosto, tai ehkä ennemmin presigned s3 URL?
    // resultRepository.fooBar...
  }

  def cancelAllMyJobs(reason: String): Boolean = todistusRepository.setRunningJobsFailed(reason)

  def truncate(): Int = todistusRepository.truncate

  def hasNext: Boolean = todistusRepository.numberOfQueuedJobs > 0

  def cleanup(koskiInstances: Seq[KoskiInstance]): Unit = {
    val instanceArns = koskiInstances.map(_.taskArn)

    todistusRepository
      .findOrphanedJobs(instanceArns)
      .foreach { todistus =>
        // TODO: TOR-2400: Uudelleenkäynnistä, jos ei ole jo yritetty turhan monta kertaa
      }
  }

  def hasWork: Boolean = todistusRepository.numberOfMyRunningJobs > 0 || todistusRepository.numberOfQueuedJobs > 0

  def runNext(): Unit = {
    todistusRepository.takeNext.foreach { todistus =>
      logStart(todistus)

      // TODO: TOR-2400: Toteuta

      logEnd(todistus)
    }
  }

  private def logStart(todistus: TodistusJob): Unit = {
    logger.info(s"Starting new for ${todistus.oppijaOid}/${todistus.opiskeluoikeusOid} as user ${todistus.userOid}")
    // TODO: TOR-2400: metriikat
  }

  private def logEnd(todistus: TodistusJob): Unit = {
    logger.info(s"Ending for ${todistus.oppijaOid}/${todistus.opiskeluoikeusOid} as user ${todistus.userOid}")
    // TODO: TOR-2400: metriikat
  }

}

object TodistusState {
  type TodistusState = String

  val QUEUED = "QUEUED"
  val GENERATING_RAW_PDF = "GENERATING_RAW_PDF"
  val SAVING_RAW_PDF = "SAVING_RAW_PDF"
  val STAMPING_PDF = "STAMPING_PDF"
  val SAVING_STAMPED_PDF = "SAVING_STAMPED_PDF"
  val COMPLETED = "COMPLETED"
  val ERROR = "ERROR"

  val runningStates: Set[String] = Set(GENERATING_RAW_PDF, SAVING_RAW_PDF, STAMPING_PDF, SAVING_STAMPED_PDF)

  val * : Set[String] = Set(QUEUED, GENERATING_RAW_PDF, SAVING_RAW_PDF, STAMPING_PDF, SAVING_STAMPED_PDF, COMPLETED, ERROR)
}

object TodistusLanguage {
  type TodistusLanguage = String

  val FI = "fi"
  val SV = "sv"
  val EN = "en"
}

object TodistusJob {
  def apply(req: TodistusGenerateRequest, oppijaHenkilötiedotHash: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSpecificSession): TodistusJob = TodistusJob(
    id = UUID.randomUUID().toString,
    userOid = user.oid,
    oppijaOid = req.oppijaOid,
    opiskeluoikeusOid = opiskeluoikeus.oid.get,
    language = req.language,
    opiskeluoikeusVersionumero = Some(opiskeluoikeus.versionumero.get),
    oppijaHenkilötiedotHash = Some(oppijaHenkilötiedotHash)
  )
}

case class TodistusJob(
  id: String,
  userOid: String, // käyttäjä, joka tämän käynnisti (voi olla eri kuin oppija_oid, jos esim. viranhaltija luo todistusta oppijalle)
  oppijaOid: String,
  opiskeluoikeusOid: String,
  language: TodistusLanguage, // pyydetty todistuksen kieli (fi/sv/en)
  opiskeluoikeusVersionumero: Option[Int], // oo-versio, mistä pdf on luotu. Voi käyttää, kun tarkistetaan, pitääkö todistus luoda uudestaan.
  oppijaHenkilötiedotHash: Option[String], // hash todistuksella näkyvistä oppijan henkilötiedoista (etunimet, sukunimi, syntymäaika jne.). Voi käyttää, kun tarkistetaan, pitääkö todistus luoda uudestaan.
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
