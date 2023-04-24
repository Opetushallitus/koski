package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

trait LukionPäätasonSuoritus2019
  extends LukionPäätasonSuoritus
    with Todistus
    with Arvioinniton
    with PuhviKokeellinen2019
    with SuullisenKielitaidonKokeellinen2019
    with OpintopistelaajuuksienYhteislaskennallinenPäätasonSuoritus[LaajuusOpintopisteissä] {
  def koulutusmoduuli: Koulutusmoduuli with Diaarinumerollinen
  @KoodistoUri("lukionoppimaara")
  @Title("Opetussuunnitelma")
  @Description("Tieto siitä, suoritetaanko lukiota nuorten vai aikuisten oppimäärän mukaisesti")
  def oppimäärä: Koodistokoodiviite
  override def osasuoritukset: Option[List[LukionOppimääränOsasuoritus2019]] = None
}

@Title("Lukion oppimäärän suoritus 2019")
@Description("Lukion oppimäärän opetussuunnitelman 2019 mukaiset suoritustiedot")
@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", "OPH-2263-2019")
@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", "OPH-2267-2019")
case class LukionOppimääränSuoritus2019(
  @Title("Koulutus")
  koulutusmoduuli: LukionOppimäärä,
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  @Description("Oppimäärän suorituksen opetuskieli/suorituskieli. Rahoituksen laskennassa käytettävä tieto.")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Osallistuminen lukiokoulutusta täydentävän saamen/romanikielen/opiskelijan oman äidinkielen opiskeluun")
  @Title("Lukion oppimäärää täydentävät oman äidinkielen opinnot")
  omanÄidinkielenOpinnot: Option[OmanÄidinkielenOpinnotLaajuusOpintopisteinä] = None,
  puhviKoe: Option[PuhviKoe2019] = None,
  @Description("Vahvistetussa lukion oppimäärän suorituksessa tulee olla suullisen kielitaidon kokeen suoritus niistä kielistä, joissa on suoritettu suullisen kielitaidon kokeen sisältävä valtakunnallinen moduuli. Nämä moduulit ovat ENA8, FIM8, FINA8, FINB16, RUA8, RUB16, RUÄ8, SMA8 ja VKA8.")
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[LukionOppimääränOsasuoritus2019]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus2019
  with Ryhmällinen
  with KoulusivistyskieliKieliaineesta
  with SuoritettavissaErityisenäTutkintona2019
  with Oppimäärällinen
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta
  with LukionOppimääränSuoritus

@Title("Lukion oppiaineiden oppimäärien suoritus 2019")
@Description("Lukion oppiaineiden oppimäärien suoritustiedot 2019")
case class LukionOppiaineidenOppimäärienSuoritus2019(
  @Title("Koulutus")
  koulutusmoduuli: LukionOppiaineidenOppimäärät2019,
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Description("Merkitään, jos lukion oppimäärä on tullut suoritetuksi aineopintoina.")
  @DefaultValue(None)
  @Deprecated("Käytä opiskeluoikeuden kenttää 'oppimääräSuoritettu'")
  @Hidden
  lukionOppimääräSuoritettu: Option[Boolean] = None,
  puhviKoe: Option[PuhviKoe2019] = None,
  @Description("Arvioituun lukion kielioppiainesuoritukseen liittyen tulee aineopintosuorituksen päätasolta löytyä suullisen kielitaidon kokeen suoritus niistä kielistä, joissa on suoritettu suullisen kielitaidon kokeen sisältävä valtakunnallinen moduuli. Nämä moduulit ovat ENA8, FIM8, FINA8, FINB16, RUA8, RUB16, RUÄ8, SMA8 ja VKA8.")
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[LukionOppimääränOsasuoritus2019]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionaineopinnot")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionaineopinnot", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus2019 with Ryhmällinen with Välisuoritus with Oppimäärällinen {
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
  @Description("Käytä aina merkkijonoa lukionaineopinnot")
  @DefaultValue("lukionaineopinnot")
  koodiarvo: String = "lukionaineopinnot"
) extends PaikallinenKoodiviite {
  override def nimi: LocalizedString = LocalizedString.empty
  override def toString = s"$koodiarvo"
}

trait LukionOppimääränOsasuoritus2019
  extends LukionOppimääränPäätasonOsasuoritus
    with OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusOpintopisteissä] {
  @Title("Moduulit ja paikalliset opintojaksot")
  override def osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019]] = None

  override def osasuoritusLista: List[LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019] = {
    osasuoritukset.toList.flatten
  }

  override final def withOsasuoritukset(oss: Option[List[Suoritus]]): LukionOppimääränOsasuoritus2019 = {
    import mojave._
    shapeless.lens[LukionOppimääränOsasuoritus2019].field[Option[List[Suoritus]]]("osasuoritukset").set(this)(oss)
  }
}

