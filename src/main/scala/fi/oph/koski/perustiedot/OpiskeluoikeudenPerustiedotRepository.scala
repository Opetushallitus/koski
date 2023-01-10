package fi.oph.koski.perustiedot

import java.time.LocalDate
import fi.oph.koski.opensearch.OpenSearch
import fi.oph.koski.henkilo.TestingException
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization.toJValue
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, KäyttöoikeusVarhaiskasvatusToimipiste}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter._
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryFilter, OpiskeluoikeusQueryService}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.koski.util.SortOrder.{Ascending, Descending}
import fi.oph.koski.util._
import org.json4s.JValue

class OpiskeluoikeudenPerustiedotRepository(
  indexer: OpiskeluoikeudenPerustiedotIndexer,
  opiskeluoikeusQueryService: OpiskeluoikeusQueryService
) extends Logging {

  def find(filters: List[OpiskeluoikeusQueryFilter], sorting: SortOrder, pagination: PaginationSettings)(implicit session: KoskiSpecificSession): OpiskeluoikeudenPerustiedotResponse = {
    if (filters.exists(_.isInstanceOf[SuoritusJsonHaku])) {
      // JSON queries go to PostgreSQL
      OpiskeluoikeudenPerustiedotResponse(None, opiskeluoikeusQueryService.opiskeluoikeusQuery(filters, Some(sorting), Some(pagination)).toList.toBlocking.last.map {
        case (opiskeluoikeusRow, henkilöRow, masterHenkilöRow) => OpiskeluoikeudenPerustiedot.makePerustiedot(opiskeluoikeusRow, henkilöRow, masterHenkilöRow)
      })
    } else {
      // Other queries go to OpenSearch
      findFromIndex(filters, sorting, pagination)
    }
  }

  private def findFromIndex(filters: List[OpiskeluoikeusQueryFilter], sorting: SortOrder, pagination: PaginationSettings)(implicit session: KoskiSpecificSession): OpiskeluoikeudenPerustiedotResponse = {
    def nimi(order: String) = List(
      Map("henkilö.sukunimi.sort" -> order),
      Map("henkilö.etunimet.sort" -> order)
    )
    def luokka(order: String) = Map("luokka.keyword" -> order) :: nimi(order)
    def alkamispäivä(order: String) = Map("alkamispäivä" -> order):: nimi(order)
    def päättymispäivä(order: String) = Map("päättymispäivä" -> order):: nimi(order)
    val openSearchSort = sorting match {
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
        indexer.index.analyze(hakusana).map { namePrefix =>
          OpenSearch.anyFilter(List(
            Map("prefix" -> Map(s"suoritukset.koulutusmoduuli.tunniste.nimi.${session.lang}" -> namePrefix)),
            Map("prefix" -> Map(s"suoritukset.osaamisala.nimi.${session.lang}" -> namePrefix)),
            Map("prefix" -> Map(s"suoritukset.tutkintonimike.nimi.${session.lang}" -> namePrefix))
          ))
        }
      case OpiskeluoikeusQueryFilter.Toimipiste(toimipisteet) => List(
        OpenSearch.anyFilter(toimipisteet.map{ toimipiste =>
          Map("term" -> Map("suoritukset.toimipiste.oid" -> toimipiste.oid))
        })
      )
      case OpiskeluoikeusQueryFilter.VarhaiskasvatuksenToimipiste(toimipisteet) => List(
        OpenSearch.anyFilter(toimipisteet.map{ toimipiste =>
          OpenSearch.allFilter(List(
            Map("term" -> Map("suoritukset.toimipiste.oid" -> toimipiste.oid)),
            Map("term" -> Map("suoritukset.tyyppi.koodiarvo" -> "esiopetuksensuoritus"))
          ))
        })
      )
      case _ => Nil
    }

    val suoritusFilter = suoritusFilters match {
      case Nil => Nil
      case filters => List(OpenSearch.nestedFilter("suoritukset", OpenSearch.allFilter(filters)))
    }

    val openSearchFilters: List[Map[String, Any]] = filters.flatMap {
      case Nimihaku(hakusana) => nameFilter(hakusana)
      case Luokkahaku(hakusana) => hakusana.trim.split(" ").toList.map(_.toLowerCase).map { prefix =>
        Map("prefix" -> Map("luokka.keyword" -> prefix))
      }
      case OpiskeluoikeudenTyyppi(tyyppi) => List(Map("term" -> Map("tyyppi.koodiarvo" -> tyyppi.koodiarvo)))
      case OpiskeluoikeudenTila(tila) =>
        List(
          OpenSearch.nestedFilter("tilat",
            OpenSearch.allFilter(List(
              Map("term" -> Map("tilat.tila.koodiarvo" -> tila.koodiarvo)),
              Map("range" -> Map("tilat.alku" -> Map("lte" -> "now/d", "format" -> "yyyy-MM-dd"))),
              OpenSearch.anyFilter(List(
                Map("range" -> Map("tilat.loppu" -> Map("gte" -> "now/d", "format" -> "yyyy-MM-dd"))),
                OpenSearch.noneFilter(List(
                  Map(
                    "exists" -> Map(
                      "field" -> "tilat.loppu"
                    )
                  )
                ))
              ))
            ))
          )
        )

      case OpiskeluoikeusAlkanutAikaisintaan(day) =>
        List(Map("range" -> Map("alkamispäivä" -> Map("gte" -> day, "format" -> "yyyy-MM-dd"))))
      case OpiskeluoikeusAlkanutViimeistään(day) =>
        List(Map("range" -> Map("alkamispäivä" -> Map("lte" -> day, "format" -> "yyyy-MM-dd"))))
      case OpiskeluoikeusPäättynytAikaisintaan(day) =>
        List(Map("range" -> Map("päättymispäivä" -> Map("gte" -> day, "format" -> "yyyy-MM-dd"))))
      case OpiskeluoikeusPäättynytViimeistään(day) =>
        List(Map("range" -> Map("päättymispäivä" -> Map("lte" -> day, "format" -> "yyyy-MM-dd"))))
      case OpiskeluoikeusQueryFilter.TaiteenPerusopetuksenOppilaitos(oppilaitokset) => List(
        OpenSearch.anyFilter(oppilaitokset.map { oppilaitos =>
          OpenSearch.allFilter(List(
            Map("term" -> Map("tyyppi.koodiarvo" -> "taiteenperusopetus")),
            Map("term" -> Map("oppilaitos.oid" -> oppilaitos.oid))
          ))
        })
      )
      case SuoritusJsonHaku(json) => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("suoritusJson-parametriä ei tueta"))
      case _ => Nil
    } ++ oppilaitosFilter(session) ++ suoritusFilter ++ mitätöityFilter

    val openSearchQuery = openSearchFilters match {
      case Nil => Map.empty
      case _ => OpenSearch.allFilter(openSearchFilters)
    }

    val doc = toJValue(OpenSearch.applyPagination(Some(pagination), Map(
      "query" -> openSearchQuery,
      "sort" -> openSearchSort,
      "track_total_hits" -> true
    )))

    indexer.index.runSearch(doc)
      .map{ response =>
        OpiskeluoikeudenPerustiedotResponse(
          Some(extract[Int](response \ "hits" \ "total" \ "value")),
          extract[List[JValue]](response \ "hits" \ "hits").map(j => extract[OpiskeluoikeudenPerustiedot](j \ "_source", ignoreExtras = true)).map(pt => pt.copy(tilat = pt.tilat.map(tilat => vainAktiivinen(tilat))))
        )
      }
      .getOrElse(OpiskeluoikeudenPerustiedotResponse(None, Nil))
  }

  private def vainAktiivinen(tilat: List[OpiskeluoikeusJaksonPerustiedot]) = {
    tilat.reverse.find(!_.alku.isAfter(LocalDate.now)).toList
  }

  def findHenkiloPerustiedotByOids(oids: Seq[String]): Seq[OpiskeluoikeudenPerustiedot] = {
    val doc = toJValue(Map("query" -> Map("terms" -> Map("henkilöOid" -> oids)), "from" -> 0, "size" -> 10000))
    indexer.index.runSearch(doc)
      .map(response => extract[List[JValue]](response \ "hits" \ "hits").map(j => extract[OpiskeluoikeudenPerustiedot](j \ "_source", ignoreExtras = true)))
      .getOrElse(Nil)
  }

  def findHenkilöPerustiedotByHenkilöOid(oid: String): Option[NimitiedotJaOid] = {
    findSingleByHenkilöOid(oid).map(j => extract[NimitiedotJaOid](j \ "henkilö"))
  }

  private def findSingleByHenkilöOid(oid: String): Option[JValue] = {
    val doc = toJValue(Map("query" -> Map("term" -> Map("henkilöOid" -> oid))))

    indexer.index.runSearch(doc)
      .flatMap(response => extract[List[JValue]](response \ "hits" \ "hits").map(j => j \ "_source").headOption)
  }


  def findOids(hakusana: String)(implicit session: KoskiSpecificSession): List[Oid] = {
    if (hakusana == "") {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
    }
    if (hakusana.toLowerCase == "#error#") {
      throw new TestingException("Testing error handling")
    }

    val filters = nameFilter(hakusana) ++ oppilaitosFilter(session) ++ mitätöityFilter
    val doc = toJValue(Map(
      "_source" -> "henkilö.oid",
      "query" -> OpenSearch.allFilter(filters),
      "aggregations" -> Map("oids" -> Map("terms" -> Map("field" -> "henkilö.oid.keyword")))
    ))

    indexer.index.runSearch(doc)
      .map(response => extract[List[JValue]](response \ "aggregations" \ "oids" \ "buckets").map(j => extract[Oid](j \ "key")))
      .getOrElse(Nil)
  }

  private def oppilaitosFilter(session: KoskiSpecificSession): List[Map[String, Any]] = {
    val filters = if (session.hasGlobalReadAccess || session.hasGlobalKoulutusmuotoReadAccess) {
      Nil
    } else {
      val varhaiskasvatusOikeudet: Set[KäyttöoikeusVarhaiskasvatusToimipiste] = session.varhaiskasvatusKäyttöoikeudet.filter(_.organisaatioAccessType.contains(AccessType.read))
      List(OpenSearch.anyFilter(List(
        Map("terms" -> Map("sisältyyOpiskeluoikeuteen.oppilaitos.oid" -> session.organisationOids(AccessType.read))),
        Map("terms" -> Map("oppilaitos.oid" -> session.organisationOids(AccessType.read))),
        OpenSearch.allFilter(List(
          Map("terms" -> Map("oppilaitos.oid" -> varhaiskasvatusOikeudet.map(_.ulkopuolinenOrganisaatio.oid))),
          Map("terms" -> Map("koulutustoimija.oid" -> varhaiskasvatusOikeudet.map(_.koulutustoimija.oid)))
        )),
        OpenSearch.allFilter(List(
          Map("term" -> Map("tyyppi.koodiarvo" -> "taiteenperusopetus")),
          Map("terms" -> Map("koulutustoimija.oid" -> session.orgKäyttöoikeudet.flatMap(_.organisaatio.toKoulutustoimija).map(_.oid)))
        ))
      )))
    }

    if (session.hasKoulutusmuotoRestrictions) {
      List(Map("terms" -> Map("tyyppi.koodiarvo" -> session.allowedOpiskeluoikeusTyypit))) ++ filters
    } else {
      filters
    }
  }

  private def mitätöityFilter: List[Map[String, Any]] = List(
    OpenSearch.noneFilter(List(
      OpenSearch.nestedFilter("tilat", OpenSearch.allFilter(List(
        Map("term" -> Map("tilat.tila.koodiarvo" -> "mitatoity"))
      )))
    ))
  )

  private def nameFilter(hakusana: String) =
    indexer.index.analyze(hakusana).map { namePrefix =>
      OpenSearch.anyFilter(List(
        Map("prefix" -> Map("henkilö.sukunimi" -> namePrefix)),
        Map("prefix" -> Map("henkilö.etunimet" -> namePrefix))
      ))
    }
}

private object OpiskeluoikeudenPerustiedotRepository

case class OpiskeluoikeudenPerustiedotResponse(total: Option[Int], tiedot: List[OpiskeluoikeudenPerustiedot])
