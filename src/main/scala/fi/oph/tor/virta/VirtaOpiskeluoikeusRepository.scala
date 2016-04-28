package fi.oph.tor.virta

import java.time.LocalDate
import java.util.Random
import com.typesafe.config.Config
import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.opiskeluoikeus.{CreateOrUpdateResult, OpiskeluOikeusRepository}
import fi.oph.tor.oppija.{OppijaRepository, PossiblyUnverifiedOppijaOid}
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema.Henkilö._
import fi.oph.tor.schema._
import fi.oph.tor.tor.QueryFilter
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.util.Files
import rx.lang.scala.Observable

import scala.RuntimeException
import scala.xml.Node

object VirtaOpiskeluoikeusRepository {
  def apply(config: Config, oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) = new MockVirtaPalvelu(oppijaRepository, oppilaitosRepository, koodistoViitePalvelu)
}

trait VirtaOpiskeluoikeusRepository extends OpiskeluOikeusRepository {
  def oppijaRepository: OppijaRepository
  def findByHetu(hetu: String): List[KorkeakoulunOpiskeluoikeus]
  def getHetu(oid: String): Option[String] = oppijaRepository.findByOid(oid).map(_.hetu)

  def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[(Oid, List[Opiskeluoikeus])] = Observable.empty
  def filterOppijat(oppijat: Seq[TaydellisetHenkilötiedot])(implicit user: TorUser): Seq[TaydellisetHenkilötiedot] = oppijat.filter(oppija => !findByHetu(oppija.hetu).isEmpty)
  def findByOppijaOid(oid: String)(implicit user: TorUser): Seq[Opiskeluoikeus] = getHetu(oid).toList.flatMap(hetu => findByHetu(hetu))
  def findById(id: Int)(implicit user: TorUser): Option[(Opiskeluoikeus, String)] = None
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser): Either[HttpStatus, CreateOrUpdateResult] = Left(TorErrorCategory.notImplemented.readOnly("Virta-järjestelmään ei voi päivittää tietoja Koskesta"))
}

class RemoteVirtaPalvelu(config: Config, val oppijaRepository: OppijaRepository) extends VirtaOpiskeluoikeusRepository {
  val virtaClient = VirtaClient(VirtaConfig.fromConfig(config))

  override def findByHetu(hetu: String): List[KorkeakoulunOpiskeluoikeus] = ???
}


class MockVirtaPalvelu(val oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) extends VirtaOpiskeluoikeusRepository {

  def tutkintoSuoritus(opiskeluoikeus: Node) = {
    (opiskeluoikeus \\ "Jakso" \\ "Koulutuskoodi").headOption.map { koulutuskoodi =>
      KorkeakouluTutkinnonSuoritus(
        koulutusmoduuli = KorkeakouluTutkinto(koodistoViitePalvelu.getKoodistoKoodiViite("koulutus", koulutuskoodi.text).getOrElse(throw new RuntimeException("missing koulutus: " + koulutuskoodi.text))),
        paikallinenId = None,
        arviointi = None,
        tila = Koodistokoodiviite("KESKEN", "suorituksentila"), // TODO, how to get this ???
        vahvistus = None,
        suorituskieli = None,
        osasuoritukset = None
      )
    }
  }

  override def findByHetu(hetu: String): List[KorkeakoulunOpiskeluoikeus] = {
    Files.asString("src/main/resources/mockdata/virta/" + hetu + ".xml") match {
      case Some(data) =>
        (scala.xml.XML.loadString(data) \\ "Opiskeluoikeus").map { (opiskeluoikeus: Node) =>
          KorkeakoulunOpiskeluoikeus(
            id = Some(new Random().nextInt()),
            versionumero = None,
            lähdejärjestelmänId = None, // TODO virta
            alkamispäivä = (opiskeluoikeus \ "AlkuPvm").headOption.map(alku => LocalDate.parse(alku.text)),
            arvioituPäättymispäivä = None,
            päättymispäivä = (opiskeluoikeus \ "LoppuPvm").headOption.map(loppu => LocalDate.parse(loppu.text)),
            oppilaitos = (opiskeluoikeus \ "Myontaja" \ "Koodi").headOption.flatMap(koodi => oppilaitosRepository.findByOppilaitosnumero(koodi.text)).getOrElse(throw new RuntimeException("missing oppilaitos")),
            koulutustoimija = None,
            suoritukset = tutkintoSuoritus(opiskeluoikeus).toList,
            tila = None,
            läsnäolotiedot = None
          )
        }.toList
      case _ => Nil
    }
  }
}
