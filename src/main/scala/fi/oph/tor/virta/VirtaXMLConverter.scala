package fi.oph.tor.virta

import java.time.LocalDate
import java.time.LocalDate.{parse => date}
import java.util.Random

import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.localization.LocalizedString.{finnish, sanitize}
import fi.oph.tor.log.Logging
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema._

import scala.xml.Node
case class VirtaXMLConverter(oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) extends Logging {

  def convertToOpiskeluoikeudet(virtaXml: Node): List[KorkeakoulunOpiskeluoikeus] = {
    implicit def localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

    def findOppilaitos(numero: String) = {
      oppilaitosRepository.findByOppilaitosnumero(numero).orElse(throw new RuntimeException("Oppilaitosta ei löydy: " + numero))
    }

    //println(XML.prettyPrint(virtaXml))
    val suoritusNodeList: List[Node] = suoritusNodes(virtaXml)
    val suoritusRoots: List[Node] = suoritusNodeList.filter(isRoot(suoritusNodeList)(_))
    val opiskeluOikeusNodes: List[Node] = (virtaXml \\ "Opiskeluoikeus").toList

    val (orphans, opiskeluoikeudet) = opiskeluOikeusNodes.foldLeft((suoritusRoots, Nil: List[KorkeakoulunOpiskeluoikeus])) { case ((suoritusRootsLeft, opiskeluOikeudet), opiskeluoikeusNode) =>
      val (opiskeluOikeudenSuoritukset: List[Node], muutSuoritukset: List[Node]) = suoritusRootsLeft.partition(sisältyyOpiskeluoikeuteen(_, opiskeluoikeusNode, suoritusNodeList))

      val oppilaitos: Option[Oppilaitos] = (opiskeluoikeusNode \ "Myontaja" \ "Koodi").headOption.orElse(opiskeluoikeusNode \ "Myontaja" headOption).flatMap(
        koodi => findOppilaitos(koodi.text)
      )

      val läsnäolotiedot = list2Optional((virtaXml \\ "LukukausiIlmoittautuminen").filter(i => opiskelijaAvain(i) == opiskelijaAvain(opiskeluoikeusNode) && myöntäjä(opiskeluoikeusNode) == myöntäjä(i))
        .sortBy(alkuPvm)
        .map(i => KorkeakoulunLäsnäolojakso(alkuPvm(i), loppuPvm(i), requiredKoodi("virtalukukausiilmtila", i \ "Tila" text).get))
        .toList, KorkeakoulunLäsnäolotiedot)

      val opiskeluoikeudenTila: Option[KorkeakoulunOpiskeluoikeudenTila] = list2Optional((opiskeluoikeusNode \ "Tila")
        .sortBy(alkuPvm)
        .map(tila => KorkeakoulunOpiskeluoikeusjakso(alkuPvm(tila), loppuPvm(tila), requiredKoodi("virtaopiskeluoikeudentila", tila \ "Koodi" text).get))
        .toList, KorkeakoulunOpiskeluoikeudenTila)


      val opiskeluoikeus = KorkeakoulunOpiskeluoikeus(
        id = Some(new Random().nextInt()),
        versionumero = None,
        lähdejärjestelmänId = Some(LähdejärjestelmäId(opiskeluoikeusNode \ "@avain" text, Koodistokoodiviite("virta", "lahdejarjestelma"))),
        alkamispäivä = (opiskeluoikeusNode \ "AlkuPvm").headOption.map(alku => LocalDate.parse(alku.text)),
        arvioituPäättymispäivä = None,
        päättymispäivä = (opiskeluoikeusNode \ "LoppuPvm").headOption.map(loppu => LocalDate.parse(loppu.text)),
        oppilaitos = oppilaitos.getOrElse(throw new RuntimeException("missing oppilaitos")),
        koulutustoimija = None,
        suoritukset = opiskeluOikeudenSuoritukset.flatMap(convertSuoritus(_, suoritusNodeList)),
        tila = opiskeluoikeudenTila,
        läsnäolotiedot = läsnäolotiedot
      )

      (muutSuoritukset, opiskeluoikeus :: opiskeluOikeudet)
    }

    // TODO: collect orphans
    // TODO: "synteettinen tutkinto" KESKEN-tilassa
    // TODO: KorkeakoulutSpec fails

    opiskeluoikeudet
  }

  private def convertSuoritus(node: Node, allNodes: List[Node]): Option[KorkeakouluSuoritus] = {
    laji(node) match {
      case "1" => // tutkinto
        koulutuskoodi(node).map { koulutuskoodi =>
          val osasuoritukset = childNodes(node, allNodes).map(convertOpintojaksonSuoritus(_, allNodes))

          KorkeakouluTutkinnonSuoritus(
            koulutusmoduuli = tutkinto(koulutuskoodi),
            paikallinenId = None,
            arviointi = None,
            tila = Koodistokoodiviite("VALMIS", "suorituksentila"),
            vahvistus = None,
            suorituskieli = None,
            osasuoritukset = optionalList(osasuoritukset)
          )
        }
      case "2" => // opintojakso
        Some(convertOpintojaksonSuoritus(node, allNodes))
      case laji: String =>
        logger.warn("Tuntematon laji: " + laji)
        None
    }
  }

  def convertOpintojaksonSuoritus(node: Node, allNodes: List[Node]): KorkeakoulunOpintojaksonSuoritus = {
    val osasuoritukset = childNodes(node, allNodes).map(convertOpintojaksonSuoritus(_, allNodes))

    KorkeakoulunOpintojaksonSuoritus(
      koulutusmoduuli = KorkeakoulunOpintojakso(
        tunniste = Paikallinenkoodi((node \\ "@koulutusmoduulitunniste").text, nimi(node), "koodistoUri"), // hardcoded uri
        nimi = nimi(node),
        laajuus = (node \ "Laajuus" \ "Opintopiste").headOption.map(op => LaajuusOpintopisteissä(op.text.toFloat))
      ),
      paikallinenId = None,
      arviointi = koodistoViitePalvelu.getKoodistoKoodiViite("virtaarvosana", node \ "Arvosana" \ "_" text).map( arvosana =>
        List(KorkeakoulunArviointi(
          arvosana = arvosana,
          päivä = Some(LocalDate.parse(node \ "SuoritusPvm" text))
        ))
      ),
      tila = Koodistokoodiviite("VALMIS", "suorituksentila"),
      vahvistus = None,
      suorituskieli = (node \\ "Kieli").headOption.flatMap(kieli => requiredKoodi("kieli", kieli.text.toUpperCase)),
      osasuoritukset = optionalList(osasuoritukset)
    )
  }

  private def isRoot(suoritukset: Seq[Node])(node: Node) = {
    !suoritukset.exists(sisaltyvatAvaimet(_).contains(avain(node)))
  }

  private def sisaltyvatAvaimet(node: Node) = {
    (node \ "Sisaltyvyys").toList.map(sisaltyvyysNode => (sisaltyvyysNode \ "@sisaltyvaOpintosuoritusAvain").text)
  }

  private def childNodes(node: Node, allNodes: List[Node]) = {
    sisaltyvatAvaimet(node).map { opintosuoritusAvain =>
      val osasuoritusNodes = allNodes.filter(avain(_) == opintosuoritusAvain)
      osasuoritusNodes match {
        case osasuoritusNode :: Nil => osasuoritusNode
        case osasuoritusNode :: _ => throw new IllegalArgumentException("Enemmän kuin yksi suoritus avaimella " + opintosuoritusAvain)
        case Nil => throw new IllegalArgumentException("Opintosuoritusta " + opintosuoritusAvain + " ei löydy dokumentista")
      }
    }
  }

  private def suoritusNodes(virtaXml: Node) = {
    (virtaXml \\ "Opintosuoritukset" \\ "Opintosuoritus").toList
  }

  def sisältyyOpiskeluoikeuteen(suoritus: Node, opiskeluoikeus: Node, allNodes: List[Node]): Boolean = {
    val opiskeluoikeusAvain: String = (suoritus \ "@opiskeluoikeusAvain").text
    opiskeluoikeusAvain == avain(opiskeluoikeus) || childNodes(suoritus, allNodes).find(sisältyyOpiskeluoikeuteen(_, opiskeluoikeus, allNodes)).isDefined
  }

  private def list2Optional[A, B](list: List[A], f: List[A] => B): Option[B] = list match {
    case Nil => None
    case xs => Some(f(xs))
  }

  private def optionalList[A](list: List[A]): Option[List[A]] = list2Optional[A, List[A]](list, identity)

  private def requiredKoodi(uri: String, koodi: String) = {
    koodistoViitePalvelu.validate(Koodistokoodiviite(koodi, uri)).orElse(throw new IllegalArgumentException("Puuttuva koodi: " + Koodistokoodiviite(koodi, uri)))
  }

  private def tutkinto(koulutuskoodi: String): KorkeakouluTutkinto = {
    KorkeakouluTutkinto(requiredKoodi("koulutus", koulutuskoodi).get)
  }

  private def loppuPvm(n: Node): Option[LocalDate] = {
    (n \ "LoppuPvm").headOption.map(l => date(l.text))
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
    (node \ "Myontaja" \ "Koodi").text
  }

  private def laji(node: Node) = {
    (node \ "Laji").text
  }

  private def koulutuskoodi(node: Node): Option[String] = {
    (node \\ "Koulutuskoodi").headOption.map(_.text)
  }

  private def nimi(suoritus: Node): LocalizedString = {
    sanitize((suoritus \\ "Nimi" map (nimi => (nimi \ "@kieli" text, nimi text))).toMap).getOrElse(finnish("Suoritus: " + avain(suoritus)))
  }
}
