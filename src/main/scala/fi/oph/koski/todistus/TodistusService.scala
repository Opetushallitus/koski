package fi.oph.koski.todistus

import fi.oph.koski.config.{KoskiApplication, KoskiInstance}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, YleisenKielitutkinnonSuoritus}
import fi.oph.koski.schema.annotation.RedundantData
import fi.oph.koski.todistus.BucketType.BucketType
import fi.oph.koski.todistus.TodistusLanguage.TodistusLanguage
import fi.oph.koski.todistus.TodistusState.TodistusState
import fi.oph.koski.todistus.swisscomclient.SwisscomClient
import fi.oph.koski.util.{ClasspathResource, Resource, TryWithLogging}
import software.amazon.awssdk.http.ContentStreamProvider

import java.io.InputStream
import java.time.LocalDateTime
import java.util.UUID
import scala.util.Using

class TodistusService(application: KoskiApplication) extends Logging {
  val workerId: String = application.ecsMetadata.taskARN.getOrElse("local")

  private val resultRepository = new TodistusResultRepository(application.config)
  private val todistusRepository: TodistusJobRepository = application.todistusRepository

  private val swisscomClient: SwisscomClient = application.swisscomClient

  lazy val mockTodistusResource: Resource = new ClasspathResource("/mockdata/todistus")

  def currentStatus(req: TodistusIdRequest)(implicit user: KoskiSpecificSession): Either[HttpStatus, TodistusJob] = {
    todistusRepository
      .get(req.id)
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

  def cancelAllMyJobs(reason: String): Boolean = todistusRepository.setRunningJobsFailed(reason)

  def truncate(): Int = todistusRepository.truncate

  def hasNext: Boolean = todistusRepository.numberOfQueuedJobs > 0

  def getDownloadUrl(bucketType: BucketType, job: TodistusJob): Either[HttpStatus, String] =
    TryWithLogging(logger, {
      resultRepository.getPresignedDownloadUrl(bucketType, job.id)
    }).left.map(t => KoskiErrorCategory.badRequest(s"Tiedostoa ei löydy tai tapahtui virhe sen jakamisessa"))

  def cleanup(koskiInstances: Seq[KoskiInstance]): Unit = {
    val instanceArns = koskiInstances.map(_.taskArn)

    todistusRepository
      .findOrphanedJobs(instanceArns)
      .foreach { todistus =>
        // TODO: TOR-2400: Uudelleenkäynnistä, jos ei ole jo yritetty turhan monta kertaa
      }
  }

  def hasWork: Boolean = {
    todistusRepository.numberOfMyRunningJobs > 0 || todistusRepository.numberOfQueuedJobs > 0
  }

  def runNext(): Unit = {
    todistusRepository.takeNext.foreach { todistus =>
      Using.Manager { use =>
        logStart(todistus)

        // TODO: TOR-2400: Pitäisikö catchätä jotain poikkeuksia ja jos tulee, merkitä ERROR tietokantaan? Nyt exceptionit menee vaan ylöspäin ja entry
        // jää jonotauluun keskeneräiseksi.

        todistusRepository.updateState(todistus.id, TodistusState.GENERATING_RAW_PDF, TodistusState.SAVING_RAW_PDF)

        mockTodistusResource.getInputStream("mock-todistus-raw.pdf").foreach(inputStream => {
          use(inputStream)
          resultRepository.putStream(BucketType.RAW, todistus.id, ContentStreamProvider.fromInputStream(inputStream))
        })

        todistusRepository.updateState(todistus.id, TodistusState.SAVING_RAW_PDF, TodistusState.STAMPING_PDF)
        todistusRepository.updateState(todistus.id, TodistusState.STAMPING_PDF, TodistusState.SAVING_STAMPED_PDF)

        // Lue tallennettu raw PDF
        val rawInputStream: InputStream = use(resultRepository.getStream(BucketType.RAW, todistus.id))

        // TODO: TOR-2400: Toistaiseksi vain verrataan, että luettu vastaa tallennettua.
        val rawExpectedInputStream = use(mockTodistusResource.getInputStream("mock-todistus-raw.pdf").get)
        val rawBytes = rawInputStream.readAllBytes()
        val rawExpectedBytes = rawExpectedInputStream.readAllBytes()
        assert(rawBytes.sameElements(rawExpectedBytes), "Raw PDF bytes do not match expected bytes")

        mockTodistusResource.getInputStream("mock-todistus-raw.pdf").foreach(inputStream => {
          use(inputStream)
          val outputStream = new java.io.ByteArrayOutputStream()
          swisscomClient.signWithStaticCertificate(todistus.id, inputStream, outputStream) match {
            case Right(_) =>
              resultRepository.putStream(BucketType.STAMPED, todistus.id, ContentStreamProvider.fromByteArray(outputStream.toByteArray))
              todistusRepository.updateState(todistus.id, TodistusState.SAVING_STAMPED_PDF, TodistusState.COMPLETED)
            case _ =>
              // TODO: TOR-2400: Tallenna virheilmoitus tietokantaan
              todistusRepository.updateState(todistus.id, TodistusState.SAVING_STAMPED_PDF, TodistusState.ERROR)
          }
        })

        logEnd(todistus)
      }
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
  // TODO: TOR-2400: Olisiko parempi vaan poistaa koko rivi ja tiedostot S3:sta? Riippuu siitä, miten siivousprosessi halutaan toteuttaa.
  val QUEUD_FOR_EXPIRE = "QUEUD_FOR_EXPIRE" // voi merkitä tämän, kun halutaan, että todistus poistetaan.
  val EXPIRED = "EXPIRED"// esim. siivousprosessi voi asettaa tämän, jos todistus on syystä tai toisesta vanhentunut (esim. oo mitätöity, tiedosto poistettu S3:sta tilan säästämiseksi tai muusta syystä jne.)

  val runningStates: Set[String] = Set(GENERATING_RAW_PDF, SAVING_RAW_PDF, STAMPING_PDF, SAVING_STAMPED_PDF)

  val * : Set[String] = Set(QUEUED, GENERATING_RAW_PDF, SAVING_RAW_PDF, STAMPING_PDF, SAVING_STAMPED_PDF, COMPLETED, ERROR, QUEUD_FOR_EXPIRE, EXPIRED)
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
  error: Option[String] = None
)
