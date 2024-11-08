package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, LähdejärjestelmäkytkennänPurkaminen, LähdejärjestelmäkytkentäPurettavissa}

object LahdejarjestelmakytkennanPurkaminenValidation {
  def validateTiedonsiirto(
    oo: KoskeenTallennettavaOpiskeluoikeus,
    koskiOpiskeluoikeudet: CompositeOpiskeluoikeusRepository,
  ): HttpStatus =
    oo match {
      case purettava: LähdejärjestelmäkytkentäPurettavissa =>
        HttpStatus.fold(
          validateTerminaalitila(purettava),
          validateMahdollinenPurkaminenTiedonsiirrossa(
            oo.oid,
            purettava,
            koskiOpiskeluoikeudet,
          )(KoskiSpecificSession.systemUser)
        )
      case _ => HttpStatus.ok
    }

  def validatePurkaminen(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    HttpStatus.fold(
      HttpStatus.validate(oo.lähdejärjestelmänId.isDefined) {
        KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu("Opiskeluoikeudella ei ole lähdejärjestelmätunnistetta")
      },
      HttpStatus.validate(!oo.aktiivinen) {
        KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu("Lähdejärjestelmäkytkentää ei voi purkaa aktiiviselta opiskeluoikeudelta")
      }
    )

  private def validateMahdollinenPurkaminenTiedonsiirrossa[T <: KoskeenTallennettavaOpiskeluoikeus with LähdejärjestelmäkytkentäPurettavissa](
    oid: Option[String],
    oo: T,
    koskiOpiskeluoikeudet: CompositeOpiskeluoikeusRepository,
  )(implicit user: KoskiSpecificSession): HttpStatus =
    oid match {
      case Some(oid) =>
        val previous = koskiOpiskeluoikeudet
          .findByOid(oid)
          .toOption
          .flatMap(_.toOpiskeluoikeusUnsafe.lähdejärjestelmäkytkentäPurettu)
        (previous, oo.lähdejärjestelmäkytkentäPurettu) match {
          case (None, Some(_)) => // Purkutiedot on yritetty lisätä tiedonsiirron kautta
            KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu()
          case (Some(_), None) => // Purkutiedot on poistettu
            KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänMuuttaminenEiSallittu()
          case (Some(a), Some(b)) if a != b => // Purkutietojen sisältöä on muutettu
            KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänMuuttaminenEiSallittu()
          case (Some(_), Some(_)) if oo.lähdejärjestelmänId.isDefined => // Yritetään päivittää lähdejärjestelmästä purkutietojen kanssa
            KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänMuuttaminenEiSallittu()
          case _ =>
            HttpStatus.ok
        }
      case None =>
        // Ei sallita purkamista uuden opiskeluoikeuden luonnin yhteydessäkään, koska eihän siinä olisi mitään tolkkua
        HttpStatus.validate(oo.lähdejärjestelmäkytkentäPurettu.isEmpty) {
          KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu()
        }
    }

  private def validateTerminaalitila[T <: KoskeenTallennettavaOpiskeluoikeus with LähdejärjestelmäkytkentäPurettavissa](oo: T): HttpStatus =
    HttpStatus.validateNot(oo.lähdejärjestelmäkytkentäPurettu.isDefined && oo.aktiivinen) {
      KoskiErrorCategory.badRequest.validation.tila.terminaalitilaaEiSaaPurkaa()
    }
}
