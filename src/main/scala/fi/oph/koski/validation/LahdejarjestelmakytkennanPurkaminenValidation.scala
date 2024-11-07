package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, LähdejärjestelmäkytkennänPurkaminen, LähdejärjestelmäkytkentäPurettavissa}

object LahdejarjestelmakytkennanPurkaminenValidation {
  def validate(
    oo: KoskeenTallennettavaOpiskeluoikeus,
    koskiOpiskeluoikeudet: CompositeOpiskeluoikeusRepository,
  ): HttpStatus =
    oo match {
      case purettava: LähdejärjestelmäkytkentäPurettavissa =>
        validateMahdollinenPurkaminenTiedonsiirrossa(
          oo.oid,
          purettava.lähdejärjestelmäkytkentäPurettu,
          koskiOpiskeluoikeudet,
        )(KoskiSpecificSession.systemUser)
      case _ => HttpStatus.ok
    }

  def validateMuutos(
    oid: Option[String],
    purku: Option[LähdejärjestelmäkytkennänPurkaminen],
    koskiOpiskeluoikeudet: CompositeOpiskeluoikeusRepository,
  )(implicit user: KoskiSpecificSession): HttpStatus =
    oid match {
      case Some(oid) =>
        val previous = koskiOpiskeluoikeudet
          .findByOid(oid)
          .toOption
          .flatMap(_.toOpiskeluoikeusUnsafe.lähdejärjestelmäkytkentäPurettu)
        (previous, purku) match {
          case (None, Some(_)) =>
            KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu()
          case (Some(_), None) =>
            KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänMuuttaminenEiSallittu()
          case (Some(a), Some(b)) if a != b =>
            KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänMuuttaminenEiSallittu()
          case _ =>
            HttpStatus.ok
        }
      case None =>
        // Ei sallita purkamista uuden opiskeluoikeuden luonnin yhteydessäkään, koska eihän siinä olisi mitään tolkkua
        HttpStatus.validate(purku.isEmpty) {
          KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu()
        }
    }
}
