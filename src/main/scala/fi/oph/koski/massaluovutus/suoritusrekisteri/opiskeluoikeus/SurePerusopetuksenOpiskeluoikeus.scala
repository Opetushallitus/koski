package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureOpiskeluoikeus
import fi.oph.koski.schema.{Aikajakso, ErityisenTuenPäätös, Koodistokoodiviite, NuortenPerusopetuksenOpiskeluoikeudenTila, NuortenPerusopetuksenOppiaineenSuoritus, NuortenPerusopetuksenOppimääränSuoritus, NuortenPerusopetus, Oppilaitos, PerusopetuksenLuokkaAste, PerusopetuksenOpiskeluoikeudenLisätiedot, PerusopetuksenOpiskeluoikeus, PerusopetuksenPäätasonSuoritus, PerusopetuksenVuosiluokanSuoritus}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, OksaUri, Tooltip}
import fi.oph.scalaschema.annotation.Description

import java.time.LocalDate

case class SurePerusopetuksenOpiskeluoikeus(
  @KoodistoKoodiarvo("perusopetus")
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila,
  suoritukset: List[SureNuortenPerusopetuksenSuoritus],
  lisätiedot: Option[SureNuortenPerusopetuksenLisätiedot],
) extends SureOpiskeluoikeus

object SurePerusopetuksenOpiskeluoikeus {
  def apply(oo: PerusopetuksenOpiskeluoikeus): SurePerusopetuksenOpiskeluoikeus =
    SurePerusopetuksenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.flatMap(SureNuortenPerusopetuksenSuoritus.apply),
      lisätiedot = oo.lisätiedot.flatMap(SureNuortenPerusopetuksenLisätiedot.apply),
    )
}

sealed trait SureNuortenPerusopetuksenSuoritus extends SurePäätasonSuoritus {
  def tyyppi: Koodistokoodiviite
  def vahvistuspäivä: Option[LocalDate]
  def suorituskieli: Koodistokoodiviite
}

object SureNuortenPerusopetuksenSuoritus {
  def apply(pts: PerusopetuksenPäätasonSuoritus): Option[SureNuortenPerusopetuksenSuoritus] =
    pts match {
      case s: NuortenPerusopetuksenOppimääränSuoritus =>
        Some(SureNuortenPerusopetuksenOppimäärä(s))
      case s: PerusopetuksenVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "9" =>
        Some(SureNuortenPerusopetuksenYhdeksäsLuokka(s))
      case _ => None
    }
}

case class SureNuortenPerusopetuksenOppimäärä(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: NuortenPerusopetus,
  vahvistuspäivä: Option[LocalDate],
  suorituskieli: Koodistokoodiviite,
) extends SureNuortenPerusopetuksenSuoritus

object SureNuortenPerusopetuksenOppimäärä {
  def apply(s: NuortenPerusopetuksenOppimääränSuoritus): SureNuortenPerusopetuksenOppimäärä =
    SureNuortenPerusopetuksenOppimäärä(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      suorituskieli = s.suorituskieli,
    )
}

case class SureNuortenPerusopetuksenYhdeksäsLuokka(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PerusopetuksenLuokkaAste,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[NuortenPerusopetuksenOppiaineenSuoritus]],
) extends SureNuortenPerusopetuksenSuoritus

object SureNuortenPerusopetuksenYhdeksäsLuokka {
  def apply(s: PerusopetuksenVuosiluokanSuoritus): SureNuortenPerusopetuksenYhdeksäsLuokka =
    SureNuortenPerusopetuksenYhdeksäsLuokka(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.map(_.collect { case os: NuortenPerusopetuksenOppiaineenSuoritus => os }),
    )
}

case class SureNuortenPerusopetuksenLisätiedot(
  @Description("Kotiopetusjaksot huoltajan päätöksestä alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole kotiopetuksessa. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Kotiopetusjaksot huoltajan päätöksestä alkamis- ja päättymispäivineen. Rahoituksen laskennassa käytettävä tieto.")
  kotiopetusjaksot: Option[List[Aikajakso]] = None,

  @Description("Erityisen tuen päätökset alkamis- ja päättymispäivineen. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen erityisen tuen päätösten alkamis- ja päättymispäivät. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  erityisenTuenPäätökset: Option[List[ErityisenTuenPäätös]] = None,
)

object SureNuortenPerusopetuksenLisätiedot {
  def apply(lt: PerusopetuksenOpiskeluoikeudenLisätiedot): Option[SureNuortenPerusopetuksenLisätiedot] =
    if (lt.kotiopetusjaksot.exists(_.nonEmpty) || lt.erityisenTuenPäätös.isDefined || lt.erityisenTuenPäätökset.exists(_.nonEmpty)) {
      Some(SureNuortenPerusopetuksenLisätiedot(
        kotiopetusjaksot = lt.kotiopetusjaksot,
        erityisenTuenPäätökset =
          if (lt.erityisenTuenPäätökset.isDefined) {
            lt.erityisenTuenPäätökset
          } else if (lt.erityisenTuenPäätös.isDefined) {
            Some(List(lt.erityisenTuenPäätös.get))
          } else {
            None
          }
      ))
    } else {
      None
    }
}
