package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluOikeusRow
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, HttpStatusException, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{KoskiSession, RequiresAuthentication}
import fi.oph.koski.oppija.ReportingQueryFacade
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException}
import fi.oph.koski.util.{ListPagination, PaginatedResponse, Pagination, PaginationSettings}
import fi.oph.scalaschema.annotation.Description
import OpiskeluoikeusQueryFilter._

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

class OpiskeluoikeudenPerustiedotRepository(henkilöRepository: HenkilöRepository, opiskeluOikeusRepository: OpiskeluOikeusRepository, koodisto: KoodistoViitePalvelu) {
  import HenkilöOrdering.aakkostettu

  def find(filters: List[OpiskeluoikeusQueryFilter], sorting: SortCriterion, pagination: PaginationSettings)(implicit session: KoskiSession): List[OpiskeluoikeudenPerustiedot] = {
    val databaseFilters = filters.filter {
      case Nimihaku(_) => false
      case f => true
    }
    val opiskeluoikeudetObservable = ReportingQueryFacade(henkilöRepository, opiskeluOikeusRepository, koodisto).findOppijat(databaseFilters, session)

    val opiskeluoikeudet: List[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = opiskeluoikeudetObservable.take(1000).toBlocking.toList

    val perustiedot: List[OpiskeluoikeudenPerustiedot] = opiskeluoikeudet.flatMap {
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
    }

    ListPagination.applyPagination(pagination, applySorting(sorting, applyFiltering(filters, perustiedot))).toList
  }

  def applyFiltering(filters: List[OpiskeluoikeusQueryFilter], list: List[OpiskeluoikeudenPerustiedot])(implicit session: KoskiSession) = {
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
      case (list, Nimihaku(hakusana)) => list.filter(_.henkilö.kokonimi.toLowerCase.contains(hakusana.toLowerCase))
      case (list, _) => list
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
  private val repository = new OpiskeluoikeudenPerustiedotRepository(application.oppijaRepository, application.opiskeluOikeusRepository, application.koodistoViitePalvelu)
  get("/") {
    renderEither({
      val filters = params.toList.flatMap {
        case (key, _) if List("sort", "pageSize", "pageNumber").contains(key) => None
        case (key, value) => Some((key, value))
      }

      val sort = params.get("sort").map {
        str => str.split(":") match {
          case Array(key: String, "asc") => Ascending(key)
          case Array(key: String, "desc") => Descending(key)
          case xs => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Invalid sort param. Expected key:asc or key: desc"))
        }
      }.getOrElse(Ascending("nimi"))

      OpiskeluoikeusQueryFilter.parseQueryFilter(filters)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession).right.map { filters =>
        val result: List[OpiskeluoikeudenPerustiedot] = repository.find(filters, sort, paginationSettings)(koskiSession)
        PaginatedResponse(Some(paginationSettings), result, result.length)
      }
    })
  }
}