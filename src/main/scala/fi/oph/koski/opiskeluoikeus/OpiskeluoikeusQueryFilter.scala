package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioWithOid}

sealed trait OpiskeluoikeusQueryFilter

object OpiskeluoikeusQueryFilter {
  case class OpiskeluoikeusPäättynytAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeusPäättynytViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeusAlkanutAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeusAlkanutViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class TutkinnonTila(tila: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class Nimihaku(hakusana: String) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeudenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class SuorituksenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class KoulutusmoduulinTunniste(tunniste: List[Koodistokoodiviite]) extends OpiskeluoikeusQueryFilter
  case class Osaamisala(osaamisala: List[Koodistokoodiviite]) extends OpiskeluoikeusQueryFilter
  case class Tutkintonimike(nimike: List[Koodistokoodiviite]) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeudenTila(tila: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class Toimipiste(toimipiste: List[OrganisaatioWithOid]) extends OpiskeluoikeusQueryFilter
  case class Luokkahaku(hakusana: String) extends OpiskeluoikeusQueryFilter

  def parseQueryFilter(params: List[(String, String)])(implicit koodisto: KoodistoViitePalvelu): Either[HttpStatus, List[OpiskeluoikeusQueryFilter]] = {
    def dateParam(q: (String, String)): Either[HttpStatus, LocalDate] = q match {
      case (p, v) => try {
        Right(LocalDate.parse(v))
      } catch {
        case e: DateTimeParseException => Left(KoskiErrorCategory.badRequest.format.pvm("Invalid date parameter: " + p + "=" + v))
      }
    }

    val queryFilters: List[Either[HttpStatus, OpiskeluoikeusQueryFilter]] = params.map {
      case (p, v) if p == "opiskeluoikeusPäättynytAikaisintaan" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytAikaisintaan(_))
      case (p, v) if p == "opiskeluoikeusPäättynytViimeistään" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytViimeistään(_))
      case (p, v) if p == "opiskeluoikeusAlkanutAikaisintaan" => dateParam((p, v)).right.map(OpiskeluoikeusAlkanutAikaisintaan(_))
      case (p, v) if p == "opiskeluoikeusAlkanutViimeistään" => dateParam((p, v)).right.map(OpiskeluoikeusAlkanutViimeistään(_))
      case ("tutkinnonTila", v) => Right(TutkinnonTila(koodisto.validateRequired("suorituksentila", v)))
      case ("nimi", v) => Right(Nimihaku(v))
      case ("opiskeluoikeudenTyyppi", v) => Right(OpiskeluoikeudenTyyppi(koodisto.validateRequired("opiskeluoikeudentyyppi", v)))
      case ("suorituksenTyyppi", v) => Right(SuorituksenTyyppi(koodisto.validateRequired("suorituksentyyppi", v)))
      //case ("tutkinto", v) => TODO: koulutusmoduuli, nimike, osaamisala
      case ("opiskeluoikeudenTila", v) => Right(OpiskeluoikeudenTila(koodisto.validateRequired("koskiopiskeluoikeudentila", v)))
      //case ("toimipiste", v) => TODO: hierarkiahaku
      case ("luokka", v) => Right(Luokkahaku(v))
      case (p, _) => Left(KoskiErrorCategory.badRequest.queryParam.unknown("Unsupported query parameter: " + p))
    }

    queryFilters.partition(_.isLeft) match {
      case (Nil, queries) =>
        Right(queries.flatMap(_.right.toOption))
      case (errors, _) =>
        Left(HttpStatus.fold(errors.map(_.left.get)))
    }
  }
}