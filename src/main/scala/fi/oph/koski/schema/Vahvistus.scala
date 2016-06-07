package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation._

trait Vahvistus {
  @Description("Tutkinnon tai tutkinnonosan vahvistettu suorituspäivämäärä, eli päivämäärä jolloin suoritus on hyväksyttyä todennettua osaamista")
  def päivä: LocalDate
  @KoodistoUri("kunta")
  @Description("Paikkakunta, jossa suoritus on vahvistettu (allekirjoituksen paikkakunta)")
  def paikkakunta: Koodistokoodiviite
  @Description("Organisaatio, joka suorituksen on vahvistanut")
  def myöntäjäOrganisaatio: Organisaatio
  @Description("Myöntäjähenkilö/-henkilöt, eli suorituksen/todistuksen allekirjoittajat")
  def myöntäjäHenkilöt: List[OrganisaatioHenkilö]
}

@Description("Suorituksen vahvistus organisaatio- ja henkilötiedoilla")
case class Henkilövahvistus(
  päivä: LocalDate,
  paikkakunta: Koodistokoodiviite,
  myöntäjäOrganisaatio: Organisaatio,
  @MinItems(1)
  myöntäjäHenkilöt: List[OrganisaatioHenkilö]
) extends Vahvistus


@Description("Suorituksen vahvistus organisaatiotiedoilla")
case class Organisaatiovahvistus(
  päivä: LocalDate,
  paikkakunta: Koodistokoodiviite,
  myöntäjäOrganisaatio: Organisaatio
) extends Vahvistus {
  def myöntäjäHenkilöt = Nil
}

case class OrganisaatioHenkilö(
  nimi: String,
  titteli: LocalizedString,
  organisaatio: Organisaatio
)