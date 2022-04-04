package fi.oph.koski.virta

import fi.oph.koski.documentation.ExampleData.laajuusOpintoviikoissa

import java.time.LocalDate
import java.time.LocalDate.{parse => date}
import scala.util.Try
import scala.xml.Node
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.LocalizedString.{finnish, sanitize}
import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering
import fi.oph.koski.util.OptionalLists.optionalList
import fi.oph.koski.virta.VirtaXMLConverterUtils._

import scala.collection.mutable.{HashMap, HashSet, ListBuffer}

case class VirtaXMLConverter(oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository) extends Logging {
  var virheet: ListBuffer[VirtaVirhe] = ListBuffer[VirtaVirhe]()

  def convertToOpiskeluoikeudet(virtaXml: Node): List[KorkeakoulunOpiskeluoikeus] = {
    import fi.oph.koski.util.DateOrdering._

    val suoritusNodeList: List[Node] = filterLABDuplikaatit(suoritusNodes(virtaXml))

    val suoritusRoots: List[Node] = suoritusNodeList.filter(isRoot(suoritusNodeList)(_))
    val opiskeluoikeusNodes: List[Node] = (virtaXml \\ "Opiskeluoikeus").toList
    val ooTyyppi: Koodistokoodiviite = koodistoViitePalvelu.validateRequired(OpiskeluoikeudenTyyppi.korkeakoulutus)

    val (orphans, opiskeluoikeudet) = opiskeluoikeusNodes.foldLeft((suoritusRoots, Nil: List[KorkeakoulunOpiskeluoikeus])) { case ((suoritusRootsLeft, opiskeluOikeudet), opiskeluoikeusNode) =>
      virheet = ListBuffer[VirtaVirhe]()
      val (opiskeluOikeudenSuoritukset: List[Node], muutSuoritukset: List[Node]) = suoritusRootsLeft.partition(sisältyyOpiskeluoikeuteen(_, opiskeluoikeusNode, suoritusNodeList, None))

      val opiskeluoikeudenTila: KorkeakoulunOpiskeluoikeudenTila = KorkeakoulunOpiskeluoikeudenTila((opiskeluoikeusNode \ "Tila")
        .sortBy(alkuPvm)
        .map(tila => KorkeakoulunOpiskeluoikeusjakso(alkuPvm(tila), jaksonNimi(opiskeluoikeusNode), requiredKoodi("virtaopiskeluoikeudentila", tila \ "Koodi" text)))
        .toList)

      val lukuvuosimaksut: Seq[KorkeakoulunOpiskeluoikeudenLukuvuosimaksu] = (opiskeluoikeusNode \ "LukuvuosiMaksu").map(lukuvuosiMaksuTiedot)

      val suoritukset: List[KorkeakouluSuoritus] = opiskeluOikeudenSuoritukset.flatMap(convertSuoritus(Some(opiskeluoikeusNode), _, suoritusNodeList))

      val vahvistus = suoritukset.flatMap(_.vahvistus).sortBy(_.päivä)(localDateOrdering).lastOption
      val oppilaitoksenNimiPäivä = getOppilaitoksenNimiPäivä(
        opiskeluoikeudenTila.opiskeluoikeusjaksot.lastOption,
        vahvistus.map(_.päivä)
      )
      val oppilaitos: Option[Oppilaitos] = optionalOppilaitos(
        opiskeluoikeusNode,
        oppilaitoksenNimiPäivä
      )
      val opiskeluoikeus = KorkeakoulunOpiskeluoikeus(
        lähdejärjestelmänId = Some(LähdejärjestelmäId(Some(opiskeluoikeusNode \ "@avain" text), requiredKoodi("lahdejarjestelma", "virta"))),
        arvioituPäättymispäivä = None,
        päättymispäivä = loppuPvm(opiskeluoikeusNode),
        oppilaitos = oppilaitos,
        koulutustoimija = None,
        suoritukset = rearrangeSuorituksetIfNecessary(suoritukset, opiskeluoikeusNode, opiskeluoikeudenTila),
        tila = opiskeluoikeudenTila,
        tyyppi = ooTyyppi,
        lisätiedot = Some(KorkeakoulunOpiskeluoikeudenLisätiedot(
          ensisijaisuus = Some((opiskeluoikeusNode \ "Ensisijaisuus").toList.map { e => Aikajakso(alkuPvm(e), loppuPvm(e)) }).filter(_.nonEmpty),
          virtaOpiskeluoikeudenTyyppi = Some(opiskeluoikeudenTyyppi(opiskeluoikeusNode)),
          lukukausiIlmoittautuminen = lukukausiIlmoittautuminen(oppilaitos, opiskeluoikeudenTila, avain(opiskeluoikeusNode), virtaXml),
          järjestäväOrganisaatio = järjestäväOrganisaatio(opiskeluoikeusNode, oppilaitoksenNimiPäivä),
          maksettavatLukuvuosimaksut = Some(lukuvuosimaksut)
        )),
        virtaVirheet = virheet.toList
      )

      (muutSuoritukset, opiskeluoikeus :: opiskeluOikeudet)
    }

    virheet = ListBuffer[VirtaVirhe]()
    val orphanSuoritukset = orphans.flatMap(convertSuoritus(None, _, suoritusNodeList))
    val orphanages = orphanSuoritukset.groupBy(_.toimipiste).toList.map { case (organisaatio, suoritukset) =>
      KorkeakoulunOpiskeluoikeus(
        lähdejärjestelmänId = Some(LähdejärjestelmäId(None, requiredKoodi("lahdejarjestelma", "virta"))),
        arvioituPäättymispäivä = None,
        päättymispäivä = None,
        oppilaitos = Some(organisaatio),
        koulutustoimija = None,
        suoritukset = suoritukset,
        tila = KorkeakoulunOpiskeluoikeudenTila(Nil),
        tyyppi = ooTyyppi,
        virtaVirheet = virheet.toList,
        synteettinen = true
      )
    }

    // huom, tämä suodattaa pois myös tapaukset jossa oppilaitos = None (esim. ulkomaiset)
    opiskeluoikeudet.filter(_.suoritukset.nonEmpty) ++ orphanages
  }

  private val LABAmmattikorkeaNumero = "10126"
  private val vanhaLahdenAmmattiKorkeaNumero = "02470"
  private def filterLABDuplikaatit(osasuoritusNodes: List[Node]): List[Node] = {
    lazy val avaimet = osasuoritusNodes.filter(o => (o \ "Myontaja").text != LABAmmattikorkeaNumero).map(o => (o \ "@avain").text)
    osasuoritusNodes.filter(o => {
      if ((o \ "Myontaja").text == LABAmmattikorkeaNumero) {
        if (avaimet.contains((o \ "@avain").text)) {
          false
        } else { true }
      } else { true }
    })
  }

  private def rearrangeSuorituksetIfNecessary(suoritukset: List[KorkeakouluSuoritus], opiskeluoikeusNode: Node, tila: KorkeakoulunOpiskeluoikeudenTila) = {
    if (tutkintoonJohtava(opiskeluoikeusNode)) {
      fixPäätasonSuoritusIfNecessary(suoritukset, opiskeluoikeusNode, tila)
    } else {
      addMuuKorkeakoulunSuoritus(tila, suoritukset, opiskeluoikeusNode, None)
    }
  }

  private def fixPäätasonSuoritusIfNecessary(suoritukset: List[KorkeakouluSuoritus], opiskeluoikeusNode: Node, tila: KorkeakoulunOpiskeluoikeudenTila) = {
    val opiskeluoikeusJaksot = koulutuskoodillisetJaksot(opiskeluoikeusNode)
    val suoritusLöytyyKoulutuskoodilla = opiskeluoikeusJaksot.exists { jakso =>
      val opiskeluoikeudenTutkinto = tutkinto(jakso.koulutuskoodi)
      suoritukset.exists(_.koulutusmoduuli.tunniste == opiskeluoikeudenTutkinto.tunniste)
    }
    def viimeisinTutkinto = tutkinto(opiskeluoikeusJaksot.maxBy(_.alku)(DateOrdering.localDateOrdering).koulutuskoodi, jaksonNimi(opiskeluoikeusNode))

    if (suoritusLöytyyKoulutuskoodilla) { // suoritus löytyy virta datasta vain jos se on valmis
      moveOpintojaksotUnderPäätasonSuoritusIfNecessary(suoritukset)
    } else if (opiskeluoikeusJaksot.nonEmpty && !päättynyt(tila)) {
      val viimeisinTutkinto = tutkinto(opiskeluoikeusJaksot.maxBy(_.alku)(DateOrdering.localDateOrdering).koulutuskoodi, jaksonNimi(opiskeluoikeusNode))
      addKeskeneräinenTutkinnonSuoritus(tila, suoritukset, opiskeluoikeusNode, viimeisinTutkinto)
    } else if (opiskeluoikeusJaksot.nonEmpty) {
      val opiskeluoikeusTila = tila.opiskeluoikeusjaksot.lastOption.map(_.tila)
      logger.info(s"Opiskeluoikeus on päättynyt. Opiskeluoikeus(avain: ${avain(opiskeluoikeusNode)}, tila: $opiskeluoikeusTila, jaksot: ${opiskeluoikeusJaksot.map(_.koulutuskoodi)}, laji: '${laji(opiskeluoikeusNode)}')")
      val viimeisinTutkinto = tutkinto(opiskeluoikeusJaksot.maxBy(_.alku)(DateOrdering.localDateOrdering).koulutuskoodi, jaksonNimi(opiskeluoikeusNode))
      addMuuKorkeakoulunSuoritus(tila, suoritukset, opiskeluoikeusNode, Some(viimeisinTutkinto))
    } else {
      val opiskeluoikeusTila = tila.opiskeluoikeusjaksot.lastOption.map(_.tila)
      logger.info(s"Tutkintoon johtavaa päätason suoritusta ei löydy tai opiskeluoikeus on päättynyt. Opiskeluoikeus(avain: ${avain(opiskeluoikeusNode)}, tila: $opiskeluoikeusTila, jaksot: ${opiskeluoikeusJaksot.map(_.koulutuskoodi)}, laji: '${laji(opiskeluoikeusNode)}')")
      addMuuKorkeakoulunSuoritus(tila, suoritukset, opiskeluoikeusNode, None)
    }
  }

  private def moveOpintojaksotUnderPäätasonSuoritusIfNecessary(suoritukset: List[KorkeakouluSuoritus]) = {
    val tutkinnot = suoritukset.collect { case t: KorkeakoulututkinnonSuoritus => t }
    val opintojaksot = suoritukset.collect { case oj: KorkeakoulunOpintojaksonSuoritus => oj }
    if (tutkinnot.size == 1 && tutkinnot.head.osasuoritukset.isEmpty && opintojaksot.nonEmpty) {
      List(tutkinnot.head.copy(osasuoritukset = Some(opintojaksot)))
    } else {
      suoritukset
    }
  }

  private def addKeskeneräinenTutkinnonSuoritus(tila: KorkeakoulunOpiskeluoikeudenTila, suoritukset: List[KorkeakouluSuoritus], opiskeluoikeusNode: Node, tutkinto: Korkeakoulututkinto): List[KorkeakouluSuoritus] = {
    val toimipiste = oppilaitos(opiskeluoikeusNode, None)
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

  private def sisällytäOpintojaksotOsasuorituksina(virtaOpiskeluoikeudenTyyppi: Koodistokoodiviite): Boolean = {
    Seq(
      "8", // Kotimainen opiskelijaliikkuvuus
      "13" // Avoimen opinnot
    ).contains(virtaOpiskeluoikeudenTyyppi.koodiarvo)
  }

  private def addMuuKorkeakoulunSuoritus(
    tila: KorkeakoulunOpiskeluoikeudenTila,
    suoritukset: List[KorkeakouluSuoritus],
    opiskeluoikeusNode: Node,
    viimeisinTutkinto: Option[Korkeakoulututkinto]
  ): List[KorkeakouluSuoritus] = {
    val virtaOpiskeluoikeudenTyyppi = opiskeluoikeudenTyyppi(opiskeluoikeusNode)

    val (päätasonSuoritukset, osasuoritukset) = if (sisällytäOpintojaksotOsasuorituksina(virtaOpiskeluoikeudenTyyppi)) {
      val (opintojaksot, muut) = suoritukset.foldRight((List.empty[KorkeakoulunOpintojaksonSuoritus], List.empty[KorkeakouluSuoritus])) {
        case (jakso: KorkeakoulunOpintojaksonSuoritus, (jaksot, muut)) => (jakso :: jaksot, muut)
        case (muu, (jaksot, muut)) => (jaksot, muu :: muut)
      }
      (muut, if (opintojaksot.isEmpty) None else Some(opintojaksot))
    } else {
      (suoritukset, None)
    }

    val vahvistusPäivä = tila.opiskeluoikeusjaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(_.alku)
    val oppilaitoksenNimiPäivä = getOppilaitoksenNimiPäivä(
      tila.opiskeluoikeusjaksot.lastOption,
      suoritukset.flatMap(_.vahvistus).sortBy(_.päivä)(DateOrdering.localDateOrdering).lastOption.map(_.päivä)
    )

    val muuKorkeakoulunSuoritus = optionalOppilaitos(opiskeluoikeusNode, oppilaitoksenNimiPäivä).map { org =>
      val nimi = Some((opiskeluoikeusNode \\ "@koulutusmoduulitunniste").text.stripPrefix("#").stripSuffix("/").trim)
        .filter(_.nonEmpty).map(finnish).getOrElse(virtaOpiskeluoikeudenTyyppi.description)
      MuuKorkeakoulunSuoritus(
        koulutusmoduuli = MuuKorkeakoulunOpinto(
          tunniste = virtaOpiskeluoikeudenTyyppi,
          nimi = jaksonNimi(opiskeluoikeusNode).getOrElse(nimi),
          laajuus = laajuus(opiskeluoikeusNode)
        ),
        vahvistus = päivämääräVahvistus(vahvistusPäivä, org),
        suorituskieli = None,
        osasuoritukset = osasuoritukset,
        toimipiste = org
      )
    }

    val tutkinnonSuoritus = optionalOppilaitos(opiskeluoikeusNode, oppilaitoksenNimiPäivä).flatMap { org =>
      viimeisinTutkinto.map { tutkinto =>
        KorkeakoulututkinnonSuoritus(
          koulutusmoduuli = tutkinto,
          arviointi = None,
          vahvistus = päivämääräVahvistus(vahvistusPäivä, org),
          suorituskieli = None,
          osasuoritukset = osasuoritukset,
          toimipiste = org
        )
      }
    }

    tutkinnonSuoritus.orElse(muuKorkeakoulunSuoritus).toList ++ päätasonSuoritukset
  }

  private def päivämääräVahvistus(vahvistusPäivä: Option[LocalDate], organisaatio: Organisaatio): Option[Päivämäärävahvistus] =
    vahvistusPäivä.map(pvm => Päivämäärävahvistus(pvm, organisaatio))

  def convertSuoritus(opiskeluoikeusNode: Option[Node], suoritus: Node, allNodes: List[Node]): Option[KorkeakouluSuoritus] = try {
    laji(suoritus) match {
      case "1" => // tutkinto
        val tutkinnonSuoritus = koulutuskoodi(suoritus).map { koulutuskoodi =>
          val koulutusmoduuli = opiskeluoikeusNode match {
            case Some(node) => tutkinto(koulutuskoodi, jaksonNimi(node))
            case _ => tutkinto(koulutuskoodi, None)
          }
          val osasuoritukset = childNodes(suoritus, allNodes).map(convertOpintojaksonSuoritus(_, allNodes))
          val päivämääräVahvistus = vahvistus(suoritus)
          KorkeakoulututkinnonSuoritus(
            koulutusmoduuli = koulutusmoduuli,
            arviointi = arviointi(suoritus),
            vahvistus = päivämääräVahvistus,
            suorituskieli = None,
            toimipiste = oppilaitos(suoritus, päivämääräVahvistus.map(_.päivä)),
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
        logger.info("Tuntematon laji: " + laji)
        None
    }
  } catch {
    case IllegalSuoritusException(msg) =>
      logger.warn(msg)
      None
  }

  private val tutkintoonJohtavienTyyppienKoodiarvot = List("1","2","3","4","6","7")
  private def tutkintoonJohtava(opiskeluoikeus: Node) = {
    val ooTyyppi = opiskeluoikeudenTyyppi(opiskeluoikeus).koodiarvo
    tutkintoonJohtavienTyyppienKoodiarvot.contains(ooTyyppi)
  }

  private def päättynyt(tila: KorkeakoulunOpiskeluoikeudenTila): Boolean =
    tila.opiskeluoikeusjaksot.lastOption.exists(_.opiskeluoikeusPäättynyt)

  private def lukuvuosiMaksuTiedot(n: Node) = KorkeakoulunOpiskeluoikeudenLukuvuosimaksu(
    alku = alkuPvm(n),
    loppu = loppuPvm(n),
    summa = (n \ "Summa").headOption.map(_.text.toInt)
  )

  private def lukukausiIlmoittautuminen(oppilaitos: Option[Oppilaitos], tila: KorkeakoulunOpiskeluoikeudenTila, opiskeluoikeusAvain: String, virtaXml: Node): Option[Lukukausi_Ilmoittautuminen] = {
    val ilmo = Ilmoittautuminen(oppilaitos, tila, opiskeluoikeusAvain, virtaXml)
    val ilmot = (virtaXml \\ "LukukausiIlmoittautuminen").toList
      .filter(ilmo.kuuluuOpiskeluoikeuteen)
      .map(lukukausiIlmo)
      .sortBy(_.alku)(DateOrdering.localDateOrdering)

    optionalList(ilmot).map(Lukukausi_Ilmoittautuminen)
  }

  private def järjestäväOrganisaatio(node: Node, päivä: Option[LocalDate]): Option[Oppilaitos] = {
    val numerot = oppilaitosnumero(node)

    if (numerot.nykyinen != numerot.järjestävä) {
      findOppilaitos(numerot.järjestävä, päivä)
    } else {
      None
    }
  }

  private def lukukausiIlmo(n: Node) = Lukukausi_Ilmoittautumisjakso(
    alku = alkuPvm(n),
    loppu = loppuPvm(n),
    tila = koodistoViitePalvelu.validate(Koodistokoodiviite((n \ "Tila").text, "virtalukukausiilmtila")).getOrElse(lukukausiIlmottautuminenPuuttuu),
    ylioppilaskunnanJäsen = (n \ "YlioppilaskuntaJasen").headOption.map(toBoolean),
    ythsMaksettu = (n \ "YTHSMaksu").headOption.map(toBoolean),
    maksetutLukuvuosimaksut = (n \ "LukuvuosiMaksu").headOption.map(lukukausiIlmoLukuvuosiMaksu)
  )

  private def lukukausiIlmoLukuvuosiMaksu(n: Node) = Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
    maksettu = (n \ "Maksettu").headOption.map(toBoolean),
    summa = (n \ "Summa").headOption.map(_.text.toInt),
    apuraha = (n \ "Apuraha").headOption.map(_.text.toInt)
  )

  private val virtaTruths = List("1", "true")
  private def toBoolean(n: Node) = virtaTruths.contains(n.text.toLowerCase)
  private lazy val lukukausiIlmottautuminenPuuttuu = koodistoViitePalvelu.validateRequired(Koodistokoodiviite("4", "virtalukukausiilmtila"))

  private def convertOpintojaksonSuoritus(suoritus: Node, allNodes: List[Node]): KorkeakoulunOpintojaksonSuoritus = {
    val osasuoritukset = childNodes(suoritus, allNodes).map(convertOpintojaksonSuoritus(_, allNodes))

    val päivämääräVahvistus = vahvistus(suoritus)
    KorkeakoulunOpintojaksonSuoritus(
      koulutusmoduuli = KorkeakoulunOpintojakso(
        tunniste = PaikallinenKoodi((suoritus \\ "@koulutusmoduulitunniste").text, suorituksenNimi(suoritus)),
        nimi = suorituksenNimi(suoritus),
        laajuus = laajuus(suoritus).orElse(laajuudetYhteensä(osasuoritukset))
      ),
      arviointi = arviointi(suoritus),
      vahvistus = päivämääräVahvistus,
      suorituskieli = (suoritus \\ "Kieli").headOption.flatMap(kieli => koodistoViitePalvelu.validate(Koodistokoodiviite(kieli.text.toUpperCase, "kieli"))),
      toimipiste = oppilaitos(suoritus, päivämääräVahvistus.map(_.päivä)),
      osasuoritukset = optionalList(osasuoritukset)
    )
  }

  private def laajuudetYhteensä(osasuoritukset: List[KorkeakoulunOpintojaksonSuoritus]) = {
    val laajuudet = osasuoritukset.flatMap(_.koulutusmoduuli.laajuus).map(_.arvo.toDouble).map(BigDecimal(_))
    if (laajuudet.isEmpty) {
      None
    } else {
      val laajuudetYhteensä = laajuudet.sum.setScale(1, scala.math.BigDecimal.RoundingMode.HALF_UP).toFloat
      Some(LaajuusOpintopisteissä(laajuudetYhteensä, opintojenlaajuusyksikkoOpintopistettä))
    }
  }

  private def laajuus(suoritusOrOpiskeluoikeus: Node): Option[Laajuus] = {
    val laajuusOpintopiste = (suoritusOrOpiskeluoikeus \ "Laajuus" \ "Opintopiste").headOption.map(_.text.toFloat).filter(_ > 0)
    val laajuusOpintoviikko = (suoritusOrOpiskeluoikeus \ "Laajuus" \ "Opintoviikko").headOption.map(_.text.toFloat).filter(_ > 0)

    (laajuusOpintopiste, laajuusOpintoviikko) match {
      case (Some(opintopiste), Some(_)) => Some(LaajuusOpintopisteissä(opintopiste, opintojenlaajuusyksikkoOpintopistettä))
      case (Some(opintopiste), None) => Some(LaajuusOpintopisteissä(opintopiste, opintojenlaajuusyksikkoOpintopistettä))
      case (None, Some(opintoviikko)) => Some(LaajuusOpintoviikoissa(opintoviikko, laajuusOpintoviikoissa))
      case _ => None
    }
  }

  private val opintojenlaajuusyksikkoOpintopistettä = koodistoViitePalvelu.validateRequired("opintojenlaajuusyksikko", "2")

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
      Some(Päivämäärävahvistus(arviointi.päivä, oppilaitos(suoritus, Some(arviointi.päivä))))
    ))
  }

  private def isRoot(suoritukset: Seq[Node])(node: Node) = {
    !suoritukset.exists(sisaltyvatAvaimet(_).contains(avain(node)))
  }

  private def sisaltyvatAvaimet(node: Node) = {
    (node \ "Sisaltyvyys").toList.map(sisaltyvyysNode => (sisaltyvyysNode \ "@sisaltyvaOpintosuoritusAvain").text)
  }

  private def childNodes(node: Node, allNodes: List[Node]) = {
    sisaltyvatAvaimet(node).flatMap { opintosuoritusAvain =>
      val osasuoritusNodes = allNodes.filter(avain(_) == opintosuoritusAvain)
      osasuoritusNodes match {
        case osasuoritusNode :: Nil => Some(osasuoritusNode)
        case Nil =>
          virheet += OpiskeluoikeusAvaintaEiLöydy(arvo = opintosuoritusAvain)
          None
        case osasuoritusNodes =>
          virheet += Duplikaatti(arvo = opintosuoritusAvain)
          Some(osasuoritusNodes.head)
      }
    }
  }

  private def suoritusNodes(virtaXml: Node) = {
    (virtaXml \\ "Opintosuoritukset" \\ "Opintosuoritus").toList
  }

  def sisältyyOpiskeluoikeuteen(suoritus: Node, opiskeluoikeus: Node, allNodes: List[Node],
                                päätasonSuoristusNode: Option[Node]): Boolean = try {
    suorituksessaOpintoOikeudenAvain(suoritus, opiskeluoikeus) &&
      LapsenTapauksessaOpiskeluoikeusavaimenPitääOllaSamaKuinPäätasonSuorituksella(opiskeluoikeus, päätasonSuoristusNode) ||
      childNodes(suoritus, allNodes).find(sisältyyOpiskeluoikeuteen(_, opiskeluoikeus, allNodes, Some(päätasonSuoristusNode.getOrElse(suoritus)))).isDefined
  } catch {
    case IllegalSuoritusException(_) => false
  }

  private def suorituksessaOpintoOikeudenAvain(suoritus: Node, opiskeluoikeus: Node): Boolean = {
    opiskeluoikeusAvain(suoritus) == avain(opiskeluoikeus)
  }

  // Sisältyvien opiskeluoikeuksien osalta kaikilla suorituksilla ei välttämättä ole sama opiskeluoikeus. Voi olla esim.
  // suoritus (OpiskeluoikeusAvain1) -> sisältyvyys -> suoritus (OpiskeluoikeusAvain2)
  // Siksi tehdään vertailu myös päätason suorituksen opiskeluoikeuteen (sisältyyOpiskeluoikeuteen), jottei tulkittaisi
  // tällaisessa tilanteessa koko päätasonsuorituksen kuuluvan johonkin toiseen opiskeluoikeuteen.
  private def LapsenTapauksessaOpiskeluoikeusavaimenPitääOllaSamaKuinPäätasonSuorituksella(opiskeluoikeus: Node, päätasonSuoristusNode: Option[Node]): Boolean = {
    päätasonSuoristusNode.isEmpty || opiskeluoikeusAvain(päätasonSuoristusNode.get) == "" || opiskeluoikeusAvain(päätasonSuoristusNode.get) == avain(opiskeluoikeus)
  }

  private def requiredKoodi(uri: String, koodi: String) = {
    koodistoViitePalvelu.validateRequired(uri, koodi)
  }

  private def opiskeluoikeudenTyyppi(opiskeluoikeus: Node): Koodistokoodiviite = {
    requiredKoodi("virtaopiskeluoikeudentyyppi", (opiskeluoikeus \ "Tyyppi").text)
  }

  private def tutkinto(koulutuskoodi: String, nimi: Option[LocalizedString] = None): Korkeakoulututkinto = {
    Korkeakoulututkinto(requiredKoodi("koulutus", koulutuskoodi), virtaNimi = nimi)
  }

  private def nimi(node: Node): Option[LocalizedString] = {
    sanitize((node \ "Nimi" map { nimi => (nimi \ "@kieli").text -> nimi.text }).toMap)
  }

  private def suorituksenNimi(suoritus: Node): LocalizedString = {
    nimi(suoritus).getOrElse(finnish("Suoritus: " + avain(suoritus)))
  }

  private def jaksonNimi(opiskeluoikeusNode: Node): Option[LocalizedString] = {
    val jakso = opiskeluoikeusNode \ "Jakso"
    jakso.headOption match {
      case Some(node) => nimi(node)
      case _ => None
    }
  }

  private def oppilaitos(node: Node, vahvistusPäivä: Option[LocalDate]): Oppilaitos =
    optionalOppilaitos(node, vahvistusPäivä).getOrElse(throw new RuntimeException(s"Nykyistä, lähde tai fuusioitunutta myöntäjäoppilaitosta ei löydy: ${oppilaitosnumero(node)}"))

  private def optionalOppilaitos(node: Node, päivä: Option[LocalDate]): Option[Oppilaitos] = {
    val numerot = oppilaitosnumero(node)
    val oppilaitos = if (siirtoOpiskelija(node) || hyväksilukuPäivämäärä(node).nonEmpty) {
      findOppilaitos(numerot.fuusioitunutMyöntäjä, päivä)
        .orElse(findOppilaitos(numerot.nykyinen, päivä))
    } else {
      findOppilaitos(numerot.lähde, päivä)
        .orElse(findOppilaitos(numerot.fuusioitunutMyöntäjä, päivä))
        .orElse(findOppilaitos(numerot.nykyinen, päivä))
    }

    if (oppilaitos.isEmpty) {
      logger.warn(s"Nykyistä tai lähdeoppilaitosta ei löydy: $numerot")
    }
    oppilaitos
  }

  private def findOppilaitos(numero: Option[String], päivä: Option[LocalDate]): Option[Oppilaitos] =
    numero.flatMap(oppilaitosRepository.findByOppilaitosnumero)
      .map(oppilaitoksenNimiAjanhetkellä(päivä))

  private def oppilaitoksenNimiAjanhetkellä(päivä: Option[LocalDate])(oppilaitos: Oppilaitos): Oppilaitos =
    päivä.flatMap(organisaatioRepository.getOrganisaationNimiHetkellä(oppilaitos.oid, _))
      .map(nimi => oppilaitos.copy(nimi = Some(nimi)))
      .getOrElse(oppilaitos)

  private def getOppilaitoksenNimiPäivä(
    viimeinenTila: Option[KorkeakoulunOpiskeluoikeusjakso],
    vahvistusPäivä: Option[LocalDate]
  ): Option[LocalDate] = viimeinenTila match {
    case Some(t) if t.opiskeluoikeusPäättynyt => Some(t.alku)
    case Some(t) => None
    case _ => vahvistusPäivä
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
      oppilaitosnumero(n).asList.exists(myöntäjä => kuuluuOpiskeluoikeuteen(LoppupäivällinenOpiskeluoikeusJakso(alkuPvm(n), loppuPvm(n)), myöntäjä))
    }
  }

  private def kuuluuOpiskeluoikeuteen(ilmoittautuminen: Jakso, myöntäjä: String) = {
    val oppilaitosNumero = oppilaitos.flatMap(_.oppilaitosnumero.map(_.koodiarvo))
    oppilaitosNumero.contains(myöntäjä) && aktiivisetJaksot.exists(_.overlaps(ilmoittautuminen))
  }

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

  def opiskeluoikeusAvain(node: Node) = {
    (node \ "@opiskeluoikeusAvain").text
  }

  def alkuPvm(node: Node) = {
    date((node \ "AlkuPvm").text)
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

  def siirtoOpiskelija(node: Node): Boolean =
    (node \\ "SiirtoOpiskelija").headOption.isDefined

  def hyväksilukuPäivämäärä(node: Node): Option[LocalDate] =
    (node \\ "HyvaksilukuPvm").headOption.map(d => date(d.text))

  def oppilaitosnumero(node: Node): Oppilaitosnumerot =
    Oppilaitosnumerot(
      nykyinen = nykyinenOppilaitosnumero(node),
      lähde = lähdeorganisaationOppilaitosnumero(node),
      järjestävä = järjestävänOrganisaationOppilaitosnumero(node),
      fuusioitunutMyöntäjä = fuusioituneenMyöntäjänOrganisaationOppilaitosnumero(node)
    )

  private def fuusioituneenMyöntäjänOrganisaationOppilaitosnumero(node: Node): Option[String] =
    // 2022-03-29: tällä hetkellä tätä roolia ei tietojen mukaan siirretä Virrasta
    findRoolinKoodi(node, OrganisaationRooli.FuusioitunutMyöntäjä)

  private def lähdeorganisaationOppilaitosnumero(node: Node): Option[String] =
    findRoolinKoodi(node, OrganisaationRooli.Lähde)

  private def järjestävänOrganisaationOppilaitosnumero(node: Node): Option[String] =
    findRoolinKoodi(node, OrganisaationRooli.Järjestävä)

  private def findRoolinKoodi(node: Node, rooli: OrganisaationRooli.Value): Option[String] = {
    def isRooli(org: Node) =
      OrganisaationRooli.parse((org \ "Rooli").text)
        .contains(rooli)

    (node \\ "Organisaatio")
      .find(isRooli)
      .map { org => (org \ "Koodi").text }
  }

  // huom, tässä kentässä voi olla oppilaitosnumeron lisäksi muitakin arvoja, esim. "UK" = "Ulkomainen korkeakoulu"
  // https://wiki.eduuni.fi/display/CSCVIRTA/Tietovarannon+koodistot#Tietovarannonkoodistot-Organisaatio,Organisation
  private def nykyinenOppilaitosnumero(node: Node): Option[String] = (node \ "Myontaja").headOption.map(_.text)
}

case class Oppilaitosnumerot(
  nykyinen: Option[String],
  lähde: Option[String],
  järjestävä: Option[String],
  fuusioitunutMyöntäjä: Option[String]
) {
  def asList = List(lähde, nykyinen, fuusioitunutMyöntäjä).flatten
}

// https://wiki.eduuni.fi/display/CSCVIRTA/Tietovarannon+koodistot#Tietovarannonkoodistot-Organisaationrooli,Organisationensroll
object OrganisaationRooli extends Enumeration {
  val Myöntävä = Value("1")
  val Järjestävä = Value("2")
  val Lähde = Value("3")
  val Kohde = Value("4")
  val FuusioitunutMyöntäjä = Value("5")
  val Tuntematon = Value("9")

  def parse(str: String) = Try(OrganisaationRooli.withName(str)).toOption
}

case class IllegalSuoritusException(msg: String) extends IllegalArgumentException(msg)
