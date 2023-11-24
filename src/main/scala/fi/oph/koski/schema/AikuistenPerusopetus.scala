package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

@Description("Aikuisten perusopetuksen opiskeluoikeus")
case class AikuistenPerusopetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila,
  lisätiedot: Option[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[AikuistenPerusopetuksenPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.aikuistenperusopetus,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  @Description("Oppijan oppimäärän päättymispäivä")
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

case class AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(
  @KoodistoUri("perusopetuksentukimuoto")
  @Description("Opiskelijan saamat laissa säädetyt tukimuodot.")
  @Tooltip("Opiskelijan saamat laissa säädetyt tukimuodot (avustajapalvelut, erityiset apuvälineet, osa-aikainen erityisopetus, tukiopetus ja/tai tulkitsemispalvelut).")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @RedundantData
  tukimuodot: Option[List[Koodistokoodiviite]] = None,
  @Description("Tehostetun tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty.")
  @Description("Mahdollisen tehostetun tuen päätös päätöksen alkamis- ja päättymispäivät.")
  @OksaUri("tmpOKSAID511", "tehostettu tuki")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @RedundantData
  tehostetunTuenPäätös: Option[Aikajakso] = None,
  @Description("Tehostetun tuen päätös. Lista alku-loppu päivämääräpareja.")
  @Tooltip("Mahdollisen tehostetun tuen päätösten alkamis- ja päättymispäivät. Voi olla useita erillisiä jaksoja.")
  @OksaUri("tmpOKSAID511", "tehostettu tuki")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @RedundantData
  tehostetunTuenPäätökset: Option[List[Aikajakso]] = None,
  @Description("Opiskelu ulkomailla alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole ulkomailla.")
  @Tooltip("Tieto opiskelusta ulkomailla alkamis- ja päättymispäivineen. Rahoituksen laskennassa käytettävä tieto.")
  @Deprecated("Käytä korvaavaa kenttää Ulkomaanjaksot")
  ulkomailla: Option[Aikajakso] = None,
  @Description("Ulkomaan opintojaksot alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole ulkomailla.")
  @Tooltip("Ulkomaan opiontojaksot alkamis- ja päättymispäivineen. Rahoituksen laskennassa käytettävä tieto.")
  ulkomaanjaksot: Option[List[Aikajakso]] = None,
  @Description("Opiskelija on vuosiluokkiin sitomattomassa opetuksessa (true/false).")
  @Tooltip("Onko opiskelija vuosiluokkiin sitomattomassa opetuksessa.")
  @DefaultValue(None)
  @Title("Vuosiluokkiin sitomaton opetus")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @RedundantData
  vuosiluokkiinSitoutumatonOpetus: Option[Boolean] = None,
  @Description("Onko oppija muu kuin vaikeimmin kehitysvammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, onko oppija muu kuin vaikeimmin kehitysvammainen (alku- ja loppupäivämäärät). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @RedundantData
  vammainen: Option[List[Aikajakso]] = None,
  @Description("Onko oppija vaikeasti kehitysvammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, onko oppija vaikeasti kehitysvammainen (alku- ja loppupäivämäärät). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @RedundantData
  vaikeastiVammainen: Option[List[Aikajakso]] = None,
  @Description("Opiskelijalla on majoitusetu. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, jos opiskelijalla on majoitusetu (alku- ja loppupäivämäärät). Rahoituksen laskennassa käytettävä tieto.")
  majoitusetu: Option[Aikajakso] = None,
  @Description("Opiskelijalla on oikeus maksuttomaan asuntolapaikkaan. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, jos opiskelijalla on oikeus maksuttomaan asuntolapaikkaan (alku- ja loppupäivämäärät).")
  @RedundantData
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Aikajakso] = None,
  @Description("Sisäoppilaitosmuotoinen majoitus, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Mahdollisen sisäoppilaitosmuotoisen majoituksen aloituspäivä ja loppupäivä. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None,
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot
  with SisäoppilaitosmainenMajoitus
  with OikeusmaksuttomaanAsuntolapaikkaanAikajaksona
  with Majoitusetuinen
  with Vammainen
  with VaikeastiVammainen
  with MaksuttomuusTieto
  with Ulkomaanaikajaksollinen

trait AikuistenPerusopetuksenPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with MonikielinenSuoritus with Suorituskielellinen

case class AikuistenPerusopetuksenOppimääränSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava koulutus ja koulutuksen opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: AikuistenPerusopetus,
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
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenOppiaineenSuoritus]] = None,
  @Tooltip("Mahdolliset todistuksella näkyvät lisätiedot.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("aikuistenperusopetuksenoppimaara", koodistoUri = "suorituksentyyppi")
) extends AikuistenPerusopetuksenPäätasonSuoritus with PerusopetuksenOppimääränSuoritus with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

@Description("Aikuisten perusopetuksen tunnistetiedot")
case class AikuistenPerusopetus(
 perusteenDiaarinumero: Option[String],
 @KoodistoKoodiarvo("201101")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201101", koodistoUri = "koulutus"),
 koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Perusopetus

@Description("Perusopetuksen oppiaineen suoritus osana aikuisten perusopetuksen oppimäärän suoritusta")
case class AikuistenPerusopetuksenOppiaineenSuoritus(
  koulutusmoduuli: AikuistenPerusopetuksenOppiaine,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Title("Kurssit")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenoppiaine", koodistoUri = "suorituksentyyppi"),
  suoritustapa: Option[Koodistokoodiviite] = None
) extends PerusopetuksenOppiaineenSuoritus with Vahvistukseton with MahdollisestiSuorituskielellinen with SuoritustapanaMahdollisestiErityinenTutkinto

trait AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine extends KoulutusmoduuliValinnainenLaajuus
trait AikuistenPerusopetuksenOppiaine extends PerusopetuksenOppiaine with AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine {
  @Tooltip("Oppiaineen laajuus kursseina.")
  def laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa]
}

trait AikuistenPerusopetuksenKoodistostaLöytyväOppiaine extends AikuistenPerusopetuksenOppiaine with YleissivistavaOppiaine {
  def kuvaus: Option[LocalizedString]
}

case class AikuistenPerusopetuksenPaikallinenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None,
  kuvaus: LocalizedString,
  perusteenDiaarinumero: Option[String] = None,
  @DefaultValue(false)
  pakollinen: Boolean = false
) extends AikuistenPerusopetuksenOppiaine with PerusopetuksenPaikallinenOppiaine

case class MuuAikuistenPerusopetuksenOppiaine(
  @KoodistoKoodiarvo("OPA")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None,
  kuvaus: Option[LocalizedString] = None
) extends MuuPerusopetuksenOppiaine with AikuistenPerusopetuksenKoodistostaLöytyväOppiaine

case class AikuistenPerusopetuksenUskonto(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None,
  kuvaus: Option[LocalizedString] = None,
  uskonnonOppimäärä: Option[Koodistokoodiviite] = None
) extends PerusopetuksenUskonto with AikuistenPerusopetuksenKoodistostaLöytyväOppiaine

case class AikuistenPerusopetuksenÄidinkieliJaKirjallisuus(
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None,
  kuvaus: Option[LocalizedString] = None
) extends PerusopetuksenÄidinkieliJaKirjallisuus with AikuistenPerusopetuksenKoodistostaLöytyväOppiaine

case class AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli(
  @KoodistoKoodiarvo("A1")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  tunniste: Koodistokoodiviite,
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None,
  kuvaus: Option[LocalizedString] = None
) extends PerusopetuksenVierasTaiToinenKotimainenKieli with AikuistenPerusopetuksenKoodistostaLöytyväOppiaine

case class AikuistenPerusopetuksenKurssinSuoritus(
  @Description("Aikuisten perusopetuksen kurssin tunnistetiedot")
  koulutusmoduuli: AikuistenPerusopetuksenKurssi,
  @FlattenInUI
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenkurssi", koodistoUri = "suorituksentyyppi"),
  @Description("Jos kurssi on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot.")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None
) extends MahdollisestiSuorituskielellinen with AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus

sealed trait AikuistenPerusopetuksenKurssi extends KoulutusmoduuliValinnainenLaajuus {
  def laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa]
}

case class PaikallinenAikuistenPerusopetuksenKurssi(
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None
) extends AikuistenPerusopetuksenKurssi with PaikallinenKoulutusmoduuliKuvauksella with StorablePreference {
  def kuvaus: LocalizedString = LocalizedString.empty
}

@Title("Aikuisten perusopetuksen opetussuunnitelman 2015 mukainen kurssi")
@OnlyWhen("../../../../../koulutusmoduuli/perusteenDiaarinumero","19/011/2015")
@OnlyWhen("../../../koulutusmoduuli/perusteenDiaarinumero", "19/011/2015")
@OnlyWhen("../..", None) // allow standalone deserialization
case class ValtakunnallinenAikuistenPerusopetuksenKurssi2015(
  @KoodistoUri("aikuistenperusopetuksenkurssit2015")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None
) extends AikuistenPerusopetuksenKurssi with KoodistostaLöytyväKoulutusmoduuli

@Title("Aikuisten perusopetuksen päättövaiheen opetussuunnitelman 2017 mukainen kurssi")
@OnlyWhen("../../../../../koulutusmoduuli/perusteenDiaarinumero", "OPH-1280-2017")
@OnlyWhen("../../../koulutusmoduuli/perusteenDiaarinumero", "OPH-1280-2017")
@OnlyWhen("../..", None) // allow standalone deserialization
case class ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017(
  @KoodistoUri("aikuistenperusopetuksenpaattovaiheenkurssit2017")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None
) extends AikuistenPerusopetuksenKurssi with KoodistostaLöytyväKoulutusmoduuli

trait AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus extends KurssinSuoritus with MahdollisestiTunnustettu

case class AikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset.")
  @Tooltip("Päättötodistukseen liittyvät oppiaineen suoritukset.")
  @Title("Oppiaine")
  @FlattenInUI
  koulutusmoduuli: AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  override val vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @Title("Kurssit")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends AikuistenPerusopetuksenPäätasonSuoritus with OppiaineenSuoritus with Todistus with SuoritustavallinenPerusopetuksenSuoritus with PerusopetuksenOppiaineenOppimääränSuoritus

@Description("Ks. tarkemmin perusopetuksen opiskeluoikeuden tilat: [wiki](https://wiki.eduuni.fi/pages/viewpage.action?pageId=190613191#Aikuistenperusopetuksenopiskeluoikeudenl%C3%A4sn%C3%A4olotiedotjaopiskeluoikeudenrahoitusmuodontiedot-Opiskeluoikeudentilat/L%C3%A4sn%C3%A4olojaopintojenlopettaminen)")
case class AikuistenPerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[AikuistenPerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class AikuistenPerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("6")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiLaajaOpiskeluoikeusjakso
