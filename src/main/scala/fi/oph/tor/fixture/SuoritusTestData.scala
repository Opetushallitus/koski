package fi.oph.tor.fixture

import fi.oph.tor.model.{Arviointi, Suoritus}

object SuoritusTestData {
  private val kouluOrganisaatio: String = "org1"
  private val personOid: String = "person1"
  val tutkintosuoritus1: Suoritus = Suoritus(None, kouluOrganisaatio, personOid, "tutkinto-1", "tutkinto", "kesken",
    Some(Arviointi(None, "1-10", 7, Some("Ihan perus ok"))),
    List(
      Suoritus(None, kouluOrganisaatio, personOid, "tutkinnonosa-1.1", "tutkinnon_osa", "kesken", Some(Arviointi(None, "1-10", 9, Some("Well done"))), List(
        Suoritus(None, kouluOrganisaatio, personOid, "kurssi-1.1.1", "kurssi", "suoritettu", Some(Arviointi(None, "1-10", 9, None)), List.empty),
        Suoritus(None, kouluOrganisaatio, personOid, "kurssi-1.1.2", "kurssi", "kesken", None, List.empty)
      )),
      Suoritus(None, kouluOrganisaatio, personOid, "tutkinnonosa-1.2", "kurssi", "kesken", None, List(
        Suoritus(None, kouluOrganisaatio, personOid, "kurssi-1.2.2", "kurssi", "kesken", None, List.empty)
      ))
    ))
}
