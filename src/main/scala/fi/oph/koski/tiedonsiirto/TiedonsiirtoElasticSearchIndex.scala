package fi.oph.koski.tiedonsiirto

import fi.oph.koski.elasticsearch.{ElasticSearch, KoskiElasticSearchIndex}
import org.json4s.jackson.JsonMethods.parse

object TiedonsiirtoElasticSearchIndex {
  def apply(elastic: ElasticSearch): KoskiElasticSearchIndex = {
    new KoskiElasticSearchIndex("koski-tiedonsiirto-index", settings, elastic)
  }

  private def settings = parse("""
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
