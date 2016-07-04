package fi.oph.koski.virta

import fi.oph.koski.cache.{CachingStrategy, KeyValueCache}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koski.{KoskiValidator, QueryFilter}
import fi.oph.koski.koskiuser.{AccessType, KoskiUser}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.{CreateOrUpdateResult, OpiskeluOikeusRepository}
import fi.oph.koski.oppija.{OppijaRepository, PossiblyUnverifiedOppijaOid}
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.schema.{Opiskeluoikeus, _}
import rx.lang.scala.Observable

abstract class HetuBasedOpiskeluoikeusRepository[OO <: Opiskeluoikeus](oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, validator: Option[KoskiValidator] = None) extends OpiskeluOikeusRepository with Logging {
  def opiskeluoikeudetByHetu(hetu: String): List[OO]

  // hetu -> org.oids cache for filtering only
  private val organizationsCache = KeyValueCache[Henkilö.Hetu, List[Organisaatio.Oid]](CachingStrategy.cacheAllNoRefresh(3600, 100000), doFindOrgs)
  private val cache = KeyValueCache[Henkilö.Hetu, List[OO]](CachingStrategy.cacheAllNoRefresh(3600, 100), doFindByHenkilö)

  def doFindOrgs(hetu: Henkilö.Hetu): List[Organisaatio.Oid] = {
    cache(hetu).map(_.oppilaitos.oid)
  }

  def doFindByHenkilö(hetu: Henkilö.Hetu): List[OO] = {
    try {
      val opiskeluoikeudet = opiskeluoikeudetByHetu(hetu)

      opiskeluoikeudet flatMap { opiskeluoikeus =>
        val oppija = Oppija(UusiHenkilö(hetu, "tuntematon", "tuntematon", "tuntematon"), List(opiskeluoikeus))
        validator match {
          case Some(validator) =>
            validator.validateAsJson(oppija)(KoskiUser.systemUser, AccessType.read) match {
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
        logger.error(e)("Failed to fetch data")
        Nil
    }
  }

  def findByHenkilö(henkilö: Henkilö with Henkilötiedot)(implicit user: KoskiUser): List[OO] = cache(henkilö.hetu).filter(oo => user.hasReadAccess(oo.oppilaitos.oid))

  private def getHetu(oid: String): Option[TäydellisetHenkilötiedot] = oppijaRepository.findByOid(oid)

  def query(filters: List[QueryFilter])(implicit user: KoskiUser): Observable[(Oid, List[Opiskeluoikeus])] = Observable.empty
  def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiUser): Seq[HenkilötiedotJaOid] = oppijat.par.filter(oppija => !organizationsCache(oppija.hetu).filter(orgOid => user.hasReadAccess(orgOid)).isEmpty).toList
  def findByOppijaOid(oid: String)(implicit user: KoskiUser): Seq[Opiskeluoikeus] = {
    getHetu(oid).toList.flatMap(findByHenkilö(_))
  }
  def findById(id: Int)(implicit user: KoskiUser): Option[(Opiskeluoikeus, String)] = None
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiUser): Either[HttpStatus, CreateOrUpdateResult] = Left(KoskiErrorCategory.notImplemented.readOnly("Ulkoiseen järjestelmään ei voi päivittää tietoja Koskesta"))

}
