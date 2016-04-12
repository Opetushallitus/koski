package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.koodisto.KoodistoViite
import fi.oph.tor.log.Loggable
import fi.oph.tor.schema.generic.annotation.{Description, MinValue, ReadOnly, RegularExpression}


case class TorOppija(
  henkilö: Henkilö,
  @Description("Lista henkilön opiskeluoikeuksista. Sisältää vain ne opiskeluoikeudet, joihin käyttäjällä on oikeudet. Esimerkiksi ammatilliselle toimijalle ei välttämättä näy henkilön lukio-opintojen tietoja")
  opiskeluoikeudet: Seq[Opiskeluoikeus]
)

@Description("Henkilötiedot. Syötettäessä vaaditaan joko `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö")
sealed trait Henkilö

@Description("Täydet henkilötiedot. Tietoja haettaessa TOR:sta saadaan aina täydet henkilötiedot.")
case class TaydellisetHenkilötiedot(
  @Description("Yksilöivä tunniste (oppijanumero) Opintopolku-palvelussa")
  @OksaUri("tmpOKSAID760", "oppijanumero")
  @RegularExpression("""1\.2\.246\.562\.24\.\d{11}""")
  oid: String,
  hetu: String,
  etunimet:String,
  kutsumanimi: String,
  sukunimi: String,
  @Description("Opiskelijan äidinkieli")
  @KoodistoUri("kieli")
  äidinkieli: Option[Koodistokoodiviite],
  @Description("Opiskelijan kansalaisuudet")
  @KoodistoUri("maatjavaltiot2")
  kansalaisuus: Option[List[Koodistokoodiviite]]
) extends HenkilöWithOid with Henkilötiedot {
  def vainHenkilötiedot = UusiHenkilö(hetu, etunimet, kutsumanimi, sukunimi)
}

@Description("Henkilö, jonka oppijanumero ei ole tiedossa. Tietoja syötettäessä luodaan mahdollisesti uusi henkilö Henkilöpalveluun, jolloin henkilölle muodostuu oppijanumero")
case class UusiHenkilö(
  hetu: String,
  etunimet:String,
  kutsumanimi: String,
  sukunimi: String
) extends Henkilö with Henkilötiedot

@Description("Henkilö, jonka oid on tiedossa. Tietoja syötettäessä henkilö haetaan henkilöpalvelusta.")
case class OidHenkilö(
  @Description("Yksilöivä tunniste (oppijanumero) Opintopolku-palvelussa")
  @OksaUri("tmpOKSAID760", "oppijanumero")
  @RegularExpression("""1\.2\.246\.562\.24\.\d{11}""")
  oid: String
) extends HenkilöWithOid

trait Henkilötiedot {
  @Description("Suomalainen henkilötunnus")
  def hetu: String
  @Description("Henkilön kaikki etunimet. Esimerkiksi Sanna Katariina")
  def etunimet:String
  @Description("Kutsumanimi, oltava yksi etunimistä. Esimerkiksi etunimille \"Juha-Matti Petteri\" kelpaavat joko \"Juha-Matti\", \"Juha\", \"Matti\" tai \"Petteri\"")
  def kutsumanimi: String
  @Description("Henkilön sukunimi. Henkilön sukunimen etuliite tulee osana sukunimeä")
  def sukunimi: String
}

trait HenkilöWithOid extends Henkilö {
  def oid: String
}

object Henkilö {
  type Oid = String
  def withOid(oid: String) = OidHenkilö(oid)
  def apply(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String) = UusiHenkilö(hetu, etunimet, kutsumanimi, sukunimi)
}

