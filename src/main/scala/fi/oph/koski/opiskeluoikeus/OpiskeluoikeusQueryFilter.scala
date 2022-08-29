package fi.oph.koski.opiskeluoikeus
import fi.oph.koski.db.KoskiTables.{HenkilöTable, OpiskeluoikeusTable}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter._
import fi.oph.koski.organisaatio.{OrganisaatioOid, OrganisaatioService}
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioWithOid}
import fi.oph.koski.servlet.InvalidRequestException
import org.json4s.JsonAST.JValue
import org.json4s.jackson.{JsonMethods, parseJson}
import org.scalatra.MultiParams
import slick.lifted.{CanBeQueryCondition, Query, Rep}

import java.sql.{Date, Timestamp}
import java.time.format.DateTimeParseException
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import scala.util.{Failure, Success, Try}

sealed trait OpiskeluoikeusQueryFilterBase {
  def composeWith(query: QueryType)(implicit wt: CanBeQueryCondition[Rep[Option[Boolean]]]): QueryType
  def requiresHenkilötiedot: Boolean = false
}

trait OpiskeluoikeusQueryFilter extends OpiskeluoikeusQueryFilterBase {
  def composeWith(query: QueryType)(implicit wt: CanBeQueryCondition[Rep[Option[Boolean]]]): QueryType =
    query.filter(f => predicate(f._1))

  def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]]
}

trait HenkilöQueryFilter extends OpiskeluoikeusQueryFilterBase {
  override def requiresHenkilötiedot: Boolean = true

  def composeWith(query: QueryType)(implicit wt: CanBeQueryCondition[Rep[Option[Boolean]]]): QueryType =
    query.filter(f => predicate(f._2, f._3))

  def predicate(henkilö: HenkilöTable, slave: Rep[Option[HenkilöTable]]): Rep[Option[Boolean]]
}

trait UnimplementedOpiskeluoikeusQueryFilter extends OpiskeluoikeusQueryFilterBase {
  override def composeWith(query: QueryType)(implicit wt: CanBeQueryCondition[Rep[Option[Boolean]]]): QueryType =
    throw new InvalidRequestException(KoskiErrorCategory.internalError("Hakua ei ole toteutettu: " + this.getClass.getName))
}

object OpiskeluoikeusQueryFilter {
  type Tables = (OpiskeluoikeusTable, HenkilöTable, Rep[Option[HenkilöTable]])
  type Rows = (OpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])
  type QueryType = Query[Tables, Rows, Seq]

  def compose(
    baseQuery: QueryType,
    filters: Seq[OpiskeluoikeusQueryFilterBase]
  ): QueryType = {
    filters.foldLeft(baseQuery)((query, filter) => filter.composeWith(query))
  }

  case class OpiskeluoikeusPäättynytAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.päättymispäivä >= Date.valueOf(päivä)
  }

  case class OpiskeluoikeusPäättynytViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.päättymispäivä <= Date.valueOf(päivä)
  }

  case class OpiskeluoikeusAlkanutAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.alkamispäivä.? >= Date.valueOf(päivä)
  }

  case class OpiskeluoikeusAlkanutViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.alkamispäivä.? <= Date.valueOf(päivä)
  }

  case class OpiskeluoikeudenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.koulutusmuoto.? === tyyppi.koodiarvo
  }

  case class OneOfOpiskeluoikeudenTyypit(opiskeluoikeudenTyypit: List[OpiskeluoikeudenTyyppi]) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.koulutusmuoto.? inSet opiskeluoikeudenTyypit.map(_.tyyppi.koodiarvo)
  }

  case class SuorituksenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.suoritustyypit.?.@>(List(tyyppi.koodiarvo))
  }

  case class NotSuorituksenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.suoritustyypit.?.@>(List(tyyppi.koodiarvo))
  }

  case class OpiskeluoikeudenTila(tila: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.data.?.#>>(List("tila", "opiskeluoikeusjaksot", "-1", "tila", "koodiarvo")) === tila.koodiarvo
  }

  case class Tutkintohaku(hakusana: String) extends UnimplementedOpiskeluoikeusQueryFilter

  case class Toimipiste(toimipisteet: List[OrganisaatioWithOid]) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] = {
      val matchers = toimipisteet.map { toimipiste =>
        parseJson(s"""[{"toimipiste":{"oid": "${toimipiste.oid}"}}]""")
      }
      opiskeluoikeus.data.?.+>("suoritukset").@>(matchers.bind.any)
    }
  }

  case class VarhaiskasvatuksenToimipiste(toimipiste: List[OrganisaatioWithOid]) extends UnimplementedOpiskeluoikeusQueryFilter

  case class Luokkahaku(hakusana: String) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.luokka ilike (hakusana + "%")
  }

  case class Nimihaku(hakusana: String) extends UnimplementedOpiskeluoikeusQueryFilter

  case class SuoritusJsonHaku(json: JValue) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.data.?.+>("suoritukset").@>(json)
  }

  case class IdHaku(ids: Seq[Int]) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.id.? inSetBind ids
  }

  case class OppijaOidHaku(oids: Seq[String]) extends HenkilöQueryFilter {
    def predicate(henkilö: HenkilöTable, slave: Rep[Option[HenkilöTable]]): Rep[Option[Boolean]] =
      (henkilö.oid inSetBind oids) || (slave.map(s => s.oid) inSetBind oids)
  }

  case class MuuttunutEnnen(aikaleima: Instant) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.aikaleima.? < Timestamp.from(aikaleima)
  }

  case class MuuttunutJälkeen(aikaleima: Instant) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.aikaleima.? >= Timestamp.from(aikaleima)
  }

  case class Poistettu(poistettu: Boolean) extends OpiskeluoikeusQueryFilter {
    def predicate(opiskeluoikeus: OpiskeluoikeusTable): Rep[Option[Boolean]] =
      opiskeluoikeus.poistettu.? === poistettu
  }

  def parse(params: MultiParams)(implicit koodisto: KoodistoViitePalvelu, organisaatiot: OrganisaatioService, session: KoskiSpecificSession): Either[HttpStatus, List[OpiskeluoikeusQueryFilterBase]] =
    OpiskeluoikeusQueryFilterParser.parse(params)
}

private object OpiskeluoikeusQueryFilterParser extends Logging {

  def parse(params: MultiParams)(implicit koodisto: KoodistoViitePalvelu, organisaatiot: OrganisaatioService, session: KoskiSpecificSession): Either[HttpStatus, List[OpiskeluoikeusQueryFilterBase]] = {
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

    val queryFilters: List[Either[HttpStatus, OpiskeluoikeusQueryFilterBase]] = params.filterNot { case (key, value) => List("sort", "pageSize", "pageNumber", "toimipisteNimi", "v").contains(key) }.map {
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
