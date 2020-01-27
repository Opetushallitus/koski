package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.Description

@Description("Opiskelija opiskelee erityisen koulutustehtävän mukaisesti (ib, musiikki, urheilu, kielet, luonnontieteet, jne.). Kentän puuttuminen tai null-arvo tulkitaan siten, ettei opiskelija opiskele erityisen koulutustehtävän mukaisesti")
case class ErityisenKoulutustehtävänJakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Erityinen koulutustehtävä. Koodisto")
  @KoodistoUri("erityinenkoulutustehtava")
  @OksaUri("tmpOKSAID181", "erityinen koulutustehtävä")
  tehtävä: Koodistokoodiviite
) extends Jakso

trait ErityisenKoulutustehtävänJaksollinen {
  val erityisenKoulutustehtävänJaksot: Option[List[ErityisenKoulutustehtävänJakso]]
}
