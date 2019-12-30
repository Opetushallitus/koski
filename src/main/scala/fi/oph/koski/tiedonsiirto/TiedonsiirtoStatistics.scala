package fi.oph.koski.tiedonsiirto

import fi.oph.koski.elasticsearch.KoskiElasticSearchIndex
import fi.oph.koski.json.JsonSerializer.extract
import org.json4s.jackson.JsonMethods.parse

case class TiedonsiirtoStatistics(index: KoskiElasticSearchIndex) {

  def statistics: TiedonsiirtoTilasto = {
    val result = index.runSearch("tiedonsiirto",
      parse(
        """
          |{
          |  "size": 0,
          |  "query": {"exists":{"field":"virheet.key"}},
          |  "aggs": {
          |    "virhekoodit": {
          |      "terms": {"field":"virheet.key.keyword", "size": 10000}
          |     },
          |    "oppilaitokset": {
          |      "cardinality": {
          |        "field": "oppilaitokset.oid.keyword",
          |        "precision_threshold": 10000
          |      }
          |    }
          |  }
          |}
        """.stripMargin)
    )
    result.map { r =>
      val virheellisiä = extract[Int](r \ "hits" \ "total")
      val oppilaitoksiaJoillaVirheellisiä = extract[Int](r \ "aggregations" \ "oppilaitokset" \ "value")
      val virhekoodiTilastot = extract[List[VirhekoodiTilasto]](r \ "aggregations" \ "virhekoodit" \ "buckets")
      TiedonsiirtoTilasto(virheellisiä, oppilaitoksiaJoillaVirheellisiä, virhekoodiTilastot.sortBy(_.doc_count).reverse)
    }.getOrElse(TiedonsiirtoTilasto())
  }
}

case class TiedonsiirtoTilasto(
  virheellisiä: Int = 0,
  oppilaitoksiaJoillaVirheellisiä: Int = 0,
  virhekoodiTilastot: List[VirhekoodiTilasto] = Nil
)

case class VirhekoodiTilasto(key: String, doc_count: Int)
