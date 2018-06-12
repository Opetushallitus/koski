package fi.oph.koski.servlet

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import scala.collection.JavaConverters._


trait MyDataSupport  {
  def application: KoskiApplication

  def getConfigForMember(id: String): Config = {
    application.config.getConfigList("mydata.members").asScala.find(member =>
      member.getString("id") == id).get
  }
}

