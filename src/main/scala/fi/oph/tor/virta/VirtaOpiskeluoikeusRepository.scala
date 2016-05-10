package fi.oph.tor.virta

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

case class VirtaOpiskeluoikeusRepository(v: VirtaClient, val oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, validator: TorValidator) extends OpiskeluOikeusRepository with Logging {
  private val converter = VirtaXMLConverter(oppijaRepository, oppilaitosRepository, koodistoViitePalvelu)

  def findByHenkilö(henkilö: Henkilö with Henkilötiedot)(implicit user: TorUser) = {
    val opiskeluoikeudet: List[KorkeakoulunOpiskeluoikeus] = v.opintotiedot(VirtaHakuehtoHetu(henkilö.hetu)).toList
      .flatMap(xmlData => converter.convert(xmlData))
      //.filter(oo => user.hasReadAccess(oo.oppilaitos)) TODO: access check, kuha on olemassa asiaan kuuluvat käyttöoikeusryhmät. Nyt kovakoodattau 2aste-rajapinnat -ryhmää ei löydy esim. aalto-yliopistolta.

    opiskeluoikeudet match {
      case Nil => Nil
      case _ =>
        val oppija = Oppija(henkilö, opiskeluoikeudet)
        validator.validateAsJson(oppija)(user, AccessType.read) match {
          case Right(oppija) => opiskeluoikeudet
          case Left(status) =>
            logger.error("Virrasta saatu opiskeluoikeus ei ole validi: " + status)
            Nil
        }
    }
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