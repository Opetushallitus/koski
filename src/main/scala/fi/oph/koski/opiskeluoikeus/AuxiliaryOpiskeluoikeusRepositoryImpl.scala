package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.koskiuser.{AccessChecker, KoskiSpecificSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Opiskeluoikeus, Organisaatio}

import scala.concurrent.duration.DurationInt
import scala.util.control.NonFatal

abstract class AuxiliaryOpiskeluoikeusRepositoryImpl[OO <: Opiskeluoikeus, CK <: AnyRef](accessChecker: AccessChecker)(implicit cacheInvalidator: CacheManager) extends AuxiliaryOpiskeluoikeusRepository with Logging {

  override def filterOppijat[A <: HenkilönTunnisteet](oppijat: List[A])(implicit user: KoskiSpecificSession): List[A] = {
    val globalAccess = accessChecker.hasGlobalAccess(user)
    try {
      if (globalAccess) {
        oppijat.filter(oppija => cachedOrganizations(oppija).nonEmpty)
      } else {
        quickAccessCheck(oppijat.par.filter(oppija => cachedOrganizations(oppija).exists(user.hasReadAccess(_, None))).toList)
      }
    } catch {
      case NonFatal(e) =>
        logger.error(e)(s"Failed to fetch data for filterOppijat, ${if (globalAccess) "returning everything" else "not returning anything"}")
        if (globalAccess) oppijat else Nil
    }
  }

  override def findByOppija(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSpecificSession): List[OO] = {
    quickAccessCheck(filterByOrganisaatio(cachedOpiskeluoikeudet(tunnisteet)))
  }

  override def findByCurrentUser(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSpecificSession): List[OO] = {
    assert(tunnisteet.oid == user.oid, "Käyttäjän oid: " + user.oid + " poikkeaa etsittävän oppijan oidista: " + tunnisteet.oid)
    cachedOpiskeluoikeudet(tunnisteet)
  }

  override def findHuollettavaByOppija(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSpecificSession): List[OO] = {
    assert(user.isUsersHuollettava(tunnisteet.oid), "Käyttäjän oidilla: " + user.oid + " ei ole huollettavaa: " + tunnisteet.oid)
    cachedOpiskeluoikeudet(tunnisteet)
  }

  protected def buildCacheKey(tunnisteet: HenkilönTunnisteet): CK

  protected def uncachedOpiskeluoikeudet(cacheKey: CK): List[OO]

  private val cache = KeyValueCache[CK, List[OO]](ExpiringCache(getClass.getSimpleName + ".opiskeluoikeudet", 1.hour, 100), uncachedOpiskeluoikeudet)

  private def cachedOpiskeluoikeudet(tunnisteet: HenkilönTunnisteet): List[OO] = {
    cache(buildCacheKey(tunnisteet))
  }

  // tunniste -> org.oids cache for filtering only (much larger than opiskeluoikeus cache)
  // can contain special value "UnknownOrganization" to indicate that opiskeluoikeus exists, but it has oppilaitos=None
  // (this is used in filterOppijat when globalAccess is True)
  private val organizationsCache = KeyValueCache[CK, List[Organisaatio.Oid]](ExpiringCache(getClass.getSimpleName + ".organisations", 1.hour, 100000), uncachedOrganizations)
  private val UnknownOrganization = "1.2.246.562.10.99999999999"

  private def uncachedOrganizations(cacheKey: CK): List[Organisaatio.Oid] = {
    val opiskeluoikeudet = cache(cacheKey)
    val oppilaitokset = opiskeluoikeudet.flatMap(_.oppilaitos).map(_.oid)
    if (oppilaitokset.isEmpty && opiskeluoikeudet.nonEmpty)
      List(UnknownOrganization)
    else
      oppilaitokset
  }

  private def cachedOrganizations(tunnisteet: HenkilönTunnisteet): List[Organisaatio.Oid] = {
    organizationsCache(buildCacheKey(tunnisteet))
  }

  private def quickAccessCheck[T](list: => List[T])(implicit user: KoskiSpecificSession): List[T] = if (accessChecker.hasAccess(user)) { list } else { Nil }

  private def filterByOrganisaatio(opiskeluoikeudet: List[OO])(implicit user: KoskiSpecificSession): List[OO] = {
    opiskeluoikeudet.filter { oo =>
      accessChecker.hasGlobalAccess(user) ||
        oo.oppilaitos.exists(oppilaitos => user.hasReadAccess(oppilaitos.oid, None))
    }
  }
}
