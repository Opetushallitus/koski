package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{ComplexObject, FlattenInUI, KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation._

@Title("Pre-IB-opintojen suoritus 2019")
case class PreIBSuoritus2019(
  @Title("Koulutus")
  koulutusmoduuli: PreIBKoulutusmoduuli2019 = PreIBKoulutusmoduuli2019(),
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Title("Lukion oppimäärää täydentävät oman äidinkielen opinnot")
  omanÄidinkielenOpinnot: Option[OmanÄidinkielenOpinnotLaajuusOpintopisteinä] = None,
  puhviKoe: Option[PuhviKoe2019] = None,
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]] = None,
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PreIBSuorituksenOsasuoritus2019]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("preiboppimaara2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara2019", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends IBPäätasonSuoritus with Toimipisteellinen with Suorituskielellinen with Todistus with Arvioinniton with PuhviKokeellinen2019 with SuullisenKielitaidonKokeellinen2019 with Ryhmällinen

trait PreIBSuorituksenOsasuoritus2019 extends Suoritus

trait IBSuoritus2019 extends IBSuoritus

@Title("Pre-IB-koulutus 2019")
@Description("Pre-IB-koulutuksen tunnistetiedot 2019")
case class PreIBKoulutusmoduuli2019(
  @Description("Pre-IB-koulutuksen tunniste")
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("preiboppimaara2019")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara2019", koodistoUri = "suorituksentyyppi")
) extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton

trait PreIBOppiaineenSuoritus2019 extends IBSuoritus2019 with PreIBSuorituksenOsasuoritus2019

@Description("IB-oppiaineen suoritus Pre-IB-opinnoissa 2019")
@Title("IB-oppiaineen Pre-IB-suoritus 2019")
case class IBOppiaineenPreIBSuoritus2019(
  @Title("Oppiaine")
  koulutusmoduuli: PreIBIBOppiaine2019,
  arviointi: Option[List[LukionOppiaineenArviointi2019]] = None, // TODO: mitä arviointeja tässä pitäisi voida käyttää? Vanhassa Pre IB:ssä nämä IB-lukion omat oppiaineet arvioidaan lukion oppiaineina, vaikka arvioidaan eri tavalla ollessaan varsinaisissa IB-opinnoissa...
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[PreIBKurssinSuoritus2019]],
  @KoodistoKoodiarvo("preibiboppiaine2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preibiboppiaine2019", koodistoUri = "suorituksentyyppi")
) extends PreIBOppiaineenSuoritus2019

// TODO: Tälle sama kanta-trait kuin LukionOppiaineenSuoritus2019-luokalla, ja käytä sitä traittia käleissä ja yhteisissä validaatioissa
@Description("Lukion oppiaineen suoritus Pre-IB-opinnoissa 2019")
@Title("Lukion oppiaineen Pre-IB-suoritus 2019")
case class LukionOppiaineenPreIBSuoritus2019(
  @Title("Oppiaine")
  koulutusmoduuli: PreIBLukionOppiaine2019,
  arviointi: Option[List[LukionOppiaineenArviointi2019]] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvat osasuoritukset")
  @Title("Osasuoritukset")
  override val osasuoritukset: Option[List[PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019]],
  @KoodistoKoodiarvo("preiblukionoppiaine2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preiblukionoppiaine2019", koodistoUri = "suorituksentyyppi")
) extends PreIBOppiaineenSuoritus2019 with Vahvistukseton with MahdollisestiSuorituskielellinen with SuoritettavissaErityisenäTutkintona2019

