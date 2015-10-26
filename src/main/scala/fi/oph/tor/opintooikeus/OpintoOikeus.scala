package fi.oph.tor.opintooikeus

import fi.oph.tor.oppilaitos.{Oppilaitos}
import fi.oph.tor.tutkinto.{Tutkinto, TutkintoRakenne}

trait OpintoOikeus {
  def tutkinto: Tutkinto
  def oppilaitosOrganisaatio: Oppilaitos
  def suoritustapa: Option[String]
  def osaamisala: Option[String]
  def id: Option[Int]
}

case class OpintoOikeusData(
  tutkinto: Tutkinto, oppilaitosOrganisaatio: Oppilaitos, suoritustapa: Option[String] = None, osaamisala: Option[String] = None, id: Option[Int] = None
) extends OpintoOikeus

case class TorOpintoOikeusView(
  tutkinto: Tutkinto, oppilaitosOrganisaatio: Oppilaitos, suoritustapa: Option[String] = None, osaamisala: Option[String] = None, id: Option[Int] = None,

  rakenne: Option[TutkintoRakenne]
) extends OpintoOikeus

case class TorOppijaView(oid: String, sukunimi: String, etunimet: String, hetu: String, opintoOikeudet: Seq[TorOpintoOikeusView])

object OpintoOikeus {
  type Id = Int
}