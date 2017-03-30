package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.elasticsearch.ElasticSearchRunner
import fi.oph.koski.henkilo.TestingException
import fi.oph.koski.http.{Http, HttpStatus, HttpStatusException, KoskiErrorCategory}
import fi.oph.koski.json.{GenericJsonFormats, Json, Json4sHttp4s, LocalDateSerializer}
import fi.oph.koski.koskiuser.{AccessType, KoskiSession, RequiresAuthentication}
import fi.oph.koski.localization.LocalizedStringDeserializer
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter._
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, ObservableSupport}
import fi.oph.koski.util.SortOrder.{Ascending, Descending}
import fi.oph.koski.util._
import fi.oph.scalaschema.annotation.Description
import org.http4s.EntityEncoder
import org.json4s.{JArray, JValue}

import scala.concurrent.Future

trait WithId {
  def id: Int
}

case class OpiskeluoikeudenPerustiedot(
  id: Int,
  henkilö: NimitiedotJaOid,
  oppilaitos: Oppilaitos,
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  tyyppi: Koodistokoodiviite,
  suoritukset: List[SuorituksenPerustiedot],
  @KoodistoUri("virtaopiskeluoikeudentila")
  @KoodistoUri("koskiopiskeluoikeudentila")
  tila: Koodistokoodiviite,
  @Description("Luokan tai ryhmän tunniste, esimerkiksi 9C")
  luokka: Option[String]
) extends WithId

case class Henkilötiedot(id: Int, henkilö: NimitiedotJaOid) extends WithId

object OpiskeluoikeudenPerustiedot {
  implicit val formats = GenericJsonFormats.genericFormats + LocalDateSerializer + LocalizedStringDeserializer + KoodiViiteDeserializer

  def makePerustiedot(row: OpiskeluoikeusRow, henkilöRow: HenkilöRow): OpiskeluoikeudenPerustiedot = {
    makePerustiedot(row.id, row.data, row.luokka, henkilöRow.toNimitiedotJaOid)
  }

  def makePerustiedot(id: Int, oo: Opiskeluoikeus, henkilö: NimitiedotJaOid): OpiskeluoikeudenPerustiedot = {
    makePerustiedot(id, Json.toJValue(oo), oo.luokka, henkilö)
  }

  def makePerustiedot(id: Int, data: JValue, luokka: Option[String], henkilö: NimitiedotJaOid): OpiskeluoikeudenPerustiedot = {
    val suoritukset: List[SuorituksenPerustiedot] = (data \ "suoritukset").asInstanceOf[JArray].arr
      .map { suoritus =>
        SuorituksenPerustiedot(
          (suoritus \ "tyyppi").extract[Koodistokoodiviite],
          KoulutusmoduulinPerustiedot((suoritus \ "koulutusmoduuli" \ "tunniste").extract[Koodistokoodiviite]), // TODO: voi olla paikallinen koodi
          (suoritus \ "osaamisala").extract[Option[List[Koodistokoodiviite]]],
          (suoritus \ "tutkintonimike").extract[Option[List[Koodistokoodiviite]]],
          (suoritus \ "toimipiste").extract[OidOrganisaatio]
        )
      }
      .filter(_.tyyppi.koodiarvo != "perusopetuksenvuosiluokka")
    OpiskeluoikeudenPerustiedot(
      id,
      henkilö,
      (data \ "oppilaitos").extract[Oppilaitos],
      (data \ "alkamispäivä").extract[Option[LocalDate]],
      (data \ "päättymispäivä").extract[Option[LocalDate]],
      (data \ "tyyppi").extract[Koodistokoodiviite],
      suoritukset,
      ((data \ "tila" \ "opiskeluoikeusjaksot").asInstanceOf[JArray].arr.last \ "tila").extract[Koodistokoodiviite],
      luokka)
  }
}

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
  toimipiste: OidOrganisaatio
)

case class KoulutusmoduulinPerustiedot(
  tunniste: KoodiViite
)

object KoulutusmoduulinPerustiedot {

}

class OpiskeluoikeudenPerustiedotRepository(config: Config, opiskeluoikeusQueryService: OpiskeluoikeusQueryService) extends Logging with GlobalExecutionContext {
  import Http._
  import OpiskeluoikeudenPerustiedot.formats
  private val host = config.getString("elasticsearch.host")
  private val port = config.getInt("elasticsearch.port")
  private val url = s"http://$host:$port"
  private val elasticSearchHttp = Http(url)

