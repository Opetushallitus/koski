package fi.oph.koski.validation

import java.time.LocalDate

import fi.oph.koski.date.DateValidation
import fi.oph.koski.date.DateValidation._
import fi.oph.koski.henkilo.OpintopolkuHenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi._
import fi.oph.koski.tutkinto.{Koulutustyyppi, TutkintoRakenneValidator, TutkintoRepository}
import fi.oph.koski.util.Timing
import org.json4s.{JArray, JValue}

class KoskiValidator(tutkintoRepository: TutkintoRepository, val koodistoPalvelu: KoodistoViitePalvelu, val organisaatioRepository: OrganisaatioRepository, opiskeluoikeudet: OpiskeluoikeusRepository, opintopolku: OpintopolkuHenkilöRepository) extends Timing {
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
    timed("extractAndValidateOppija"){
      val extractionResult: Either[HttpStatus, Oppija] = timed("extract")(ValidatingAndResolvingExtractor.extract[Oppija](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository)))
      extractionResult.right.flatMap { oppija =>
        validateOpiskeluoikeudet(oppija)
      }
    }
  }

  def extractAndValidateOpiskeluoikeus(parsedJson: JValue)(implicit user: KoskiSession, accessType: AccessType.Value): Either[HttpStatus, Opiskeluoikeus] = {
    timed("extractAndValidateOpiskeluoikeus") {
      val extractionResult: Either[HttpStatus, Opiskeluoikeus] = timed("extract")(ValidatingAndResolvingExtractor.extract[Opiskeluoikeus](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository)))
      extractionResult.right.flatMap { opiskeluoikeus =>
        validateOpiskeluoikeus(opiskeluoikeus, None)
      }
    }
  }

  private def validateOpiskeluoikeudet(oppija: Oppija)(implicit user: KoskiSession, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    val results: Seq[Either[HttpStatus, Opiskeluoikeus]] = oppija.opiskeluoikeudet.map(validateOpiskeluoikeus(_, Some(oppija.henkilö)))
    HttpStatus.foldEithers(results).right.flatMap {
      case Nil => Left(KoskiErrorCategory.badRequest.validation.tyhjäOpiskeluoikeusLista())
      case opiskeluoikeudet => Right(oppija.copy(opiskeluoikeudet = opiskeluoikeudet))
    }
  }

  private def validateOpiskeluoikeus(opiskeluoikeus: Opiskeluoikeus, henkilö: Option[Henkilö])(implicit user: KoskiSession, accessType: AccessType.Value): Either[HttpStatus, Opiskeluoikeus] = opiskeluoikeus match {
    case opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus =>
      fillMissingOrganisations(opiskeluoikeus).right.flatMap { opiskeluoikeus =>
        (validateAccess(opiskeluoikeus.getOppilaitos)
          .then { validateLähdejärjestelmä(opiskeluoikeus) }
          .then { HttpStatus.fold(
            validateSisältyvyys(henkilö, opiskeluoikeus),
            validatePäivämäärät(opiskeluoikeus),
            HttpStatus.fold(opiskeluoikeus.suoritukset.map(validatePäätasonSuorituksenStatus(_, opiskeluoikeus))),
            HttpStatus.fold(opiskeluoikeus.suoritukset.map(validateSuoritus(_, opiskeluoikeus, None)))
          )}
          .then {
            HttpStatus.fold(opiskeluoikeus.suoritukset.map(TutkintoRakenneValidator(tutkintoRepository, koodistoPalvelu).validateTutkintoRakenne(_)))
          }) match {
            case HttpStatus.ok => Right(opiskeluoikeus)
            case status =>
              Left(status)
        }
      }

    case _ if accessType == AccessType.write => Left(KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä"))
    case _ => Right(opiskeluoikeus)
  }

  def fillMissingOrganisations(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    addOppilaitos(oo).right.flatMap(addKoulutustoimija)
  }

  def addOppilaitos(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    val oppilaitos: Either[HttpStatus, Oppilaitos] = oo.oppilaitos.map(Right(_)).getOrElse {
      val toimipisteet: List[OrganisaatioWithOid] = oo.suoritukset.map(_.toimipiste)
      val oppilaitokset: Either[HttpStatus, List[Oppilaitos]] = HttpStatus.foldEithers(toimipisteet.map { toimipiste =>
        organisaatioRepository.findOppilaitosForToimipiste(toimipiste) match {
          case Some(oppilaitos) => Right(oppilaitos)
          case None => Left(KoskiErrorCategory.badRequest.validation.organisaatio.eiOppilaitos(s"Toimipisteenä käytetylle organisaatiolle ${toimipiste.oid} ei löydy oppilaitos-tyyppistä yliorganisaatiota."))
        }
      })
      oppilaitokset.right.map(_.distinct).flatMap {
        case List(oppilaitos) => Right(oppilaitos)
        case _ => Left(KoskiErrorCategory.badRequest.validation.organisaatio.oppilaitosPuuttuu("Opiskeluoikeudesta puuttuu oppilaitos, eikä sitä voi yksiselitteisesti päätellä annetuista toimipisteistä."))
      }
    }
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

  private def validateSisältyvyys(henkilö: Option[Henkilö], opiskeluoikeus: Opiskeluoikeus)(implicit user: KoskiSession, accessType: AccessType.Value): HttpStatus = opiskeluoikeus.sisältyyOpiskeluoikeuteen match {
    case Some(SisältäväOpiskeluoikeus(Oppilaitos(oppilaitosOid, _, _, _), id)) if accessType == AccessType.write =>
      opiskeluoikeudet.findById(id)(KoskiSession.systemUser) match {
        case Some(sisältäväOpiskeluoikeus) if (sisältäväOpiskeluoikeus.oppilaitosOid != oppilaitosOid) =>
          KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.vääräOppilaitos()
        case Some(sisältäväOpiskeluoikeus) =>
          val löydettyHenkilö: Either[HttpStatus, Oid] = henkilö match {
            case None => Left(HttpStatus.ok)
            case Some(h: HenkilöWithOid) => Right(h.oid)
            case Some(h: UusiHenkilö) => h.hetu.flatMap(opintopolku.findByHetu) match {
              case Some(henkilö) => Right(henkilö.oid)
              case None => Left(KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.henkilöTiedot())
            }
          }

          löydettyHenkilö match {
            case Right(löydettyHenkilöOid) if (löydettyHenkilöOid != sisältäväOpiskeluoikeus.oppijaOid) =>
              KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.henkilöTiedot()
            case Left(status) =>
              status
            case _ =>
              HttpStatus.ok
          }
        case None =>
          KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.eiLöydy(s"Sisältävää opiskeluoikeutta ei löydy id-arvolla $id")
        case _ =>
          HttpStatus.ok
      }
    case _ => HttpStatus.ok
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
      validateNotInFuture("päättymispäivä", KoskiErrorCategory.badRequest.validation.date.päättymispäiväTulevaisuudessa, opiskeluoikeus.päättymispäivä),
      validateDateOrder(("alkamispäivä", opiskeluoikeus.alkamispäivä), ("päättymispäivä", opiskeluoikeus.päättymispäivä), KoskiErrorCategory.badRequest.validation.date.päättymisPäiväEnnenAlkamispäivää),
      validateDateOrder(("alkamispäivä", opiskeluoikeus.alkamispäivä), ("arvioituPäättymispäivä", opiskeluoikeus.arvioituPäättymispäivä), KoskiErrorCategory.badRequest.validation.date.arvioituPäättymisPäiväEnnenAlkamispäivää),
      HttpStatus.validate(päättäväJakso == None || päättäväJakso == viimeinenJakso)(KoskiErrorCategory.badRequest.validation.tila.tilaMuuttunutLopullisenTilanJälkeen(s"Opiskeluoikeuden tila muuttunut lopullisen tilan (${päättäväJakso.get.tila.koodiarvo}) jälkeen"))
        .then(HttpStatus.validate(!opiskeluoikeus.päättymispäivä.isDefined || opiskeluoikeus.päättymispäivä == päättävänJaksonPäivä)(KoskiErrorCategory.badRequest.validation.date.päättymispäivämäärä(s"Opiskeluoikeuden päättymispäivä (${formatOptionalDate(opiskeluoikeus.päättymispäivä)}) ei vastaa opiskeluoikeuden päättävän opiskeluoikeusjakson alkupäivää (${formatOptionalDate(päättävänJaksonPäivä)})"))),
      DateValidation.validateJaksot("tila.opiskeluoikeusjaksot", opiskeluoikeus.tila.opiskeluoikeusjaksot, KoskiErrorCategory.badRequest.validation.date.opiskeluoikeusjaksojenPäivämäärät),
      HttpStatus.validate(opiskeluoikeus.alkamispäivä == ensimmäisenJaksonPäivä)(KoskiErrorCategory.badRequest.validation.date.alkamispäivä(s"Opiskeluoikeuden alkamispäivä (${formatOptionalDate(opiskeluoikeus.alkamispäivä)}) ei vastaa ensimmäisen opiskeluoikeusjakson alkupäivää (${formatOptionalDate(ensimmäisenJaksonPäivä)})"))
    )
  }

  def validateSuoritus(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, vahvistus: Option[Vahvistus])(implicit user: KoskiSession, accessType: AccessType.Value): HttpStatus = {
    val arviointipäivät: List[LocalDate] = suoritus.arviointi.toList.flatten.flatMap(_.arviointipäivä)
    val alkamispäivä: (String, Iterable[LocalDate]) = ("suoritus.alkamispäivä", suoritus.alkamispäivä)
    val vahvistuspäivät: Option[LocalDate] = suoritus.vahvistus.map(_.päivä)
    HttpStatus.fold(
      validateDateOrder(alkamispäivä, ("suoritus.arviointi.päivä", arviointipäivät), KoskiErrorCategory.badRequest.validation.date.arviointiEnnenAlkamispäivää)
        .then(validateDateOrder(("suoritus.arviointi.päivä", arviointipäivät), ("suoritus.vahvistus.päivä", vahvistuspäivät), KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenArviointia)
          .then(validateDateOrder(alkamispäivä, ("suoritus.vahvistus.päivä", vahvistuspäivät), KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenAlkamispäivää)))
        :: validateNotInFuture("suoritus.arviointi.päivä", KoskiErrorCategory.badRequest.validation.date.arviointipäiväTulevaisuudessa, arviointipäivät)
        :: validateNotInFuture("suoritus.vahvistus.päivä", KoskiErrorCategory.badRequest.validation.date.vahvistuspäiväTulevaisuudessa, vahvistuspäivät)
        :: validateToimipiste(suoritus)
        :: validateStatus(suoritus, vahvistus)
        :: validateLaajuus(suoritus)
        :: validateOppiaineet(suoritus)
        :: validateTutkinnonosanRyhmä(suoritus)
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
          (osasuoritustenLaajuudet, suoritus.valmis) match {
            case (_, false) => HttpStatus.ok
            case (Nil, _) => HttpStatus.ok
            case (_, _) =>
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

  def validateTutkinnonosanRyhmä(suoritus: Suoritus): HttpStatus = {

    def validateTutkinnonosaSuoritus(suoritus: AmmatillisenTutkinnonOsanSuoritus, koulutustyyppi: Koulutustyyppi): HttpStatus = {
      if (ammatillisenPerustutkinnonTyypit.contains(koulutustyyppi)) {
        suoritus.tutkinnonOsanRyhmä
          .map(_ => HttpStatus.ok)
          .getOrElse(KoskiErrorCategory.badRequest.validation.rakenne.tutkinnonOsanRyhmäPuuttuu("Tutkinnonosalta " + suoritus.koulutusmoduuli.tunniste + " puuttuu tutkinnonosan ryhmä joka on pakollinen ammatillisen perustutkinnon tutkinnonosille." ))
      } else {
        suoritus.tutkinnonOsanRyhmä
          .map(_ => KoskiErrorCategory.badRequest.validation.rakenne.koulutustyyppiEiSalliTutkinnonOsienRyhmittelyä("Tutkinnonosalle " + suoritus.koulutusmoduuli.tunniste + " on määritetty tutkinnonosan ryhmä vaikka kyseessä ei ole ammatillinen perustutkinto."))
          .getOrElse(HttpStatus.ok)
      }
    }

    def validateTutkinnonosaSuoritukset(tutkinto: AmmatillinenTutkintoKoulutus, suoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]]) = {
      koulutustyyppi(tutkinto.perusteenDiaarinumero.get)
        .map(tyyppi => HttpStatus.fold(suoritukset.toList.flatten.map(s => validateTutkinnonosaSuoritus(s, tyyppi))))
        .getOrElse {
          logger.warn("Ammatilliselle tutkintokoulutukselle " + tutkinto.perusteenDiaarinumero.get + " ei löydy koulutustyyppiä e-perusteista.")
          HttpStatus.ok
        }
    }

    def koulutustyyppi(diaarinumero: String): Option[Koulutustyyppi] = tutkintoRepository.findPerusteRakenne(diaarinumero).map(r => r.koulutustyyppi)

    suoritus match {
      case s: AmmatillisenTutkinnonSuoritus => validateTutkinnonosaSuoritukset(s.koulutusmoduuli, s.osasuoritukset)
      case s: AmmatillisenTutkinnonOsittainenSuoritus => validateTutkinnonosaSuoritukset(s.koulutusmoduuli, s.osasuoritukset)
      case _ => HttpStatus.ok
    }
  }

  def validateOppiaineet(suoritus: Suoritus) = suoritus match {
    case s: PerusopetuksenOppimääränSuoritus if s.osasuoritusLista.isEmpty && s.valmis =>
      KoskiErrorCategory.badRequest.validation.tila.oppiaineetPuuttuvat("Suorituksella ei ole osasuorituksena yhtään oppiainetta, vaikka sen tila on VALMIS")
    case s: PerusopetuksenVuosiluokanSuoritus if s.koulutusmoduuli.luokkaAste == "9" && s.valmis && !s.jääLuokalle && s.osasuoritusLista.nonEmpty =>
      KoskiErrorCategory.badRequest.validation.tila.oppiaineitaEiSallita("9.vuosiluokan suoritukseen ei voi syöttää oppiaineita, kun suoritus on VALMIS, eikä oppilas jää luokalle")
    case _ =>
      HttpStatus.ok
  }
}