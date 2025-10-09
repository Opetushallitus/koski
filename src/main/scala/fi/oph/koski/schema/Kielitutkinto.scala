package fi.oph.koski.schema

import fi.oph.koski.schema.LocalizedString.unlocalized
import fi.oph.koski.schema.annotation.{AllowKoulutustoimijaOidAsOppilaitos, KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.util.DateOrdering.localDateOrdering
import fi.oph.scalaschema.annotation.{Description, Discriminator, MaxItems, MinItems, SyntheticProperty, Title}

import java.time.{LocalDate, LocalDateTime}

// Kielitutkinnon opiskeluoikeus

case class KielitutkinnonOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  @AllowKoulutustoimijaOidAsOppilaitos
  oppilaitos: Option[Oppilaitos] = None,
  koulutustoimija: Option[Koulutustoimija] = None,
  tila: KielitutkinnonOpiskeluoikeudenTila,
  @MinItems(1) @MaxItems(1)
  suoritukset: List[KielitutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.kielitutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.kielitutkinto,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmäkytkentäPurettu: Option[LähdejärjestelmäkytkennänPurkaminen] = None,
) extends KoskeenTallennettavaOpiskeluoikeus with Organisaatiohistoriaton {
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija): KoskeenTallennettavaOpiskeluoikeus = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withOppilaitos(oppilaitos: Oppilaitos): KoskeenTallennettavaOpiskeluoikeus = this.copy(oppilaitos = Some(oppilaitos))
  override def arvioituPäättymispäivä: Option[LocalDate] = None
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None

  def lisätiedot: Option[OpiskeluoikeudenLisätiedot] = None

  def isOphValtionhallinnonKielitutkinto: Boolean = suoritukset.exists {
    case _: ValtionhallinnonKielitutkinnonSuoritus => true
    case _ => false
  }
}

case class KielitutkinnonOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
  alku: LocalDate,
  @Description("Yleisessä kielitutkinnossa 'lasna' vastaa tutkintopäivää, 'hyvaksytystisuoritettu' arviointipäivää")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("hyvaksytystisuoritettu")
  @KoodistoKoodiarvo("paattynyt")
  @KoodistoKoodiarvo("mitatoity")
  tila: Koodistokoodiviite,
) extends KoskiPäällekkäisenPäivämääränSallivaOpiskeluoikeusjakso

trait KielitutkinnonPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus

// Yleisen kielitutkinnon päätason suoritus

case class YleisenKielitutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: YleinenKielitutkinto,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Päivämäärävahvistus] = None,
  @Description("Yleisen kielitutkinnon osakokeet")
  override val osasuoritukset: Option[List[YleisenKielitutkinnonOsakokeenSuoritus]],
  @KoodistoKoodiarvo("yleinenkielitutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("yleinenkielitutkinto", koodistoUri = "suorituksentyyppi"),
  @KoodistoUri("ykiarvosana")
  yleisarvosana: Option[Koodistokoodiviite],
) extends KielitutkinnonPäätasonSuoritus with Arvioinniton

