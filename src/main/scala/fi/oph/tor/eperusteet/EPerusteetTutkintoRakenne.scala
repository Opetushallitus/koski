package fi.oph.tor.eperusteet

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.koodisto.KoodistoViittaus
import fi.oph.tor.tutkinto
import fi.oph.tor.tutkinto._
import org.json4s.JsonAST.JObject
import org.json4s.reflect.TypeInfo
import org.json4s._

object EPerusteetTutkintoRakenne {
  def convertRakenne(rakenne: EPerusteRakenne)(implicit arviointiAsteikot: ArviointiasteikkoRepository): TutkintoRakenne = {
    val suoritustavat: List[tutkinto.Suoritustapa] = rakenne.suoritustavat.map { (suoritustapa: ESuoritustapa) =>
      Suoritustapa(suoritustapa.suoritustapakoodi.capitalize /* TODO: i18n */, suoritustapa.suoritustapakoodi, convertRakenneOsa(rakenne.tutkinnonOsat, suoritustapa.rakenne, suoritustapa.tutkinnonOsaViitteet))
    }

    val osaamisalat: List[Osaamisala] = rakenne.osaamisalat.map(o => Osaamisala(o.nimi("fi"), o.arvo))

    TutkintoRakenne(suoritustavat, osaamisalat, arviointiAsteikot.getAll)
  }

  private def convertRakenneOsa(tutkinnonOsat: List[ETutkinnonOsa], rakenneOsa: ERakenneOsa, tutkinnonOsaViitteet: List[ETutkinnonOsaViite]): RakenneOsa = {
    rakenneOsa match {
      case x: ERakenneModuuli => RakenneModuuli(
        x.nimi.getOrElse(Map.empty).getOrElse("fi", ""),
        x.osat.map(osa => convertRakenneOsa(tutkinnonOsat, osa, tutkinnonOsaViitteet)),
        x.osaamisala.map(_.osaamisalakoodiArvo)
      )
      case x: ERakenneTutkinnonOsa => tutkinnonOsaViitteet.find(v => v.id.toString == x._tutkinnonOsaViite) match {
        case Some(tutkinnonOsaViite) => TutkinnonOsa(tutkinnonOsat.find(o => o.id.toString == tutkinnonOsaViite._tutkinnonOsa).get.nimi.getOrElse("fi", ""), ArviointiasteikkoRepository.example)
        case None => throw new RuntimeException("Tutkinnonosaviitettä ei löydy: " + x._tutkinnonOsaViite)
      }
    }
  }
}

case class EPerusteRakenne(id: Long, nimi: Map[String, String], diaarinumero: String, suoritustavat: List[ESuoritustapa], tutkinnonOsat: List[ETutkinnonOsa], osaamisalat: List[EOsaamisala])
case class ESuoritustapa(suoritustapakoodi: String, rakenne: ERakenneOsa, tutkinnonOsaViitteet: List[ETutkinnonOsaViite])
case class ETutkinnonOsaViite(id: Long, _tutkinnonOsa: String)
case class EOsaamisala(nimi: Map[String, String], arvo: String)
case class EOsaamisalaViite(osaamisalakoodiArvo: String)
case class ETutkinnonOsa(id: Long, nimi: Map[String, String], koodiArvo: String)

sealed trait ERakenneOsa
case class ERakenneModuuli(nimi: Option[Map[String, String]], osat: List[ERakenneOsa], osaamisala: Option[EOsaamisalaViite]) extends ERakenneOsa
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