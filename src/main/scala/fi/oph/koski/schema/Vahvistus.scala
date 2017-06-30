package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation._

trait Vahvistus {
  @Description("Tutkinnon tai tutkinnonosan vahvistettu suorituspäivämäärä, eli päivämäärä jolloin suoritus on hyväksyttyä todennettua osaamista. Muoto YYYY-MM-DD")
  def päivä: LocalDate
  @Description("Organisaatio, joka suorituksen on vahvistanut")
  @Title("Organisaatio")
  def myöntäjäOrganisaatio: Organisaatio
  @Description("Myöntäjähenkilö/-henkilöt, eli suorituksen/todistuksen allekirjoittajat")
  @Title("Myöntäjät")
  def myöntäjäHenkilöt: List[Vahvistaja]

  def getPaikkakunta: Option[Koodistokoodiviite] = None
}

trait VahvistusPaikkakunnalla extends Vahvistus {
  @KoodistoUri("kunta")
  @Description("Paikkakunta, jossa suoritus on vahvistettu (allekirjoituksen paikkakunta)")
  def paikkakunta: Koodistokoodiviite
  override def getPaikkakunta: Option[Koodistokoodiviite] = Some(paikkakunta)
}

trait Henkilövahvistus extends Vahvistus {}
trait HenkilövahvistusValinnaisellaTittelillä extends Vahvistus {}

@Description("Suorituksen vahvistus organisaatio- ja henkilötiedoilla")
case class HenkilövahvistusPaikkakunnalla(
  päivä: LocalDate,
  paikkakunta: Koodistokoodiviite,
  myöntäjäOrganisaatio: Organisaatio,
  @MinItems(1)
  myöntäjäHenkilöt: List[Organisaatiohenkilö]
) extends Henkilövahvistus with VahvistusPaikkakunnalla

@Description("Suorituksen vahvistus organisaatio- ja henkilötiedoilla")
case class HenkilövahvistusIlmanPaikkakuntaa(
  päivä: LocalDate,
  myöntäjäOrganisaatio: Organisaatio,
  @MinItems(1)
  myöntäjäHenkilöt: List[Organisaatiohenkilö]
) extends Henkilövahvistus

@Description("Suorituksen vahvistus organisaatio- ja henkilötiedoilla")
case class HenkilövahvistusValinnaisellaTittelilläJaPaikkakunnalla(
  päivä: LocalDate,
  paikkakunta: Koodistokoodiviite,
  myöntäjäOrganisaatio: Organisaatio,
  @MinItems(1)
  myöntäjäHenkilöt: List[OrganisaatiohenkilöValinnaisellaTittelillä]
) extends HenkilövahvistusValinnaisellaTittelillä with VahvistusPaikkakunnalla

@Description("Suorituksen vahvistus organisaatio- ja henkilötiedoilla")
case class HenkilövahvistusValinnaisellaTittelilläJaIlmanPaikkakuntaa(
  päivä: LocalDate,
  myöntäjäOrganisaatio: Organisaatio,
  @MinItems(1)
  myöntäjäHenkilöt: List[OrganisaatiohenkilöValinnaisellaTittelillä]
) extends HenkilövahvistusValinnaisellaTittelillä

@Description("Suorituksen vahvistus organisaatiotiedoilla")
case class Organisaatiovahvistus(
  päivä: LocalDate,
  paikkakunta: Koodistokoodiviite,
  myöntäjäOrganisaatio: Organisaatio
) extends VahvistusPaikkakunnalla {
  def myöntäjäHenkilöt = Nil
}

trait Vahvistaja {
  @Description("Henkilön koko nimi, esimerkiksi \"Matti Meikäläinen\"")
  def nimi: String
  @Description("Organisaation tunnistetiedot")
  def organisaatio: Organisaatio
  def getTitteli: Option[LocalizedString]
}

@Description("Henkilö- ja organisaatiotiedot, mahdollisesti titteli.")
case class OrganisaatiohenkilöValinnaisellaTittelillä(
  nimi: String,
  titteli: Option[LocalizedString] = None,
  organisaatio: Organisaatio
) extends Vahvistaja {
  override def getTitteli: Option[LocalizedString] = titteli
}

@Description("Henkilö- ja organisaatiotiedot")
case class Organisaatiohenkilö(
  nimi: String,
  titteli: LocalizedString,
  organisaatio: Organisaatio
) extends Vahvistaja {
  override def getTitteli = Some(titteli)
}