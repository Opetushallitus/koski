package fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{InfoDescription, InfoLinkTitle, InfoLinkUrl, KoodistoKoodiarvo, KoodistoUri, ReadOnly, Tooltip}
import fi.oph.scalaschema.annotation.{Description, MaxItems, Title}

import java.time.{LocalDate, LocalDateTime}

case class MuunKuinSäännellynKoulutuksenOpiskeluoikeus (
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime],
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  tila: MuunKuinSäännellynKoulutuksenTila,
  @MaxItems(1)
  suoritukset: List[MuunKuinSäännellynKoulutuksenPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
  lisätiedot: Option[MuunKuinSäännellynKoulutuksenLisätiedot] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija): KoskeenTallennettavaOpiskeluoikeus =
    this.copy(koulutustoimija = Some(koulutustoimija))
  override def withOppilaitos(oppilaitos: Oppilaitos): KoskeenTallennettavaOpiskeluoikeus =
    this.copy(oppilaitos = Some(oppilaitos))
}

case class MuunKuinSäännellynKoulutuksenTila(
  opiskeluoikeusjaksot: List[MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso],
) extends OpiskeluoikeudenTila

case class MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso(
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("hyvaksytystisuoritettu")
  @KoodistoKoodiarvo("keskeytynyt")
  @KoodistoKoodiarvo("mitatoity")
  tila: Koodistokoodiviite,
  alku: LocalDate,
  @KoodistoKoodiarvo("14")
  @KoodistoKoodiarvo("15")
  override val opintojenRahoitus: Option[Koodistokoodiviite],
) extends KoskiOpiskeluoikeusjakso

case class MuunKuinSäännellynKoulutuksenLisätiedot(
  jotpaAsianumero: Option[Koodistokoodiviite] = None
) extends OpiskeluoikeudenLisätiedot with JotpaAsianumero

case class MuunKuinSäännellynKoulutuksenPäätasonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: MuuKuinSäänneltyKoulutus,
  @KoodistoKoodiarvo("muukuinsaanneltykoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("muukuinsaanneltykoulutus", "suorituksentyyppi"),
  vahvistus: Option[Päivämäärävahvistus] = None,
  toimipiste: OrganisaatioWithOid,
  override val osasuoritukset: Option[List[MuunKuinSäännellynKoulutuksenOsasuoritus]] = None,
  suorituskieli: Koodistokoodiviite,
  arviointi: Option[List[MuunKuinSäännellynKoulutuksenArviointi]] = None,
) extends KoskeenTallennettavaPäätasonSuoritus
  with MahdollisestiArvioinniton
  with Suorituskielellinen

case class MuuKuinSäänneltyKoulutus(
  @KoodistoKoodiarvo("999951")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999951", "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @ReadOnly("Laajuus lasketaan automaattisesti osasuoritusten laajuuksista.")
  laajuus: Option[LaajuusTunneissa] = None,
  @KoodistoUri("opintokokonaisuudet")
  @Description("Opintokokonaisuus")
  @Tooltip("Opintokokonaisuus")
  @InfoDescription("opintokokonaisuuden_tarkemmat_tiedot_eperusteissa")
  @InfoLinkTitle("opintokokonaisuudet_eperusteissa")
  @InfoLinkUrl("eperusteet_opintopolku_url")
  opintokokonaisuus: Koodistokoodiviite,
) extends Koulutus
  with KoulutusmoduuliValinnainenLaajuus

case class MuunKuinSäännellynKoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkomuks")
  @KoodistoKoodiarvo("hyvaksytty")
  @KoodistoKoodiarvo("hylatty")
  arvosana: Koodistokoodiviite,
  arviointipäivä: Option[LocalDate],
) extends KoodistostaLöytyväArviointi {
  override def hyväksytty: Boolean = MuunKuinSäännellynKoulutuksenArviointi.hyväksytty(arvosana)
  override def arvosanaKirjaimin: LocalizedString = LocalizedString.finnish(arvosana.koodiarvo)
  override def arvioitsijat: Option[List[SuorituksenArvioitsija]] = None
}

object MuunKuinSäännellynKoulutuksenArviointi {
  def hyväksytty(arvosana: Koodistokoodiviite): Boolean = arvosana.koodiarvo == "hyvaksytty"
}

case class MuunKuinSäännellynKoulutuksenOsasuoritus(
  koulutusmoduuli: MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli,
  @KoodistoKoodiarvo("muunkuinsaannellynkoulutuksenosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("muunkuinsaannellynkoulutuksenosasuoritus", "suorituksentyyppi"),
  vahvistus: Option[Vahvistus] = None,
  arviointi: Option[List[MuunKuinSäännellynKoulutuksenArviointi]] = None,
  override val osasuoritukset: Option[List[MuunKuinSäännellynKoulutuksenOsasuoritus]] = None,
) extends Suoritus
  with MahdollisestiArvioinniton

case class MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli(
  kuvaus: LocalizedString,
  tunniste: PaikallinenKoodi,
  laajuus: LaajuusTunneissa,
) extends KoulutusmoduuliPakollinenLaajuusTunneissa
  with PaikallinenKoulutusmoduuli
  with StorablePreference
