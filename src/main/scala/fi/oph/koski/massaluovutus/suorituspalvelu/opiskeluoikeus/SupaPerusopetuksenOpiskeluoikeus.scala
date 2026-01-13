package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, OnlyWhen, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Perusopetuksen opiskeluoikeus")
case class SupaPerusopetuksenOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.perusopetus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  suoritukset: List[SupaPerusopetuksenPäätasonSuoritus],
  lisätiedot: Option[SupaPerusopetuksenOpiskeluoikeudenLisätiedot],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
) extends SupaOpiskeluoikeus

object SupaPerusopetuksenOpiskeluoikeus {
  def apply(oo: PerusopetuksenOpiskeluoikeus, oppijaOid: String): SupaPerusopetuksenOpiskeluoikeus =
    SupaPerusopetuksenOpiskeluoikeus(
      oppijaOid = oppijaOid,
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      alkamispäivä = oo.alkamispäivä,
      päättymispäivä = oo.päättymispäivä,
      suoritukset = oo.suoritukset.flatMap(SupaPerusopetuksenPäätasonSuoritus.apply),
      lisätiedot = oo.lisätiedot.flatMap(SupaPerusopetuksenOpiskeluoikeudenLisätiedot.apply),
      versionumero = oo.versionumero,
      aikaleima = oo.aikaleima
    )
}

sealed trait SupaPerusopetuksenPäätasonSuoritus extends SupaSuoritus with Suorituskielellinen

object SupaPerusopetuksenPäätasonSuoritus {

  def apply(pts: PerusopetuksenPäätasonSuoritus): Option[SupaPerusopetuksenPäätasonSuoritus] = {

    lazy val is7tai8VuosiluokanSuoritus: Boolean = pts match {
      case s: PerusopetuksenVuosiluokanSuoritus =>
        s.koulutusmoduuli.tunniste.koodiarvo == "7" || s.koulutusmoduuli.tunniste.koodiarvo == "8"
      case _ => false
    }

    pts match {
      case s: NuortenPerusopetuksenOppimääränSuoritus =>
        Some(SupaNuortenPerusopetuksenOppimääränSuoritus(s))
      case s: PerusopetuksenVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "9" || is7tai8VuosiluokanSuoritus =>
        Some(SupaPerusopetuksenYhdeksännenVuosiluokanSuoritus(s, !is7tai8VuosiluokanSuoritus))
      case s: NuortenPerusopetuksenOppiaineenOppimääränSuoritus =>
        Some(SupaNuortenPerusopetuksenOppiaineenOppimääränSuoritus(s))
      case _ => None
    }
  }
}

@Title("Nuorten perusopetuksen oppimäärän suoritus")
case class SupaNuortenPerusopetuksenOppimääränSuoritus(
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: NuortenPerusopetus,
  vahvistus: Option[SupaVahvistus],
  suorituskieli: Koodistokoodiviite,
  koulusivistyskieli: Option[List[Koodistokoodiviite]],
  osasuoritukset: Option[List[NuortenPerusopetuksenOppiaineenSuoritus]]
) extends SupaPerusopetuksenPäätasonSuoritus
  with Suorituskielellinen
  with SupaVahvistuksellinen
  with Koulusivistyskieli

object SupaNuortenPerusopetuksenOppimääränSuoritus {
  def apply(s: NuortenPerusopetuksenOppimääränSuoritus): SupaNuortenPerusopetuksenOppimääränSuoritus =
    SupaNuortenPerusopetuksenOppimääränSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      suorituskieli = s.suorituskieli,
      koulusivistyskieli = s.koulusivistyskieli,
      osasuoritukset = s.osasuoritukset.map(_.collect { case os: NuortenPerusopetuksenOppiaineenSuoritus => os }).filter(_.nonEmpty),
    )
}

@Title("Perusopetuksen vuosiluokan suoritus")
case class SupaPerusopetuksenYhdeksännenVuosiluokanSuoritus(
  @KoodistoKoodiarvo("perusopetuksenvuosiluokka")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PerusopetuksenLuokkaAste,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  suorituskieli: Koodistokoodiviite,
  jääLuokalle: Boolean,
  osasuoritukset: Option[List[NuortenPerusopetuksenOppiaineenSuoritus]],
) extends SupaPerusopetuksenPäätasonSuoritus
  with Suorituskielellinen
  with SupaVahvistuksellinen

