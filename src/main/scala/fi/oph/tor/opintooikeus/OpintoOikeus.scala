package fi.oph.tor.opintooikeus

import fi.oph.tor.oppilaitos.{Oppilaitos}
import fi.oph.tor.tutkinto.{Tutkinto, TutkintoRakenne}

case class OpintoOikeus(
  tutkinto: Tutkinto, oppilaitosOrganisaatio: Oppilaitos, suoritustapa: Option[String] = None, osaamisala: Option[String] = None, id: Option[Int] = None
)

object OpintoOikeus {
  type Id = Int
}