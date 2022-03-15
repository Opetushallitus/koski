package fi.oph.koski.koodisto

import fi.oph.koski.raportit.PerusopetuksenVuosiluokkaRaportti.logger
import fi.oph.koski.log.Logging

class Kunta

object Kunta extends Logging {
  def getKunnanNimi(koodi: Option[String], koodistoPalvelu: KoodistoPalvelu, lang: String): Option[String] = {
    koodi match {
      case Some(koodi) => {
        val koodistoKoodit = koodistoPalvelu.getKoodistoKoodit(koodistoPalvelu.getLatestVersionRequired("kunta"))
        val koodistoKoodi = koodistoKoodit.find(_.koodiArvo == koodi)
        koodistoKoodi match {
          case Some(koodi) => Some(koodi.nimi.get.get(lang))
          case None => {
            logger.warn(s"Koodiarvolle $koodi ei löytynyt koodia kunta-koodistosta")
            None
          }
        }
      }
      case None => None
    }
  }

  def kuntaExists(koodi: String, koodistoPalvelu: KoodistoPalvelu): Boolean = {
    val koodistoKoodit = koodistoPalvelu.getKoodistoKoodit(koodistoPalvelu.getLatestVersionRequired("kunta"))
    koodistoKoodit.find(_.koodiArvo == koodi).isDefined
  }

  // Kuntakoodit, jotka indikoivat puuttuvaa tietoa tai esim. ulkomailla-asumista
  def onPuuttuvaKunta(koodi: String): Boolean = {
    Set("198", "199", "200", "999").contains(koodi)
  }
}
