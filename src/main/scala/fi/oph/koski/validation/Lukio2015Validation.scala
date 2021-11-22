package fi.oph.koski.validation

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema.{Henkilö, KoskeenTallennettavaOpiskeluoikeus, LukionOpiskeluoikeus, LukionOppiaineenOppimääränSuoritus2015}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService

object Lukio2015Validation {
  def validateOppimääräSuoritettu(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus match {
      case lukio: LukionOpiskeluoikeus if lukio.oppimääräSuoritettu.getOrElse(false) =>
        val aineopinnot = lukio.suoritukset.filter(_.isInstanceOf[LukionOppiaineenOppimääränSuoritus2015])
        if (aineopinnot.isEmpty || aineopinnot.exists(_.vahvistettu)) {
          HttpStatus.ok
        } else {
          KoskiErrorCategory.badRequest.validation.rakenne.oppimääräSuoritettuIlmanVahvistettuaOppiaineenOppimäärää()
        }
      case _ => HttpStatus.ok
    }
  }

  def validateAlkamispäivä(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    oppijanSyntymäpäivä: Option[LocalDate],
    oppijanOid: String,
    opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository,
    rajapäivät: ValpasRajapäivätService
  ): HttpStatus = {
    if (oppijaRiittävänNuori(oppijanSyntymäpäivä, rajapäivät) && onVanhanLopsinOpiskeluoikeus(opiskeluoikeus)) {
      opiskeluoikeus.alkamispäivä match {
        case Some(alkamispäivä) if alkamispäivä.isBefore(rajapäivä) =>
          HttpStatus.ok
        case Some(alkamispäivä)
          if eiRajapäivääEdeltäviäMuitaOpiskeluoikeuksia(oppijanOid, opiskeluoikeus.oid, opiskeluoikeusRepository) && !onUlkomainenVaihtoopiskelija(opiskeluoikeus) =>
            KoskiErrorCategory.badRequest.validation.rakenne.liianVanhaOpetussuunnitelma()
        case _ =>
          HttpStatus.ok
      }
    } else {
      HttpStatus.ok
    }
  }

  private def oppijaRiittävänNuori(
    oppijanSyntymäpäivä: Option[LocalDate],
    rajapäivät: ValpasRajapäivätService): Boolean = {
    oppijanSyntymäpäivä match {
      case Some(syntymäpäivä) if !syntymäpäivä.isBefore(rajapäivät.lakiVoimassaVanhinSyntymäaika.plusYears(1)) => true
      case _ => false
    }
  }

  private def onVanhanLopsinOpiskeluoikeus(uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean = {
    uusiOpiskeluoikeus match {
      case lukionOpiskeluoikeus: LukionOpiskeluoikeus => lukionOpiskeluoikeus.on2015Opiskeluoikeus
      case _ => false
    }
  }

  private def eiRajapäivääEdeltäviäMuitaOpiskeluoikeuksia(
    henkilöOid: Henkilö.Oid,
    muutettavanOpiskeluoikeudenOid: Option[String],
    repository: CompositeOpiskeluoikeusRepository
  ): Boolean =
  {
    val muutAlkamisajat =
      repository.getLukionOpiskeluoikeuksienAlkamisajatIlmanKäyttöoikeustarkistusta(
        henkilöOid,
        muutettavanOpiskeluoikeudenOid
      )

    muutAlkamisajat.forall(!_.isBefore(rajapäivä))
  }

  private def onUlkomainenVaihtoopiskelija(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean = {
    opiskeluoikeus match {
      case lukionOpiskeluoikeus: LukionOpiskeluoikeus => lukionOpiskeluoikeus.lisätiedot.exists(_.ulkomainenVaihtoopiskelija)
      case _ => false
    }
  }

  private val rajapäivä = date(2021, 8, 1)
}
