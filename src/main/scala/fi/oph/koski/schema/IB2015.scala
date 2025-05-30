package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation._

@Title("Pre-IB-opintojen suoritus")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "preiboppimaara")
case class PreIBSuoritus2015(
  @Title("Koulutus")
  koulutusmoduuli: PreIBKoulutusmoduuli2015 = PreIBKoulutusmoduuli2015(),
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PreIBSuorituksenOsasuoritus2015]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("preiboppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends IBPäätasonSuoritus with OppivelvollisuudenSuorittamiseenKelpaava with Ryhmällinen

@Title("Pre-IB-koulutus")
@Description("Pre-IB-koulutuksen tunnistetiedot")
case class PreIBKoulutusmoduuli2015(
  @Description("Pre-IB-koulutuksen tunniste")
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("preiboppimaara")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara", koodistoUri = "suorituksentyyppi")
) extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton

@Description("Pre-IB-oppiaineiden suoritusten tiedot")
@Title("Pre-IB-oppiaineen suoritus")
case class PreIBOppiaineenSuoritus2015(
  @Title("Oppiaine")
  koulutusmoduuli: PreIBOppiaine2015,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[PreIBKurssinSuoritus2015]],
  @KoodistoKoodiarvo("preiboppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preiboppiaine", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus with PreIBSuorituksenOsasuoritus2015

trait PreIBOppiaine2015 extends Koulutusmoduuli

@Title("Pre-IB-kurssin suoritus")
case class PreIBKurssinSuoritus2015(
  @Description("Pre-IB-kurssin tunnistetiedot")
  koulutusmoduuli: PreIBKurssi2015,
  arviointi: Option[List[LukionArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("preibkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preibkurssi", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus with KurssinSuoritus

trait PreIBKurssi2015 extends Koulutusmoduuli
