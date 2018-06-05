package fi.oph.koski.virta

import java.time.LocalDate
import java.time.LocalDate.{parse => date}

import fi.oph.koski.config.Environment
import fi.oph.koski.date.DateOrdering
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.{finnish, sanitize}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.log.Logging
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.oppilaitos.{MockOppilaitosRepository, OppilaitosRepository}
import fi.oph.koski.schema._
import fi.oph.koski.util.OptionalLists
import fi.oph.koski.util.OptionalLists.optionalList

import scala.xml.Node
case class VirtaXMLConverter(oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) extends Logging {

  def convertToOpiskeluoikeudet(virtaXml: Node): List[KorkeakoulunOpiskeluoikeus] = {
    import DateOrdering._

    val suoritusNodeList: List[Node] = suoritusNodes(virtaXml)
    val suoritusRoots: List[Node] = suoritusNodeList.filter(isRoot(suoritusNodeList)(_))
    val opiskeluoikeusNodes: List[Node] = (virtaXml \\ "Opiskeluoikeus").toList

    val (orphans, opiskeluoikeudet) = opiskeluoikeusNodes.foldLeft((suoritusRoots, Nil: List[KorkeakoulunOpiskeluoikeus])) { case ((suoritusRootsLeft, opiskeluOikeudet), opiskeluoikeusNode) =>
      val (opiskeluOikeudenSuoritukset: List[Node], muutSuoritukset: List[Node]) = suoritusRootsLeft.partition(sisältyyOpiskeluoikeuteen(_, opiskeluoikeusNode, suoritusNodeList))

      val opiskeluoikeudenTila: KorkeakoulunOpiskeluoikeudenTila = KorkeakoulunOpiskeluoikeudenTila((opiskeluoikeusNode \ "Tila")
        .sortBy(alkuPvm)
        .map(tila => KorkeakoulunOpiskeluoikeusjakso(alkuPvm(tila), requiredKoodi("virtaopiskeluoikeudentila", tila \ "Koodi" text)))
        .toList)

      val suoritukset: List[KorkeakouluSuoritus] = opiskeluOikeudenSuoritukset.flatMap(convertSuoritus(_, suoritusNodeList))

      val opiskeluoikeus = KorkeakoulunOpiskeluoikeus(
        lähdejärjestelmänId = Some(LähdejärjestelmäId(Some(opiskeluoikeusNode \ "@avain" text), requiredKoodi("lahdejarjestelma", "virta"))),
        arvioituPäättymispäivä = None,
        päättymispäivä = loppuPvm(opiskeluoikeusNode),
        oppilaitos = optionalOppilaitos(opiskeluoikeusNode),
        koulutustoimija = None,
        suoritukset = lisääKeskeneräinenTutkintosuoritus(suoritukset, opiskeluoikeusNode, opiskeluoikeudenTila),
        tila = opiskeluoikeudenTila,
        ensisijaisuus = (opiskeluoikeusNode \ "Ensisijaisuus").headOption.map { e => // TODO, should this be a list ?
          Ensisijaisuus(alkuPvm(e), loppuPvm(e))
        }
      )

      (muutSuoritukset, opiskeluoikeus :: opiskeluOikeudet)
    }

    val orphanSuoritukset = orphans.flatMap(convertSuoritus(_, suoritusNodeList))
    val orphanages = orphanSuoritukset.groupBy(_.toimipiste).toList.map { case (organisaatio, suoritukset) =>
      KorkeakoulunOpiskeluoikeus(
        lähdejärjestelmänId = Some(LähdejärjestelmäId(None, requiredKoodi("lahdejarjestelma", "virta"))),
        arvioituPäättymispäivä = None,
        päättymispäivä = None,
        oppilaitos = Some(organisaatio),
        koulutustoimija = None,
        suoritukset = suoritukset,
        tila = KorkeakoulunOpiskeluoikeudenTila(Nil),
        synteettinen = true
      )
    }

    // huom, tämä suodattaa pois myös tapaukset jossa oppilaitos = None (esim. ulkomaiset)
    opiskeluoikeudet.filter(_.suoritukset.nonEmpty) ++ orphanages
  }

  private def lisääKeskeneräinenTutkintosuoritus(suoritukset: List[KorkeakouluSuoritus], opiskeluoikeusNode: Node, tila: KorkeakoulunOpiskeluoikeudenTila) = {
    def vahvistusOpiskeluoikeudenTilausta(organisaatio: Organisaatio): Option[Päivämäärävahvistus] =
      tila.opiskeluoikeusjaksot.lastOption.filter(_.tila.koodiarvo == "3").map(jakso => Päivämäärävahvistus(jakso.alku, organisaatio))

    koulutuskoodi(opiskeluoikeusNode).map { koulutuskoodi =>
      val t = tutkinto(koulutuskoodi)
      if (suoritukset.exists(_.koulutusmoduuli == t)) {
        suoritukset
      } else {
        val toimipiste = oppilaitos(opiskeluoikeusNode)
        val (opintojaksot, muutSuoritukset) = suoritukset.partition {
          case _: KorkeakoulunOpintojaksonSuoritus => true
          case _ => false
        }
        KorkeakoulututkinnonSuoritus(
          koulutusmoduuli = t,
          arviointi = None,
          vahvistus = vahvistusOpiskeluoikeudenTilausta(toimipiste),
          suorituskieli = None,
          osasuoritukset = Some(opintojaksot collect { case s: KorkeakoulunOpintojaksonSuoritus => s }),
          toimipiste = toimipiste
        ) :: muutSuoritukset
      }
    }.orElse {
      optionalOppilaitos(opiskeluoikeusNode).flatMap(oppilaitos => {
        koodistoViitePalvelu.validate("virtaopiskeluoikeudentyyppi", (opiskeluoikeusNode \ "Tyyppi").text)
          .map(virtaOpiskeluoikeudenTyyppi => {
            val nimi = Some((opiskeluoikeusNode \\ "@koulutusmoduulitunniste").text.stripPrefix("#").stripSuffix("/").trim)
              .filter(_.nonEmpty).map(finnish).getOrElse(virtaOpiskeluoikeudenTyyppi.description)
            MuuKorkeakoulunSuoritus(
              koulutusmoduuli = MuuKorkeakoulunOpinto(
                tunniste = virtaOpiskeluoikeudenTyyppi,
                nimi = nimi,
                laajuus = laajuus(opiskeluoikeusNode)
              ),
              vahvistus = vahvistusOpiskeluoikeudenTilausta(oppilaitos),
              suorituskieli = None,
              osasuoritukset = None,
              toimipiste = oppilaitos
            ) :: suoritukset
          })
      })
    }.getOrElse(suoritukset)
  }

  def convertSuoritus(suoritus: Node, allNodes: List[Node]): Option[KorkeakouluSuoritus] = {
    laji(suoritus) match {
      case "1" => // tutkinto
        koulutuskoodi(suoritus).map { koulutuskoodi =>
          val osasuoritukset = childNodes(suoritus, allNodes).map(convertOpintojaksonSuoritus(_, allNodes))

          KorkeakoulututkinnonSuoritus(
            koulutusmoduuli = tutkinto(koulutuskoodi),
            arviointi = arviointi(suoritus),
            vahvistus = vahvistus(suoritus),
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

  private def convertOpintojaksonSuoritus(suoritus: Node, allNodes: List[Node]): KorkeakoulunOpintojaksonSuoritus = {
    val osasuoritukset = childNodes(suoritus, allNodes).map(convertOpintojaksonSuoritus(_, allNodes))

    KorkeakoulunOpintojaksonSuoritus(
      koulutusmoduuli = KorkeakoulunOpintojakso(
        tunniste = PaikallinenKoodi((suoritus \\ "@koulutusmoduulitunniste").text, nimi(suoritus)),
        nimi = nimi(suoritus),
        laajuus = laajuus(suoritus)
      ),
      arviointi = arviointi(suoritus),
      vahvistus = vahvistus(suoritus),
      suorituskieli = (suoritus \\ "Kieli").headOption.flatMap(kieli => koodistoViitePalvelu.validate(Koodistokoodiviite(kieli.text.toUpperCase, "kieli"))),
      toimipiste = oppilaitos(suoritus),
      osasuoritukset = optionalList(osasuoritukset)
    )
  }

  private def laajuus(suoritusOrOpiskeluoikeus: Node): Option[LaajuusOpintopisteissä] = for {
    yksikko <- koodistoViitePalvelu.validate("opintojenlaajuusyksikko", "2")
    laajuus <- (suoritusOrOpiskeluoikeus \ "Laajuus" \ "Opintopiste").headOption.map(_.text.toFloat).filter(_ > 0)
  } yield LaajuusOpintopisteissä(laajuus, yksikko)

  private def arviointi(suoritus: Node): Option[List[KorkeakoulunArviointi]] = {
    if ((suoritus \ "Arvosana" \ "Muu").length > 0) {
      paikallinenArviointi(suoritus)
    } else {
      koodistoViitePalvelu.validate("virtaarvosana", suoritus \ "Arvosana" \ "_" text).map(arvosana =>
        List(KorkeakoulunKoodistostaLöytyväArviointi(
          arvosana = arvosana,
          päivä = LocalDate.parse(suoritus \ "SuoritusPvm" text)
        ))
      )
    }
  }

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

  private def vahvistus(suoritus: Node): Option[Päivämäärävahvistus] = {
    arviointi(suoritus).flatMap(_.lastOption.flatMap(arviointi =>
      Some(Päivämäärävahvistus(arviointi.päivä, oppilaitos(suoritus)))
    ))
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

  private def requiredKoodi(uri: String, koodi: String) = {
    koodistoViitePalvelu.validateRequired(uri, koodi)
  }

  private def tutkinto(koulutuskoodi: String): Korkeakoulututkinto = {
    Korkeakoulututkinto(requiredKoodi("koulutus", koulutuskoodi))
  }

  private def loppuPvm(n: Node): Option[LocalDate] = {
    (n \ "LoppuPvm").headOption.flatMap(l => optionalDate(l.text))
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

  private def optionalDate(str: String): Option[LocalDate] = {
    if (str == "2112-12-21") {
      None
    } else {
      Some(date(str))
    }
  }

  private def koulutuskoodi(node: Node): Option[String] = {
    (node \\ "Koulutuskoodi").headOption.map(_.text)
  }

  private def nimi(suoritus: Node): LocalizedString = {
    sanitize((suoritus \\ "Nimi" map (nimi => (nimi \ "@kieli" text, nimi text))).toMap).getOrElse(finnish("Suoritus: " + avain(suoritus)))
  }

  // huom, tässä kentässä voi olla oppilaitosnumeron lisäksi muitakin arvoja, esim. "UK" = "Ulkomainen korkeakoulu"
  // https://confluence.csc.fi/display/VIRTA/Tietovarannon+koodistot#Tietovarannonkoodistot-Organisaatio
  private def oppilaitosnumero(node: Node): Option[String] = (node \ "Myontaja" headOption).map(_.text)

  private def oppilaitos(node: Node): Oppilaitos = {
    val numero = oppilaitosnumero(node)
    numero.flatMap(oppilaitosRepository.findByOppilaitosnumero)
      .orElse(possiblyMockOppilaitos)
      .getOrElse(throw new RuntimeException(s"Oppilaitosta ei löydy: $numero"))
  }

  private def optionalOppilaitos(node: Node): Option[Oppilaitos] = {
    val numero = oppilaitosnumero(node)
    numero.flatMap(oppilaitosRepository.findByOppilaitosnumero)
      .orElse(possiblyMockOppilaitos)
      .orElse({
        logger.warn(s"Oppilaitosta ei löydy: $numero")
        None
      })
  }

  // Jos ajetaan paikallista Koskea Virta-testiympäristön kanssa, useimpia oppilaitoksia ei löydy
  // MockOppilaitosRepositorystä. Käytetään Aalto-yliopistoa, jotta pystytään näyttämään edes jotain.
  private def possiblyMockOppilaitos: Option[Oppilaitos] = {
    if (Environment.isLocalDevelopmentEnvironment && oppilaitosRepository == MockOppilaitosRepository) {
      oppilaitosRepository.findByOppilaitosnumero("10076")
    } else {
      None
    }
  }
}
