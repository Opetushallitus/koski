package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import com.sksamuel.elastic4s.embedded.LocalNode
import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{Http, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.{Json, Json4sHttp4s}
import fi.oph.koski.koskiuser.{AccessType, KoskiSession, RequiresAuthentication}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusSortOrder.{Ascending, Descending}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException}
import fi.oph.koski.util.{PaginatedResponse, Pagination, PaginationSettings}
import fi.oph.scalaschema.annotation.Description
import org.elasticsearch.common.settings.Settings
import org.json4s.JValue

case class OpiskeluoikeudenPerustiedot(
  id: Option[Int], // TODO: remove optionality
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

class OpiskeluoikeudenPerustiedotRepository(config: Config) extends Logging {
  import Http._

  private val url = config.getString("elasticsearch.url")
  private val elasticSearchHttp = Http(url)

  def find(filters: List[OpiskeluoikeusQueryFilter], sorting: OpiskeluoikeusSortOrder, pagination: PaginationSettings)(implicit session: KoskiSession): List[OpiskeluoikeudenPerustiedot] = {
    def nimi(order: String) = List(
      Map("henkilö.sukunimi.keyword" -> order),
      Map("henkilö.etunimet.keyword" -> order)
    )
    def luokka(order: String) = Map("luokka" -> order) :: nimi(order)
    def alkamispäivä(order: String) = Map("alkamispäivä" -> order):: nimi(order)

    val elasticSort = sorting match {
      case Ascending("nimi") => nimi("asc")
      case Ascending("luokka") => luokka("asc")
      case Ascending("alkamispäivä") => alkamispäivä("asc")
      case Descending("nimi") => nimi("desc")
      case Descending("luokka") => luokka("desc")
      case Descending("alkamispäivä") => alkamispäivä("desc")
      case _ => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Epäkelpo järjestyskriteeri: " + sorting.field))
    }

    val elasticFilters = filters.flatMap {
      case Nimihaku(hakusana) => hakusana.trim.split(" ").map { namePrefix =>
        Map("bool" -> Map("should" -> List(
          Map("prefix" -> Map("henkilö.sukunimi" -> namePrefix)),
          Map("prefix" -> Map("henkilö.etunimet" -> namePrefix))
        )))
      }
      case Luokkahaku(hakusana) => hakusana.trim.split(" ").map { prefix =>
        Map("prefix" -> Map("luokka" -> prefix))
      }
      case SuorituksenTyyppi(tyyppi) => List(Map("term" -> Map("suoritukset.tyyppi.koodiarvo" -> tyyppi.koodiarvo)))
      case OpiskeluoikeudenTyyppi(tyyppi) => List(Map("term" -> Map("tyyppi.koodiarvo" -> tyyppi.koodiarvo)))
      case OpiskeluoikeudenTila(tila) => List(Map("term" -> Map("tila.koodiarvo" -> tila.koodiarvo)))
      case Tutkintohaku(koulutukset, osaamisalat, nimikkeet) => List(Map("bool" -> Map("should" ->
        (koulutukset.map{ koulutus => Map("term" -> Map("suoritukset.koulutusmoduuli.tunniste.koodiarvo" -> koulutus.koodiarvo))} ++
          osaamisalat.map{ ala => Map("term" -> Map("suoritukset.koulutusmoduuli.osaamisala.koodiarvo" -> ala.koodiarvo))} ++
          nimikkeet.map{ nimike => Map("term" -> Map("suoritukset.koulutusmoduuli.tutkintonimike.koodiarvo" -> nimike.koodiarvo))}
        )
      )))
      case OpiskeluoikeusQueryFilter.Toimipiste(toimipisteet) => List(Map("bool" -> Map("should" ->
        toimipisteet.map{ toimipiste => Map("term" -> Map("suoritukset.toimipiste.oid" -> toimipiste.oid))}
      )))
      case SuorituksenTila(tila) => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("suorituksenTila-parametriä ei tueta"))
      case SuoritusJsonHaku(json) => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("suoritusJson-parametriä ei tueta"))
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
    val response = Http.runTask(elasticSearchHttp.post(uri"/koski/_search", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])) // TODO: hardcoded url
    (response \ "hits" \ "hits").extract[List[JValue]].map(j => (j \ "_source").extract[OpiskeluoikeudenPerustiedot])
  }

  def update(perustiedot: OpiskeluoikeudenPerustiedot): Unit = {
    implicit val formats = Json.jsonFormats
    val doc = Json.toJValue(Map("doc_as_upsert" -> true, "doc" -> perustiedot))

    val response = Http.runTask(elasticSearchHttp
      .post(uri"/koski/perustiedot/${perustiedot.id.get}/_update", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])) // TODO: hardcoded url

    val success: Int = (response \ "_shards" \ "successful").extract[Int]

    if (success < 1) {
      logger.error("Elasticsearch indexing failed (success count < 1)")
    }
  }

  val es = {
    if (url.startsWith("http://localhost:9200")) {
      logger.info("Starting embedded elasticsearch node")
      val settings = Settings.builder()
        .put("cluster.name", "elasticsearch")
        .put("path.home", ".")
        .put("path.data", "elastic-data")
        .put("path.repo", "elastic-repo")
        .build()
      val node = LocalNode(settings)
    }
  }

}

class OpiskeluoikeudenPerustiedotServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Pagination {

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
          val result: List[OpiskeluoikeudenPerustiedot] = application.perustiedotRepository.find(filters, sort, paginationSettings)(koskiSession)
          Right(PaginatedResponse(Some(paginationSettings), result, result.length))
        case Left(HttpStatus(404, _)) =>
          Right(PaginatedResponse(None, List[OpiskeluoikeudenPerustiedot](), 0))
        case e @ Left(_) => e
      }
    })
  }
}