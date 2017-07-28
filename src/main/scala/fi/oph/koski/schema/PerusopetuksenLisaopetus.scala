package fi.oph.koski.schema


import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

@Description("Perusopetuksen lisäopetuksen opiskeluoikeus")
case class PerusopetuksenLisäopetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  @Description("Oppijan oppimäärän alkamispäivä")
  alkamispäivä: Option[LocalDate],
  @Description("Oppijan oppimäärän päättymispäivä")
  päättymispäivä: Option[LocalDate],
  tila: PerusopetuksenOpiskeluoikeudenTila,
  @Description("Perusopetuksen lisäopetuksen opiskeluoikeuden lisätiedot")
  lisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = None,
  @MaxItems(1)
  suoritukset: List[PerusopetuksenLisäopetuksenSuoritus],
  @KoodistoKoodiarvo("perusopetuksenlisaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenlisaopetus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[PerusopetuksenLisäopetuksenSuoritus]])
  override def arvioituPäättymispäivä = None
}

@Description("Perusopetuksen lisäopetuksen suoritustiedot")
case class PerusopetuksenLisäopetuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: PerusopetuksenLisäopetus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PerusopetuksenLisäopetuksenAlisuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetuksenlisaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenlisaopetus", koodistoUri = "suorituksentyyppi")
) extends PäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with MonikielinenSuoritus

trait PerusopetuksenLisäopetuksenAlisuoritus extends Suoritus {
  @Description("Tieto siitä, onko kyseessä perusopetuksen oppiaineen arvosanan korotus. Tietoa käytetään todistuksella.")
  def korotus: Boolean
}

@Description("Perusopetuksen oppiaineen suoritus osana perusopetuksen lisäopetusta")
case class PerusopetuksenLisäopetuksenOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PerusopetuksenOppiaine,
  yksilöllistettyOppimäärä: Boolean = false,
  tila: Koodistokoodiviite,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  korotus: Boolean,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("perusopetuksenlisaopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenlisaopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenLisäopetuksenAlisuoritus with OppiaineenSuoritus with VahvistuksetonSuoritus with Yksilöllistettävä

@Description("Perusopetuksen toiminta-alueen suoritus osana perusopetuksen lisäopetusta")
case class PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus(
  @Title("Toiminta-alue")
  koulutusmoduuli: PerusopetuksenToiminta_Alue,
  tila: Koodistokoodiviite,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  korotus: Boolean = false,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetuksenlisaopetuksentoimintaalue")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenlisaopetuksentoimintaalue", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenLisäopetuksenAlisuoritus with VahvistuksetonSuoritus

@Description("Muu perusopetuksen lisäopetuksessa suoritettu opintokokonaisuus")
case class MuuPerusopetuksenLisäopetuksenSuoritus(
  @Title("Suoritetut opinnot")
  koulutusmoduuli: MuuPerusopetuksenLisäopetuksenKoulutusmoduuli,
  tila: Koodistokoodiviite,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("muuperusopetuksenlisaopetuksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "muuperusopetuksenlisaopetuksensuoritus", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenLisäopetuksenAlisuoritus with VahvistuksetonSuoritus {
  def korotus = false
}

case class MuuPerusopetuksenLisäopetuksenKoulutusmoduuli(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PaikallinenKoulutusmoduuli

@Description("Perusopetuksen lisäopetuksen tunnistetiedot")
case class PerusopetuksenLisäopetus(
  @KoodistoKoodiarvo("020075")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("020075", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = None
) extends DiaarinumerollinenKoulutus {
  def laajuus = None
}
