package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.{concat, finnish}
import fi.oph.scalaschema.annotation.{MinItems, Description}

@Description("Perusopetuksen opiskeluoikeus")
case class PerusopetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int]  = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid] = None,
  @Description("Opiskeluoikeuden tavoite-tieto kertoo sen, suorittaako perusopetuksen koko oppimäärää vai yksittäisen oppiaineen oppimäärää")
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tavoite: Koodistokoodiviite,
  suoritukset: List[PerusopetuksenPäätasonSuoritus],
  tila: PerusopetuksenOpiskeluoikeudenTila,
  läsnäolotiedot: Option[YleisetLäsnäolotiedot] = None,
  @KoodistoKoodiarvo("perusopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetus", "opiskeluoikeudentyyppi"),
  lisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

case class PerusopetuksenOpiskeluoikeudenLisätiedot(
  @Description("""Perusopetuksen aloittamisesta lykkäys (true/false). Oppilas saa luvan  aloittaa perusopetuksen myöhemmin.""")
  @OksaUri("tmpOKSAID242", "koulunkäynnin aloittamisen lykkääminen")
  perusopetuksenAloittamistaLykätty: Boolean = false,
  @Description("""Perusopetuksen aikastaminen (true/false). Oppilas aloittaa ennen oppivelvollisuusikää.""")
  aloittanutEnnenOppivelvollisuutta: Boolean = false,
  @Description("""Pidennetty oppivelvollisuus alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että oppilaalla ei ole pidennettyä oppivelvollisuutta.""")
  @OksaUri("tmpOKSAID517", "pidennetty oppivelvollisuus")
  pidennettyOppivelvollisuus: Option[Päätösjakso] = None,
  @KoodistoUri("perusopetuksentukimuoto")
  @Description("""Oppilaan saamat laissa säädetyt tukimuodot""")
  tukimuodot: Option[List[Koodistokoodiviite]] = None,
  @Description("""Erityisen tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty.""")
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  erityisenTuenPäätös: Option[ErityisenTuenPäätös] = None,
  @Description("""Tehostetun tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty.""")
  @OksaUri("tmpOKSAID511", "tehostettu tuki")
  tehostetunTuenPäätös: Option[Päätösjakso] = None,
  @Description("""Opiskelu joustavassa perusopetuksessa (JOPO) alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole joustavassa perusopetuksessa.""")
  @OksaUri("tmpOKSAID453", "joustava perusopetus")
  joustavaPerusopetus: Option[Päätösjakso] = None,
  @Description("""Opiskelu kotiopetuksessa huoltajan päätöksestä, alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole kotiopetuksessa.""")
  kotiopetus: Option[Päätösjakso] = None,
  @Description("""Opiskelu ulkomailla huoltajan ilmoituksesta, alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole ulkomailla.""")
  ulkomailla: Option[Päätösjakso] = None,
  @Description("""Oppilas on vuosiluokkiin sitoutumattomassa opetuksessa (true/false)""")
  vuosiluokkiinSitoutumatonOpetus: Boolean = false,
  @Description("""Tieto siitä, että oppilas on sisäoppilaismaisessa majoituksessa, alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole sisäoppilasmaisessa majoituksessa.""")
  sisäoppilaitosmainenMajoitus: Option[Päätösjakso] = None,
  @Description("""Tieto siitä, että oppilas saa majoitusetua, alkamis- ja päättymispäivineen. Jos oppilaalla on sisäoppilaitosmuotoinen majoitus, hän ei voi saada majoitusetua. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas saa majoitusetua.""")
  majoitusetu: Option[Päätösjakso] = None,
  @Description("""Tieto siitä, että oppilas saa kuljetusetua, alkamis- ja päättymispäivineen. Jos oppilas on sisäoppilaitosmuotoisessa majoituksessa tai saa majoitusetua, hän ei voi saada kuljetusetua. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas saa kuljetusetua.""")
  kuljetusetu: Option[Päätösjakso] = None,
  @Description("""Tieto siitä, että oppilaalla on oikeus maksuttomaan asuntolapaikkaan, alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilaalla ole oikeutta maksuttomaan asuntolapaikkaan.""")
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Päätösjakso] = None,
  @Description("""Tieto siitä, että oppilas on osallistunut perusopetuslain (628/1998, 8a) mukaiseen aamu- tai iltapäivätoimintaan, alkamis- ja päättymispäivämäärineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole osallistunut aamu- tai iltapäivätoimintaan.""")
  aamuTaiIltapäivätoiminta: Option[Päätösjakso] = None
)

