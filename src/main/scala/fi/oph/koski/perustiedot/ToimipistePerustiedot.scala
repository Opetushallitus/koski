package fi.oph.koski.perustiedot

import fi.oph.koski.opensearch.OpenSearch
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization
import org.json4s.JValue

case class ToimipistePerustiedot(indexer: OpiskeluoikeudenPerustiedotIndexer) {
  def haeVarhaiskasvatustoimipisteet(koulutustoimijaOidit: Set[String]): Set[String] = {
    toimipisteetSearch(
      toimipisteetQuery(koulutustoimijaOidit, "esiopetus")
    )
  }

  def haeTaiteenPerusopetuksenToimipisteet(koulutustoimijaOidit: Set[String]): Set[String] = {
    toimipisteetSearch(
      toimipisteetQuery(koulutustoimijaOidit, "taiteenperusopetus")
    )
  }

  // TODO: Hae postgres-kannasta
  private def toimipisteetQuery(koulutustoimijaOidit: Set[String], tyyppi: String) = {
    LegacyJsonSerialization.toJValue(Map(
      "size" -> 0,
      "query" -> OpenSearch.allFilter(
        List(
          Map("term" -> Map("tyyppi.koodiarvo" -> tyyppi)),
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

  private def toimipisteetSearch(query: JValue): Set[String] = {
    indexer.index.runSearch(query).toList.flatMap { r =>
      extract[List[OppilaitosBucket]](r \ "aggregations" \ "oppilaitokset" \ "buckets", ignoreExtras = true)
    }.map(_.key).toSet
  }
}

case class OppilaitosBucket(key: String)
