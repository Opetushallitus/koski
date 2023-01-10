package fi.oph.koski.opiskeluoikeus
import java.time.format.DateTimeParseException
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter._
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, OrganisaatioOid, OrganisaatioRepository, OrganisaatioService}
import fi.oph.koski.perustiedot.ToimipistePerustiedot
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioWithOid}
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods
import org.scalatra.MultiParams

import scala.util.{Failure, Success, Try}

sealed trait OpiskeluoikeusQueryFilter

object OpiskeluoikeusQueryFilter {
  case class OpiskeluoikeusPäättynytAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeusPäättynytViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeusAlkanutAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeusAlkanutViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeudenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class OneOfOpiskeluoikeudenTyypit(opiskeluoikeudenTyypit: List[OpiskeluoikeudenTyyppi]) extends OpiskeluoikeusQueryFilter
  case class SuorituksenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class NotSuorituksenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class OpiskeluoikeudenTila(tila: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
  case class Tutkintohaku(hakusana: String) extends OpiskeluoikeusQueryFilter
  case class Toimipiste(toimipiste: List[OrganisaatioWithOid]) extends OpiskeluoikeusQueryFilter
  case class VarhaiskasvatuksenToimipiste(toimipiste: List[OrganisaatioWithOid]) extends OpiskeluoikeusQueryFilter
  case class TaiteenPerusopetuksenOppilaitos(oppilaitokset: List[OrganisaatioWithOid]) extends OpiskeluoikeusQueryFilter
  case class Luokkahaku(hakusana: String) extends OpiskeluoikeusQueryFilter
  case class Nimihaku(hakusana: String) extends OpiskeluoikeusQueryFilter
  case class SuoritusJsonHaku(json: JValue) extends OpiskeluoikeusQueryFilter
  case class IdHaku(ids: Seq[Int]) extends OpiskeluoikeusQueryFilter
  case class OppijaOidHaku(oids: Seq[String]) extends OpiskeluoikeusQueryFilter
  case class MuuttunutEnnen(aikaleima: Instant) extends OpiskeluoikeusQueryFilter
  case class MuuttunutJälkeen(aikaleima: Instant) extends OpiskeluoikeusQueryFilter
  case class Poistettu(poistettu: Boolean) extends OpiskeluoikeusQueryFilter

  def parse(params: MultiParams)(implicit koodisto: KoodistoViitePalvelu, organisaatiot: OrganisaatioService, session: KoskiSpecificSession): Either[HttpStatus, List[OpiskeluoikeusQueryFilter]] =
    OpiskeluoikeusQueryFilterParser.parse(params)
}

private object OpiskeluoikeusQueryFilterParser extends Logging {
  def parse(params: MultiParams)(implicit koodisto: KoodistoViitePalvelu, organisaatiot: OrganisaatioService, session: KoskiSpecificSession): Either[HttpStatus, List[OpiskeluoikeusQueryFilter]] = {
    def dateParam(q: (String, String)): Either[HttpStatus, LocalDate] = q match {
      case (p, v) => try {
        Right(LocalDate.parse(v))
      } catch {
        case e: DateTimeParseException => Left(KoskiErrorCategory.badRequest.format.pvm("Invalid date parameter: " + p + "=" + v))
      }
    }

    // Accept both with and without time zone due to legacy reasons
    // (of course, without time zone the conversion is ambiguous when changing to/from DST...)
    def dateTimeParam(q: (String, String)): Either[HttpStatus, Instant] = q match {
      case (p, v) => try {
        Right(Instant.parse(v))
      } catch {
        case e: DateTimeParseException => try {
          Right(LocalDateTime.parse(v).atZone(ZoneId.systemDefault).toInstant)
        } catch {
          case e: DateTimeParseException => Left(KoskiErrorCategory.badRequest.format.pvm("Invalid datetime parameter: " + p + "=" + v))
        }
      }
    }

    val queryFilters: List[Either[HttpStatus, OpiskeluoikeusQueryFilter]] = params.filterNot { case (key, value) => List("sort", "pageSize", "pageNumber", "toimipisteNimi", "v").contains(key) }.map {
      case (p, v +: _) if p == "opiskeluoikeusPäättynytAikaisintaan" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytAikaisintaan)
      case (p, v +: _) if p == "opiskeluoikeusPäättynytViimeistään" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytViimeistään)
      case (p, v +: _) if p == "opiskeluoikeusAlkanutAikaisintaan" => dateParam((p, v)).right.map(OpiskeluoikeusAlkanutAikaisintaan)
      case (p, v +: _) if p == "opiskeluoikeusAlkanutViimeistään" => dateParam((p, v)).right.map(OpiskeluoikeusAlkanutViimeistään)
      case ("opiskeluoikeudenTyyppi", Seq(tyyppi)) => Right(OpiskeluoikeudenTyyppi(koodisto.validateRequired("opiskeluoikeudentyyppi", tyyppi)))
      case ("opiskeluoikeudenTyyppi", tyypit) => Right(OneOfOpiskeluoikeudenTyypit(tyypit.toList.map(tyyppi => OpiskeluoikeudenTyyppi(koodisto.validateRequired("opiskeluoikeudentyyppi", tyyppi)))))
      case ("opiskeluoikeudenTila", v +: _) => Right(OpiskeluoikeudenTila(koodisto.validateRequired("koskiopiskeluoikeudentila", v)))
      case ("suorituksenTyyppi", v +: _) => Right(SuorituksenTyyppi(koodisto.validateRequired("suorituksentyyppi", v)))
      case ("tutkintohaku", hakusana +: _) if hakusana.length < 3 => Left(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
      case ("tutkintohaku", hakusana +: _) => Right(Tutkintohaku(hakusana))
      case ("toimipiste", oid +: _) if oid == organisaatiot.hankintakoulutusRootOid => Right(
        TaiteenPerusopetuksenOppilaitos(
          organisaatiot.koulutustoimijoidenHankintakoulutuksenOrganisaatiot().map(_.toOrganisaatio)
        )
      )
      case ("toimipiste", oid +: _) if oid == organisaatiot.ostopalveluRootOid => organisaatiot.omatOstopalveluOrganisaatiot match {
        case Nil => Left(KoskiErrorCategory.notFound.oppilaitostaEiLöydy("Ostopalvelu/palveluseteli-toimipisteitä ei löydy"))
        case hierarkia => Right(VarhaiskasvatuksenToimipiste(hierarkia.map(_.toOrganisaatio)))
      }
      case ("toimipiste", oid +: _) =>
        OrganisaatioOid.validateOrganisaatioOid(oid).right.flatMap { oid =>
          organisaatiot.organisaatioRepository.getOrganisaatioHierarkia(oid) match {
            case Some(hierarkia) => Right(Toimipiste(hierarkia.flatten))
            case None => Left(KoskiErrorCategory.notFound.oppilaitostaEiLöydy("Oppilaitosta/koulutustoimijaa/toimipistettä ei löydy: " + oid))
          }
        }
      case ("luokkahaku", v +: _) => Right(Luokkahaku(v))
      case ("nimihaku", hakusana +: _) if hakusana.length < 3 => Left(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
      case ("nimihaku", hakusana +: _) => Right(Nimihaku(hakusana))
      case ("suoritusJson", jsonString +: _) => Try(JsonMethods.parse(jsonString)) match {
        case Success(json) => Right(SuoritusJsonHaku(json))
        case Failure(e) => Left(KoskiErrorCategory.badRequest.queryParam("Epävalidi json-dokumentti parametrissa suoritusJson"))
      }
      case (p, v +: _) if p == "muuttunutEnnen" => dateTimeParam((p, v)).right.map(MuuttunutEnnen)
      case (p, v +: _) if p == "muuttunutJälkeen" => dateTimeParam((p, v)).right.map(MuuttunutJälkeen)
      case (p, _) => Left(KoskiErrorCategory.badRequest.queryParam.unknown("Unsupported query parameter: " + p))
      // IdHaku, OppijaOidHaku, OneOfOpiskeluoikeudenTyypit, NotSuorituksenTyyppi, Poistettu missing from here (intentionally)
    }.toList

    queryFilters.partition(_.isLeft) match {
      case (Nil, queries) =>
        Right(queries.flatMap(_.right.toOption))
      case (errors, _) =>
        Left(HttpStatus.fold(errors.map(_.left.get)))
    }
  }
}
