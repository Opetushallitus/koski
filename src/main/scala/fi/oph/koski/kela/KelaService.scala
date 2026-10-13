package fi.oph.koski.kela

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.history.{OpiskeluoikeusHistoryPatch, RawOpiskeluoikeusData}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.schema.{KorkeakoulunOpiskeluoikeus, KoskiSchema, OpiskeluoikeudenTyyppi, YlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.util.Futures
import org.json4s.JsonAST.JValue
import rx.lang.scala.Observable

import java.time.LocalDateTime
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class KelaService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val kelaOpiskeluoikeusRepository = new KelaOpiskeluoikeusRepository(
    application.replicaDatabase.db,
    application.validatingAndResolvingExtractor
  )

  // Palautettavat tiedot rajataan Kela-skeemalla ja estolistalla, ei enää koulutusmuotokohtaisilla rooleilla: haut tehdään
  // OPH-katselijan järjestelmäsessiolla, vastaavasti kuin SDG- ja KIOS-rajapinnoissa. Kutsujan omaa
  // sessiota käytetään arkaluontoisten kenttien suodatukseen ja auditlokitukseen.
  private val systemSession: KoskiSpecificSession = KoskiSpecificSession.systemKatselijaUser

  def findKelaOppijaByHetu(hetu: String)
    (kelaSession: KoskiSpecificSession): Either[HttpStatus, KelaOppija] = {

    val (opiskeluoikeudet, ytrResult, virtaResult) = haeOpiskeluoikeudet(List(hetu), haeUlkoisetJärjestelmät = true)

    val oppija = opiskeluoikeudet.headOption.map {
      case (hlö, oos) => virtaResult.getOrElse(hlö, Right(Nil))
        .flatMap(virta => teePalautettavaKelaOppija(hlö, oos, ytrResult.getOrElse(hlö, Nil), virta))
    }.toRight(
      KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(
        "Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."
      )
    ).flatMap(identity)
    oppija.foreach(auditLogOpiskeluoikeusKatsominen(_)(kelaSession))
    oppija
  }

  def streamOppijatByHetu(hetut: Seq[String])(kelaSession: KoskiSpecificSession): Observable[JValue] = {
    val (opiskeluoikeudet, _, _) = haeOpiskeluoikeudet(hetut, haeUlkoisetJärjestelmät = false)

    Observable
      .from(
        opiskeluoikeudet.map {
          case (oppijaMasterOid, opiskeluoikeusRows) => teePalautettavaKelaOppija(
            oppijaMasterOid,
            opiskeluoikeusRows,
            Seq.empty,
            Seq.empty
          )
        }
      )
      .collect {
        case Right(kelaOppija) => kelaOppija
        case Left(status) if (status.statusCode != 404) =>
          throw new RuntimeException("Error while streaming KelaOppija")
      }
      .doOnEach(auditLogOpiskeluoikeusKatsominen(_)(kelaSession))
      .map(JsonSerializer.serializeWithUser(kelaSession))
  }

  private def haeOpiskeluoikeudet(hetut: Seq[String], haeUlkoisetJärjestelmät: Boolean): (
    Map[LaajatOppijaHenkilöTiedot, Seq[RawOpiskeluoikeusData]],
    Map[LaajatOppijaHenkilöTiedot, Seq[KelaYlioppilastutkinnonOpiskeluoikeus]],
    Map[LaajatOppijaHenkilöTiedot, Either[HttpStatus, Seq[KelaKorkeakoulunOpiskeluoikeus]]]
  ) = {
    val masterHenkilötFut: Future[Map[String, LaajatOppijaHenkilöTiedot]] = Future(
      application.opintopolkuHenkilöFacade.findMasterOppijat(
        application.opintopolkuHenkilöFacade.findOppijatByHetusNoSlaveOids(hetut).map(_.oid).toList
      )
    )

    val ytrResultFut: Future[Map[LaajatOppijaHenkilöTiedot, Seq[KelaYlioppilastutkinnonOpiskeluoikeus]]] = {
      if (haeUlkoisetJärjestelmät && kelallePalautettavaOpiskeluoikeusTyyppi(OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)) {
        masterHenkilötFut
          .map { masterHenkilöt =>
            masterHenkilöt.values
              .map(hlö => hlö -> application.ytr.findByOppija(hlö)(systemSession).map {
                case yo: YlioppilastutkinnonOpiskeluoikeus =>
                  KelaYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema(yo)
              }
              ).toMap
          }
      } else Future.successful(Map.empty)
    }

    val virtaResultFut: Future[Map[LaajatOppijaHenkilöTiedot, Either[HttpStatus, Seq[KelaKorkeakoulunOpiskeluoikeus]]]] = {
      if (haeUlkoisetJärjestelmät) {
        masterHenkilötFut
          .map { masterHenkilöt =>
            masterHenkilöt.values.map(hlö => hlö -> haeVirranOpiskeluoikeudet(hlö)).toMap
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

    val (opiskeluoikeudet, ytrResult, virtaResult) = Futures.await(
      future = for {
        opiskeluoikeudet <- opiskeluoikeudetFut
        ytrResult <- ytrResultFut
        virtaResult <- virtaResultFut
      } yield (opiskeluoikeudet, ytrResult, virtaResult),
      atMost = if (Environment.isUnitTestEnvironment(application.config)) { 10.seconds } else { 20.minutes }
    )
    (opiskeluoikeudet, ytrResult, virtaResult)
  }

  private def haeVirranOpiskeluoikeudet(hlö: LaajatOppijaHenkilöTiedot): Either[HttpStatus, Seq[KelaKorkeakoulunOpiskeluoikeus]] =
    if (!kelallePalautettavaOpiskeluoikeusTyyppi(OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)) {
      Right(Nil)
    } else Try(application.virta.findByOppija(hlö)(systemSession)) match {
      case Success(opiskeluoikeudet) => Right(opiskeluoikeudet
        .collect { case kk: KorkeakoulunOpiskeluoikeus => kk }
        .filter(kk => kelallePalautettavaOpiskeluoikeusTyyppi(kk.tyyppi.koodiarvo))
        .map(KelaKorkeakoulunOpiskeluoikeus.fromKoskiSchema))
      case Failure(e) =>
        logger.error(e)(s"Virta-haku epäonnistui oppijalle ${hlö.oid}")
        Left(KoskiErrorCategory.unavailable.virta())
    }

  def opiskeluoikeudenHistoria(opiskeluoikeusOid: String)
    (kelaSession: KoskiSpecificSession): Option[List[KelaOpiskeluoikeusHistoryPatch]] = {
    opiskeluoikeudenHistoriaLaajatTiedot(opiskeluoikeusOid)(kelaSession).map(
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
    (kelaSession: KoskiSpecificSession): Option[List[OpiskeluoikeusHistoryPatch]] = {
    val history: Option[List[OpiskeluoikeusHistoryPatch]] = application
      .opiskeluoikeusRepository
      .findByOid(opiskeluoikeusOid)(systemSession)
      .toOption
      .filter(kelallePalautettava)
      .flatMap(_ => application.historyRepository.findByOpiskeluoikeusOid(opiskeluoikeusOid)(systemSession))
    history.foreach { _ => auditLogHistoryView(opiskeluoikeusOid)(kelaSession) }
    history
  }

  private def kelallePalautettava(rivi: KoskiOpiskeluoikeusRow): Boolean =
    deserializeAndCleanKelaOpiskeluoikeus(RawOpiskeluoikeusData(rivi.data, rivi.oid, rivi.versionumero, rivi.aikaleima)).isRight

  def findKelaOppijaVersion(opiskeluoikeusOid: String, version: Int)
    (kelaSession: KoskiSpecificSession): Either[HttpStatus, KelaOppija] = {
    lazy val notFound = KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + opiskeluoikeusOid + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun")

    val result = for {
      masterOid <- application.opiskeluoikeusRepository
        .getMasterOppijaOidForOpiskeluoikeus(opiskeluoikeusOid)(systemSession)
      hlö <- application.henkilöRepository.findByOid(masterOid).toRight(notFound)
      opiskeluoikeusData <- application.historyRepository.findVersionRaw(opiskeluoikeusOid, version)(systemSession)
      oppija <- teePalautettavaKelaOppija(
        hlö,
        Seq(opiskeluoikeusData),
        Seq.empty,
        Seq.empty
      )
    } yield oppija

    result.flatMap(oppija => {
      // Koska Kelan normaalin API:n kautta voi hakea vain hetullisia, ei palauteta hetuttomia myöskään historia-API:sta
      if (oppija.henkilö.hetu.isDefined && oppija.opiskeluoikeudet.nonEmpty) {
        auditLogOpiskeluoikeusKatsominen(oppija)(kelaSession)
        Right(oppija)
      } else {
        Left(notFound)
      }
    })
  }

  private def teePalautettavaKelaOppija(
    oppijaHenkilö: LaajatOppijaHenkilöTiedot,
    rawOpiskeluoikeudet: Seq[RawOpiskeluoikeusData],
    ytrOpiskeluoikeudet: Seq[KelaYlioppilastutkinnonOpiskeluoikeus],
    virtaOpiskeluoikeudet: Seq[KelaKorkeakoulunOpiskeluoikeus]
  ): Either[HttpStatus, KelaOppija] = {
    val opiskeluoikeudet =
      rawOpiskeluoikeudet
        .map(deserializeAndCleanKelaOpiskeluoikeus)
        .collect { case Right(oo) => oo }

    if (opiskeluoikeudet.nonEmpty || ytrOpiskeluoikeudet.nonEmpty || virtaOpiskeluoikeudet.nonEmpty) {
      Right(
        KelaOppija(
          henkilö = Henkilo.fromOppijaHenkilö(oppijaHenkilö),
          opiskeluoikeudet = opiskeluoikeudet.toList ++ ytrOpiskeluoikeudet ++ virtaOpiskeluoikeudet
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
    val configKey = "kela.eiPalautettavatOpiskeluoikeustyypit"
    if (application.config.hasPath(configKey)) {
      val estetyt = application.config.getList(configKey)
      !estetyt.unwrapped().asScala.toList.contains(opiskeluoikeusTyyppi)
    } else {
      true
    }
  }

  private def auditLogOpiskeluoikeusKatsominen(oppija: KelaOppija)(kelaSession: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.OPISKELUOIKEUS_KATSOMINEN,
          kelaSession,
          Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppija.henkilö.oid)
        )
      )

  private def auditLogHistoryView(opiskeluoikeusOid: String)(kelaSession: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.MUUTOSHISTORIA_KATSOMINEN,
          kelaSession,
          Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> opiskeluoikeusOid)
        )
      )
}

case class KelaOpiskeluoikeusHistoryPatch(opiskeluoikeusOid: String, versionumero: Int, aikaleima: LocalDateTime)
