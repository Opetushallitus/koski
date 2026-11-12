package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation._

import java.time.{LocalDate, LocalDateTime}

// TODO TOR-747: Luonnos IBO:n myöntämän IB-tutkinnon (IB Diploma) tietomallista. Nykyinen IBOpiskeluoikeus (ibtutkinto)
// kuvaa oppilaitoksen antamaa IB-koulutusta; tämä opiskeluoikeus kuvaa IBO:n myöntämää tutkintoa ja täytetään IBIS-tulostiedostosta
// (ks. fi.oph.koski.ibis). Koodiarvot ibdiploma, ibdiplomanoppiaine ja ibdiplomancore sekä koodisto ibdiplomantulos ovat
// työnimiä, koska ibtutkinto on jo käytössä sekä opiskeluoikeuden että suorituksen tyyppinä.

@Title("IB Diploma -opiskeluoikeus")
@Description("IBO:n myöntämän IB-tutkinnon (IB Diploma) opiskeluoikeus. Tiedot tuodaan IBO:n IBIS-järjestelmän tulostiedostosta.")
case class IBDiplomaOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  // Oppilaitos kopioidaan siitä IB-koulutuksen tai International schoolin opiskeluoikeudesta, johon diploma on yhdistetty.
  // IBO ei ole organisaatiopalvelussa, joten YTR:n tapaan tutkintolautakuntaa ei voi käyttää koulutustoimijana.
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
  // TODO TOR-747: Muoto on IBIS-dokumentaation mukaan 3 kirjainta + 3 numeroa. Lisätään @RegularExpression, kun oikea aineisto
  // vahvistaa kirjainkoon. Koulutuksen puolella (IBOpiskeluoikeus, InternationalSchoolOpiskeluoikeus) tunniste pysyy vapaaehtoisena.
  @Description("Opiskelijan tunniste IBO:n järjestelmässä (IBIS PERSONAL CODE)")
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
  // tuonnin yhteydessä. IBO ei ole organisaatiopalvelussa, joten myöntäjäorganisaationa käytetään oppilaitosta.
  vahvistus: Option[Päivämäärävahvistus] = None,
  @Description("Tutkintokerta, jossa diploma myönnettiin")
  tutkintokerta: IBTutkintokerta,
  @Description("Diploman kokonaispistemäärä (IBIS TOTAL POINTS)")
  @MinValue(0)
  @MaxValue(45)
  kokonaispistemäärä: Option[Int] = None,
  @Description("Extended essayn ja Theory of knowledgen yhteispisteet (IBIS EE/TOK POINTS)")
  @MinValue(0)
  @MaxValue(3)
  lisäpisteet: Option[Int] = None,
  @Description("Diploman tulos IBO:n tuloskoodin mukaan (IBIS RESULT CODE)")
  @KoodistoUri("ibdiplomantulos")
  tulos: Koodistokoodiviite,
  @KoodistoKoodiarvo("ibdiploma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibdiploma", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[IBDiplomanOsasuoritus]] = None
) extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Arvioinniton

@Title("IB-tutkintokerta")
@Description("IB-tutkintokerta (examination session), joita on vuosittain kaksi: toukokuu ja marraskuu")
case class IBTutkintokerta(
  @Description("Tutkintokerran tunniste muodossa VVVVK, jossa K on M (toukokuu) tai N (marraskuu)")
  @RegularExpression("^[0-9]{4}[MN]$")
  koodiarvo: String,
  vuosi: Int,
  kuukausi: LocalizedString
)

trait IBDiplomanOsasuoritus extends Vahvistukseton with DuplikaatitSallittu {
  @Description("Tutkintokerta, jossa tulos on saatu")
  def tutkintokerta: IBTutkintokerta
}

@Title("IB Diploman oppiaineen suoritus")
case class IBDiplomanOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBDiplomanOppiaine,
  tutkintokerta: IBTutkintokerta,
  arviointi: Option[List[IBDiplomanOppiaineenArviointi]] = None,
  @Description("Oppiaine ei kuulu diploman vaatimuksiin eikä vaikuta kokonaispistemäärään (IBIS ADDITIONAL SUBJECT)")
  additionalSubject: Boolean = false,
  @KoodistoKoodiarvo("ibdiplomanoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibdiplomanoppiaine", "suorituksentyyppi")
) extends IBDiplomanOsasuoritus

@Title("IB Diploman core-suoritus")
@Description("Extended essayn (EE) tai Theory of knowledgen (TOK) tulos")
case class IBDiplomanCoreSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBDiplomanCoreOppiaine,
  tutkintokerta: IBTutkintokerta,
  arviointi: Option[List[IBDiplomanCoreArviointi]] = None,
  @KoodistoKoodiarvo("ibdiplomancore")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibdiplomancore", "suorituksentyyppi")
) extends IBDiplomanOsasuoritus

trait IBDiplomanOppiaine extends IBOppiaine with Laajuudeton {
  @Description("Oppiaineen taso: Higher Level (HL) tai Standard Level (SL)")
  @KoodistoUri("oppiaineentasoib")
  def taso: Koodistokoodiviite
  // TODO TOR-747: IBO:n koodit vaihtuvat tutkintokerroittain (SUB cross-reference file); alkuperäinen koodi säilytetään jäljitettävyyden vuoksi.
  @Description("IBO:n kuusinumeroinen oppiainekoodi (IBIS SUBJECT CODE)")
  def iboOppiainekoodi: Option[String]
}

@Title("Muu IB Diploman oppiaine")
case class IBDiplomanMuuOppiaine(
  tunniste: Koodistokoodiviite,
  taso: Koodistokoodiviite,
  iboOppiainekoodi: Option[String] = None
) extends IBDiplomanOppiaine with MuuOppiaineIB

@Title("IB Diploman kielioppiaine")
case class IBDiplomanKieliOppiaine(
  tunniste: Koodistokoodiviite,
  taso: Koodistokoodiviite,
  @Discriminator
  kieli: Koodistokoodiviite,
  iboOppiainekoodi: Option[String] = None
) extends IBDiplomanOppiaine with KieliOppiaineIB

@Title("IB Diploman core-oppiaine")
case class IBDiplomanCoreOppiaine(
  @KoodistoKoodiarvo("EE")
  @KoodistoKoodiarvo("TOK")
  tunniste: Koodistokoodiviite,
  @Description("Oppiaine, josta extended essay on kirjoitettu")
  @KoodistoUri("oppiaineetib")
  aine: Option[Koodistokoodiviite] = None,
  @Description("IBO:n kuusinumeroinen oppiainekoodi (IBIS SUBJECT CODE)")
  iboOppiainekoodi: Option[String] = None
) extends IBOppiaine with Laajuudeton

// TODO TOR-747: arviointiasteikkoib ei sisällä IBIS-arvosanoja N (no grade) ja P (pending). Myönnetyn diploman oppiaineilla
// ne ovat harvinaisia, ja konvertteri jättää ne toistaiseksi pois.
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
