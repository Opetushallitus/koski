package fi.oph.koski.ytr.download

import fi.oph.koski.cloudwatch.CloudWatchMetricsService
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log.Logging
import rx.lang.scala.schedulers.NewThreadScheduler
import rx.lang.scala.{Observable, Scheduler}

import java.time.format.DateTimeFormatter
import java.time.LocalDate

class YtrDownloadService(
  val db: DB,
  application: KoskiApplication
) extends QueryMethods with Logging {
  val status = new YtrDownloadStatus(db)

  val converter = new YtrDownloadOppijaConverter(
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
        logger.info("YTR data already downloading, do nothing")
        onEnd()
      case (Some(birthmonthStart), Some(birthmonthEnd), _) =>
        startDownloadingUsingMonthInterval(birthmonthStart, birthmonthEnd, scheduler, onEnd)
      case (_, _, Some(modifiedSince)) =>
        startDownloadingUsingModifiedSince(modifiedSince, scheduler, onEnd)
      case _ =>
        logger.info("Valid parameters for YTR download not defined")
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
          Observable.from(application.ytrClient.oppijaHetutBySyntymäaika(birthmonthStart, birthmonthEnd))
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

    val ssnDataObservable = Observable.from(application.ytrClient.oppijaHetutByModifiedSince(modifiedSince))

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
//          logger.info(s"Downloaded oppija with ${
//            val exams: Seq[YtrLaajaExam] = oppija.examinations.flatMap(_.examinationPeriods.flatMap(_.exams))
//            exams.size
//          } exams")

          // TODO: TOR-1639: Datan konversio ja kirjoitus Koskeen
          try {
            converter.convert(oppija)
          } catch {
            case e: Throwable => logger.info(s"YTR-datan konversio epäonnistui: ${e.getMessage}")
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

  def shutdown: Nothing = {
    Thread.sleep(60000) //Varmistetaan, että kaikki logit ehtivät varmasti siirtyä Cloudwatchiin ennen sulkemista.
    sys.exit()
  }
}

case class MonthParameters(
  birthmonthStart: String,
  birthmonthEnd: String
)