case class YleinenKielitutkinto(
  @KoodistoUri("ykitutkintotaso")
  tunniste: Koodistokoodiviite,
  @KoodistoUri("kieli")
  kieli: Koodistokoodiviite,
) extends Koulutusmoduuli {
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

// Yleisen kielitutkinnon osakoe

case class YleisenKielitutkinnonOsakokeenSuoritus(
  @KoodistoKoodiarvo("yleisenkielitutkinnonosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("yleisenkielitutkinnonosa", "suorituksentyyppi"),
  koulutusmoduuli: YleisenKielitutkinnonOsakoe,
  arviointi: Option[List[YleisenKielitutkinnonOsakokeenArviointi]],
) extends Suoritus with Vahvistukseton

case class YleisenKielitutkinnonOsakoe(
  @KoodistoUri("ykisuorituksenosa")
  tunniste: Koodistokoodiviite,
) extends Koulutusmoduuli {
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

case class YleisenKielitutkinnonOsakokeenArviointi(
  @KoodistoUri("ykiarvosana")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
) extends ArviointiPäivämäärällä {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
  override def hyväksytty: Boolean = List("1", "2", "3", "4", "5", "6").contains(arvosana.koodiarvo)
  override def arvosanaKirjaimin: LocalizedString = arvosana.nimi.getOrElse(unlocalized(arvosana.koodiarvo))
}

// Valtiohallinnon kielitutkinnon päätason suoritus

case class ValtionhallinnonKielitutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: ValtionhallinnonKielitutkinto,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[PäivämäärävahvistusPaikkakunnalla] = None,
  override val osasuoritukset: Option[List[ValtionhallinnonKielitutkinnonKielitaidonSuoritus]],
  @KoodistoKoodiarvo("valtionhallinnonkielitutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valtionhallinnonkielitutkinto", koodistoUri = "suorituksentyyppi"),
) extends KielitutkinnonPäätasonSuoritus with Arvioinniton

case class ValtionhallinnonKielitutkinto(
  @KoodistoUri("vkttutkintotaso")
  tunniste: Koodistokoodiviite,
  @KoodistoUri("kieli")
  kieli: Koodistokoodiviite,
) extends Koulutusmoduuli {
  def nimi: LocalizedString =
    unlocalized(List(
      tunniste,
      kieli,
    ).map(k => k.nimi.getOrElse(unlocalized(k.koodiarvo))).mkString(", "))
}

// Valtionhallunnon kielitutkinnon kielitaidot

trait ValtionhallinnonKielitutkinnonKielitaidonSuoritus extends Suoritus with Vahvistukseton {
  @Discriminator
  def koulutusmoduuli: ValtionhallinnonKielitutkinnonKielitaito
  @KoodistoKoodiarvo("valtionhallinnonkielitaito")
  def tyyppi: Koodistokoodiviite
  def arviointi: Option[List[ValtionhallinnonKielitutkinnonArviointi]]

  override def valmis: Boolean = arviointi.isDefined
  override def salliDuplikaatit = true

  @SyntheticProperty
  def tutkintopäiväTodistuksella: Option[LocalDate] =
    arviointi.flatMap { arviointi =>
      if (arviointi.nonEmpty) {
        val tutkinnonArvosana = arviointi
          .max(ValtionhallinnonKielitutkinnonArviointi.ordering)
          .arvosana
        val tutkinnonArvosananMukaisetOsasuoritukset = osasuoritukset
          .toList
          .flatten
          .filter(_.arviointi.exists(_.exists(_.arvosana == tutkinnonArvosana)))
        val osakokeidenTutkintopäivät = tutkinnonArvosananMukaisetOsasuoritukset
          .flatMap(_.alkamispäivä)
        if (osakokeidenTutkintopäivät.nonEmpty) Some(osakokeidenTutkintopäivät.max) else None
      } else {
        None
      }
    }
}

trait ValtionhallinnonKielitutkinnonKielitaito extends Koulutusmoduuli {
  @KoodistoUri("vktkielitaito")
  def tunniste: Koodistokoodiviite

  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

case class ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus(
  koulutusmoduuli: ValtionhallinnonKielitutkinnonSuullinenKielitaito,
  @KoodistoKoodiarvo("valtionhallinnonkielitaito")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valtionhallinnonkielitaito", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[ValtionhallinnonKielitutkinnonArviointi]] = None,
  override val osasuoritukset: Option[List[ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus]],
  override val alkamispäivä: Option[LocalDate] = None
) extends ValtionhallinnonKielitutkinnonKielitaidonSuoritus

case class ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus(
  koulutusmoduuli: ValtionhallinnonKielitutkinnonKirjallinenKielitaito,
  @KoodistoKoodiarvo("valtionhallinnonkielitaito")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valtionhallinnonkielitaito", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[ValtionhallinnonKielitutkinnonArviointi]] = None,
  override val osasuoritukset: Option[List[ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus]],
  override val alkamispäivä: Option[LocalDate] = None
) extends ValtionhallinnonKielitutkinnonKielitaidonSuoritus

case class ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus(
  koulutusmoduuli: ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito,
  @KoodistoKoodiarvo("valtionhallinnonkielitaito")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valtionhallinnonkielitaito", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[ValtionhallinnonKielitutkinnonArviointi]] = None,
  override val osasuoritukset: Option[List[ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus]],
  override val alkamispäivä: Option[LocalDate] = None
) extends ValtionhallinnonKielitutkinnonKielitaidonSuoritus

case class ValtionhallinnonKielitutkinnonSuullinenKielitaito(
  @KoodistoKoodiarvo("suullinen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("suullinen", "vktkielitaito"),
) extends ValtionhallinnonKielitutkinnonKielitaito

case class ValtionhallinnonKielitutkinnonKirjallinenKielitaito(
  @KoodistoKoodiarvo("kirjallinen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("kirjallinen", "vktkielitaito"),
) extends ValtionhallinnonKielitutkinnonKielitaito

case class ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito(
  @KoodistoKoodiarvo("ymmartaminen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("ymmartaminen", "vktkielitaito"),
) extends ValtionhallinnonKielitutkinnonKielitaito

// Valtionhallinnon kielitutkinnon osakoe

trait ValtionhallinnonKielitutkinnonOsakokeenSuoritus extends Suoritus with Vahvistukseton {
  @KoodistoKoodiarvo("valtionhallinnonkielitutkinnonosakoe")
  def tyyppi: Koodistokoodiviite
  def koulutusmoduuli: ValtionhallinnonKielitutkinnonOsakoe
  def arviointi: Option[List[ValtionhallinnonKielitutkinnonArviointi]]

  override def valmis: Boolean = arviointi.isDefined
}

// Valtionhallinnon kielitutkinnon osakokeet

case class ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus(
  @KoodistoKoodiarvo("valtionhallinnonkielitutkinnonosakoe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valtionhallinnonkielitutkinnonosakoe", "suorituksentyyppi"),
  koulutusmoduuli: ValtionhallinnonSuullisenKielitaidonOsakoe,
  arviointi: Option[List[ValtionhallinnonKielitutkinnonArviointi]],
  override val alkamispäivä: Option[LocalDate] = None
) extends ValtionhallinnonKielitutkinnonOsakokeenSuoritus

case class ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus(
  @KoodistoKoodiarvo("valtionhallinnonkielitutkinnonosakoe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valtionhallinnonkielitutkinnonosakoe", "suorituksentyyppi"),
  koulutusmoduuli: ValtionhallinnonKirjallisenKielitaidonOsakoe,
  arviointi: Option[List[ValtionhallinnonKielitutkinnonArviointi]],
  override val alkamispäivä: Option[LocalDate] = None
) extends ValtionhallinnonKielitutkinnonOsakokeenSuoritus

case class ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus(
  @KoodistoKoodiarvo("valtionhallinnonkielitutkinnonosakoe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valtionhallinnonkielitutkinnonosakoe", "suorituksentyyppi"),
  koulutusmoduuli: ValtionhallinnonYmmärtämisenKielitaidonOsakoe,
  arviointi: Option[List[ValtionhallinnonKielitutkinnonArviointi]],
  override val alkamispäivä: Option[LocalDate] = None
) extends ValtionhallinnonKielitutkinnonOsakokeenSuoritus

trait ValtionhallinnonKielitutkinnonOsakoe extends Koulutusmoduuli {
  @KoodistoUri("vktosakoe")
  def tunniste: Koodistokoodiviite
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

trait ValtionhallinnonSuullisenKielitaidonOsakoe extends ValtionhallinnonKielitutkinnonOsakoe
trait ValtionhallinnonKirjallisenKielitaidonOsakoe extends ValtionhallinnonKielitutkinnonOsakoe
trait ValtionhallinnonYmmärtämisenKielitaidonOsakoe extends ValtionhallinnonKielitutkinnonOsakoe

case class ValtionhallinnonPuhumisenOsakoe(
  @KoodistoKoodiarvo("puhuminen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("puhuminen", "vktosakoe"),
) extends ValtionhallinnonSuullisenKielitaidonOsakoe

case class ValtionhallinnonPuheenYmmärtämisenOsakoe(
  @KoodistoKoodiarvo("puheenymmartaminen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("puheenymmartaminen", "vktosakoe"),
) extends ValtionhallinnonSuullisenKielitaidonOsakoe with ValtionhallinnonYmmärtämisenKielitaidonOsakoe

case class ValtionhallinnonKirjoittamisenOsakoe(
  @KoodistoKoodiarvo("kirjoittaminen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("kirjoittaminen", "vktosakoe"),
) extends ValtionhallinnonKirjallisenKielitaidonOsakoe

case class ValtionhallinnonTekstinYmmärtämisenOsakoe(
  @KoodistoKoodiarvo("tekstinymmartaminen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("tekstinymmartaminen", "vktosakoe"),
) extends ValtionhallinnonKirjallisenKielitaidonOsakoe with ValtionhallinnonYmmärtämisenKielitaidonOsakoe

// Valtionhallinnon kielitutkinnon arviointi

case class ValtionhallinnonKielitutkinnonArviointi(
  @KoodistoUri("vktarvosana")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
) extends ArviointiPäivämäärällä {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
  override def hyväksytty: Boolean = arvosana.koodiarvo != "hylatty"
  override def arvosanaKirjaimin: LocalizedString = arvosana.nimi.getOrElse(unlocalized(arvosana.koodiarvo))
}

object ValtionhallinnonKielitutkinnonArviointi {
  private val order = List("hylatty", "tyydyttava", "hyva", "erinomainen")
  val ordering: Ordering[ValtionhallinnonKielitutkinnonArviointi] =
    Ordering.fromLessThan[ValtionhallinnonKielitutkinnonArviointi]((a, b) => order.indexOf(a.arvosana.koodiarvo) - order.indexOf(b.arvosana.koodiarvo) < 0)
}
