package fi.oph.tor.schema

import java.time.LocalDate
import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.schema.generic.annotation._


case class Vahvistus(
  @Description("Tutkinnon tai tutkinnonosan vahvistettu suorituspäivämäärä, eli päivämäärä jolloin suoritus on hyväksyttyä todennettua osaamista")
  päivä: LocalDate,
  @KoodistoUri("kunta")
  paikkakunta: Koodistokoodiviite,
  myöntäjäOrganisaatio: Organisaatio,
  @MinItems(1)
  myöntäjäHenkilöt: List[OrganisaatioHenkilö]
)

case class OrganisaatioHenkilö(
  nimi: String,
  titteli: LocalizedString,
  organisaatio: Organisaatio
)