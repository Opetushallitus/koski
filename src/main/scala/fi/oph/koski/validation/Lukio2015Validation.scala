package fi.oph.koski.validation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema.{Henkilö, KoskeenTallennettavaOpiskeluoikeus, LukionOpiskeluoikeus}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService

object Lukio2015Validation {
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
        case Some(alkamispäivä) if eiRajapäivääEdeltäviäMuitaOpiskeluoikeuksia(oppijanOid, opiskeluoikeusRepository) =>
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
      case Some(syntymäpäivä) if !syntymäpäivä.isBefore(rajapäivät.lakiVoimassaVanhinSyntymäaika) => true
      case _ => false
    }
  }

  private def onVanhanLopsinOpiskeluoikeus(uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean = {
    uusiOpiskeluoikeus match {
      case lukionOpiskeluoikeus: LukionOpiskeluoikeus => lukionOpiskeluoikeus.on2015Opiskeluoikeus
      case _ => false
    }
  }

  private def eiRajapäivääEdeltäviäMuitaOpiskeluoikeuksia(henkilöOid: Henkilö.Oid, repository: CompositeOpiskeluoikeusRepository): Boolean = {
    val muutAlkamisajat = repository.getLukionOpiskeluoikeuksienAlkamisajatIlmanKäyttöoikeustarkistusta(henkilöOid)

    muutAlkamisajat.forall(!_.isBefore(rajapäivä))
  }

  private val rajapäivä = date(2021, 8, 1)
}
