package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{DefaultValue, Description, MinItems, Title}

@Description("Lukion opiskeluoikeus")
case class LukionOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  @Description("Opiskelijan opiskeluoikeuden arvioitu päättymispäivä joko koko lukiokoulutuksen oppimäärätavoitteisessa koulutuksessa tai oppiaineen oppimäärätavoitteisessa koulutuksessa")
  arvioituPäättymispäivä: Option[LocalDate] = None,
  tila: LukionOpiskeluoikeudenTila,
  lisätiedot: Option[LukionOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[LukionPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.lukiokoulutus,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
  @Description("Tieto siitä, onko lukion oppimäärän suorittamiseen vaadittavat opinnot suoritettu. Jos lukion oppimäärän suoritus on vahvistettu, lukion oppimäärän suorittamiseen vaadittavat opinnot tulkitaan automaattisesti suoritetuksi, jolloin tähän kenttään tallennetaan automaattisesti arvo 'true'.")
  oppimääräSuoritettu: Option[Boolean] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä joko koko lukiokoulutuksen oppimäärätavoitteisessa koulutuksessa tai oppiaineen oppimäärätavoitteisessa koulutuksessa")
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))

  def on2015Opiskeluoikeus: Boolean = suoritukset.exists({
    case _:LukionPäätasonSuoritus2015 => true
    case _ => false
  })

  def isOppimääräSuoritettu = oppimääräSuoritettu.getOrElse(false)
}

@Description("Lukion opiskeluoikeuden lisätiedot")
case class LukionOpiskeluoikeudenLisätiedot(
  @Description("Opiskeluajan pidennetty päättymispäivä (true/false). Lukion oppimäärä tulee suorittaa enintään neljässä vuodessa, jollei opiskelijalle perustellusta syystä myönnetä suoritusaikaan pidennystä (lukiolaki 21.8.1998/629 24 §)")
  @DefaultValue(false)
  pidennettyPäättymispäivä: Boolean = false,
  @Description("Opiskelija on ulkomainen vaihto-opiskelija Suomessa (true/false). Rahoituksen laskennassa käytettävä tieto.")
  @Title("Ulkomainen vaihto-opiskelija.")
  @DefaultValue(false)
  ulkomainenVaihtoopiskelija: Boolean = false,
  @Description("Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei opiskelija opiskele aikuisten lukiokoulutuksessa alle 18-vuotiaana")
  @Title("Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @RedundantData
  alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy: Option[LocalizedString] = None,
  @Description("Yksityisopiskelija aikuisten lukiokoulutuksessa (true/false). Rahoituksen laskennassa käytettävä tieto.")
  @DefaultValue(None)
  @RedundantData
  yksityisopiskelija: Option[Boolean] = None,
  erityisenKoulutustehtävänJaksot: Option[List[ErityisenKoulutustehtävänJakso]] = None,
  @Description("Rahoituksen laskennassa käytettävä tieto.")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @Description("Tieto onko oppijalla maksuton majoitus. Rahoituksen laskennassa käytettävä tieto.")
  @DefaultValue(None)
  @RedundantData
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Boolean] = None,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  @Description("Onko opiskelija sisöoppilaitosmaisessa majoituksessa. Rahoituksen laskennassa käytettävä tieto.")
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None,
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot
  with ErityisenKoulutustehtävänJaksollinen
  with Ulkomaanjaksollinen
  with SisäoppilaitosmainenMajoitus
  with UlkomainenVaihtoopiskelija
  with MaksuttomuusTieto
  with OikeusmaksuttomaanAsuntolapaikkaanBooleanina

trait LukionPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Suorituskielellinen

@Description("Lukiokoulutuksen tunnistetiedot")
case class LukionOppimäärä(
 @KoodistoKoodiarvo("309902")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("309902", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String],
 koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Tutkinto with Laajuudeton

trait LukionOppimääränPäätasonOsasuoritus extends Suoritus

case class LukionOppiaineenArviointi(
  @Description("Oppiaineen suorituksen arvosana on kokonaisarvosana oppiaineelle")
  arvosana: Koodistokoodiviite,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: Option[LocalDate]
) extends YleissivistävänKoulutuksenArviointi {
  def arviointipäivä = päivä
}

object LukionOppiaineenArviointi {
  def apply(arvosana: String, päivä: Option[LocalDate] = None) = new LukionOppiaineenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), päivä)
}

trait LukionArviointi extends ArviointiPäivämäärällä

case class NumeerinenLukionArviointi(
  arvosana: Koodistokoodiviite,
  päivä: LocalDate
) extends LukionArviointi with NumeerinenYleissivistävänKoulutuksenArviointi

case class SanallinenLukionArviointi(
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends LukionArviointi with SanallinenYleissivistävänKoulutuksenArviointi

@Description("Ks. tarkemmin lukion ja IB-tutkinnon opiskeluoikeuden tilat: [wiki](https://wiki.eduuni.fi/display/OPHPALV/1.2.+Lukion+opiskeluoikeuden+tilajaksot+ja+opintojen+rahoitusmuodon+ilmaiseminen+tilajaksossa#id-1.2.Lukionopiskeluoikeudentilajaksotjaopintojenrahoitusmuodonilmaiseminentilajaksossa-Opiskeluoikeudentilat)")
case class LukionOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[LukionOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class LukionOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus. Mikäli kyseessä on kaksoitutkintoa suorittava opiskelija, jonka rahoituksen saa ammatillinen oppilaitos, tulee käyttää arvoa 6: Muuta kautta rahoitettu. Muussa tapauksessa käytetään arvoa 1: Valtionosuusrahoitteinen koulutus.")
  @KoodistoUri("opintojenrahoitus")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("6")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiOpiskeluoikeusjakso

@Description("Lukion/IB-lukion oppiaineen tunnistetiedot")
trait LukionOppiaine extends Koulutusmoduuli with Valinnaisuus with Diaarinumerollinen {
  @Title("Oppiaine")
  def tunniste: KoodiViite
}

trait LukionÄidinkieliJaKirjallisuus extends LukionOppiaine with Äidinkieli

trait LukionOppimääränSuoritus
