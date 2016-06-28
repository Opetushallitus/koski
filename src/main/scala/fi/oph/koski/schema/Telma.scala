package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid] = None,
  tila: Option[AmmatillinenOpiskeluoikeudenTila] = None,
  läsnäolotiedot: Option[YleisetLäsnäolotiedot] = None,
  @MinItems(1) @MaxItems(1)
  suoritukset: List[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telma", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenSuoritus(
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  @Description("Työhön ja itsenäiseen elämään valmentava koulutuksen osasuoritukset")
  override val osasuoritukset: Option[List[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telma", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: TyöhönJaItsenäiseenElämäänValmentavaKoulutus,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends ValmentavaSuoritus

case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsanSuoritus(
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  @KoodistoKoodiarvo("telmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telmakoulutuksenosa", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa,
  arviointi: Option[List[TelmaArviointi]],
  tunnustettu: Option[Tunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None
) extends ValmentavanKoulutuksenOsanSuoritus

@Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen (TELMA) tunnistetiedot")
case class TyöhönJaItsenäiseenElämäänValmentavaKoulutus(
  @KoodistoKoodiarvo("999903")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999903", koodistoUri = "koulutus"),
  laajuus: Option[Laajuus] = None
) extends Koulutus

@Description("Työhön ja itsenäiseen elämään valmentava koulutuksen osan tunnistiedot")
case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusOsaamispisteissä],
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli with Valinnaisuus

case class TelmaArviointi(
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]] = None,
  kuvaus: Option[LocalizedString] = None
) extends AmmatillinenKoodistostaLöytyväArviointi with SanallinenArviointi
