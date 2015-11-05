package fi.oph.tor.schema

import java.time.LocalDate
import scala.annotation.meta._

case class TorOppija(
  henkilö: Henkilö,
  @Description("Lista henkilön opinto-oikeuksista. Sisältää vain ne opinto-oikeudet, joihin käyttäjällä on oikeudet. Esimerkiksi ammatilliselle toimijalle ei välttämättä näy henkilön lukio-opintojen tietoja.")
  opintoOikeudet: Seq[OpintoOikeus]
)

@Description("Henkilötiedot. Syötettäessä vaaditaan joko `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö.")
case class Henkilö(
  @Description("Yksilöivä tunniste Opintopolku-palvelussa")
  oid: Option[String],
  @Description("Suomalainen henkilötunnus")
  hetu: Option[String],
  etunimet: Option[String],
  @Description("Kutsumanimi, oltava yksi etunimistä. Esimerkiksi etunimille \"Juha-Matti Petteri\" kelpaavat joko \"Juha-Matti\", \"Juha\", \"Matti\" tai \"Petteri\".")
  kutsumanimi: Option[String],
  sukunimi: Option[String]
)
object Henkilö {
  type Id = String
  def withOid(oid: String) = Henkilö(Some(oid), None, None, None, None)
}

// TODO: käytettyjen koodistojen formaali annotointi

case class OpintoOikeus(
  @Description("Opinto-oikeuden uniikki tunniste. Tietoja syötettäessä kenttä ei ole pakollinen. Tietoja päivitettäessä TOR tunnistaa opinto-oikeuden joko tämän id:n tai muiden kenttien (oppijaOid, organisaatio, diaarinumero) perusteella.")
  id: Option[Int],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  @Description("Opinnot tarjoava koulutustoimija")
  koulutustoimija: Organisaatio,
  @Description("Oppilaitos, jossa opinnot on suoritettu")
  oppilaitos: Organisaatio,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  toimipiste: Option[Organisaatio],
  @Description("Opinto-oikeuteen liittyvän (tutkinto-)suorituksen tiedot.")
  suoritus: Suoritus,
  hojks: Option[Hojks],
  @Description("Opintojen tavoit tutkinto / tutkinnon osa. Koodisto 'opintojentavoite'")
  tavoite: Option[KoodistoKoodiViite],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @Description("Opintojen rahoitus. Koodisto 'opintojenrahoitus'")
  opintojenRahoitus: Option[KoodistoKoodiViite]
)

case class Suoritus(
  @Description("Koulutusmoduulin tunniste. Joko tutkinto tai tutkinnon osa")
  koulutusmoduuli: Koulutusmoduulitoteutus,
  @Description("Opintojen suorituskieli. Koodisto 'kieli'")
  suorituskieli: Option[KoodistoKoodiViite],
  suoritustapa: Suoritustapa,
  @Description("Suorituksen tila. Koodisto 'suorituksentila'")
  tila: Option[KoodistoKoodiViite],
  alkamispäivä: Option[LocalDate],
  arviointi: Option[Arviointi],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[Suoritus]]
)

trait Koulutusmoduulitoteutus
  case class Koulutustoteutus(
    @Description("Tutkinnon 6-numeroinen tutkintokoodi. Koodisto 'koulutus'")
    koulutuskoodi: KoodistoKoodiViite,
    @Description("Tutkinnon perusteen diaarinumero (pakollinen). Ks. ePerusteet-palvelu.")
    perusteenDiaarinumero: Option[String],
    @Description("Tutkintonimike. Koodisto 'tutkintonimikkeet'")
    tutkintonimike: Option[KoodistoKoodiViite] = None,
    @Description("Osaamisala. Koodisto 'osaamisala'")
    osaamisala: Option[KoodistoKoodiViite] = None
  ) extends Koulutusmoduulitoteutus

  case class TutkinnonosatoteutusOps(
    @Description("Tutkinnon osan kansallinen koodi. Koodisto 'tutkinnonosat'")
    tutkinnonosakoodi: KoodistoKoodiViite,
    @Description("Onko pakollinen osa tutkinnossa")
    pakollinen: Boolean,
    paikallinenKoodi: Option[Paikallinenkoodi] = None,
    kuvaus: Option[String] = None
  ) extends Koulutusmoduulitoteutus

  case class TutkinnonosatoteutusPaikallinen(
    paikallinenKoodi: Paikallinenkoodi,
    kuvaus: String,
    @Description("Onko pakollinen osa tutkinnossa")
    pakollinen: Boolean
  ) extends Koulutusmoduulitoteutus

case class Arviointi(
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa.")
  arvosana: KoodistoKoodiViite,
  päivä: Option[LocalDate],
  arvosananKorottaminen: Option[Boolean]
)

case class Vahvistus(
  päivä: Option[LocalDate]
)

case class Suoritustapa(
  @Description("Tutkinnon tai tutkinnon osan suoritustapa. Koodisto 'suoritustapa'")
  tunniste: KoodistoKoodiViite,
  hyväksiluku: Option[Hyväksiluku] = None,
  näyttö: Option[Näyttö] = None,
  oppisopimus: Option[Oppisopimus] = None
)

case class Hyväksiluku(
  osaaminen: Koulutusmoduulitoteutus
)

case class Näyttö(
  kuvaus: String,
  suorituspaikka: String
)

case class Oppisopimus(
  työnantaja: Yritys
)

case class Yritys(
  nimi: String,
  yTunnus: String
)

case class Läsnäolotiedot(
  läsnäolojaksot: List[Läsnäolojakso]
)

case class Läsnäolojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Läsnäolotila (läsnä, poissa...). Koodisto 'lasnaolotila'")
  tila: KoodistoKoodiViite
)

case class Kunta(koodi: String, nimi: Option[String])

case class KoodistoKoodiViite(koodiarvo: String, nimi: Option[String], koodistoUri: String, koodistoVersio: Int)

case class Hojks(hojksTehty: Boolean)

case class Paikallinenkoodi(koodiarvo: String, nimi: String, koodistoUri: String)

case class Organisaatio(
  @Description("Organisaation tunniste Opintopolku-palvelussa")
  oid: String,
  @Description("Organisaation (kielistetty) nimi. Tiedon syötössä tämän kentän arvo jätetään huomioimatta; arvo haetaan Organisaatiopalvelusta.")
  nimi: Option[String] = None
)