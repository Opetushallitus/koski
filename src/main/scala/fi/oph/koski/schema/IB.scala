package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.schema.LocalizedString.english
import fi.oph.koski.schema.annotation.{Deprecated, FlattenInUI, Hidden, KoodistoKoodiarvo, KoodistoUri, OksaUri}
import fi.oph.koski.util.DateOrdering.localDateOrdering
import fi.oph.scalaschema.annotation._

@Description("IB-tutkinnon opiskeluoikeus")
@Title("IB-tutkinnon opiskeluoikeus")
case class IBOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  tila: LukionOpiskeluoikeudenTila,
  @MaxItems(2)
  suoritukset: List[IBPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.ibtutkinto,
  override val lisätiedot: Option[LukionOpiskeluoikeudenLisätiedot] = None,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
  lähdejärjestelmäkytkentäPurettu: Option[LähdejärjestelmäkytkennänPurkaminen] = None,
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))

  def withLisääPuuttuvaMaksuttomuustieto = {
    lazy val uudellaMaksuttomuustiedolla = this.copy(
      lisätiedot = Some(
        this.lisätiedot.getOrElse(LukionOpiskeluoikeudenLisätiedot())
          .copy(
            maksuttomuus = Some(List(Maksuttomuus(this.alkamispäivä.getOrElse(throw new InternalError("Alkupäivä puuttuu")), None, true)))
          )
      )
    )

    lisätiedot match {
      case None =>
        uudellaMaksuttomuustiedolla
      case Some(lt) if lt.maksuttomuus.toSeq.flatten.isEmpty =>
        uudellaMaksuttomuustiedolla
      case _ => this
    }
  }

}

@Title("IB-tutkinnon suoritus")
case class IBTutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: IBTutkinto = IBTutkinto(),
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[IBTutkinnonOppiaineenSuoritus]],
  @Deprecated("Theory of Knowledge tallennetaan osasuorituksiin 1.8.2025 tai myöhemmin alkaneille IB-opinnoille")
  theoryOfKnowledge: Option[IBTheoryOfKnowledgeSuoritus],
  @Deprecated("Extended Essay tallennetaan osasuorituksiin 1.8.2025 tai myöhemmin alkaneille IB-opinnoille")
  extendedEssay: Option[IBExtendedEssaySuoritus],
  @Deprecated("CAS tallennetaan osasuorituksiin 1.8.2025 tai myöhemmin alkaneille IB-opinnoille")
  creativityActionService: Option[IBCASSuoritus],
  @Description("Tutkinnon lisäpisteet. Max 3 pistettä yhteensä")
  @KoodistoUri("arviointiasteikkolisapisteetib")
  @Deprecated("Lisäpisteitä ei tallennetaa 1.8.2025 tai myöhemmin alkaneille IB-opinnoille")
  lisäpisteet: Option[Koodistokoodiviite] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibtutkinto", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends IBPäätasonSuoritus with Todistus with OppivelvollisuudenSuorittamiseenKelpaava with Ryhmällinen

trait IBPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Arvioinniton with Suorituskielellinen
trait PreIBSuorituksenOsasuoritus2015 extends Suoritus

