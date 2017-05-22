package fi.oph.koski.perustiedot

import fi.oph.koski.json.Json

case class OpiskeluoikeudenPerustiedotStatistics(index: PerustiedotSearchIndex) {
  import PerustiedotSearchIndex._
  def statistics: Map[String, Any] = {
    rawStatistics.map { stats =>
      Map(
        "opiskeluoikeuksienMäärä" -> stats.total,
        "määrätKoulutusmuodoittain" -> stats.tyypit.map { tyyppi =>
          val määrätTiloittain = tyyppi.tila.tila.buckets.map { bucket =>
            Map("nimi" -> bucket.key, "opiskeluoikeuksienMäärä" -> bucket.doc_count)
          }

          Map(
            "nimi" -> tyyppi.key,
            "opiskeluoikeuksienMäärä" -> tyyppi.doc_count,
            "määrätTiloittain" -> määrätTiloittain,
            "siirtäneitäOppilaitoksia" -> tyyppi.toimipiste.count.value
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
          |    "tyyppi": {
          |      "terms": {
          |        "field": "tyyppi.nimi.fi.keyword"
          |      },
          |      "aggs": {
          |      "tila": {
          |        "nested": {
          |          "path": "tilat"
          |        },
          |        "aggs": {
          |          "tila": {
          |            "terms": {
          |              "field": "tilat.tila.koodiarvo.keyword",
          |              "include" : "valmistunut"
          |            }
          |          }
          |        }
          |      },
          |        "toimipiste": {
          |          "nested": {
          |            "path": "suoritukset"
          |          },
          |          "aggs": {
          |            "count": {
          |              "cardinality": {
          |                "field": "suoritukset.toimipiste.oid.keyword",
          |                "precision_threshold" : 10000
          |              }
          |            }
          |          }
          |        }
          |      }
          |    }
          |  }
          |}
        """.stripMargin)
    )

    result.map { r =>
      val total = (r \ "hits" \ "total").extract[Int]
      OpiskeluoikeudetTyypeittäin(total, (r \ "aggregations" \ "tyyppi" \ "buckets").extract[List[Tyyppi]])
    }
  }
}

case class OpiskeluoikeudetTyypeittäin(total: Int, tyypit: List[Tyyppi])
case class Tyyppi(key: String, doc_count: Int, tila: TilaNested, toimipiste: ToimipisteNested)
case class TilaNested(tila: Buckets)
case class ToimipisteNested(count: Count)
case class Count(value: Int)
case class Buckets(buckets: List[Bucket])
case class Bucket(key: String, doc_count: Int)