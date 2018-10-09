package fi.oph.koski.virta

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.henkilo.FindByOid
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{AccessChecker, AccessType, KoskiSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.AuxiliaryOpiskeluoikeusRepository
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.schema.{Opiskeluoikeus, _}
import fi.oph.koski.validation.KoskiValidator

import scala.concurrent.duration._
import scala.util.control.NonFatal

abstract class HetuBasedOpiskeluoikeusRepository[OO <: Opiskeluoikeus](henkilöRepository: FindByOid, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, accessChecker: AccessChecker, validator: Option[KoskiValidator] = None)(implicit cacheInvalidator: CacheManager) extends AuxiliaryOpiskeluoikeusRepository with Logging {
  protected def opiskeluoikeudetByHetu(hetu: String): List[OO]

  // hetu -> org.oids cache for filtering only
  private val organizationsCache = KeyValueCache[Henkilö.Hetu, List[Organisaatio.Oid]](ExpiringCache(getClass.getSimpleName + ".organisations", 1.hour, 100000), doFindOrgs)
  private val cache = KeyValueCache[Henkilö.Hetu, List[OO]](ExpiringCache(getClass.getSimpleName + ".opiskeluoikeudet", 1.hour, 100), doFindByHenkilö)

  private def doFindOrgs(hetu: Henkilö.Hetu): List[Organisaatio.Oid] = {
    cache(hetu).flatMap(_.oppilaitos).map(_.oid)
  }

  private def doFindByHenkilö(hetu: Henkilö.Hetu): List[OO] = {
    val opiskeluoikeudet = opiskeluoikeudetByHetu(hetu)
    opiskeluoikeudet flatMap { opiskeluoikeus =>
      val oppija = Oppija(UusiHenkilö(hetu, "tuntematon", Some("tuntematon"), "tuntematon"), List(opiskeluoikeus))
      validator match {
        case Some(validator) =>
          validator.validateAsJson(oppija)(KoskiSession.systemUser, AccessType.read).left.foreach { status: HttpStatus =>
            logger.warn("Ulkoisesta järjestelmästä saatu opiskeluoikeus sisältää validointivirheitä " + status)
          }
          Some(opiskeluoikeus)
        case None => Some(opiskeluoikeus)
      }
    }
  }
  private def getHenkilötiedot(oid: String)(implicit user: KoskiSession): Option[TäydellisetHenkilötiedot] = henkilöRepository.findByOid(oid)
  private def quickAccessCheck[T](list: => List[T])(implicit user: KoskiSession): List[T] = if (accessChecker.hasAccess(user)) { list } else { Nil }
  private def findByHenkilö(henkilö: Henkilö with Henkilötiedot)(implicit user: KoskiSession): List[OO] = henkilö.hetu.toList.flatMap( h =>
    quickAccessCheck(cache(h)).filter { oo =>
      accessChecker.hasGlobalAccess(user) ||
      oo.oppilaitos.exists(oppilaitos => user.hasReadAccess(oppilaitos.oid))
    }
  )

  override def filterOppijat(oppijat: List[HenkilötiedotJaOid])(implicit user: KoskiSession): List[HenkilötiedotJaOid] = {
    val globalAccess = accessChecker.hasGlobalAccess(user)
    try {
      if (globalAccess) {
        oppijat.filter(_.hetu.exists(cache(_).nonEmpty))
      } else {
        quickAccessCheck(oppijat.par.filter(_.hetu.exists(organizationsCache(_).exists(user.hasReadAccess))).toList)
      }
    } catch {
      case NonFatal(e) =>
        logger.error(e)(s"Failed to fetch data for filterOppijat, ${if (globalAccess) "returning everything" else "not returning anything"}")
        if (globalAccess) oppijat else Nil
    }
  }

  override def findByOppija(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSession): List[Opiskeluoikeus] = {
    quickAccessCheck(getHenkilötiedot(tunnisteet.oid).toList.flatMap(findByHenkilö(_)))
  }

  override def findByCurrentUser(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSession): List[Opiskeluoikeus] = {
    val oid = tunnisteet.oid
    assert(oid == user.oid, "Käyttäjän oid: " + user.oid + " poikkeaa etsittävän oppijan oidista: " + oid)
    getHenkilötiedot(oid).toList.flatMap(_.hetu.toList.flatMap(cache(_)))
  }
}
