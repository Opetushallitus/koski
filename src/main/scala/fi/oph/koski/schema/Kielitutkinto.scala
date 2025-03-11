package fi.oph.koski.schema

import fi.oph.koski.schema.LocalizedString.unlocalized
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

import java.time.{LocalDate, LocalDateTime}

// Kielitutkinnon opiskeluoikeus

case class KielitutkinnonOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
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

  def lisätiedot: Option[KielitutkinnonLisätiedot] = None
}

case class KielitutkinnonOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
  alku: LocalDate,
  @Description("Yleisessä kielitutkinnossa 'lasna' vastaa tutkintopäivää, 'hyvaksytystisuoritettu' arviointipäivää")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("hyvaksytystisuoritettu")
  @KoodistoKoodiarvo("mitatoity")
  tila: Koodistokoodiviite,
) extends KoskiOpiskeluoikeusjakso

case class KielitutkinnonLisätiedot() extends OpiskeluoikeudenLisätiedot

// Kielitutkinnon päätason suoritus

trait KielitutkinnonPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus

case class YleisenKielitutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: YleinenKielitutkinto,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Päivämäärävahvistus] = None,
  @Description("Kielitutkinnon suoritukset osat")
  override val osasuoritukset: Option[List[YleisenKielitutkinnonOsanSuoritus]],
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

// Kielitutkinnon osasuoritus

case class YleisenKielitutkinnonOsanSuoritus(
  @KoodistoKoodiarvo("yleisenkielitutkinnonosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("yleisenkielitutkinnonosa", "suorituksentyyppi"),
  koulutusmoduuli: YleisenKielitutkinnonOsa,
  arviointi: Option[List[YleisenKielitutkinnonOsanArviointi]],
) extends Suoritus with Vahvistukseton

case class YleisenKielitutkinnonOsa(
  @KoodistoUri("ykisuorituksenosa")
  tunniste: Koodistokoodiviite,
) extends Koulutusmoduuli {
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

case class YleisenKielitutkinnonOsanArviointi(
  @KoodistoUri("ykiarvosana")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
) extends ArviointiPäivämäärällä {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
  override def hyväksytty: Boolean = List("1", "2", "3", "4", "5", "6").contains(arvosana.koodiarvo)
  override def arvosanaKirjaimin: LocalizedString = arvosana.nimi.getOrElse(unlocalized(arvosana.koodiarvo))
}