object SupaPerusopetuksenYhdeksännenVuosiluokanSuoritus {
  def apply(s: PerusopetuksenVuosiluokanSuoritus, lisääOsasuoritukset: Boolean): SupaPerusopetuksenYhdeksännenVuosiluokanSuoritus =
    SupaPerusopetuksenYhdeksännenVuosiluokanSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      suorituskieli = s.suorituskieli,
      jääLuokalle = s.jääLuokalle,
      osasuoritukset = if(lisääOsasuoritukset) s.osasuoritukset.map(_.collect { case os: NuortenPerusopetuksenOppiaineenSuoritus => os }).filter(_.nonEmpty) else None,
    )
}

@Title("Nuorten perusopetuksen oppiaineen oppimäärän suoritus")
case class SupaNuortenPerusopetuksenOppiaineenOppimääränSuoritus(
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset.")
  koulutusmoduuli: NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  vahvistus: Option[SupaVahvistus] = None,
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
) extends SupaPerusopetuksenPäätasonSuoritus with SupaVahvistuksellinen

object SupaNuortenPerusopetuksenOppiaineenOppimääränSuoritus {
  def apply(s: NuortenPerusopetuksenOppiaineenOppimääränSuoritus): SupaNuortenPerusopetuksenOppiaineenOppimääränSuoritus =
    SupaNuortenPerusopetuksenOppiaineenOppimääränSuoritus(
      koulutusmoduuli = s.koulutusmoduuli,
      toimipiste = s.toimipiste,
      arviointi = s.arviointi,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      suoritustapa = s.suoritustapa,
      luokkaAste = s.luokkaAste,
      suorituskieli = s.suorituskieli,
      muutSuorituskielet = s.muutSuorituskielet,
      todistuksellaNäkyvätLisätiedot = s.todistuksellaNäkyvätLisätiedot,
      tyyppi = s.tyyppi,
    )
}

@Title("Perusopetuksen opiskeluoikeuden lisätiedot")
case class SupaPerusopetuksenOpiskeluoikeudenLisätiedot(
  @Description("Kotiopetusjaksot huoltajan päätöksestä alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole kotiopetuksessa. Rahoituksen laskennassa käytettävä tieto.")
  kotiopetusjaksot: Option[List[Aikajakso]] = None,

  @Description("Erityisen tuen päätökset alkamis- ja päättymispäivineen. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  erityisenTuenPäätökset: Option[List[SupaErityisenTuenPäätös]] = None,

  @Description("Oppilas on vuosiluokkiin sitomattomassa opetuksessa (kyllä/ei).")
  @Title("Vuosiluokkiin sitomaton opetus")
  vuosiluokkiinSitoutumatonOpetus: Option[Boolean] = Some(false),
)

object SupaPerusopetuksenOpiskeluoikeudenLisätiedot {
  def apply(lt: PerusopetuksenOpiskeluoikeudenLisätiedot): Option[SupaPerusopetuksenOpiskeluoikeudenLisätiedot] =
    if (lt.kotiopetusjaksot.exists(_.nonEmpty) ||
      lt.erityisenTuenPäätös.isDefined ||
      lt.erityisenTuenPäätökset.exists(_.nonEmpty) ||
      lt.vuosiluokkiinSitoutumatonOpetus.contains(true)) {
      Some(SupaPerusopetuksenOpiskeluoikeudenLisätiedot(
        kotiopetusjaksot = lt.kotiopetusjaksot,
        erityisenTuenPäätökset =
          if (lt.erityisenTuenPäätökset.isDefined) {
            lt.erityisenTuenPäätökset.map(_.map(SupaErityisenTuenPäätös.apply))
          } else if (lt.erityisenTuenPäätös.isDefined) {
            Some(List(lt.erityisenTuenPäätös.get).map(SupaErityisenTuenPäätös.apply))
          } else {
            None
          },
        vuosiluokkiinSitoutumatonOpetus = lt.vuosiluokkiinSitoutumatonOpetus
      ))
    } else {
      None
    }
}

case class SupaErityisenTuenPäätös(
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

object SupaErityisenTuenPäätös {
  def apply(s: ErityisenTuenPäätös): SupaErityisenTuenPäätös =
    SupaErityisenTuenPäätös(
      alku = s.alku,
      loppu = s.loppu,
      opiskeleeToimintaAlueittain = s.opiskeleeToimintaAlueittain,
    )
}
