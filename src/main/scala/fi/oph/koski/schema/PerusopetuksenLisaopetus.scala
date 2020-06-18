package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.{Deprecated, Hidden, KoodistoKoodiarvo, SensitiveData, Tooltip}
import fi.oph.scalaschema.annotation._

@Description("Perusopetuksen lisäopetuksen opiskeluoikeus")
case class PerusopetuksenLisäopetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  @Hidden
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila,
  @Description("Perusopetuksen lisäopetuksen opiskeluoikeuden lisätiedot")
  lisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = None,
  @MaxItems(1)
  suoritukset: List[PerusopetuksenLisäopetuksenSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
) extends KoskeenTallennettavaOpiskeluoikeus with TukimuodollinenOpiskeluoikeus {
  @Description("Oppijan oppimäärän päättymispäivä")
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

@Description("Perusopetuksen lisäopetuksen suoritustiedot")
case class PerusopetuksenLisäopetuksenSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava koulutus ja koulutuksen opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: PerusopetuksenLisäopetus,
  @Description("Luokan tunniste, esimerkiksi 9C.")
  @Tooltip("Luokan tunniste, esimerkiksi 9C.")
  luokka: Option[String] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Description("Tieto siitä, osallistuuko oppilas osa-aikaiseen erityisopetukseen perusopetuksen lisäopetuksen aikana")
  @Tooltip("Osallistuuko oppilas osa-aikaiseen erityisopetukseen perusopetuksen lisäopetuksen aikana")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @DefaultValue(false)
  @Title("Osa-aikainen erityisopetus perusopetuksen lisäopetuksen aikana")
  @Deprecated("Tätä kenttää ei toistaiseksi käytetä.")
  @Hidden
  osaAikainenErityisopetus: Boolean = false,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PerusopetuksenLisäopetuksenAlisuoritus]],
  @Description("Tieto siitä, mikäli osa opiskelijan opinnoista on tapahtunut muussa oppilaitoksessa, työpaikalla tai työpaikkaan rinnastettavassa muussa paikassa.")
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi tieto siitä, mikäli osa opiskelijan opinnoista on tapahtunut muussa oppilaitoksessa, työpaikalla tai työpaikkaan rinnastettavassa muussa paikassa.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetuksenlisaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenlisaopetus", koodistoUri = "suorituksentyyppi")
) extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with MonikielinenSuoritus with Suorituskielellinen with ErityisopetuksellinenPäätasonSuoritus {
  def sisältääOsaAikaisenErityisopetuksen: Boolean = osaAikainenErityisopetus
}

trait PerusopetuksenLisäopetuksenAlisuoritus extends Suoritus with MahdollisestiSuorituskielellinen {
  @Description("Tieto siitä, onko kyseessä perusopetuksen oppiaineen arvosanan korotus. Tietoa käytetään todistuksella")
  def korotus: Boolean
}

@Description("Perusopetuksen oppiaineen suoritus osana perusopetuksen lisäopetusta")
case class PerusopetuksenLisäopetuksenOppiaineenSuoritus(
  koulutusmoduuli: NuortenPerusopetuksenOppiaine,
  @DefaultValue(false)
  yksilöllistettyOppimäärä: Boolean = false,
  @Description("Jos opiskelijan lisäopetuksessa saama uusi arvosana perusopetuksen yhteisissä tai valinnaisissa oppiaineissa on korkeampi kuin perusopetuksen päättöarvosana, se merkitään tähän")
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  @Tooltip("Onko kyseessä peruskoulun päättötodistuksen arvosanan korotus.")
  korotus: Boolean,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("perusopetuksenlisaopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenlisaopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenLisäopetuksenAlisuoritus with PerusopetuksenOppiaineenSuoritus with Vahvistukseton with Yksilöllistettävä with MahdollisestiSuorituskielellinen

@Description("Perusopetuksen toiminta-alueen suoritus osana perusopetuksen lisäopetusta")
case class PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus(
  @Title("Toiminta-alue")
  koulutusmoduuli: PerusopetuksenToiminta_Alue,
  @Description("Toiminta-alueet voivat sisältää yksittäisen oppiaineen tavoitteita ja sisältöjä, jos oppilaalla on vahvuuksia jossakin yksittäisessä oppiaineessa. Opetuksen toteuttamisessa eri toiminta-alueiden sisältöjä voidaan yhdistää")
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  @DefaultValue(false)
  korotus: Boolean = false,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetuksenlisaopetuksentoimintaalue")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenlisaopetuksentoimintaalue", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenLisäopetuksenAlisuoritus with Vahvistukseton with Toiminta_AlueenSuoritus

@Description("Muu perusopetuksen lisäopetuksessa suoritettu opintokokonaisuus")
case class MuuPerusopetuksenLisäopetuksenSuoritus(
  @Title("Suoritetut opinnot")
  koulutusmoduuli: MuuPerusopetuksenLisäopetuksenKoulutusmoduuli,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("muuperusopetuksenlisaopetuksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "muuperusopetuksenlisaopetuksensuoritus", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenLisäopetuksenAlisuoritus with Vahvistukseton {
  def korotus = false
}

case class MuuPerusopetuksenLisäopetuksenKoulutusmoduuli(
  tunniste: PaikallinenKoodi,
  @Tooltip("Paikallisen oppiaineen vapaamuotoinen kuvaus.")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PaikallinenKoulutusmoduuli

@Description("Perusopetuksen lisäopetuksen tunnistetiedot")
case class PerusopetuksenLisäopetus(
  @KoodistoKoodiarvo("020075")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("020075", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus {
  def laajuus = None
}
