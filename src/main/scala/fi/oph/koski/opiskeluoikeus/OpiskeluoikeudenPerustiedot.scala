package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, HttpStatusException, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiSession, RequiresAuthentication}
import fi.oph.koski.oppija.ReportingQueryFacade
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException}
import fi.oph.koski.util.{ListPagination, PaginatedResponse, Pagination, PaginationSettings}
import fi.oph.scalaschema.annotation.Description

case class OpiskeluoikeudenPerustiedot(
  henkilö: NimitiedotJaOid,
  oppilaitos: Oppilaitos,
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  alkamispäivä: Option[LocalDate],
  tyyppi: Koodistokoodiviite,
  suoritukset: List[SuorituksenPerustiedot],
  @KoodistoUri("virtaopiskeluoikeudentila")
  @KoodistoUri("koskiopiskeluoikeudentila")
  tila: Koodistokoodiviite
)

case class SuorituksenPerustiedot(
  @Description("Suorituksen tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) ja eri tasoihin (tutkinto, tutkinnon osa, kurssi, oppiaine...) liittyvät suoritukset")
  @KoodistoUri("suorituksentyyppi")
  @Hidden
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: KoulutusmoduulinPerustiedot,
  @Description("Tieto siitä mihin osaamisalaan/osaamisaloihin oppijan tutkinto liittyy")
  @KoodistoUri("osaamisala")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  osaamisala: Option[List[Koodistokoodiviite]] = None,
  @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
  @KoodistoUri("tutkintonimikkeet")
  @OksaUri("tmpOKSAID588", "tutkintonimike")
  tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  toimipiste: OrganisaatioWithOid,
  @Description("Luokan tai ryhmän tunniste, esimerkiksi 9C")
  luokka: Option[String]
)

case class KoulutusmoduulinPerustiedot(
  tunniste: KoodiViite
)

object KoulutusmoduulinPerustiedot {

}

trait SortCriterion {
  def field: String
}
case class Ascending(field: String) extends SortCriterion
case class Descending(field: String) extends SortCriterion

case class FilterCriterion(field: String, value: String)

class OpiskeluoikeudenPerustiedotRepository(henkilöRepository: HenkilöRepository, opiskeluOikeusRepository: OpiskeluOikeusRepository) {
  import HenkilöOrdering.aakkostettu

  def findAll(filters: List[FilterCriterion], sorting: SortCriterion, pagination: PaginationSettings)(implicit session: KoskiSession): Either[HttpStatus, List[OpiskeluoikeudenPerustiedot]] = {
    ReportingQueryFacade(henkilöRepository, opiskeluOikeusRepository).findOppijat(Nil, session).right.map { opiskeluoikeudetObservable =>
      ListPagination.paged(pagination, applySorting(sorting, applyFiltering(filters, opiskeluoikeudetObservable.take(1000).toBlocking.toList.flatMap {
        case (henkilö, rivit) => rivit.map { rivi =>
          val oo = rivi.toOpiskeluOikeus
          OpiskeluoikeudenPerustiedot(henkilö.nimitiedotJaOid, oo.oppilaitos, oo.alkamispäivä, oo.tyyppi, oo.suoritukset.map { suoritus =>
            val (osaamisala, tutkintonimike) = suoritus match {
              case s: AmmatillisenTutkinnonSuoritus => (s.osaamisala, s.tutkintonimike)
              case s: NäyttötutkintoonValmistavanKoulutuksenSuoritus => (s.osaamisala, s.tutkintonimike)
              case _ => (None, None)
            }
            val ryhmä = suoritus match {
              case s: PerusopetuksenVuosiluokanSuoritus => Some(s.luokka)
              case _ => None
            }
            SuorituksenPerustiedot(suoritus.tyyppi, KoulutusmoduulinPerustiedot(suoritus.koulutusmoduuli.tunniste), osaamisala, tutkintonimike, suoritus.toimipiste, ryhmä)
          }, oo.tila.opiskeluoikeusjaksot.last.tila)
        }
      }))).toList
    }
  }

