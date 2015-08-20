package fi.oph.tor.fixture

import fi.oph.tor.date.ISO8601DateParser
import fi.oph.tor.model.{Arviointi, Komoto, Suoritus}

object SuoritusTestData {
  private val kouluOrganisaatio: String = "org1"
  private val myöntäjäOrganisaatio: String = "org1"
  private val oppijaId: String = "person1"

  private val tutkinto = Komoto(None, Some("tutkinto-1"), None, Some("tutkinto-1"), Some("tutkinto"), None, None, None)
  private val tutkinnonOsa1 = Komoto(None, Some("tutkinnonosa-1-1"), None, Some("tutkinnonosa-1-1"), Some("tutkinnon_osa"), None, None, None)
  private val tutkinnonOsa2 = Komoto(None, Some("tutkinnonosa-1-2"), None, Some("tutkinnonosa-1-1"), Some("tutkinnon_osa"), None, None, None)
  private val kurssi1_1 = Komoto(None, Some("kurssi-1-1-1"), None, Some("kurssi-1-1-1"), Some("kurssi"), None, None, None)
  private val kurssi1_2 = Komoto(None, Some("kurssi-1-1-2"), None, Some("kurssi-1-1-2"), Some("kurssi"), None, None, None)
  private val kurssi2_2 = Komoto(None, Some("kurssi-1-2-2"), None, Some("kurssi-1-2-2"), Some("kurssi"), None, None, None)

  val tutkintosuoritus1: Suoritus = Suoritus(None, None, kouluOrganisaatio, myöntäjäOrganisaatio, oppijaId, "kesken", None, tutkinto, Some(Arviointi(None, "1-10", 7, Some("Ihan perus ok"))),
    List(
      Suoritus(None, d("2014-06-20T09:00:00Z"), kouluOrganisaatio, myöntäjäOrganisaatio, oppijaId, "suoritettu", None, tutkinnonOsa1, Some(Arviointi(None, "1-10", 9, Some("Well done"))), List(
        Suoritus(None, d("2014-06-20T08:00:00Z"), kouluOrganisaatio, myöntäjäOrganisaatio, oppijaId, "suoritettu", None, kurssi1_1, Some(Arviointi(None, "1-10", 9, None)), List.empty),
        Suoritus(None, d("2014-03-01T10:00:00Z"), kouluOrganisaatio, myöntäjäOrganisaatio, oppijaId, "suoritettu", None, kurssi1_2, Some(Arviointi(None, "1-10", 8, None)), List.empty)
      )),
      Suoritus(None, None, kouluOrganisaatio, myöntäjäOrganisaatio, oppijaId, "kesken", None, tutkinnonOsa2, None, List(
        Suoritus(None, None, kouluOrganisaatio, myöntäjäOrganisaatio, oppijaId, "kesken", None, kurssi2_2, None, List.empty)
      ))
    ))

  val vainKomo112: Suoritus = includeListedKomotos(tutkintosuoritus1, List(tutkinnonOsa1, kurssi1_2))
  val vainKomo111: Suoritus = includeListedKomotos(tutkintosuoritus1, List(tutkinnonOsa1, kurssi1_1))
  val vainKomo12: Suoritus = includeListedKomotos(tutkintosuoritus1, List(tutkinnonOsa2))
  val vainKomo122: Suoritus = includeListedKomotos(tutkintosuoritus1, List(tutkinnonOsa2, kurssi2_2))

  private def includeListedKomotos(s: Suoritus, komos: List[Komoto]): Suoritus = {
    s.copy(osasuoritukset = s.osasuoritukset.filter(s => komos.contains(s.komoto)).map(s => includeListedKomotos(s, komos)))
  }
  private def d(s: String) = Some(ISO8601DateParser.parseDateTime(s))
}
