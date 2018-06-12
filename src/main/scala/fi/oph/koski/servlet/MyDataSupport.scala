package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import scala.collection.JavaConverters._


trait MyDataSupport extends LanguageSupport {
  def application: KoskiApplication

  def getConfigForMember(id: String): com.typesafe.config.Config = {
    application.config.getConfigList("mydata.members").asScala.find(member =>
      member.getString("id") == id).get
  }

  def getLoginUrlForMember(id: String): String = getConfigForMember(id).getString("login." + langFromCookie.getOrElse(langFromDomain))

}

