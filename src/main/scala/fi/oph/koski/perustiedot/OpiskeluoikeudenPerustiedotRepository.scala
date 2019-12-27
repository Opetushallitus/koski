package fi.oph.koski.perustiedot

import java.time.LocalDate

import fi.oph.koski.elasticsearch.ElasticSearch
import fi.oph.koski.elasticsearch.ElasticSearch.anyFilter
import fi.oph.koski.henkilo.TestingException
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json4sHttp4s
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization.toJValue
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter._
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryFilter, OpiskeluoikeusQueryService}
import fi.oph.koski.schema
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.koski.util.SortOrder.{Ascending, Descending}
import fi.oph.koski.util._
import org.json4s.JValue
import org.json4s.JsonAST.{JObject, JString}

class OpiskeluoikeudenPerustiedotRepository(index: KoskiElasticSearchIndex, opiskeluoikeusQueryService: OpiskeluoikeusQueryService) extends Logging {

  def find(filters: List[OpiskeluoikeusQueryFilter], sorting: SortOrder, pagination: PaginationSettings)(implicit session: KoskiSession): OpiskeluoikeudenPerustiedotResponse = {
    if (filters.find(_.isInstanceOf[SuoritusJsonHaku]).isDefined) {
      // JSON queries go to PostgreSQL
      OpiskeluoikeudenPerustiedotResponse(None, opiskeluoikeusQueryService.opiskeluoikeusQuery(filters, Some(sorting), Some(pagination)).toList.toBlocking.last.map {
        case (opiskeluoikeusRow, henkilöRow, masterHenkilöRow) => OpiskeluoikeudenPerustiedot.makePerustiedot(opiskeluoikeusRow, henkilöRow, masterHenkilöRow)
      })
    } else {
      // Other queries got to ElasticSearch
      findFromIndex(filters, sorting, pagination)
    }
  }

