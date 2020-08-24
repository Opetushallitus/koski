package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.json.JsonSerializer.serializeWithRoot
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

trait LukionPäätasonSuoritus2019 extends LukionPäätasonSuoritus with Todistus with Arvioinniton with PuhviKokeellinen2019 with SuullisenKielitaidonKokeellinen2019 {
  def koulutusmoduuli: Koulutusmoduuli with Diaarinumerollinen
  @KoodistoUri("lukionoppimaara")
  @Title("Opetussuunnitelma")
  @Description("Tieto siitä, suoritetaanko lukiota nuorten vai aikuisten oppimäärän mukaisesti")
  def oppimäärä: Koodistokoodiviite
}

@Title("Lukion oppimäärän suoritus 2019")
@Description("Lukion oppimäärän opetussuunnitelman 2019 mukaiset suoritustiedot")
case class LukionOppimääränSuoritus2019(
  @Title("Koulutus")
  koulutusmoduuli: LukionOppimäärä,
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  @Description("Oppimäärän suorituksen opetuskieli/suorituskieli. Rahoituksen laskennassa käytettävä tieto.")
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Osallistuminen lukiokoulutusta täydentävän saamen/romanikielen/opiskelijan oman äidinkielen opiskeluun")
  omanÄidinkielenOpinnot: Option[OmanÄidinkielenOpinnotLaajuusOpintopisteinä] = None,
  puhviKoe: Option[PuhviKoe2019] = None,
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[LukionOppimääränOsasuoritus2019]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppimaara2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara2019", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus2019 with Ryhmällinen with KoulusivistyskieliKieliaineesta with SuoritettavissaErityisenäTutkintona2019 with Oppimäärällinen

@Title("Lukion oppiaineiden oppimäärien suoritus 2019")
@Description("Lukion oppiaineiden oppimäärien suoritustiedot 2019")
case class LukionOppiaineidenOppimäärienSuoritus2019(
  @Title("Koulutus")
  koulutusmoduuli: LukionOppiaineidenOppimäärät2019,
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Merkitään, jos lukion oppimäärä on tullut suoritetuksi aineopintoina.")
  @DefaultValue(false)
  lukionOppimääräSuoritettu: Boolean = false,
  puhviKoe: Option[PuhviKoe2019] = None,
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[LukionOppiaineenSuoritus2019]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppiaineidenoppimaarat2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppiaineidenoppimaarat2019", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus2019 with Välisuoritus with Oppimäärällinen {
  override def tarvitseeVahvistuksen: Boolean = false
}

@Title("Lukion oppiaineiden oppimäärät 2019")
case class LukionOppiaineidenOppimäärät2019(
  @Hidden
  tunniste: LukionOppiaineidenOppimäärätKoodi2019 = LukionOppiaineidenOppimäärätKoodi2019(),
  perusteenDiaarinumero: Option[String]
) extends Koulutusmoduuli with Diaarinumerollinen {
  override def nimi: LocalizedString = LocalizedString.empty
}

@Description("Koodi, jota käytetään lukion oppiaineiden oppimäärien ryhmittelyssä 2019.")
case class LukionOppiaineidenOppimäärätKoodi2019(
  koodiarvo: String = "lukionoppiaineidenoppimaarat2019"
) extends PaikallinenKoodiviite {
  override def nimi: LocalizedString = LocalizedString.empty
}

trait LukionOppimääränOsasuoritus2019 extends LukionOppimääränPäätasonOsasuoritus

@Title("Muiden lukio-opintojen suoritus 2019")
@Description("Kategoria opintojaksoille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esim. lukiodiplomit tai temaattiset opinnot.")
case class MuidenLukioOpintojenSuoritus2019(
  @KoodistoKoodiarvo("lukionmuuopinto2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionmuuopinto2019", "suorituksentyyppi"),
  koulutusmoduuli: MuutSuorituksetTaiVastaavat2019,
  @MinItems(1)
  @Description("Moduulien ja paikallisten opintojaksojen suoritukset")
  @Title("Moduulit ja paikalliset opintojaksot")
  override val osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019]]
) extends LukionOppimääränOsasuoritus2019 with Vahvistukseton with Arvioinniton