trait Opiskeluoikeus extends Loggable {
  @Description("Opiskeluoikeden tyyppi, jolla erotellaan eri koulutusmuotoihin (peruskoulutus, lukio, ammatillinen...) liittyvät opiskeluoikeudet")
  @OksaUri("tmpOKSAID869", "koulutusmuoto (1)")
  @KoodistoUri("opiskeluoikeudentyyppi")
  def tyyppi: Koodistokoodiviite
  @Description("Opiskeluoikeuden uniikki tunniste, joka generoidaan TOR-järjestelmässä. Tietoja syötettäessä kenttä ei ole pakollinen. " +
    "Tietoja päivitettäessä TOR tunnistaa opiskeluoikeuden joko tämän id:n tai muiden kenttien (oppijaOid, organisaatio, diaarinumero) perusteella")
  def id: Option[Int]
  @Description("Versionumero, joka generoidaan TOR-järjestelmässä. Tietoja syötettäessä kenttä ei ole pakollinen. " +
    "Ensimmäinen tallennettu versio saa versionumeron 1, jonka jälkeen jokainen päivitys aiheuttaa versionumeron noston yhdellä. " +
    "Jos tietoja päivitettäessä käytetään versionumeroa, pitää sen täsmätä viimeisimpään tallennettuun versioon. " +
    "Tällä menettelyllä esimerkiksi käyttöliittymässä varmistetaan, ettei tehdä päivityksiä vanhentuneeseen dataan.")
  def versionumero: Option[Int]
  @Description("Lähdejärjestelmän tunniste ja opiskeluoikeuden tunniste lähdejärjestelmässä. " +
    "Käytetään silloin, kun opiskeluoikeus on tuotu TOR:iin tiedonsiirrolla ulkoisesta järjestelmästä, eli käytännössä oppilashallintojärjestelmästä.")
  def lähdejärjestelmänId: Option[LähdejärjestelmäId]
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  def alkamispäivä: Option[LocalDate]
  @Description("Opiskelijan opiskeluoikeuden arvioitu päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  def arvioituPäättymispäivä: Option[LocalDate]
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  def päättymispäivä: Option[LocalDate]
  @Description("Oppilaitos, jossa opinnot on suoritettu")
  def oppilaitos: Oppilaitos
  @Description("Koulutustoimija, käytännössä oppilaitoksen yliorganisaatio")
  @ReadOnly("Tiedon syötössä tietoa ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
  def koulutustoimija: Option[OrganisaatioWithOid]
  @Description("Opiskeluoikeuteen liittyvien (tutkinto-)suorituksien tiedot")
  def suoritukset: List[Suoritus]
  def opiskeluoikeudenTila: Option[OpiskeluoikeudenTila]
  def läsnäolotiedot: Option[Läsnäolotiedot]

  override def toString = id match {
    case None => "opiskeluoikeus"
    case Some(id) => "opiskeluoikeus " + id
  }

  def withIdAndVersion(id: Option[Int], versionumero: Option[Int]): Opiskeluoikeus
  def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid): Opiskeluoikeus
}

object Opiskeluoikeus {
  type Id = Int
  type Versionumero = Int
  val VERSIO_1 = 1
}

trait Suoritus {
  @Description("Suorituksen tyyppi")
  @KoodistoUri("suorituksentyyppi")
  def tyyppi: Koodistokoodiviite
  def koulutusmoduuli: Koulutusmoduuli
  @Description("Paikallinen tunniste suoritukselle. Tiedonsiirroissa tarpeellinen, jotta voidaan varmistaa päivitysten osuminen oikeaan suoritukseen")
  def paikallinenId: Option[String]
  def alkamispäivä: Option[LocalDate] = None
  @Description("Opintojen suorituskieli")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID309", "opintosuorituksen kieli")
  def suorituskieli: Option[Koodistokoodiviite]
  @Description("Suorituksen tila (KESKEN, VALMIS, KESKEYTYNYT)")
  @KoodistoUri("suorituksentila")
  def tila: Koodistokoodiviite
  @Description("Arviointi. Jos listalla useampi arviointi, tulkitaan myöhemmät arvioinnit arvosanan korotuksiksi. Jos aiempaa, esimerkiksi väärin kirjattua, arviota korjataan, ei listalle tule uutta arviota")
  def arviointi: Option[List[Arviointi]]
  @Description("Suorituksen virallinen vahvistus (päivämäärä, henkilöt). Vaaditaan silloin, kun suorituksen tila on VALMIS.")
  def vahvistus: Option[Vahvistus]
  def osasuoritukset: Option[List[Suoritus]] = None

