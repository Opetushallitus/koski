package fi.oph.koski.perustiedot

import java.time.LocalDate

import fi.oph.koski.elasticsearch.ElasticSearch
import fi.oph.koski.henkilo.TestingException
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter._
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryFilter, OpiskeluoikeusQueryService}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.koski.util.SortOrder.{Ascending, Descending}
import fi.oph.koski.util._
import org.http4s.EntityEncoder
import org.json4s.JValue

class OpiskeluoikeudenPerustiedotRepository(index: PerustiedotSearchIndex, opiskeluoikeusQueryService: OpiskeluoikeusQueryService) extends Logging {
  import PerustiedotSearchIndex._

  def find(filters: List[OpiskeluoikeusQueryFilter], sorting: SortOrder, pagination: PaginationSettings)(implicit session: KoskiSession): List[OpiskeluoikeudenPerustiedot] = {
    if (filters.find(_.isInstanceOf[SuoritusJsonHaku]).isDefined) {
      // JSON queries go to PostgreSQL
      opiskeluoikeusQueryService.streamingQuery(filters, Some(sorting), Some(pagination)).toList.toBlocking.last.map {
        case (opiskeluoikeusRow, henkilöRow) =>  OpiskeluoikeudenPerustiedot.makePerustiedot(opiskeluoikeusRow, henkilöRow)
      }
    } else {
      // Other queries got to ElasticSearch
      findFromIndex(filters, sorting, pagination)
    }
  }

