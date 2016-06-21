package fi.oph.koski.virta

import java.time.LocalDate
import java.time.LocalDate.{parse => date}

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.{finnish, sanitize}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.log.Logging
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.schema._

import scala.xml.Node
case class VirtaXMLConverter(oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) extends Logging {

  def convertToOpiskeluoikeudet(virtaXml: Node): List[KorkeakoulunOpiskeluoikeus] = {
    implicit def localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

    val suoritusNodeList: List[Node] = suoritusNodes(virtaXml)
    val suoritusRoots: List[Node] = suoritusNodeList.filter(isRoot(suoritusNodeList)(_))
    val opiskeluOikeusNodes: List[Node] = (virtaXml \\ "Opiskeluoikeus").toList

    val (orphans, opiskeluoikeudet) = opiskeluOikeusNodes.foldLeft((suoritusRoots, Nil: List[KorkeakoulunOpiskeluoikeus])) { case ((suoritusRootsLeft, opiskeluOikeudet), opiskeluoikeusNode) =>
      val (opiskeluOikeudenSuoritukset: List[Node], muutSuoritukset: List[Node]) = suoritusRootsLeft.partition(sisältyyOpiskeluoikeuteen(_, opiskeluoikeusNode, suoritusNodeList))

      val läsnäolotiedot = list2Optional((virtaXml \\ "LukukausiIlmoittautuminen").filter(i => opiskelijaAvain(i) == opiskelijaAvain(opiskeluoikeusNode) && myöntäjä(opiskeluoikeusNode) == myöntäjä(i))
        .sortBy(alkuPvm)
        .map(i => KorkeakoulunLäsnäolojakso(alkuPvm(i), loppuPvm(i), requiredKoodi("virtalukukausiilmtila", i \ "Tila" text).get))
        .toList, KorkeakoulunLäsnäolotiedot)

      val opiskeluoikeudenTila: Option[KorkeakoulunOpiskeluoikeudenTila] = list2Optional((opiskeluoikeusNode \ "Tila")
        .sortBy(alkuPvm)
        .map(tila => KorkeakoulunOpiskeluoikeusjakso(alkuPvm(tila), loppuPvm(tila), requiredKoodi("virtaopiskeluoikeudentila", tila \ "Koodi" text).get))
        .toList, KorkeakoulunOpiskeluoikeudenTila)

      val suoritukset: List[KorkeakouluSuoritus] = opiskeluOikeudenSuoritukset.flatMap(convertSuoritus(_, suoritusNodeList))

      val opiskeluoikeus = KorkeakoulunOpiskeluoikeus(
        id = None,
        lähdejärjestelmänId = Some(LähdejärjestelmäId(opiskeluoikeusNode \ "@avain" text, requiredKoodi("lahdejarjestelma", "virta").get)),
        alkamispäivä = (opiskeluoikeusNode \ "AlkuPvm").headOption.map(alku => LocalDate.parse(alku.text)),
        arvioituPäättymispäivä = None,
        päättymispäivä = (opiskeluoikeusNode \ "LoppuPvm").headOption.map(loppu => LocalDate.parse(loppu.text)),
        oppilaitos = oppilaitos(opiskeluoikeusNode),
        koulutustoimija = None,
        suoritukset = lisääKeskeneräinenTutkintosuoritus(suoritukset, opiskeluoikeusNode),
        tila = opiskeluoikeudenTila,
        läsnäolotiedot = läsnäolotiedot,
        ensisijaisuus = (opiskeluoikeusNode \ "Ensisijaisuus").headOption.map { e => // TODO, should this be a list ?
          Ensisijaisuus(alkuPvm(e), loppuPvm(e))
        }
      )

      (muutSuoritukset, opiskeluoikeus :: opiskeluOikeudet)
    }

    val orphanSuoritukset = orphans.flatMap(convertSuoritus(_, suoritusNodeList))
    val orphanages = orphanSuoritukset.groupBy(_.toimipiste).toList.map { case (organisaatio, suoritukset) =>
      KorkeakoulunOpiskeluoikeus(
        id = None,
        lähdejärjestelmänId = None,
        alkamispäivä = None,
        arvioituPäättymispäivä = None,
        päättymispäivä = None,
        oppilaitos = organisaatio,
        koulutustoimija = None,
        suoritukset = suoritukset,
        tila = None,
        läsnäolotiedot = None
      )
    }

    opiskeluoikeudet.filter(_.suoritukset.nonEmpty) ++ orphanages
  }

  private def lisääKeskeneräinenTutkintosuoritus(suoritukset: List[KorkeakouluSuoritus], opiskeluoikeusNode: Node) = {
    koulutuskoodi(opiskeluoikeusNode).map { koulutuskoodi =>
      val t = tutkinto(koulutuskoodi)
      if (suoritukset.exists(_.koulutusmoduuli == t)) suoritukset
      else KorkeakouluTutkinnonSuoritus(
        koulutusmoduuli = t,
        paikallinenId = None,
        arviointi = None,
        tila = requiredKoodi("suorituksentila", "KESKEN").get,
        vahvistus = None,
        suorituskieli = None,
        osasuoritukset = None,
        toimipiste = oppilaitos(opiskeluoikeusNode)
      ) :: suoritukset
    }.getOrElse(suoritukset)
  }

  private def convertSuoritus(suoritus: Node, allNodes: List[Node]): Option[KorkeakouluSuoritus] = {
    laji(suoritus) match {
      case "1" => // tutkinto
        koulutuskoodi(suoritus).map { koulutuskoodi =>
          val osasuoritukset = childNodes(suoritus, allNodes).map(convertOpintojaksonSuoritus(_, allNodes))

          KorkeakouluTutkinnonSuoritus(
            koulutusmoduuli = tutkinto(koulutuskoodi),
            paikallinenId = None,
            arviointi = arviointi(suoritus),
            tila = requiredKoodi("suorituksentila", "VALMIS").get,
            vahvistus = None,
            suorituskieli = None,
            toimipiste = oppilaitos(suoritus),
            osasuoritukset = optionalList(osasuoritukset)
          )
        }
      case "2" => // opintojakso
        Some(convertOpintojaksonSuoritus(suoritus, allNodes))
      case laji: String =>
        logger.warn("Tuntematon laji: " + laji)
        None
    }
  }

  def convertOpintojaksonSuoritus(suoritus: Node, allNodes: List[Node]): KorkeakoulunOpintojaksonSuoritus = {
    val osasuoritukset = childNodes(suoritus, allNodes).map(convertOpintojaksonSuoritus(_, allNodes))

    KorkeakoulunOpintojaksonSuoritus(
      koulutusmoduuli = KorkeakoulunOpintojakso(
        tunniste = PaikallinenKoodi((suoritus \\ "@koulutusmoduulitunniste").text, nimi(suoritus)),
        nimi = nimi(suoritus),
        laajuus = (suoritus \ "Laajuus" \ "Opintopiste").headOption.map(op => LaajuusOpintopisteissä(op.text.toFloat))
      ),
      paikallinenId = None,
      arviointi = arviointi(suoritus),
      tila = requiredKoodi("suorituksentila", "VALMIS").get,
      vahvistus = None,
      suorituskieli = (suoritus \\ "Kieli").headOption.flatMap(kieli => requiredKoodi("kieli", kieli.text.toUpperCase)),
      toimipiste = oppilaitos(suoritus),
      osasuoritukset = optionalList(osasuoritukset)
    )
  }

  private def arviointi(suoritus: Node) =
    koodistoViitePalvelu.getKoodistoKoodiViite("virtaarvosana", suoritus \ "Arvosana" \ "_" text).map( arvosana =>
      List(KorkeakoulunKoodistostaLöytyväArviointi(
        arvosana = arvosana,
        päivä = LocalDate.parse(suoritus \ "SuoritusPvm" text)
      ))
    ).orElse(paikallinenArviointi(suoritus)) // TODO, Mitä jos arvosanaa ei löydy koodistosta eikä ole paikallinen arvosana ?

  private def paikallinenArviointi(suoritus: Node): Option[List[KorkeakoulunArviointi]] = {
    val asteikkoUri = "virta/" + (suoritus \ "Arvosana" \ "Muu" \ "Asteikko" \ "@avain").text
    def nimi(a: Node) = (a \ "Nimi").headOption.getOrElse(a \ "Koodi").text

    (suoritus \ "Arvosana" \ "Muu" \\ "AsteikkoArvosana")
      .find(a => (a \ "@avain").text == (suoritus \ "Arvosana" \ "Muu" \ "Koodi").text)
      .map { a => List(
        KorkeakoulunPaikallinenArviointi(
          PaikallinenKoodi((a \ "Koodi").text, nimi(a), Some(asteikkoUri)),
          LocalDate.parse(suoritus \ "SuoritusPvm" text)
        ))
      }
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

  private def oppilaitos(node: Node): Oppilaitos = (node \ "Myontaja" headOption).flatMap(
    koodi => findOppilaitos(koodi.text)
  ).getOrElse(throw new RuntimeException("missing oppilaitos"))

  private def findOppilaitos(numero: String) = {
    oppilaitosRepository.findByOppilaitosnumero(numero).orElse(throw new RuntimeException("Oppilaitosta ei löydy: " + numero))
  }

}