  def osasuoritusLista: List[Suoritus] = osasuoritukset.toList.flatten
  def rekursiivisetOsasuoritukset: List[Suoritus] = {
    osasuoritusLista ++ osasuoritusLista.flatMap(_.rekursiivisetOsasuoritukset)
  }
}

trait Koulutusmoduuli {
  def tunniste: KoodiViite
  def laajuus: Option[Laajuus] = None
  def nimi: String // TODO: localize
}

trait KoodistostaLöytyväKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: Koodistokoodiviite
  def nimi = tunniste.nimi.getOrElse(tunniste.koodiarvo)
}

trait PaikallinenKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: Paikallinenkoodi
  def nimi = tunniste.nimi
}

trait Arviointi {
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
  def arvosana: Koodistokoodiviite
  @Description("Päivämäärä, jolloin arviointi on annettu")
  def päivä: Option[LocalDate]
  def arvioitsijat: Option[List[Arvioitsija]]
}

case class Arvioitsija(
  nimi: String
)

case class Vahvistus(
  @Description("Tutkinnon tai tutkinnonosan vahvistettu suorituspäivämäärä, eli päivämäärä jolloin suoritus on hyväksyttyä todennettua osaamista")
  päivä: LocalDate,
  myöntäjäOrganisaatio: Option[Organisaatio] = None,
  myöntäjäHenkilöt: Option[List[OrganisaatioHenkilö]] = None
)

case class OrganisaatioHenkilö(
  nimi: String,
  titteli: String,
  organisaatio: Organisaatio
)

case class Suoritustapa(
  @KoodistoUri("suoritustapa")
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
  selite: Option[String]
)

case class Läsnäolotiedot(
  @Description("Läsnä- ja poissaolojaksot päivämääräväleinä.")
  läsnäolojaksot: List[Läsnäolojakso]
)

trait Jakso {
  def alku: LocalDate
  def loppu: Option[LocalDate]
}

case class Läsnäolojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Läsnäolotila (läsnä, poissa...)")
  @KoodistoUri("lasnaolotila")
  tila: Koodistokoodiviite
) extends Jakso

case class OpiskeluoikeudenTila(
  @Description("Opiskeluoikeuden tilahistoria (aktiivinen, keskeyttänyt, päättynyt...) jaksoittain. Sisältää myös tiedon opintojen rahoituksesta jaksoittain.")
  opiskeluoikeusjaksot: List[Opiskeluoikeusjakso]
)

case class Opiskeluoikeusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Opiskeluoikeuden tila (aktiivinen, keskeyttänyt, päättynyt...)")
  @KoodistoUri("opiskeluoikeudentila")
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoUri("opintojenrahoitus")
  opintojenRahoitus: Option[Koodistokoodiviite]
) extends Jakso

case class Kunta(koodi: String, nimi: Option[String])

trait KoodiViite {
  def koodiarvo: String
  def koodistoUri: String
}

case class Koodistokoodiviite(
  @Description("Koodin tunniste koodistossa")
  koodiarvo: String,
  @Description("Koodin selväkielinen, kielistetty nimi")
  @ReadOnly("Tiedon syötössä kuvausta ei tarvita; kuvaus haetaan Koodistopalvelusta")
  nimi: Option[String],
  @Description("Käytetyn koodiston tunniste")
  koodistoUri: String,
  @Description("Käytetyn koodiston versio. Jos versiota ei määritellä, käytetään uusinta versiota")
  koodistoVersio: Option[Int] = None
) extends KoodiViite {
  override def toString = koodistoUri + "/" + koodiarvo
  def koodistoViite = koodistoVersio.map(KoodistoViite(koodistoUri, _))
}

object Koodistokoodiviite {
  def apply(koodiarvo: String, koodistoUri: String): Koodistokoodiviite = Koodistokoodiviite(koodiarvo, None, koodistoUri)
}