@Title("Lukion oppiaineen suoritus 2019")
@Description("Lukion oppiaineen suoritustiedot 2019")
case class LukionOppiaineenSuoritus2019(
  koulutusmoduuli: LukionOppiaine2019,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  @Title("Suorituskieli, jos muu kuin opetuskieli")
  @Description("Suorituskieli, mikäli opiskelija on opiskellut yli puolet oppiaineen oppimäärän opinnoista muulla kuin koulun varsinaisella opetuskielellä.")
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien moduulien ja paikallisten opintojaksojen suoritukset")
  @Title("Moduulit ja paikalliset opintojaksot")
  override val osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019]],
  @KoodistoKoodiarvo("lukionoppiaine2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine2019", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with Vahvistukseton with LukionOppimääränOsasuoritus2019 with MahdollisestiSuorituskielellinen with SuoritettavissaErityisenäTutkintona2019

trait LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 extends Suoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu with Vahvistukseton {
  @FlattenInUI
  def arviointi: Option[List[LukionArviointi]]
  @ComplexObject
  def tunnustettu: Option[OsaamisenTunnustaminen]
}

@Title("Lukion moduulin suoritus 2019")
@Description("Lukion moduulin suoritustiedot 2019")
case class LukionModuulinSuoritus2019(
  @Description("Lukion moduulin tunnistetiedot")
  koulutusmoduuli: LukionModuuli2019,
  arviointi: Option[List[LukionArviointi]] = None,
  // TODO: descriptionin lakiviitteet yms. teksti
  @Description("Jos moduuli on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot. Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia opintoja. Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä. Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan moduuliin, tulee moduulista antaa numeroarvosana")
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionvaltakunnallinenmoduuli2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionvaltakunnallinenmoduuli2019", koodistoUri = "suorituksentyyppi")
) extends LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 with ValtakunnallisenModuulinSuoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu

trait MuutSuorituksetTaiVastaavat2019 extends KoodistostaLöytyväKoulutusmoduuliValinnainenLaajuus {
  @KoodistoUri("lukionmuutopinnot")
  def tunniste: Koodistokoodiviite
}

@Title("Muut suoritukset 2019")
@Description("Kategoria moduuleille ja opintojaksoille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen.")
case class MuutLukionSuoritukset2019(
  @KoodistoKoodiarvo("MS")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä]
) extends MuutSuorituksetTaiVastaavat2019

@Title("Lukiodiplomit 2019")
@Description("Kategoria lukiodiplomeille 2019.")
case class Lukiodiplomit2019(
  @KoodistoKoodiarvo("LD")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä]
) extends MuutSuorituksetTaiVastaavat2019

@Title("Temaattiset opinnot 2019")
@Description("Kategoria temaattisille opinnoille 2019.")
case class TemaattisetOpinnot2019(
  @KoodistoKoodiarvo("TO")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä]
) extends MuutSuorituksetTaiVastaavat2019

@Title("Lukion paikallisen opintojakson suoritus 2019")
@Description("Lukion paikallisen opintojakson suoritustiedot 2019")
case class LukionPaikallisenOpintojaksonSuoritus2019(
  @Title("Paikallinen opintojakso")
  @Description("Lukion paikallisen opintojakson tunnistetiedot")
  @FlattenInUI
  koulutusmoduuli: LukionPaikallinenOpintojakso2019,
  arviointi: Option[List[LukionArviointi]] = None,
  // TODO: Descriptionin lakiviitteet ja teksti
  @Description("Jos opintojakso on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot. Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia pakollisia tai vapaaehtoisia opintoja. Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä. Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan opintojaksoon, tulee opintojaksosta antaa numeroarvosana")
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionpaikallinenopintojakso2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionpaikallinenopintojakso2019", koodistoUri = "suorituksentyyppi")
) extends LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu with Vahvistukseton

trait LukionModuuliTaiPaikallinenOpintojakso2019 extends KoulutusmoduuliPakollinenLaajuus with Valinnaisuus {
  def laajuus: LaajuusOpintopisteissä
}

@Title("Lukion moduuli 2019")
@Description("Valtakunnallisen lukion/IB-lukion moduulin tunnistetiedot")
case class LukionModuuli2019(
  @Description("Lukion/IB-lukion valtakunnallinen moduuli")
  @KoodistoUri("moduulikoodistolops2021")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusOpintopisteissä,
  pakollinen: Boolean
) extends LukionModuuliTaiPaikallinenOpintojakso2019 with KoodistostaLöytyväKoulutusmoduuliPakollinenLaajuus

@Title("Lukion paikallinen opintojakso 2019")
@Description("Paikallisen lukion/IB-lukion opintojakson tunnistetiedot 2019")
case class LukionPaikallinenOpintojakso2019(
  tunniste: PaikallinenKoodi,
  @Discriminator
  laajuus: LaajuusOpintopisteissä,
  kuvaus: LocalizedString,
  pakollinen: Boolean
) extends LukionModuuliTaiPaikallinenOpintojakso2019 with PaikallinenKoulutusmoduuli with StorablePreference

trait LukionOppiaine2019 extends LukionOppiaine {
  def withLaajuus(laajuusArvo: Double): LukionLaajuudellinenOppiaine2019 = {
    val laajuudellinen = serializeWithRoot(this).merge(serializeWithRoot(Map("laajuus" -> LaajuusOpintopisteissä(laajuusArvo))))
    JsonSerializer.extract[LukionLaajuudellinenOppiaine2019](laajuudellinen )
  }
}

@Description("Lukion/IB-lukion oppiaineen tunnistetiedot 2019")
trait LukionLaajuudellinenOppiaine2019 extends KoulutusmoduuliPakollinenLaajuus with LukionOppiaine2019 {
  @Discriminator
  def laajuus: LaajuusOpintopisteissä
  override def perusteenDiaarinumero: Option[String] = None
}

