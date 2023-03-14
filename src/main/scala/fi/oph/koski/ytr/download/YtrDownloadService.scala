package fi.oph.koski.ytr.download

import fi.oph.koski.cloudwatch.CloudWatchMetricsService
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonManipulation
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.schema.{KoskiSchema, Oppija, UusiHenkilö, YlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.util.Timing
import fi.oph.scalaschema.{SerializationContext, Serializer}
import rx.lang.scala.schedulers.NewThreadScheduler
import rx.lang.scala.{Observable, Scheduler}

import java.time.format.DateTimeFormatter
import java.time.LocalDate

class YtrDownloadService(
  val db: DB,
  application: KoskiApplication
) extends QueryMethods with Logging with Timing {
  val status = new YtrDownloadStatus(db)

  val oppijaConverter = new YtrDownloadOppijaConverter(
    application.koodistoViitePalvelu,
    application.organisaatioRepository,
    application.koskiLocalizationRepository
  )

  private val batchSize = application.config.getInt("ytr.download.batchSize").max(1).min(1500)
  private val extraSleepPerStudentInMs = application.config.getInt("ytr.download.extraSleepPerStudentInMs").max(0).min(100000)

  private lazy val defaultScheduler: Scheduler = NewThreadScheduler()

  // TODO: TOR-1639 metriikat cloudwatchiin
  // TODO: TOR-1639 paremmat logitukset
  private val cloudWatchMetrics = CloudWatchMetricsService.apply(application.config)

  def downloadAndShutdown(): Unit = {
    val config = Environment.ytrDownloadConfig

    download(
      birthmonthStart = config.birthmonthStart,
      birthmonthEnd = config.birthmonthEnd,
      modifiedSince = config.modifiedSince,
      force = config.force,
      onEnd = () => {
        logger.info(s"Ended downloading YTR data, shutting down...")
        shutdown
      }
    )
  }

  def download(
    birthmonthStart: Option[String] = None,
    birthmonthEnd: Option[String] = None,
    modifiedSince: Option[LocalDate] = None,
    force: Boolean = false,
    scheduler: Scheduler = defaultScheduler,
    onEnd: () => Unit = () => (),
  ): Unit = {
    (birthmonthStart, birthmonthEnd, modifiedSince) match {
      case _ if status.isLoading && !force =>
        logger.warn("YTR data already downloading, do nothing")
        onEnd()
      case (Some(birthmonthStart), Some(birthmonthEnd), _) =>
        startDownloadingUsingMonthInterval(birthmonthStart, birthmonthEnd, scheduler, onEnd)
      case (_, _, Some(modifiedSince)) =>
        startDownloadingUsingModifiedSince(modifiedSince, scheduler, onEnd)
      case _ =>
        logger.warn("Valid parameters for YTR download not defined")
        onEnd()
    }
  }

  private def startDownloadingUsingMonthInterval(
    birthmonthStart: String,
    birthmonthEnd: String,
    scheduler: Scheduler,
    onEnd: () => Unit
  ): Unit = {
    logger.info(s"Start downloading YTR data (birthmonthStart: ${birthmonthStart}, birthmonthEnd: ${birthmonthEnd}, batchSize: ${batchSize}, extraSleepPerStudentInMs: ${extraSleepPerStudentInMs})")

    status.setLoading

    val ssnDataObservable = splitToOneMonthIntervals(birthmonthStart, birthmonthEnd)
      .flatMap {
        case MonthParameters(birthmonthStart, birthmonthEnd) =>
          Observable.from(application.ytrClient.getHetutBySyntymäaika(birthmonthStart, birthmonthEnd))
      }

    startDownloadingAndUpdateToKoskiDatabase(
      createOppijatObservable(ssnDataObservable),
      scheduler,
      onEnd
    )
  }

  private def splitToOneMonthIntervals(birthmonthStart: String, birthmonthEnd: String): Observable[MonthParameters] = {
    val representativeStartDate = LocalDate.parse(birthmonthStart + "-01")
    val representativeEndDate = LocalDate.parse(birthmonthEnd + "-01")

    Observable.from(
      Iterator.iterate(representativeStartDate)(_.plusMonths(1))
        .takeWhile(_.isBefore(representativeEndDate))
        .map(startDate =>
          MonthParameters(
            startDate.format(DateTimeFormatter.ofPattern("yyyy-MM")),
            startDate.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"))
          )
        )
        .toIterable
    )
  }

  private def startDownloadingUsingModifiedSince(
    modifiedSince: LocalDate,
    scheduler: Scheduler,
    onEnd: () => Unit
  ): Unit = {
    logger.info(s"Start downloading YTR data (modifiedSince: ${modifiedSince.toString}, batchSize: ${batchSize}, extraSleepPerStudentInMs: ${extraSleepPerStudentInMs})")

    status.setLoading

    val ssnDataObservable = Observable.from(application.ytrClient.getHetutByModifiedSince(modifiedSince))

    startDownloadingAndUpdateToKoskiDatabase(
      createOppijatObservable(ssnDataObservable),
      scheduler,
      onEnd
    )
  }

  private def createOppijatObservable(ssnData: Observable[YtrSsnData]): Observable[YtrLaajaOppija] = {
    val groupedSsns = ssnData
      .doOnEach(o =>
        logger.info(s"Downloaded ${o.ssns.map(_.length).getOrElse('-')} ssns from YTR")
      )
      .map(_.sortedByBirthdays)
      .flatMap(a => Observable.from(a.ssns.toList.flatten))
      .tumblingBuffer(batchSize)
      .map(ssns => YtrSsnData(Some(ssns.toList)))

    val oppijat: Observable[YtrLaajaOppija] = groupedSsns
      .doOnEach(o =>
        logger.info(s"Downloading a batch of ${o.ssns.map(_.length).getOrElse("-")} students from YTR from ${o.minMonth} to ${o.maxMonth}")
      )
      .flatMap(a => Observable.from(application.ytrClient.oppijatByHetut(a)))

    oppijat
  }

  private def startDownloadingAndUpdateToKoskiDatabase(
    oppijatObservable: Observable[YtrLaajaOppija],
    scheduler: Scheduler,
    onEnd: () => Unit
  ): Unit = {
    var latestHandledBirthMonth = "-"
    var latestHandledBirthMonthCount = 0

    oppijatObservable
      .subscribeOn(scheduler)
      .subscribe(
        onNext = oppija => {
          timed("handleSingleOppija", thresholdMs = 1) {
            implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUserTallennetutYlioppilastutkinnonOpiskeluoikeudet
            implicit val accessType: AccessType.Value = AccessType.write

            // TODO: TOR-1639 Kunhan tätä on testattu try-catchien kanssa tuotannossa tarpeeksi, siisti koodi siten, että mahdolliset poikkeukset saa valua
            //  ylemmäksikin. Pitää myös miettiä silloin, onko ok, että yksittäisiä failaavia oppijoita skipataan, kuten koodi nyt tekee.
            try {
              val koskiOpiskeluoikeus =
                timed("convert", thresholdMs = 1) {
                  oppijaConverter.convertOppijastaOpiskeluoikeus(oppija)
                }

              koskiOpiskeluoikeus match {
                case Some(ytrOo) =>
                  val henkilö = UusiHenkilö(
                    hetu = oppija.ssn,
                    etunimet = oppija.firstNames,
                    sukunimi = oppija.lastName,
                    kutsumanimi = None
                  )

                  try {
                    val result =
                      timed("createOrUpdate", thresholdMs = 1) {
                        createOrUpdate(henkilö, ytrOo)
                      }

                    result match {
                      case Left(error) =>
                        logger.warn(s"YTR-datan tallennus epäonnistui (syntymäkuukausi ${oppija.birthMonth}): ${error.errorString.getOrElse("-")}")
                      case _ => timed("tallennaAlkuperäinenJson", thresholdMs = 1) {
                        tallennaAlkuperäinenJson(oppija)
                      }
                    }
                  } catch {
                    case e: Throwable => logger.warn(e)(s"YTR-datan tallennus epäonnistui (syntymäkuukausi ${oppija.birthMonth}): ${e.getMessage}")
                  }

                case _ => logger.info(s"YTR-datan konversio palautti tyhjän opiskeluoikeuden (syntymäkuukausi ${oppija.birthMonth})")
              }
            } catch {
              case e: Throwable => logger.warn(s"YTR-datan konversio epäonnistui (syntymäkuukausi ${oppija.birthMonth}): ${e.getMessage}")
            }

            val birthMonth = oppija.birthMonth
            if (latestHandledBirthMonth != birthMonth) {
              logger.info(s"Handled first oppija of birth month ${birthMonth}. Previously handled birth month ${latestHandledBirthMonth} had ${latestHandledBirthMonthCount} oppijas.")
              latestHandledBirthMonth = birthMonth
              latestHandledBirthMonthCount = 0
            }
            latestHandledBirthMonthCount = latestHandledBirthMonthCount + 1

            if (extraSleepPerStudentInMs > 0) {
              Thread.sleep(extraSleepPerStudentInMs)
            }
          }
        },
        onError = e => {
          logger.error(e)("YTR download failed:" + e.toString)
          logger.info(s"From final handled birth month ${latestHandledBirthMonth} handled ${latestHandledBirthMonthCount} oppijas.")
          status.setError
          onEnd()
        },
        onCompleted = () => {
          try {
            logger.info(s"Final handled birth month ${latestHandledBirthMonth} had ${latestHandledBirthMonthCount} oppijas.")
            status.setComplete
            // TODO: Tilastot yms.
            onEnd()
          } catch {
            case e: Throwable =>
              logger.error(e)("Exception in YTR download:" + e.toString)
              onEnd()
          }
        }
      )
  }

  private def tallennaAlkuperäinenJson(oppija: YtrLaajaOppija)(implicit user: KoskiSpecificSession, accessType: AccessType.Value)
  = {
    application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(
      hetu = oppija.ssn,
      userForAccessChecks = Some(user)
    ).map(_.oid).map(oppijaOid => {
      val serializationContext = SerializationContext(KoskiSchema.schemaFactory, KoskiSchema.skipSyntheticProperties)
      val fieldsToExcludeInJson = Set("ssn", "firstNames", "lastName")
      val serialisoituRaakaJson = JsonManipulation.removeFields(Serializer.serialize(oppija, serializationContext), fieldsToExcludeInJson)

      application.ytrPossu.createOrUpdateAlkuperäinenYTRJson(oppijaOid, serialisoituRaakaJson)
    })
  }

  def createOrUpdate(
    henkilö: UusiHenkilö,
    ytrOo: YlioppilastutkinnonOpiskeluoikeus
  )(implicit user: KoskiSpecificSession, accessType: AccessType.Value): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    application.validator.updateFieldsAndValidateOpiskeluoikeus(ytrOo, None) match {
      case Left(error) =>
        logger.warn(s"YTR-datan validointi epäonnistui: ${error.errorString.getOrElse("-")}")
        Left(error)
      case Right(_) =>
        val koskiOppija = Oppija(
          henkilö = henkilö,
          opiskeluoikeudet = List(ytrOo)
        )
        application.oppijaFacade.createOrUpdate(
          oppija = koskiOppija,
          allowUpdate = true,
          allowDeleteCompleted = true
        )
    }
  }

  def shutdown: Nothing = {
    Thread.sleep(60000) //Varmistetaan, että kaikki logit ehtivät varmasti siirtyä Cloudwatchiin ennen sulkemista.
    sys.exit()
  }
}

case class MonthParameters(
  birthmonthStart: String,
  birthmonthEnd: String
)
