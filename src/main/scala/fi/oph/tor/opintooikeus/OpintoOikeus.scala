package fi.oph.tor.opintooikeus

import fi.oph.tor.arvosana.Arvosana
import fi.oph.tor.koodisto.KoodistoViittaus
import fi.oph.tor.oppilaitos.{Oppilaitos}
import fi.oph.tor.tutkinto.{KoulutusModuuliTunniste, Tutkinto, TutkintoRakenne}

case class OpintoOikeus(
  tutkinto: Tutkinto, oppilaitosOrganisaatio: Oppilaitos,
  suoritustapa: Option[String] = None, osaamisala: Option[String] = None,
  suoritukset: List[Suoritus] = Nil,
  id: Option[Int] = None
)

object OpintoOikeus {
  type Id = Int
}

case class Suoritus(koulutusModuuli: KoulutusModuuliTunniste, arviointi: Option[Arviointi])

case class Arviointi(asteikko: KoodistoViittaus, arvosana: Arvosana)