case class ErityisenTuenPäätös(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @Description("""Oppilas opiskelee toiminta-alueittain (true/false).
                 | Toiminta-alueittain opiskelussa oppilaalla on yksilöllistetty oppimäärä ja opetus järjestetty toiminta-alueittain.
                 | Tuolloin oppilaalla on aina erityisen tuen päätös.
                 | Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.
                 | huomautuksena: toiminta-alue arviointeineen on kuvattu oppiaineen suorituksessa.""")
  opiskeleeToimintaAlueittain: Boolean = false,
  @Description("""Suorittaako erityisoppilas koulutusta omassa erityisryhmässään vai inklusiivisesti opetuksen mukana""")
  @OksaUri("tmpOKSAID444", "opetusryhmä")
  erityisryhmässä: Boolean
)


trait PerusopetuksenPäätasonSuoritus extends Suoritus with Toimipisteellinen

@Description("Perusopetuksen vuosiluokan suoritus. Nämä suoritukset näkyvät lukuvuositodistuksella.")
case class PerusopetuksenVuosiluokanSuoritus(
  @Description("Luokan tunniste, esimerkiksi 9C")
  luokka: String,
  override val alkamispäivä: Option[LocalDate],
  tila: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite],
  jääLuokalle: Boolean = false,
  käyttäytymisenArvio: Option[PerusopetuksenOppiaineenArviointi] = None,
  @Description("Luokka-aste ilmaistaan perusopetuksenluokkaaste-koodistolla")
  koulutusmoduuli: PerusopetuksenLuokkaAste,
  @KoodistoKoodiarvo("perusopetuksenvuosiluokka")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenvuosiluokka", koodistoUri = "suorituksentyyppi"),
  @Description("Vuosiluokan suoritukseen liittyvät oppiaineen suoritukset")
  override val osasuoritukset: Option[List[OppiaineenTaiToiminta_AlueenSuoritus]] = None
) extends PerusopetuksenPäätasonSuoritus {
  override def arviointi = None
}

@Description("Perusopetuksen koko oppimäärän suoritus. Nämä suoritukset näkyvät päättötodistuksella.")
case class PerusopetuksenOppimääränSuoritus(
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  koulutusmoduuli: Perusopetus,
  @KoodistoUri("perusopetuksenoppimaara")
  @Description("Tieto siitä, suoritetaanko perusopetusta nuorten vai aikuisten oppimäärän mukaisesti")
  oppimäärä: Koodistokoodiviite,
  @KoodistoUri("perusopetuksensuoritustapa")
  @Description("Tieto siitä, suoritetaanko perusopetusta normaalina koulutuksena vai erityisenä tutkintona")
  suoritustapa: Koodistokoodiviite,
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppimaara", koodistoUri = "suorituksentyyppi"),
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  override val osasuoritukset: Option[List[OppiaineenTaiToiminta_AlueenSuoritus]] = None
) extends PerusopetuksenPäätasonSuoritus {
  def arviointi: Option[List[KoodistostaLöytyväArviointi]] = None
}

@Description("Perusopetuksen yksittäisen oppiaineen oppimäärän suoritus erillisenä kokonaisuutena")
case class PerusopetuksenOppiaineenOppimääränSuoritus(
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  override val vahvistus: Option[Henkilövahvistus] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: PerusopetuksenOppiaine,
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None
) extends PerusopetuksenPäätasonSuoritus with OppiaineenSuoritus

sealed trait OppiaineenTaiToiminta_AlueenSuoritus extends Suoritus

