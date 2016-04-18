package fi.oph.tor.eperusteet

import org.json4s.JsonAST.JObject
import org.json4s._
import org.json4s.reflect.TypeInfo

case class EPerusteRakenne(id: Long, nimi: Map[String, String], diaarinumero: String, koulutustyyppi: String,
                           koulutukset: List[EPerusteKoulutus], suoritustavat: List[ESuoritustapa], tutkinnonOsat: List[ETutkinnonOsa], osaamisalat: List[EOsaamisala]) {
  def toEPeruste = EPeruste(id, nimi, diaarinumero, koulutukset)
}

case class ESuoritustapa(suoritustapakoodi: String, laajuusYksikko: Option[String], rakenne: Option[ERakenneOsa], tutkinnonOsaViitteet: Option[List[ETutkinnonOsaViite]])
case class ETutkinnonOsaViite(id: Long, laajuus: Option[Float], _tutkinnonOsa: String)
case class EOsaamisala(nimi: Map[String, String], arvo: String)
case class EOsaamisalaViite(osaamisalakoodiArvo: String)
case class ETutkinnonOsa(id: Long, nimi: Map[String, String], koodiArvo: String)

sealed trait ERakenneOsa
case class ERakenneModuuli(nimi: Option[Map[String, String]], osat: List[ERakenneOsa], osaamisala: Option[EOsaamisalaViite]) extends ERakenneOsa
case class ERakenneTutkinnonOsa(_tutkinnonOsaViite: String, pakollinen: Boolean) extends ERakenneOsa

object RakenneOsaSerializer extends Serializer[ERakenneOsa] {
  private val RakenneOsaClass = classOf[ERakenneOsa]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ERakenneOsa] = {
    case (TypeInfo(RakenneOsaClass, _), json) => json match {
      case moduuli: JObject if moduuli.values.contains("osat") => moduuli.extract[ERakenneModuuli]
      case osa: JObject => osa.extract[ERakenneTutkinnonOsa]
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}