@Title("Muiden lukio-opintojen suoritus 2019")
@Description("Kategoria opintojaksoille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esim. lukiodiplomit tai temaattiset opinnot.")
case class MuidenLukioOpintojenSuoritus2019(
  @KoodistoKoodiarvo("lukionmuuopinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionmuuopinto", "suorituksentyyppi"),
  koulutusmoduuli: MuutSuorituksetTaiVastaavat2019,
  @MinItems(1)
  @Description("Moduulien ja paikallisten opintojaksojen suoritukset")
  override val osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019]]
) extends LukionOppimääränOsasuoritus2019 with Vahvistukseton with Arvioinniton

@Title("Lukion oppiaineen suoritus 2019")
@Description("Lukion oppiaineen suoritustiedot 2019")
case class LukionOppiaineenSuoritus2019(
  koulutusmoduuli: LukionOppiaine2019,
  arviointi: Option[List[LukionOppiaineenArviointi2019]] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  @Title("Suorituskieli, jos muu kuin opetuskieli")
  @Description("Suorituskieli, mikäli opiskelija on opiskellut yli puolet oppiaineen oppimäärän opinnoista muulla kuin koulun varsinaisella opetuskielellä.")
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien moduulien ja paikallisten opintojaksojen suoritukset")
  override val osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019]],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with Vahvistukseton with LukionOppimääränOsasuoritus2019 with MahdollisestiSuorituskielellinen with SuoritettavissaErityisenäTutkintona2019

trait LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 extends Suoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu with Vahvistukseton {
  def koulutusmoduuli: LukionModuuliTaiPaikallinenOpintojakso2019
  @FlattenInUI
  def arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]]
  @ComplexObject
  def tunnustettu: Option[OsaamisenTunnustaminen]
}

trait LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 extends LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019

trait LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 extends LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019

trait LukionModuulinSuoritus2019 extends ValtakunnallisenModuulinSuoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu {
  @Description("Lukion moduulin tunnistetiedot")
  def koulutusmoduuli: LukionModuuli2019
  def arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]]
  @Description("Jos moduuli on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot. Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia opintoja. Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä. Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan moduuliin, tulee moduulista antaa numeroarvosana")
  def tunnustettu: Option[OsaamisenTunnustaminen]
  def suorituskieli: Option[Koodistokoodiviite]
  @KoodistoKoodiarvo("lukionvaltakunnallinenmoduuli")
  def tyyppi: Koodistokoodiviite
}

@Title("Lukion moduulin suoritus oppiaineissa 2019")
@Description("Lukion moduulin suoritustiedot oppiaineissa 2019")
@OnlyWhen("../../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../../tyyppi/koodiarvo", "luvalukionoppiaine2019")
case class LukionModuulinSuoritusOppiaineissa2019(
  koulutusmoduuli: LukionModuuliOppiaineissa2019,
  arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionvaltakunnallinenmoduuli", koodistoUri = "suorituksentyyppi")
) extends LukionModuulinSuoritus2019 with LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019

@Title("Lukion moduulin suoritus muissa opinnoissa 2019")
@Description("Lukion moduulin suoritustiedot muissa opinnoissa 2019")
@OnlyWhen("../../tyyppi/koodiarvo", "lukionmuuopinto")
case class LukionModuulinSuoritusMuissaOpinnoissa2019(
  koulutusmoduuli: LukionModuuliMuissaOpinnoissa2019,
  arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionvaltakunnallinenmoduuli", koodistoUri = "suorituksentyyppi")
) extends LukionModuulinSuoritus2019 with LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019

trait MuutSuorituksetTaiVastaavat2019
  extends KoodistostaLöytyväKoulutusmoduuliValinnainenLaajuus
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusOpintopisteissä]
    with PreIBMuutSuorituksetTaiVastaavat2019 {
  @KoodistoUri("lukionmuutopinnot")
  def tunniste: Koodistokoodiviite
  override def makeLaajuus(laajuusArvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(laajuusArvo)
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
  arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = None,
  @Description("Jos opintojakso on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot. Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia pakollisia tai vapaaehtoisia opintoja. Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä. Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan opintojaksoon, tulee opintojaksosta antaa numeroarvosana")
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionpaikallinenopintojakso")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionpaikallinenopintojakso", koodistoUri = "suorituksentyyppi")
) extends LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 with LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu with Vahvistukseton

