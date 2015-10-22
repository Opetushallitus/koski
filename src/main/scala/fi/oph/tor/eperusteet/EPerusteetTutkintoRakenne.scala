package fi.oph.tor.eperusteet

import fi.oph.tor.tutkinto.{RakenneOsa, RakenneModuuli, TutkinnonOsa}
import org.json4s.JsonAST.JObject
import org.json4s.reflect.TypeInfo
import org.json4s._

object EPerusteetTutkintoRakenne {
  def convertRakenne(rakenne: EPerusteRakenne): Option[RakenneOsa] = {
    rakenne.suoritustavat.find(_.suoritustapakoodi == "naytto").map((rakenne, _)).map {
      case (rakenne, suoritustapa) => convertRakenneOsa(rakenne.tutkinnonOsat, suoritustapa.rakenne, suoritustapa.tutkinnonOsaViitteet)
    }
  }

  private def convertRakenneOsa(tutkinnonOsat: List[ETutkinnonOsa], rakenneOsa: ERakenneOsa, tutkinnonOsaViitteet: List[TutkinnonOsaViite]): RakenneOsa = {
    rakenneOsa match {
      case x: ERakenneModuuli => RakenneModuuli(x.nimi.getOrElse(Map.empty).getOrElse("fi", ""), x.osat.map(osa => convertRakenneOsa(tutkinnonOsat, osa, tutkinnonOsaViitteet)))
      case x: ERakenneTutkinnonOsa => tutkinnonOsaViitteet.find(v => v.id.toString == x._tutkinnonOsaViite) match {
        case Some(tutkinnonOsaViite) => TutkinnonOsa(tutkinnonOsat.find(o => o.id.toString == tutkinnonOsaViite._tutkinnonOsa).get.nimi.getOrElse("fi", ""))
      }
    }
  }
}

case class EPerusteRakenne(id: Long, nimi: Map[String, String], diaarinumero: String, suoritustavat: List[Suoritustapa], tutkinnonOsat: List[ETutkinnonOsa])
case class Suoritustapa(suoritustapakoodi: String, rakenne: ERakenneOsa, tutkinnonOsaViitteet: List[TutkinnonOsaViite])
case class TutkinnonOsaViite(id: Long, _tutkinnonOsa: String)
case class ETutkinnonOsa(id: Long, nimi: Map[String, String], koodiArvo: String)

sealed trait ERakenneOsa
case class ERakenneModuuli(nimi: Option[Map[String, String]], osat: List[ERakenneOsa]) extends ERakenneOsa
case class ERakenneTutkinnonOsa(_tutkinnonOsaViite: String) extends ERakenneOsa

class RakenneOsaSerializer extends Serializer[ERakenneOsa] {
  private val PieceClass = classOf[ERakenneOsa]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ERakenneOsa] = {
    case (TypeInfo(PieceClass, _), json) => json match {
      case moduuli: JObject if moduuli.values.contains("osat") => moduuli.extract[ERakenneModuuli]
      case osa: JObject => osa.extract[ERakenneTutkinnonOsa]
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}