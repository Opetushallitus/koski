package fi.oph.koski.schema

import java.time.LocalDate
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

case class YlioppilastutkinnonOpiskeluoikeus(
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  tila: Option[YleissivistäväOpiskeluoikeudenTila],
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[YlioppilastutkinnonSuoritus],
  @KoodistoKoodiarvo("ylioppilastutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinto", "opiskeluoikeudentyyppi")
) extends Opiskeluoikeus {
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
  override def alkamispäivä = None
  override def päättymispäivä = None
  override def id = None
  override def versionumero = None
  override def läsnäolotiedot = None
  override def lähdejärjestelmänId = None
}

case class YlioppilastutkinnonSuoritus(
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  override val osasuoritukset: Option[List[YlioppilastutkinnonKokeenSuoritus]],
  @KoodistoKoodiarvo("ylioppilastutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinto", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: Ylioppilastutkinto = Ylioppilastutkinto(perusteenDiaarinumero = None),
  vahvistus: Option[Vahvistus] = None
) extends Suoritus {
  def arviointi: Option[List[KoodistostaLöytyväArviointi]] = None
  override def paikallinenId = None
  override def suorituskieli: Option[Koodistokoodiviite] = None
}

case class YlioppilastutkinnonKokeenSuoritus(
  tila: Koodistokoodiviite,
  arviointi: Option[List[YlioppilaskokeenArviointi]],
  koulutusmoduuli: YlioppilasTutkinnonKoe,
  @KoodistoKoodiarvo("ylioppilastutkinnonkoe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinnonkoe", koodistoUri = "suorituksentyyppi")
) extends Suoritus {
  def vahvistus = None
  override def tarvitseeVahvistuksen = false
  override def paikallinenId = None
  override def suorituskieli: Option[Koodistokoodiviite] = None
}

case class YlioppilaskokeenArviointi(
  @KoodistoUri("koskiyoarvosanat")
  arvosana: Koodistokoodiviite
) extends KoodistostaLöytyväArviointi {
  override def arviointipäivä = None
  override def arvioitsijat = None
}

object YlioppilaskokeenArviointi {
  def apply(arvosana: String) = new YlioppilaskokeenArviointi(Koodistokoodiviite(arvosana, "koskiyoarvosanat"))
}

case class Ylioppilastutkinto(
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @KoodistoKoodiarvo("301000")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("301000", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String]
) extends KoodistostaLöytyväKoulutusmoduuli with EPerusteistaLöytyväKoulutusmoduuli {
  override def laajuus = None
  override def isTutkinto = true
}

case class YlioppilasTutkinnonKoe(
  tunniste: PaikallinenKoodi
) extends PaikallinenKoulutusmoduuli {
  def laajuus = None
}