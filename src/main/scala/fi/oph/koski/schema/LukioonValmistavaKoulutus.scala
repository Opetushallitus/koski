package fi.oph.koski.schema

import java.time.LocalDate
import fi.oph.scalaschema.annotation.{MaxItems, MinItems, Description}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavanKoulutuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  tila: Option[YleissivistäväOpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[LukioonValmistavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", "opiskeluoikeudentyyppi")
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavanKoulutuksenSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Vahvistus] = None,
  override val osasuoritukset: Option[List[LukioonValmistavanKoulutuksenOsasuoritus]],
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukioonValmistavaKoulutus
) extends Suoritus {
  def arviointi: Option[List[KoodistostaLöytyväArviointi]] = None
}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavaKoulutus(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("039997")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("039997", koodistoUri = "koulutus")
) extends KoodistostaLöytyväKoulutusmoduuli {
  def laajuus = None
}

trait LukioonValmistavanKoulutuksenOsasuoritus extends Suoritus

case class LukioonValmistavanKurssinSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionKurssinArviointi]],
  @KoodistoKoodiarvo("luvakurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luvakurssi", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi
) extends LukioonValmistavanKoulutuksenOsasuoritus {
  def vahvistus: Option[Vahvistus] = None
}

@Description("Lukioon valmistavassa koulutuksessa suoritettava lukioon valmistava kurssi")
case class LukioonValmistavanKoulutuksenKurssi(
  tunniste: Paikallinenkoodi,
  laajuus: Option[LaajuusKursseissa]
) extends PaikallinenKoulutusmoduuli
