package fi.oph.koski.ytr.download

import fi.oph.koski.cloudwatch.CloudWatchMetricsService
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.{DB, KoskiTables, QueryMethods}
import fi.oph.koski.log.Logging
import org.json4s.JValue
import rx.lang.scala.schedulers.NewThreadScheduler
import rx.lang.scala.{Observable, Scheduler, Subscription}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import org.json4s._
import org.json4s.jackson.JsonMethods

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}
import scala.language.postfixOps

class YtrDownloadService(
  val db: DB,
  application: KoskiApplication
) extends QueryMethods with Logging {
  implicit val formats = DefaultFormats

  private val tietokantaStatusRivinNimi = "ytr_download"

  // TODO: TOR-1639 metriikat cloudwatchiin
  // TODO: TOR-1639 paremmat logitukset
  private val cloudWatchMetrics = CloudWatchMetricsService.apply(application.config)

  def download(
    birthmonthStart: Option[String] = None,
    birthmonthEnd: Option[String] = None,
    modifiedSince: Option[LocalDate] = None,
    force: Boolean = false,
    scheduler: Scheduler = defaultScheduler,
    onEnd: () => Unit = () => (),
  ): Boolean = {
    if (isLoading && !force) {
      logger.info("YTR data already downloading, do nothing")
      onEnd()
      false
    } else if (birthmonthStart.isDefined && birthmonthEnd.isDefined) {
      startDownloading(birthmonthStart, birthmonthEnd, scheduler, onEnd)
      logger.info(s"Started downloading YTR data (force: $force, birthmonthStart: ${
        birthmonthStart.getOrElse("-")
      }, birthmonthEnd: ${
        birthmonthEnd.getOrElse("-")
      }, modifiedSince: ${
        modifiedSince.map(_.toString).getOrElse("-")
      } )")
      true
    } else if (modifiedSince.isDefined) {
      // TODO: TOR-1639: toteuta modifiedSince:llä lataus
      logger.info("Downloading with modifiedSince is not yet supported")
      onEnd()
      false
    } else {
      logger.info("Valid parameters for YTR download not defined")
      onEnd()
      false
    }
  }

  def downloadAndExit(): Unit = {
    val config = Environment.ytrDownloadConfig

    download(
      birthmonthStart = config.birthmonthStart,
      birthmonthEnd = config.birthmonthEnd,
      modifiedSince = config.modifiedSince,
      force = config.force,
      onEnd = () => {
        logger.info(s"Ended downloading YTR data, shutting down...")
      }
    )
  }

  private def isLoading: Boolean = getDownloadStatus == "loading"
  def isComplete: Boolean = getDownloadStatus == "complete"
  private def setLoading = setStatus("loading")
  private def setComplete = setStatus("complete")
  private def setError = setStatus("error")

  private def getDownloadStatus: String = {
    (getDownloadStatusJson \ "current" \ "status").extract[String]
  }

  def getDownloadStatusJson: JValue = {
    runDbSync(KoskiTables.YtrDownloadStatus.filter(_.nimi === tietokantaStatusRivinNimi).result).headOption.map(_.data)
      .getOrElse(constructStatusJson("idle"))
  }

  private def setStatus(currentStatus: String) = {
    runDbSync(KoskiTables.YtrDownloadStatus.insertOrUpdate(
      YtrDownloadStatusRow(
        tietokantaStatusRivinNimi,
        Timestamp.valueOf(LocalDateTime.now),
        constructStatusJson(currentStatus, Some(LocalDateTime.now))
      )
    ))
  }

  // TODO: TOR-1639 tee tämä serialisointi (ja deserialisointi) paremmilla työkaluilla suoraan Scala case luokasta tms.
  private def constructStatusJson(currentStatus: String, timestamp: Option[LocalDateTime] = None): JValue = {
    val timestampPart = timestamp.map(Timestamp.valueOf).map(t =>
      s"""
         |, "timestamp": "${t.toString}"
         |""".stripMargin).getOrElse("")

    JsonMethods.parse(s"""
      | {
      |   "current": {
      |     "status": "${currentStatus}"
      |     ${timestampPart}
      |   }
      | }""".stripMargin
    )
  }

  private def startDownloading(
    birthmonthStart: Option[String],
    birthmonthEnd: Option[String],
    scheduler: Scheduler ,
    onEnd: () => Unit
  ): Subscription = {
    logger.info(s"Start downloading YTR data (birthmonthStart: ${
      birthmonthStart.getOrElse("-")
    }, birthmonthEnd: ${
      birthmonthEnd.getOrElse("-")
    } )")

    setLoading

    download(birthmonthStart, birthmonthEnd)
      .subscribeOn(scheduler)
      .subscribe(
        onNext = oppija => {
          logger.info(s"Downloaded oppija with ${
            val exams: Seq[YtrLaajaExam] = oppija.examinations.flatMap(_.examinationPeriods.flatMap(_.exams))
            exams.size
          } exams")
          Thread.sleep(100)
        },
        onError = e => {
          logger.error(e)("YTR download failed:" + e.toString)
          setError
          onEnd()
        },
        onCompleted = () => {
          try {
            setComplete
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


  private def download(
    birthmonthStart: Option[String],
    birthmonthEnd: Option[String]
  ): Observable[YtrLaajaOppija] = {
    // TODO: TOR-1639 YTR:n API tukee korkeintaan 1 vuosi kerralla hakua, splittaa syntymäpäiväjaksot tarvittaessa
    // Myös splittaus sen jälkeen batchein kannatta tehdä sitten siistimmin Observable-työkaluilla.

    val groupedSsns = Observable.from(application.ytrClient.oppijaHetutBySyntymäaika(birthmonthStart.get, birthmonthEnd.get))
      .flatMap(a => Observable.from(a.ssns))
      .doOnEach(o =>
        logger.info(s"Downloaded ${o.length} ssns from YTR")
      )
      .map(_.grouped(50).toList) // TODO: hyvä ja configista tuleva batch size? 1500 on maksimi
      .flatMap(a => Observable.from(a.map(Option.apply).map(YtrSsnData.apply)))

    val oppijat: Observable[YtrLaajaOppija] = groupedSsns
      .doOnEach(o =>
        logger.info(s"Downloading a batch of ${o.ssns.map(_.length).getOrElse("-")} students from YTR")
      )
      .flatMap(a => Observable.from(application.ytrClient.oppijatByHetut(a)))

    oppijat
  }

  private val doNothing = (_: Any) => ()

  protected lazy val defaultScheduler: Scheduler = NewThreadScheduler()

  protected lazy val isMockEnvironment: Boolean = Environment.isMockEnvironment(application.config)

  def shutdown = {
    Thread.sleep(60000) //Varmistetaan, että kaikki logit ehtivät varmasti siirtyä Cloudwatchiin ennen sulkemista.
    sys.exit()
  }
}
