package fi.oph.tor.opintooikeus

import fi.oph.tor.oppilaitos.{OppilaitosId, OppilaitosOrId, Oppilaitos}
import fi.oph.tor.tutkinto.TutkintoRakenne

trait OpintoOikeus {
  def ePerusteetDiaarinumero: String
  def oppilaitosOrganisaatio: OppilaitosOrId
  def suoritustapa: Option[String]
  def osaamisala: Option[String]
  def id: Option[Int]
}

case class OpintoOikeusData(
  ePerusteetDiaarinumero: String, oppilaitosOrganisaatio: OppilaitosId, suoritustapa: Option[String] = None, osaamisala: Option[String] = None, id: Option[Int] = None
) extends OpintoOikeus

case class TorOpintoOikeusView(
  ePerusteetDiaarinumero: String, oppilaitosOrganisaatio: Oppilaitos, suoritustapa: Option[String] = None, osaamisala: Option[String] = None, id: Option[Int] = None,

  nimi: String, rakenne: Option[TutkintoRakenne]
) extends OpintoOikeus

case class TorOppijaView(oid: String, sukunimi: String, etunimet: String, hetu: String, opintoOikeudet: Seq[TorOpintoOikeusView])

object OpintoOikeus {
  type Id = Int
}