  def find(filters: List[OpiskeluoikeusQueryFilter], sorting: SortOrder, pagination: PaginationSettings)(implicit session: KoskiSession): List[OpiskeluoikeudenPerustiedot] = {
    if (filters.find(_.isInstanceOf[SuoritusJsonHaku]).isDefined) {
      // JSON queries go to PostgreSQL
      opiskeluoikeusQueryService.streamingQuery(filters, Some(sorting), Some(pagination)).toList.toBlocking.last.map {
        case (opiskeluoikeusRow, henkilöRow) =>  OpiskeluoikeudenPerustiedot.makePerustiedot(opiskeluoikeusRow, henkilöRow)
      }
    } else {
      // Other queries got to ElasticSearch
      findFromElastic(filters, sorting, pagination)
    }
  }

  def findFromElastic(filters: List[OpiskeluoikeusQueryFilter], sorting: SortOrder, pagination: PaginationSettings)(implicit session: KoskiSession): List[OpiskeluoikeudenPerustiedot] = {
    def nimi(order: String) = List(
      Map("henkilö.sukunimi.keyword" -> order),
      Map("henkilö.etunimet.keyword" -> order)
    )
    def luokka(order: String) = Map("luokka.keyword" -> order) :: nimi(order)
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
      case Nimihaku(hakusana) => nameFilter(hakusana)
      case Luokkahaku(hakusana) => hakusana.trim.split(" ").map(_.toLowerCase).map { prefix =>
        Map("prefix" -> Map("luokka" -> prefix))
      }
      case SuorituksenTyyppi(tyyppi) => List(Map("term" -> Map("suoritukset.tyyppi.koodiarvo" -> tyyppi.koodiarvo)))
      case OpiskeluoikeudenTyyppi(tyyppi) => List(Map("term" -> Map("tyyppi.koodiarvo" -> tyyppi.koodiarvo)))
      case OpiskeluoikeudenTila(tila) => List(Map("term" -> Map("tila.koodiarvo" -> tila.koodiarvo)))
      case Tutkintohaku(hakusana) =>
        analyzeString(hakusana).map { namePrefix =>
          Map("bool" -> Map("should" -> List(
            Map("prefix" -> Map(s"suoritukset.koulutusmoduuli.tunniste.nimi.${session.lang}" -> namePrefix)),
            Map("prefix" -> Map(s"suoritukset.osaamisala.nimi.${session.lang}" -> namePrefix)),
            Map("prefix" -> Map(s"suoritukset.tutkintonimike.nimi.${session.lang}" -> namePrefix))
          )))
        }
      case OpiskeluoikeusQueryFilter.Toimipiste(toimipisteet) => List(Map("bool" -> Map("should" ->
        toimipisteet.map{ toimipiste => Map("term" -> Map("suoritukset.toimipiste.oid" -> toimipiste.oid))}
      )))
      case OpiskeluoikeusAlkanutAikaisintaan(day) =>
        Map("range" -> Map("alkamispäivä" -> Map("gte" -> day, "format" -> "yyyy-MM-dd")))
      case OpiskeluoikeusAlkanutViimeistään(day) =>
        Map("range" -> Map("alkamispäivä" -> Map("lte" -> day, "format" -> "yyyy-MM-dd")))
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

    runSearch(doc)
      .map(response => (response \ "hits" \ "hits").extract[List[JValue]].map(j => (j \ "_source").extract[OpiskeluoikeudenPerustiedot]))
      .getOrElse(Nil)
  }

  def findHenkiloPerustiedotByOids(oids: List[String]): List[OpiskeluoikeudenPerustiedot] = {
    val doc = Json.toJValue(Map("query" -> Map("terms" -> Map("henkilö.oid" -> oids)), "from" -> 0, "size" -> 10000))
    runSearch(doc)
      .map(response => (response \ "hits" \ "hits").extract[List[JValue]].map(j => (j \ "_source").extract[OpiskeluoikeudenPerustiedot]))
      .getOrElse(Nil)
  }

  def findHenkilöPerustiedot(oid: String): Option[NimitiedotJaOid] = {
    val doc = Json.toJValue(Map("query" -> Map("term" -> Map("henkilö.oid" -> oid))))

    refreshIndex
    runSearch(doc)
      .flatMap(response => (response \ "hits" \ "hits").extract[List[JValue]].map(j => (j \ "_source" \ "henkilö").extract[NimitiedotJaOid]).headOption)
  }

