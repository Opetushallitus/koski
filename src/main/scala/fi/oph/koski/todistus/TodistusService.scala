package fi.oph.koski.todistus

import fi.oph.koski.config.{KoskiApplication, KoskiInstance}
import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, Opiskeluoikeus, YleisenKielitutkinnonSuoritus}
import fi.oph.koski.todistus.BucketType.BucketType
import fi.oph.koski.todistus.swisscomclient.SwisscomClient
import fi.oph.koski.util.{ClasspathResource, Resource, TryWithLogging}
import software.amazon.awssdk.http.ContentStreamProvider

import java.io.InputStream
import java.security.MessageDigest
import scala.util.Using
import fi.oph.koski.util.ChainingSyntax.eitherChainingOps

import java.time.LocalDateTime
import java.util.UUID

class TodistusService(application: KoskiApplication) extends Logging {
  private val resultRepository = new TodistusResultRepository(application.config)
  private val todistusRepository: TodistusJobRepository = application.todistusRepository

  private val swisscomClient: SwisscomClient = application.swisscomClient

  lazy val mockTodistusResource: Resource = new ClasspathResource("/mockdata/todistus")

  def currentStatus(req: TodistusIdRequest)(implicit user: KoskiSpecificSession): Either[HttpStatus, TodistusJob] = {
    if (user.hasRole(OPHPAAKAYTTAJA)) {
      todistusRepository
        .get(req.id)
    } else {
      for {
        oppijaOidit <- haeOppijaOiditJoihinKansalaisellaOnOikeudet
        todistus <- todistusRepository.get(req.id, oppijaOidit)
      } yield todistus
    }
  }

