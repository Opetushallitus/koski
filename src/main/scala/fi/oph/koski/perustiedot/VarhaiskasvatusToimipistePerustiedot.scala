package fi.oph.koski.perustiedot

import fi.oph.koski.opensearch.OpenSearch
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization

case class VarhaiskasvatusToimipistePerustiedot(indexer: OpiskeluoikeudenPerustiedotIndexer) {
  def haeVarhaiskasvatustoimipisteet(koulutustoimijaOidit: Set[String]): Set[String] = {
    val query = varhaiskasvatustoimipisteetQuery(koulutustoimijaOidit)
    indexer.index.runSearch(query).toList.flatMap { r =>
      extract[List[PäiväkotiBucket]](r \ "aggregations" \ "oppilaitokset" \ "buckets", ignoreExtras = true)
    }.map(_.key).toSet
  }

  // TODO: Hae postgres-kannasta
  private def varhaiskasvatustoimipisteetQuery(koulutustoimijaOidit: Set[String]) = {
    LegacyJsonSerialization.toJValue(Map(
      "size" -> 0,
      "query" -> OpenSearch.allFilter(
        List(
          Map("term" -> Map("tyyppi.koodiarvo" -> "esiopetus")),
          Map("terms" -> Map("koulutustoimija.oid" -> koulutustoimijaOidit))
        )
      ),
      "aggs" -> Map(
        "oppilaitokset" -> Map(
          "terms" -> Map(
            "field" -> "oppilaitos.oid.keyword",
            "size" -> 2147483647
          )
        )
      )
    ))
  }
}

case class PäiväkotiBucket(key: String)
