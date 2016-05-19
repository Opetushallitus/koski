package fi.oph.tor.virta

import fi.oph.tor.cache.{CachingStrategy, KeyValueCache}
import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.log.Logging
import fi.oph.tor.opiskeluoikeus.{CreateOrUpdateResult, OpiskeluOikeusRepository}
import fi.oph.tor.oppija.{OppijaRepository, PossiblyUnverifiedOppijaOid}
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema.Henkilö._
import fi.oph.tor.schema._
import fi.oph.tor.tor.{TorValidator, QueryFilter}
import fi.oph.tor.toruser.{AccessType, TorUser}
import rx.lang.scala.Observable

case class VirtaOpiskeluoikeusRepository(virta: VirtaClient, val oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, validator: Option[TorValidator] = None) extends OpiskeluOikeusRepository with Logging {
  private val converter = VirtaXMLConverter(oppijaRepository, oppilaitosRepository, koodistoViitePalvelu)

  private val cache = KeyValueCache[Henkilö with Henkilötiedot, List[KorkeakoulunOpiskeluoikeus]](CachingStrategy.cacheAllNoRefresh(3600, 100), doFindByHenkilö)

  def doFindByHenkilö(henkilö: Henkilö with Henkilötiedot): List[KorkeakoulunOpiskeluoikeus] = {
    try {
      val opiskeluoikeudet: List[KorkeakoulunOpiskeluoikeus] = virta.opintotiedot(VirtaHakuehtoHetu(henkilö.hetu)).toList
        .flatMap(xmlData => converter.convertToOpiskeluoikeudet(xmlData))

      opiskeluoikeudet flatMap { opiskeluoikeus =>
        val oppija = Oppija(henkilö, List(opiskeluoikeus))
        validator match {
          case Some(validator) =>
            validator.validateAsJson(oppija)(TorUser.systemUser, AccessType.read) match {
              case Right(oppija) =>
                Some(opiskeluoikeus)
              case Left(status) =>
                if (status.errors.map(_.key).contains(TorErrorCategory.badRequest.validation.jsonSchema.key)) {
                  logger.error("Virrasta saatu opiskeluoikeus ei ole validi Koski-järjestelmän JSON schemassa: " + status)
                  None
                } else {
                  logger.warn("Virrasta saatu opiskeluoikeus sisältää validointivirheitä " + status)
                  Some(opiskeluoikeus)
                }
            }
          case None => Some(opiskeluoikeus)
        }
      }
    } catch {
      case e: Exception =>
        logger.error("Failed to fetch data from Virta", e)
        Nil
    }
  }

  def findByHenkilö(henkilö: Henkilö with Henkilötiedot)(implicit user: TorUser): List[KorkeakoulunOpiskeluoikeus] = {
    cache(henkilö).filter(oo => user.hasReadAccess(oo.oppilaitos))
  }

  private def getHetu(oid: String): Option[TaydellisetHenkilötiedot] = oppijaRepository.findByOid(oid)

  def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[(Oid, List[Opiskeluoikeus])] = Observable.empty
  def filterOppijat(oppijat: Seq[TaydellisetHenkilötiedot])(implicit user: TorUser): Seq[TaydellisetHenkilötiedot] = oppijat.par.filter(oppija => !findByHenkilö(oppija).isEmpty).toList
  def findByOppijaOid(oid: String)(implicit user: TorUser): Seq[Opiskeluoikeus] = {
    getHetu(oid).toList.flatMap(findByHenkilö(_))
  }
  def findById(id: Int)(implicit user: TorUser): Option[(Opiskeluoikeus, String)] = None
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser): Either[HttpStatus, CreateOrUpdateResult] = Left(TorErrorCategory.notImplemented.readOnly("Virta-järjestelmään ei voi päivittää tietoja Koskesta"))
}