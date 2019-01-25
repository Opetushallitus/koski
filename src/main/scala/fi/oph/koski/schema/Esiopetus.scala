package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, MaxItems, Title}

case class EsiopetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int]  = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Hidden
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  @Description("Oppijan opinto-oikeuden arvioitu päättymispäivä esiopetuksessa")
  arvioituPäättymispäivä: Option[LocalDate] = None,
  @Description("Oppijan esiopetuksen lukuvuoden päättymispäivä. Esiopetuksen suoritusaika voi olla 2-vuotinen")
  päättymispäivä: Option[LocalDate] = None,
  @Description("Tila-tieto/tiedot oppijan läsnäolosta: [confluence](https://confluence.csc.fi/display/OPHPALV/KOSKI+opiskeluoikeuden+tilojen+selitteet+koulutusmuodoittain#KOSKIopiskeluoikeudentilojenselitteetkoulutusmuodoittain-Esiopetus)")
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila,
  @Description("Esiopetuksen opiskeluoikeuden lisätiedot")
  lisätiedot: Option[EsiopetuksenOpiskeluoikeudenLisätiedot] = None,
  @MaxItems(1)
  suoritukset: List[EsiopetuksenSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.esiopetus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.esiopetus
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
}

case class EsiopetuksenOpiskeluoikeudenLisätiedot(
  @Description("Pidennetty oppivelvollisuus alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että oppilaalla ei ole pidennettyä oppivelvollisuutta.")
  @Description("Tieto mahdollisesta pidennetystä oppivelvollisuudesta alkamis- ja päättymispäivineen.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @OksaUri("tmpOKSAID517", "pidennetty oppivelvollisuus")
  pidennettyOppivelvollisuus: Option[Aikajakso] = None,
  @Description("Erityisen tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen erityisen tuen päätöksen alkamis- ja päättymispäivät. Rahoituksen laskennassa käytettävä tieto.")
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  @Deprecated("Käytä korvaavaa kenttää Erityisen tuen päätökset")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  erityisenTuenPäätös: Option[ErityisenTuenPäätös] = None,
  @Description("Erityisen tuen päätökset alkamis- ja päättymispäivineen. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen erityisen tuen päätösten alkamis- ja päättymispäivät. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN, Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  erityisenTuenPäätökset: Option[List[ErityisenTuenPäätös]] = None
) extends OpiskeluoikeudenLisätiedot

case class EsiopetuksenSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava koulutus ja koulutuksen opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: Esiopetus,
  toimipiste: OrganisaatioWithOid,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Description("Tieto siitä kielestä, joka on oppilaan kotimaisten kielten kielikylvyn kieli")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID439", "kielikylpy")
  @Tooltip("Oppilaan kotimaisten kielten kielikylvyn kieli.")
  kielikylpykieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("esiopetuksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("esiopetuksensuoritus", koodistoUri = "suorituksentyyppi"),
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None
) extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Arvioinniton with MonikielinenSuoritus with Suorituskielellinen

@Description("Esiopetuksen tunnistetiedot")
case class Esiopetus(
  perusteenDiaarinumero: Option[String],
  @KoodistoKoodiarvo("001101")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("001101", koodistoUri = "koulutus"),
  @Description("Kuvaus esiopetuksesta. Esiopetuksen päätteeksi voidaan antaa osallistumistodistus, jossa kuvataan järjestettyä esiopetusta")
  @Tooltip("Kuvaus esiopetuksesta. Esiopetuksen päätteeksi voidaan antaa osallistumistodistus, jossa kuvataan järjestettyä esiopetusta")
  @MultiLineString(4)
  kuvaus: Option[LocalizedString] = None,
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Laajuudeton