@Title("IB-tutkinto")
@Description("IB-tutkinnon tunnistetiedot")
case class IBTutkinto(
  @KoodistoKoodiarvo("301102")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("301102", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Koulutus with Laajuudeton with Tutkinto

trait IBSuoritus extends Vahvistukseton with MahdollisestiSuorituskielellinen

trait IBTutkinnonOppiaineenSuoritus extends IBSuoritus

@Title("IB-oppiaineen suoritus")
case class IBOppiaineenSuoritus(
  @Description("IB-lukion oppiaineen tunnistetiedot")
  @Title("Oppiaine")
  koulutusmoduuli: IBAineRyhmäOppiaine,
  arviointi: Option[List[IBOppiaineenArviointi]] = None,
  predictedArviointi: Option[List[IBOppiaineenPredictedArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[IBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiaine", koodistoUri = "suorituksentyyppi")
) extends IBTutkinnonOppiaineenSuoritus {
  override def ryhmittelytekijä: Option[String] = koulutusmoduuli.taso.map(_.koodiarvo)
  override def parasArviointi: Option[Arviointi] = {
    arviointi
      .map(_.sortBy(_.arviointipäivä))
      .fold(sortedPredictedArviointi)(_.map(IBOppiaineenPredictedArviointi.apply))
      .reduceOption(Arviointi.korkeampiArviointi)
  }
  def sortedPredictedArviointi = predictedArviointi.toList.flatten.sortBy(_.arviointipäivä)
}

@Title("IB-lukion DP Core -suoritus")
case class IBDBCoreSuoritus(
  @Description("IB-lukion oppiaineen tunnistetiedot")
  @Title("Oppiaine")
  koulutusmoduuli: IBDPCoreOppiaine,
  arviointi: Option[List[IBOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[IBKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("ibcore")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ibcore", koodistoUri = "suorituksentyyppi")
) extends IBTutkinnonOppiaineenSuoritus

@Description("Theory of Knowledge-suorituksen tiedot")
@Title("IB Theory of Knowledge -suoritus")
@Deprecated("Theory of Knowledge tullaan 1.8.2025 alkaen tallentamaan osasuorituksiin")
case class IBTheoryOfKnowledgeSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaineTheoryOfKnowledge,
  arviointi: Option[List[IBCoreRequirementsArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[IBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiainetok")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiainetok", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

@Description("CAS-suorituksen tiedot")
@Title("IB CAS -suoritus")
@Deprecated("CAS tullaan 1.8.2025 alkaen tallentamaan osasuorituksiin")
case class IBCASSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaineCAS,
  arviointi: Option[List[IBCASOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("iboppiainecas")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiainecas", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

@Description("Extended Essay-suorituksen tiedot")
@Title("IB Extended Essay -suoritus")
@Deprecated("Extended Essay tullaan 1.8.2025 alkaen tallentamaan osasuorituksiin")
case class IBExtendedEssaySuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaineExtendedEssay,
  arviointi: Option[List[IBCoreRequirementsArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("iboppiaineee")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiaineee", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

@Title("IB-oppiaineen arviointi")
case class IBOppiaineenArviointi(
  @Description("Onko arvoitu arvosana vai ei, jos ei niin tarkoittaa IBOn vahvistamaa arvosanaa")
  @Deprecated("Käytä IB-oppiaineen suorituksen predictedArviointi-kenttää")
  @Hidden
  predicted: Option[Boolean] = None,
  @KoodistoUri("arviointiasteikkoib")
  arvosana: Koodistokoodiviite,
  @Description("Effort-arvosana, kuvaa opiskelijan tunnollisuutta, aktiivisuutta ja yritteliäisyyttä. Arvosteluasteikko: A = very good, B = good, C = needs improvement")
  @KoodistoUri("effortasteikkoib")
  @Deprecated("Effort-arvosanaa ei enää tallenneta KOSKI-tietovarantoon")
  effort: Option[Koodistokoodiviite] = None,
  @Description("Arviointipäivämäärä")
  päivä: Option[LocalDate]
) extends IBArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
}

object IBOppiaineenArviointi {
  def apply(predicted: IBOppiaineenPredictedArviointi): IBOppiaineenArviointi = IBOppiaineenArviointi(
    predicted = Some(true),
    arvosana = predicted.arvosana,
    päivä = predicted.päivä,
  )
}

@Title("IB-oppiaineen predicted-arviointi")
case class IBOppiaineenPredictedArviointi(
  @KoodistoUri("arviointiasteikkoib")
  arvosana: Koodistokoodiviite,
  @Description("Arviointipäivämäärä")
  päivä: Option[LocalDate]
) extends IBArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
}

object IBOppiaineenPredictedArviointi {
  def apply(arviointi: IBOppiaineenArviointi): IBOppiaineenPredictedArviointi = IBOppiaineenPredictedArviointi(
    arvosana = arviointi.arvosana,
    päivä = arviointi.päivä,
  )
}

@Title("IB CAS -oppiaineen arviointi")
@OnlyWhen("../tyyppi/koodiarvo","iboppiainecas")
case class IBCASOppiaineenArviointi(
  @Deprecated("Tietoa ei kirjata IB CAS -oppiaineen arviointiin")
  predicted: Option[Boolean] = None,
  @KoodistoUri("arviointiasteikkoib")
  @KoodistoKoodiarvo("S")
  arvosana: Koodistokoodiviite,
  @Description("Effort-arvosana, kuvaa opiskelijan tunnollisuutta, aktiivisuutta ja yritteliäisyyttä. Arvosteluasteikko: A = very good, B = good, C = needs improvement")
  @KoodistoUri("effortasteikkoib")
  effort: Option[Koodistokoodiviite] = None,
  @Description("Arviointipäivämäärä")
  päivä: Option[LocalDate]
) extends IBArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
}

@Title("IB-kurssin suoritus")
case class IBKurssinSuoritus(
  koulutusmoduuli: IBKurssi,
  arviointi: Option[List[IBKurssinArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ibkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ibkurssi", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus with KurssinSuoritus

@Title("IB-kurssi")
@Description("IB-lukion kurssin tunnistetiedot")
case class IBKurssi(
  kuvaus: LocalizedString,
  @OksaUri("tmpOKSAID873", "kurssi")
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  @Discriminator
  pakollinen: Boolean = true,
  @Description("1.8.2025 alkaen laajuus tallennetaan opintopisteissä. Sitä ennen alkaneisiin opiskeluoikeuksiin tallennetaan kursseina.")
  override val laajuus: Option[LaajuusOpintopisteissäTaiKursseissa]
) extends KoulutusmoduuliValinnainenLaajuus with Valinnaisuus with PreIBKurssi2015 with StorablePreference {
  def nimi: LocalizedString = tunniste.nimi
}

@Title("IB-kurssin arviointi")
case class IBKurssinArviointi(
  @KoodistoUri("arviointiasteikkoib")
  arvosana: Koodistokoodiviite,
  @Description("Effort-arvosana, kuvaa opiskelijan tunnollisuutta, aktiivisuutta ja yritteliäisyyttä. Arvosteluasteikko: A = very good, B = good, C = needs improvement")
  @KoodistoUri("effortasteikkoib")
  effort: Option[Koodistokoodiviite] = None,
  päivä: LocalDate
) extends IBArviointi with ArviointiPäivämäärällä

trait IBArviointi extends KoodistostaLöytyväArviointi {
  def arvosana: Koodistokoodiviite
  def arvioitsijat: Option[List[Arvioitsija]] = None
  override def arvosanaKirjaimin: LocalizedString = arvosana.nimi.getOrElse(english(arvosana.koodiarvo))
  def hyväksytty: Boolean = IBArviointi.hyväksytty(arvosana)
}
object IBArviointi {
  def hyväksytty(arvosana: Koodistokoodiviite): Boolean = arvosana.koodiarvo match {
    case "O" | "1" => false
    case _ => true
  }
}

trait CoreRequirementsArvionti extends IBArviointi {
  @KoodistoUri("arviointiasteikkocorerequirementsib")
  def arvosana: Koodistokoodiviite
  def päivä: Option[LocalDate]
  override def arviointipäivä: Option[LocalDate] = päivä
  override def hyväksytty: Boolean = CoreRequirementsArvionti.hyväksytty(arvosana)
}
object CoreRequirementsArvionti {
  def hyväksytty(arvosana: Koodistokoodiviite): Boolean = arvosana.koodiarvo match {
    case "f" => false
    case _ => true
  }
}

@Title("IB Core Requirements -arviointi")
case class IBCoreRequirementsArviointi(
  arvosana: Koodistokoodiviite,
  @Deprecated("Tietoa ei kirjata IB Core Requirements -arviointiin")
  predicted: Option[Boolean] = None,
  @Description("Arviointipäivämäärä")
  päivä: Option[LocalDate]
) extends CoreRequirementsArvionti

@Description("IB-lukion oppiaineen tunnistetiedot")
trait IBOppiaine extends KoodistostaLöytyväKoulutusmoduuliValinnainenLaajuus {
  @KoodistoUri("oppiaineetib")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
}

trait IBTaso {
  @KoodistoUri("oppiaineentasoib")
  def taso: Option[Koodistokoodiviite]
}

trait IBAineRyhmäOppiaine extends IBOppiaine with PreIBOppiaine2015 with IBTaso with Valinnaisuus {
  @KoodistoUri("aineryhmaib")
  def ryhmä: Koodistokoodiviite
}

trait KieliOppiaineIB extends IBOppiaine with Kieliaine {
  @KoodistoKoodiarvo("A")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B")
  @KoodistoKoodiarvo("AB")
  @KoodistoKoodiarvo("CLA")
  def tunniste: Koodistokoodiviite
  @KoodistoUri("kielivalikoima")
  def kieli: Koodistokoodiviite
  override def description: LocalizedString = kieliaineDescription
}

trait MuuOppiaineIB extends IBOppiaine {
  @KoodistoKoodiarvo("BIO")
  @KoodistoKoodiarvo("BU")
  @KoodistoKoodiarvo("CHE")
  @KoodistoKoodiarvo("DAN")
  @KoodistoKoodiarvo("DIS")
  @KoodistoKoodiarvo("ECO")
  @KoodistoKoodiarvo("FIL")
  @KoodistoKoodiarvo("GEO")
  @KoodistoKoodiarvo("HIS")
  @KoodistoKoodiarvo("MAT")
  @KoodistoKoodiarvo("MATFT")
  @KoodistoKoodiarvo("MATST")
  @KoodistoKoodiarvo("MUS")
  @KoodistoKoodiarvo("PHI")
  @KoodistoKoodiarvo("PHY")
  @KoodistoKoodiarvo("POL")
  @KoodistoKoodiarvo("PSY")
  @KoodistoKoodiarvo("REL")
  @KoodistoKoodiarvo("SOC")
  @KoodistoKoodiarvo("ESS")
  @KoodistoKoodiarvo("THE")
  @KoodistoKoodiarvo("VA")
  @KoodistoKoodiarvo("CS")
  @KoodistoKoodiarvo("LIT")
  @KoodistoKoodiarvo("INF")
  @KoodistoKoodiarvo("DES")
  @KoodistoKoodiarvo("SPO")
  @KoodistoKoodiarvo("MATAA")
  @KoodistoKoodiarvo("MATAI")
  def tunniste: Koodistokoodiviite
}

@Title("Muu IB-oppiaine")
case class IBOppiaineMuu(
  @Description("IB-lukion oppiaineen tunnistetiedot")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa] = None,
  @Description("Oppiaineen taso (Higher Level (HL) tai Standard Level (SL)")
  taso: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineen aineryhmä (1-6)")
  ryhmä: Koodistokoodiviite,
  pakollinen: Boolean = true
) extends IBAineRyhmäOppiaine with MuuOppiaineIB

@Title("IB-kielioppiaine")
case class IBOppiaineLanguage(
  @Description("IB-lukion kielioppiaineen tunnistetiedot")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa],
  @Description("Oppiaineen taso (Higher Level (HL) tai Standard Level (SL)")
  taso: Option[Koodistokoodiviite],
  @Discriminator
  @Description("Mikä kieli on kyseessä")
  kieli: Koodistokoodiviite,
  @Description("Oppiaineen aineryhmä (1-6)")
  ryhmä: Koodistokoodiviite,
  pakollinen: Boolean = true
) extends IBAineRyhmäOppiaine with KieliOppiaineIB

trait IBCoreElementOppiaine extends IBOppiaine with Valinnaisuus

@Title("IB-oppiaine CAS")
case class IBOppiaineCAS(
  @Description("Oppiaineen Creativity, activity, service tunniste")
  @KoodistoKoodiarvo("CAS")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "CAS", nimi = Some(english("Creativity, activity, service"))),
  laajuus: Option[LaajuusTunneissa] = None,
  pakollinen: Boolean = true
) extends IBCoreElementOppiaine

@Title("IB-oppiaine Theory of Knowledge")
case class IBOppiaineTheoryOfKnowledge(
  @Description("Oppiaineen Theory of Knowledge tunniste")
  @KoodistoKoodiarvo("TOK")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "TOK", nimi = Some(english("Theory of knowledge"))),
  pakollinen: Boolean = true
) extends IBCoreElementOppiaine with Laajuudeton

@Title("IB-oppiaine Extended Essay")
case class IBOppiaineExtendedEssay(
  @Description("Oppiaineen Extended Essay tunniste")
  @KoodistoKoodiarvo("EE")
  tunniste: Koodistokoodiviite =  Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "EE", nimi = Some(english("Extended essay"))),
  aine: IBAineRyhmäOppiaine,
  aihe: LocalizedString,
  pakollinen: Boolean = true
) extends IBCoreElementOppiaine with Laajuudeton

/**
 * IB DP Core -luokat
 */

trait IBDPCoreOppiaine extends IBOppiaine with Valinnaisuus

trait IBDPCoreAineRyhmäOppiaine extends IBOppiaine with IBTaso with Koulutusmoduuli {
  @KoodistoUri("aineryhmaib")
  def ryhmä: Koodistokoodiviite
}

@Title("IB-oppiaine CAS")
case class IBDPCoreOppiaineCAS(
  @Description("Oppiaineen Creativity, activity, service tunniste")
  @KoodistoKoodiarvo("CAS")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "CAS", nimi = Some(english("Creativity, activity, service"))),
  laajuus: Option[LaajuusOpintopisteissä] = None,
  pakollinen: Boolean = true
) extends IBDPCoreOppiaine

@Title("IB-oppiaine Theory of Knowledge")
case class IBDPCoreOppiaineTheoryOfKnowledge(
  @Description("Oppiaineen Theory of Knowledge tunniste")
  @KoodistoKoodiarvo("TOK")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "TOK", nimi = Some(english("Theory of knowledge"))),
  laajuus: Option[LaajuusOpintopisteissä] = None,
  pakollinen: Boolean = true
) extends IBDPCoreOppiaine

@Title("IB-oppiaine Extended Essay")
case class IBDPCoreOppiaineExtendedEssay(
  @Description("Oppiaineen Extended Essay tunniste")
  @KoodistoKoodiarvo("EE")
  tunniste: Koodistokoodiviite =  Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "EE", nimi = Some(english("Extended essay"))),
  aine: IBDPCoreAineRyhmäOppiaine,
  aihe: LocalizedString,
  pakollinen: Boolean = true
) extends IBDPCoreOppiaine with Laajuudeton {
  override def getLaajuus: Option[Laajuus] = aine.getLaajuus
}

@Title("Muu IB-oppiaine")
case class IBDPCoreOppiaineMuu(
  @Description("IB-lukion oppiaineen tunnistetiedot")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  @Description("Oppiaineen taso (Higher Level (HL) tai Standard Level (SL)")
  taso: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineen aineryhmä (1-6)")
  ryhmä: Koodistokoodiviite
) extends IBDPCoreAineRyhmäOppiaine with MuuOppiaineIB

@Title("IB-kielioppiaine")
case class IBDPCoreOppiaineLanguage(
  @Description("IB-lukion kielioppiaineen tunnistetiedot")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä],
  @Description("Oppiaineen taso (Higher Level (HL) tai Standard Level (SL)")
  taso: Option[Koodistokoodiviite],
  @Discriminator
  @Description("Mikä kieli on kyseessä")
  kieli: Koodistokoodiviite,
  @Description("Oppiaineen aineryhmä (1-6)")
  ryhmä: Koodistokoodiviite
) extends IBDPCoreAineRyhmäOppiaine with KieliOppiaineIB

