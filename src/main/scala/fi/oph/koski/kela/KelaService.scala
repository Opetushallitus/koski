package fi.oph.koski.kela

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.schema
import fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus
import fi.oph.koski.util.Futures
import org.json4s.JsonAST.JValue
import rx.lang.scala.Observable

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class KelaService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val kelaOpiskeluoikeusRepository = new KelaOpiskeluoikeusRepository(
    application.replicaDatabase.db,
    application.validatingAndResolvingExtractor
  )

  def findKelaOppijaByHetu(hetu: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, KelaOppija] = {

    val (opiskeluoikeudet, ytrResult) = haeOpiskeluoikeudet(List(hetu), true)

    val oppija = opiskeluoikeudet.headOption.map {
      case (hlö, oos) => teePalautettavatKelaOppijat(hlö, oos, ytrResult(hlö))
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
          case (oppijaMasterOid, opiskeluoikeusRows) => teePalautettavatKelaOppijat(
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
  ): (Map[LaajatOppijaHenkilöTiedot, Seq[KelaOppijanOpiskeluoikeusRow]], Map[LaajatOppijaHenkilöTiedot, Seq[KelaYlioppilastutkinnonOpiskeluoikeus]]) = {
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

    val opiskeluoikeudetFut: Future[Map[LaajatOppijaHenkilöTiedot, Seq[KelaOppijanOpiskeluoikeusRow]]] =
      masterHenkilötFut
        .map { masterHenkilöt =>
          masterHenkilöt.values.map { henkilö =>
            henkilö -> kelaOpiskeluoikeusRepository.getOppijanKaikkiOpiskeluoikeudet(
              palautettavatOpiskeluoikeudenTyypit = KelaSchema.schemassaTuetutOpiskeluoikeustyypit,
              oppijaMasterOids = List(henkilö.oid)
            )
          }.toMap
        }

    val (opiskeluoikeudet, ytrResult) = Futures.await(
      future = for {
        opiskeluoikeudet <- opiskeluoikeudetFut
        ytrResult <- ytrResultFut
      } yield (opiskeluoikeudet, ytrResult),
      atMost = 20.minutes
    )
    (opiskeluoikeudet, ytrResult)
  }

  def opiskeluoikeudenHistoria(opiskeluoikeusOid: String)
    (implicit koskiSession: KoskiSpecificSession): Option[List[OpiskeluoikeusHistoryPatch]] = {
    val history: Option[List[OpiskeluoikeusHistoryPatch]] = application
      .historyRepository
      .findByOpiskeluoikeusOid(opiskeluoikeusOid)(koskiSession)
    history.foreach { _ => auditLogHistoryView(opiskeluoikeusOid)(koskiSession) }
    history
  }

  def findKelaOppijaVersion(oppijaOid: String, opiskeluoikeusOid: String, version: Int)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, KelaOppija] = {
    application.oppijaFacade.findVersion(oppijaOid, opiskeluoikeusOid, version)
      .map(t => t.oppija)
      .flatMap(KelaOppijaConverter.convertOppijaToKelaOppija)
  }

  private def teePalautettavatKelaOppijat(
    oppijaHenkilö: LaajatOppijaHenkilöTiedot,
    opiskeluoikeusRows: Seq[KelaOppijanOpiskeluoikeusRow],
    ytrOpiskeluoikeudet: Seq[KelaYlioppilastutkinnonOpiskeluoikeus]
  ): Either[HttpStatus, KelaOppija] = {
    val opiskeluoikeudet = opiskeluoikeusRows.map(_.opiskeluoikeus)

    if (opiskeluoikeudet.nonEmpty) {
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
