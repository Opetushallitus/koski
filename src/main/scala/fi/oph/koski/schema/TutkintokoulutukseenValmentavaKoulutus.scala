package fi.oph.koski.schema

import fi.oph.koski.schema.LocalizedString.unlocalized
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

import java.time.{LocalDate, LocalDateTime}

@Title("Tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeus")
@Description("Tutkintokoulutukseen valmentavan koulutuksen (TUVA) opiskeluoikeus")
case class TutkintokoulutukseenValmentavanOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila,
  lisätiedot: Option[TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot] = None,
  @MaxItems(1)
  suoritukset: List[TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.tuva.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.tuva,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
  @KoodistoUri("tuvajarjestamislupa")
  @ReadOnly("")
  järjestämislupa: Koodistokoodiviite
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija): KoskeenTallennettavaOpiskeluoikeus = this.copy(koulutustoimija = Some(koulutustoimija))

  override def withOppilaitos(oppilaitos: Oppilaitos): KoskeenTallennettavaOpiskeluoikeus = this.copy(oppilaitos = Some(oppilaitos))

  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

case class TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[TutkintokoulutukseenValmentavanOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class TutkintokoulutukseenValmentavanOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("katsotaaneronneeksi")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("valiaikaisestikeskeytynyt")
  @KoodistoKoodiarvo("valmistunut")
  @KoodistoKoodiarvo("loma")
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("10")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiOpiskeluoikeusjakso

trait TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus
  extends KoskeenTallennettavaPäätasonSuoritus
    with OpintopistelaajuuksienYhteislaskennallinenPäätasonSuoritus[LaajuusViikoissa]
    with OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusViikoissa]

@Title("Tutkintokoulutukseen valmentavan koulutuksen suoritustiedot")
@Description("Tutkintokoulutukseen valmentavan koulutuksen suoritustiedot.")
case class TutkintokoulutukseenValmentavanKoulutuksenSuoritus(
  toimipiste: OrganisaatioWithOid,
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("tuvakoulutuksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "tuvakoulutuksensuoritus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutus,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla],
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Osaamiskokonaisuudet")
  override val osasuoritukset: Option[List[TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus with Toimipisteellinen with Suorituskielellinen
  with Todistus with Arvioinniton with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta with Suoritus

@Description("Tutkintokoulutukseen valmentavan koulutuksen tunnistetiedot")
case class TutkintokoulutukseenValmentavanKoulutus(
  @KoodistoKoodiarvo("999908")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999908", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = Some("OPH-1488-2021"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @DefaultValue(None)
  @ReadOnly("Laajuus lasketaan automaattisesti osasuoritusten laajuuksista.")
  laajuus: Option[LaajuusViikoissa] = None
) extends DiaarinumerollinenKoulutus
    with Tutkinto
    with KoulutusmoduuliValinnainenLaajuus
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusViikoissa] {
  override def makeLaajuus(laajuusArvo: Double): LaajuusViikoissa = LaajuusViikoissa(laajuusArvo)
}

trait TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus
  extends OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusViikoissa] with MahdollisestiTunnustettu

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuoritus")
@Description("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen tiedot")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "101")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "102")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "103")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "105")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "106")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "107")
case class TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenMuuOsa,
  arviointi: Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("tutkintokoulutukseenvalmentava")
  @KoodistoKoodiarvo("tuvaperusopetus")
  @KoodistoKoodiarvo("tuvalukiokoulutus")
  @KoodistoKoodiarvo("tuvaammatillinenkoulutus")
  tyyppi: Koodistokoodiviite,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen]
) extends Suoritus with Vahvistukseton with TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus with MahdollisestiSuorituskielellinen

trait TutkintokoulutukseenValmentavanKoulutuksenMuuOsa
  extends KoulutusmoduuliValinnainenLaajuus
    with KoodistostaLöytyväKoulutusmoduuli
    with OpintopistelaajuuksienYhteenlaskennanOhittavaKoulutusmoduuli[LaajuusViikoissa] {
  def laajuus: Option[LaajuusViikoissa]
  override def makeLaajuus(laajuusArvo: Double): LaajuusViikoissa = LaajuusViikoissa(laajuusArvo)
}

@Title("Opiskelu- ja urasuunnittelutaidot")
@Description("Opiskelu- ja urasuunnittelutaidot")
case class TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("101")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(
    koodiarvo = "101",
    koodistoUri = "koulutuksenosattuva",
    nimi = Some(LocalizedString.unlocalized("Opiskelu- ja urasuunnittelutaidot"))
  ),
  @DefaultValue(None)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa

@Title("Perustaitojen vahvistaminen")
@Description("Perustaitojen vahvistaminen")
case class TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("107")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(
    koodiarvo = "107",
    koodistoUri = "koulutuksenosattuva",
    nimi = Some(LocalizedString.unlocalized("Perustaitojen vahvistaminen"))
  ),
  @DefaultValue(None)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa

