package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, OnlyWhen, Title}

import java.time.LocalDate

@Title("Perusopetus")
case class SurePerusopetuksenOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.perusopetus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila,
  suoritukset: List[SurePerusopetuksenPäätasonSuoritus],
  lisätiedot: Option[SurePerusopetuksenOpiskeluoikeudenLisätiedot],
) extends SureOpiskeluoikeus

object SurePerusopetuksenOpiskeluoikeus {
  def apply(oo: PerusopetuksenOpiskeluoikeus): SurePerusopetuksenOpiskeluoikeus =
    SurePerusopetuksenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.flatMap(SurePerusopetuksenPäätasonSuoritus.apply),
      lisätiedot = oo.lisätiedot.flatMap(SurePerusopetuksenOpiskeluoikeudenLisätiedot.apply),
    )
}

sealed trait SurePerusopetuksenPäätasonSuoritus extends SureSuoritus with Suorituskielellinen

object SurePerusopetuksenPäätasonSuoritus {
  def apply(pts: PerusopetuksenPäätasonSuoritus): Option[SurePerusopetuksenPäätasonSuoritus] =
    pts match {
      case s: NuortenPerusopetuksenOppimääränSuoritus =>
        Some(SureNuortenPerusopetuksenOppimääränSuoritus(s))
      case s: PerusopetuksenVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "9" =>
        Some(SurePerusopetuksenYhdeksännenVuosiluokanSuoritus(s))
      case s: NuortenPerusopetuksenOppiaineenOppimääränSuoritus =>
        Some(SureNuortenPerusopetuksenOppiaineenOppimääränSuoritus(s))
      case _ => None
    }
}

@Title("Oppimäärän suoritus")
case class SureNuortenPerusopetuksenOppimääränSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: NuortenPerusopetus,
  vahvistuspäivä: Option[LocalDate],
  suorituskieli: Koodistokoodiviite,
) extends SurePerusopetuksenPäätasonSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

object SureNuortenPerusopetuksenOppimääränSuoritus {
  def apply(s: NuortenPerusopetuksenOppimääränSuoritus): SureNuortenPerusopetuksenOppimääränSuoritus =
    SureNuortenPerusopetuksenOppimääränSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      suorituskieli = s.suorituskieli,
    )
}

@Title("Yhdeksännen vuosiluokan suoritus")
case class SurePerusopetuksenYhdeksännenVuosiluokanSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PerusopetuksenLuokkaAste,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[NuortenPerusopetuksenOppiaineenSuoritus]],
) extends SurePerusopetuksenPäätasonSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

object SurePerusopetuksenYhdeksännenVuosiluokanSuoritus {
  def apply(s: PerusopetuksenVuosiluokanSuoritus): SurePerusopetuksenYhdeksännenVuosiluokanSuoritus =
    SurePerusopetuksenYhdeksännenVuosiluokanSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.map(_.collect { case os: NuortenPerusopetuksenOppiaineenSuoritus => os }),
    )
}

@Title("Nuorten perusopetuksen oppiaineen oppimäärän suoritus")
case class SureNuortenPerusopetuksenOppiaineenOppimääränSuoritus(
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset.")
  koulutusmoduuli: NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  @Description("Luokka-asteen tunniste (1-9). Minkä vuosiluokan mukaisesta oppiainesuorituksesta on kyse.")
  @KoodistoUri("perusopetuksenluokkaaste")
  @OnlyWhen("suoritustapa/koodiarvo", "erityinentutkinto")
  luokkaAste: Option[Koodistokoodiviite] = None,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("nuortenperusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nuortenperusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends SurePerusopetuksenPäätasonSuoritus

object SureNuortenPerusopetuksenOppiaineenOppimääränSuoritus {
  def apply(s: NuortenPerusopetuksenOppiaineenOppimääränSuoritus): SureNuortenPerusopetuksenOppiaineenOppimääränSuoritus =
    SureNuortenPerusopetuksenOppiaineenOppimääränSuoritus(
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

@Title("Perusopetuksen opiskeluoikeuden lisätiedot")
case class SurePerusopetuksenOpiskeluoikeudenLisätiedot(
  @Description("Kotiopetusjaksot huoltajan päätöksestä alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole kotiopetuksessa. Rahoituksen laskennassa käytettävä tieto.")
  kotiopetusjaksot: Option[List[Aikajakso]] = None,

  @Description("Erityisen tuen päätökset alkamis- ja päättymispäivineen. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  erityisenTuenPäätökset: Option[List[SureErityisenTuenPäätös]] = None,
)

object SurePerusopetuksenOpiskeluoikeudenLisätiedot {
  def apply(lt: PerusopetuksenOpiskeluoikeudenLisätiedot): Option[SurePerusopetuksenOpiskeluoikeudenLisätiedot] =
    if (lt.kotiopetusjaksot.exists(_.nonEmpty) || lt.erityisenTuenPäätös.isDefined || lt.erityisenTuenPäätökset.exists(_.nonEmpty)) {
      Some(SurePerusopetuksenOpiskeluoikeudenLisätiedot(
        kotiopetusjaksot = lt.kotiopetusjaksot,
        erityisenTuenPäätökset =
          if (lt.erityisenTuenPäätökset.isDefined) {
            lt.erityisenTuenPäätökset.map(_.map(SureErityisenTuenPäätös.apply))
          } else if (lt.erityisenTuenPäätös.isDefined) {
            Some(List(lt.erityisenTuenPäätös.get).map(SureErityisenTuenPäätös.apply))
          } else {
            None
          }
      ))
    } else {
      None
    }
}

case class SureErityisenTuenPäätös(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @Description(
    """Oppilas opiskelee toiminta-alueittain (true/false).
Toiminta-alueittain opiskelussa oppilaalla on yksilöllistetty oppimäärä ja opetus järjestetty toiminta-alueittain.
Tuolloin oppilaalla on aina erityisen tuen päätös.
Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.
Huom: toiminta-alue arviointeineen on kuvattu oppiaineen suorituksessa.""")
  opiskeleeToimintaAlueittain: Boolean = false,
)

object SureErityisenTuenPäätös {
  def apply(s: ErityisenTuenPäätös): SureErityisenTuenPäätös =
    SureErityisenTuenPäätös(
      alku = s.alku,
      loppu = s.loppu,
      opiskeleeToimintaAlueittain = s.opiskeleeToimintaAlueittain,
    )
}
