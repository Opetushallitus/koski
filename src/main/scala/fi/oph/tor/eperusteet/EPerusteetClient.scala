package fi.oph.tor.eperusteet

import fi.oph.tor.http.Http
import fi.oph.tor.tutkinto
import fi.oph.tor.tutkinto._
import org.json4s.JsonAST.JObject
import org.json4s.reflect.TypeInfo
import org.json4s.{Formats, JValue, Serializer}

class EPerusteetClient(ePerusteetRoot: String) extends TutkintoRepository {
  private val http: Http = Http()

  override def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto] = {
    ePerusteetToTutkinnot(http(ePerusteetRoot + "/api/perusteet?sivukoko=100&nimi=" + query)(Http.parseJson[EPerusteet]))
  }

  override def findByEPerusteDiaarinumero(diaarinumero: String) = {
    ePerusteetToTutkinnot(http(ePerusteetRoot + "/api/perusteet?diaarinumero=" + diaarinumero)(Http.parseJson[EPerusteet])).headOption
  }

  private def ePerusteetToTutkinnot(perusteet: EPerusteet) = {
    perusteet.data.flatMap { peruste =>
      peruste.koulutukset.map(koulutus => Tutkinto(koulutus.nimi("fi"), peruste.diaarinumero, koulutus.koulutuskoodiArvo))
    }
  }

  override def findPerusteRakenne(diaariNumero: String): Option[tutkinto.RakenneOsa] = {
    http(ePerusteetRoot + s"/api/perusteet/diaari?diaarinumero=$diaariNumero")(Http.parseJsonOptional[EPerusteTunniste])
      .map(e => http(ePerusteetRoot + "/api/perusteet/" + e.id + "/kaikki")(Http.parseJson[EPerusteRakenne]))
      .flatMap(rakenne => rakenne.suoritustavat.find(_.suoritustapakoodi == "naytto").map((rakenne, _)))
      .map {
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

case class EPerusteet(data: List[EPeruste])
case class EPeruste(diaarinumero: String, koulutukset: List[EPerusteKoulutus])
case class EPerusteKoulutus(nimi: Map[String, String], koulutuskoodiArvo: String)

case class EPerusteTunniste(id: String)
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
