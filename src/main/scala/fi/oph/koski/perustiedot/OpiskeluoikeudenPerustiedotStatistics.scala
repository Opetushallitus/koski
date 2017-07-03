package fi.oph.koski.perustiedot

import fi.oph.koski.json.Json

case class OpiskeluoikeudenPerustiedotStatistics(index: KoskiElasticSearchIndex) {
  import PerustiedotSearchIndex._
  def statistics: OpiskeluoikeusTilasto = {
    rawStatistics.map { stats =>
      OpiskeluoikeusTilasto(
        stats.total,
        stats.tyypit.map { tyyppi =>
          KoulutusmuotoTilasto(
            tyyppi.key,
            tyyppi.doc_count,
            tyyppi.tila.tila.buckets.headOption.map(_.doc_count).getOrElse(0),
            tyyppi.toimipiste.count.value
          )
        }
      )
    }.getOrElse(OpiskeluoikeusTilasto())
  }

  def henkilöCount: Option[Int] = {
    val result = index.runSearch("perustiedot",
      Json.parse(
        """
          |{
          |  "size": 0,
          |  "aggs": {
          |    "henkilöcount": {
          |      "cardinality": {
          |        "field": "henkilö.oid.keyword",
          |        "precision_threshold": 40000
          |      }
          |    }
          |  }
          |}
        """.stripMargin)
    )

    result.map(r => (r \ "aggregations" \ "henkilöcount" \ "value").extract[Int])
  }

  private def rawStatistics: Option[OpiskeluoikeudetTyypeittäin] = {
    val result = index.runSearch("perustiedot",
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
          |        "tila": {
          |          "nested": {
          |            "path": "tilat"
          |          },
          |          "aggs": {
          |            "tila": {
          |              "terms": {
          |                "field": "tilat.tila.koodiarvo.keyword",
          |                "include": "valmistunut"
          |              }
          |            }
          |          }
          |        },
          |        "toimipiste": {
          |          "nested": {
          |            "path": "suoritukset"
          |          },
          |          "aggs": {
          |            "count": {
          |              "cardinality": {
          |                "field": "suoritukset.toimipiste.oid.keyword",
          |                "precision_threshold": 10000
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

case class OpiskeluoikeusTilasto(
  opiskeluoikeuksienMäärä: Int = 0,
  koulutusmuotoTilastot: List[KoulutusmuotoTilasto] = Nil
) {
  def siirtäneitäOppilaitoksiaYhteensä: Int = koulutusmuotoTilastot.map(_.siirtäneitäOppilaitoksia).sum
}

case class KoulutusmuotoTilasto(koulutusmuoto: String, opiskeluoikeuksienMäärä: Int, valmistuneidenMäärä: Int, siirtäneitäOppilaitoksia: Int) {
  def koulutusmuotoStr: String = koulutusmuoto.toLowerCase.replaceAll(" ", "-").replaceAll("[()]", "")
}

case class OpiskeluoikeudetTyypeittäin(total: Int, tyypit: List[Tyyppi])
case class Tyyppi(key: String, doc_count: Int, tila: TilaNested, toimipiste: ToimipisteNested)
case class TilaNested(tila: Buckets)
case class ToimipisteNested(count: Count)
case class Count(value: Int)
case class Buckets(buckets: List[Bucket])
case class Bucket(key: String, doc_count: Int)