  def findOids(hakusana: String): List[Oid] = {
    if (hakusana == "") {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
    }
    if (hakusana == "#error#") {
      throw new TestingException("Testing error handling")
    }

    val doc = Json.toJValue(Map(
      "_source" -> "henkilö.oid",
      "query" -> Map("bool" -> Map("must" -> List(nameFilter(hakusana)))),
      "aggregations" -> Map("oids" -> Map("terms" -> Map("field" -> "henkilö.oid.keyword")))
    ))

    runSearch(doc)
      .map(response => (response \ "aggregations" \ "oids" \ "buckets").extract[List[JValue]].map(j => (j \ "key").extract[Oid]))
      .getOrElse(Nil)
  }

  def refreshIndex =
    Http.runTask(elasticSearchHttp.post(uri"/koski/_refresh", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))

  /**
    * Update info to Elasticsearch. Return error status or a boolean indicating whether data was changed.
    */
  def update(perustiedot: OpiskeluoikeudenPerustiedot): Either[HttpStatus, Int] = updateBulk(List(perustiedot), insertMissing = true)

  def updateBulk(items: Seq[WithId], insertMissing: Boolean): Either[HttpStatus, Int] = {
    if (items.isEmpty) {
      return Right(0)
    }
    val jsonLines = items.flatMap { perustiedot =>
      List(
        Map("update" -> Map("_id" -> perustiedot.id, "_index" -> "koski", "_type" -> "perustiedot")),
        Map("doc_as_upsert" -> insertMissing, "doc" -> perustiedot)
      )
    }
    val response = Http.runTask(elasticSearchHttp.post(uri"/koski/_bulk", jsonLines)(Json4sHttp4s.multiLineJson4sEncoderOf[Map[String, Any]])(Http.parseJson[JValue]))
    val errors = (response \ "errors").extract[Boolean]
    if (errors) {
      val msg = s"Elasticsearch indexing failed for some of ids ${items.map(_.id)}"
      logger.error(msg)
      Left(KoskiErrorCategory.internalError(msg))
    } else {
      val itemResults = (response \ "items").extract[List[JValue]].map(_ \ "update" \ "_shards" \ "successful").map(_.extract[Int])
      Right(itemResults.sum)
    }
  }

  def deleteByOppijaOids(oids: List[Oid]) = {
    val doc = Json.toJValue(Map("query" -> Map("bool" -> Map("should" -> Map("terms" -> Map("henkilö.oid" -> oids))))))

    val deleted = Http.runTask(elasticSearchHttp
      .post(uri"/koski/perustiedot/_delete_by_query", doc)(Json4sHttp4s.json4sEncoderOf[JValue]) {
        case (200, text, request) => (Json.parse(text) \ "deleted").extract[Int]
        case (status, text, request) if List(404, 409).contains(status) => 0
        case (status, text, request) => throw HttpStatusException(status, text, request)
      })
    deleted
  }

  def reIndex(pagination: Option[PaginationSettings] = None) = {
    logger.info("Starting elasticsearch re-indexing")
    val bufferSize = 1000
    val observable = opiskeluoikeusQueryService.streamingQuery(Nil, None, pagination)(KoskiSession.systemUser).tumblingBuffer(bufferSize).zipWithIndex.map {
      case (rows, index) =>
        val perustiedot = rows.par.map { case (opiskeluoikeusRow, henkilöRow) =>
          OpiskeluoikeudenPerustiedot.makePerustiedot(opiskeluoikeusRow, henkilöRow)
        }.toList
        val changed = updateBulk(perustiedot, insertMissing = true) match {
          case Right(count) => count
          case Left(_) => 0 // error already logged
        }
        UpdateStatus(rows.length, changed)
    }.scan(UpdateStatus(0, 0))(_ + _)


    observable.subscribe({case UpdateStatus(countSoFar, actuallyChanged) => logger.info(s"Updated elasticsearch index for ${countSoFar} rows, actually changed ${actuallyChanged}")},
      {e: Throwable => logger.error(e)("Error updating Elasticsearch index")},
      { () => logger.info("Finished updating Elasticsearch index")})
    observable
  }

