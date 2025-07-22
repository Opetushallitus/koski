package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema.{IBCASSuoritus, IBCoreOppiaineenArviointi, IBDPCoreSuoritus, IBDPCoreOppiaineCAS, IBDPCoreOppiaineExtendedEssay, IBDPCoreOppiaineTheoryOfKnowledge, IBExtendedEssaySuoritus, IBKurssi, IBKurssinSuoritus, IBOpiskeluoikeus, IBOppiaineenArviointi, IBOppiaineenPredictedArviointi, IBOppiaineenSuoritus, IBPäätasonSuoritus, IBTheoryOfKnowledgeSuoritus, IBTutkinnonSuoritus, KoskeenTallennettavaOpiskeluoikeus, LaajuusKursseissa, LaajuusOpintopisteissä, LaajuusOsaamispisteissä, PreIBSuoritus2015}
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.util.DateOrdering.localDateOrdering
import fi.oph.koski.util.FinnishDateFormat

import java.time.LocalDate

object IBValidation {
  def validateIbOpiskeluoikeus(config: Config)(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    opiskeluoikeus match {
      case oo: IBOpiskeluoikeus => validateIbTutkinnonSuoritus(oo, config)
      case _ => HttpStatus.ok
    }

  def validateIbOpiskeluoikeusGlobal(
    oppijaOid: String,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    ooRepository: CompositeOpiskeluoikeusRepository,
    config: Config,
  ): HttpStatus = opiskeluoikeus match {
    case oo: IBOpiskeluoikeus =>
      oo.alkamispäivä match {
        case Some(alkamispäivä) =>
          val aiemminTallennetutAlkamispäivät = ooRepository.getKoulutusmuodonAlkamisajatIlmanKäyttöoikeustarkistusta(oppijaOid, "ibtutkinto")
          val alkamispäivät = aiemminTallennetutAlkamispäivät + (oo.oid.getOrElse("") -> alkamispäivä)
          val varhaisinAlkamispäivä = alkamispäivät.values.min
          val rajapäivä = ibKurssinLaajuusOpintopisteissäAlkaen(config)
          val ibTutkinto = oo.suoritukset.collect { case pts: IBTutkinnonSuoritus => pts }

          HttpStatus.fold(
            List(validateIBKurssienLaajuusyksiköt(oo, varhaisinAlkamispäivä, rajapäivä)) ++
            List(validateIBCoreSuoritustenArvioinnit(oo, varhaisinAlkamispäivä, rajapäivä)) ++
            ibTutkinto.map(validateCoreRequirements(_, varhaisinAlkamispäivä, rajapäivä)) ++
            oo.suoritukset.map(validatePreIB2019Suoritus(_, varhaisinAlkamispäivä, rajapäivä))
          )
        case None =>
          // Ei oikeasti ok, mutta alkamispäivän validaatio saa napata tämän tapauksen,
          // eikä tämä validaatio ota kantaa missä järjestyksessä validaatiot ajetaan.
          HttpStatus.ok
      }
    case _ =>
      HttpStatus.ok
  }


  private def validateIbTutkinnonSuoritus(opiskeluoikeus: IBOpiskeluoikeus, config: Config): HttpStatus = {
    // Ib-tutkinnolla voi olla 2 päätason suoritusta
    val suoritusHttpStatus: HttpStatus = opiskeluoikeus.suoritukset.foldLeft(HttpStatus.ok) {
      (accStatus, suoritus) =>
        suoritus match {
          case s: IBTutkinnonSuoritus if predictedArvioinninVaatiminenVoimassa(config) =>
            suorituksenVahvistusVaatiiPredictedArvioinnin(s)
          case _ =>
            accStatus
        }
    }

    suoritusHttpStatus
  }

  def suorituksenVahvistusVaatiiPredictedArvioinnin(päätasonSuoritus: IBTutkinnonSuoritus): HttpStatus =
    if (
      päätasonSuoritus.vahvistettu &&
      päätasonSuoritus.vahvistus.exists(!_.päivä.isBefore(LocalDate.of(2024, 1, 1)))
    ) {
      HttpStatus.validate(päätasonSuoritus.osasuoritukset.exists(_.exists {
        case os: IBOppiaineenSuoritus => os.predictedArviointi.exists(_.nonEmpty)
        case _ => true
      })) {
        KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(s"Vahvistettu suoritus ${päätasonSuoritus.koulutusmoduuli.tunniste} ei sisällä vähintään yhtä osasuoritusta, jolla on predicted grade")
      }
    } else {
      HttpStatus.ok
    }

  private def validateIBKurssienLaajuusyksiköt(oo: IBOpiskeluoikeus, alkamispäivä: LocalDate, rajapäivä: LocalDate): HttpStatus = {
    HttpStatus.fold(
      oo.suoritukset
        .flatMap(_.osasuoritukset.toList.flatten) // oppiaineet
        .flatMap(_.osasuoritukset.toList.flatten) // kurssit
        .collect { case kurssi: IBKurssinSuoritus => kurssi }
        .map { kurssi => validateIBKurssiLaajuusyksikkö(kurssi.koulutusmoduuli, alkamispäivä, rajapäivä) }
    )
  }

  private def validateIBKurssiLaajuusyksikkö(kurssi: IBKurssi, alkamispäivä: LocalDate, rajapäivä: LocalDate): HttpStatus =
    kurssi.laajuus.map {
      case _: LaajuusOpintopisteissä if alkamispäivä.isBefore(rajapäivä) =>
        KoskiErrorCategory.badRequest.validation.laajuudet.osauoritusVääräLaajuus(s"Osasuorituksen laajuuden voi ilmoitettaa opintopisteissä vain ${FinnishDateFormat.format(rajapäivä)} tai myöhemmin alkaneille IB-tutkinnon opiskeluoikeuksille")
      case _: LaajuusKursseissa if alkamispäivä.isEqualOrAfter(rajapäivä) =>
        KoskiErrorCategory.badRequest.validation.laajuudet.osauoritusVääräLaajuus(s"Osasuorituksen laajuus on ilmoitettava opintopisteissä ${FinnishDateFormat.format(rajapäivä)} tai myöhemmin alkaneille IB-tutkinnon opiskeluoikeuksille")
      case _ => HttpStatus.ok
    }.getOrElse(HttpStatus.ok)

  private def validateIBCoreSuoritustenArvioinnit(oo: IBOpiskeluoikeus, alkamispäivä: LocalDate, rajapäivä: LocalDate): HttpStatus = {
    HttpStatus.fold(
      oo.suoritukset
        .flatMap(_.osasuoritukset.toList.flatten) // oppiaineet
        .collect { case s: IBDPCoreSuoritus => s }
        .map { kurssi => validateIBCoreSuorituksenArviointi(kurssi) }
    )
  }

  private def validateIBCoreSuorituksenArviointi(s: IBDPCoreSuoritus): HttpStatus = {
    val sallitutArvosanatTOKJaEE = Set("A", "B", "C", "D", "E", "P")
    val sallitutArvosanatCAS = Set("1", "2", "3", "4", "5", "6", "7", "F", "O", "S")

    val invalidGrades: Seq[String] = s.koulutusmoduuli match {
      case _: IBDPCoreOppiaineTheoryOfKnowledge | _: IBDPCoreOppiaineExtendedEssay =>
        s.arviointi.toSeq.flatten.map(_.arvosana.koodiarvo).filterNot(sallitutArvosanatTOKJaEE.contains)
      case _: IBDPCoreOppiaineCAS =>
        s.arviointi.toSeq.flatten.map(_.arvosana.koodiarvo).filterNot(sallitutArvosanatCAS.contains)
      case _ => Seq.empty
    }

    if (invalidGrades.nonEmpty) {
      KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(
        s"IB Core suorituksella (${s.koulutusmoduuli.tunniste.koodiarvo}) on vääriä arvosanoja: ${invalidGrades.mkString(", ")}"
      )
    } else {
      HttpStatus.ok
    }
  }

  private def predictedArvioinninVaatiminenVoimassa(config: Config): Boolean =
    Option(LocalDate.parse(config.getString("validaatiot.ibSuorituksenVahvistusVaatiiPredictedArvosanan")))
      .exists(_.isEqualOrBefore(LocalDate.now()))

  private def ibKurssinLaajuusOpintopisteissäAlkaen(config: Config): LocalDate =
    LocalDate.parse(config.getString("validaatiot.ibLaajuudetOpintopisteinäAlkaen"))

  private def validateCoreRequirements(pts: IBTutkinnonSuoritus, alkamispäivä: LocalDate, rajapäivä: LocalDate): HttpStatus = {
    def validate(isEmpty: Boolean, errorText: => String): HttpStatus =
      HttpStatus.validate(isEmpty)(KoskiErrorCategory.badRequest.validation.rakenne.dpCoreDeprecated(errorText))

    lazy val dateString = FinnishDateFormat.format(rajapäivä)

    if (alkamispäivä.isEqualOrAfter(rajapäivä)) {
      HttpStatus.fold(
        validate(pts.theoryOfKnowledge.isEmpty, s"Theory of Knowledge -suoritus on siirrettävä osasuorituksena $dateString tai myöhemmin alkaneelle IB-opiskeluoikeudelle"),
        validate(pts.creativityActionService.isEmpty, s"Creativity Action Service -suoritus on siirrettävä osasuorituksena $dateString tai myöhemmin alkaneelle IB-opiskeluoikeudelle"),
        validate(pts.extendedEssay.isEmpty, s"Extended Essay -suoritus on siirrettävä osasuorituksena $dateString tai myöhemmin alkaneelle IB-opiskeluoikeudelle"),
        validate(pts.lisäpisteet.isEmpty, s"Lisäpisteitä ei voi siirtää $dateString tai myöhemmin alkaneelle IB-opiskeluoikeudelle"),
      )
    } else {
      validate(
        !pts.osasuoritukset.exists(_.exists(_.isInstanceOf[IBDPCoreSuoritus])),
        s"DP Core -oppiaineita ei voi siirtää osasuorituksena ennen $dateString alkaneelle IB-opiskeluoikeudelle"
      )
    }
  }

  private def validatePreIB2019Suoritus(pts: IBPäätasonSuoritus, alkamispäivä: LocalDate, rajapäivä: LocalDate): HttpStatus =
    pts match {
      case _: PreIBSuoritus2015 if alkamispäivä.isEqualOrAfter(rajapäivä) =>
        KoskiErrorCategory.badRequest.validation.rakenne(s"${FinnishDateFormat.format(rajapäivä)} tai myöhemmin alkaneelle IB-opiskeluoikeudelle voi siirtää vain vuoden 2019 opetussuunnitelman mukaisen pre-IB-suorituksen")
      case _ =>
        HttpStatus.ok
    }
}
