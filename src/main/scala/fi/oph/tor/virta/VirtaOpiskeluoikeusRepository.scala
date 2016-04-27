package fi.oph.tor.virta

import java.time.LocalDate
import java.util.Random
import com.typesafe.config.Config
import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.opiskeluoikeus.{CreateOrUpdateResult, OpiskeluOikeusRepository}
import fi.oph.tor.oppija.{OppijaRepository, PossiblyUnverifiedOppijaOid}
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema.Henkilö._
import fi.oph.tor.schema._
import fi.oph.tor.tor.QueryFilter
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.util.Files
import rx.lang.scala.Observable

object VirtaOpiskeluoikeusRepository {
  def apply(config: Config, oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository) = new MockVirtaPalvelu(oppijaRepository, oppilaitosRepository)
}

trait VirtaOpiskeluoikeusRepository extends OpiskeluOikeusRepository {
  def oppijaRepository: OppijaRepository
  def findByHetu(hetu: String): List[KorkeakoulunOpiskeluoikeus]
  def getHetu(oid: String): Option[String] = oppijaRepository.findByOid(oid).map(_.hetu)

  def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[(Oid, List[Opiskeluoikeus])] = Observable.never
  def filterOppijat(oppijat: Seq[TaydellisetHenkilötiedot])(implicit user: TorUser): Seq[TaydellisetHenkilötiedot] = oppijat.filter(oppija => !findByHetu(oppija.hetu).isEmpty)
  def findByOppijaOid(oid: String)(implicit user: TorUser): Seq[Opiskeluoikeus] = getHetu(oid).toList.flatMap(hetu => findByHetu(hetu))
  def findById(id: Int)(implicit user: TorUser): Option[(Opiskeluoikeus, String)] = None
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser): Either[HttpStatus, CreateOrUpdateResult] = Left(TorErrorCategory.badRequest.readOnly("Virta-järjestelmään ei voi päivittää tietoja Koskesta"))
}

class RemoteVirtaPalvelu(config: Config, val oppijaRepository: OppijaRepository) extends VirtaOpiskeluoikeusRepository {
  val virtaClient = VirtaClient(VirtaConfig.fromConfig(config))

  override def findByHetu(hetu: String): List[KorkeakoulunOpiskeluoikeus] = ???
}


class MockVirtaPalvelu(val oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository) extends VirtaOpiskeluoikeusRepository {
  override def findByHetu(hetu: String): List[KorkeakoulunOpiskeluoikeus] = {
    Files.asString("src/main/resources/mockdata/virta/" + hetu + ".xml") match {
      case Some(data) => (scala.xml.XML.loadString(data) \\ "Opiskeluoikeus").map { oo =>
          KorkeakoulunOpiskeluoikeus(
            id = Some(new Random().nextInt()),
            versionumero = None,
            lähdejärjestelmänId = None, // TODO virta
            alkamispäivä = (oo \ "AlkuPvm").headOption.map(alku => LocalDate.parse(alku.text)),
            arvioituPäättymispäivä = None,
            päättymispäivä = (oo \ "LoppuPvm").headOption.map(loppu => LocalDate.parse(loppu.text)),
            oppilaitos = (oo \ "Myontaja" \ "Koodi").headOption.flatMap(koodi => oppilaitosRepository.findByOppilaitosnumero(koodi.text)).getOrElse(throw new RuntimeException("missing oppilaitos")),
            koulutustoimija = None,
            suoritukset = Nil,
            tila = None,
            läsnäolotiedot = None
          )
        }.toList
      case _ => Nil
    }
  }
}
