package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

/******************************************************************************
 * OPISKELUOIKEUS
 *****************************************************************************/

case class TaiteenPerusopetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos] = None,
  koulutustoimija: Option[Koulutustoimija] = None,
  tila: TaiteenPerusopetuksenOpiskeluoikeudenTila,
  @Title("Oppimäärä")
  @Description("Taiteen perusopetuksen opiskeluoikeuden oppimäärä")
  @KoodistoUri("taiteenperusopetusoppimaara")
  oppimäärä: Koodistokoodiviite,
  @MaxItems(2)
  suoritukset: List[TaiteenPerusopetuksenPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.taiteenperusopetus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.taiteenperusopetus,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
  arvioituPäättymispäivä: Option[LocalDate]
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
  override def lisätiedot: Option[OpiskeluoikeudenLisätiedot] = None
}

/******************************************************************************
 * TILAT
 *****************************************************************************/

case class TaiteenPerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[TaiteenPerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class TaiteenPerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("paattynyt")
  @KoodistoKoodiarvo("hyvaksytystisuoritettu")
  tila: Koodistokoodiviite,
) extends KoskiOpiskeluoikeusjakso

/******************************************************************************
 * PÄÄTASON SUORITUKSET
 *****************************************************************************/

trait TaiteenPerusopetuksenPäätasonSuoritus
  extends KoskeenTallennettavaPäätasonSuoritus

@Title("Yleisen oppimäärän yhteisten opintojen suoritus")
@Description("Taiteen perusopetuksen yleisen oppimäärän yhteisten opintojen opintotason suoritus")
case class TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus(
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[TaiteenPerusopetuksenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla],
  @KoodistoKoodiarvo("taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus]]
) extends TaiteenPerusopetuksenPäätasonSuoritus

@Title("Yleisen oppimäärän teemaopintojen suoritus")
@Description("Taiteen perusopetuksen yleisen oppimäärän teemaopintojen opintotason suoritus")
case class TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus(
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[TaiteenPerusopetuksenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla],
  @KoodistoKoodiarvo("taiteenperusopetuksenyleisenoppimaaranteemaopinnot")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("taiteenperusopetuksenyleisenoppimaaranteemaopinnot", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus]]
) extends TaiteenPerusopetuksenPäätasonSuoritus

@Title("Laajan oppimäärän perusopintojen suoritus")
@Description("Taiteen perusopetuksen laajan oppimäärän perusopintojen opintotason suoritus")
case class TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus(
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[TaiteenPerusopetuksenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla],
  @KoodistoKoodiarvo("taiteenperusopetuksenlaajanoppimaaranperusopinnot")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("taiteenperusopetuksenlaajanoppimaaranperusopinnot", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus]]
) extends TaiteenPerusopetuksenPäätasonSuoritus

@Title("Laajan oppimäärän syventävien opintojen suoritus")
@Description("Taiteen perusopetuksen laajan oppimäärän syventävien opintojen opintotason suoritus")
case class TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus(
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[TaiteenPerusopetuksenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla],
  @KoodistoKoodiarvo("taiteenperusopetuksenlaajanoppimaaransyventavatopinnot")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("taiteenperusopetuksenlaajanoppimaaransyventavatopinnot", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus]]
) extends TaiteenPerusopetuksenPäätasonSuoritus


/******************************************************************************
 * PÄÄTASON SUORITUKSET - KOULUTUSMODUULIT
 *****************************************************************************/

trait TaiteenPerusopetuksenOpintotaso extends DiaarinumerollinenKoulutus with KoulutusmoduuliValinnainenLaajuus {

  @Title("Taiteenala")
  @Description("Suoritettavan oppimäärän opintotason taiteenala")
  @KoodistoUri("taiteenperusopetustaiteenala")
  @Discriminator
  def taiteenala: Koodistokoodiviite
}

@Title("Arkkitehtuuri")
case class ArkkitehtuurinOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("arkkitehtuuri")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends TaiteenPerusopetuksenOpintotaso

@Title("Kuvataide")
case class KuvataiteenOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("kuvataide")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends TaiteenPerusopetuksenOpintotaso

@Title("Käsityö")
case class KäsityönOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("kasityo")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends TaiteenPerusopetuksenOpintotaso

@Title("Mediataide")
case class MediataiteenOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("mediataiteet")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends TaiteenPerusopetuksenOpintotaso

@Title("Musiikki")
case class MusiikinOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("musiikki")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends TaiteenPerusopetuksenOpintotaso

@Title("Sanataide")
case class SanataiteenOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("sanataide")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends TaiteenPerusopetuksenOpintotaso

@Title("Sirkustaide")
case class SirkustaiteenOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("sirkustaide")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends TaiteenPerusopetuksenOpintotaso

@Title("Tanssi")
case class TanssinOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("tanssi")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends TaiteenPerusopetuksenOpintotaso

@Title("Teatteritaide")
case class TeatteritaiteenOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("teatteritaide")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends TaiteenPerusopetuksenOpintotaso

/******************************************************************************
 * SUORITUKSET - ARVIOINTI
 *****************************************************************************/

case class TaiteenPerusopetuksenArviointi(
  @KoodistoUri("arviointiasteikkotaiteenperusopetus")
  @KoodistoKoodiarvo("hyvaksytty")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("hyvaksytty", "arviointiasteikkotaiteenperusopetus"),
  päivä: LocalDate,
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends ArviointiPäivämäärällä with KoodistostaLöytyväArviointi {
  override def hyväksytty: Boolean = true
}

/******************************************************************************
 * OSASUORITUKSET
 *****************************************************************************/

@Title("Paikallisen opintokokonaisuuden suoritus")
@Description("Taiteen perusopetuksen paikallisen opintokokonaisuuden suoritus")
case class TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus(
  koulutusmoduuli: TaiteenPerusopetuksenPaikallinenOpintokokonaisuus,
  arviointi: Option[List[TaiteenPerusopetuksenArviointi]],
  @KoodistoKoodiarvo("taiteenperusopetuksenpaikallinenopintokokonaisuus")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite("taiteenperusopetuksenpaikallinenopintokokonaisuus", "suorituksentyyppi")
) extends Suoritus {
  override def vahvistus: Option[Vahvistus] = None
}

/******************************************************************************
 * OSASUORITUKSET - KOULUTUSMODUULIT
 *****************************************************************************/

@Title("Paikallinen opintokokonaisuus")
@Description("Taiteen perusopetuksen paikallinen opintokokonaisuus")
case class TaiteenPerusopetuksenPaikallinenOpintokokonaisuus(
  tunniste: PaikallinenKoodi,
  laajuus: LaajuusOpintopisteissä
) extends PaikallinenKoulutusmoduuli with KoulutusmoduuliPakollinenLaajuus with StorablePreference