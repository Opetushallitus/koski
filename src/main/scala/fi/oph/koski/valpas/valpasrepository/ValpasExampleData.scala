package fi.oph.koski.valpas.valpasrepository

import java.time.LocalDate.{of => date}

import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{Koodistokoodiviite, OidOrganisaatio}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import java.time.{LocalDate, LocalDateTime}

case class ValpasKuntailmoitusFixture(
  kuntailmoitus: ValpasKuntailmoitusLaajatTiedotJaOppijaOid,
  aikaleimaOverride: Option[LocalDate] = None
)

object ValpasExampleData {
  val foo: (String, Int) = ("a", 1)

  def ilmoitukset: Seq[ValpasKuntailmoitusFixture] = Seq(
    ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid = ValpasMockOppijat.valmistunutYsiluokkalainenJollaIlmoitus.oid,
      kuntailmoitus = oppilaitoksenIlmoitusKaikillaTiedoilla
    )),
    ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid = ValpasMockOppijat.kasiinAstiToisessaKoulussaOllutJollaIlmoitus.oid,
      kuntailmoitus = oppilaitoksenIlmoitusMinimitiedoilla
    )),
    ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid =  ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus.oid,
      kuntailmoitus = oppilaitoksenIlmoitusKaikillaTiedoilla
    )),
    ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid =  ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster.oid,
      kuntailmoitus = oppilaitoksenIlmoitusKaikillaTiedoilla
    )),
      ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid =  ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusKolmas.oid,
      kuntailmoitus = oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta
    )),
    ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid =  ValpasMockOppijat.lukionAloittanutJollaVanhaIlmoitus.oid,
      kuntailmoitus = oppilaitoksenIlmoitusKaikillaTiedoilla
    ),
      Some(date(2021, 6, 15))
    ),
    ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid =  ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia.oid,
      kuntailmoitus = oppilaitoksenIlmoitusKaikillaTiedoilla
    ),
      Some(date(2021, 6, 15))
    ),
    ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid =  ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia.oid,
      kuntailmoitus = oppilaitoksenIlmoitusKaikillaTiedoilla.copy(kunta = helsinginKaupunki)
    ),
      Some(date(2021, 9, 15))
    ),
    ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid =  ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia.oid,
      kuntailmoitus = oppilaitoksenIlmoitusKaikillaTiedoilla
    ),
      Some(date(2021, 9, 20))
    ),
    ValpasKuntailmoitusFixture(ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid =  ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia.oid,
      kuntailmoitus = oppilaitoksenIlmoitusKaikillaTiedoilla.copy(kunta = helsinginKaupunki)
    ),
      Some(date(2021, 11, 30))
    )
  )

  def ilmoitus = ValpasKuntailmoitusLaajatTiedot(
    id = None,
    kunta = pyhtäänKunta,
    aikaleima = Some(LocalDateTime.of(2021, 8, 15, 8, 0)),
    tekijä = ValpasKuntailmoituksenTekijäLaajatTiedot(
      organisaatio = jyväskylänNormaalikoulu,
      henkilö = Some(tekijäHenkilö(ValpasMockUsers.valpasJklNormaalikoulu))
    ),
    yhteydenottokieli = suomi,
    oppijanYhteystiedot = Some(ValpasKuntailmoituksenOppijanYhteystiedot(
      puhelinnumero = Some("0401234567"),
      email = Some("Veijo.Valpas@gmail.com"),
      lähiosoite = Some("Esimerkkikatu 123"),
      postinumero = Some("000000"),
      postitoimipaikka = Some("Pyhtää"),
      maa = Some(Koodistokoodiviite("246", "maatjavaltiot2"))
    )),
    hakenutMuualle = Some(false)
  )

  def oppilaitoksenIlmoitusKaikillaTiedoilla = ilmoitus

  def oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta =
    oppilaitoksenIlmoitusKaikillaTiedoilla.copy(
      tekijä = oppilaitoksenIlmoitusKaikillaTiedoilla.tekijä.copy(
        organisaatio = aapajoenPeruskoulu
      )
    )

  def oppilaitoksenIlmoitusMinimitiedoilla = ValpasKuntailmoitusLaajatTiedot(
    id = None,
    kunta = OidOrganisaatio(
      oid = MockOrganisaatiot.pyhtäänKunta,
      nimi = None,
      kotipaikka = None
    ),
    aikaleima = None,
    tekijä = ValpasKuntailmoituksenTekijäLaajatTiedot(
      organisaatio = OidOrganisaatio(
        oid = MockOrganisaatiot.jyväskylänNormaalikoulu,
        nimi = None
      ),
      henkilö = Some(ValpasKuntailmoituksenTekijäHenkilö(
        oid = tekijäHenkilö(ValpasMockUsers.valpasJklNormaalikoulu).oid,
        etunimet = Some("Valpas"),
        sukunimi = Some("Käyttäjä"),
        kutsumanimi = None,
        email = None,
        puhelinnumero = None
      ))
    ),
    yhteydenottokieli = None,
    oppijanYhteystiedot = Some(ValpasKuntailmoituksenOppijanYhteystiedot(
      puhelinnumero = None,
      email = None,
      lähiosoite = None,
      postinumero = None,
      postitoimipaikka = None,
      maa = None
    )),
    hakenutMuualle = Some(false)
  )

  lazy val suomi = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli"))
  lazy val ruotsi = Some(Koodistokoodiviite("SV", Some("ruotsi"), "kieli"))

  lazy val pyhtäänKunta = OidOrganisaatio(
    oid = MockOrganisaatiot.pyhtäänKunta,
    nimi = Some("Pyhtään kunta"),
    kotipaikka = Some(Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "624", nimi = Some("Pyhtää")))
  )

  lazy val helsinginKaupunki = OidOrganisaatio(
    oid = MockOrganisaatiot.helsinginKaupunki,
    nimi = Some("Helsingin kaupunki"),
    kotipaikka = Some(Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "091", nimi = Some("Helsinki")))
  )

  lazy val jyväskylänNormaalikoulu = OidOrganisaatio(
    oid = MockOrganisaatiot.jyväskylänNormaalikoulu,
    nimi = Some("Jyväskylän normaalikoulu")
  )

  lazy val aapajoenPeruskoulu = OidOrganisaatio(
    oid = MockOrganisaatiot.aapajoenKoulu,
    nimi = Some("Aapajoen koulu")
  )

  def tekijäHenkilö(mockUser: ValpasMockUser) = ValpasKuntailmoituksenTekijäHenkilö(
    oid = Some(mockUser.oid),
    etunimet = Some(s"${mockUser.firstname} Mestari"),
    sukunimi = Some(mockUser.lastname),
    kutsumanimi = Some(mockUser.firstname),
    email = Some(s"${mockUser.firstname}@gmail.com"),
    puhelinnumero = Some("040 123 4567")
  )
}
