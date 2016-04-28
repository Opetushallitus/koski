package fi.oph.tor.virta

import java.time.LocalDate
import java.util.Random

import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.localization.LocalizedString.finnish
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema._

import scala.xml.Node
case class VirtaXMLParser(oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) {
  def parseVirtaXML(virtaXml: Node) = {
    (virtaXml \\ "Opiskeluoikeus").map { (opiskeluoikeus: Node) =>
      KorkeakoulunOpiskeluoikeus(
        id = Some(new Random().nextInt()),
        versionumero = None,
        lähdejärjestelmänId = None, // TODO virta
        alkamispäivä = (opiskeluoikeus \ "AlkuPvm").headOption.map(alku => LocalDate.parse(alku.text)),
        arvioituPäättymispäivä = None,
        päättymispäivä = (opiskeluoikeus \ "LoppuPvm").headOption.map(loppu => LocalDate.parse(loppu.text)),
        oppilaitos = (opiskeluoikeus \ "Myontaja" \ "Koodi").headOption.flatMap(koodi => oppilaitosRepository.findByOppilaitosnumero(koodi.text)).getOrElse(throw new RuntimeException("missing oppilaitos")),
        koulutustoimija = None,
        suoritukset = tutkintoSuoritus(opiskeluoikeus, virtaXml).toList,
        tila = None,
        läsnäolotiedot = None
      )
    }.toList
  }

  def tutkintoSuoritus(opiskeluoikeus: Node, virtaXml: Node) = {
    (opiskeluoikeus \\ "Jakso" \\ "Koulutuskoodi").headOption.map { koulutuskoodi =>
      KorkeakouluTutkinnonSuoritus(
        koulutusmoduuli = KorkeakouluTutkinto(koodistoViitePalvelu.getKoodistoKoodiViite("koulutus", koulutuskoodi.text).getOrElse(throw new RuntimeException("missing koulutus: " + koulutuskoodi.text))),
        paikallinenId = None,
        arviointi = None,
        tila = Koodistokoodiviite("KESKEN", "suorituksentila"), // TODO, how to get this ???
        vahvistus = None,
        suorituskieli = None,
        osasuoritukset = opintoSuoritukset(opiskeluoikeus, virtaXml)
      )
    }
  }

  def opintoSuoritukset(opiskeluoikeus: Node, virtaXml: Node) = {
    def nimi(suoritus: Node): LocalizedString = {
      finnish((suoritus \\ "Nimi").filter(node => node.attribute("kieli").exists(_.text == "fi")).text)
    }

    (virtaXml \\ "Opintosuoritukset" \\ "Opintosuoritus").filter(suoritus => (suoritus \ "@opiskeluoikeusAvain").text == (opiskeluoikeus \ "@avain").text).map { suoritus =>
      KorkeakoulunOpintojaksonSuoritus(
        koulutusmoduuli = KorkeakoulunOpintojakso(
          tunniste = Paikallinenkoodi(
            (suoritus \\ "@koulutusmoduulitunniste").text,
            nimi(suoritus),
            "koodistoUri"), // hardcoded
          nimi = nimi(suoritus),
          laajuus = Some(LaajuusOsaamispisteissä(15)) // hardcoded
        ),
        paikallinenId = None,
        arviointi = None,
        tila = Koodistokoodiviite("VALMIS", "suorituksentila"),
        vahvistus = None,
        suorituskieli = None
      )
    }.toList match {
      case Nil => None
      case xs => Some(xs)
    }
  }
}
