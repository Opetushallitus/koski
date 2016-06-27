package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavanKoulutuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  tila: Option[LukionOpiskeluoikeudenTila],
  läsnäolotiedot: Option[YleisetLäsnäolotiedot],
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[LukioonValmistavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavanKoulutuksenSuoritus(
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  override val osasuoritukset: Option[List[LukioonValmistavanKoulutuksenOsasuoritus]],
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukioonValmistavaKoulutus
) extends Suoritus with Toimipisteellinen {
  def arviointi: Option[List[KoodistostaLöytyväArviointi]] = None
}

@Description("Lukioon valmistavan koulutuksen (LUVA) tunnistetiedot")
case class LukioonValmistavaKoulutus(
  @KoodistoKoodiarvo("039997")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("039997", koodistoUri = "koulutus")
) extends Koulutus {
  def laajuus = None
}

trait LukioonValmistavanKoulutuksenOsasuoritus extends Suoritus

case class LukioonValmistavanKurssinSuoritus(
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionKurssinArviointi]],
  @KoodistoKoodiarvo("luvakurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luvakurssi", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi
) extends LukioonValmistavanKoulutuksenOsasuoritus {
  def vahvistus: Option[Henkilövahvistus] = None
}

@Description("Lukioon valmistavassa koulutuksessa suoritettava lukioon valmistavan kurssin tunnistetiedot")
case class LukioonValmistavanKoulutuksenKurssi(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusKursseissa]
) extends PaikallinenKoulutusmoduuli
