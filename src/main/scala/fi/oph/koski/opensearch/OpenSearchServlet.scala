package fi.oph.koski.opensearch

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache, ObservableSupport}
import org.scalatra._

class OpenSearchServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with RequiresVirkailijaOrPalvelukäyttäjä
    with NoCache
    with ObservableSupport
    with ContentEncodingSupport {

  private def withNamedIndex[T](operation: IndexManager => Option[T])(renderer: T => Unit): Unit = {
    operation(application.indexManager) match {
      case Some(result) => renderer(result)
      case None => haltWithStatus(KoskiErrorCategory.notFound("OpenSearch index not found"))
    }
  }

  before() {
    noRemoteCalls()
  }

  post("/:indexName/create/:version") {
    withNamedIndex[String](
      _.createIndex(getStringParam("indexName"), getIntegerParam("version"))
    )(
      foundIndex => renderObject(Map("created index" -> foundIndex))
    )
  }

  post("/:indexName/reindex/:fromVersion/:toVersion") {
    withNamedIndex[(String, String)](
      _.reindex(getStringParam("indexName"), getIntegerParam("fromVersion"), getIntegerParam("toVersion"))
    ){
      case (source, target) => renderObject(Map("reindexing" -> Map("from" -> source, "to" -> target)))
    }
  }

  post("/:indexName/reload") {
    withNamedIndex[String](
      _.reload(getStringParam("indexName"))
    )(
      indexName => renderObject(Map("started full index reload" -> indexName))
    )
  }

  private def aliasOperation(
    indexName: String, aliasType: String, toVersion: Int, fromVersion: Option[Int] = None
  ): IndexManager => Option[String] = {
    aliasType match {
      case "read" => _.migrateReadAlias(indexName, toVersion, fromVersion)
      case "write" => _.migrateWriteAlias(indexName, toVersion, fromVersion)
      case _ => haltWithStatus(KoskiErrorCategory.badRequest(s"Invalid alias type: $aliasType"))
    }
  }

  post("/:indexName/setAlias/:aliasType/:version") {
    val indexName = getStringParam("indexName")
    val aliasType = getStringParam("aliasType")
    val toVersion = getIntegerParam("version")
    withNamedIndex[String](
      aliasOperation(indexName, aliasType, toVersion)
    )(
      createdAlias => renderObject(Map("created alias" -> createdAlias))
    )
  }

  post("/:indexName/migrateAlias/:aliasType/:fromVersion/:toVersion") {
    val indexName = getStringParam("indexName")
    val aliasType = getStringParam("aliasType")
    val fromVersion = getIntegerParam("fromVersion")
    val toVersion = getIntegerParam("toVersion")
    withNamedIndex[String](
      aliasOperation(indexName, aliasType, toVersion, Some(fromVersion))
    )(
      migratedAlias => renderObject(Map("migrated alias" -> migratedAlias))
    )
  }
}
