package fi.oph.koski.raportit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationUser
import scala.collection.JavaConverters._

object RaportitAccessResolver {

  private val raportit = Map(
    "ammatillinenkoulutus" -> Seq("opiskelijavuositiedot", "suoritustietojentarkistus", "ammatillinenosittainensuoritustietojentarkistus"),
    "perusopetus" -> Seq("perusopetuksenvuosiluokka")
  )

  def availableRaportit(koulutusmuodot: Set[String], application: KoskiApplication, authUser: AuthenticationUser): Set[String] = {
    val raportitKoulutusmuodoille = koulutusmuodot.flatMap(raportit.getOrElse(_, Seq.empty))
    raportitKoulutusmuodoille.filter(checkAccessIfAccessIsLimited(_, application, authUser))
  }

  private def checkAccessIfAccessIsLimited(raportinNimi: String, application: KoskiApplication, authUser: AuthenticationUser) = {
    val rajatutRaportit = application.config.getConfigList("raportit.rajatut")
    val conf = rajatutRaportit.asScala.find(_.getString("name") == raportinNimi)
    conf match {
      case Some(c) => c.getStringList("whitelist").contains(authUser.oid)
      case _ => true
    }
  }
}
