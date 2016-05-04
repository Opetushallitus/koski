package fi.oph.tor.virta

import java.time.LocalDate
import java.time.LocalDate.{parse => date}
import java.util.Random

import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.localization.LocalizedString.{finnish, sanitize}
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema._

import scala.xml.Node
case class VirtaXMLConverter(oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) {

  def convert(virtaXml: Node): List[KorkeakoulunOpiskeluoikeus] = {
    implicit def localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

    def findOppilaitos(numero: String) = {
      oppilaitosRepository.findByOppilaitosnumero(numero).orElse(throw new RuntimeException("Oppilaitosta ei löydy: " + numero))
    }

    (virtaXml \\ "Opiskeluoikeus").map { (opiskeluoikeus: Node) =>

      val oppilaitos: Option[Oppilaitos] = (opiskeluoikeus \ "Myontaja" \ "Koodi").headOption.orElse(opiskeluoikeus \ "Myontaja" headOption).flatMap(
        koodi => findOppilaitos(koodi.text)
      )

      val läsnäolotiedot: Option[Läsnäolotiedot] = {
        (virtaXml \\ "LukukausiIlmoittautuminen").filter(i => opiskelijaAvain(i) == opiskelijaAvain(opiskeluoikeus) && myöntäjä(opiskeluoikeus) == myöntäjä(i))
          .sortBy(alkuPvm)
          .map(i => Läsnäolojakso(alkuPvm(i), (i \ "LoppuPvm").headOption.map(l => date(l.text)), koodistoViitePalvelu.getKoodistoKoodiViite("korkeakoululasnaolotila", i \ "Tila" text).getOrElse(throw new RuntimeException("invalid läsnäolontila: " + (i \ "Tila").text))))
          .toList match {
            case Nil => None
            case xs => Some(Läsnäolotiedot(xs))
        }
      }

      KorkeakoulunOpiskeluoikeus(
        id = Some(new Random().nextInt()),
        versionumero = None,
        lähdejärjestelmänId = Some(LähdejärjestelmäId(opiskeluoikeus \ "@avain" text, Koodistokoodiviite("virta", "lahdejarjestelma"))),
        alkamispäivä = (opiskeluoikeus \ "AlkuPvm").headOption.map(alku => LocalDate.parse(alku.text)),
        arvioituPäättymispäivä = None,
        päättymispäivä = (opiskeluoikeus \ "LoppuPvm").headOption.map(loppu => LocalDate.parse(loppu.text)),
        oppilaitos = oppilaitos.getOrElse(throw new RuntimeException("missing oppilaitos")),
        koulutustoimija = None,
        suoritukset = tutkintoSuoritukset(opiskeluoikeus, virtaXml),
        tila = None,
        läsnäolotiedot = läsnäolotiedot
      )
    }.toList
  }

  private def tutkintoSuoritukset(opiskeluoikeus: Node, virtaXml: Node) = {
    def tutkinto(koulutuskoodi: String): KorkeakouluTutkinto = {
      KorkeakouluTutkinto(koodistoViitePalvelu.getKoodistoKoodiViite("koulutus", koulutuskoodi).getOrElse(throw new scala.RuntimeException("missing koulutus: " + koulutuskoodi)))
    }
    def opintojaksonSuoritukset(opiskeluoikeus: Node, virtaXml: Node) = {
      buildHierarchy(suoritusNodes(opiskeluoikeus, virtaXml).filter(laji(_) == "2"))
    }

    val tutkintosuoritusNodes: List[Node] = suoritusNodes(opiskeluoikeus, virtaXml).filter(laji(_) == "1")
    tutkintosuoritusNodes match {
      case Nil =>
        koulutuskoodi(opiskeluoikeus).map { koulutuskoodi =>
          KorkeakouluTutkinnonSuoritus(
            koulutusmoduuli = tutkinto(koulutuskoodi),
            paikallinenId = None,
            arviointi = None,
            tila = Koodistokoodiviite("KESKEN", "suorituksentila"),
            vahvistus = None,
            suorituskieli = None,
            osasuoritukset = optionalList(opintojaksonSuoritukset(opiskeluoikeus, virtaXml))
          )
        }.toList
      case _ =>
        tutkintosuoritusNodes flatMap { node: Node =>
          koulutuskoodi(node).map { koulutuskoodi =>
            KorkeakouluTutkinnonSuoritus(
              koulutusmoduuli = tutkinto(koulutuskoodi),
              paikallinenId = None,
              arviointi = None,
              tila = Koodistokoodiviite("VALMIS", "suorituksentila"),
              vahvistus = None,
              suorituskieli = None,
              osasuoritukset = optionalList(opintojaksonSuoritukset(opiskeluoikeus, virtaXml))
            )
          }
        }
    }
  }

  private def buildHierarchy(suoritukset: List[Node]): List[KorkeakoulunOpintojaksonSuoritus] = {
    def sisaltyvatAvaimet(node: Node) = {
      (node \ "Sisaltyvyys").toList.map(sisaltyvyysNode => (sisaltyvyysNode \ "@sisaltyvaOpintosuoritusAvain").text)
    }
    def isRoot(node: Node) = {
      !suoritukset.find(sisaltyvatAvaimet(_).contains(avain(node))).isDefined
    }
    def buildHierarchyFromNode(node: Node): KorkeakoulunOpintojaksonSuoritus = {
      val suoritus = convertSuoritus(node)
      val osasuoritukset: List[KorkeakoulunOpintojaksonSuoritus] = sisaltyvatAvaimet(node).map { opintosuoritusAvain =>
        val (osasuoritusNodes, rest) = suoritukset.partition(avain(_) == opintosuoritusAvain)
        osasuoritusNodes match {
          case osasuoritusNode :: Nil => buildHierarchyFromNode(osasuoritusNode)
          case osasuoritusNode :: _ => throw new IllegalArgumentException("Enemmän kuin yksi suoritus avaimella " + opintosuoritusAvain)
          case Nil => throw new IllegalArgumentException("Opintosuoritusta " + opintosuoritusAvain + " ei löydy dokumentista")
        }
      }
      suoritus.copy(osasuoritukset = optionalList(osasuoritukset))
    }
    suoritukset.filter(isRoot).map(buildHierarchyFromNode)
  }

  private def suoritusNodes(opiskeluoikeus: Node, virtaXml: Node): List[Node] = {
    def sisältyyOpiskeluoikeuteen(suoritus: Node): Boolean = {
      val opiskeluoikeusAvain: String = (suoritus \ "@opiskeluoikeusAvain").text
      opiskeluoikeusAvain match {
        case "" => myöntäjä(suoritus) == myöntäjä(opiskeluoikeus) // Jos suorituksella ei ole linkkiä opiskeluoikeuteen, oletetaan sen kuuluvan saman oppilaitoksen opiskeluoikeuteen
        case _ => opiskeluoikeusAvain == avain(opiskeluoikeus)
      }
    }

    val suoritusNodes: List[Node] = (virtaXml \\ "Opintosuoritukset" \\ "Opintosuoritus").filter(sisältyyOpiskeluoikeuteen).toList
    suoritusNodes
  }

  private def convertSuoritus(suoritus: Node) = {
    KorkeakoulunOpintojaksonSuoritus(
      koulutusmoduuli = KorkeakoulunOpintojakso(
        tunniste = Paikallinenkoodi((suoritus \\ "@koulutusmoduulitunniste").text, nimi(suoritus), "koodistoUri"), // hardcoded uri
        nimi = nimi(suoritus),
        laajuus = (suoritus \ "Laajuus" \ "Opintopiste").headOption.map(op => LaajuusOpintopisteissä(op.text.toFloat))
      ),
      paikallinenId = None,
      arviointi = koodistoViitePalvelu.getKoodistoKoodiViite("virtaarvosana", suoritus \ "Arvosana" text).map( arvosana =>
        List(KorkeakoulunArviointi(
          arvosana = arvosana,
          päivä = Some(LocalDate.parse(suoritus \ "SuoritusPvm" text))
        ))
      ),
      tila = Koodistokoodiviite("VALMIS", "suorituksentila"),
      vahvistus = None,
      suorituskieli = (suoritus \\ "Kieli").headOption.flatMap(kieli => koodistoViitePalvelu.getKoodistoKoodiViite("kieli", kieli.text.toUpperCase))
    )
  }

  private def optionalList[A](list: List[A]): Option[List[A]] = list match {
    case Nil => None
    case _ => Some(list)
  }

  private def opiskelijaAvain(node: Node) = {
    (node \ "@opiskelijaAvain").text
  }

  private def avain(node: Node) = {
    (node \ "@avain").text
  }

  private def alkuPvm(node: Node) = {
    date((node \ "AlkuPvm").text)
  }


  private def myöntäjä(node: Node) = {
    (node \ "Myontaja").text
  }

  private def laji(node: Node) = {
    (node \ "Laji").text
  }

  private def koulutuskoodi(node: Node) = {
    (node \\ "Koulutuskoodi").headOption.map(_.text)
  }

  private def nimi(suoritus: Node): LocalizedString = {
    sanitize((suoritus \\ "Nimi" map (nimi => (nimi \ "@kieli" text, nimi text))).toMap).getOrElse(finnish("Suoritus: " + avain(suoritus)))
  }
}
