package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import com.typesafe.config.{Config => TypeSafeConfig}
import fi.oph.koski.servlet.InvalidRequestException
import scala.jdk.CollectionConverters._


trait MyDataConfig extends Logging  {
  def application: KoskiApplication
  protected def conf: TypeSafeConfig = application.config.getConfig("mydata")

  def hasConfigForMember(id: String = null): Boolean = getConfigOption(id).isDefined

  def getConfigForMember(id: String = null): TypeSafeConfig = {
    getConfigOption(id).getOrElse({
      logger.warn("No MyData configuration found for member: " + id)
      throw InvalidRequestException(KoskiErrorCategory.notFound.myDataMemberEiLöydy)
    })
  }

  def findMemberForMemberCode(memberCode: String): Option[TypeSafeConfig] = {
    conf.getConfigList("members").asScala.find(member =>
      member.getStringList("membercodes").contains(memberCode))
  }

  private def getConfigOption(id: String): Option[TypeSafeConfig] = {
    conf.getConfigList("members").asScala.find(member => member.getString("id") == id)
  }

  /**
    * Check if given URL is defined in configuration mydata.callbackURLs
    */
  def isWhitelistedCallbackURL(callbackURL: String): Boolean = {

    conf.getStringList("callbackURLs").asScala.exists(whitelistExpression =>
      callbackURL.matches(whitelistExpression)
    )
  }

  def isMyDataOrg(orgOid: String): Boolean = {
    conf.getConfigList("members").asScala.exists(member =>
      member.getString("orgOid") == orgOid
    )
  }
}
