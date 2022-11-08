package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, Title}
import fi.oph.koski.schema.annotation._

case class AikuistenPerusopetuksenAlkuvaiheenSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava koulutus ja koulutuksen opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaihe,
  @Description("Luokan tunniste, esimerkiksi 9C.")
  @Tooltip("Luokan tunniste, esimerkiksi 9C.")
  luokka: Option[String] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Tooltip("Osallistuminen perusopetusta täydentävän saamen/romanikielen/opiskelijan oman äidinkielen opiskeluun")
  omanÄidinkielenOpinnot: Option[OmanÄidinkielenOpinnotLaajuusKursseina] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus]] = None,
  @Tooltip("Mahdolliset todistuksella näkyvät lisätiedot.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaaranalkuvaihe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("aikuistenperusopetuksenoppimaaranalkuvaihe", koodistoUri = "suorituksentyyppi")
) extends AikuistenPerusopetuksenPäätasonSuoritus
  with Suoritus
  with Todistus
  with Arvioinniton
  with SuoritustavallinenPerusopetuksenSuoritus
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

@Description("Aikuisten perusopetuksen alkuvaiheen tunnistetiedot")
case class AikuistenPerusopetuksenAlkuvaihe(
 perusteenDiaarinumero: Option[String],
 @KoodistoUri("suorituksentyyppi")
 @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaaranalkuvaihe")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("aikuistenperusopetuksenoppimaaranalkuvaihe", koodistoUri = "suorituksentyyppi")
) extends KoodistostaLöytyväKoulutusmoduuli with Diaarinumerollinen with Laajuudeton

@Description("Oppiaineen suoritus osana aikuisten perusopetuksen oppimäärän alkuvaiheen suoritusta")
case class AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus(
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenOppiaine,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Title("Kurssit")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenalkuvaiheenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenalkuvaiheenoppiaine", koodistoUri = "suorituksentyyppi"),
  suoritustapa: Option[Koodistokoodiviite] = None
) extends OppiaineenSuoritus with Vahvistukseton with MahdollisestiSuorituskielellinen with SuoritustapanaMahdollisestiErityinenTutkinto with MahdollisestiArvioinniton {
  override def salliDuplikaatit: Boolean = true
}

case class AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus(
  @Description("Aikuisten perusopetuksen alkuvaiheen kurssin tunnistetiedot")
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenKurssi,
  @FlattenInUI
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenalkuvaiheenkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenalkuvaiheenkurssi", koodistoUri = "suorituksentyyppi"),
  @Description("Jos kurssi on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot.")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None
) extends MahdollisestiSuorituskielellinen with AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus

@Description("Perusopetuksen oppiaineen tunnistetiedot")
sealed trait AikuistenPerusopetuksenAlkuvaiheenOppiaine extends KoulutusmoduuliValinnainenLaajuus with Laajuudeton {
  @Title("Oppiaine")
  def tunniste: KoodiViite
}

trait AikuistenPerusopetuksenAlkuvaiheenKoodistostaLöytyväOppiaine extends AikuistenPerusopetuksenAlkuvaiheenOppiaine with KoodistostaLöytyväKoulutusmoduuli {
  @Description("Oppiaine")
  @KoodistoUri("aikuistenperusopetuksenalkuvaiheenoppiaineet")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
}

@Title("Paikallinen oppiaine")
case class AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine(
  tunniste: PaikallinenKoodi,
  @Tooltip("Paikallisen oppiaineen vapaamuotoinen kuvaus.")
  kuvaus: LocalizedString
) extends AikuistenPerusopetuksenAlkuvaiheenOppiaine with PaikallinenKoulutusmoduuliKuvauksella with StorablePreference

@Title("Muu oppiaine")
case class MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine(
  @KoodistoKoodiarvo("MA")
  @KoodistoKoodiarvo("YH")
  @KoodistoKoodiarvo("YL")
  @KoodistoKoodiarvo("TE")
  @KoodistoKoodiarvo("OP")
  tunniste: Koodistokoodiviite
) extends AikuistenPerusopetuksenAlkuvaiheenKoodistostaLöytyväOppiaine

@Title("Äidinkieli ja kirjallisuus")
case class AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "aikuistenperusopetuksenalkuvaiheenoppiaineet"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite
) extends AikuistenPerusopetuksenAlkuvaiheenKoodistostaLöytyväOppiaine with Äidinkieli

@Title("Vieras tai toinen kotimainen kieli")
@Description("Oppiaineena vieras tai toinen kotimainen kieli")
case class AikuistenPerusopetuksenAlkuvaiheenVierasKieli(
  @KoodistoKoodiarvo("A1")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("A1", koodistoUri = "aikuistenperusopetuksenalkuvaiheenoppiaineet"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite
) extends AikuistenPerusopetuksenAlkuvaiheenKoodistostaLöytyväOppiaine with Kieliaine {
  override def description = kieliaineDescription
}

sealed trait AikuistenPerusopetuksenAlkuvaiheenKurssi extends KoulutusmoduuliValinnainenLaajuus {
  def laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa]
}

case class PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi(
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None
) extends AikuistenPerusopetuksenAlkuvaiheenKurssi with PaikallinenKoulutusmoduuliKuvauksella with StorablePreference {
  def kuvaus: LocalizedString = LocalizedString.empty
}

@Title("Aikuisten perusopetuksen opetussuunnitelman 2017 mukainen kurssi")
case class ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017(
  @KoodistoUri("aikuistenperusopetuksenalkuvaiheenkurssit2017")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None
) extends AikuistenPerusopetuksenAlkuvaiheenKurssi with KoodistostaLöytyväKoulutusmoduuli
