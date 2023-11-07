package fi.oph.koski.kela

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.history.{OpiskeluoikeusHistoryPatch, RawOpiskeluoikeusData}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.schema.{KoskiSchema, YlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.util.Futures
import org.json4s.JsonAST.JValue
import rx.lang.scala.Observable

import java.time.LocalDateTime
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.collection.JavaConverters._

class KelaService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val kelaOpiskeluoikeusRepository = new KelaOpiskeluoikeusRepository(
    application.replicaDatabase.db,
    application.validatingAndResolvingExtractor
  )

  def findKelaOppijaByHetu(hetu: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, KelaOppija] = {

    val (opiskeluoikeudet, ytrResult) = haeOpiskeluoikeudet(List(hetu), true)

    val oppija = opiskeluoikeudet.headOption.map {
      case (hlö, oos) => teePalautettavaKelaOppija(hlö, oos, ytrResult(hlö))
    }.toRight(
      KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(
        "Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."
      )
    ).flatMap(identity)
    oppija.foreach(auditLogOpiskeluoikeusKatsominen(_)(koskiSession))
    oppija
  }

  def streamOppijatByHetu(hetut: Seq[String])(implicit koskiSession: KoskiSpecificSession): Observable[JValue] = {
    val (opiskeluoikeudet, _) = haeOpiskeluoikeudet(hetut, false)

    Observable
      .from(
        opiskeluoikeudet.map {
          case (oppijaMasterOid, opiskeluoikeusRows) => teePalautettavaKelaOppija(
            oppijaMasterOid,
            opiskeluoikeusRows,
            Seq.empty
          )
        }
      )
      .collect {
        case Right(kelaOppija) => kelaOppija
        case Left(status) if (status.statusCode != 404) =>
          throw new RuntimeException("Error while streaming KelaOppija")
      }
      .doOnEach(auditLogOpiskeluoikeusKatsominen(_)(koskiSession))
      .map(JsonSerializer.serializeWithUser(koskiSession))
  }

  private def haeOpiskeluoikeudet(hetut: Seq[String], haeYtr: Boolean)(
    implicit user: KoskiSpecificSession
  ): (Map[LaajatOppijaHenkilöTiedot, Seq[RawOpiskeluoikeusData]], Map[LaajatOppijaHenkilöTiedot, Seq[KelaYlioppilastutkinnonOpiskeluoikeus]]) = {
    val masterHenkilötFut: Future[Map[String, LaajatOppijaHenkilöTiedot]] = Future(
      application.opintopolkuHenkilöFacade.findMasterOppijat(
        application.opintopolkuHenkilöFacade.findOppijatByHetusNoSlaveOids(hetut).map(_.oid).toList
      )
    )

    val ytrResultFut: Future[Map[LaajatOppijaHenkilöTiedot, Seq[KelaYlioppilastutkinnonOpiskeluoikeus]]] = {
      if (haeYtr) {
        masterHenkilötFut
          .map { masterHenkilöt =>
            masterHenkilöt.values
              .map(hlö => hlö -> application.ytr.findByOppija(hlö).map {
                case yo: YlioppilastutkinnonOpiskeluoikeus =>
                  KelaYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema(yo)
              }
              ).toMap
          }
      } else Future.successful(Map.empty)
    }

    val opiskeluoikeudetFut: Future[Map[LaajatOppijaHenkilöTiedot, Seq[RawOpiskeluoikeusData]]] =
      masterHenkilötFut
        .map { masterHenkilöt =>
          masterHenkilöt.values.map { henkilö =>
            henkilö -> kelaOpiskeluoikeusRepository.getOppijanKaikkiOpiskeluoikeudet(
              oppijaMasterOids = List(henkilö.oid)
            )
          }.toMap
        }

    val (opiskeluoikeudet, ytrResult) = Futures.await(
      future = for {
        opiskeluoikeudet <- opiskeluoikeudetFut
        ytrResult <- ytrResultFut
      } yield (opiskeluoikeudet, ytrResult),
      atMost = if (Environment.isUnitTestEnvironment(application.config)) { 10.seconds } else { 20.minutes }
    )
    (opiskeluoikeudet, ytrResult)
  }

  def opiskeluoikeudenHistoria(opiskeluoikeusOid: String)
    (implicit koskiSession: KoskiSpecificSession): Option[List[KelaOpiskeluoikeusHistoryPatch]] = {
    opiskeluoikeudenHistoriaLaajatTiedot(opiskeluoikeusOid).map(
      _.map(täysiHistoriaPatch =>
        KelaOpiskeluoikeusHistoryPatch(
          täysiHistoriaPatch.opiskeluoikeusOid,
          täysiHistoriaPatch.versionumero,
          täysiHistoriaPatch.aikaleima.toLocalDateTime
        )
      )
    )
  }

  def opiskeluoikeudenHistoriaLaajatTiedot(opiskeluoikeusOid: String)
    (implicit koskiSession: KoskiSpecificSession): Option[List[OpiskeluoikeusHistoryPatch]] = {
    val history: Option[List[OpiskeluoikeusHistoryPatch]] = application
      .historyRepository
      .findByOpiskeluoikeusOid(opiskeluoikeusOid)(koskiSession)
    history.foreach { _ => auditLogHistoryView(opiskeluoikeusOid)(koskiSession) }
    history
  }

  def findKelaOppijaVersion(opiskeluoikeusOid: String, version: Int)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, KelaOppija] = {
    lazy val notFound = KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + opiskeluoikeusOid + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun")

    val result = for {
      masterOid <- application.opiskeluoikeusRepository
        .getMasterOppijaOidForOpiskeluoikeus(opiskeluoikeusOid)
      hlö <- application.henkilöRepository.findByOid(masterOid).toRight(notFound)
      opiskeluoikeusData <- application.historyRepository.findVersionRaw(opiskeluoikeusOid, version)
      oppija <- teePalautettavaKelaOppija(
        hlö,
        Seq(opiskeluoikeusData),
        Seq.empty
      )
    } yield oppija

    result.flatMap(oppija => {
      // Koska Kelan normaalin API:n kautta voi hakea vain hetullisia, ei palauteta hetuttomia myöskään historia-API:sta
      if (oppija.henkilö.hetu.isDefined && oppija.opiskeluoikeudet.nonEmpty) {
        auditLogOpiskeluoikeusKatsominen(oppija)(koskiSession)
        Right(oppija)
      } else {
        Left(notFound)
      }
    })
  }

  private def teePalautettavaKelaOppija(
    oppijaHenkilö: LaajatOppijaHenkilöTiedot,
    rawOpiskeluoikeudet: Seq[RawOpiskeluoikeusData],
    ytrOpiskeluoikeudet: Seq[KelaYlioppilastutkinnonOpiskeluoikeus]
  ): Either[HttpStatus, KelaOppija] = {
    val opiskeluoikeudet =
      rawOpiskeluoikeudet
        .map(deserializeAndCleanKelaOpiskeluoikeus)
        .collect { case Right(oo) => oo }

    if (opiskeluoikeudet.nonEmpty || ytrOpiskeluoikeudet.nonEmpty) {
      Right(
        KelaOppija(
          henkilö = Henkilo.fromOppijaHenkilö(oppijaHenkilö),
          opiskeluoikeudet = opiskeluoikeudet.toList ++ ytrOpiskeluoikeudet
        )
      )
    }
    else {
      Left(KoskiErrorCategory
        .notFound
        .oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.")
      )
    }
  }

  private def deserializeAndCleanKelaOpiskeluoikeus(rawOpiskeluoikeusData: RawOpiskeluoikeusData): Either[HttpStatus, KelaOpiskeluoikeus] = {
    val json = rawOpiskeluoikeusData.readAsJValue

    application.validatingAndResolvingExtractor
      .extract[KelaOpiskeluoikeus](KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItemsWithoutValidation)(json)
      .map(oo => oo.withCleanedData)
      .flatMap {
        case oo if kelallePalautettavaOpiskeluoikeusTyyppi(oo.tyyppi.koodiarvo) && oo.suoritukset.length > 0 => Right(oo)
        case _ =>
          Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + rawOpiskeluoikeusData.oid + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
      }
  }

  private def kelallePalautettavaOpiskeluoikeusTyyppi(opiskeluoikeusTyyppi: String): Boolean = {
    val configKey = "kela.palautettavatOpiskeluoikeustyypit"
    if (application.config.hasPath(configKey)) {
      val allowList = application.config.getList(configKey)
      allowList.unwrapped().asScala.toList.contains(opiskeluoikeusTyyppi)
    } else {
      true
    }
  }

  private def auditLogOpiskeluoikeusKatsominen(oppija: KelaOppija)(koskiSession: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.OPISKELUOIKEUS_KATSOMINEN,
          koskiSession,
          Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppija.henkilö.oid)
        )
      )

  private def auditLogHistoryView(opiskeluoikeusOid: String)(koskiSession: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.MUUTOSHISTORIA_KATSOMINEN,
          koskiSession,
          Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> opiskeluoikeusOid)
        )
      )
}

case class KelaOpiskeluoikeusHistoryPatch(opiskeluoikeusOid: String, versionumero: Int, aikaleima: LocalDateTime)
