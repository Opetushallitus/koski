package fi.oph.koski.perustiedot

import fi.oph.koski.elasticsearch.{ElasticSearch, KoskiElasticSearchIndex}
import org.json4s.jackson.JsonMethods.parse

object PerustiedotElasticSearchIndex {
  def apply(elastic: ElasticSearch): KoskiElasticSearchIndex = {
    new KoskiElasticSearchIndex("koski-perustiedot-index", settings, elastic)
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