  private def findFromIndex(filters: List[OpiskeluoikeusQueryFilter], sorting: SortOrder, pagination: PaginationSettings)(implicit session: KoskiSession): OpiskeluoikeudenPerustiedotResponse = {
    def nimi(order: String) = List(
      Map("henkilö.sukunimi.keyword" -> order),
      Map("henkilö.etunimet.keyword" -> order)
    )
    def luokka(order: String) = Map("luokka.keyword" -> order) :: nimi(order)
    def alkamispäivä(order: String) = Map("alkamispäivä" -> order):: nimi(order)
    def päättymispäivä(order: String) = Map("päättymispäivä" -> order):: nimi(order)
    val elasticSort = sorting match {
      case Ascending("nimi") => nimi("asc")
      case Ascending("luokka") => luokka("asc")
      case Ascending("alkamispäivä") => alkamispäivä("asc")
      case Ascending("päättymispäivä") => päättymispäivä("asc")
      case Descending("nimi") => nimi("desc")
      case Descending("luokka") => luokka("desc")
      case Descending("alkamispäivä") => alkamispäivä("desc")
      case Descending("päättymispäivä") => päättymispäivä("desc")
      case _ => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Epäkelpo järjestyskriteeri: " + sorting.field))
    }

    val suoritusFilters = filters.flatMap {
      case SuorituksenTyyppi(tyyppi) => List(Map("term" -> Map("suoritukset.tyyppi.koodiarvo" -> tyyppi.koodiarvo)))
      case Tutkintohaku(hakusana) =>
        analyzeString(hakusana).map { namePrefix =>
          anyFilter(List(
            Map("prefix" -> Map(s"suoritukset.koulutusmoduuli.tunniste.nimi.${session.lang}" -> namePrefix)),
            Map("prefix" -> Map(s"suoritukset.osaamisala.nimi.${session.lang}" -> namePrefix)),
            Map("prefix" -> Map(s"suoritukset.tutkintonimike.nimi.${session.lang}" -> namePrefix))
          ))
        }
      case OpiskeluoikeusQueryFilter.Toimipiste(toimipisteet) => List(anyFilter(toimipisteet.map{ toimipiste => Map("term" -> Map("suoritukset.toimipiste.oid" -> toimipiste.oid))}))
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
                anyFilter(List(
                  Map("range" -> Map("tilat.loppu" -> Map("gte" -> "now/d", "format" -> "yyyy-MM-dd"))),
                  Map("bool" -> Map(
                    "must_not" -> Map(
                      "exists" -> Map(
                        "field" -> "tilat.loppu"
                      )
                    )
                  ))
                ))
              )
            )
          ))
        )

      case OpiskeluoikeusAlkanutAikaisintaan(day) =>
        List(Map("range" -> Map("alkamispäivä" -> Map("gte" -> day, "format" -> "yyyy-MM-dd"))))
      case OpiskeluoikeusAlkanutViimeistään(day) =>
        List(Map("range" -> Map("alkamispäivä" -> Map("lte" -> day, "format" -> "yyyy-MM-dd"))))
      case OpiskeluoikeusPäättynytAikaisintaan(day) =>
        List(Map("range" -> Map("päättymispäivä" -> Map("gte" -> day, "format" -> "yyyy-MM-dd"))))
      case OpiskeluoikeusPäättynytViimeistään(day) =>
        List(Map("range" -> Map("päättymispäivä" -> Map("lte" -> day, "format" -> "yyyy-MM-dd"))))
      case SuoritusJsonHaku(json) => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("suoritusJson-parametriä ei tueta"))
      case _ => Nil
    } ++ oppilaitosFilter(session) ++ suoritusFilter ++ mitätöityFilter

    val elasticQuery = elasticFilters match {
      case Nil => Map.empty
      case _ => ElasticSearch.allFilter(elasticFilters)
    }

    val doc = toJValue(ElasticSearch.applyPagination(Some(pagination), Map(
      "query" -> elasticQuery,
      "sort" -> elasticSort)
    ))

    index.runSearch("perustiedot", doc)
      .map{ response =>
        OpiskeluoikeudenPerustiedotResponse(
          Some(extract[Int](response \ "hits" \ "total")),
          extract[List[JValue]](response \ "hits" \ "hits").map(j => extract[OpiskeluoikeudenPerustiedot](j \ "_source", ignoreExtras = true)).map(pt => pt.copy(tilat = pt.tilat.map(tilat => vainAktiivinen(tilat))))
        )
      }
      .getOrElse(OpiskeluoikeudenPerustiedotResponse(None, Nil))
  }

  private def nestedFilter(path: String, query: Map[String, AnyRef]) = Map(
    "nested" -> Map(
      "path" -> path,
      "query" -> query
    )
  )

  private def vainAktiivinen(tilat: List[OpiskeluoikeusJaksonPerustiedot]) = {
    tilat.reverse.find(!_.alku.isAfter(LocalDate.now)).toList
  }

  def findHenkiloPerustiedotByOids(oids: List[String]): List[OpiskeluoikeudenPerustiedot] = {
    val doc = toJValue(Map("query" -> Map("terms" -> Map("henkilöOid" -> oids)), "from" -> 0, "size" -> 10000))
    index.runSearch("perustiedot", doc)
      .map(response => extract[List[JValue]](response \ "hits" \ "hits").map(j => extract[OpiskeluoikeudenPerustiedot](j \ "_source", ignoreExtras = true)))
      .getOrElse(Nil)
  }

  def findHenkilöPerustiedotByHenkilöOid(oid: String): Option[NimitiedotJaOid] = {
    findSingleByHenkilöOid(oid).map(j => extract[NimitiedotJaOid](j \ "henkilö"))
  }

  private def findSingleByHenkilöOid(oid: String): Option[JValue] = {
    val doc = toJValue(Map("query" -> Map("term" -> Map("henkilöOid" -> oid))))

    index.runSearch("perustiedot", doc)
      .flatMap(response => extract[List[JValue]](response \ "hits" \ "hits").map(j => j \ "_source").headOption)
  }


  def findOids(hakusana: String)(implicit session: KoskiSession): List[Oid] = {
    if (hakusana == "") {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
    }
    if (hakusana.toLowerCase == "#error#") {
      throw new TestingException("Testing error handling")
    }

    val filters = List(nameFilter(hakusana)) ++ oppilaitosFilter(session) ++ mitätöityFilter
    val doc = toJValue(Map(
      "_source" -> "henkilö.oid",
      "query" -> Map("bool" -> Map("must" -> filters)),
      "aggregations" -> Map("oids" -> Map("terms" -> Map("field" -> "henkilö.oid.keyword")))
    ))

    index.runSearch("perustiedot", doc)
      .map(response => extract[List[JValue]](response \ "aggregations" \ "oids" \ "buckets").map(j => extract[Oid](j \ "key")))
      .getOrElse(Nil)
  }

  private def oppilaitosFilter(session: KoskiSession): List[Map[String, Any]] = {
    val filters = if (session.hasGlobalReadAccess || session.hasGlobalKoulutusmuotoReadAccess) {
      Nil
    } else {
      List(anyFilter(List(
        Map("terms" -> Map("sisältyyOpiskeluoikeuteen.oppilaitos.oid" -> session.orgKäyttöoikeudetByAccessType(AccessType.read).map(_.organisaatioOid))),
        Map("terms" -> Map("oppilaitos.oid" -> session.orgKäyttöoikeudetByAccessType(AccessType.read).map(_.organisaatioOid)))
      )))
    }

    if (session.hasKoulutusmuotoRestrictions) {
      List(Map("terms" -> Map("tyyppi.koodiarvo" -> session.allowedOpiskeluoikeusTyypit))) ++ filters
    } else {
      filters
    }
  }

  private def mitätöityFilter: List[Map[String, Any]] = List(
    Map("bool" -> Map("must_not" -> nestedFilter("tilat", Map(
      "bool" -> Map(
        "must" -> List(
          Map("term" -> Map("tilat.tila.koodiarvo" -> "mitatoity"))
        )
    ))))))

  private def nameFilter(hakusana: String) =
    analyzeString(hakusana).map { namePrefix =>
      anyFilter(List(
        Map("prefix" -> Map("henkilö.sukunimi" -> namePrefix)),
        Map("prefix" -> Map("henkilö.etunimet" -> namePrefix))
      ))
    }

  private def analyzeString(string: String): List[String] = {
    val document: JValue = Http.runTask(index.http.post(uri"/koski/_analyze", JObject("analyzer" -> JString("default"), "text" -> JString(string)))(Json4sHttp4s.json4sEncoderOf[JObject])(Http.parseJson[JValue]))
    val tokens: List[JValue] = extract[List[JValue]](document \ "tokens")
    tokens.map(token => extract[String](token \ "token"))
  }
}

private object OpiskeluoikeudenPerustiedotRepository

case class OpiskeluoikeudenPerustiedotResponse(total: Option[Int], tiedot: List[OpiskeluoikeudenPerustiedot])
