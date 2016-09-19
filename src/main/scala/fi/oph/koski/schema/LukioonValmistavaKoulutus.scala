package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavanKoulutuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OidOrganisaatio],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate],
  tila: LukionOpiskeluoikeudenTila,
  läsnäolotiedot: Option[YleisetLäsnäolotiedot],
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[LukioonValmistavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OidOrganisaatio) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[LukioonValmistavanKoulutuksenSuoritus]])
}

@Description("Lukioon valmistavan koulutus (LUVA) suoritus")
case class LukioonValmistavanKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: LukioonValmistavaKoulutus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Lukioon valmistavaan koulutukseen sisältyvien kurssien suoritukset")
  override val osasuoritukset: Option[List[LukioonValmistavanKoulutuksenOsasuoritus]],
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", koodistoUri = "suorituksentyyppi")
) extends PäätasonSuoritus with Toimipisteellinen {
  def arviointi: Option[List[KoodistostaLöytyväArviointi]] = None
}

@Description("Lukioon valmistavan koulutuksen (LUVA) tunnistetiedot")
case class LukioonValmistavaKoulutus(
  @KoodistoKoodiarvo("999906")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999906", koodistoUri = "koulutus")
) extends Koulutus {
  def laajuus = None
}

trait LukioonValmistavanKoulutuksenOsasuoritus extends Suoritus

case class LukioonValmistavanKurssinSuoritus(
  @Title("Kurssi")
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionKurssinArviointi]],
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("luvakurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luvakurssi", koodistoUri = "suorituksentyyppi")
) extends LukioonValmistavanKoulutuksenOsasuoritus {
  def vahvistus: Option[Henkilövahvistus] = None
}

@Description("Lukioon valmistavassa koulutuksessa suoritettava lukioon valmistavan kurssin tunnistetiedot")
case class LukioonValmistavanKoulutuksenKurssi(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusKursseissa],
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuli
