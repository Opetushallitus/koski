package fi.oph.tor.opintooikeus

import fi.oph.tor.oppilaitos.Oppilaitos
import fi.oph.tor.tutkinto.TutkintoRakenne

trait OpintoOikeus {
  def ePerusteetDiaarinumero: String
  def oppilaitosOrganisaatio: String
  def suoritustapa: Option[String]
  def osaamisala: Option[String]
  def id: Option[Int]
}

case class OpintoOikeusData(
  ePerusteetDiaarinumero: String, oppilaitosOrganisaatio: String, suoritustapa: Option[String] = None, osaamisala: Option[String] = None, id: Option[Int] = None
) extends OpintoOikeus

case class TorOpintoOikeusView(
  ePerusteetDiaarinumero: String, oppilaitosOrganisaatio: String, suoritustapa: Option[String] = None, osaamisala: Option[String] = None, id: Option[Int] = None,

  nimi: String, oppilaitos: Oppilaitos, rakenne: Option[TutkintoRakenne]
) extends OpintoOikeus

case class TorOppijaView(oid: String, sukunimi: String, etunimet: String, hetu: String, opintoOikeudet: Seq[TorOpintoOikeusView])

object OpintoOikeus {
  type Id = Int
}