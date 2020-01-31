package fi.oph.koski.perustiedot

import fi.oph.koski.elasticsearch.{ElasticSearch, ElasticSearchIndex}
import org.json4s.jackson.JsonMethods

object PerustiedotIndex {
  private def settings = JsonMethods.parse("""
    {
        "analysis": {
          "filter": {
            "finnish_folding": {
              "type": "icu_folding",
              "unicodeSetFilter": "[^åäöÅÄÖ]"
            }
          },
          "analyzer": {
            "default": {
              "tokenizer": "icu_tokenizer",
              "filter":  [ "finnish_folding", "lowercase" ]
            }
          }
        }
    }""")
}

class PerustiedotIndex(elastic: ElasticSearch) extends ElasticSearchIndex(
  elastic = elastic,
  name = "koski-index",
  mappingType = "perustiedot",
  settings = PerustiedotIndex.settings
) {
  def indexIsLarge: Boolean = {
    OpiskeluoikeudenPerustiedotStatistics(this).statistics.opiskeluoikeuksienMäärä > 100
  }
}
