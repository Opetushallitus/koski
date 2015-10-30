package fi.oph.tor.tor

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.Json
import fi.oph.tor.opintooikeus._
import fi.oph.tor.oppija._
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.tutkinto.{Suoritustapa, TutkintoRakenne, TutkintoRepository}
import fi.oph.tor.user.UserContext

class TodennetunOsaamisenRekisteri(oppijaRepository: OppijaRepository,
                                   opintoOikeusRepository: OpintoOikeusRepository,
                                   tutkintoRepository: TutkintoRepository,
                                   oppilaitosRepository: OppilaitosRepository,
                                   arviointiAsteikot: ArviointiasteikkoRepository) {

  def findOppijat(query: String)(implicit userContext: UserContext): Seq[Oppija] = {
    val oppijat: List[Oppija] = oppijaRepository.findOppijat(query)
    val filtered = opintoOikeusRepository.filterOppijat(oppijat)
    filtered
  }

  def createOrUpdate(oppija: TorOppija)(implicit userContext: UserContext): Either[HttpStatus, Oppija.Id] = {
    if (oppija.opintoOikeudet.length == 0) {
      Left(HttpStatus.badRequest("At least one OpintoOikeus required"))
    }
    else {
      HttpStatus.fold(oppija.opintoOikeudet.map(validateOpintoOikeus)) match {
        case error if error.isError => Left(error)
        case _ =>
          val oppijaOid: Either[HttpStatus, PossiblyUnverifiedOppijaOid] = oppija.henkilo.oid match {
            case Some(oid) => Right(UnverifiedOppijaOid(oid, oppijaRepository))
            case None => oppijaRepository.findOrCreate(oppija).right.map(VerifiedOppijaOid(_))
          }
          oppijaOid.right.flatMap { oppijaOid: PossiblyUnverifiedOppijaOid =>
            val opintoOikeusCreationResults = oppija.opintoOikeudet.map { opintoOikeus =>
              opintoOikeusRepository.createOrUpdate(oppijaOid, opintoOikeus)
            }
            opintoOikeusCreationResults.find(_.isLeft) match {
              case Some(Left(error)) => Left(error)
              case _ => Right(oppijaOid.oppijaOid)
            }
          }
      }
    }
  }

  def validateOpintoOikeus(opintoOikeus: OpintoOikeus)(implicit userContext: UserContext): HttpStatus = {
    tutkintoRepository.findPerusteRakenne(opintoOikeus.tutkinto.ePerusteetDiaarinumero)(arviointiAsteikot) match {
      case None =>
        HttpStatus.badRequest("Invalid ePeruste: " + opintoOikeus.tutkinto.ePerusteetDiaarinumero)
      case Some(rakenne) =>
        HttpStatus.ifThen(!userContext.hasReadAccess(opintoOikeus.oppilaitosOrganisaatio)) { HttpStatus.forbidden("Forbidden") }
          .ifOkThen{ HttpStatus
              .each(opintoOikeus.suoritustapa.filter(!Suoritustapa.apply(_).isDefined)) { suoritustapa => HttpStatus.badRequest("Invalid suoritustapa: " + suoritustapa)}
              .appendEach(opintoOikeus.osaamisala.filter(osaamisala => !TutkintoRakenne.findOsaamisala(rakenne, osaamisala).isDefined)) { osaamisala => HttpStatus.badRequest("Invalid osaamisala: " + osaamisala) }
              .appendEach(opintoOikeus.suoritukset)(validateSuoritus(_, opintoOikeus.suoritustapa.flatMap(Suoritustapa.apply), rakenne))
          }
    }
  }

  def validateSuoritus(suoritus: Suoritus, suoritusTapa: Option[Suoritustapa], rakenne: TutkintoRakenne): HttpStatus = {
    suoritusTapa match {
      case None => HttpStatus.badRequest("Suoritustapa puuttuu")
      case Some(suoritusTapa) => TutkintoRakenne.findTutkinnonOsa(rakenne, suoritusTapa, suoritus.koulutusModuuli) match {
        case None =>
          HttpStatus.badRequest("Tuntematon tutkinnon osa: " + suoritus.koulutusModuuli)
        case Some(tutkinnonOsa) =>
          HttpStatus.each(suoritus.arviointi) { arviointi =>
            HttpStatus
              .ifThen(Some(arviointi.asteikko) != tutkinnonOsa.arviointiAsteikko) {
              HttpStatus.badRequest("Perusteiden vastainen arviointiasteikko: " + arviointi.asteikko)
            }
              .ifOkThen {
              rakenne.arviointiAsteikot.find(_.koodisto == arviointi.asteikko) match {
                case Some(asteikko) if (!asteikko.arvosanat.contains(arviointi.arvosana)) =>
                  HttpStatus.badRequest("Arvosana " + Json.write(arviointi.arvosana) + " ei kuulu asteikkoon " + Json.write(asteikko))
                case None =>
                  HttpStatus.internalError("Asteikkoa " + arviointi.asteikko + " ei löydy tutkintorakenteesta")
                case _ =>
                  HttpStatus.ok
              }
            }
          }
      }
    }
  }

  def userView(oid: String)(implicit userContext: UserContext): Either[HttpStatus, TorOppija] = {
    oppijaRepository.findByOid(oid) match {
      case Some(oppija) =>
        opintoOikeudetForOppija(oppija) match {
          case Nil => notFound(oid)
          case opintoOikeudet => Right(TorOppija(oppija, opintoOikeudet))
        }
      case None => notFound(oid)
    }
  }

  def notFound(oid: String): Left[HttpStatus, Nothing] = {
    Left(HttpStatus.notFound(s"Oppija with oid: $oid not found"))
  }

  private def opintoOikeudetForOppija(oppija: Oppija)(implicit userContext: UserContext): Seq[OpintoOikeus] = {
    for {
      opintoOikeus   <- opintoOikeusRepository.findByOppijaOid(oppija.oid.get)
      tutkinto   <- tutkintoRepository.findByEPerusteDiaarinumero(opintoOikeus.tutkinto.ePerusteetDiaarinumero)
      oppilaitos <- oppilaitosRepository.findById(opintoOikeus.oppilaitosOrganisaatio.oid)
    } yield {
      opintoOikeus.copy(
        tutkinto = tutkinto.copy(rakenne = tutkintoRepository.findPerusteRakenne(tutkinto.ePerusteetDiaarinumero)(arviointiAsteikot)),
        oppilaitosOrganisaatio = oppilaitos
      )
    }
  }
}

