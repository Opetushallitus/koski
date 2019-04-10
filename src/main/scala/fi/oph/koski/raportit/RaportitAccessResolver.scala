package fi.oph.koski.raportit


import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationUser

object RaportitAccessResolver {

  private val raportit = Map(
    "ammatillinenkoulutus" -> Seq("opiskelijavuositiedot", "suoritustietojentarkistus"),
    "perusopetus" -> Seq("perusopetuksenvuosiluokka")
  )

  def availableRaportit(koulutusmuodot: Set[String], application: KoskiApplication, authUser: AuthenticationUser): Set[String] = {
    val raportitKoulutusmuodoille = koulutusmuodot.flatMap(raportit.getOrElse(_, Seq.empty))
    val käyttäjälläPääsy = raportitKoulutusmuodoille.flatMap(checkAccessIfAccessIsLimited(_, application, authUser))
    käyttäjälläPääsy
  }

  private def checkAccessIfAccessIsLimited(raportinNimi: String, application: KoskiApplication, authUser: AuthenticationUser) = {
    val rajatutRaportit = application.config.getStringList(s"raportit.rajatut")
    if (rajatutRaportit.contains(raportinNimi)) {
      val sallitutOidit = application.config.getStringList(s"raportit.${raportinNimi}")
      if (sallitutOidit.contains(authUser.oid)) Some(raportinNimi) else None
    } else {
      Some(raportinNimi)
    }
  }
}
