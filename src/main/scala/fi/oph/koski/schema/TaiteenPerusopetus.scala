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
  @MinItems(1)
  @MaxItems(1)
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
  @KoodistoKoodiarvo("keskeytynyt")
  @KoodistoKoodiarvo("hyvaksytystisuoritettu")
  tila: Koodistokoodiviite,
) extends KoskiOpiskeluoikeusjakso

/******************************************************************************
 * PÄÄTASON SUORITUKSET
 *****************************************************************************/

trait TaiteenPerusopetuksenPäätasonSuoritus
  extends KoskeenTallennettavaPäätasonSuoritus

trait TaiteenPerusopetuksenYleisenOppimääränSuoritus
  extends TaiteenPerusopetuksenPäätasonSuoritus

trait TaiteenPerusopetuksenLaajanOppimääränSuoritus
  extends TaiteenPerusopetuksenPäätasonSuoritus

@Title("Taiteen perusopetuksen yleisen oppimäärän yhteisten opintojen suoritus")
@Description("Taiteen perusopetuksen yleisen oppimäärän yhteisten opintojen opintotason suoritus")
@OnlyWhen("koulutusmoduuli/opintotaso/koodiarvo", "yleisenoppimaaranyhteisetopinnot")
case class TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus(
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[TaiteenPerusopetuksenSanallinenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla],
  @KoodistoKoodiarvo("taiteenperusopetuksenyleisenoppimääränyhteisetopinnot")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("taiteenperusopetuksenyleisenoppimääränyhteisetopinnot", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus]]
) extends TaiteenPerusopetuksenYleisenOppimääränSuoritus

@Title("Taiteen perusopetuksen yleisen oppimäärän teemaopintojen suoritus")
@Description("Taiteen perusopetuksen yleisen oppimäärän teemaopintojen opintotason suoritus")
@OnlyWhen("koulutusmoduuli/opintotaso/koodiarvo", "yleisenoppimaaranteemaopinnot")
case class TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus(
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[TaiteenPerusopetuksenSanallinenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla],
  @KoodistoKoodiarvo("taiteenperusopetuksenyleisenoppimääränteemaopinnot")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("taiteenperusopetuksenyleisenoppimääränteemaopinnot", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus]]
) extends TaiteenPerusopetuksenYleisenOppimääränSuoritus

@Title("Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus")
@Description("Taiteen perusopetuksen laajan oppimäärän perusopintojen opintotason suoritus")
@OnlyWhen("koulutusmoduuli/opintotaso/koodiarvo", "laajanoppimaaranperusopinnot")
case class TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus(
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[TaiteenPerusopetuksenSanallinenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla],
  @KoodistoKoodiarvo("taiteenperusopetuksenlaajanoppimääränperusopinnot")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("taiteenperusopetuksenlaajanoppimääränperusopinnot", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus]]
) extends TaiteenPerusopetuksenLaajanOppimääränSuoritus

@Title("Taiteen perusopetuksen laajan oppimäärän syventävien opintojen suoritus")
@Description("Taiteen perusopetuksen laajan oppimäärän syventävien opintojen opintotason suoritus")
@OnlyWhen("koulutusmoduuli/opintotaso/koodiarvo", "laajanoppimaaransyventavatopinnot")
case class TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus(
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[TaiteenPerusopetuksenSanallinenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla],
  @KoodistoKoodiarvo("taiteenperusopetuksenlaajanoppimääränsyventävätopinnot")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("taiteenperusopetuksenlaajanoppimääränsyventävätopinnot", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus]]
) extends TaiteenPerusopetuksenLaajanOppimääränSuoritus


/******************************************************************************
 * PÄÄTASON SUORITUKSET - KOULUTUSMODUULIT
 *****************************************************************************/

@Title("Taiteen perusopetuksen opintotaso")
@Description("Taiteen perusopetuksen oppimäärän opintotaso")
case class TaiteenPerusopetuksenOpintotaso(
  @KoodistoKoodiarvo("999907")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999907", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @Title("Opintotaso")
  @Description("Suoritettavan oppimäärän opintotaso")
  @KoodistoUri("taiteenperusopetusopintotaso")
  opintotaso: Koodistokoodiviite,
  @Title("Taiteenala")
  @Description("Suoritettavan oppimäärän opintotason taiteenala")
  @KoodistoUri("taiteenperusopetustaiteenala")
  taiteenala: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String]
) extends DiaarinumerollinenKoulutus with KoulutusmoduuliValinnainenLaajuus

/******************************************************************************
 * SUORITUKSET - ARVIOINTI
 *****************************************************************************/

case class TaiteenPerusopetuksenSanallinenArviointi(
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
  arviointi: Option[List[TaiteenPerusopetuksenSanallinenArviointi]],
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
  kuvaus: LocalizedString,
  laajuus: LaajuusOpintopisteissä
) extends PaikallinenKoulutusmoduuliPakollinenLaajuus with StorablePreference
