package fi.oph.tor.schema

import java.time.LocalDate
import fi.oph.tor.schema.generic.DescriptionAnnotation
import scala.annotation.meta._

case class TorOppija(
  henkilö: Henkilö,
  @DescriptionAnnotation("Lista henkilön opinto-oikeuksista. Sisältää vain ne opinto-oikeudet, joihin käyttäjällä on oikeudet. Esimerkiksi ammatilliselle toimijalle ei välttämättä näy henkilön lukio-opintojen tietoja.")
  opintoOikeudet: Seq[OpintoOikeus]
)

@DescriptionAnnotation("Henkilötiedot. Syötettäessä vaaditaan joko `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö.")
case class Henkilö(
  @DescriptionAnnotation("Yksilöivä tunniste Opintopolku-palvelussa")
  oid: Option[String],
  @DescriptionAnnotation("Suomalainen henkilötunnus")
  hetu: Option[String],
  etunimet: Option[String],
  @DescriptionAnnotation("Kutsumanimi, oltava yksi etunimistä. Esimerkiksi etunimille \"Juha-Matti Petteri\" kelpaavat joko \"Juha-Matti\", \"Juha\", \"Matti\" tai \"Petteri\".")
  kutsumanimi: Option[String],
  sukunimi: Option[String]
)
object Henkilö {
  type Id = String
  def withOid(oid: String) = Henkilö(Some(oid), None, None, None, None)
}

case class OpintoOikeus(
  @DescriptionAnnotation("Opinto-oikeuden uniikki tunniste. Tietoja syötettäessä kenttä ei ole pakollinen. Tietoja päivitettäessä TOR tunnistaa opinto-oikeuden joko tämän id:n tai muiden kenttien (oppijaOid, organisaatio, diaarinumero) perusteella.")
  id: Option[Int],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  @DescriptionAnnotation("Opinnot tarjoava koulutustoimija")
  koulutustoimija: Organisaatio,
  @DescriptionAnnotation("Oppilaitos, jossa opinnot on suoritettu")
  oppilaitos: Organisaatio,
  @DescriptionAnnotation("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  toimipiste: Option[Organisaatio],
  @DescriptionAnnotation("Opinto-oikeuteen liittyvän (tutkinto-)suorituksen tiedot.")
  suoritus: Suoritus,
  hojks: Option[Hojks],
  @DescriptionAnnotation("Opintojen tavoit tutkinto / tutkinnon osa")
  @KoodistoAnnotation("opintojentavoite")
  tavoite: Option[KoodistoKoodiViite],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @DescriptionAnnotation("Opintojen rahoitus")
  @KoodistoAnnotation("opintojenrahoitus")
  opintojenRahoitus: Option[KoodistoKoodiViite]
)

case class Suoritus(
  @DescriptionAnnotation("Koulutusmoduulin tunniste. Joko tutkinto tai tutkinnon osa")
  koulutusmoduuli: Koulutusmoduulitoteutus,
  @DescriptionAnnotation("Opintojen suorituskieli")
  @KoodistoAnnotation("kieli")
  suorituskieli: Option[KoodistoKoodiViite],
  suoritustapa: Suoritustapa,
  @DescriptionAnnotation("Suorituksen tila")
  @KoodistoAnnotation("suorituksentila")
  tila: Option[KoodistoKoodiViite],
  alkamispäivä: Option[LocalDate],
  arviointi: Option[Arviointi],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[Suoritus]]
)

trait Koulutusmoduulitoteutus
  case class Koulutustoteutus(
    @DescriptionAnnotation("Tutkinnon 6-numeroinen tutkintokoodi")
    @KoodistoAnnotation("koulutus")
    koulutuskoodi: KoodistoKoodiViite,
    @DescriptionAnnotation("Tutkinnon perusteen diaarinumero (pakollinen). Ks. ePerusteet-palvelu.")
    perusteenDiaarinumero: Option[String],
    @DescriptionAnnotation("Tutkintonimike")
    @KoodistoAnnotation("tutkintonimikkeet")
    tutkintonimike: Option[KoodistoKoodiViite] = None,
    @DescriptionAnnotation("Osaamisala")
    @KoodistoAnnotation("osaamisala")
    osaamisala: Option[KoodistoKoodiViite] = None
  ) extends Koulutusmoduulitoteutus

  case class TutkinnonosatoteutusOps(
    @DescriptionAnnotation("Tutkinnon osan kansallinen koodi")
    @KoodistoAnnotation("tutkinnonosat")
    tutkinnonosakoodi: KoodistoKoodiViite,
    @DescriptionAnnotation("Onko pakollinen osa tutkinnossa")
    pakollinen: Boolean,
    paikallinenKoodi: Option[Paikallinenkoodi] = None,
    kuvaus: Option[String] = None
  ) extends Koulutusmoduulitoteutus

  case class TutkinnonosatoteutusPaikallinen(
    paikallinenKoodi: Paikallinenkoodi,
    kuvaus: String,
    @DescriptionAnnotation("Onko pakollinen osa tutkinnossa")
    pakollinen: Boolean
  ) extends Koulutusmoduulitoteutus

case class Arviointi(
  @DescriptionAnnotation("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa.")
  arvosana: KoodistoKoodiViite,
  päivä: Option[LocalDate],
  arvosananKorottaminen: Option[Boolean]
)

case class Vahvistus(
  päivä: Option[LocalDate]
)

case class Suoritustapa(
  @DescriptionAnnotation("Tutkinnon tai tutkinnon osan suoritustapa")
  @KoodistoAnnotation("suoritustapa")
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
  @DescriptionAnnotation("Läsnäolotila (läsnä, poissa...)")
  @KoodistoAnnotation("lasnaolotila")
  tila: KoodistoKoodiViite
)

case class Kunta(koodi: String, nimi: Option[String])

case class KoodistoKoodiViite(koodiarvo: String, nimi: Option[String], koodistoUri: String, koodistoVersio: Int)

case class Hojks(hojksTehty: Boolean)

case class Paikallinenkoodi(koodiarvo: String, nimi: String, koodistoUri: String)

case class Organisaatio(
  @DescriptionAnnotation("Organisaation tunniste Opintopolku-palvelussa")
  oid: String,
  @DescriptionAnnotation("Organisaation (kielistetty) nimi. Tiedon syötössä tämän kentän arvo jätetään huomioimatta; arvo haetaan Organisaatiopalvelusta.")
  nimi: Option[String] = None
)