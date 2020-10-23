package fi.oph.koski.perustiedot

import fi.oph.koski.elasticsearch.ElasticSearchIndex
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization

case class VarhaiskasvatusToimipistePerustiedot(index: ElasticSearchIndex) {
  def haeVarhaiskasvatustoimipisteet(koulutustoimijaOidit: Set[String]): Set[String] = {
    val query = varhaiskasvatustoimipisteetQuery(koulutustoimijaOidit)
    index.runSearch(query).toList.flatMap { r =>
      extract[List[PäiväkotiBucket]](r \ "aggregations" \ "oppilaitokset" \ "buckets", ignoreExtras = true)
    }.map(_.key).toSet
  }

  private def varhaiskasvatustoimipisteetQuery(koulutustoimijaOidit: Set[String]) = {
    LegacyJsonSerialization.toJValue(Map(
      "size" -> 0,
      "query" -> Map(
        "bool" -> Map(
          "must" -> List(
            Map("term" -> Map("tyyppi.koodiarvo" -> "esiopetus")),
            Map("terms" -> Map("koulutustoimija.oid" -> koulutustoimijaOidit))
          )
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
