package fi.oph.koski.tiedonsiirto

import fi.oph.koski.elasticsearch.{ElasticSearch, ElasticSearchIndex}
import org.json4s.jackson.JsonMethods

object TiedonsiirtoIndex {
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

class TiedonsiirtoIndex(elastic: ElasticSearch) extends ElasticSearchIndex(
  elastic = elastic,
  name = "koski-index",
  mappingType = "tiedonsiirto",
  settings = TiedonsiirtoIndex.settings
)
