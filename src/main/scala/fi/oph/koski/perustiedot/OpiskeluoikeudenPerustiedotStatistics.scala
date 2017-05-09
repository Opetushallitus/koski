package fi.oph.koski.perustiedot

import fi.oph.koski.json.Json

case class OpiskeluoikeudenPerustiedotStatistics(index: PerustiedotSearchIndex) {
  import PerustiedotSearchIndex._
  def statistics: Map[String, Any] = {
    rawStatistics.map { stats =>
      Map(
        "opiskeluoikeuksienMäärä" -> stats.total,
        "määrätKoulutusmuodoittain" -> stats.tyypit.map { tyyppi =>
          val määrätTiloittain = tyyppi.tila.buckets.map { bucket =>
            Map("nimi" -> bucket.key, "opiskeluoikeuksienMäärä" -> bucket.doc_count)
          }

          Map(
            "nimi" -> tyyppi.key,
            "opiskeluoikeuksienMäärä" -> tyyppi.doc_count,
            "määrätTiloittain" -> määrätTiloittain,
            "siirtäneitäOppilaitoksia" -> tyyppi.toimipiste.buckets.size
          )
        }
      )
    }.getOrElse(Map())
  }

  def rawStatistics: Option[OpiskeluoikeudetTyypeittäin] = {
    val result = index.runSearch(
      Json.parse(
        """
          |{
          |  "size": 0,
          |  "aggs": {
          |    "tila": {
          |      "terms": {
          |        "field": "tila.koodiarvo.keyword"
          |      }
          |    },
          |    "tyyppi": {
          |      "terms": {
          |        "field": "tyyppi.nimi.fi.keyword"
          |      },
          |      "aggs": {
          |        "tila": {
          |          "terms": {
          |            "field": "tila.koodiarvo.keyword"
          |          }
          |        },
          |        "toimipiste": {
          |          "terms": {
          |            "field": "suoritukset.toimipiste.oid.keyword"
          |          }
          |        }
          |      }
          |    }
          |  }
          |}
          |
        """.stripMargin)
    )

    result.map { r =>
      val total = (r \ "hits" \ "total").extract[Int]
      OpiskeluoikeudetTyypeittäin(total, (r \ "aggregations" \ "tyyppi" \ "buckets").extract[List[Tyyppi]])
    }
  }
}

case class OpiskeluoikeudetTyypeittäin(total: Int, tyypit: List[Tyyppi])
case class Tyyppi(key: String, doc_count: Int, tila: Buckets, toimipiste: Buckets)
case class Buckets(buckets: List[Bucket])
case class Bucket(key: String, doc_count: Int)