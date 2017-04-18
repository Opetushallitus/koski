package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

case class EsiopetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int]  = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  alkamispäivä: Option[LocalDate] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  tila: PerusopetuksenOpiskeluoikeudenTila,
  lisätiedot: Option[EsiopetuksenOpiskeluoikeudenLisätiedot] = None,
  @MaxItems(1)
  suoritukset: List[EsiopetuksenSuoritus],
  @KoodistoKoodiarvo("esiopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("esiopetus", koodistoUri = "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[EsiopetuksenSuoritus]])
}

case class EsiopetuksenOpiskeluoikeudenLisätiedot(
  @Description("Pidennetty oppivelvollisuus alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että oppilaalla ei ole pidennettyä oppivelvollisuutta.")
  @OksaUri("tmpOKSAID517", "pidennetty oppivelvollisuus")
  pidennettyOppivelvollisuus: Option[Päätösjakso] = None
) extends OpiskeluoikeudenLisätiedot

case class EsiopetuksenSuoritus(
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  @KoodistoKoodiarvo("esiopetuksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("esiopetuksensuoritus", koodistoUri = "suorituksentyyppi"),
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Title("Koulutus")
  koulutusmoduuli: Esiopetus = Esiopetus(),
  @Description("Tieto siitä kielestä, joka on oppilaan kotimaisten kielten kielikylvyn kieli.")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID439", "kielikylpy")
  kielikylpykieli: Option[Koodistokoodiviite] = None
) extends PäätasonSuoritus with Toimipisteellinen with Arvioinniton with MonikielinenSuoritus

@Description("Esiopetuksen tunnistetiedot")
case class Esiopetus(
  perusteenDiaarinumero: Option[String] = None,
  @KoodistoKoodiarvo("001101")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("001101", koodistoUri = "koulutus"),
  @Description("Kuvaus esiopetuksesta")
  kuvaus: Option[LocalizedString] = None
) extends DiaarinumerollinenKoulutus {
  override def laajuus: Option[Laajuus] = None
}