trait LukionModuuliTaiPaikallinenOpintojakso2019 extends KoulutusmoduuliPakollinenLaajuusOpintopisteissä with Valinnaisuus with PreIBLukionModuuliTaiPaikallinenOpintojakso2019

@Description("Valtakunnallisen lukion/IB-lukion moduulin tunnistetiedot")
trait LukionModuuli2019 extends LukionModuuliTaiPaikallinenOpintojakso2019 with KoodistostaLöytyväKoulutusmoduuliPakollinenLaajuus with PreIBLukionModuuli2019 {
  @Description("Lukion/IB-lukion valtakunnallinen moduuli")
  @KoodistoUri("moduulikoodistolops2021")
  @Title("Nimi")
  def tunniste: Koodistokoodiviite
  def laajuus: LaajuusOpintopisteissä
  def pakollinen: Boolean
}

trait LukionModuuliOppiaineissa2019 extends LukionModuuli2019 with PreIBLukionModuuliOppiaineissa2019

trait LukionModuuliMuissaOpinnoissa2019 extends LukionModuuli2019 with PreIBLukionModuuliMuissaOpinnoissa2019

@Title("Lukion vieraan kielen moduuli muissa opinnoissa 2019")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "MS")
case class LukionVieraanKielenModuuliMuissaOpinnoissa2019(
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusOpintopisteissä,
  pakollinen: Boolean,
  @Description("Pakollinen tieto VK-alkuisille moduuleille. Muille vieraan kielen moduuleille täytetään moduulin koodin perusteella.")
  @KoodistoUri("kielivalikoima")
  @Discriminator
  kieli: Koodistokoodiviite
) extends LukionModuuliMuissaOpinnoissa2019

@Title("Lukion muu moduuli muissa opinnoissa 2019")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "MS")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "LD")
case class LukionMuuModuuliMuissaOpinnoissa2019(
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusOpintopisteissä,
  pakollinen: Boolean
) extends LukionModuuliMuissaOpinnoissa2019

@Title("Lukion vieraan kielen moduuli oppiaineissa 2019")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "A")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "B1")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "B2")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "B3")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "AOM")
case class LukionVieraanKielenModuuliOppiaineissa2019(
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusOpintopisteissä,
  pakollinen: Boolean,
  @Description("Täytetään tiedonsiirrossa automaattisesti oppiaineen kielen perusteella.")
  @KoodistoUri("kielivalikoima")
  kieli: Option[Koodistokoodiviite] = None
) extends LukionModuuliOppiaineissa2019

@Title("Lukion muu moduuli oppiaineissa 2019")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "MA")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "BI")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "ET")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "FI")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "FY")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "GE")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "HI")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "KE")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "KU")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "LI")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "MU")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "OP")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "PS")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "TE")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "YH")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "KT")
@OnlyWhen("../../../koulutusmoduuli/tunniste/koodiarvo", "AI")
case class LukionMuuModuuliOppiaineissa2019(
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusOpintopisteissä,
  pakollinen: Boolean
) extends LukionModuuliOppiaineissa2019

@Title("Lukion paikallinen opintojakso 2019")
@Description("Paikallisen lukion/IB-lukion opintojakson tunnistetiedot 2019")
case class LukionPaikallinenOpintojakso2019(
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  laajuus: LaajuusOpintopisteissä,
  kuvaus: LocalizedString,
  pakollinen: Boolean
) extends LukionModuuliTaiPaikallinenOpintojakso2019 with PaikallinenKoulutusmoduuliKuvauksella with StorablePreference  with PreIBPaikallinenOpintojakso2019

