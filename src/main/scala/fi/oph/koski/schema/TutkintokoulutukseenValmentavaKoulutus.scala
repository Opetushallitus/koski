package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{FlattenInUI, KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.{DefaultValue, Description, MaxItems, MaxValue, MinItems, MinValue, Title}

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
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
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
  @KoodistoKoodiarvo("tuva-koulutuksen-suoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutus,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla],
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Osaamiskokonaisuudet")
  override val osasuoritukset: Option[List[TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends VapaanSivistystyönPäätasonSuoritus with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta with OpintopistelaajuuksienYhteislaskennallinenSuoritus

@Description("Tutkintokoulutukseen valmistavan koulutuksen tunnistetiedot")
case class TutkintokoulutukseenValmentavanKoulutus(
  @KoodistoKoodiarvo("666")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("666", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = None,
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends DiaarinumerollinenKoulutus with Tutkinto with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli

trait TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus extends Suoritus with MahdollisestiTunnustettu

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuoritus")
@Description("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen tiedot")
case class TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenMuuOsa,
  arviointi: Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine", koodistoUri = "suorituksentyyppi"),
  tunnustettu: Option[OsaamisenTunnustaminen]
) extends OppiaineenSuoritus with Vahvistukseton with TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus with MahdollisestiSuorituskielellinen

trait TutkintokoulutukseenValmentavanKoulutuksenMuuOsa extends KoulutusmoduuliValinnainenLaajuus {
  def laajuus: Option[LaajuusViikoissa]
}

@Title("Opiskelu- ja urasuunnittelutaidot")
@Description("Opiskelu- ja urasuunnittelutaidot")
case class TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @DefaultValue(None)
  @MinValue(2)
  @MaxValue(10)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Perustaitojen vahvistaminen")
@Description("Perustaitojen vahvistaminen")
case class TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(30)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Lukiokoulutuksen opinnot ja niihin valmentautuminen")
@Description("Lukiokoulutuksen opinnot ja niihin valmentautuminen")
case class TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot(
  //TODO:
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  //TODO:
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(30)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli with Oppimäärä {
  override def description = oppimäärä.description
}

@Title("Ammatillisen koulutuksen opinnot ja niihin valmentautuminen")
@Description("Ammatillisen koulutuksen opinnot ja niihin valmentautuminen")
case class TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot(
  //TODO:
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(30)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Työelämätaidot ja työpaikalla tapahtuva oppiminen")
@Description("Työelämätaidot ja työpaikalla tapahtuva oppiminen")
case class TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(
  //TODO
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(20)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Arjen ja yhteiskunnallisen osallisuuden taidot")
@Description("Arjen ja yhteiskunnallisen osallisuuden taidot")
case class TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot(
  //TODO:
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(20)
  laajuus: Option[LaajuusViikoissa] = None
) extends TutkintokoulutukseenValmentavanKoulutuksenMuuOsa with KoodistostaLöytyväKoulutusmoduuli

@Title("Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritus")
@Description("Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritus")
case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa,
  arviointi: Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus]],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine", koodistoUri = "suorituksentyyppi"),
  tunnustettu: Option[OsaamisenTunnustaminen]
) extends OppiaineenSuoritus with Vahvistukseton with TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus with MahdollisestiSuorituskielellinen

case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa(
  //TODO:
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @DefaultValue(None)
  @MinValue(1)
  @MaxValue(10)
  laajuus: Option[LaajuusViikoissa] = None
) extends KoulutusmoduuliValinnainenLaajuus with KoodistostaLöytyväKoulutusmoduuli

@Title("Tutkintokoulutukseen valmentavan valinnaisen opintojakson paikallinen osasuoritus")
@Description("Tutkintokoulutukseen valmentavan valinnaisen opintojakson paikallinen osasuoritus, jolla on laajuus viikkoina sekä arvosana.")
case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus(
  //TODO
  @Description("Lukion kurssin tunnistetiedot")
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus,
  @FlattenInUI
  arviointi: Option[List[TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = None,
  //  @Description("Jos kurssi on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot. Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia pakollisia, syventäviä tai soveltavia opintoja. Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä. Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan kurssiin, tulee kurssista antaa numeroarvosana")
  //                                      @ComplexObject
  //                                      tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionkurssi", koodistoUri = "suorituksentyyppi"),
) extends KurssinSuoritus with MahdollisestiSuorituskielellinen //with MahdollisestiTunnustettu

case class TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus(
  nimi: LocalizedString,
  //TODO:
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @DefaultValue(None)
  laajuus: Option[LaajuusViikoissa] = None
) extends KoulutusmoduuliValinnainenLaajuus

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen sanallinen arviointi")
@Description("Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen hyväksytty/hylätty arviointi")
case class SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
  arvosana: Koodistokoodiviite, // = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
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