@Title("Lukiokoulutuksen opinnot ja niihin valmentautuminen")
@Description("Lukiokoulutuksen opinnot ja niihin valmentautuminen")
case class TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("106")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(
    koodiarvo = "106",
    koodistoUri = "koulutuksenosattuva",
    nimi = Some(LocalizedString.unlocalized("Lukiokoulutuksen opinnot ja niihin valmentautuminen"))
  ),
  @DefaultValue(None)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa

@Title("Ammatillisen koulutuksen opinnot ja niihin valmentautuminen")
@Description("Ammatillisen koulutuksen opinnot ja niihin valmentautuminen")
case class TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("105")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(
    koodiarvo = "105",
    koodistoUri = "koulutuksenosattuva",
    nimi = Some(LocalizedString.unlocalized("Ammatillisen koulutuksen opinnot ja niihin valmentautuminen"))
  ),
  @DefaultValue(None)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa

@Title("Työelämätaidot ja työpaikalla tapahtuva oppiminen")
@Description("Työelämätaidot ja työpaikalla tapahtuva oppiminen")
case class TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("102")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(
    koodiarvo = "102",
    koodistoUri = "koulutuksenosattuva",
    nimi = Some(LocalizedString.unlocalized("Työelämätaidot ja työpaikalla tapahtuva oppiminen"))
  ),
  @DefaultValue(None)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa

@Title("Arjen ja yhteiskunnallisen osallisuuden taidot")
@Description("Arjen ja yhteiskunnallisen osallisuuden taidot")
case class TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("103")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(
    koodiarvo = "103",
    koodistoUri = "koulutuksenosattuva",
    nimi = Some(LocalizedString.unlocalized("Arjen ja yhteiskunnallisen osallisuuden taidot"))
  ),
  @DefaultValue(None)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa

@Title("Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritus")
@Description("Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritus")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "104")
case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa,
  arviointi: Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Tabular
  override val osasuoritukset: Option[List[TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus]] = None,
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("tutkintokoulutukseenvalmentava")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "tutkintokoulutukseenvalmentava", koodistoUri = "suorituksentyyppi"),
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen]
) extends Suoritus with Vahvistukseton with TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus with MahdollisestiSuorituskielellinen

@Title("Valinnaiset koulutuksen osat")
@Description("Valinnaiset koulutuksen osat")
case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("104")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(
    koodiarvo = "104",
    koodistoUri = "koulutuksenosattuva",
    nimi = Some(LocalizedString.unlocalized("Valinnaiset koulutuksen osat"))
  ),
  @DefaultValue(None)
  @ReadOnly("Laajuus lasketaan automaattisesti osasuoritusten laajuuksista.")
  laajuus: Option[LaajuusViikoissa] = None
) extends KoulutusmoduuliValinnainenLaajuus
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusViikoissa] {
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
  override def makeLaajuus(laajuusArvo: Double): LaajuusViikoissa = LaajuusViikoissa(laajuusArvo)
}

@Title("Valinnaisten tutkintokoulutukseen valmentavan koulutuksen opintojen osasuoritukset")
@Description("Valinnaisten tutkintokoulutukseen valmentavan koulutuksen opintojen osasuoritukset. Tutkintokoulutukseen valmentavan valinnaisen koulutuksen osan paikalliset osasuoritukset, joilla on laajuus viikkoina sekä arvosana.")
case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus(
  @Description("Valinnaisen koulutusosan osasuorituksen paikallinen opintojakso.")
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus,
  @FlattenInUI
  arviointi: Option[List[TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = None,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("tutkintokoulutukseenvalmentava")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "tutkintokoulutukseenvalmentava", koodistoUri = "suorituksentyyppi"),

) extends KurssinSuoritus
    with MahdollisestiSuorituskielellinen
    with MahdollisestiTunnustettu
    with OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusViikoissa]

case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus(
  nimi: LocalizedString,
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusViikoissa]
) extends KoulutusmoduuliValinnainenLaajuus
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusViikoissa] {
  override def makeLaajuus(laajuusArvo: Double): LaajuusViikoissa = LaajuusViikoissa(laajuusArvo)
}

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen sanallinen arviointi")
@Description("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen hyväksytty/hylätty arviointi")
case class SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
  @KoodistoKoodiarvo("Hyväksytty")
  @KoodistoKoodiarvo("Hylätty")
  @KoodistoUri("arviointiasteikkotuva")
  arvosana: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi with SanallinenArviointi {
  override def hyväksytty: Boolean =
    SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi.hyväksytty(arvosana)
  override def arvioitsijat: Option[List[SuorituksenArvioitsija]] = None
}
object SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi {
  def hyväksytty(arvosana: Koodistokoodiviite): Boolean = arvosana.koodiarvo == "Hyväksytty"
}

trait TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi
  extends KoodistostaLöytyväArviointi with ArviointiPäivämäärällä