  def applyFiltering(filters: List[FilterCriterion], list: List[OpiskeluoikeudenPerustiedot])(implicit session: KoskiSession) = {
    def filterBySuoritukset(list: List[OpiskeluoikeudenPerustiedot], f: SuorituksenPerustiedot => Boolean): List[OpiskeluoikeudenPerustiedot] = {
      list.flatMap { opiskeluoikeus =>
        val suoritukset = opiskeluoikeus.suoritukset.filter(f)
        suoritukset match {
          case Nil => None
          case _ => Some(opiskeluoikeus.copy(suoritukset = suoritukset))
        }
      }
    }
    filters.foldLeft(list) {
      case (list, FilterCriterion("nimi", hakusana)) => list.filter(_.henkilö.kokonimi.toLowerCase.contains(hakusana.toLowerCase))
      case (list, FilterCriterion("tyyppi", tyyppi)) => list.filter(_.tyyppi.koodiarvo == tyyppi)
      case (list, FilterCriterion("suoritus.tyyppi", tyyppi)) => filterBySuoritukset(list, _.tyyppi.koodiarvo == tyyppi)
      case (list, FilterCriterion("tutkinto", hakusana)) => filterBySuoritukset(list, { suoritus =>
        val koulutusmoduuli: String = suoritus.koulutusmoduuli.tunniste.getNimi.map(_.get(session.lang)).getOrElse("")
        val osaamisala: List[String] = suoritus.osaamisala.toList.flatten.flatMap(_.nimi.map(_.get(session.lang)))
        val tutkintonimike: List[String] = suoritus.tutkintonimike.toList.flatten.flatMap(_.nimi.map(_.get(session.lang)))
        (koulutusmoduuli :: osaamisala :: tutkintonimike).mkString("||").toLowerCase.contains(hakusana.toLowerCase)
      })
      case (list, FilterCriterion("tila", tila)) => list.filter(_.tila.koodiarvo == tila)
      case (list, FilterCriterion("oppilaitos", oppilaitos)) => list.filter(_.oppilaitos.oid == oppilaitos)
      case (list, FilterCriterion("luokka", hakusana)) => filterBySuoritukset(list, _.luokka.getOrElse("").toLowerCase.contains(hakusana.toLowerCase))
      case (list, FilterCriterion(key, _)) => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Invalid filter key " + key))
    }
  }

  def applySorting(sortCriterion: SortCriterion, list: List[OpiskeluoikeudenPerustiedot]) = {
    import fi.oph.koski.date.DateOrdering._
    def ordering(field: String): Ordering[OpiskeluoikeudenPerustiedot] = field match {
      case "nimi" => Ordering.by(_.henkilö.nimitiedot)
      case "alkamispäivä" => Ordering.by(_.alkamispäivä)
      case "luokka" => Ordering.by(_.suoritukset.headOption.flatMap(_.luokka))
      case _ => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Invalid sort criterion: " + field))
    }
    list.sorted(sortCriterion match {
      case Ascending(field) => ordering(field)
      case Descending(field) => ordering(field).reverse
    })
  }
}

class OpiskeluoikeudenPerustiedotServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Pagination {
  get("/") {
    renderEither({
      val filters = params.toList.flatMap {
        case (key, _) if List("sort", "pageSize", "pageNumber").contains(key) => None
        case (key, value) => Some(FilterCriterion(key, value))
      }

      val sort = params.get("sort").map {
        str => str.split(":") match {
          case Array(key: String, "asc") => Ascending(key)
          case Array(key: String, "desc") => Descending(key)
          case xs => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Invalid sort param. Expected key:asc or key: desc"))
        }
      }.getOrElse(Ascending("nimi"))
      new OpiskeluoikeudenPerustiedotRepository(application.oppijaRepository, application.opiskeluOikeusRepository).findAll(filters, sort, paginationSettings)(koskiSession).right.map { result =>
        PaginatedResponse(Some(paginationSettings), result, result.length)
      }
    })
  }
}