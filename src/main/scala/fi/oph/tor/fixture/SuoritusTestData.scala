package fi.oph.tor.fixture

import java.text.SimpleDateFormat
import fi.oph.tor.date.FinnishDateParser
import fi.oph.tor.model.{Arviointi, Suoritus}

object SuoritusTestData {
  private val kouluOrganisaatio: String = "org1"
  private val personOid: String = "person1"
  val tutkintosuoritus1: Suoritus = Suoritus(None, kouluOrganisaatio, personOid, "tutkinto-1", "tutkinto", "kesken", None,
    Some(Arviointi(None, "1-10", 7, Some("Ihan perus ok"))),
    List(
      Suoritus(None, kouluOrganisaatio, personOid, "tutkinnonosa-1.1", "tutkinnon_osa", "suoritettu", d("20.6.2014 12:00"), Some(Arviointi(None, "1-10", 9, Some("Well done"))), List(
        Suoritus(None, kouluOrganisaatio, personOid, "kurssi-1.1.1", "kurssi", "suoritettu", d("20.6.2014 11:00"), Some(Arviointi(None, "1-10", 9, None)), List.empty),
        Suoritus(None, kouluOrganisaatio, personOid, "kurssi-1.1.2", "kurssi", "suoritettu", d("1.3.2014 12:00"), Some(Arviointi(None, "1-10", 8, None)), List.empty)
      )),
      Suoritus(None, kouluOrganisaatio, personOid, "tutkinnonosa-1.2", "tutkinnon_osa", "kesken", None, None, List(
        Suoritus(None, kouluOrganisaatio, personOid, "kurssi-1.2.2", "kurssi", "kesken", None, None, List.empty)
      ))
    ))

  val vainKomo112: Suoritus = Suoritus(None, kouluOrganisaatio, personOid, "tutkinto-1", "tutkinto", "kesken", None,
    Some(Arviointi(None, "1-10", 7, Some("Ihan perus ok"))),
    List(
      Suoritus(None, kouluOrganisaatio, personOid, "tutkinnonosa-1.1", "tutkinnon_osa", "suoritettu", d("20.6.2014 12:00"), Some(Arviointi(None, "1-10", 9, Some("Well done"))), List(
        Suoritus(None, kouluOrganisaatio, personOid, "kurssi-1.1.2", "kurssi", "suoritettu", d("1.3.2014 12:00"), Some(Arviointi(None, "1-10", 8, None)), List.empty)
      ))
    ))

  val vainKomo111: Suoritus = Suoritus(None, kouluOrganisaatio, personOid, "tutkinto-1", "tutkinto", "kesken", None,
    Some(Arviointi(None, "1-10", 7, Some("Ihan perus ok"))),
    List(
      Suoritus(None, kouluOrganisaatio, personOid, "tutkinnonosa-1.1", "tutkinnon_osa", "suoritettu", d("20.6.2014 12:00"), Some(Arviointi(None, "1-10", 9, Some("Well done"))), List(
        Suoritus(None, kouluOrganisaatio, personOid, "kurssi-1.1.1", "kurssi", "suoritettu", d("20.6.2014 11:00"), Some(Arviointi(None, "1-10", 9, None)), List.empty)
      ))
    ))

  def d(s: String) = Some(FinnishDateParser.parseDateTime(s))
}