  private def findFromIndex(filters: List[OpiskeluoikeusQueryFilter], sorting: SortOrder, pagination: PaginationSettings)(implicit session: KoskiSession): List[OpiskeluoikeudenPerustiedot] = {
    def nimi(order: String) = List(
      Map("henkilö.sukunimi.keyword" -> order),
      Map("henkilö.etunimet.keyword" -> order)
    )
    def luokka(order: String) = Map("luokka.keyword" -> order) :: nimi(order)
    def alkamispäivä(order: String) = Map("alkamispäivä" -> order):: nimi(order)
    def nestedFilter(path: String, query: Map[String, AnyRef]) = Map(
      "nested" -> Map(
        "path" -> path,
        "query" -> query
      )
    )
    val elasticSort = sorting match {
      case Ascending("nimi") => nimi("asc")
      case Ascending("luokka") => luokka("asc")
      case Ascending("alkamispäivä") => alkamispäivä("asc")
      case Descending("nimi") => nimi("desc")
      case Descending("luokka") => luokka("desc")
      case Descending("alkamispäivä") => alkamispäivä("desc")
      case _ => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Epäkelpo järjestyskriteeri: " + sorting.field))
    }

    val suoritusFilters = filters.flatMap {
      case SuorituksenTyyppi(tyyppi) => List(Map("term" -> Map("suoritukset.tyyppi.koodiarvo" -> tyyppi.koodiarvo)))
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
      case _ => Nil
    }

    val suoritusFilter = suoritusFilters match {
      case Nil => Nil
      case filters => List(nestedFilter("suoritukset", ElasticSearch.allFilter(filters)))
    }

    val elasticFilters: List[Map[String, Any]] = filters.flatMap {
      case Nimihaku(hakusana) => nameFilter(hakusana)
      case Luokkahaku(hakusana) => hakusana.trim.split(" ").toList.map(_.toLowerCase).map { prefix =>
        Map("prefix" -> Map("luokka" -> prefix))
      }
      case OpiskeluoikeudenTyyppi(tyyppi) => List(Map("term" -> Map("tyyppi.koodiarvo" -> tyyppi.koodiarvo)))
      case OpiskeluoikeudenTila(tila) =>
        List(
          nestedFilter("tilat", Map(
            "bool" -> Map(
              "must" -> List(
                Map("term" -> Map("tilat.tila.koodiarvo" -> tila.koodiarvo)),
                Map("range" -> Map("tilat.alku" -> Map("lte" -> "now/d", "format" -> "yyyy-MM-dd"))),
                Map("bool" -> Map(
                  "should" -> List(
                    Map("range" -> Map("tilat.loppu" -> Map("gte" -> "now/d", "format" -> "yyyy-MM-dd"))),
                    Map("bool" -> Map(
                      "must_not" -> Map(
                        "exists" -> Map(
                          "field" -> "tilat.loppu"
                        )
                      )
                    ))
                  )
                ))
              )
            )
          ))
        )

      case OpiskeluoikeusAlkanutAikaisintaan(day) =>
        List(Map("range" -> Map("alkamispäivä" -> Map("gte" -> day, "format" -> "yyyy-MM-dd"))))
      case OpiskeluoikeusAlkanutViimeistään(day) =>
        List(Map("range" -> Map("alkamispäivä" -> Map("lte" -> day, "format" -> "yyyy-MM-dd"))))
      case SuorituksenTila(tila) => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("suorituksenTila-parametriä ei tueta"))
      case SuoritusJsonHaku(json) => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("suoritusJson-parametriä ei tueta"))
      case _ => Nil
    } ++ oppilaitosFilter(session) ++ suoritusFilter

    val elasticQuery = elasticFilters match {
      case Nil => Map.empty
      case _ => ElasticSearch.allFilter(elasticFilters)
    }

    val doc = Json.toJValue(Map(
      "query" -> elasticQuery,
      "sort" -> elasticSort,
      "from" -> pagination.page * pagination.size,
      "size" -> pagination.size
    ))

    index.runSearch(doc)
      .map{ response =>
        (response \ "hits" \ "hits").extract[List[JValue]].map(j => (j \ "_source").extract[OpiskeluoikeudenPerustiedot]).map(pt => pt.copy(tilat = pt.tilat.map(tilat => vainAktiivinen(tilat))))
      }
      .getOrElse(Nil)
  }

  private def vainAktiivinen(tilat: List[OpiskeluoikeusJaksonPerustiedot]) = {
    tilat.reverse.find(!_.alku.isAfter(LocalDate.now)).toList
  }

  def findHenkiloPerustiedotByOids(oids: List[String]): List[OpiskeluoikeudenPerustiedot] = {
    val doc = Json.toJValue(Map("query" -> Map("terms" -> Map("henkilö.oid" -> oids)), "from" -> 0, "size" -> 10000))
    index.runSearch(doc)
      .map(response => (response \ "hits" \ "hits").extract[List[JValue]].map(j => (j \ "_source").extract[OpiskeluoikeudenPerustiedot]))
      .getOrElse(Nil)
  }

  def findHenkilöPerustiedot(oid: String): Option[NimitiedotJaOid] = {
    val doc = Json.toJValue(Map("query" -> Map("term" -> Map("henkilö.oid" -> oid))))

    index.runSearch(doc)
      .flatMap(response => (response \ "hits" \ "hits").extract[List[JValue]].map(j => (j \ "_source" \ "henkilö").extract[NimitiedotJaOid]).headOption)
  }

  def findOids(hakusana: String)(implicit session: KoskiSession): List[Oid] = {
    if (hakusana == "") {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
    }
    if (hakusana == "#error#") {
      throw new TestingException("Testing error handling")
    }

    val filters = List(nameFilter(hakusana)) ++ oppilaitosFilter(session)
    val doc = Json.toJValue(Map(
      "_source" -> "henkilö.oid",
      "query" -> Map("bool" -> Map("must" -> filters)),
      "aggregations" -> Map("oids" -> Map("terms" -> Map("field" -> "henkilö.oid.keyword")))
    ))

    index.runSearch(doc)
      .map(response => (response \ "aggregations" \ "oids" \ "buckets").extract[List[JValue]].map(j => (j \ "key").extract[Oid]))
      .getOrElse(Nil)
  }

  private def oppilaitosFilter(session: KoskiSession): List[Map[String, Any]] =
    if (session.hasGlobalReadAccess) {
      Nil
    } else {
      List(Map("terms" -> Map("oppilaitos.oid" -> session.organisationOids(AccessType.read))))
    }


  private def nameFilter(hakusana: String) =
    analyzeString(hakusana).map { namePrefix =>
      Map("bool" -> Map("should" -> List(
        Map("prefix" -> Map("henkilö.sukunimi" -> namePrefix)),
        Map("prefix" -> Map("henkilö.etunimet" -> namePrefix))
      )))
    }

  private def analyzeString(string: String): List[String] = {
    val document: JValue = Http.runTask(index.elasticSearchHttp.post(uri"/koski/_analyze", string)(EntityEncoder.stringEncoder)(Http.parseJson[JValue]))
    val tokens: List[JValue] = (document \ "tokens").extract[List[JValue]]
    tokens.map(token => (token \ "token").extract[String])
  }
}

private object OpiskeluoikeudenPerustiedotRepository