@Title("Paikallinen oppiaine 2019")
case class PaikallinenLukionOppiaine2019(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  pakollinen: Boolean = false,
  @Discriminator
  laajuus: LaajuusOpintopisteissä
) extends LukionLaajuudellinenOppiaine2019 with PaikallinenKoulutusmoduuli with StorablePreference

trait LukionValtakunnallinenOppiaine2019 extends LukionOppiaine2019 with YleissivistavaOppiaine
trait LukionValtakunnallinenLaajuudellinenOppiaine2019 extends LukionLaajuudellinenOppiaine2019 with LukionValtakunnallinenOppiaine2019

@Title("Muu valtakunnallinen oppiaine 2019")
case class LukionMuuValtakunnallinenOppiaine2019(
  @KoodistoKoodiarvo("BI")
  @KoodistoKoodiarvo("ET")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("FY")
  @KoodistoKoodiarvo("GE")
  @KoodistoKoodiarvo("HI")
  @KoodistoKoodiarvo("KE")
  @KoodistoKoodiarvo("KU")
  @KoodistoKoodiarvo("LI")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("OP")
  @KoodistoKoodiarvo("PS")
  @KoodistoKoodiarvo("TE")
  @KoodistoKoodiarvo("YH")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @Discriminator
  laajuus: LaajuusOpintopisteissä
) extends LukionValtakunnallinenLaajuudellinenOppiaine2019

@Title("Uskonto 2019")
case class LukionUskonto2019(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @Discriminator
  laajuus: LaajuusOpintopisteissä,
  uskonnonOppimäärä: Option[Koodistokoodiviite] = None
) extends LukionValtakunnallinenLaajuudellinenOppiaine2019 with Uskonto

@Title("Äidinkieli ja kirjallisuus 2019")
@Description("Oppiaineena äidinkieli ja kirjallisuus")
case class LukionÄidinkieliJaKirjallisuus2019(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @Discriminator
  laajuus: LaajuusOpintopisteissä
) extends LukionValtakunnallinenLaajuudellinenOppiaine2019 with LukionÄidinkieliJaKirjallisuus {
  override def description: LocalizedString = kieliaineDescription
}

@Title("Vieras tai toinen kotimainen kieli 2019")
@Description("Oppiaineena vieras tai toinen kotimainen kieli 2019")
case class VierasTaiToinenKotimainenKieli2019(
  @KoodistoKoodiarvo("A")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  @KoodistoKoodiarvo("AOM") // TODO: rajoita kielivaihtoehdot suomi+ruotsi? Miksei tätä ole tehty myös perinteisessä 2. kotimaisessa?
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @Discriminator
  laajuus: LaajuusOpintopisteissä
) extends LukionValtakunnallinenLaajuudellinenOppiaine2019 with Kieliaine {
  override def description = kieliaineDescription
}

@Description("Oppiaineena vieras tai toinen kotimainen kieli")
case class LaajuudetonVierasTaiToinenKotimainenKieli2019(
  @KoodistoKoodiarvo("A")
  @KoodistoKoodiarvo("AOM")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true
) extends LukionValtakunnallinenOppiaine2019 with Kieliaine with Laajuudeton {
  override def description: LocalizedString = kieliaineDescription
  override def perusteenDiaarinumero: Option[String] = None
}

@Title("Matematiikka 2019")
@Description("Oppiaineena matematiikka")
case class LukionMatematiikka2019(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @Discriminator
  laajuus: LaajuusOpintopisteissä = LaajuusOpintopisteissä(1)
) extends LukionValtakunnallinenLaajuudellinenOppiaine2019 with KoodistostaLöytyväKoulutusmoduuliPakollinenLaajuus with Oppimäärä {
  override def description = oppimäärä.description
}

trait SuoritettavissaErityisenäTutkintona2019 {
  @DefaultValue(false)
  def suoritettuErityisenäTutkintona: Boolean
}

trait PuhviKokeellinen2019 {
  @Title("Puhvi-koe")
  @Description("Toisen asteen puheviestintätaitojen päättökoe")
  def puhviKoe: Option[PuhviKoe2019]
}

trait SuullisenKielitaidonKokeellinen2019 {
  def suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]]
}

@Title("Puhvi-koe 2019")
case class PuhviKoe2019(
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  @KoodistoKoodiarvo("S")
  @KoodistoKoodiarvo("H")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends ArviointiPäivämäärällä with YleissivistävänKoulutuksenArviointi


@Title("Suullisen kielitaidon koe 2019")
case class SuullisenKielitaidonKoe2019(
  @KoodistoUri("kieli")
  kieli: Koodistokoodiviite,
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  @KoodistoKoodiarvo("S")
  @KoodistoKoodiarvo("H")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  @KoodistoUri("arviointiasteikkosuullisenkielitaidonkoetaitotaso")
  taitotaso: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends ArviointiPäivämäärällä with YleissivistävänKoulutuksenArviointi
