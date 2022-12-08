package fi.oph.koski.validation

import fi.oph.koski.documentation.ExamplesLukio.aikuistenOpsinPerusteet2015
import fi.oph.koski.documentation.LukioExampleData.aikuistenOpetussuunnitelma

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema.{Diaarinumerollinen, Henkilö, KoskeenTallennettavaOpiskeluoikeus, LukionKurssinSuoritus2015, LukionOpiskeluoikeus, LukionOppiaine, LukionOppiaine2015, LukionOppiaineenOppimääränSuoritus2015, LukionOppimääränSuoritus2015}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService

object Lukio2015Validation {
  def validateOppimääräSuoritettu(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus match {
      case oo: LukionOpiskeluoikeus if oo.oppimääräSuoritettu.getOrElse(false) =>
        HttpStatus.fold(
          aineopintojaOlemassa(oo),
          kurssejaRiittävästi(oo)
        )
      case _ => HttpStatus.ok
    }
  }

  def aineopintojaOlemassa(oo: LukionOpiskeluoikeus) = {
    val aineopinnot = oo.suoritukset.filter(_.isInstanceOf[LukionOppiaineenOppimääränSuoritus2015])
    HttpStatus.validate(aineopinnot.isEmpty || aineopinnot.exists(_.vahvistettu))(
      KoskiErrorCategory.badRequest.validation.rakenne.oppimääräSuoritettuIlmanVahvistettuaOppiaineenOppimäärää()
    )
  }

  def kurssejaRiittävästi(oo: LukionOpiskeluoikeus): HttpStatus = {
    val oppimääränSuoritus: Option[LukionOppimääränSuoritus2015] = oo.suoritukset
      .find(_.isInstanceOf[LukionOppimääränSuoritus2015])
      .map(_.asInstanceOf[LukionOppimääränSuoritus2015])

    if (oppimääränSuoritus.isDefined) {
      val kurssit = oppimääränSuoritus.get.osasuoritusLista.flatMap(_.osasuoritusLista)

      val laajuudet = kurssit
        .map(_.koulutusmoduuli.laajuusArvo(1))

      val kokonaislaajuus = laajuudet.sum

      oppimääränSuoritus.get.oppimääränKoodiarvo.get match {
        case "aikuistenops" => HttpStatus.validate(kokonaislaajuus >= 44)(KoskiErrorCategory.badRequest.validation.laajuudet.yhteenlaskettuLaajuusVääräLops2015Aikuiset())
        case "nuortenops" => HttpStatus.validate(kokonaislaajuus >= 75)(KoskiErrorCategory.badRequest.validation.laajuudet.yhteenlaskettuLaajuusVääräLops2015Nuoret())
      }
    } else {
      HttpStatus.ok
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
          if eiRajapäivääEdeltäviäMuitaOpiskeluoikeuksia(oppijanOid, opiskeluoikeus.oid, opiskeluoikeusRepository)
            && !onUlkomainenVaihtoopiskelija(opiskeluoikeus)
            && !onAikuistenOppimäärä(opiskeluoikeus) =>
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

  private def onAikuistenOppimäärä(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean = {
    opiskeluoikeus match {
      case lukionOpiskeluoikeus: LukionOpiskeluoikeus => lukionOpiskeluoikeus.suoritukset.exists{
        case s: LukionOppimääränSuoritus2015 if s.oppimäärä == aikuistenOpetussuunnitelma => true
        case s: LukionOppiaineenOppimääränSuoritus2015 => s.koulutusmoduuli match {
          case oppi: Diaarinumerollinen => oppi.perusteenDiaarinumero.getOrElse("") == aikuistenOpsinPerusteet2015
          case _ => false
        }
        case _ => false
      }
      case _ => false
    }
  }

  private val rajapäivä = date(2021, 8, 1)
}
