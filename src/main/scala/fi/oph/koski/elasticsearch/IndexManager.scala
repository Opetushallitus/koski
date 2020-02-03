package fi.oph.koski.elasticsearch

class IndexManager(indexes: List[ElasticSearchIndex]) {
  def refreshAll(): Unit = {
    indexes.foreach(_.refreshIndex)
  }
}
