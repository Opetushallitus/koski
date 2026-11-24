package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, MapOidOrganisaatioToOppilaitos}
import fi.oph.scalaschema.annotation._

import java.time.{LocalDate, LocalDateTime}

// TODO TOR-747: Luonnos IBO:n myöntämän IB-tutkinnon (IB Diploma) tietomallista. Nykyinen IBOpiskeluoikeus (ibtutkinto)
// kuvaa oppilaitoksen antamaa IB-koulutusta; tämä opiskeluoikeus kuvaa IBO:n myöntämää tutkintoa ja täytetään IBIS-tulostiedostosta
// (ks. fi.oph.koski.ibis). Koodiarvot ibdiploma, ibdiplomanoppiaine ja ibdiplomancore sekä koodisto ibdiplomaresult ovat
// työnimiä, koska ibtutkinto on jo käytössä sekä opiskeluoikeuden että suorituksen tyyppinä.

@Title("IB Diploma -opiskeluoikeus")
@Description("IBO:n myöntämän IB-tutkinnon (IB Diploma) opiskeluoikeus. Tiedot tuodaan IBO:n IBIS-järjestelmän tulostiedostosta.")
case class IBDiplomaOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  // IBO on koulutustoimija, joka on myös diploman oppilaitos.
  @MapOidOrganisaatioToOppilaitos
  oppilaitos: Option[Oppilaitos] = None,
  koulutustoimija: Option[Koulutustoimija] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  tila: IBDiplomaOpiskeluoikeudenTila,
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[IBDiplomanSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ibdiploma.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.ibdiploma,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
  lähdejärjestelmäkytkentäPurettu: Option[LähdejärjestelmäkytkennänPurkaminen] = None,
  @Description("Opiskelijan tunniste IBO:n järjestelmässä")
  iboTunniste: String,
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def lisätiedot: Option[OpiskeluoikeudenLisätiedot] = None
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

case class IBDiplomaOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[IBDiplomaOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

// TODO TOR-747: Vain myönnetyt diplomat tuodaan, joten ainoa varsinainen tila on valmistunut (vrt. ylioppilastutkinto).
// mitatoity tarvitaan virheellisen tuonnin korjaamiseen.
case class IBDiplomaOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("valmistunut")
  @KoodistoKoodiarvo("mitatoity")
  tila: Koodistokoodiviite,
) extends KoskiOpiskeluoikeusjakso

@Title("IB Diploman suoritus")
case class IBDiplomanSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: IBTutkinto = IBTutkinto(),
  toimipiste: OrganisaatioWithOid,
  // TODO TOR-747: IBIS-tiedostossa ei ole todistuksen myöntämispäivää (vain LAST CHANGE DATE), joten vahvistuspäivä annetaan
  // tuonnin yhteydessä.
  vahvistus: Option[Päivämäärävahvistus] = None,
  @Description("Tutkintokerta, jossa diploma myönnettiin")
  examinationSession: IBExaminationSession,
  @Description("Diploman kokonaispistemäärä")
  @MinValue(0)
  @MaxValue(45)
  totalPoints: Option[Int] = None,
  @Description("Oppiaineiden Extended Essay ja Theory of Knowledge yhdistetty arvosana")
  @MinValue(0)
  @MaxValue(3)
  combinedGrade: Option[Int] = None,
  @Description("Diploman tulos")
  @KoodistoUri("ibdiplomaresult")
  result: Koodistokoodiviite,
  @KoodistoKoodiarvo("ibdiploma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibdiploma", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[IBDiplomanOsasuoritus]] = None
) extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Arvioinniton

@Title("IB-tutkintokerta")
@Description("IB-tutkintokerta (examination session), joita on vuosittain kaksi: toukokuu ja marraskuu")
case class IBExaminationSession(
  year: Int,
  @MinValue(1)
  @MaxValue(12)
  month: Int,
)