@Description("Perusopetuksen oppiaineen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta")
case class PerusopetuksenOppiaineenSuoritus(
  koulutusmoduuli: PerusopetuksenOppiaine,
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @Description("Tieto siitä, onko oppiaineen oppimäärä yksilöllistetty (true/false)")
  yksilöllistettyOppimäärä: Boolean = false,
  @KoodistoKoodiarvo("perusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenoppiaine", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None
) extends OppiaineenSuoritus with OppiaineenTaiToiminta_AlueenSuoritus {
  override def tarvitseeVahvistuksen = false
}

@Description("Perusopetuksen toiminta-alueen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta. Suoritukset voidaan kirjata oppiaineiden sijaan toiminta-alueittain, jos opiskelijalle on tehty erityisen tuen päätös.")
case class PerusopetuksenToiminta_AlueenSuoritus(
  koulutusmoduuli: PerusopetuksenToiminta_Alue,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  @KoodistoKoodiarvo("perusopetuksentoimintaalue")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksentoimintaalue", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None
) extends OppiaineenTaiToiminta_AlueenSuoritus {
  def vahvistus: Option[Vahvistus] = None
}

trait PerusopetuksenOppiaineenArviointi extends YleissivistävänKoulutuksenArviointi

@Description("Numeerinen arviointi asteikolla 4 (hylätty) - 10 (erinomainen)")
case class NumeerinenPerusopetuksenOppiaineenArviointi(
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  arvosana: Koodistokoodiviite,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: Option[LocalDate]
) extends PerusopetuksenOppiaineenArviointi {
  def arviointipäivä = päivä
}

@Description("Sanallisessa arvioinnissa suorituksen hyväksymisen ilmaisuun käytetään koodiarvoja S (suoritettu) ja H (hylätty). Koodiarvon lisäksi voidaan liittää sanallinen arviointi vapaana tekstinä kuvaus-kenttään.")
case class SanallinenPerusopetuksenOppiaineenArviointi(
  @KoodistoKoodiarvo("S")
  @KoodistoKoodiarvo("H")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  kuvaus: Option[LocalizedString],
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: Option[LocalDate] = None
) extends PerusopetuksenOppiaineenArviointi with SanallinenArviointi {
  def arviointipäivä = päivä
}

object PerusopetuksenOppiaineenArviointi {
  def apply(arvosana: String, kuvaus: Option[LocalizedString] = None) = new SanallinenPerusopetuksenOppiaineenArviointi(
    arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"),
    päivä = None,
    kuvaus = kuvaus
  )
  def apply(arvosana: Int) = new NumeerinenPerusopetuksenOppiaineenArviointi(
    arvosana = Koodistokoodiviite(koodiarvo = arvosana.toString, koodistoUri = "arviointiasteikkoyleissivistava"),
    päivä = None
  )
}

@Description("Perusopetuksen toiminta-alueen tunnistetiedot")
case class PerusopetuksenToiminta_Alue(
  @KoodistoUri("perusopetuksentoimintaalue")
  tunniste: Koodistokoodiviite
) extends KoodistostaLöytyväKoulutusmoduuli {
  def laajuus = None
}

@Description("Perusopetuksen tunnistetiedot")
case class Perusopetus(
 perusteenDiaarinumero: Option[String],
 @KoodistoKoodiarvo("201101")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201101", koodistoUri = "koulutus")
) extends Koulutus with EPerusteistaLöytyväKoulutusmoduuli {
  override def laajuus = None
  override def isTutkinto = true
}

@Description("Perusopetuksen luokka-asteen (1-9) tunnistetiedot")
case class PerusopetuksenLuokkaAste(
 @Description("Luokka-asteen tunniste (1-9)")
 @KoodistoUri("perusopetuksenluokkaaste")
 tunniste: Koodistokoodiviite,
 perusteenDiaarinumero: Option[String]
) extends KoodistostaLöytyväKoulutusmoduuli with EPerusteistaLöytyväKoulutusmoduuli {
  override def laajuus = None
  override def isTutkinto = false
}

object PerusopetuksenLuokkaAste {
  def apply(luokkaAste: Int): PerusopetuksenLuokkaAste = PerusopetuksenLuokkaAste(Koodistokoodiviite(luokkaAste.toString, "perusopetuksenluokkaaste"), None)
}

@Description("Perusopetuksen oppiaineen tunnistetiedot")
trait PerusopetuksenOppiaine extends YleissivistavaOppiaine {
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

case class MuuPeruskoulunOppiaine(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenOppiaine

@Description("Oppiaineena äidinkieli ja kirjallisuus")
case class PeruskoulunAidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenOppiaine


@Description("Oppiaineena vieras tai toinen kotimainen kieli")
case class PeruskoulunVierasTaiToinenKotimainenKieli(
  @KoodistoKoodiarvo("A1")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenOppiaine {
  override def description = concat(nimi, ", ", kieli)
}

case class LaajuusVuosiviikkotunneissa(
  arvo: Float,
  @KoodistoKoodiarvo("3")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "3", nimi = Some(finnish("Vuosiviikkotuntia")), koodistoUri = "opintojenlaajuusyksikko")
) extends Laajuus

case class PerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[PerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class PerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("erotettumaaraajaksi")
  @KoodistoKoodiarvo("luvatonpoissaolo")
  @KoodistoKoodiarvo("valmistunut")
  @KoodistoKoodiarvo("eronnut")
  @KoodistoKoodiarvo("keskeyttanytmaaraajaksi")
  tila: Koodistokoodiviite
) extends KoskiOpiskeluoikeusjakso