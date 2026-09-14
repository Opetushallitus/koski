package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.eperusteetvalidation.EPerusteetOpiskeluoikeusChangeValidator
import fi.oph.koski.henkilo._
import fi.oph.koski.history.{OpiskeluoikeusHistory, YtrOpiskeluoikeusHistoryRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._
import org.json4s.{JArray, JValue}
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.{DBIOAction, NoStream}

class PostgresYtrOpiskeluoikeusRepositoryActions(
  val db: DB,
  val oidGenerator: OidGenerator,
  val henkilöRepository: OpintopolkuHenkilöRepository,
  val henkilöCache: KoskiHenkilöCache,
  val historyRepository: YtrOpiskeluoikeusHistoryRepository,
  val tableCompanion: OpiskeluoikeusTableCompanion[YtrOpiskeluoikeusRow],
  val organisaatioRepository: OrganisaatioRepository,
  val ePerusteetChangeValidator: EPerusteetOpiskeluoikeusChangeValidator,
  val config: Config
) extends PostgresOpiskeluoikeusRepositoryActions[YtrOpiskeluoikeusRow, YtrOpiskeluoikeusTable, YtrOpiskeluoikeusHistoryTable] {
  lazy val validator = new OpiskeluoikeusChangeValidator(organisaatioRepository, ePerusteetChangeValidator, config)

  protected def Opiskeluoikeudet = YtrOpiskeluOikeudet
  protected def OpiskeluOikeudetWithAccessCheck(implicit user: KoskiSpecificSession) = YtrOpiskeluOikeudetWithAccessCheck

  protected def saveHistoryError(opiskeluoikeus: JValue, historia: OpiskeluoikeusHistory, diff: JArray): Int = {
    errorRepository.saveYtr(opiskeluoikeus, historia, diff)
  }

  protected def syncAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    result: Either[HttpStatus, CreateOrUpdateResult]
  )(implicit user: KoskiSpecificSession): DBIOAction[Any, NoStream, Read with Write] = {
    DBIO.successful(())
  }

  protected override def createOrUpdateAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean,
    skipValidations: Boolean = false,
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    val identifier = OpiskeluoikeusIdentifier(oppijaOid.oppijaOid, opiskeluoikeus)

    findByIdentifierAction(identifier)
      .flatMap {
        case Right(Nil) =>
          createAction(oppijaOid, opiskeluoikeus)
        case Right(aiemmatOpiskeluoikeudet) if allowUpdate =>
          updateIfUnambiguousAiempiOpiskeluoikeusAction(oppijaOid, opiskeluoikeus, aiemmatOpiskeluoikeudet, allowDeleteCompleted, skipValidations)
        case Right(_) =>
          DBIO.successful(Left(KoskiErrorCategory.conflict.exists())) // Ei tehdä uutta, koska vastaava vanha YO-opiskeluoikeus on olemassa
        case Left(err) =>
          DBIO.successful(Left(err))
      }
  }

  private def updateIfUnambiguousAiempiOpiskeluoikeusAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    aiemmatOpiskeluoikeudet: List[YtrOpiskeluoikeusRow],
    allowDeleteCompleted: Boolean,
    skipValidations: Boolean = false,
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    aiemmatOpiskeluoikeudet match {
      case List(vanhaOpiskeluoikeus) =>
        updateIfSameOppijaAction(oppijaOid, vanhaOpiskeluoikeus, opiskeluoikeus, allowDeleteCompleted, skipValidations)
      case Nil =>
        // Ei pitäisi olla mahdollista: kutsuja käsittelee tyhjän tuloksen luontina
        DBIO.successful(Left(KoskiErrorCategory.internalError("Päivitettävää YTR-opiskeluoikeutta ei löytynyt")))
      case duplikaatit =>
        mitätöiDuplikaatitJaPäivitäAction(oppijaOid, opiskeluoikeus, duplikaatit, allowDeleteCompleted, skipValidations)
    }
  }

  // Oppijalla voi olla useampi YTR-opiskeluoikeus, jos kummallekin hänen oppijanumerolleen on ehditty tallentaa
  // sellainen ennen oppijanumeroiden yhdistämistä oppijanumerorekisterissä. Ilman duplikaattien mitätöintiä päivitys
  // jäisi pysyvästi jumiin, koska YTR-opiskeluoikeutta ei tunnisteta muulla kuin oppijan oidilla.
  // Kutsutaan vain, kun duplikaatteja on vähintään kaksi.
  private def mitätöiDuplikaatitJaPäivitäAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    duplikaatit: List[YtrOpiskeluoikeusRow],
    allowDeleteCompleted: Boolean,
    skipValidations: Boolean,
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    // Säilytetään ensisijaisesti se rivi, jonka oppija-oidiin lataus tällä hetkellä ratkeaa, ja toissijaisesti vanhin
    // rivi. Näin säilyvän opiskeluoikeuden oid pysyy samana niille palveluille, joille se on jo luovutettu.
    val säilytettävä = duplikaatit
      .find(_.oppijaOid == oppijaOid.oppijaOid)
      .getOrElse(duplikaatit.minBy(_.id))
    val mitätöitävät = duplikaatit.filterNot(_.id == säilytettävä.id)

    logger.warn(
      s"Oppijalla ${oppijaOid.oppijaOid} on ${duplikaatit.length} YTR-opiskeluoikeutta " +
        s"(${duplikaatit.map(rivi => s"${rivi.oid} (oppija ${rivi.oppijaOid})").mkString(", ")}). " +
        s"Todennäköinen syy: oppijanumerot on yhdistetty oppijanumerorekisterissä vasta opiskeluoikeuksien " +
        s"tallentamisen jälkeen. Säilytetään ${säilytettävä.oid} ja mitätöidään ${mitätöitävät.map(_.oid).mkString(", ")}."
    )

    DBIO.sequence(
      mitätöitävät.map(rivi => Opiskeluoikeudet.filter(_.id === rivi.id).map(_.mitätöity).update(true))
    ).flatMap { _ =>
      updateIfSameOppijaAction(oppijaOid, säilytettävä, opiskeluoikeus, allowDeleteCompleted, skipValidations)
    }
  }

  protected override def generateOid(oppija: OppijaHenkilöWithMasterInfo): String = {
    oidGenerator.generateYtrOid(oppija.henkilö.oid)
  }
}
