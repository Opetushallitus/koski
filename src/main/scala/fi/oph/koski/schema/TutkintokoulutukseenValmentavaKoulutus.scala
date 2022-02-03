package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{ComplexObject, FlattenInUI, KoodistoKoodiarvo, KoodistoUri, Tabular}
import fi.oph.scalaschema.annotation.{DefaultValue, Description, MaxItems, MaxValue, MinItems, MinValue, OnlyWhen, Title}

import java.time.{LocalDate, LocalDateTime}

@Description("Tutkintokoulutukseen valmistavan koulutuksen (TUVA) opiskeluoikeus")
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
  suoritukset: List[TutkintokoulutukseenValmentavanKoulutuksenSuoritus],
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.tuva,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
  @KoodistoUri("tuvajarjestamislupa")
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
  tila: Koodistokoodiviite
) extends KoskiSuppeaOpiskeluoikeusjakso

case class TutkintokoulutukseenValmentavanKoulutuksenSuoritus(
  toimipiste: OrganisaatioWithOid,
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("tuva-koulutuksen-suoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "tuva-koulutuksen-suoritus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutus,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla],
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Osaamiskokonaisuudet")
  override val osasuoritukset: Option[List[TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends VapaanSivistystyönPäätasonSuoritus with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta with Suoritus

@Description("Tutkintokoulutukseen valmistavan koulutuksen tunnistetiedot")
case class TutkintokoulutukseenValmentavanKoulutus(
  //TODO: itse keksitty uusi koodiarvo
  @KoodistoKoodiarvo("999908")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999908", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = Some("OPH-1488-2021"),
  koulutustyyppi: Option[Koodistokoodiviite] = Some(Koodistokoodiviite("40", "koulutustyyppi")),
  laajuus: Option[LaajuusViikoissa] = None
) extends DiaarinumerollinenKoulutus with Tutkinto with KoulutusmoduuliValinnainenLaajuus

trait TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus extends Suoritus with MahdollisestiTunnustettu

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuoritus")
@Description("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen tiedot")
case class TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenMuuOsa,
  arviointi: Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoUri("suorituksentyyppituva")
  tyyppi: Koodistokoodiviite,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen]
) extends Suoritus with Vahvistukseton with TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus with MahdollisestiSuorituskielellinen

trait TutkintokoulutukseenValmentavanKoulutuksenMuuOsa extends KoulutusmoduuliValinnainenLaajuus {
  def laajuus: Option[LaajuusViikoissa]
}

@Title("Opiskelu- ja urasuunnittelutaidot")
@Description("Opiskelu- ja urasuunnittelutaidot")
case class TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("101")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "101", koodistoUri = "koulutuksenosattuva"),
  @DefaultValue(None)
  @MinValue(2)
  @MaxValue(10)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Perustaitojen vahvistaminen")
@Description("Perustaitojen vahvistaminen")
case class TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("perustaitojenvahvistaminen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perustaitojenvahvistaminen", koodistoUri = "koulutuksenosattuva"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(30)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Lukiokoulutuksen opinnot ja niihin valmentautuminen")
@Description("Lukiokoulutuksen opinnot ja niihin valmentautuminen")
case class TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("lukiokoulutuksenopinnot")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukiokoulutuksenopinnot", koodistoUri = "koulutuksenosattuva"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(30)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Ammatillisen koulutuksen opinnot ja niihin valmentautuminen")
@Description("Ammatillisen koulutuksen opinnot ja niihin valmentautuminen")
case class TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("ammatillisenkoulutuksenopinnot")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ammatillisenkoulutuksenopinnot", koodistoUri = "koulutuksenosattuva"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(30)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Työelämätaidot ja työpaikalla tapahtuva oppiminen")
@Description("Työelämätaidot ja työpaikalla tapahtuva oppiminen")
case class TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("102")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "102", koodistoUri = "koulutuksenosattuva"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(20)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Arjen ja yhteiskunnallisen osallisuuden taidot")
@Description("Arjen ja yhteiskunnallisen osallisuuden taidot")
case class TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("103")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "103", koodistoUri = "koulutuksenosattuva"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(20)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritus")
@Description("Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritus")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "104")
case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa,
  arviointi: Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritukset")
  @Title("Kurssit")
  @Tabular
  override val osasuoritukset: Option[List[TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus]] = None,
  @KoodistoUri("suorituksentyyppituva")
  @KoodistoKoodiarvo("tutkintokoulutukseenvalmentava")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "tutkintokoulutukseenvalmentava", koodistoUri = "suorituksentyyppituva"),
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen]
) extends Suoritus with Vahvistukseton with TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus with MahdollisestiSuorituskielellinen

@Title("Valinnaiset koulutuksen osat")
@Description("Valinnaiset koulutuksen osat")
case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa(
  @KoodistoUri("koulutuksenosattuva")
  @KoodistoKoodiarvo("104")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "104", koodistoUri = "koulutuksenosattuva"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(10)
  laajuus: Option[LaajuusViikoissa] = None
) extends KoulutusmoduuliValinnainenLaajuus with KoodistostaLöytyväKoulutusmoduuli

@Title("Tutkintokoulutukseen valmentavan valinnaisen opintojakson paikallinen osasuoritus")
@Description("Tutkintokoulutukseen valmentavan valinnaisen opintojakson paikallinen osasuoritus, jolla on laajuus viikkoina sekä arvosana.")
case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus(
  @Description("Valinnaisen koulutusosan osasuorituksen paikallinen opintojakso.")
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus,
  @FlattenInUI
  arviointi: Option[List[TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = None,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoUri("suorituksentyyppituva")
  @KoodistoKoodiarvo("tutkintokoulutukseenvalmentava")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "tutkintokoulutukseenvalmentava", koodistoUri = "suorituksentyyppituva"),

) extends KurssinSuoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu

case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus(
  nimi: LocalizedString,
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusViikoissa]
) extends KoulutusmoduuliValinnainenLaajuus

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen sanallinen arviointi")
@Description("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen hyväksytty/hylätty arviointi")
case class SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
  arvosana: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi with SanallinenYleissivistävänKoulutuksenArviointi

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen numeerinen arviointi")
@Description("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen arviointi numeerisella arvosanalla")
case class NumeerinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
  arvosana: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi with NumeerinenYleissivistävänKoulutuksenArviointi

trait TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi extends ArviointiPäivämäärällä
