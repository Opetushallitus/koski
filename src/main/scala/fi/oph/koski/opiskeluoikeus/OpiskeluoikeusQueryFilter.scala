package fi.oph.koski.opiskeluoikeus
import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.{Luokkahaku, Nimihaku, SuoritusJsonHaku, _}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioOid, OrganisaatioWithOid}
import org.json4s.JsonAST.JValue

import scala.util.{Failure, Success}

sealed trait OpiskeluoikeusQueryFilter

object OpiskeluoikeusQueryFilter {
  case class OpiskeluoikeusPäättynytAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeusPäättynytViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeusAlkanutAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeusAlkanutViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class SuorituksenTila(tila: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeudenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class SuorituksenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeudenTila(tila: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class Tutkintohaku(hakusana: String) extends OpiskeluoikeusQueryFilter
  case class Toimipiste(toimipiste: List[OrganisaatioWithOid]) extends OpiskeluoikeusQueryFilter
  case class Luokkahaku(hakusana: String) extends OpiskeluoikeusQueryFilter
  case class Nimihaku(hakusana: String) extends OpiskeluoikeusQueryFilter
  case class SuoritusJsonHaku(json: JValue) extends OpiskeluoikeusQueryFilter

  def parse(params: List[(String, String)])(implicit koodisto: KoodistoViitePalvelu, organisaatiot: OrganisaatioRepository, session: KoskiSession): Either[HttpStatus, List[OpiskeluoikeusQueryFilter]] = OpiskeluoikeusQueryFilterParser.parse(params)
}

private object OpiskeluoikeusQueryFilterParser {
  def parse(params: List[(String, String)])(implicit koodisto: KoodistoViitePalvelu, organisaatiot: OrganisaatioRepository, session: KoskiSession): Either[HttpStatus, List[OpiskeluoikeusQueryFilter]] = {
    def dateParam(q: (String, String)): Either[HttpStatus, LocalDate] = q match {
      case (p, v) => try {
        Right(LocalDate.parse(v))
      } catch {
        case e: DateTimeParseException => Left(KoskiErrorCategory.badRequest.format.pvm("Invalid date parameter: " + p + "=" + v))
      }
    }

    def koodistohaku(koodistoUri: String, hakusana: String) = {
      val koodit: List[Koodistokoodiviite] = koodisto.getKoodistoKoodiViitteet(koodisto.getLatestVersion(koodistoUri).get).get
      koodit.filter(koodi => koodi.nimi.map(_.get(session.lang)).getOrElse(koodi.koodiarvo).toLowerCase.contains(hakusana.toLowerCase))
    }


    val queryFilters: List[Either[HttpStatus, OpiskeluoikeusQueryFilter]] = params.filterNot{case (key, value) => List("sort", "pageSize", "pageNumber", "toimipisteNimi").contains(key)}.map {
      case (p, v) if p == "opiskeluoikeusPäättynytAikaisintaan" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytAikaisintaan(_))
      case (p, v) if p == "opiskeluoikeusPäättynytViimeistään" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytViimeistään(_))
      case (p, v) if p == "opiskeluoikeusAlkanutAikaisintaan" => dateParam((p, v)).right.map(OpiskeluoikeusAlkanutAikaisintaan(_))
      case (p, v) if p == "opiskeluoikeusAlkanutViimeistään" => dateParam((p, v)).right.map(OpiskeluoikeusAlkanutViimeistään(_))
      case ("opiskeluoikeudenTyyppi", v) => Right(OpiskeluoikeudenTyyppi(koodisto.validateRequired("opiskeluoikeudentyyppi", v)))
      case ("opiskeluoikeudenTila", v) => Right(OpiskeluoikeudenTila(koodisto.validateRequired("koskiopiskeluoikeudentila", v)))
      case ("suorituksenTyyppi", v) => Right(SuorituksenTyyppi(koodisto.validateRequired("suorituksentyyppi", v)))
      case ("suorituksenTila", v) => Right(SuorituksenTila(koodisto.validateRequired("suorituksentila", v)))
      case ("tutkintohaku", hakusana) if hakusana.length < 3 => Left(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
      case ("tutkintohaku", hakusana) => Right(Tutkintohaku(hakusana))
      case ("toimipiste", oid) =>
        OrganisaatioOid.validateOrganisaatioOid(oid).right.flatMap { oid =>
          organisaatiot.getOrganisaatioHierarkia(oid) match {
            case Some(hierarkia) => Right(Toimipiste(hierarkia.flatten))
            case None => Left(KoskiErrorCategory.notFound.oppilaitostaEiLöydy("Oppilaitosta/koulutustoimijaa/toimipistettä ei löydy: " + oid))
          }
        }
      case ("luokkahaku", v) => Right(Luokkahaku(v))
      case ("nimihaku", hakusana) if hakusana.length < 3 => Left(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
      case ("nimihaku", hakusana) => Right(Nimihaku(hakusana))
      case ("suoritusJson", jsonString) => Json.tryParse(jsonString) match {
        case Success(json) => Right(SuoritusJsonHaku(json))
        case Failure(e) => Left(KoskiErrorCategory.badRequest.queryParam("Epävalidi json-dokumentti parametrissa suoritusJson"))
      }
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