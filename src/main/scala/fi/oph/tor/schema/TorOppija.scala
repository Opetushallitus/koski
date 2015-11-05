package fi.oph.tor.schema

import java.time.LocalDate
import java.util.Date

case class TorOppija(henkilö: Henkilö, opintoOikeudet: Seq[OpintoOikeus])

case class Henkilö(
  oid: Option[String],
  hetu: Option[String],
  etunimet: Option[String],
  kutsumanimi: Option[String],
  sukunimi: Option[String]
)
object Henkilö {
  type Id = String
  def withOid(oid: String) = Henkilö(Some(oid), None, None, None, None)
}

case class OpintoOikeus(
  id: Option[Int],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  koulutustoimija: Organisaatio,
  oppilaitos: Organisaatio,
  toimipiste: Option[Organisaatio],
  suoritus: Suoritus,
  hojks: Option[Hojks],
  tavoite: Option[KoodistoKoodiViite],           // Koodisto: opintojentavoite
  läsnäolotiedot: Option[Läsnäolotiedot],
  opintojenRahoitus: Option[KoodistoKoodiViite]  // Koodisto: opintojenrahoitus
)

case class Suoritus(
  koulutusmoduuli: Koulutusmoduulitoteutus,
  suorituskieli: Option[KoodistoKoodiViite],     // Koodisto: kieli
  suoritustapa: Suoritustapa,
  tila: Option[KoodistoKoodiViite],              // Koodisto: suorituksentila
  alkamispäivä: Option[LocalDate],
  arviointi: Option[Arviointi],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[Suoritus]]
)

@Implementations(List(classOf[Koulutustoteutus], classOf[Tutkinnonosatoteutus]))
trait Koulutusmoduulitoteutus
  case class Koulutustoteutus(
    koulutuskoodi: KoodistoKoodiViite,            // Koodisto: koulutus
    perusteenDiaarinumero: Option[String],
    tutkintonimike: Option[KoodistoKoodiViite] = None,           // Koodisto: tutkintonimikkeet // TODO: mihin kuuluu?
    osaamisala: Option[KoodistoKoodiViite] = None                // Koodisto: osaamisala
  ) extends Koulutusmoduulitoteutus

  case class Tutkinnonosatoteutus(
    tutkinnonosakoodi: Option[KoodistoKoodiViite],// Koodisto: tutkinnonosat
    paikallinenKoodi: Option[Paikallinenkoodi],
    kuvaus: Option[String],
    pakollinen: Option[Boolean]
  ) extends Koulutusmoduulitoteutus

case class Arviointi(
  arvosana: KoodistoKoodiViite,                   // Koodisto kertoo asteikon, koodi arvosanan
  päivä: Option[LocalDate],
  arvosananKorottaminen: Option[Boolean]
)

case class Vahvistus(
  päivä: Option[LocalDate]
)

case class Suoritustapa(
  tunniste: KoodistoKoodiViite,                   // Koodisto: suoritustapa
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
  tila: KoodistoKoodiViite                   // Koodisto: lasnaolotila
)

case class Kunta(koodi: String, nimi: Option[String])

case class KoodistoKoodiViite(koodiarvo: String, nimi: Option[String], koodistoUri: String, koodistoVersio: Int)

case class Hojks(hojksTehty: Boolean)

case class Paikallinenkoodi(koodiarvo: String, nimi: String, koodistoUri: String)

case class Organisaatio(oid: String, nimi: Option[String] = None)