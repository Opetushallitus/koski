package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{ComplexObject, FlattenInUI, KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation._

@Title("Pre-IB-opintojen suoritus 2019")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "preiboppimaara2019")
case class PreIBSuoritus2019(
  @Title("Koulutus")
  koulutusmoduuli: PreIBKoulutusmoduuli2019 = PreIBKoulutusmoduuli2019(),
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Title("Lukion oppimäärää täydentävät oman äidinkielen opinnot")
  omanÄidinkielenOpinnot: Option[LukionOmanÄidinkielenOpinnot] = None,
  puhviKoe: Option[PuhviKoe2019] = None,
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]] = None,
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PreIBSuorituksenOsasuoritus2019]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("preiboppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends IBPäätasonSuoritus
  with Toimipisteellinen
  with Suorituskielellinen
  with Todistus
  with Arvioinniton
  with PuhviKokeellinen2019
  with SuullisenKielitaidonKokeellinen2019
  with Ryhmällinen
  with OpintopistelaajuuksienYhteislaskennallinenPäätasonSuoritus[LaajuusOpintopisteissä]
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

trait PreIBSuorituksenOsasuoritus2019
  extends Suoritus with OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusOpintopisteissä]

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

trait PreIBLukionOsasuoritus2019 extends PreIBSuorituksenOsasuoritus2019 {
  @Title("Moduulit ja paikalliset opintojaksot")
  override def osasuoritukset: Option[List[PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019]] = None

  override def osasuoritusLista: List[PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019] = {
    osasuoritukset.toList.flatten
  }

  override final def withOsasuoritukset(oss: Option[List[Suoritus]]): PreIBLukionOsasuoritus2019 = {
    import mojave._
    shapeless.lens[PreIBLukionOsasuoritus2019].field[Option[List[Suoritus]]]("osasuoritukset").set(this)(oss)
  }
}

@Description("Lukion oppiaineen suoritus Pre-IB-opinnoissa 2019")
@Title("Lukion oppiaineen Pre-IB-suoritus 2019")
case class LukionOppiaineenPreIBSuoritus2019(
  @Title("Oppiaine")
  koulutusmoduuli: PreIBLukionOppiaine2019,
  arviointi: Option[List[LukionOppiaineenArviointi2019]] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  suorituskieli: Option[Koodistokoodiviite] = None,
  override val osasuoritukset: Option[List[PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019]],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine", koodistoUri = "suorituksentyyppi")
) extends PreIBOppiaineenSuoritus2019 with PreIBLukionOsasuoritus2019 with Vahvistukseton with MahdollisestiSuorituskielellinen with SuoritettavissaErityisenäTutkintona2019

@Description("Muiden lukio-opintojen suoritus Pre-IB-opinnoissa 2019")
@Title("Muiden lukio-opintojen PreIB-suoritus 2019")
case class MuidenLukioOpintojenPreIBSuoritus2019(
  @Title("Oppiaine")
  koulutusmoduuli: PreIBMuutSuorituksetTaiVastaavat2019,
  override val osasuoritukset: Option[List[PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019]],
  @KoodistoKoodiarvo("lukionmuuopinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionmuuopinto", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus2019 with PreIBLukionOsasuoritus2019 with Vahvistukseton with Arvioinniton {
  override def suorituskieli: Option[Koodistokoodiviite] = None
}

trait PreIBMuutSuorituksetTaiVastaavat2019
  extends Koulutusmoduuli with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusOpintopisteissä]

trait PreIBLukionOppiaine2019
  extends Koulutusmoduuli
    with Valinnaisuus
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusOpintopisteissä]

trait PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 extends IBSuoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu with Vahvistukseton {
  def koulutusmoduuli: PreIBLukionModuuliTaiPaikallinenOpintojakso2019
  @FlattenInUI
  def arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]]
  @ComplexObject
  def tunnustettu: Option[OsaamisenTunnustaminen]
}

trait PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 extends PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019
trait PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 extends PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019

trait PreIBLukionModuulinSuoritus2019 extends ValtakunnallisenModuulinSuoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu {
  @Description("Lukion moduulin tunnistetiedot")
  def koulutusmoduuli: PreIBLukionModuuli2019
  def arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]]
  def tunnustettu: Option[OsaamisenTunnustaminen]
  def suorituskieli: Option[Koodistokoodiviite]
  @KoodistoKoodiarvo("lukionvaltakunnallinenmoduuli")
  def tyyppi: Koodistokoodiviite
}

@Title("Pre-IB lukion moduulin suoritus oppiaineissa 2019")
@OnlyWhen("../../tyyppi/koodiarvo", "lukionoppiaine")
case class PreIBLukionModuulinSuoritusOppiaineissa2019(
  @Description("Pre-IB lukion moduulin tunnistetiedot oppiaineissa 2019")
  @Title("Osasuoritus")
  @FlattenInUI
  koulutusmoduuli: PreIBLukionModuuliOppiaineissa2019,
  arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionvaltakunnallinenmoduuli", koodistoUri = "suorituksentyyppi")
) extends PreIBLukionModuulinSuoritus2019 with PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019

@Title("Pre-IB lukion moduulin suoritus muissa opinnoissa 2019")
@OnlyWhen("../../tyyppi/koodiarvo", "lukionmuuopinto")
case class PreIBLukionModuulinSuoritusMuissaOpinnoissa2019(
  @Description("Pre-IB lukion moduulin tunnistetiedot muissa opinnoissa 2019")
  @Title("Osasuoritus")
  @FlattenInUI
  koulutusmoduuli: PreIBLukionModuuliMuissaOpinnoissa2019,
  arviointi: Option[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionvaltakunnallinenmoduuli", koodistoUri = "suorituksentyyppi")
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
  @KoodistoKoodiarvo("lukionpaikallinenopintojakso")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionpaikallinenopintojakso", koodistoUri = "suorituksentyyppi")
) extends PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 with PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019

trait PreIBLukionModuuliTaiPaikallinenOpintojakso2019 extends Koulutusmoduuli with KoulutusmoduuliPakollinenLaajuusOpintopisteissä with Valinnaisuus

trait PreIBLukionModuuli2019 extends PreIBLukionModuuliTaiPaikallinenOpintojakso2019 with KoodistostaLöytyväKoulutusmoduuli

trait PreIBLukionModuuliOppiaineissa2019 extends PreIBLukionModuuli2019
trait PreIBLukionModuuliMuissaOpinnoissa2019 extends PreIBLukionModuuli2019
trait PreIBPaikallinenOpintojakso2019 extends PreIBLukionModuuliTaiPaikallinenOpintojakso2019