// TODO: Tälle sama kanta-trait kuin MuidenLukioOpintojenSuoritus2019-luokalla, ja käytä sitä traittia käleissä ja yhteisissä validaatioissa
@Description("Muiden lukio-opintojen suoritus Pre-IB-opinnoissa 2019")
@Title("Muiden lukio-opintojen PreIB-suoritus 2019")
case class MuidenLukioOpintojenPreIBSuoritus2019(
  @Title("Oppiaine")
  koulutusmoduuli: PreIBMuutSuorituksetTaiVastaavat2019,
  @Description("Oppiaineeseen kuuluvat osasuoritukset")
  @Title("Osasuoritukset")
  override val osasuoritukset: Option[List[PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019]],
  @KoodistoKoodiarvo("preiblukionmuuopinto2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preiblukionmuuopinto2019", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus2019 with PreIBSuorituksenOsasuoritus2019 with Vahvistukseton with Arvioinniton {
  override def suorituskieli: Option[Koodistokoodiviite] = None
}

trait PreIBMuutSuorituksetTaiVastaavat2019 extends Koulutusmoduuli

trait PreIBIBOppiaine2019 extends Koulutusmoduuli
trait PreIBLukionOppiaine2019 extends Koulutusmoduuli

@Title("Pre-IB-kurssin suoritus 2019")
case class PreIBKurssinSuoritus2019(
  @Description("Pre-IB-kurssin tunnistetiedot 2019")
  koulutusmoduuli: PreIBKurssi2019,
  arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = None, // TODO: mikä tässä pitäisi olla arviointina? Vanhassa Pre-IB:ssä oli lukion kurssin arviointi, mutta onko tässä uuden lukion osalta järkeä?
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("preibkurssi2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preibkurssi2019", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus with KurssinSuoritus

trait PreIBKurssi2019 extends Koulutusmoduuli

// TODO: Yhteinen kanta-trait LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 kanssa?
trait PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 extends IBSuoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu with Vahvistukseton {
  def koulutusmoduuli: PreIBLukionModuuliTaiPaikallinenOpintojakso2019
  @FlattenInUI
  def arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]]
  @ComplexObject
  def tunnustettu: Option[OsaamisenTunnustaminen]
}

trait PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 extends PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019
trait PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 extends PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019

// TODO: Tälle sama kanta-trait kuin LukionModuulinSuoritus2019-traitilla, ja käytä sitä traittia käleissä ja yhteisissä validaatioissa
trait PreIBLukionModuulinSuoritus2019 extends ValtakunnallisenModuulinSuoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu {
  @Description("Lukion moduulin tunnistetiedot")
  def koulutusmoduuli: PreIBLukionModuuli2019
  def arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]]
  def tunnustettu: Option[OsaamisenTunnustaminen]
  def suorituskieli: Option[Koodistokoodiviite]
  @KoodistoKoodiarvo("preiblukionvaltakunnallinenmoduuli2019")
  def tyyppi: Koodistokoodiviite
}

@Title("Pre-IB lukion moduulin suoritus oppiaineissa 2019")
@OnlyWhen("../../tyyppi/koodiarvo", "preiblukionoppiaine2019")
case class PreIBLukionModuulinSuoritusOppiaineissa2019(
  @Description("Pre-IB lukion moduulin tunnistetiedot oppiaineissa 2019")
  @Title("Osasuoritus")
  @FlattenInUI
  koulutusmoduuli: PreIBLukionModuuliOppiaineissa2019,
  arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preiblukionvaltakunnallinenmoduuli2019", koodistoUri = "suorituksentyyppi")
) extends PreIBLukionModuulinSuoritus2019 with PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019

@Title("Pre-IB lukion moduulin suoritus muissa opinnoissa 2019")
@OnlyWhen("../../tyyppi/koodiarvo", "preiblukionmuuopinto2019")
case class PreIBLukionModuulinSuoritusMuissaOpinnoissa2019(
  @Description("Pre-IB lukion moduulin tunnistetiedot muissa opinnoissa 2019")
  @Title("Osasuoritus")
  @FlattenInUI
  koulutusmoduuli: PreIBLukionModuuliMuissaOpinnoissa2019,
  arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preiblukionvaltakunnallinenmoduuli2019", koodistoUri = "suorituksentyyppi")
) extends PreIBLukionModuulinSuoritus2019 with PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019

@Title("Pre-IB lukion paikallisen opintojakson suoritus 2019")
case class PreIBLukionPaikallisenOpintojaksonSuoritus2019(
  @Description("Pre-IB lukion paikallisen opintojakson tunnistetiedot 2019")
  @Title("Paikallinen opintojakso")
  @FlattenInUI
  koulutusmoduuli: PreIBPaikallinenOpintojakso2019,
  arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("preiblukionpaikallinenopintojakso2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preiblukionpaikallinenopintojakso2019", koodistoUri = "suorituksentyyppi")
) extends PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 with PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019

trait PreIBLukionModuuliTaiPaikallinenOpintojakso2019 extends Koulutusmoduuli

trait PreIBLukionModuuli2019 extends PreIBLukionModuuliTaiPaikallinenOpintojakso2019

trait PreIBLukionModuuliOppiaineissa2019 extends PreIBLukionModuuli2019
trait PreIBLukionModuuliMuissaOpinnoissa2019 extends PreIBLukionModuuli2019
trait PreIBPaikallinenOpintojakso2019 extends PreIBLukionModuuliTaiPaikallinenOpintojakso2019
