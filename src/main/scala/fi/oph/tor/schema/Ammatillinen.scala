package fi.oph.tor.schema

import java.time.LocalDate
import fi.oph.tor.localization.LocalizedString
import fi.oph.scalaschema.annotation.{MaxItems, MinItems, Description}
import fi.oph.tor.localization.LocalizedString.finnish

@Description("Ammatillisen koulutuksen opiskeluoikeus")
case class AmmatillinenOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  @MinItems(1) @MaxItems(1)
  suoritukset: List[AmmatillisenTutkinnonSuoritus],
  hojks: Option[Hojks],
  @Description("Opiskelijan suorituksen tavoite-tieto kertoo sen, suorittaako opiskelija tutkintotavoitteista koulutusta (koko tutkintoa) vai tutkinnon osa tavoitteista koulutusta (tutkinnon osaa)")
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("ammatillinentutkinto")
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  tavoite: Koodistokoodiviite,
  tila: Option[AmmatillinenOpiskeluoikeudenTila],
  läsnäolotiedot: Option[AmmatillisenLäsnäolotiedot],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinenkoulutus", "opiskeluoikeudentyyppi")
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
}

case class AmmatillinenOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[AmmatillinenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class AmmatillinenOpiskeluoikeusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("opiskeluoikeudentila")
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoUri("opintojenrahoitus")
  opintojenRahoitus: Option[Koodistokoodiviite]
) extends Opiskeluoikeusjakso


case class AmmatillisenTutkinnonSuoritus(
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
  suoritustapa: Option[AmmatillisenTutkinnonSuoritustapa] = None,
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
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None,
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkinto", "suorituksentyyppi")
) extends Suoritus

case class AmmatillisenTutkinnonOsanSuoritus(
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
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
  toimipiste: Option[OrganisaatioWithOid],
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  vahvistus: Option[Vahvistus] = None,
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends Suoritus

@Description("Tutkintoon johtava koulutus")
case class AmmatillinenTutkintoKoulutus(
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: Koodistokoodiviite,
 perusteenDiaarinumero: Option[String]
) extends KoodistostaLöytyväKoulutusmoduuli with EPerusteistaLöytyväKoulutusmoduuli {
  override def laajuus = None
  override def isTutkinto = true
}

sealed trait AmmatillisenTutkinnonOsa extends Koulutusmoduuli {
  def laajuus: Option[LaajuusOsaamispisteissä]
}

@Description("Opetussuunnitelmaan kuuluva tutkinnon osa")
case class OpsTutkinnonosa(
  @Description("Tutkinnon osan kansallinen koodi")
  @KoodistoUri("tutkinnonosat")
  tunniste: Koodistokoodiviite,
  @Description("Onko pakollinen osa tutkinnossa")
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä],
  paikallinenKoodi: Option[Paikallinenkoodi] = None,
  kuvaus: Option[LocalizedString] = None
) extends AmmatillisenTutkinnonOsa with KoodistostaLöytyväKoulutusmoduuli

@Description("Paikallinen tutkinnon osa")
case class PaikallinenTutkinnonosa(
  tunniste: Paikallinenkoodi,
  kuvaus: LocalizedString,
  @Description("Onko pakollinen osa tutkinnossa")
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsa with PaikallinenKoulutusmoduuli

case class AmmatillisenTutkinnonOsanLisätieto(
  @Description("Lisätiedon tyyppi kooditettuna")
  @KoodistoUri("ammatillisentutkinnonosanlisatieto")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus siinä muodossa, kuin se näytetään todistuksella")
  kuvaus: LocalizedString
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
  kuvaus: LocalizedString,
  suorituspaikka: NäytönSuorituspaikka,
  arviointi: Option[NäytönArviointi]
)

@Description("Ammatillisen näytön suorituspaikka")
case class NäytönSuorituspaikka(
  @Description("Suorituspaikan tyyppi 1-numeroisella koodilla")
  @KoodistoUri("ammatillisennaytonsuorituspaikka")
  tunniste: Koodistokoodiviite,
  @Description("Vapaamuotoinen suorituspaikan kuvaus")
  kuvaus: LocalizedString
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


case class AmmatillisenTutkinnonSuoritustapa(
  @KoodistoUri("ammatillisentutkinnonsuoritustapa")
  tunniste: Koodistokoodiviite
)

trait Järjestämismuoto {
  def tunniste: Koodistokoodiviite
}

@Description("Järjestämismuoto ilman lisätietoja")
case class DefaultJärjestämismuoto(
  @KoodistoUri("jarjestamismuoto")
  tunniste: Koodistokoodiviite
) extends Järjestämismuoto

case class Hyväksiluku(
  @Description("Aiemman, korvaavan suorituksen kuvaus")
  osaaminen: Koulutusmoduuli, // TODO: tähän ehkä mieluummin Suoritus, koska se on "standalone"-entiteetti (löytyy diskriminaattori)
  @Description("Osaamisen tunnustamisen kautta saatavan tutkinnon osan suorituksen selite")
  @OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
  selite: Option[LocalizedString]
)

@Description("Henkilökohtainen opetuksen järjestämistä koskeva suunnitelma, https://fi.wikipedia.org/wiki/HOJKS")
@OksaUri("tmpOKSAID228", "erityisopiskelija")
case class Hojks(
  hojksTehty: Boolean,
  @KoodistoUri("opetusryhma")
  opetusryhmä: Option[Koodistokoodiviite]
)

case class LaajuusOsaamispisteissä(
  arvo: Float,
  @KoodistoKoodiarvo("6")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("6", Some(finnish("Osaamispistettä")), "opintojenlaajuusyksikko")
) extends Laajuus

case class AmmatillisenLäsnäolotiedot(
  läsnäolojaksot: List[AmmatillinenLäsnäolojakso]
) extends Läsnäolotiedot

case class AmmatillinenLäsnäolojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("lasnaolotila")
  tila: Koodistokoodiviite
) extends Läsnäolojakso