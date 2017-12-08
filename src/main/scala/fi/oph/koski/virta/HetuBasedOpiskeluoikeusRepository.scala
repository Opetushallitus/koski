package fi.oph.koski.virta

import fi.oph.koski.cache.{Cache, CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{AccessChecker, AccessType, KoskiSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.AuxiliaryOpiskeluoikeusRepository
import fi.oph.koski.henkilo.{FindByOid, HenkilöRepository}
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.schema.{Opiskeluoikeus, _}
import fi.oph.koski.validation.KoskiValidator
import scala.concurrent.duration._

abstract class HetuBasedOpiskeluoikeusRepository[OO <: Opiskeluoikeus](henkilöRepository: FindByOid, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, accessChecker: AccessChecker, validator: Option[KoskiValidator] = None)(implicit cacheInvalidator: CacheManager) extends AuxiliaryOpiskeluoikeusRepository with Logging {
  def opiskeluoikeudetByHetu(hetu: String): List[OO]

  // hetu -> org.oids cache for filtering only
  private val organizationsCache = KeyValueCache[Henkilö.Hetu, List[Organisaatio.Oid]](ExpiringCache(getClass.getSimpleName + ".organisations", 1 hour, 100000), doFindOrgs)
  private val cache = KeyValueCache[Henkilö.Hetu, List[OO]](ExpiringCache(getClass.getSimpleName + ".opiskeluoikeudet", 1 hour, 100), doFindByHenkilö)

  def doFindOrgs(hetu: Henkilö.Hetu): List[Organisaatio.Oid] = {
    cache(hetu).map(_.getOppilaitos.oid)
  }

  def doFindByHenkilö(hetu: Henkilö.Hetu): List[OO] = {
    try {
      val opiskeluoikeudet = opiskeluoikeudetByHetu(hetu)

      opiskeluoikeudet flatMap { opiskeluoikeus =>
        val oppija = Oppija(UusiHenkilö(Some(hetu), "tuntematon", "tuntematon", "tuntematon"), List(opiskeluoikeus))
        validator match {
          case Some(validator) =>
            validator.validateAsJson(oppija)(KoskiSession.systemUser, AccessType.read) match {
              case Right(oppija) =>
                Some(opiskeluoikeus)
              case Left(status) =>
                if (status.errors.map(_.key).contains(KoskiErrorCategory.badRequest.validation.jsonSchema.key)) {
                  logger.error("Ulkoisesta järjestelmästä saatu opiskeluoikeus ei ole validi Koski-järjestelmän JSON schemassa: " + status)
                  None
                } else {
                  logger.warn("Ulkoisesta järjestelmästä saatu opiskeluoikeus sisältää validointivirheitä " + status)
                  Some(opiskeluoikeus)
                }
            }
          case None => Some(opiskeluoikeus)
        }
      }
    } catch {
      case e: Exception =>
        logger.error(e)(s"Failed to fetch data for $hetu")
        Nil
    }
  }
  private def getHenkilötiedot(oid: String)(implicit user: KoskiSession): Option[TäydellisetHenkilötiedot] = henkilöRepository.findByOid(oid)
  private def accessCheck[T](list: => List[T])(implicit user: KoskiSession): List[T] = if (accessChecker.hasAccess(user)) { list } else { Nil }
  private def findByHenkilö(henkilö: Henkilö with Henkilötiedot)(implicit user: KoskiSession): List[OO] = henkilö.hetu.map(h => accessCheck(cache(h)).filter(oo => user.hasReadAccess(oo.getOppilaitos.oid))).getOrElse(Nil)

  // Public methods
  def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession): List[HenkilötiedotJaOid] =
    accessCheck(oppijat.par.filter(oppija => oppija.hetu.exists(organizationsCache(_).filter(orgOid => user.hasReadAccess(orgOid)).nonEmpty)).toList)

  def findByOppijaOid(oid: String)(implicit user: KoskiSession): List[Opiskeluoikeus] = accessCheck(getHenkilötiedot(oid).toList.flatMap(findByHenkilö(_)))

  def findByUserOid(oid: String)(implicit user: KoskiSession): List[Opiskeluoikeus] = {
    assert(oid == user.oid, "Käyttäjän oid: " + user.oid + " poikkeaa etsittävän oppijan oidista: " + oid)
    getHenkilötiedot(oid).toList.flatMap(_.hetu.toList.flatMap(cache(_)))
  }
}
