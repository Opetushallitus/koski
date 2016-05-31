package fi.oph.koski.koski

import java.time.LocalDate
import fi.oph.koski.http.{KoskiErrorCategory, HttpStatus}
import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._
import fi.oph.koski.koski.DateValidation._
import fi.oph.koski.koskiuser.{KoskiUser, AccessType, KoskiUser$}
import fi.oph.koski.tutkinto.{TutkintoRakenneValidator, TutkintoRepository}
import fi.oph.koski.util.Timing
import org.json4s.{JArray, JValue}

class KoskiValidator(tutkintoRepository: TutkintoRepository, val koodistoPalvelu: KoodistoViitePalvelu, val organisaatioRepository: OrganisaatioRepository) extends Timing {
  def validateAsJson(oppija: Oppija)(implicit user: KoskiUser, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    extractAndValidate(Json.toJValue(oppija))
  }

  def extractAndValidateBatch(parsedJson: JArray)(implicit user: KoskiUser, accessType: AccessType.Value): List[Either[HttpStatus, Oppija]] = {
    timed("extractAndValidateBatch") {
      parsedJson.arr.par.map { row =>
        extractAndValidate(row.asInstanceOf[JValue])
      }.toList
    }
  }

  def fillMissingInfo(oppija: Oppija) = oppija.copy(opiskeluoikeudet = oppija.opiskeluoikeudet.map(addKoulutusToimija(_)))

  def addKoulutusToimija(oo: Opiskeluoikeus) = {
    organisaatioRepository.getOrganisaatioHierarkiaIncludingParents(oo.oppilaitos.oid) match {
      case Some(hierarkia) => oo.withKoulutustoimija(hierarkia.toOrganisaatio)
      case _ => oo
    }
  }

  def extractAndValidate(parsedJson: JValue)(implicit user: KoskiUser, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    timed("jsonSchemaValidate")(KoskiJsonSchemaValidator.jsonSchemaValidate(parsedJson)) match {
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
    HttpStatus.validate(user.hasAccess(opiskeluOikeus.oppilaitos.oid, accessType)) { KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + opiskeluOikeus.oppilaitos.oid) }
      .then { HttpStatus.fold(
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("päättymispäivä", opiskeluOikeus.päättymispäivä)),
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("arvioituPäättymispäivä", opiskeluOikeus.arvioituPäättymispäivä)),
      DateValidation.validateJaksot("tila.opiskeluoikeusjaksot", opiskeluOikeus.tila.toList.flatMap(_.opiskeluoikeusjaksot)),
      DateValidation.validateJaksot("läsnäolotiedot.läsnäolojaksot", opiskeluOikeus.läsnäolotiedot.toList.flatMap(_.läsnäolojaksot)),
      HttpStatus.fold(opiskeluOikeus.suoritukset.map(validateSuoritus(_, None)))
    )}
      .then {
        HttpStatus.fold(opiskeluOikeus.suoritukset.map(TutkintoRakenneValidator(tutkintoRepository).validateTutkintoRakenne(_)))
      }
  }

  def validateSuoritus(suoritus: Suoritus, vahvistus: Option[Vahvistus]): HttpStatus = {
    val arviointipäivä = ("suoritus.arviointi.päivä", suoritus.arviointi.toList.flatten.flatMap(_.arviointipäivä))
    val alkamispäivä: (String, Iterable[LocalDate]) = ("suoritus.alkamispäivä", suoritus.alkamispäivä)
    val vahvistuspäivä: (String, Iterable[LocalDate]) = ("suoritus.vahvistus.päivä", suoritus.vahvistus.map(_.päivä))
    HttpStatus.fold(
      validateDateOrder(alkamispäivä, arviointipäivä).then(validateDateOrder(arviointipäivä, vahvistuspäivä).then(validateDateOrder(alkamispäivä, vahvistuspäivä)))
        :: validateStatus(suoritus, vahvistus)
        :: validateLaajuus(suoritus)
        :: suoritus.osasuoritusLista.map(validateSuoritus(_, suoritus.vahvistus.orElse(vahvistus)))
    )
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
    val tilaValmis: Boolean = suoritus.tila.koodiarvo == "VALMIS"
    if (hasVahvistus && !tilaValmis) {
      KoskiErrorCategory.badRequest.validation.tila.vahvistusVäärässäTilassa("Suorituksella " + suorituksenTunniste(suoritus) + " on vahvistus, vaikka suorituksen tila on " + suoritus.tila.koodiarvo)
    } else if (suoritus.tarvitseeVahvistuksen && !hasVahvistus && tilaValmis && !parentVahvistus.isDefined) {
      KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta " + suorituksenTunniste(suoritus) + " puuttuu vahvistus, vaikka suorituksen tila on " + suoritus.tila.koodiarvo)
    } else {
      (tilaValmis, suoritus.rekursiivisetOsasuoritukset.find(_.tila.koodiarvo == "KESKEN")) match {
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
