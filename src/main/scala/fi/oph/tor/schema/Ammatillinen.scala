package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.schema.generic.annotation.{MaxItems, MinItems, Description}

@Description("Ammatillisen koulutuksen opiskeluoikeus")
case class AmmatillinenOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  @MinItems(1) @MaxItems(1)
  suoritukset: List[AmmatillinenTutkintosuoritus],
  hojks: Option[Hojks],
  @Description("Opiskelijan suorituksen tavoite-tieto kertoo sen, suorittaako opiskelija tutkintotavoitteista koulutusta (koko tutkintoa) vai tutkinnon osa tavoitteista koulutusta (tutkinnon osaa)")
  @KoodistoUri("opintojentavoite")
  tavoite: Option[Koodistokoodiviite],
  opiskeluoikeudenTila: Option[OpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinenkoulutus", Some("Ammatillinen koulutus"), "opiskeluoikeudentyyppi", None)
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
}

case class AmmatillinenTutkintosuoritus(
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
  @KoodistoUri("tutkintonimikkeet")
  @OksaUri("tmpOKSAID588", "tutkintonimike")
  tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  @Description("Osaamisala")
  @KoodistoUri("osaamisala")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  osaamisala: Option[List[Koodistokoodiviite]] = None,
  @Description("Tutkinnon tai tutkinnon osan suoritustapa")
  @OksaUri("tmpOKSAID141", "ammatillisen koulutuksen järjestämistapa")
  suoritustapa: Option[Suoritustapa] = None,
  @Description("Koulutuksen järjestämismuoto")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuoto: Option[Järjestämismuoto] = None,

  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate],
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  vahvistus: Option[Vahvistus] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  override val osasuoritukset: Option[List[AmmatillinenTutkinnonosasuoritus]] = None,
  @KoodistoKoodiarvo("ammatillinentutkintosuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkintosuoritus", koodistoUri = "suorituksentyyppi")
) extends Suoritus

case class AmmatillinenTutkinnonosasuoritus(
  koulutusmoduuli: AmmatillinenTutkinnonOsa,
  hyväksiluku: Option[Hyväksiluku] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  näyttö: Option[Näyttö] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  @Description("Tutkinto, jonka rakenteeseen tutkinnon osa liittyy. Käytetään vain tapauksissa, joissa tutkinnon osa on poimittu toisesta tutkinnosta.")
  tutkinto: Option[AmmatillinenTutkintoKoulutus] = None,

  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate],
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  vahvistus: Option[Vahvistus] = None,
  @KoodistoKoodiarvo("ammatillinentutkinnonosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkinnonosasuoritus", koodistoUri = "suorituksentyyppi")
) extends Suoritus

@Description("Tutkintoon johtava koulutus")
case class AmmatillinenTutkintoKoulutus(
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: Koodistokoodiviite,
 @Description("Tutkinnon perusteen diaarinumero (pakollinen). Ks. ePerusteet-palvelu")
 perusteenDiaarinumero: Option[String]
) extends Koulutusmoduuli

sealed trait AmmatillinenTutkinnonOsa extends Koulutusmoduuli
  @Description("Opetussuunnitelmaan kuuluva tutkinnon osa")
  case class OpsTutkinnonosa(
    @Description("Tutkinnon osan kansallinen koodi")
    @KoodistoUri("tutkinnonosat")
    tunniste: Koodistokoodiviite,
    @Description("Onko pakollinen osa tutkinnossa")
    pakollinen: Boolean,
    override val laajuus: Option[Laajuus],
    paikallinenKoodi: Option[Paikallinenkoodi] = None,
    kuvaus: Option[String] = None
  ) extends AmmatillinenTutkinnonOsa

  @Description("Paikallinen tutkinnon osa")
  case class PaikallinenTutkinnonosa(
    tunniste: Paikallinenkoodi,
    kuvaus: String,
    @Description("Onko pakollinen osa tutkinnossa")
    pakollinen: Boolean,
    override val laajuus: Option[Laajuus]
  ) extends AmmatillinenTutkinnonOsa

case class AmmatillisenTutkinnonOsanLisätieto(
  @Description("Lisätiedon tyyppi kooditettuna")
  @KoodistoUri("ammatillisentutkinnonosanlisatieto")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus siinä muodossa, kuin se näytetään todistuksella")
  kuvaus: String
)

case class OppisopimuksellinenJärjestämismuoto(
  @KoodistoUri("jarjestamismuoto")
  @KoodistoKoodiarvo("20")
  tunniste: Koodistokoodiviite,
  oppisopimus: Oppisopimus
) extends Järjestämismuoto

case class AmmatillinenArviointi(
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends Arviointi

@Description("Näytön kuvaus")
case class Näyttö(
  @Description("Vapaamuotoinen kuvaus suoritetusta näytöstä")
  kuvaus: String,
  suorituspaikka: NäytönSuorituspaikka,
  arviointi: Option[NäytönArviointi]
)

@Description("Ammatillisen näytön suorituspaikka")
case class NäytönSuorituspaikka(
  @Description("Suorituspaikan tyyppi 1-numeroisella koodilla")
  @KoodistoUri("ammatillisennaytonsuorituspaikka")
  tunniste: Koodistokoodiviite,
  @Description("Vapaamuotoinen suorituspaikan kuvaus")
  kuvaus: String
)

case class NäytönArviointi (
  @Description("Näytön eri arviointikohteiden (Työprosessin hallinta jne) arvosanat.")
  arviointiKohteet: List[NäytönArviointikohde],
  @KoodistoUri("ammatillisennaytonarvioinnistapaattaneet")
  @Description("Arvioinnista päättäneet tahot, ilmaistuna 1-numeroisella koodilla")
  arvioinnistaPäättäneet: Koodistokoodiviite,
  @KoodistoUri("ammatillisennaytonarviointikeskusteluunosallistuneet")
  @Description("Arviointikeskusteluun osallistuneet tahot, ilmaistuna 1-numeroisella koodilla")
  arviointikeskusteluunOsallistuneet: Koodistokoodiviite
)

case class NäytönArviointikohde(
  @Description("Arviointikohteen tunniste")
  @KoodistoUri("ammatillisennaytonarviointikohde")
  tunniste: Koodistokoodiviite,
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  arvosana: Koodistokoodiviite
)

@Description("Oppisopimuksen tiedot")
case class Oppisopimus(
  työnantaja: Yritys
)