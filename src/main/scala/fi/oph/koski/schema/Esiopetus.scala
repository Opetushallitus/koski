package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

case class EsiopetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  oid: Option[String] = None,
  versionumero: Option[Int]  = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Description("Oppijan esiopetuksen lukuvuoden alkamisaika. Esiopetuksen alkamisaika 1.8.")
  alkamispäivä: Option[LocalDate] = None,
  @Description("Oppijan opinto-oikeuden arvioitu päättymispäivä esiopetuksessa")
  arvioituPäättymispäivä: Option[LocalDate] = None,
  @Description("Oppijan esiopetuksen lukuvuoden päättymispäivä. Esiopetuksen suoritusaika voi olla 2-vuotinen.")
  päättymispäivä: Option[LocalDate] = None,
  @Description("Tila-tieto/tiedot oppijan läsnäolosta: [confluence](https://confluence.csc.fi/display/OPHPALV/KOSKI+opiskeluoikeuden+tilojen+selitteet+koulutusmuodoittain#KOSKIopiskeluoikeudentilojenselitteetkoulutusmuodoittain-Esiopetus)")
  tila: PerusopetuksenOpiskeluoikeudenTila,
  @Description("Esiopetuksen opiskeluoikeuden lisätiedot.")
  lisätiedot: Option[EsiopetuksenOpiskeluoikeudenLisätiedot] = None,
  @MaxItems(1)
  suoritukset: List[EsiopetuksenSuoritus],
  @KoodistoKoodiarvo("esiopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("esiopetus", koodistoUri = "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], oid: Option[String], versionumero: Option[Int]) = this.copy(id = id, oid = oid, versionumero = versionumero)
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
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Title("Koulutus")
  koulutusmoduuli: Esiopetus,
  @Description("Tieto siitä kielestä, joka on oppilaan kotimaisten kielten kielikylvyn kieli.")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID439", "kielikylpy")
  kielikylpykieli: Option[Koodistokoodiviite] = None
) extends PäätasonSuoritus with Toimipisteellinen with Arvioinniton with MonikielinenSuoritus with Suorituskielellinen

@Description("Esiopetuksen tunnistetiedot")
case class Esiopetus(
  perusteenDiaarinumero: Option[String],
  @KoodistoKoodiarvo("001101")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("001101", koodistoUri = "koulutus"),
  @Description("Kuvaus esiopetuksesta. Esiopetuksen päätteeksi voidaan antaa osallistumistodistus, jossa voidaan kuvata järjestettyä esiopetusta.")
  kuvaus: Option[LocalizedString] = None
) extends DiaarinumerollinenKoulutus with Laajuudeton
