package fi.oph.koski.elasticsearch

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import org.scalatra._

class ElasticSearchServlet(implicit val application: KoskiApplication)
  extends ApiServlet
    with RequiresVirkailijaOrPalvelukäyttäjä
    with NoCache
    with ObservableSupport
    with ContentEncodingSupport {

  private def noRemoteCalls(): Unit = {
    if (!List("127.0.0.1", "[0:0:0:0:0:0:0:1]").contains(request.getRemoteHost)) {
      haltWithStatus(KoskiErrorCategory.forbidden(""))
    }
  }

  private def withNamedIndex[T](operation: IndexManager => Option[T])(renderer: T => Unit): Unit = {
    operation(application.indexManager) match {
      case Some(result) => renderer(result)
      case None => haltWithStatus(KoskiErrorCategory.notFound("Elasticsearch index not found"))
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
