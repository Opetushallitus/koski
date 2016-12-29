package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{Http, HttpStatus, HttpStatusException, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{AccessType, KoskiSession, RequiresAuthentication}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException}
import fi.oph.koski.util.{ListPagination, PaginatedResponse, Pagination, PaginationSettings}
import fi.oph.scalaschema.annotation.Description
import OpiskeluoikeusQueryFilter._
import com.sksamuel.elastic4s.ElasticClient
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusSortOrder.{Ascending, Descending}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.searches.RichSearchResponse
import com.sksamuel.elastic4s.searches.sort.FieldSortDefinition
import fi.oph.koski.json.{Json, Json4sHttp4s}
import org.json4s.JValue

import scala.concurrent.Future

case class OpiskeluoikeudenPerustiedot(
  henkilö: NimitiedotJaOid,
  oppilaitos: Oppilaitos,
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  alkamispäivä: Option[LocalDate],
  tyyppi: Koodistokoodiviite,
  suoritukset: List[SuorituksenPerustiedot],
  @KoodistoUri("virtaopiskeluoikeudentila")
  @KoodistoUri("koskiopiskeluoikeudentila")
  tila: Koodistokoodiviite,
  @Description("Luokan tai ryhmän tunniste, esimerkiksi 9C")
  luokka: Option[String]
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
  toimipiste: OrganisaatioWithOid
)

case class KoulutusmoduulinPerustiedot(
  tunniste: KoodiViite
)

object KoulutusmoduulinPerustiedot {

}

class OpiskeluoikeudenPerustiedotRepository(henkilöRepository: HenkilöRepository, opiskeluoikeusRepository: OpiskeluoikeusQueryService, koodisto: KoodistoViitePalvelu, es: ElasticClient) {
  def find(filters: List[OpiskeluoikeusQueryFilter], sorting: OpiskeluoikeusSortOrder, pagination: PaginationSettings)(implicit session: KoskiSession): List[OpiskeluoikeudenPerustiedot] = {
    import Http._

    val elasticSort = List(
      Map("henkilö.sukunimi.keyword" -> "asc"),
      Map("henkilö.etunimet.keyword" -> "asc")
    )

    val elasticFilters = filters.flatMap {
      case OpiskeluoikeusQueryFilter.Nimihaku(hakusana) => hakusana.trim.split(" ").map { namePrefix =>
        Map("bool" -> Map("should" -> List(
          Map("prefix" -> Map("henkilö.sukunimi" -> namePrefix)),
          Map("prefix" -> Map("henkilö.etunimet" -> namePrefix))
        )))
      }
    } ++ (if (session.hasGlobalReadAccess) { Nil } else { List(Map("terms" -> Map("oppilaitos.oid" -> session.organisationOids(AccessType.read))))})

    val elasticQuery = elasticFilters match {
      case Nil => Map.empty
      case _ => Map(
        "bool" -> Map(
          "must" -> List(
            elasticFilters
          )
        )
      )
    }


    val doc = Json.toJValue(Map(
      "query" -> elasticQuery,
      "sort" -> elasticSort,
      "from" -> pagination.page * pagination.size,
      "size" -> pagination.size
    ))

    implicit val formats = Json.jsonFormats
    val response = Http.runTask(Http("http://localhost:9200").post(uri"/koski/_search", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])) // TODO: hardcoded url
    (response \ "hits" \ "hits").extract[List[JValue]].map(j => (j \ "_source").extract[OpiskeluoikeudenPerustiedot])

  }
}

class OpiskeluoikeudenPerustiedotServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Pagination {
  private val repository = new OpiskeluoikeudenPerustiedotRepository(application.henkilöRepository, application.opiskeluoikeusQueryRepository, application.koodistoViitePalvelu, application.es)
  get("/") {
    renderEither({
      val sort = params.get("sort").map {
        str => str.split(":") match {
          case Array(key: String, "asc") => Ascending(key)
          case Array(key: String, "desc") => Descending(key)
          case xs => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Invalid sort param. Expected key:asc or key: desc"))
        }
      }.getOrElse(Ascending("nimi"))

      OpiskeluoikeusQueryFilter.parse(params.toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
        case Right(filters) =>
          val result: List[OpiskeluoikeudenPerustiedot] = repository.find(filters, sort, paginationSettings)(koskiSession)
          Right(PaginatedResponse(Some(paginationSettings), result, result.length))
        case Left(HttpStatus(404, _)) =>
          Right(PaginatedResponse(None, List[OpiskeluoikeudenPerustiedot](), 0))
        case e @ Left(_) => e
      }
    })
  }
}