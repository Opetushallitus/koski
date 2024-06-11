package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, OnlyWhen, Title}

import java.time.LocalDate

object SurePerusopetuksenOpiskeluoikeus {
  def apply(oo: PerusopetuksenOpiskeluoikeus): SureOpiskeluoikeus =
    SureOpiskeluoikeus(
      oo = oo,
      suoritukset = oo.suoritukset.flatMap(SureNuortenPerusopetuksenSuoritus.apply),
      lisätiedot = oo.lisätiedot.flatMap(SureNuortenPerusopetuksenLisätiedot.apply),
    )
}

sealed trait SureNuortenPerusopetuksenSuoritus extends SurePäätasonSuoritus {
  def tyyppi: Koodistokoodiviite
  def suorituskieli: Koodistokoodiviite
}

object SureNuortenPerusopetuksenSuoritus {
  def apply(pts: PerusopetuksenPäätasonSuoritus): Option[SureNuortenPerusopetuksenSuoritus] =
    pts match {
      case s: NuortenPerusopetuksenOppimääränSuoritus =>
        Some(SureNuortenPerusopetuksenOppimäärä(s))
      case s: PerusopetuksenVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "9" =>
        Some(SureNuortenPerusopetuksenYhdeksäsLuokka(s))
      case s: NuortenPerusopetuksenOppiaineenOppimääränSuoritus =>
        Some(SureNuortenPerusopetusAineopetus(s))
      case _ => None
    }
}

@Title("Nuorten perusopetuksen oppimäärä")
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

@Title("Nuorten perusopetuksen yhdeksäs luokka")
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

@Title("Nuorten perusopetuksen aineopetus")
case class SureNuortenPerusopetusAineopetus(
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset.")
  @Tooltip("Päättötodistukseen liittyvät oppiaineen suoritukset.")
  @Title("Oppiaine")
  @FlattenInUI
  koulutusmoduuli: NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  @Description("Luokka-asteen tunniste (1-9). Minkä vuosiluokan mukaisesta oppiainesuorituksesta on kyse.")
  @Tooltip("Minkä vuosiluokan mukaisesta oppiainesuorituksesta on kyse")
  @Title("Luokka-aste")
  @KoodistoUri("perusopetuksenluokkaaste")
  @OnlyWhen("suoritustapa/koodiarvo", "erityinentutkinto")
  luokkaAste: Option[Koodistokoodiviite] = None,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("nuortenperusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nuortenperusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends SureNuortenPerusopetuksenSuoritus

object SureNuortenPerusopetusAineopetus {
  def apply(s: NuortenPerusopetuksenOppiaineenOppimääränSuoritus): SureNuortenPerusopetusAineopetus =
    SureNuortenPerusopetusAineopetus(
      koulutusmoduuli = s.koulutusmoduuli,
      toimipiste = s.toimipiste,
      arviointi = s.arviointi,
      vahvistus = s.vahvistus,
      suoritustapa = s.suoritustapa,
      luokkaAste = s.luokkaAste,
      suorituskieli = s.suorituskieli,
      muutSuorituskielet = s.muutSuorituskielet,
      todistuksellaNäkyvätLisätiedot = s.todistuksellaNäkyvätLisätiedot,
      tyyppi = s.tyyppi,
    )
}

@Title("Nuorten perusopetuksen lisätiedot")
case class SureNuortenPerusopetuksenLisätiedot(
  @Description("Kotiopetusjaksot huoltajan päätöksestä alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole kotiopetuksessa. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Kotiopetusjaksot huoltajan päätöksestä alkamis- ja päättymispäivineen. Rahoituksen laskennassa käytettävä tieto.")
  kotiopetusjaksot: Option[List[Aikajakso]] = None,

  @Description("Erityisen tuen päätökset alkamis- ja päättymispäivineen. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen erityisen tuen päätösten alkamis- ja päättymispäivät. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  erityisenTuenPäätökset: Option[List[ErityisenTuenPäätös]] = None,
) extends SureOpiskeluoikeudenLisätiedot

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

