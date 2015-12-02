package fi.oph.tor.tor

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.Json
import fi.oph.tor.koodisto.{KoodistoViittaus, KoodistoPalvelu}
import fi.oph.tor.opiskeluoikeus._
import fi.oph.tor.oppija._
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema._
import fi.oph.tor.tutkinto.{TutkintoRakenne, TutkintoRepository}
import fi.oph.tor.user.UserContext

class TodennetunOsaamisenRekisteri(oppijaRepository: OppijaRepository,
                                   opiskeluOikeusRepository: OpiskeluOikeusRepository,
                                   tutkintoRepository: TutkintoRepository,
                                   oppilaitosRepository: OppilaitosRepository,
                                   arviointiAsteikot: ArviointiasteikkoRepository,
                                   koodistoPalvelu: KoodistoPalvelu) {

  def findOppijat(query: String)(implicit userContext: UserContext): Seq[FullHenkilö] = {
    val oppijat: List[FullHenkilö] = oppijaRepository.findOppijat(query)
    val filtered = opiskeluOikeusRepository.filterOppijat(oppijat)
    filtered
  }

  def createOrUpdate(oppija: TorOppija)(implicit userContext: UserContext): Either[HttpStatus, Henkilö.Id] = {
    validate(oppija) match {
      case status if (status.isOk) =>
        val oppijaOid: Either[HttpStatus, PossiblyUnverifiedOppijaOid] = oppija.henkilö match {
          case h:NewHenkilö => oppijaRepository.findOrCreate(oppija).right.map(VerifiedOppijaOid(_))
          case h:HenkilöWithOid => Right(UnverifiedOppijaOid(h.oid, oppijaRepository))
        }
        oppijaOid.right.flatMap { oppijaOid: PossiblyUnverifiedOppijaOid =>
          val opiskeluOikeusCreationResults = oppija.opiskeluoikeudet.map { opiskeluOikeus =>
            opiskeluOikeusRepository.createOrUpdate(oppijaOid, opiskeluOikeus)
          }
          opiskeluOikeusCreationResults.find(_.isLeft) match {
            case Some(Left(error)) => Left(error)
            case _ => Right(oppijaOid.oppijaOid)
          }
        }
      case notOk => Left(notOk)
    }
  }

  def validate(oppija: TorOppija)(implicit userContext: UserContext): HttpStatus = {
    if (oppija.opiskeluoikeudet.length == 0) {
      HttpStatus.badRequest("At least one OpiskeluOikeus required")
    }
    else {
      HttpStatus.fold(oppija.opiskeluoikeudet.map(validateOpiskeluOikeus))
    }
  }

  def validateOpiskeluOikeus(opiskeluOikeus: OpiskeluOikeus)(implicit userContext: UserContext): HttpStatus = {
    HttpStatus.ifThen(!userContext.hasReadAccess(opiskeluOikeus.oppilaitos)) { HttpStatus.forbidden("Ei oikeuksia organisatioon " + opiskeluOikeus.oppilaitos.oid) }
      .ifOkThen {
        validateSuoritus(opiskeluOikeus.suoritus, None, None)
      }
  }

  def validateSuoritus(suoritus: Suoritus, rakenne: Option[TutkintoRakenne], suoritustapa: Option[Suoritustapa]): HttpStatus = (suoritus.koulutusmoduulitoteutus, rakenne, suoritustapa) match {
    case (t: TutkintoKoulutustoteutus, _, _) =>
      t.koulutusmoduuli.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
        case None =>
          HttpStatus.badRequest(t.koulutusmoduuli.perusteenDiaarinumero.map(d => "Tutkinnon peruste puuttuu tai on virheellinen: " + d).getOrElse("Tutkinnon peruste puuttuu"))
        case Some(rakenne) =>
          HttpStatus.each(t.osaamisala.toList.flatten.filter(osaamisala => !TutkintoRakenne.findOsaamisala(rakenne, osaamisala.koodiarvo).isDefined)) { osaamisala: KoodistoKoodiViite => HttpStatus.badRequest("Invalid osaamisala: " + osaamisala.koodiarvo) }
            .appendEach(suoritus.osasuoritukset.toList.flatten)(validateSuoritus(_, Some(rakenne), t.suoritustapa))
      }
    case (t: OpsTutkinnonosatoteutus, Some(rakenne), None)  =>
      HttpStatus.badRequest("Tutkinnolta puuttuu suoritustapa. Tutkinnon osasuorituksia ei hyväksytä.")
    case (t: OpsTutkinnonosatoteutus, Some(rakenne), Some(suoritustapa))  =>
      TutkintoRakenne.findTutkinnonOsa(rakenne, suoritustapa.tunniste, t.koulutusmoduuli.tunniste) match {
        case None =>
          HttpStatus.badRequest("Tutkinnon osa ei löydy perusterakenteesta: " + t.koulutusmoduuli.tunniste)
        case Some(tutkinnonOsa) =>
          HttpStatus.ok
      }
    case _ =>
      HttpStatus.ok
  }

  def userView(oid: String)(implicit userContext: UserContext): Either[HttpStatus, TorOppija] = {
    oppijaRepository.findByOid(oid) match {
      case Some(oppija) =>
        opiskeluoikeudetForOppija(oppija) match {
          case Nil => notFound(oid)
          case opiskeluoikeudet => Right(TorOppija(oppija, opiskeluoikeudet))
        }
      case None => notFound(oid)
    }
  }

  def notFound(oid: String): Left[HttpStatus, Nothing] = {
    Left(HttpStatus.notFound(s"Oppija with oid: $oid not found"))
  }

  private def opiskeluoikeudetForOppija(oppija: FullHenkilö)(implicit userContext: UserContext): Seq[OpiskeluOikeus] = {
    for {
      opiskeluOikeus   <- opiskeluOikeusRepository.findByOppijaOid(oppija.oid)
      oppilaitos <- oppilaitosRepository.findById(opiskeluOikeus.oppilaitos.oid)
    } yield {
      opiskeluOikeus.copy(
        oppilaitos = oppilaitos
      )
    }
  }
}

