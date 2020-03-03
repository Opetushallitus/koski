package fi.oph.koski.perustiedot

import fi.oph.koski.elasticsearch.ElasticSearchIndex
import org.json4s.JValue
import org.json4s.jackson.JsonMethods
import fi.oph.koski.json.JsonSerializer.extract

case class VarhaiskasvatusToimipistePerustiedot(index: ElasticSearchIndex) {
  def haeVarhaiskasvatustoimipisteet(koulutustoimijaOidit: Set[String]): Set[String] = {
    val result: Option[JValue] = index.runSearch(
      JsonMethods.parse(
        s"""
          |{
          |  "size": 0,
          |  "query": {
          |    "bool": {
          |      "must": [
          |        {
          |          "term": {
          |            "tyyppi.koodiarvo": "esiopetus"
          |          }
          |        },
          |        {
          |          "terms": {
          |            "koulutustoimija.oid": [
          |              ${koulutustoimijaOidit.mkString("\"", ",", "\"")}
          |            ]
          |          }
          |        }
          |      ]
          |    }
          |  },
          |  "aggs": {
          |    "oppilaitokset": {
          |      "terms": {
          |        "field": "oppilaitos.oid.keyword"
          |      }
          |    }
          |  }
          |}
        """.stripMargin)
    )

    result.toList.flatMap { r =>
      extract[List[PäiväkotiBucket]](r \ "aggregations" \ "oppilaitokset" \ "buckets", ignoreExtras = true)
    }.map(_.key).toSet
  }
}

case class PäiväkotiBucket(key: String)
