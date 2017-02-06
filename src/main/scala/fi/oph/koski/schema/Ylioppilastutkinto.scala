package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

case class YlioppilastutkinnonOpiskeluoikeus(
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[Koulutustoimija],
  tila: YlioppilastutkinnonOpiskeluoikeudenTila,
  @MinItems(1) @MaxItems(1)
  suoritukset: List[YlioppilastutkinnonSuoritus],
  @KoodistoKoodiarvo("ylioppilastutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinto", "opiskeluoikeudentyyppi")
) extends Opiskeluoikeus {
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
  override def alkamispäivä = None
  override def päättymispäivä = None
  override def id = None
  override def versionumero = None
  override def lisätiedot = None
}

case class YlioppilastutkinnonOpiskeluoikeudenTila(opiskeluoikeusjaksot: List[LukionOpiskeluoikeusjakso]) extends OpiskeluoikeudenTila

case class YlioppilastutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: Ylioppilastutkinto = Ylioppilastutkinto(perusteenDiaarinumero = None),
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Organisaatiovahvistus] = None,
  @Description("Ylioppilastutkinnon kokeiden suoritukset")
  @Title("Kokeet")
  override val osasuoritukset: Option[List[YlioppilastutkinnonKokeenSuoritus]],
  @KoodistoKoodiarvo("ylioppilastutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinto", koodistoUri = "suorituksentyyppi")
) extends PäätasonSuoritus with Toimipisteellinen with Arvioinniton {
  override def suorituskieli: Option[Koodistokoodiviite] = None
}

case class YlioppilastutkinnonKokeenSuoritus(
  @Title("Koe")
  koulutusmoduuli: YlioppilasTutkinnonKoe,
  tila: Koodistokoodiviite,
  arviointi: Option[List[YlioppilaskokeenArviointi]],
  @KoodistoKoodiarvo("ylioppilastutkinnonkoe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinnonkoe", koodistoUri = "suorituksentyyppi")
) extends VahvistuksetonSuoritus {
  override def suorituskieli: Option[Koodistokoodiviite] = None
}

case class YlioppilaskokeenArviointi(
  @KoodistoUri("koskiyoarvosanat")
  arvosana: Koodistokoodiviite
) extends KoodistostaLöytyväArviointi {
  override def arviointipäivä = None
  override def arvioitsijat = None
  def hyväksytty = arvosana.koodiarvo != "I"
}

@Description("Ylioppilastutkinnon tunnistetiedot")
case class Ylioppilastutkinto(
 @KoodistoKoodiarvo("301000")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("301000", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String]
) extends DiaarinumerollinenKoulutus {
  override def laajuus = None
  override def isTutkinto = true
}

@Description("Ylioppilastutkinnon kokeen tunnistetiedot")
case class YlioppilasTutkinnonKoe(
  tunniste: PaikallinenKoodi
) extends PaikallinenKoulutusmoduuli {
  def laajuus = None
}