package fi.oph.tor.virta

import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.opiskeluoikeus.{CreateOrUpdateResult, OpiskeluOikeusRepository}
import fi.oph.tor.oppija.{OppijaRepository, PossiblyUnverifiedOppijaOid}
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema.Henkilö._
import fi.oph.tor.schema._
import fi.oph.tor.tor.QueryFilter
import fi.oph.tor.toruser.TorUser
import rx.lang.scala.Observable

case class VirtaOpiskeluoikeusRepository(v: VirtaClient, val oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) extends OpiskeluOikeusRepository {
  def findByHetu(hetu: String)(implicit user: TorUser) = findByHetuWithoutAccessCheck(hetu).filter(oo => user.hasReadAccess(oo.oppilaitos))

  def findByHetuWithoutAccessCheck(hetu: String): List[KorkeakoulunOpiskeluoikeus] =  {
    v.fetchVirtaData(VirtaHakuehtoHetu(hetu)).toList.flatMap( xmlData =>
      VirtaXMLParser(oppijaRepository, oppilaitosRepository, koodistoViitePalvelu).parseVirtaXML(xmlData)
    )
  }

  private def getHetu(oid: String): Option[String] = oppijaRepository.findByOid(oid).map(_.hetu)

  def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[(Oid, List[Opiskeluoikeus])] = Observable.empty
  def filterOppijat(oppijat: Seq[TaydellisetHenkilötiedot])(implicit user: TorUser): Seq[TaydellisetHenkilötiedot] = oppijat.filter(oppija => !findByHetu(oppija.hetu).isEmpty)
  def findByOppijaOid(oid: String)(implicit user: TorUser): Seq[Opiskeluoikeus] = getHetu(oid).toList.flatMap(hetu => findByHetu(hetu))
  def findById(id: Int)(implicit user: TorUser): Option[(Opiskeluoikeus, String)] = None
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser): Either[HttpStatus, CreateOrUpdateResult] = Left(TorErrorCategory.notImplemented.readOnly("Virta-järjestelmään ei voi päivittää tietoja Koskesta"))
}