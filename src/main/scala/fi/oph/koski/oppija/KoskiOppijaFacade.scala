package fi.oph.koski.oppija

import com.sksamuel.elastic4s.ElasticClient
import fi.oph.koski.henkilo._
import fi.oph.koski.http.{Http, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.{Json, Json4sHttp4s}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{opiskeluoikeusId, opiskeluoikeusVersio, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, _}
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing
import com.sksamuel.elastic4s.ElasticDsl._
import fi.oph.koski.db.GlobalExecutionContext
import org.json4s._

import scala.util.{Failure, Success}

class KoskiOppijaFacade(henkilöRepository: HenkilöRepository, OpiskeluoikeusRepository: OpiskeluoikeusRepository) extends Logging with Timing with GlobalExecutionContext {
  def findOppija(oid: String)(implicit user: KoskiSession): Either[HttpStatus, Oppija] = toOppija(OpiskeluoikeusRepository.findByOppijaOid)(user)(oid)

  def findUserOppija(implicit user: KoskiSession): Either[HttpStatus, Oppija] = toOppija(OpiskeluoikeusRepository.findByUserOid)(user)(user.oid)

  def createOrUpdate(oppija: Oppija)(implicit user: KoskiSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val oppijaOid: Either[HttpStatus, PossiblyUnverifiedHenkilöOid] = oppija.henkilö match {
      case h:UusiHenkilö =>
        Hetu.validate(h.hetu, acceptSynthetic = false).right.flatMap { hetu =>
          henkilöRepository.findOrCreate(h).right.map(VerifiedHenkilöOid(_))
        }
      case h:HenkilöWithOid => Right(UnverifiedHenkilöOid(h.oid, henkilöRepository))
    }

    timed("createOrUpdate") {
      val opiskeluoikeudet: Seq[KoskeenTallennettavaOpiskeluoikeus] = oppija.tallennettavatOpiskeluoikeudet

      oppijaOid.right.flatMap { oppijaOid: PossiblyUnverifiedHenkilöOid =>
        if (oppijaOid.oppijaOid == user.oid) {
          Left(KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
        } else {
          val opiskeluoikeusCreationResults: Seq[Either[HttpStatus, OpiskeluoikeusVersio]] = opiskeluoikeudet.map { opiskeluoikeus =>
            createOrUpdateOpiskeluoikeus(oppijaOid, opiskeluoikeus, oppija.henkilö)
          }

          opiskeluoikeusCreationResults.find(_.isLeft) match {
            case Some(Left(error)) => Left(error)
            case _ => Right(HenkilönOpiskeluoikeusVersiot(OidHenkilö(oppijaOid.oppijaOid), opiskeluoikeusCreationResults.toList.map {
              case Right(r) => r
            }))
          }
        }
      }
    }
  }

  private def createOrUpdateOpiskeluoikeus(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, henkilö: Henkilö)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusVersio] = {
    def applicationLog(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: Opiskeluoikeus, result: CreateOrUpdateResult): Unit = {
      val verb = result match {
        case _: Updated => "Päivitetty"
        case _: Created => "Luotu"
        case _: NotChanged => "Päivitetty (ei muutoksia)"
      }
      logger(user).info(verb + " opiskeluoikeus " + result.id + " (versio " + result.versionumero + ")" + " oppijalle " + oppijaOid +
        " tutkintoon " + opiskeluoikeus.suoritukset.map(_.koulutusmoduuli.tunniste).mkString(",") +
        " oppilaitoksessa " + opiskeluoikeus.oppilaitos.oid)
    }

    def auditLog(oppijaOid: PossiblyUnverifiedHenkilöOid, result: CreateOrUpdateResult): Unit = {
      (result match {
        case _: Updated => Some(OPISKELUOIKEUS_MUUTOS)
        case _: Created => Some(OPISKELUOIKEUS_LISAYS)
        case _ => None
      }).foreach { operaatio =>
        AuditLog.log(AuditLogMessage(operaatio, user,
          Map(oppijaHenkiloOid -> oppijaOid.oppijaOid, opiskeluoikeusId -> result.id.toString, opiskeluoikeusVersio -> result.versionumero.toString))
        )
      }
    }

    if (oppijaOid.oppijaOid == user.oid) {
      Left(KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
    } else {
      val result = OpiskeluoikeusRepository.createOrUpdate(oppijaOid, opiskeluoikeus)
      result.right.map { (result: CreateOrUpdateResult) =>
        applicationLog(oppijaOid, opiskeluoikeus, result)
        auditLog(oppijaOid, result)

        val nimitiedotJaOid = henkilö match {
          case h: NimellinenHenkilö => NimitiedotJaOid(oppijaOid.oppijaOid, h.etunimet, h.kutsumanimi, h.sukunimi)
          case _ => throw new RuntimeException("Nimitiedot puuttuu")
        }

        val oo = opiskeluoikeus
        val suoritukset: List[SuorituksenPerustiedot] = oo.suoritukset
          .filterNot(_.isInstanceOf[PerusopetuksenVuosiluokanSuoritus])
          .map { suoritus =>
            val (osaamisala, tutkintonimike) = suoritus match {
              case s: AmmatillisenTutkinnonSuoritus => (s.osaamisala, s.tutkintonimike)
              case s: NäyttötutkintoonValmistavanKoulutuksenSuoritus => (s.osaamisala, s.tutkintonimike)
              case _ => (None, None)
            }
            SuorituksenPerustiedot(suoritus.tyyppi, KoulutusmoduulinPerustiedot(suoritus.koulutusmoduuli.tunniste), osaamisala, tutkintonimike, suoritus.toimipiste)
          }

        val perustiedot = OpiskeluoikeudenPerustiedot(nimitiedotJaOid, oo.oppilaitos, oo.alkamispäivä, oo.tyyppi, suoritukset, oo.tila.opiskeluoikeusjaksot.last.tila, oo.luokka)

        import Http._

        implicit val formats = Json.jsonFormats
        val doc = Json.toJValue(Map("doc_as_upsert" -> true, "doc" -> perustiedot))

        val response = Http.runTask(Http("http://localhost:9200")
          .post(uri"/koski/perustiedot/${result.id}/_update", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])) // TODO: hardcoded url

        val success: Int = (response \ "_shards" \ "successful").extract[Int]

        if (success < 1) {
          logger.error("Elasticsearch indexing failed (success count < 1)")
        }

        OpiskeluoikeusVersio(result.id, result.versionumero)
      }
    }
  }

  private def toOppija(findFunc: String => Seq[Opiskeluoikeus])(implicit user: KoskiSession): String => Either[HttpStatus, Oppija] = oid => {
    def notFound = Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa " + oid + " ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
    val result = henkilöRepository.findByOid(oid) match {
      case Some(oppija) =>
        findFunc(oppija.oid) match {
          case Nil => notFound
          case opiskeluoikeudet: Seq[Opiskeluoikeus] => Right(Oppija(oppija, opiskeluoikeudet.sortBy(_.id)))
        }
      case None => notFound
    }

    result.right.foreach((oppija: Oppija) => writeViewingEventToAuditLog(user, oid))
    result
  }

  private def writeViewingEventToAuditLog(user: KoskiSession, oid: Henkilö.Oid): Unit = {
    if (user != KoskiSession.systemUser) { // To prevent health checks from pollutings the audit log
      AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, user, Map(oppijaHenkiloOid -> oid)))
    }
  }
}

case class HenkilönOpiskeluoikeusVersiot(henkilö: OidHenkilö, opiskeluoikeudet: List[OpiskeluoikeusVersio])
case class OpiskeluoikeusVersio(id: Opiskeluoikeus.Id, versionumero: Int)