package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu

case class OpiskeluoikeusQueryParamParser(koodisto: KoodistoViitePalvelu) {
  def queryFilters(params: List[(String, String)]): Either[HttpStatus, List[OpiskeluoikeusQueryFilter]] = {
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