@Description("Henkilökohtainen opetuksen järjestämistä koskeva suunnitelma, https://fi.wikipedia.org/wiki/HOJKS")
@OksaUri("tmpOKSAID228", "erityisopiskelija")
case class Hojks(
  hojksTehty: Boolean,
  @KoodistoUri("opetusryhma")
  opetusryhmä: Option[Koodistokoodiviite]
)

@Description("Paikallinen, koulutustoimijan oma kooditus koulutukselle. Käytetään kansallisen koodiston puuttuessa")
case class Paikallinenkoodi(
  @Description("Koodin tunniste koodistossa")
  koodiarvo: String,
  @Description("Koodin selväkielinen nimi")
  nimi: String,
  @Description("Koodiston tunniste")
  koodistoUri: String
) extends KoodiViite

@Description("Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä")
case class Laajuus(
  @Description("Opintojen laajuuden arvo")
  @MinValue(0)
  arvo: Float,
  @Description("Opintojen laajuuden yksikkö")
  @KoodistoUri("opintojenlaajuusyksikko")
  yksikkö: Koodistokoodiviite
)

case class LähdejärjestelmäId(
  @Description("Opiskeluoikeuden paikallinen uniikki tunniste lähdejärjestelmässä. Tiedonsiirroissa tarpeellinen, jotta voidaan varmistaa päivitysten osuminen oikeaan opiskeluoikeuteen.")
  id: String,
  @Description("Lähdejärjestelmän yksilöivä tunniste. Tällä tunnistetaan järjestelmä, josta tiedot on tuotu TOR:iin. " +
    "Kullakin erillisellä tietojärjestelmäinstanssilla tulisi olla oma tunniste. " +
    "Jos siis oppilaitoksella on oma tietojärjestelmäinstanssi, tulee myös tällä instanssilla olla uniikki tunniste.")
  @KoodistoUri("lahdejarjestelma")
  lähdejärjestelmä: Koodistokoodiviite
)

@Description("Organisaatio. Voi olla Opintopolun organisaatiosta löytyvä oid:illinen organisaatio, y-tunnuksellinen yritys tai tutkintotoimikunta.")
sealed trait Organisaatio
  @Description("Opintopolun organisaatiopalvelusta löytyvä organisaatio. Esimerkiksi koulutustoimijat, oppilaitokset ja toimipisteet ovat tällaisia organisaatioita.")
  case class OidOrganisaatio(
    @Description("Organisaation tunniste Opintopolku-palvelussa")
    @RegularExpression("""1\.2\.246\.562\.10\.\d{11}""")
    oid: String,
    @Description("Organisaation (kielistetty) nimi")
    @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
    nimi: Option[String] = None
  ) extends OrganisaatioWithOid

  @Description("Opintopolun organisaatiopalvelusta löytyvä oppilaitos-tyyppinen organisaatio.")
  case class Oppilaitos(
     @Description("Organisaation tunniste Opintopolku-palvelussa")
     @RegularExpression("""1\.2\.246\.562\.10\.\d{11}""")
     oid: String,
     @Description("5-numeroinen oppilaitosnumero, esimerkiksi 00001")
     @ReadOnly("Tiedon syötössä oppilaitosnumeroa ei tarvita; numero haetaan Organisaatiopalvelusta")
     @KoodistoUri("oppilaitosnumero")
     oppilaitosnumero: Option[Koodistokoodiviite] = None,
     @Description("Organisaation (kielistetty) nimi")
     @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
     nimi: Option[String] = None
  ) extends OrganisaatioWithOid

  @Description("Yritys, jolla on y-tunnus")
  case class Yritys(
    nimi: String,
    @RegularExpression("\\d{7}-\\d")
    yTunnus: String
  ) extends Organisaatio

  @Description("Tutkintotoimikunta")
  case class Tutkintotoimikunta(
    nimi: String,
    @MinValue(1)
    tutkintotoimikunnanNumero: Int
  ) extends Organisaatio

trait OrganisaatioWithOid extends Organisaatio {
  @Description("Organisaation tunniste Opintopolku-palvelussa")
  def oid: String
  @Description("Organisaation (kielistetty) nimi")
  @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
  def nimi: Option[String]
}