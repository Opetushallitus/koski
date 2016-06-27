package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation._

trait Vahvistus {
  @Description("Tutkinnon tai tutkinnonosan vahvistettu suorituspäivämäärä, eli päivämäärä jolloin suoritus on hyväksyttyä todennettua osaamista. Muoto YYYY-MM-DD")
  def päivä: LocalDate
  @KoodistoUri("kunta")
  @Description("Paikkakunta, jossa suoritus on vahvistettu (allekirjoituksen paikkakunta)")
  def paikkakunta: Koodistokoodiviite
  @Description("Organisaatio, joka suorituksen on vahvistanut")
  def myöntäjäOrganisaatio: Organisaatio
  @Description("Myöntäjähenkilö/-henkilöt, eli suorituksen/todistuksen allekirjoittajat")
  def myöntäjäHenkilöt: List[Organisaatiohenkilö]
}

@Description("Suorituksen vahvistus organisaatio- ja henkilötiedoilla")
case class Henkilövahvistus(
  päivä: LocalDate,
  paikkakunta: Koodistokoodiviite,
  myöntäjäOrganisaatio: Organisaatio,
  @MinItems(1)
  myöntäjäHenkilöt: List[Organisaatiohenkilö]
) extends Vahvistus


@Description("Suorituksen vahvistus organisaatiotiedoilla")
case class Organisaatiovahvistus(
  päivä: LocalDate,
  paikkakunta: Koodistokoodiviite,
  myöntäjäOrganisaatio: Organisaatio
) extends Vahvistus {
  def myöntäjäHenkilöt = Nil
}

@Description("Henkilö- ja organisaatiotiedot")
case class Organisaatiohenkilö(
  @Description("Henkilön koko nimi, esimerkiksi \"Matti Meikäläinen\"")
  nimi: String,
  @Description("Henkilön titteli organisaatiossa, esimerkiksi \"rehtori\"")
  titteli: LocalizedString,
  @Description("Organisaation tunnistetiedot")
  organisaatio: Organisaatio
)