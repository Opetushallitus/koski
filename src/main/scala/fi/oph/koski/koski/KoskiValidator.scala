package fi.oph.koski.koski

import java.time.LocalDate

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koski.DateValidation._
import fi.oph.koski.koskiuser.{AccessType, KoskiUser}
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, OrganisaatioRepository}
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.{TutkintoRakenneValidator, TutkintoRepository}
import fi.oph.koski.util.Timing
import org.json4s.{JArray, JValue}

class KoskiValidator(tutkintoRepository: TutkintoRepository, val koodistoPalvelu: KoodistoViitePalvelu, val organisaatioRepository: OrganisaatioRepository) extends Timing {
  def validateAsJson(oppija: Oppija)(implicit user: KoskiUser, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    extractAndValidateOppija(Json.toJValue(oppija))
  }

  def extractAndValidateBatch(oppijatJson: JArray)(implicit user: KoskiUser, accessType: AccessType.Value): List[(Either[HttpStatus, Oppija], JValue)] = {
    timed("extractAndValidateBatch") {
      oppijatJson.arr.par.map { oppijaJson =>
        (extractAndValidateOppija(oppijaJson), oppijaJson)
      }.toList
    }
  }

  def extractAndValidateOppija(parsedJson: JValue)(implicit user: KoskiUser, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    timed("extractAndValidateOppija")(KoskiJsonSchemaValidator.validateOppijaJson(parsedJson)) match {
      case status: HttpStatus if status.isOk =>
        val extractionResult: Either[HttpStatus, Oppija] = timed("extract")(ValidatingAndResolvingExtractor.extract[Oppija](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository)))
        extractionResult.right.flatMap { oppija =>
          validateOpiskeluoikeudet(oppija) match {
            case status: HttpStatus if status.isOk => Right(fillMissingInfo(oppija))
            case status: HttpStatus => Left(status)
          }
        }
      case status: HttpStatus => Left(status)
    }
  }

  def extractAndValidateOpiskeluoikeus(parsedJson: JValue)(implicit user: KoskiUser, accessType: AccessType.Value): Either[HttpStatus, Opiskeluoikeus] = {
    timed("extractAndValidateOpiskeluoikeus")(KoskiJsonSchemaValidator.validateOpiskeluoikeusJson(parsedJson)) match {
      case status: HttpStatus if status.isOk =>
        val extractionResult: Either[HttpStatus, Opiskeluoikeus] = timed("extract")(ValidatingAndResolvingExtractor.extract[Opiskeluoikeus](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository)))
        extractionResult.right.flatMap { opiskeluoikeus =>
          validateOpiskeluOikeus(opiskeluoikeus) match {
            case status: HttpStatus if status.isOk => Right(fillMissingInfo(opiskeluoikeus))
            case status: HttpStatus => Left(status)
          }
        }
      case status: HttpStatus => Left(status)
    }
  }

  def fillMissingInfo(oppija: Oppija): Oppija = oppija.copy(opiskeluoikeudet = oppija.opiskeluoikeudet.map(fillMissingInfo(_)))

  def fillMissingInfo(oo: Opiskeluoikeus): Opiskeluoikeus = addKoulutustoimija(oo)

  def addKoulutustoimija(oo: Opiskeluoikeus): Opiskeluoikeus = {
    organisaatioRepository.findKoulutustoimija(oo.oppilaitos) match {
      case Some(koulutustoimija) => oo.withKoulutustoimija(koulutustoimija)
      case _ => oo
    }
  }

  private def validateOpiskeluoikeudet(oppija: Oppija)(implicit user: KoskiUser, accessType: AccessType.Value): HttpStatus = {
    if (oppija.opiskeluoikeudet.length == 0) {
      KoskiErrorCategory.badRequest.validation.tyhjäOpiskeluoikeusLista()
    } else if (accessType == AccessType.write && !oppija.ulkoisistaJärjestelmistäHaetutOpiskeluoikeudet.isEmpty) {
      KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä")
    } else {
      HttpStatus.fold(oppija.opiskeluoikeudet.map(validateOpiskeluOikeus))
    }
  }

  private def validateOpiskeluOikeus(opiskeluOikeus: Opiskeluoikeus)(implicit user: KoskiUser, accessType: AccessType.Value): HttpStatus = {
    validateAccess(opiskeluOikeus)
    .then { validateLähdejärjestelmä(opiskeluOikeus) }
    .then { HttpStatus.fold(
      validatePäivämäärät(opiskeluOikeus),
      HttpStatus.fold(opiskeluOikeus.suoritukset.map(validateSuoritus(_, None)))
    )}
    .then {
      HttpStatus.fold(opiskeluOikeus.suoritukset.map(TutkintoRakenneValidator(tutkintoRepository).validateTutkintoRakenne(_)))
    }
  }

  private def validateAccess(organisaatiollinen: OrganisaatioonLiittyvä)(implicit user: KoskiUser, accessType: AccessType.Value): HttpStatus = {
    HttpStatus.validate(user.hasAccess(organisaatiollinen.omistajaOrganisaatio.oid, accessType)) { KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + organisaatiollinen.omistajaOrganisaatio.oid) }
  }

  private def validateLähdejärjestelmä(opiskeluoikeus: Opiskeluoikeus)(implicit user: KoskiUser): HttpStatus = {
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

  def validatePäivämäärät(opiskeluOikeus: Opiskeluoikeus) = {
    val ensimmäisenJaksonPäivä: Option[LocalDate] = opiskeluOikeus.tila.opiskeluoikeusjaksot.headOption.map(_.alku)
    val päättävänJaksonPäivä: Option[LocalDate] = opiskeluOikeus.tila.opiskeluoikeusjaksot.filter(_.opiskeluoikeusPäättynyt).lastOption.map(_.alku)
    def formatOptionalDate(date: Option[LocalDate]) = date match {
      case Some(d) => d.toString
      case None => "null"
    }
    HttpStatus.fold(
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("päättymispäivä", opiskeluOikeus.päättymispäivä)),
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("arvioituPäättymispäivä", opiskeluOikeus.arvioituPäättymispäivä)),
      DateValidation.validateJaksot("tila.opiskeluoikeusjaksot", opiskeluOikeus.tila.opiskeluoikeusjaksot),
      DateValidation.validateJaksot("läsnäolotiedot.läsnäolojaksot", opiskeluOikeus.läsnäolotiedot.toList.flatMap(_.läsnäolojaksot)),
      HttpStatus.validate(opiskeluOikeus.alkamispäivä == ensimmäisenJaksonPäivä)(KoskiErrorCategory.badRequest.validation.date.alkamispäivä(s"Opiskeluoikeuden alkamispäivä (${formatOptionalDate(opiskeluOikeus.alkamispäivä)}) ei vastaa ensimmäisen opiskeluoikeusjakson alkupäivää (${formatOptionalDate(ensimmäisenJaksonPäivä)})")),
      HttpStatus.validate(!opiskeluOikeus.päättymispäivä.isDefined || opiskeluOikeus.päättymispäivä == päättävänJaksonPäivä)(KoskiErrorCategory.badRequest.validation.date.päättymispäivämäärä(s"Opiskeluoikeuden päättymispäivä (${formatOptionalDate(opiskeluOikeus.päättymispäivä)}) ei vastaa opiskeluoikeuden päättävän opiskeluoikeusjakson alkupäivää (${formatOptionalDate(päättävänJaksonPäivä)})"))
    )
  }

  def validateSuoritus(suoritus: Suoritus, vahvistus: Option[Vahvistus])(implicit user: KoskiUser, accessType: AccessType.Value): HttpStatus = {
    val arviointipäivä = ("suoritus.arviointi.päivä", suoritus.arviointi.toList.flatten.flatMap(_.arviointipäivä))
    val alkamispäivä: (String, Iterable[LocalDate]) = ("suoritus.alkamispäivä", suoritus.alkamispäivä)
    val vahvistuspäivä: (String, Iterable[LocalDate]) = ("suoritus.vahvistus.päivä", suoritus.vahvistus.map(_.päivä))
    HttpStatus.fold(
      validateDateOrder(alkamispäivä, arviointipäivä).then(validateDateOrder(arviointipäivä, vahvistuspäivä).then(validateDateOrder(alkamispäivä, vahvistuspäivä)))
        :: validateToimipiste(suoritus)
        :: validateStatus(suoritus, vahvistus)
        :: validateLaajuus(suoritus)
        :: suoritus.osasuoritusLista.map(validateSuoritus(_, suoritus.vahvistus.orElse(vahvistus)))
    )
  }

  private def validateToimipiste(suoritus: Suoritus)(implicit user: KoskiUser, accessType: AccessType.Value): HttpStatus = suoritus match {
    case s:Toimipisteellinen => validateAccess(s)
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

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }

}