@Description("Lukion/IB-lukion oppiaineen tunnistetiedot 2019")
trait LukionOppiaine2019
  extends LukionOppiaine
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusOpintopisteissä]
    with KoulutusmoduuliValinnainenLaajuus
    with PreIBLukionOppiaine2019 {
  def laajuus: Option[LaajuusOpintopisteissä]
  override def perusteenDiaarinumero: Option[String] = None
  override def makeLaajuus(laajuusArvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(laajuusArvo)
}

@Title("Paikallinen oppiaine 2019")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine2019")
case class PaikallinenLukionOppiaine2019(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  pakollinen: Boolean = false,
  @DefaultValue(None)
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends LukionOppiaine2019 with PaikallinenKoulutusmoduuliKuvauksella with StorablePreference

trait LukionValtakunnallinenOppiaine2019 extends LukionOppiaine2019 with YleissivistavaOppiaine

@Title("Muu valtakunnallinen oppiaine 2019")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine2019")
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
  @DefaultValue(None)
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends LukionValtakunnallinenOppiaine2019

@Title("Uskonto 2019")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine2019")
case class LukionUskonto2019(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusOpintopisteissä] = None,
  uskonnonOppimäärä: Option[Koodistokoodiviite] = None
) extends LukionValtakunnallinenOppiaine2019 with Uskonto

@Title("Äidinkieli ja kirjallisuus 2019")
@Description("Oppiaineena äidinkieli ja kirjallisuus")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine2019")
case class LukionÄidinkieliJaKirjallisuus2019(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends LukionValtakunnallinenOppiaine2019 with LukionÄidinkieliJaKirjallisuus {
  override def description: LocalizedString = kieliaineDescription
}

@Title("Vieras tai toinen kotimainen kieli 2019")
@Description("Oppiaineena vieras tai toinen kotimainen kieli 2019")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine2019")
case class VierasTaiToinenKotimainenKieli2019(
  @KoodistoKoodiarvo("A")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  @KoodistoKoodiarvo("AOM")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends LukionValtakunnallinenOppiaine2019 with Kieliaine {
  override def description = kieliaineDescription
}

@Title("Matematiikka 2019")
@Description("Oppiaineena matematiikka")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine2019")
case class LukionMatematiikka2019(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends LukionValtakunnallinenOppiaine2019 with KoodistostaLöytyväKoulutusmoduuliValinnainenLaajuus with Oppimäärä {
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
  @KoodistoUri("kielivalikoima")
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
  @KoodistoUri("arviointiasteikkokehittyvankielitaidontasot")
  @KoodistoKoodiarvo("alle_A1.1")
  @KoodistoKoodiarvo("A1.1")
  @KoodistoKoodiarvo("A1.2")
  @KoodistoKoodiarvo("A1.3")
  @KoodistoKoodiarvo("A2.1")
  @KoodistoKoodiarvo("A2.2")
  @KoodistoKoodiarvo("B1.1")
  @KoodistoKoodiarvo("B1.2")
  @KoodistoKoodiarvo("B2.1")
  @KoodistoKoodiarvo("B2.2")
  @KoodistoKoodiarvo("C1.1")
  @KoodistoKoodiarvo("yli_C1.1")
  taitotaso: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends ArviointiPäivämäärällä with YleissivistävänKoulutuksenArviointi

trait LukionOppiaineenArviointi2019 extends YleissivistävänKoulutuksenArviointi {
  @Description("Oppiaineen suorituksen arvosana on kokonaisarvosana oppiaineelle")
  def arvosana: Koodistokoodiviite
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  def päivä: Option[LocalDate]

  def arviointipäivä = päivä
}

@Title("Numeerinen lukion oppiaineen arviointi 2019")
case class NumeerinenLukionOppiaineenArviointi2019(
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate]
) extends LukionOppiaineenArviointi2019

object NumeerinenLukionOppiaineenArviointi2019 {
  def apply(arvosana: String) = new NumeerinenLukionOppiaineenArviointi2019(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), None)
}

@Title("Sanallinen lukion oppiaineen arviointi 2019")
case class SanallinenLukionOppiaineenArviointi2019(
  @KoodistoKoodiarvo("H")
  @KoodistoKoodiarvo("S")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate]
) extends LukionOppiaineenArviointi2019

object SanallinenLukionOppiaineenArviointi2019 {
  def apply(arvosana: String) = new SanallinenLukionOppiaineenArviointi2019(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), None)
}

trait LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 extends ArviointiPäivämäärällä

@Title("Numeerinen lukion moduulin tai paikallisen opintojakson arviointi 2019")
case class NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate
) extends LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 with YleissivistävänKoulutuksenArviointi

@Title("Sanallinen lukion moduulin tai paikallisen opintojakson arviointi 2019")
case class SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(
  @KoodistoKoodiarvo("H")
  @KoodistoKoodiarvo("S")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 with YleissivistävänKoulutuksenArviointi
