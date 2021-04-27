package fi.oph.koski.valpas.valpasrepository

import java.time.LocalDateTime

import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{Koodistokoodiviite, OidOrganisaatio}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}

object ValpasExampleData {

  def ilmoitus = ValpasKuntailmoitusLaajatTiedot(
    id = None,
    kunta = pyhtäänKunta,
    aikaleima = Some(LocalDateTime.of(2021, 8, 15, 8, 0)),
    tekijä = ValpasKuntailmoituksenTekijäLaajatTiedot(
      organisaatio = jyväskylänNormaalikoulu,
      henkilö = tekijäHenkilö(ValpasMockUsers.valpasJklNormaalikoulu)
    ),
    yhteydenottokieli = suomi,
    oppijanYhteystiedot = Some(ValpasKuntailmoituksenOppijanYhteystiedot(
      puhelinnumero = Some("0401234567"),
      email = Some("Veijo.Valpas@gmail.com"),
      lähiosoite = Some("Esimerkkikatu 123"),
      postinumero = Some("000000"),
      postitoimipaikka = Some("Pyhtää"),
      maa = Some("Finland")
    )),
    hakenutUlkomaille = Some(false)
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
        oid = None,
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
    hakenutUlkomaille = None
  )

  lazy val suomi = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli"))
  lazy val ruotsi = Some(Koodistokoodiviite("SV", Some("ruotsi"), "kieli"))

  lazy val pyhtäänKunta = OidOrganisaatio(
    oid = MockOrganisaatiot.pyhtäänKunta,
    nimi = Some("Pyhtään kunta"),
    kotipaikka = Some(Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "624", nimi = Some("Pyhtää")))
  )

  lazy val jyväskylänNormaalikoulu = OidOrganisaatio(
    oid = MockOrganisaatiot.jyväskylänNormaalikoulu,
    nimi = Some("Jyväskylän normaalikoulu")
  )

  def tekijäHenkilö(mockUser: ValpasMockUser) = Some(ValpasKuntailmoituksenTekijäHenkilö(
    oid = Some(mockUser.oid),
    etunimet = Some(s"${mockUser.firstname} Mestari"),
    sukunimi = Some(mockUser.lastname),
    kutsumanimi = Some(mockUser.firstname),
    email = Some(s"${mockUser.firstname}@gmail.com"),
    puhelinnumero = Some("040 123 4567")
  ))
}
