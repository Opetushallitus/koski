package fi.oph.koski.koodisto

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportit.AhvenanmaanKunnat
import fi.oph.koski.valpas.oppija.ValpasErrorCategory

class Kunta

object Kunta extends Logging {

  val helsinki = "091"
  val jyväskylä = "179"
  val pyhtää = "624"

  val eiKotikuntaaSuomessa = "198"
  val kotikuntaTuntematon = "199"
  val kotikuntaUlkomailla = "200"
  val eiTiedossa = "999"


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

  def validateAndGetKuntaKoodi(
    organisaatiot: OrganisaatioService,
    koodistoPalvelu: KoodistoPalvelu,
    kuntaOid: String
  ): Either[HttpStatus, String] = {
    organisaatiot
      .haeKuntakoodi(kuntaOid)
      .flatMap(kuntakoodi => {
        if (
          Kunta.kuntaExists(kuntakoodi, koodistoPalvelu) &&
            !AhvenanmaanKunnat.onAhvenanmaalainenKunta(kuntakoodi) &&
            !Kunta.onPuuttuvaKunta(kuntakoodi)
        ) {
          Some(kuntakoodi)
        } else {
          None
        }
      })
      .toRight(ValpasErrorCategory.badRequest(s"Kunta ${kuntaOid} ei ole koodistopalvelun tuntema manner-Suomen kunta"))
  }

  def kuntaExists(koodi: String, koodistoPalvelu: KoodistoPalvelu): Boolean = {
    val koodistoKoodit = koodistoPalvelu.getKoodistoKoodit(koodistoPalvelu.getLatestVersionRequired("kunta"))
    koodistoKoodit.find(_.koodiArvo == koodi).isDefined
  }

  // Kuntakoodit, jotka indikoivat puuttuvaa tietoa tai esim. ulkomailla-asumista
  def onPuuttuvaKunta(koodi: String): Boolean = {
    Set(eiKotikuntaaSuomessa, kotikuntaTuntematon, kotikuntaUlkomailla, eiTiedossa).contains(koodi)
  }
}
