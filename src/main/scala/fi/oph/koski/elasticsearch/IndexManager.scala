package fi.oph.koski.elasticsearch

class IndexManager(indexes: List[ElasticSearchIndex]) {
  private val namedIndexes = indexes.map(i => i.name -> i).toMap

  private def withNamedIndex[T](indexName: String, operation: ElasticSearchIndex => T): Option[T] = {
    namedIndexes.get(indexName) match {
      case Some(index) => Some(operation(index))
      case None => None
    }
  }

  def refreshAll(): Unit = {
    indexes.foreach(_.refreshIndex())
  }

  def createIndex(indexName: String, version: Int): Option[String] = {
    withNamedIndex[String](indexName, _.createIndex(version))
  }

  def reindex(indexName: String, fromVersion: Int, toVersion: Int): Option[(String, String)] = {
    withNamedIndex[(String, String)](indexName, _.reindex(fromVersion, toVersion))
  }

  def reload(indexName: String): Option[String] = {
    withNamedIndex[Unit](indexName, _.reload())
    Some(indexName)
  }

  def migrateReadAlias(indexName: String, toVersion: Int, fromVersion: Option[Int] = None): Option[String] = {
    withNamedIndex[String](indexName, _.migrateReadAlias(toVersion, fromVersion))
  }

  def migrateWriteAlias(indexName: String, toVersion: Int, fromVersion: Option[Int] = None): Option[String] = {
    withNamedIndex[String](indexName, _.migrateWriteAlias(toVersion, fromVersion))
  }
}