trait IBDiplomanOsasuoritus extends Vahvistukseton with DuplikaatitSallittu {
  @Description("Tutkintokerta, jossa tulos on saatu")
  def examinationSession: IBExaminationSession
}

@Title("IB Diploman oppiaineen suoritus")
case class IBDiplomanOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBDiplomanOppiaine,
  examinationSession: IBExaminationSession,
  arviointi: Option[List[IBDiplomanOppiaineenArviointi]] = None,
  @Description("Oppiaine ei kuulu diploman vaatimuksiin eikä vaikuta kokonaispistemäärään (IBIS ADDITIONAL SUBJECT)")
  additionalSubject: Boolean = false,
  @KoodistoKoodiarvo("ibdiplomanoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibdiplomanoppiaine", "suorituksentyyppi")
) extends IBDiplomanOsasuoritus

@Title("IB Diploman core-suoritus")
case class IBDiplomanCoreSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBDiplomanCoreOppiaine,
  examinationSession: IBExaminationSession,
  arviointi: Option[List[IBDiplomanCoreArviointi]] = None,
  @KoodistoKoodiarvo("ibdiplomancore")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibdiplomancore", "suorituksentyyppi")
) extends IBDiplomanOsasuoritus

trait IBDiplomanOppiaine extends IBOppiaine with Laajuudeton {
  // Valinnainen, koska extended essayn aineella ei ole tasoa (vrt. IBTaso).
  @Description("Oppiaineen taso: Higher Level (HL) tai Standard Level (SL)")
  @KoodistoUri("oppiaineentasoib")
  def taso: Option[Koodistokoodiviite]
}

@Title("Muu IB Diploman oppiaine")
case class IBDiplomanMuuOppiaine(
  tunniste: Koodistokoodiviite,
  taso: Option[Koodistokoodiviite]
) extends IBDiplomanOppiaine with MuuOppiaineIB

@Title("IB Diploman kielioppiaine")
case class IBDiplomanKieliOppiaine(
  tunniste: Koodistokoodiviite,
  taso: Option[Koodistokoodiviite],
  @Discriminator
  kieli: Koodistokoodiviite
) extends IBDiplomanOppiaine with KieliOppiaineIB

trait IBDiplomanCoreOppiaine extends IBOppiaine with Laajuudeton

@Title("IB Diploman Theory of Knowledge")
case class IBDiplomanTheoryOfKnowledge(
  @Description("Oppiaineen Theory of Knowledge tunniste")
  @KoodistoKoodiarvo("TOK")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("TOK", "oppiaineetib")
) extends IBDiplomanCoreOppiaine

@Title("IB Diploman Extended Essay")
case class IBDiplomanExtendedEssay(
  @Description("Oppiaineen Extended Essay tunniste")
  @KoodistoKoodiarvo("EE")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("EE", "oppiaineetib"),
  @Description("Oppiaine, josta extended essay on kirjoitettu")
  aine: Option[IBDiplomanOppiaine] = None
) extends IBDiplomanCoreOppiaine

// TODO TOR-747: IBIS merkitsee arvosanan puuttumisen koodilla N (no grade issued) tai P (pending), joita arviointiasteikkoib ei
// sisällä. Myönnetyn diploman oppiaineilla ne ovat harvinaisia, ja konvertteri jättää tällaiset oppiaineet toistaiseksi pois.
@Title("IB Diploman oppiaineen arviointi")
case class IBDiplomanOppiaineenArviointi(
  @KoodistoUri("arviointiasteikkoib")
  arvosana: Koodistokoodiviite,
  @Description("Arviointipäivämäärä")
  päivä: Option[LocalDate] = None
) extends IBArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
}

@Title("IB Diploman core-arviointi")
case class IBDiplomanCoreArviointi(
  arvosana: Koodistokoodiviite,
  @Description("Arviointipäivämäärä")
  päivä: Option[LocalDate] = None
) extends CoreRequirementsArvionti
