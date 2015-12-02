package fi.oph.tor.tor

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.koodisto.KoodistoPalvelu
import fi.oph.tor.opiskeluoikeus._
import fi.oph.tor.oppija._
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema._
import fi.oph.tor.tutkinto.{TutkintoRakenneValidator, TutkintoRakenne, TutkintoRepository}
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

  private def validateOpiskeluOikeus(opiskeluOikeus: OpiskeluOikeus)(implicit userContext: UserContext): HttpStatus = {
    HttpStatus.ifThen(!userContext.hasReadAccess(opiskeluOikeus.oppilaitos)) { HttpStatus.forbidden("Ei oikeuksia organisatioon " + opiskeluOikeus.oppilaitos.oid) }
      .ifOkThen {
        TutkintoRakenneValidator(tutkintoRepository).validateTutkintoRakenne(opiskeluOikeus)
      }
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

