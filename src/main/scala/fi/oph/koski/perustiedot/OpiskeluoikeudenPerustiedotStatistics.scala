package fi.oph.koski.perustiedot

import fi.oph.koski.opensearch.OpenSearchIndex
import fi.oph.koski.json.JsonSerializer.extract
import org.json4s.JsonAST.JValue

case class OpiskeluoikeudenPerustiedotStatistics(index: OpenSearchIndex) {
  def statistics: OpiskeluoikeusTilasto = {
    rawStatistics.map { case (oppilaitosTotal, stats) =>
      OpiskeluoikeusTilasto(
        siirtäneitäOppilaitoksiaYhteensä = oppilaitosTotal,
        opiskeluoikeuksienMäärä = stats.total,
        koulutusmuotoTilastot = stats.tyypit.map { tyyppi =>
          KoulutusmuotoTilasto(
            tyyppi.key,
            tyyppi.doc_count,
            tyyppi.tila.tila.buckets.headOption.map(_.doc_count).getOrElse(0),
            tyyppi.oppilaitos.value
          )
        }
      )
    }.getOrElse(OpiskeluoikeusTilasto())
  }

  import org.json4s.jackson.JsonMethods.parse

  def henkilöCount: Option[Int] = {
    val result = index.runSearch(
      parse(
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

    result.map(r => extract[Int](r \ "aggregations" \ "henkilöcount" \ "value"))
  }

  private def rawStatistics: Option[(Int, OpiskeluoikeudetTyypeittäin)] = {
    val result = index.runSearch(
      parse(
        """
          |{
          |  "size": 0,
          |  "track_total_hits": "true",
          |  "aggs": {
          |    "oppilaitos_total": {
          |      "cardinality": {
          |        "field": "oppilaitos.oid.keyword",
          |        "precision_threshold": 10000
          |      }
          |    },
          |    "tyyppi": {
          |      "terms": {
          |        "field": "tyyppi.nimi.fi.keyword",
          |        "size": 100
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
          |                "include": ["valmistunut", "hyvaksytystisuoritettu"]
          |              }
          |            }
          |          }
          |        },
          |        "oppilaitos": {
          |          "cardinality": {
          |            "field": "oppilaitos.oid.keyword",
          |            "precision_threshold": 10000
          |          }
          |        }
          |      }
          |    }
          |  }
          |}
        """.stripMargin)
    )
    result.map { r =>
      val total = extract[Int](r \ "hits" \ "total" \ "value")
      val aggs = extract[JValue](r \ "aggregations")
      val oppilaitosTotal = extract[Int](aggs \ "oppilaitos_total" \ "value")
      (oppilaitosTotal, OpiskeluoikeudetTyypeittäin(total, extract[List[Tyyppi]](aggs \ "tyyppi" \ "buckets", ignoreExtras = true)))
    }
  }
}

case class OpiskeluoikeusTilasto(
  siirtäneitäOppilaitoksiaYhteensä: Int = 0,
  opiskeluoikeuksienMäärä: Int = 0,
  koulutusmuotoTilastot: List[KoulutusmuotoTilasto] = Nil
)

case class KoulutusmuotoTilasto(koulutusmuoto: String, opiskeluoikeuksienMäärä: Int, valmistuneidenMäärä: Int, siirtäneitäOppilaitoksia: Int) {
  def koulutusmuotoStr: String = koulutusmuoto.toLowerCase.replaceAll(" ", "-").replaceAll("[()]", "")
}

case class OpiskeluoikeudetTyypeittäin(total: Int, tyypit: List[Tyyppi])
case class Tyyppi(key: String, doc_count: Int, tila: TilaNested, oppilaitos: Count)
case class TilaNested(tila: Buckets)
case class Count(value: Int)
case class Buckets(buckets: List[Bucket])
case class Bucket(key: String, doc_count: Int)
