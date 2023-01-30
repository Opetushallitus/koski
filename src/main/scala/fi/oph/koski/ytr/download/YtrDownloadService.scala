package fi.oph.koski.ytr.download

import fi.oph.koski.cloudwatch.CloudWatchMetricsService
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.log.Logging
import rx.lang.scala.schedulers.NewThreadScheduler
import rx.lang.scala.{Observable, Scheduler, Subscription}

import java.time.LocalDate
import scala.language.postfixOps

class YtrDownloadService(application: KoskiApplication) extends Logging {
  // TODO: metriikat cloudwatchiin
  // TODO: paremmat logitukset
  private val cloudWatchMetrics = CloudWatchMetricsService.apply(application.config)

  def download(
    birthdateStart: Option[LocalDate] = None,
    birthdateEnd: Option[LocalDate] = None,
    modifiedSince: Option[LocalDate] = None,
    force: Boolean = false,
    scheduler: Scheduler = defaultScheduler,
    onEnd: () => Unit = () => (),
  ): Boolean = {
    if (isLoading && !force) {
      logger.info("YTR data already downloading, do nothing")
      onEnd()
      false
    } else if (birthdateStart.isDefined && birthdateEnd.isDefined) {
      startDownloading(birthdateStart, birthdateEnd, scheduler, onEnd)
      logger.info(s"Started downloading YTR data (force: $force, birthdateStart: ${
        birthdateStart.map(_.toString).getOrElse("-")
      }, birthdateEnd: ${
        birthdateEnd.map(_.toString).getOrElse("-")
      }, modifiedSince: ${
        modifiedSince.map(_.toString).getOrElse("-")
      } )")
      true
    } else if (modifiedSince.isDefined) {
      // TODO
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
      birthdateStart = config.birthdateStart,
      birthdateEnd = config.birthdateEnd,
      modifiedSince = config.modifiedSince,
      force = config.force,
      onEnd = () => {
        logger.info(s"Ended downloading YTR data, shutting down...")
      }
    )
  }

  def isLoading: Boolean = {
    // TODO: Kirjoita tietokantaan statustieto ja lue sieltä
    false
  }

  def isAvailable: Boolean = true // TODO
  def isLoadComplete: Boolean = !isLoading && isAvailable

  private def startDownloading(
    birthdateStart: Option[LocalDate],
    birthdateEnd: Option[LocalDate],
    scheduler: Scheduler ,
    onEnd: () => Unit
  ): Subscription = {
    logger.info(s"Start downloading YTR data (birthdateStart: ${
      birthdateStart.map(_.toString).getOrElse("-")
    }, birthdateEnd: ${
      birthdateEnd.map(_.toString).getOrElse("-")
    } )")

    download(birthdateStart, birthdateEnd)
      .subscribeOn(scheduler)
      .subscribe(
        onNext = oppija => {
          // TODO: Käsittele oppija, älä turhaan yritä logittaa ssn:ää, joka kuitenkin maskataan
          logger.info(s"Downloaded oppija ${oppija.ssn} with ${
            val exams: Seq[YtrLaajaExam] = oppija.examinations.flatMap(_.examinationPeriods.flatMap(_.exams))
            exams.size
          } exams")
        },
        onError = e => {
          logger.error(e)("YTR download failed:" + e.toString)
          onEnd()
        },
        onCompleted = () => {
          try {
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
    birthdateStart: Option[LocalDate],
    birthdateEnd: Option[LocalDate]
  ): Observable[YtrLaajaOppija] = {
    // TODO: Korkeintaan 1 vuosi kerrallaan voi hakea, splittaa syntymäpäiväjaksot tarvittaessa

    val groupedSsns = Observable.from(application.ytrClient.oppijaHetutBySyntymäaika(birthdateStart.get, birthdateEnd.get))
      .flatMap(a => Observable.from(a.ssns))
      .map(_.grouped(50).toList) // TODO: hyvä ja configista tuleva batch size? 1500 on maksimi
      .flatMap(a => Observable.from(a.map(Option.apply).map(YtrSsns.apply)))

    val oppijat: Observable[YtrLaajaOppija] = groupedSsns.flatMap(a => Observable.from(application.ytrClient.oppijatByHetut(a)))

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
