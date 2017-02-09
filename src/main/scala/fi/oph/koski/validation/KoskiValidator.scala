package fi.oph.koski.validation

import java.time.LocalDate

import fi.oph.koski.date.DateValidation
import fi.oph.koski.date.DateValidation._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.{TutkintoRakenneValidator, TutkintoRepository}
import fi.oph.koski.util.Timing
import org.json4s.{JArray, JValue}

class KoskiValidator(tutkintoRepository: TutkintoRepository, val koodistoPalvelu: KoodistoViitePalvelu, val organisaatioRepository: OrganisaatioRepository) extends Timing {
  def validateAsJson(oppija: Oppija)(implicit user: KoskiSession, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    extractAndValidateOppija(Json.toJValue(oppija))
  }

  def extractAndValidateBatch(oppijatJson: JArray)(implicit user: KoskiSession, accessType: AccessType.Value): List[(Either[HttpStatus, Oppija], JValue)] = {
    timed("extractAndValidateBatch") {
      oppijatJson.arr.par.map { oppijaJson =>
        (extractAndValidateOppija(oppijaJson), oppijaJson)
      }.toList
    }
  }

  def extractAndValidateOppija(parsedJson: JValue)(implicit user: KoskiSession, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    timed("extractAndValidateOppija")(KoskiJsonSchemaValidator.validateOppijaJson(parsedJson)) match {
      case status: HttpStatus if status.isOk =>
        val extractionResult: Either[HttpStatus, Oppija] = timed("extract")(ValidatingAndResolvingExtractor.extract[Oppija](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository)))
        extractionResult.right.flatMap { oppija =>
          validateOpiskeluoikeudet(oppija)
        }
      case status: HttpStatus => Left(status)
    }
  }

  def extractAndValidateOpiskeluoikeus(parsedJson: JValue)(implicit user: KoskiSession, accessType: AccessType.Value): Either[HttpStatus, Opiskeluoikeus] = {
    timed("extractAndValidateOpiskeluoikeus")(KoskiJsonSchemaValidator.validateOpiskeluoikeusJson(parsedJson)) match {
      case status: HttpStatus if status.isOk =>
        val extractionResult: Either[HttpStatus, Opiskeluoikeus] = timed("extract")(ValidatingAndResolvingExtractor.extract[Opiskeluoikeus](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository)))
        extractionResult.right.flatMap { opiskeluoikeus =>
          validateOpiskeluoikeus(opiskeluoikeus)
        }
      case status: HttpStatus => Left(status)
    }
  }

  private def validateOpiskeluoikeudet(oppija: Oppija)(implicit user: KoskiSession, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    val results: Seq[Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus]] = oppija.opiskeluoikeudet.map(validateOpiskeluoikeus)
    results.collect { case Left(e) => e} match {
      case Nil =>
        results.collect { case Right(oo) => oo } match {
          case Nil => Left(KoskiErrorCategory.badRequest.validation.tyhjäOpiskeluoikeusLista())
          case opiskeluoikeudet => Right(oppija.copy(opiskeluoikeudet = opiskeluoikeudet))
        }
      case errors => Left(HttpStatus.fold(errors))
    }
  }

  // TODO: mocha fail!!!


  private def validateOpiskeluoikeus(opiskeluoikeus: Opiskeluoikeus)(implicit user: KoskiSession, accessType: AccessType.Value): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = opiskeluoikeus match {
    case opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus =>
      fillMissingOrganisations(opiskeluoikeus).right.flatMap { opiskeluoikeus =>
        (validateAccess(opiskeluoikeus.getOppilaitos)
          .then { validateLähdejärjestelmä(opiskeluoikeus) }
          .then { HttpStatus.fold(
            validatePäivämäärät(opiskeluoikeus),
            HttpStatus.fold(opiskeluoikeus.suoritukset.map(validatePäätasonSuorituksenStatus(_, opiskeluoikeus))),
            HttpStatus.fold(opiskeluoikeus.suoritukset.map(validateSuoritus(_, opiskeluoikeus, None)))
          )}
          .then {
            HttpStatus.fold(opiskeluoikeus.suoritukset.map(TutkintoRakenneValidator(tutkintoRepository).validateTutkintoRakenne(_)))
          }) match {
            case HttpStatus.ok => Right(opiskeluoikeus)
            case status =>
              Left(status)
        }
      }

    case _ => Left(KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä"))
  }

  def fillMissingOrganisations(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    addOppilaitos(oo).right.flatMap(addKoulutustoimija)
  }

  def addOppilaitos(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    val oppilaitos: Either[HttpStatus, Oppilaitos] = oo.oppilaitos.map(Right(_)).getOrElse(oo.suoritukset.map(_.toimipiste) match {
      case List(toimipiste) => organisaatioRepository.findOppilaitosForToimipiste(toimipiste) match {
        case Some(oppilaitos) => Right(oppilaitos) // TODO: riittää yksiselitteinin oppilaitos, vaikka olisi useampi toimipiste
        case None => Left(KoskiErrorCategory.badRequest.validation.organisaatio.vääränTyyppinen(s"Toimipisteenä käytetylle organisaatiolle ${toimipiste.oid} ei löydy oppilaitos-tyyppistä yliorganisaatiota."))
      }
      case _ => Left(KoskiErrorCategory.badRequest.validation.organisaatio.oppilaitosPuuttuu("Opiskeluoikeudesta puuttuu oppilaitos, eikä sitä voi yksiselitteisesti päätellä annetuista toimipisteistä."))
    })
    oppilaitos.right.map(oo.withOppilaitos(_))
  }

  def addKoulutustoimija(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    organisaatioRepository.findKoulutustoimijaForOppilaitos(oo.getOppilaitos) match {
      case Some(löydettyKoulutustoimija) =>
        oo.koulutustoimija.map(_.oid) match {
          case Some(oid) if oid != löydettyKoulutustoimija.oid =>
            Left(KoskiErrorCategory.badRequest.validation.organisaatio.vääräKoulutustoimija(s"Annettu koulutustoimija $oid ei vastaa organisaatiopalvelusta löytyvää koulutustoimijaa ${löydettyKoulutustoimija.oid}"))
          case _ =>
            Right(oo.withKoulutustoimija(löydettyKoulutustoimija))
        }
      case _ =>
        logger.warn(s"Koulutustoimijaa ei löydy oppilaitokselle ${oo.oppilaitos}")
        Right(oo)
    }
  }

  private def validateAccess(org: OrganisaatioWithOid)(implicit user: KoskiSession, accessType: AccessType.Value): HttpStatus = {
    HttpStatus.validate(user.hasAccess(org.oid, accessType)) { KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + org.oid) }
  }

  private def validateLähdejärjestelmä(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): HttpStatus = {
    if (opiskeluoikeus.lähdejärjestelmänId.isDefined && !user.isPalvelukäyttäjä && !user.isRoot) {
      KoskiErrorCategory.forbidden.lähdejärjestelmäIdEiSallittu("Lähdejärjestelmä määritelty, mutta käyttäjä ei ole palvelukäyttäjä")
    } else if (user.isPalvelukäyttäjä && opiskeluoikeus.lähdejärjestelmänId.isEmpty) {
      KoskiErrorCategory.forbidden.lähdejärjestelmäIdPuuttuu("Käyttäjä on palvelukäyttäjä mutta lähdejärjestelmää ei ole määritelty")
    } else if (opiskeluoikeus.lähdejärjestelmänId.isDefined && user.isPalvelukäyttäjä && !user.juuriOrganisaatio.isDefined) {
      KoskiErrorCategory.forbidden.juuriorganisaatioPuuttuu("Automaattisen tiedonsiirron palvelukäyttäjällä ei yksiselitteistä juuriorganisaatiota")
    } else {
      HttpStatus.ok
    }
  }

  def validatePäivämäärät(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    val ensimmäisenJaksonPäivä: Option[LocalDate] = opiskeluoikeus.tila.opiskeluoikeusjaksot.headOption.map(_.alku)
    val viimeinenJakso = opiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption
    val päättäväJakso: Option[Opiskeluoikeusjakso] = opiskeluoikeus.tila.opiskeluoikeusjaksot.filter(_.opiskeluoikeusPäättynyt).lastOption
    val päättävänJaksonPäivä: Option[LocalDate] = päättäväJakso.map(_.alku)
    def formatOptionalDate(date: Option[LocalDate]) = date match {
      case Some(d) => d.toString
      case None => "null"
    }

    HttpStatus.fold(
      validateNotInFuture("päättymispäivä", opiskeluoikeus.päättymispäivä),
      validateDateOrder(("alkamispäivä", opiskeluoikeus.alkamispäivä), ("päättymispäivä", opiskeluoikeus.päättymispäivä)),
      validateDateOrder(("alkamispäivä", opiskeluoikeus.alkamispäivä), ("arvioituPäättymispäivä", opiskeluoikeus.arvioituPäättymispäivä)),
      HttpStatus.validate(päättäväJakso == None || päättäväJakso == viimeinenJakso)(KoskiErrorCategory.badRequest.validation.tila.tilaMuuttunutLopullisenTilanJälkeen(s"Opiskeluoikeuden tila muuttunut lopullisen tilan (${päättäväJakso.get.tila.koodiarvo}) jälkeen"))
        .then(HttpStatus.validate(!opiskeluoikeus.päättymispäivä.isDefined || opiskeluoikeus.päättymispäivä == päättävänJaksonPäivä)(KoskiErrorCategory.badRequest.validation.date.päättymispäivämäärä(s"Opiskeluoikeuden päättymispäivä (${formatOptionalDate(opiskeluoikeus.päättymispäivä)}) ei vastaa opiskeluoikeuden päättävän opiskeluoikeusjakson alkupäivää (${formatOptionalDate(päättävänJaksonPäivä)})"))),
      DateValidation.validateJaksot("tila.opiskeluoikeusjaksot", opiskeluoikeus.tila.opiskeluoikeusjaksot),
      HttpStatus.validate(opiskeluoikeus.alkamispäivä == ensimmäisenJaksonPäivä)(KoskiErrorCategory.badRequest.validation.date.alkamispäivä(s"Opiskeluoikeuden alkamispäivä (${formatOptionalDate(opiskeluoikeus.alkamispäivä)}) ei vastaa ensimmäisen opiskeluoikeusjakson alkupäivää (${formatOptionalDate(ensimmäisenJaksonPäivä)})"))
    )
  }

  def validateSuoritus(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, vahvistus: Option[Vahvistus])(implicit user: KoskiSession, accessType: AccessType.Value): HttpStatus = {
    val arviointipäivät: List[LocalDate] = suoritus.arviointi.toList.flatten.flatMap(_.arviointipäivä)
    val alkamispäivä: (String, Iterable[LocalDate]) = ("suoritus.alkamispäivä", suoritus.alkamispäivä)
    val vahvistuspäivät: Option[LocalDate] = suoritus.vahvistus.map(_.päivä)
    HttpStatus.fold(
      validateDateOrder(alkamispäivä, ("suoritus.arviointi.päivä", arviointipäivät)).then(validateDateOrder(("suoritus.arviointi.päivä", arviointipäivät), ("suoritus.vahvistus.päivä", vahvistuspäivät)).then(validateDateOrder(alkamispäivä, ("suoritus.vahvistus.päivä", vahvistuspäivät))))
        :: validateNotInFuture("suoritus.arviointi.päivä", arviointipäivät)
        :: validateNotInFuture("suoritus.vahvistus.päivä", vahvistuspäivät)
        :: validateToimipiste(suoritus)
        :: validateStatus(suoritus, vahvistus)
        :: validateLaajuus(suoritus)
        :: suoritus.osasuoritusLista.map(validateSuoritus(_, opiskeluoikeus, suoritus.vahvistus.orElse(vahvistus)))
    )
  }

  private def validateToimipiste(suoritus: Suoritus)(implicit user: KoskiSession, accessType: AccessType.Value): HttpStatus = suoritus match {
    case s:Toimipisteellinen => validateAccess(s.toimipiste)
    case _ => HttpStatus.ok
  }

  private def validateLaajuus(suoritus: Suoritus): HttpStatus = {
    suoritus.koulutusmoduuli.laajuus match {
      case Some(laajuus: Laajuus) =>
        val yksikköValidaatio = HttpStatus.fold(suoritus.osasuoritusLista.map { case osasuoritus =>
          osasuoritus.koulutusmoduuli.laajuus match {
            case Some(osasuorituksenLaajuus: Laajuus) if laajuus.yksikkö != osasuorituksenLaajuus.yksikkö =>
              KoskiErrorCategory.badRequest.validation.laajuudet.osasuorituksellaEriLaajuusyksikkö("Osasuorituksella " + suorituksenTunniste(osasuoritus) + " eri laajuuden yksikkö kuin suorituksella " + suorituksenTunniste(suoritus))
            case _ => HttpStatus.ok
          }
        })

        yksikköValidaatio.then({
        val osasuoritustenLaajuudet: List[Laajuus] = suoritus.osasuoritusLista.flatMap(_.koulutusmoduuli.laajuus)
        osasuoritustenLaajuudet match {
          case Nil => HttpStatus.ok
          case _ =>
            osasuoritustenLaajuudet.map(_.arvo).sum match {
              case summa if summa == laajuus.arvo =>
                HttpStatus.ok
              case summa =>
                KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Suorituksen " + suorituksenTunniste(suoritus) + " osasuoritusten laajuuksien summa " + summa + " ei vastaa suorituksen laajuutta " + laajuus.arvo)
            }
        }
        })
      case _ => HttpStatus.ok
    }
  }

  private def validateStatus(suoritus: Suoritus, parentVahvistus: Option[Vahvistus]): HttpStatus = {
    val hasArviointi: Boolean = !suoritus.arviointi.toList.flatten.isEmpty
    val hasVahvistus: Boolean = suoritus.vahvistus.isDefined
    if (hasVahvistus && !suoritus.valmis) {
      KoskiErrorCategory.badRequest.validation.tila.vahvistusVäärässäTilassa("Suorituksella " + suorituksenTunniste(suoritus) + " on vahvistus, vaikka suorituksen tila on " + suoritus.tila.koodiarvo)
    } else if (suoritus.valmis && !hasArviointi && !suoritus.isInstanceOf[Arvioinniton]) {
      KoskiErrorCategory.badRequest.validation.tila.arviointiPuuttuu("Suoritukselta " + suorituksenTunniste(suoritus) + " puuttuu arviointi, vaikka suorituksen tila on " + suoritus.tila.koodiarvo)
    } else if (suoritus.tarvitseeVahvistuksen && !hasVahvistus && suoritus.valmis && !parentVahvistus.isDefined) {
      KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta " + suorituksenTunniste(suoritus) + " puuttuu vahvistus, vaikka suorituksen tila on " + suoritus.tila.koodiarvo)
    } else {
      (suoritus.valmis, suoritus.rekursiivisetOsasuoritukset.find(_.tila.koodiarvo == "KESKEN")) match {
        case (true, Some(keskeneräinenOsasuoritus)) =>
          KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus(
            "Suorituksella " + suorituksenTunniste(suoritus) + " on keskeneräinen osasuoritus " + suorituksenTunniste(keskeneräinenOsasuoritus) + " vaikka suorituksen tila on " + suoritus.tila.koodiarvo)
        case _ =>
          HttpStatus.ok
      }
    }
  }

  private def validatePäätasonSuorituksenStatus(suoritus: PäätasonSuoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    if (suoritus.kesken && opiskeluoikeus.tila.opiskeluoikeusjaksot.last.tila.koodiarvo == "valmistunut") {
      KoskiErrorCategory.badRequest.validation.tila.suoritusVäärässäTilassa("Suoritus " + suorituksenTunniste(suoritus) + " on tilassa KESKEN, vaikka opiskeluoikeuden tila on valmistunut")
    } else {
      HttpStatus.ok
    }

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }

}
