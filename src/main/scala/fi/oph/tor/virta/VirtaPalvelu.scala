package fi.oph.tor.virta

import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.tor.schema.{Koodistokoodiviite, Oppilaitos, KorkeakoulunOpiskeluoikeus}
import fi.oph.tor.util.Files

object VirtaPalvelu {
  def apply(config: Config) = MockVirtaPalvelu
}

trait VirtaPalvelu {
  def findByHetu(hetu: String): Option[List[KorkeakoulunOpiskeluoikeus]]
}

class RemoteVirtaPalvelu(config: Config) extends VirtaPalvelu {
  val virtaClient = VirtaClient(VirtaConfig.fromConfig(config))

  override def findByHetu(hetu: String): Option[List[KorkeakoulunOpiskeluoikeus]] = ???
}


object MockVirtaPalvelu extends VirtaPalvelu {

  override def findByHetu(hetu: String): Option[List[KorkeakoulunOpiskeluoikeus]] = {
    Files.asString("src/main/resources/mockdata/virta/" + hetu + ".xml") match {
      case Some(data) => (scala.xml.XML.loadString(data) \\ "Opiskeluoikeus").toList match {
        case xs => Some(xs.map { oo =>
          KorkeakoulunOpiskeluoikeus(
            id = None,
            versionumero = None,
            lähdejärjestelmänId = None, // TODO virta
            alkamispäivä = (oo \ "AlkuPvm").headOption.map(alku => LocalDate.parse(alku.text)),
            arvioituPäättymispäivä = None,
            päättymispäivä = (oo \ "LoppuPvm").headOption.map(loppu => LocalDate.parse(loppu.text)),
            oppilaitos = (oo \ "Myontaja" \ "Koodi").headOption.map(koodi => Oppilaitos("dummy", Some(Koodistokoodiviite(koodi.text, None, None, koodistoUri = "oppilaitosnumero", None)))).getOrElse(throw new RuntimeException("missing oppilaitos")),
            koulutustoimija = None,
            suoritukset = Nil,
            tila = None,
            läsnäolotiedot = None
          )
        })
        case Nil => None
      }
      case _ => None
    }
  }

}