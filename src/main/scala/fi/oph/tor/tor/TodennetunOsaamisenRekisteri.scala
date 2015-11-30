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
    if (oppija.opiskeluoikeudet.length == 0) {
      Left(HttpStatus.badRequest("At least one OpiskeluOikeus required"))
    }
    else {
      HttpStatus.fold(oppija.opiskeluoikeudet.map(validateOpiskeluOikeus)) match {
        case error if error.isError => Left(error)
        case _ =>
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
      }
    }
  }

  def validateOpiskeluOikeus(opiskeluOikeus: OpiskeluOikeus)(implicit userContext: UserContext): HttpStatus = {
    HttpStatus.ifThen(!userContext.hasReadAccess(opiskeluOikeus.oppilaitos)) { HttpStatus.forbidden("Ei oikeuksia organisatioon " + opiskeluOikeus.oppilaitos.oid) }
      .ifOkThen {
        validateSuoritus(opiskeluOikeus.suoritus, None)
      }
  }

  def validateSuoritus(suoritus: Suoritus, rakenne: Option[TutkintoRakenne]): HttpStatus = (suoritus.koulutusmoduulitoteutus, rakenne) match {
    case (t: TutkintoKoulutustoteutus, None) =>
      t.koulutusmoduuli.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
        case None =>
          HttpStatus.badRequest(t.koulutusmoduuli.perusteenDiaarinumero.map(d => "Tutkinnon peruste puuttuu tai on virheellinen: " + d).getOrElse("Tutkinnon peruste puuttuu"))
        case Some(rakenne) =>
          HttpStatus.each(t.suoritustapa.filter(suoritustapa => !validateKoodistoKoodiViite(suoritustapa.tunniste))) { suoritustapa: Suoritustapa => HttpStatus.badRequest("Invalid suoritustapa: " + suoritustapa.tunniste.koodiarvo) }
            .appendEach(t.osaamisala.toList.flatten.filter(osaamisala => !TutkintoRakenne.findOsaamisala(rakenne, osaamisala.koodiarvo).isDefined)) { osaamisala: KoodistoKoodiViite => HttpStatus.badRequest("Invalid osaamisala: " + osaamisala.koodiarvo) }
            .appendEach(suoritus.osasuoritukset.toList.flatten)(validateSuoritus(_, Some(rakenne)))
      }
    case (t: OpsTutkinnonosatoteutus, Some(rakenne))  =>
      t.suoritustapa match {
        case None => HttpStatus.badRequest("Suoritustapa puuttuu tutkinnon osalta " + t.koulutusmoduuli.tunniste)
        case Some(suoritusTapa) =>
          KoodistoPalvelu.validate(koodistoPalvelu, suoritusTapa.tunniste) match {
            case Some(suoritustapaKoodi) =>
              TutkintoRakenne.findTutkinnonOsa(rakenne, suoritusTapa.tunniste, t.koulutusmoduuli.tunniste) match {
                case None =>
                  HttpStatus.badRequest("Tutkinnon osa ei löydy perusterakenteesta: " + t.koulutusmoduuli.tunniste)
                case Some(tutkinnonOsa) =>
                  HttpStatus.each(suoritus.arviointi.toList.flatten) { arviointi =>
                    val arviointiAsteikko: Option[KoodistoViittaus] = KoodistoPalvelu.koodisto(koodistoPalvelu, arviointi.arvosana)
                    HttpStatus
                      .ifThen(arviointiAsteikko != tutkinnonOsa.arviointiAsteikko) {
                      HttpStatus.badRequest("Perusteiden vastainen arviointiasteikko: " + arviointi.arvosana)
                    }
                      .ifOkThen {
                      KoodistoPalvelu.validate(koodistoPalvelu, arviointi.arvosana) match {
                        case None => HttpStatus.badRequest("Arvosanaa " + arviointi.arvosana + " ei löydy koodistosta")
                        case _ => HttpStatus.ok
                      }
                    }
                  }
              }
            case None => HttpStatus.badRequest("Suoritustapaa " + suoritusTapa.tunniste + " ei löydy koodistosta")
          }
      }

    case _ => HttpStatus.ok
  }

  private def validateKoodistoKoodiViite(viite: KoodistoKoodiViite) = {
    KoodistoPalvelu.validate(koodistoPalvelu, viite).isDefined
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

