package fi.oph.koski.virta

import java.time.LocalDate
import java.time.LocalDate.{parse => date}

import fi.oph.koski.config.Environment
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.{MockOppilaitosRepository, OppilaitosRepository}
import fi.oph.koski.schema.LocalizedString.{finnish, sanitize}
import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering
import fi.oph.koski.util.OptionalLists.optionalList
import fi.oph.koski.virta.VirtaXMLConverterUtils._

import scala.xml.Node
case class VirtaXMLConverter(oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) extends Logging {

  def convertToOpiskeluoikeudet(virtaXml: Node): List[KorkeakoulunOpiskeluoikeus] = {
    import fi.oph.koski.util.DateOrdering._

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

      val oppilaitos: Option[Oppilaitos] = optionalOppilaitos(opiskeluoikeusNode)
      val opiskeluoikeus = KorkeakoulunOpiskeluoikeus(
        lähdejärjestelmänId = Some(LähdejärjestelmäId(Some(opiskeluoikeusNode \ "@avain" text), requiredKoodi("lahdejarjestelma", "virta"))),
        arvioituPäättymispäivä = None,
        päättymispäivä = loppuPvm(opiskeluoikeusNode),
        oppilaitos = oppilaitos,
        koulutustoimija = None,
        suoritukset = addPäätasonSuoritusIfNecessary(suoritukset, opiskeluoikeusNode, opiskeluoikeudenTila),
        tila = opiskeluoikeudenTila,
        lisätiedot = Some(KorkeakoulunOpiskeluoikeudenLisätiedot(
          ensisijaisuus = Some((opiskeluoikeusNode \ "Ensisijaisuus").toList.map { e => Aikajakso(alkuPvm(e), loppuPvm(e)) }).filter(_.nonEmpty),
          virtaOpiskeluoikeudenTyyppi = Some(opiskeluoikeudenTyyppi(opiskeluoikeusNode)),
          lukukausiIlmoittautuminen = lukukausiIlmoittautuminen(oppilaitos, opiskeluoikeudenTila, avain(opiskeluoikeusNode), virtaXml)
        ))
      )

      (muutSuoritukset.filterNot(sisältyyOpiskeluoikeuteen(_, opiskeluoikeusNode, suoritusNodeList, ignoreDuplicates = true)), opiskeluoikeus :: opiskeluOikeudet)
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

  private def addPäätasonSuoritusIfNecessary(suoritukset: List[KorkeakouluSuoritus], opiskeluoikeusNode: Node, tila: KorkeakoulunOpiskeluoikeudenTila) = {
    if (tutkintoonJohtava(opiskeluoikeusNode)) {
      addTutkintoonJohtavaPäätasonSuoritusIfNecessery(suoritukset, opiskeluoikeusNode, tila)
    } else {
      addMuuKorkeakoulunSuoritus(tila, suoritukset, opiskeluoikeusNode)
    }
  }

  private def addTutkintoonJohtavaPäätasonSuoritusIfNecessery(suoritukset: List[KorkeakouluSuoritus], opiskeluoikeusNode: Node, tila: KorkeakoulunOpiskeluoikeudenTila) = {
    val opiskeluoikeusJaksot = koulutuskoodillisetJaksot(opiskeluoikeusNode)
    val suoritusLöytyyKoulutuskoodilla = opiskeluoikeusJaksot.exists { jakso =>
      val opiskeluoikeudenTutkinto = tutkinto(jakso.koulutuskoodi)
      suoritukset.exists(_.koulutusmoduuli == opiskeluoikeudenTutkinto)
    }

    if (suoritusLöytyyKoulutuskoodilla) { // suoritus on valmis
      suoritukset
    } else if (opiskeluoikeusJaksot.nonEmpty && !päättynyt(tila)) {
      val viimeisinTutkinto = tutkinto(opiskeluoikeusJaksot.maxBy(_.alku)(DateOrdering.localDateOrdering).koulutuskoodi)
      addKeskeneräinenTutkinnonSuoritus(tila, suoritukset, opiskeluoikeusNode, viimeisinTutkinto)
    } else {
      val opiskeluoikeusTila = tila.opiskeluoikeusjaksot.lastOption.map(_.tila)
      logger.warn(s"Tutkintoon johtavaa päätason suoritusta ei löydy tai opiskeluoikeus on päättynyt. Opiskeluoikeus ${avain(opiskeluoikeusNode)}, tila: $opiskeluoikeusTila, jaksot: ${opiskeluoikeusJaksot.map(_.koulutuskoodi)}, laji: '${laji(opiskeluoikeusNode)}'" )
      addMuuKorkeakoulunSuoritus(tila, suoritukset, opiskeluoikeusNode)
    }
  }

  private def addKeskeneräinenTutkinnonSuoritus(tila: KorkeakoulunOpiskeluoikeudenTila, suoritukset: List[KorkeakouluSuoritus], opiskeluoikeusNode: Node, tutkinto: Korkeakoulututkinto): List[KorkeakouluSuoritus] = {
    val toimipiste = oppilaitos(opiskeluoikeusNode)
    val (opintojaksot, muutSuoritukset) = suoritukset.partition(_.isInstanceOf[KorkeakoulunOpintojaksonSuoritus])
    KorkeakoulututkinnonSuoritus(
      koulutusmoduuli = tutkinto,
      arviointi = None,
      vahvistus = None,
      suorituskieli = None,
      osasuoritukset = Some(opintojaksot collect { case s: KorkeakoulunOpintojaksonSuoritus => s }),
      toimipiste = toimipiste
    ) :: muutSuoritukset
  }

  private def addMuuKorkeakoulunSuoritus(tila: KorkeakoulunOpiskeluoikeudenTila, suoritukset: List[KorkeakouluSuoritus], opiskeluoikeusNode: Node) =
    optionalOppilaitos(opiskeluoikeusNode).map { org =>
      val virtaOpiskeluoikeudenTyyppi = opiskeluoikeudenTyyppi(opiskeluoikeusNode)
      val nimi = Some((opiskeluoikeusNode \\ "@koulutusmoduulitunniste").text.stripPrefix("#").stripSuffix("/").trim)
        .filter(_.nonEmpty).map(finnish).getOrElse(virtaOpiskeluoikeudenTyyppi.description)
      MuuKorkeakoulunSuoritus(
        koulutusmoduuli = MuuKorkeakoulunOpinto(
          tunniste = virtaOpiskeluoikeudenTyyppi,
          nimi = nimi,
          laajuus = laajuus(opiskeluoikeusNode)
        ),
        vahvistus = vahvistusOpiskeluoikeudenTilasta(tila, org),
        suorituskieli = None,
        osasuoritukset = None,
        toimipiste = org
      )
    }.toList ++ suoritukset

  private def vahvistusOpiskeluoikeudenTilasta(tila: KorkeakoulunOpiskeluoikeudenTila, organisaatio: Organisaatio): Option[Päivämäärävahvistus] =
    tila.opiskeluoikeusjaksot.lastOption.filter(_.tila.koodiarvo == "3").map(jakso => Päivämäärävahvistus(jakso.alku, organisaatio))

  def convertSuoritus(suoritus: Node, allNodes: List[Node]): Option[KorkeakouluSuoritus] = {
    laji(suoritus) match {
      case "1" => // tutkinto
        val tutkinnonSuoritus = koulutuskoodi(suoritus).map { koulutuskoodi =>
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
        if (tutkinnonSuoritus.isEmpty) {
          logger.warn(s"Tutkinnon suoritukselta puuttuu koulutuskoodi $suoritus")
        }
        tutkinnonSuoritus
      case "2" => // opintojakso
        Some(convertOpintojaksonSuoritus(suoritus, allNodes))
      case laji: String =>
        logger.warn("Tuntematon laji: " + laji)
        None
    }
  }

  private val tutkintoonJohtavienTyyppienKoodiarvot = List("1","2","3","4","6","7")
  private def tutkintoonJohtava(opiskeluoikeus: Node) = {
    val ooTyyppi = opiskeluoikeudenTyyppi(opiskeluoikeus).koodiarvo
    tutkintoonJohtavienTyyppienKoodiarvot.contains(ooTyyppi)
  }

  private def päättynyt(tila: KorkeakoulunOpiskeluoikeudenTila) =
    tila.opiskeluoikeusjaksot.lastOption.exists(_.tila.koodiarvo == "3")

  private def lukukausiIlmoittautuminen(oppilaitos: Option[Oppilaitos], tila: KorkeakoulunOpiskeluoikeudenTila, opiskeluoikeusAvain: String, virtaXml: Node): Option[Lukukausi_Ilmoittautuminen] = {
    val ilmo = Ilmoittautuminen(oppilaitos, tila, opiskeluoikeusAvain, virtaXml)
    val ilmot = (virtaXml \\ "LukukausiIlmoittautuminen").toList
      .filter(ilmo.kuuluuOpiskeluoikeuteen)
      .map(lukukausiIlmo)
      .sortBy(_.alku)(DateOrdering.localDateOrdering)

    optionalList(ilmot).map(Lukukausi_Ilmoittautuminen)
  }

  private def lukukausiIlmo(n: Node) = Lukukausi_Ilmoittautumisjakso(
    alku = alkuPvm(n),
    loppu = loppuPvm(n),
    tila = koodistoViitePalvelu.validate(Koodistokoodiviite((n \ "Tila").text, "virtalukukausiilmtila")).getOrElse(lukukausiIlmottautuminenPuuttuu),
    ylioppilaskunnanJäsen = (n \ "YlioppilaskuntaJasen").headOption.map(toBoolean),
    ythsMaksettu = (n \ "YTHSMaksu").headOption.map(toBoolean)
  )

  private val virtaTruths = List("1", "true")
  private def toBoolean(n: Node) = virtaTruths.contains(n.text.toLowerCase)
  private lazy val lukukausiIlmottautuminenPuuttuu = koodistoViitePalvelu.validateRequired(Koodistokoodiviite("4", "virtalukukausiilmtila"))

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
      koodistoViitePalvelu.validate("virtaarvosana", (suoritus \ "Arvosana" \ "_").text).map(arvosana =>
        List(KorkeakoulunKoodistostaLöytyväArviointi(
          arvosana = arvosana,
          päivä = LocalDate.parse((suoritus \ "SuoritusPvm").text)
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
          PaikallinenKoodi((a \ "Koodi").text, LocalizedString.finnish(nimi(a)), Some(asteikkoUri)),
          LocalDate.parse((suoritus \ "SuoritusPvm").text)
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

  private val fuusioitunutMyöntäjä = "5"
  private def childNodes(node: Node, allNodes: List[Node]) = {
    sisaltyvatAvaimet(node).flatMap { opintosuoritusAvain =>
      val osasuoritusNodes = allNodes.filter(avain(_) == opintosuoritusAvain)
      if (osasuoritusNodes.isEmpty) {
        throw new IllegalArgumentException("Opintosuoritusta " + opintosuoritusAvain + " ei löydy dokumentista")
      } else if (osasuoritusNodes.length == 1) {
        List(osasuoritusNodes.head)
      } else {
        val fuusioKoulunSuoritus = osasuoritusNodes.find(n => (n \ "Organisaatio" \ "Rooli").headOption.exists(_.text == fuusioitunutMyöntäjä))
        if (fuusioKoulunSuoritus.isDefined) {
          fuusioKoulunSuoritus.toList
        } else {
          throw new IllegalArgumentException("Enemmän kuin yksi suoritus avaimella " + opintosuoritusAvain)
        }
      }
    }
  }

  private def suoritusNodes(virtaXml: Node) = {
    (virtaXml \\ "Opintosuoritukset" \\ "Opintosuoritus").toList
  }

  def sisältyyOpiskeluoikeuteen(suoritus: Node, opiskeluoikeus: Node, allNodes: List[Node], ignoreDuplicates: Boolean = false): Boolean = {
    val opiskeluoikeusAvain: String = (suoritus \ "@opiskeluoikeusAvain").text
    if (opiskeluoikeusAvain == avain(opiskeluoikeus)) {
      val suoritusAvain = avain(suoritus)
      ignoreDuplicates ||
      allNodes.count(avain(_) == suoritusAvain) == 1 ||
      // duplikaatteja löytyi -> tulkitse vain fuusioituneen koulun suoritus sisältyväksi
      suoritus.exists(n => (n \ "Organisaatio" \ "Rooli").headOption.exists(_.text == fuusioitunutMyöntäjä))
    } else {
      childNodes(suoritus, allNodes).exists(sisältyyOpiskeluoikeuteen(_, opiskeluoikeus, allNodes, ignoreDuplicates))
    }
  }

  private def requiredKoodi(uri: String, koodi: String) = {
    koodistoViitePalvelu.validateRequired(uri, koodi)
  }

  private def opiskeluoikeudenTyyppi(opiskeluoikeus: Node): Koodistokoodiviite = {
    requiredKoodi("virtaopiskeluoikeudentyyppi", (opiskeluoikeus \ "Tyyppi").text)
  }

  private def tutkinto(koulutuskoodi: String): Korkeakoulututkinto = {
    Korkeakoulututkinto(requiredKoodi("koulutus", koulutuskoodi))
  }

  private def nimi(suoritus: Node): LocalizedString = {
    sanitize((suoritus \\ "Nimi" map (nimi => ((nimi \ "@kieli").text, nimi.text))).toMap).getOrElse(finnish("Suoritus: " + avain(suoritus)))
  }

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

case class Ilmoittautuminen(oppilaitos: Option[Oppilaitos], tila: KorkeakoulunOpiskeluoikeudenTila, ooAvain: String, virtaXml: Node) {
  private lazy val jaksot = tila.opiskeluoikeusjaksot.map(Some.apply)
  private lazy val kaikkiJaksot = jaksot.zipAll(jaksot.drop(1), None, None)
  private lazy val aktiivisetJaksot = kaikkiJaksot.collect {
    case (Some(a), b) if a.tila.koodiarvo == "1" => LoppupäivällinenOpiskeluoikeusJakso(a.alku, b.map(_.alku))
  }

  def kuuluuOpiskeluoikeuteen(n: Node): Boolean = {
    val jaksonOpiskeluoikeusAvain = opiskeluoikeusAvain(n)
    if (jaksonOpiskeluoikeusAvain.nonEmpty) {
      ooAvain == jaksonOpiskeluoikeusAvain
    } else {
      oppilaitosnumero(n).exists(myöntäjä => kuuluuOpiskeluoikeuteen(LoppupäivällinenOpiskeluoikeusJakso(alkuPvm(n), loppuPvm(n)), myöntäjä))
    }
  }

  private def kuuluuOpiskeluoikeuteen(ilmoittautuminen: Jakso, myöntäjä: String) = {
    val oppilaitosNumero = oppilaitos.flatMap(_.oppilaitosnumero.map(_.koodiarvo))
    oppilaitosNumero.contains(myöntäjä) && aktiivisetJaksot.exists(_.overlaps(ilmoittautuminen))
  }

  private def opiskeluoikeusAvain(node: Node) = (node \ "@opiskeluoikeusAvain").text
}

case class LoppupäivällinenOpiskeluoikeusJakso(
  alku: LocalDate,
  loppu: Option[LocalDate]
) extends Jakso

case class KoulutuskoodillinenOpiskeluoikeusJakso(
  alku: LocalDate,
  koulutuskoodi: String
)

object VirtaXMLConverterUtils {
  def loppuPvm(n: Node): Option[LocalDate] = {
    (n \ "LoppuPvm").headOption.flatMap(l => optionalDate(l.text))
  }

  def avain(node: Node) = {
    (node \ "@avain").text
  }

  def alkuPvm(node: Node) = {
    date((node \ "AlkuPvm").text)
  }

  def myöntäjä(node: Node) = {
    (node \ "Myontaja" \ "Koodi").text
  }

  def laji(node: Node) = {
    (node \ "Laji").text
  }

  def optionalDate(str: String): Option[LocalDate] = {
    if (str == "2112-12-21") {
      None
    } else {
      Some(date(str))
    }
  }

  def koulutuskoodi(node: Node): Option[String] =
    (node \\ "Koulutuskoodi").headOption.map(_.text)

  def koulutuskoodillisetJaksot(node: Node): Seq[KoulutuskoodillinenOpiskeluoikeusJakso] =
    (node \\ "Jakso").flatMap { jakso =>
      (jakso \ "Koulutuskoodi").headOption.map { koulutus =>
        KoulutuskoodillinenOpiskeluoikeusJakso(
          alku = alkuPvm(jakso),
          koulutuskoodi = koulutus.text
        )
      }
    }

  // huom, tässä kentässä voi olla oppilaitosnumeron lisäksi muitakin arvoja, esim. "UK" = "Ulkomainen korkeakoulu"
  // https://confluence.csc.fi/display/VIRTA/Tietovarannon+koodistot#Tietovarannonkoodistot-Organisaatio
  def oppilaitosnumero(node: Node): Option[String] = (node \ "Myontaja").headOption.map(_.text)
}