  private def runSearch(doc: JValue): Option[JValue] = try {
    Some(Http.runTask(elasticSearchHttp.post(uri"/koski/perustiedot/_search", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])))
  } catch {
    case e: HttpStatusException if e.status == 400 =>
      logger.warn(e.getMessage)
      None
  }

  private def nameFilter(hakusana: String) =
    analyzeString(hakusana).map { namePrefix =>
      Map("bool" -> Map("should" -> List(
        Map("prefix" -> Map("henkilö.sukunimi" -> namePrefix)),
        Map("prefix" -> Map("henkilö.etunimet" -> namePrefix))
      )))
    }

  private def analyzeString(string: String): List[String] = {
    val document: JValue = Http.runTask(elasticSearchHttp.post(uri"/koski/_analyze", string)(EntityEncoder.stringEncoder)(Http.parseJson[JValue]))
    val tokens: List[JValue] = (document \ "tokens").extract[List[JValue]]
    tokens.map(token => (token \ "token").extract[String])
  }


  case class UpdateStatus(updated: Int, changed: Int) {
    def +(other: UpdateStatus) = UpdateStatus(this.updated + other.updated, this.changed + other.changed)
  }

  val init = OpiskeluoikeudenPerustiedotRepository.synchronized {
    if (host == "localhost") {
      new ElasticSearchRunner("./elasticsearch", port, port + 100).start
    }
    logger.info(s"Using elasticsearch at $host:$port")

    Wait.until(clusterHealthOk)
    val reIndexingNeeded = setupIndex

    if (reIndexingNeeded || config.getBoolean("elasticsearch.reIndexAtStartup")) {
      Future {
        reIndex() // Re-index on background
      }
    }
  }

  private def clusterHealthOk = {
    val healthResponse: JValue = Http.runTask(elasticSearchHttp.get(uri"/_cluster/health")(Http.parseJson[JValue]))
    val healthCode = (healthResponse \ "status").extract[String]
    List("green", "yellow").contains(healthCode)
  }

  private def setupIndex: Boolean = {
    val settings = Json.parse("""
    {
        "analysis": {
          "filter": {
            "finnish_folding": {
              "type": "icu_folding",
              "unicodeSetFilter": "[^åäöÅÄÖ]"
            }
          },
          "analyzer": {
            "default": {
              "tokenizer": "icu_tokenizer",
              "filter":  [ "finnish_folding", "lowercase" ]
            }
          }
        }
    }""").extract[Map[String, Any]]

    val statusCode = Http.runTask(elasticSearchHttp.get(uri"/koski")(Http.statusCode))
    if (indexExists) {
      val serverSettings = (Http.runTask(elasticSearchHttp.get(uri"/koski/_settings")(Http.parseJson[JValue])) \ "koski-index" \ "settings" \ "index").extract[Map[String, Any]]
      val alreadyApplied = (serverSettings ++ settings) == serverSettings
      if (alreadyApplied) {
        logger.info("Elasticsearch index settings are up to date")
        false
      } else {
        logger.info("Updating Elasticsearch index settings")
        Http.runTask(elasticSearchHttp.post(uri"/koski/_close", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
        Http.runTask(elasticSearchHttp.put(uri"/koski/_settings", Json.toJValue(settings))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
        Http.runTask(elasticSearchHttp.post(uri"/koski/_open", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
        logger.info("Updated Elasticsearch index settings. Re-indexing is needed.")
        true
      }
    } else {
      logger.info("Creating Elasticsearch index")
      Http.runTask(elasticSearchHttp.put(uri"/koski-index", Json.toJValue(Map("settings" -> settings)))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
      logger.info("Creating Elasticsearch index alias")
      Http.runTask(elasticSearchHttp.post(uri"/_aliases", Json.toJValue(Map("actions" -> List(Map("add" -> Map("index" -> "koski-index", "alias" -> "koski"))))))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
      logger.info("Created index and alias.")
      true
    }
  }

  private def indexExists = {
    Http.runTask(elasticSearchHttp.get(uri"/koski")(Http.statusCode)) match {
      case 200 => true
      case 404 => false
      case statusCode =>
        throw new RuntimeException("Unexpected status code from elasticsearch: " + statusCode)
    }
  }
}

private object OpiskeluoikeudenPerustiedotRepository

class OpiskeluoikeudenPerustiedotServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Pagination with ObservableSupport {
  // TODO: Pitäisikö näistäkin katseluista tehdä auditlog-merkintä?
  get("/") {
    renderEither({
      val sort = SortOrder.parseSortOrder(params.get("sort"), Ascending("nimi"))

      OpiskeluoikeusQueryFilter.parse(params.toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
        case Right(filters) =>
          val pagination: PaginationSettings = paginationSettings.getOrElse(PaginationSettings(0, 100))
          val result: List[OpiskeluoikeudenPerustiedot] = application.perustiedotRepository.find(filters, sort, pagination)(koskiSession)
          Right(PaginatedResponse(Some(pagination), result, result.length))
        case Left(HttpStatus(404, _)) =>
          Right(PaginatedResponse(None, List[OpiskeluoikeudenPerustiedot](), 0))
        case e @ Left(_) => e
      }
    })
  }
}

object PerustiedotIndexUpdater extends App with Timing {
  val perustiedotRepository = KoskiApplication.apply.perustiedotRepository
  timed("Reindex") {
    perustiedotRepository.reIndex(None).toBlocking.last
    println("done")
  }
}