  def checkStatus(req: TodistusGenerateRequest)(implicit user: KoskiSpecificSession): Either[HttpStatus, TodistusJob] = {
    for {
      yleisenKielitutkinnonVahvistettuOpiskeluoikeus <- kielitutkinnonVahvistettuOpiskeluoikeusJohonKutsujallaKäyttöoikeudet(req)
      oppijanHenkilö <- application.henkilöRepository.findByOid(yleisenKielitutkinnonVahvistettuOpiskeluoikeus.oppijaOid).toRight(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
      oppijanHenkilötiedotHash = laskeHenkilötiedotHash(oppijanHenkilö)
      opiskeluoikeusVersionumero = yleisenKielitutkinnonVahvistettuOpiskeluoikeus.versionumero
      todistus <- todistusRepository.findByParameters(
        yleisenKielitutkinnonVahvistettuOpiskeluoikeus.oid,
        req.language,
        opiskeluoikeusVersionumero,
        oppijanHenkilötiedotHash
      ).toRight(KoskiErrorCategory.notFound())
    } yield todistus
  }

  def checkAccessAndInitiateGenerating(req: TodistusGenerateRequest)(implicit user: KoskiSpecificSession): Either[HttpStatus, TodistusJob] = {
    val uusiJobId = UUID.randomUUID().toString

    logSkedulointiAlkaa(uusiJobId, req)

    val result = for {
      yleisenKielitutkinnonVahvistettuOpiskeluoikeus <- kielitutkinnonVahvistettuOpiskeluoikeusJohonKutsujallaKäyttöoikeudet(req)
      oppijanHenkilö <- application.henkilöRepository.findByOid(yleisenKielitutkinnonVahvistettuOpiskeluoikeus.oppijaOid).toRight(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
      job = TodistusJob(uusiJobId, req, laskeHenkilötiedotHash(oppijanHenkilö), yleisenKielitutkinnonVahvistettuOpiskeluoikeus)
      result <- todistusRepository.addOrReuseExisting(job)
    } yield result

    result match {
      case Right(job) =>
        logSkedulointiValmis(uusiJobId, job)
        logJononTilanne()
        Right(job)
      case Left(error) =>
        logSkedulointiEpäonnistui(uusiJobId, req, error)
        logJononTilanne()
        Left(error)
    }
  }

  private def laskeHenkilötiedotHash(henkilö: OppijaHenkilö): String = {
    val data = s"${henkilö.etunimet}|${henkilö.sukunimi}|${henkilö.syntymäaika.getOrElse("")}"
    val digest = MessageDigest.getInstance("SHA-256")
    digest.digest(data.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }

  private def haeOppijaOiditJoihinKansalaisellaOnOikeudet(implicit user: KoskiSpecificSession): Either[HttpStatus, Set[String]] = {
    for {
      huollettavat <- user.huollettavat.map(_.flatMap(_.oid).toList).fold(
        s => {
          logger.warn(s"Huollettavien haku epäonnistui. Tehdään oletus, että huollettavia ei ole. ${s.toString}")
          Right(List.empty[String])
        },
        Right(_)
      )
      kaikkiPääOppijaOiditJoihinOikeus = user.oid :: huollettavat
      henkilöt = kaikkiPääOppijaOiditJoihinOikeus.flatMap(oid => application.henkilöRepository.findByOid(oid).toList)
      kaikkiHenkilöOiditJoihinOikeus = henkilöt.flatMap(h => h.oid :: h.linkitetytOidit).toSet
    } yield kaikkiHenkilöOiditJoihinOikeus
  }

  private def kielitutkinnonVahvistettuOpiskeluoikeusJohonKutsujallaKäyttöoikeudet(req: TodistusGenerateRequest)(implicit user: KoskiSpecificSession): Either[HttpStatus, KoskiOpiskeluoikeusRow] = {
    for {
      rawOpiskeluoikeus <- application.possu.findByOidIlmanKäyttöoikeustarkistusta(req.opiskeluoikeusOid)
      validatedOpiskeluoikeus <- tarkistaKäyttöoikeudetOpiskeluoikeuteen(rawOpiskeluoikeus)
      opiskeluoikeus = validatedOpiskeluoikeus.toOpiskeluoikeusUnsafe
      _ <- tarkistaOnVahvistettuYleisenKielitutkinnonOpiskeluoikeus(opiskeluoikeus)
    } yield validatedOpiskeluoikeus
  }

  private def tarkistaKäyttöoikeudetOpiskeluoikeuteen(rawOpiskeluoikeus: KoskiOpiskeluoikeusRow)(implicit user: KoskiSpecificSession): Either[HttpStatus, KoskiOpiskeluoikeusRow] = {
    if (rawOpiskeluoikeus.mitätöity) {
      return Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    }

    if (user.hasRole(OPHPAAKAYTTAJA)) {
      Right(rawOpiskeluoikeus)
    } else {
      tarkistaKansalaisenKäyttöoikeudetOpiskeluoikeuteen(rawOpiskeluoikeus)
    }
  }

  private def tarkistaKansalaisenKäyttöoikeudetOpiskeluoikeuteen(rawOpiskeluoikeus: KoskiOpiskeluoikeusRow)(implicit user: KoskiSpecificSession): Either[HttpStatus, KoskiOpiskeluoikeusRow] = {
    for {
      oppijatOiditJoihinOikeus <- haeOppijaOiditJoihinKansalaisellaOnOikeudet
      _ <- Either.cond(
        oppijatOiditJoihinOikeus.contains(rawOpiskeluoikeus.oppijaOid),
        (),
        KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
      )
    } yield rawOpiskeluoikeus
  }

  private def tarkistaOnVahvistettuYleisenKielitutkinnonOpiskeluoikeus(opiskeluoikeus: Opiskeluoikeus): Either[HttpStatus, Unit] = {
    opiskeluoikeus match {
      case ktOo: KielitutkinnonOpiskeluoikeus if onVahvistettuYleisenKielitutkinnonSuoritus(ktOo) =>
        Right(())
      case _ =>
        Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    }
  }

  private def onVahvistettuYleisenKielitutkinnonSuoritus(ktOo: KielitutkinnonOpiskeluoikeus): Boolean = {
    ktOo.suoritukset.exists {
      case s: YleisenKielitutkinnonSuoritus => s.vahvistus.isDefined
      case _ => false
    }
  }

  def markAllMyJobsInterrupted(): Unit = {
    todistusRepository.markAllMyJobsInterrupted()
  }

  def hasNext: Boolean = todistusRepository.numberOfQueuedJobs > 0

  def getDownloadUrl(bucketType: BucketType, filename: String, job: TodistusJob): Either[HttpStatus, String] =
    for {
      _ <- validateOpiskeluoikeusExistsForDownloadAccess(job)
      result <- TryWithLogging(logger, {
        resultRepository.getPresignedDownloadUrl(bucketType, filename, job.id)
      }).left.map(t => KoskiErrorCategory.badRequest(s"Tiedostoa ei löydy tai tapahtui virhe sen jakamisessa"))
    } yield result

  def getDownloadStream(bucketType: BucketType, job: TodistusJob): Either[HttpStatus, InputStream] =
    for {
      _ <- validateOpiskeluoikeusExistsForDownloadAccess(job)
      result <- resultRepository.getStream(bucketType, job.id)
    } yield result

  private def validateOpiskeluoikeusExistsForDownloadAccess(todistusJob: TodistusJob): Either[HttpStatus, TodistusJob] = {
    for {
      rawOpiskeluoikeus <- application.possu.findByOidIlmanKäyttöoikeustarkistusta(todistusJob.opiskeluoikeusOid)
      _ <- Either.cond(
        !rawOpiskeluoikeus.mitätöity,
        (),
        KoskiErrorCategory.unavailable.todistus.opiskeluoikeusMitatoity()
      )
    } yield todistusJob
  }

  def cleanup(koskiInstances: Seq[KoskiInstance]): Unit = {
    val instanceArns = koskiInstances.map(_.taskArn)
    val maxAttempts = 3

    // TODO: TOR-2400: merkitse QUEUD_FOR_EXPIRE:ksi todistukset, jotka ovat määräaikaa (esim. vuosi?) vanhempia.
    // TODO: TOR-2400: Poista S3:sta jotain määräaikaa (2 vuotta?) vanhemmat todistukset? Ehkä joku erillinen eräajo,
    // mikä vaatii manuaalisen stepin vahvistukseksi, eikä tässä?

    todistusRepository
      .findOrphanedJobs(instanceArns)
      .foreach { todistus =>
        val attemptsCount = todistus.attempts.getOrElse(0)
        if (attemptsCount < maxAttempts) {
          logger.info(s"Uudelleenkäynnistetään orpo todistus ${todistus.id} (yritys ${attemptsCount}/${maxAttempts})")
          todistusRepository.requeueJob(todistus.id)
        } else {
          val errorMessage = s"Todistuksen ${todistus.id} luonti epäonnistui ${attemptsCount} yrityksen jälkeen"
          logger.error(s"Orpo todistus ${todistus.id}: ${errorMessage}")
          todistusRepository.setJobFailed(todistus.id, errorMessage)
        }
      }
  }

  def runNext(): Unit = {
    todistusRepository.takeNext.foreach { todistus =>
      // TODO: TOR-2400: Onko mahdollista, että todistuksilla näkyy jotain sensitiivistä dataa? Jos ei ole, niin tee ja käytä käyttäjätunnusta, joka ei niihin pääse edes käsiksi. Selviää, kun todistuspohjat valmiina.
      implicit val systemUser = KoskiSpecificSession.systemUser

      Using.Manager { use =>
        logGenerointiAlkaa(todistus)

        val generoituTodistus: Either[HttpStatus, TodistusJob] = for {
          //
          // GATHERING_INPUT: Hae tuoreet henkilötiedot ja opiskeluoikeus, joista todistus generoidaan, sekä tallenna ne tietokantaan.
          //
          oppijanHenkilö <-
            application.henkilöRepository.findByOid(oid = todistus.oppijaOid, findMasterIfSlaveOid = true)
              .toRight(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
          oppijanHenkilötiedotHash = laskeHenkilötiedotHash(oppijanHenkilö)
          rawOpiskeluoikeus <- application.opiskeluoikeusRepository.findByOid(todistus.opiskeluoikeusOid)
          opiskeluoikeus <- TryWithLogging(logger, {
            rawOpiskeluoikeus.toOpiskeluoikeusUnsafe
          }).left.map(t => KoskiErrorCategory.internalError("Deserialisointi epäonnistui"))
          opiskeluoikeusVersionumero <- opiskeluoikeus.versionumero.toRight({
            val error = s"Opiskeluoikeudella ei ole versionumeroa, todistus ${todistus.id}"
            logger.error(error)
            KoskiErrorCategory.internalError(error)
          })

          todistus <- todistusRepository.updateStateWithHashAndVersion(
            todistus.id,
            TodistusState.GATHERING_INPUT,
            TodistusState.GENERATING_RAW_PDF,
            oppijanHenkilötiedotHash,
            opiskeluoikeusVersionumero
          )

          //
          // GENERATING_RAW_PDF: Generoi PDF-todistus, jota ei ole vielä allekirjoitettu
          //
          // TODO: TOR-2400: Generoi todistus-PDF kerätyistä tiedoista

          //
          // SAVING_RAW_PDF: Tallenna allekirjoittamaton todistus S3:een
          //
          todistus <- todistusRepository.updateState(todistus.id, TodistusState.GENERATING_RAW_PDF, TodistusState.SAVING_RAW_PDF)
          // TODO: TOR-2400: Korvaa tämä mock-datan tallentaminen oikealla toteutuksella ja tallenna generoitu raw-PDF
          mockIs <- mockTodistusResource.getInputStream("mock-todistus-raw.pdf")
            .toRight(KoskiErrorCategory.internalError(s"Mock-datan luku epäonnistui todistukselle ${todistus.id}"))
            .tap(is => use(is))
          _ <- resultRepository.putStream(BucketType.RAW, todistus.id, ContentStreamProvider.fromInputStream(mockIs))
          // TODO: TOR-2400: Verrataan, että luettu vastaa tallennettua, poista tämä, kun oikea toteutus tehty
          rawInputStream <- resultRepository.getStream(BucketType.RAW, todistus.id)
            .tap(is => use(is))
          rawExpectedInputStream = use(mockTodistusResource.getInputStream("mock-todistus-raw.pdf").get)
          rawBytes = rawInputStream.readAllBytes()
          rawExpectedBytes = rawExpectedInputStream.readAllBytes()
          _ = assert(rawBytes.sameElements(rawExpectedBytes), "Raw PDF bytes do not match expected bytes")

          //
          // STAMPING_PDF: Allekirjoita PDF
          //
          _ <- todistusRepository.updateState(todistus.id, TodistusState.SAVING_RAW_PDF, TodistusState.STAMPING_PDF)
          rawInputStream <- resultRepository.getStream(BucketType.RAW, todistus.id)
            .tap(is => use(is))
          outputStream = new java.io.ByteArrayOutputStream()
          _ <- swisscomClient.signWithStaticCertificate(todistus.id, rawInputStream, outputStream)

          //
          // SAVING_STAMPED_PDF: Tallenna allekirjoitettu PDF
          //
          todistus <- todistusRepository.updateState(todistus.id, TodistusState.STAMPING_PDF, TodistusState.SAVING_STAMPED_PDF)
          _ <- resultRepository.putStream(BucketType.STAMPED, todistus.id, ContentStreamProvider.fromByteArray(outputStream.toByteArray))

          //
          // COMPLETED: Valmis
          //
          todistus <- todistusRepository.updateState(
            todistus.id,
            TodistusState.SAVING_STAMPED_PDF,
            TodistusState.COMPLETED,
            completedAt = Some(LocalDateTime.now())
          )
        } yield todistus

        generoituTodistus match {
          case Right(todistus) =>
            logGenerointiValmis(todistus)
          case Left(error) =>
            logGenerointiEpäonnistui(todistus, error.toString)
            todistusRepository.setJobFailed(todistus.id, error.toString)
        }
      }
    }
  }

  private def logSkedulointiAlkaa(uusiJobId: String, req: TodistusGenerateRequest)(implicit user: KoskiSpecificSession): Unit = {
    val konteksti = teeKonteksti(uusiJobId, "EI TIEDOSSA", req.opiskeluoikeusOid, req.language, user.user.oid)
    logger.info(s"Lisää jonoon, $konteksti")
  }

  private def logSkedulointiValmis(uusiJobId: String, todistus: TodistusJob): Unit = {
    if (uusiJobId == todistus.id) {
      val konteksti = teeKonteksti(todistus.id, todistus.oppijaOid, todistus.opiskeluoikeusOid, todistus.language, todistus.userOid.getOrElse("EI TIEDOSSA"))
      logger.info(s"Lisätty jonoon, $konteksti")
    } else {
      val konteksti = teeKonteksti(s"pyydetty:$uusiJobId,palautettu ${todistus.id}", todistus.oppijaOid, todistus.opiskeluoikeusOid, todistus.language, todistus.userOid.getOrElse("EI TIEDOSSA"))
      logger.info(s"Ei lisätty jonoon: Pyyntö on jo jonossa, $konteksti")
    }
  }

  private def logJononTilanne(): Unit = {
    val running = todistusRepository.numberOfRunningJobs
    val queued = todistusRepository.numberOfQueuedJobs

    logger.info(s"Todistuksia jonossa:$queued käsittelyssä:$running")
  }

  private def logSkedulointiEpäonnistui(uusiJobId: String, req: TodistusGenerateRequest, status: HttpStatus)(implicit user: KoskiSpecificSession): Unit = {
    val konteksti = teeKonteksti(uusiJobId, "EI TIEDOSSA", req.opiskeluoikeusOid, req.language, user.user.oid)
    logger.error(s"Jonoon lisäys epäonnistui, $konteksti: ${status.toString}")
  }

  private def logGenerointiAlkaa(todistus: TodistusJob): Unit = {
    val konteksti = teeKonteksti(todistus.id, todistus.oppijaOid, todistus.opiskeluoikeusOid, todistus.language, todistus.userOid.getOrElse("EI TIEDOSSA"))
    logger.info(s"Aloita generointi, $konteksti")

    // TODO: TOR-2400: metriikat Cloudwatchiin?
  }

  private def logGenerointiValmis(todistus: TodistusJob): Unit = {
    val konteksti = teeKonteksti(todistus.id, todistus.oppijaOid, todistus.opiskeluoikeusOid, todistus.language, todistus.userOid.getOrElse("EI TIEDOSSA"))
    logger.info(s"Generointi valmis, $konteksti")

    // TODO: TOR-2400: metriikat Cloudwatchiin?
  }

  private def logGenerointiEpäonnistui(todistus: TodistusJob, error: String): Unit = {
    val konteksti = teeKonteksti(todistus.id, todistus.oppijaOid, todistus.opiskeluoikeusOid, todistus.language, todistus.userOid.getOrElse("EI TIEDOSSA"))
    logger.error(s"Generointi epäonnistui $error, $konteksti")
  }

  private def teeKonteksti(id: String, oppijaOid: String, opiskeluoikeusOid: String, language: String, user: String): String =
    s"job:${id}/oppija:${oppijaOid}/oo:${opiskeluoikeusOid}/lang:${language}/user:${user}"
}
