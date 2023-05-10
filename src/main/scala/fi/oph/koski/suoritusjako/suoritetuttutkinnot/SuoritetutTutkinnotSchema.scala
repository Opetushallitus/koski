package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.scalaschema.annotation.Discriminator
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.{LocalDate, LocalDateTime}

object SuoritetutTutkinnotSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[SuoritetutTutkinnotOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    "ammatillinenkoulutus",
    // TODO: TOR-1025 ebtutkinto
    // TODO: TOR-1025 diatutkinto
    // TODO: TOR-1025 virta
    "ylioppilastutkinto",
  ).filter(_.nonEmpty)
}

case class SuoritetutTutkinnotOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[SuoritetutTutkinnotOpiskeluoikeus]
)

case class Henkilo(
  oid: String,
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  kutsumanimi: String
)
object Henkilo {
  def fromOppijaHenkilö(oppijaHenkilö: fi.oph.koski.henkilo.OppijaHenkilö) = Henkilo(
    oid = oppijaHenkilö.oid,
    syntymäaika = oppijaHenkilö.syntymäaika,
    etunimet = oppijaHenkilö.etunimet,
    sukunimi = oppijaHenkilö.sukunimi,
    kutsumanimi = oppijaHenkilö.kutsumanimi
  )
}

trait SuoritetutTutkinnotOpiskeluoikeus {
  def oid: Option[String]
  def versionumero: Option[Int]
  def aikaleima: Option[LocalDateTime]
  def oppilaitos: Option[Oppilaitos]
  def koulutustoimija: Option[Koulutustoimija]
  def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus]
  def suoritukset: List[Suoritus]
  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
  def organisaatiohistoria: Option[List[OrganisaatioHistoria]]

  def withSuoritukset(suoritukset: List[Suoritus]): SuoritetutTutkinnotOpiskeluoikeus
}

case class SisältäväOpiskeluoikeus(
  oid: String,
  oppilaitos: Oppilaitos
)

case class OrganisaatioHistoria(
  muutospäivä: LocalDate,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija]
)

trait Suoritus{
  def koulutusmoduuli: SuorituksenKoulutusmoduuli
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
  def vahvistus: Option[Vahvistus]
}

trait SuorituksenKoulutusmoduuli {
  def tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

case class SuoritetutTutkinnotKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
)
object SuoritetutTutkinnotKoodistokoodiviite{
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = SuoritetutTutkinnotKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

// TODO: TOR-1025 tarvitaanko muita tietoja kuin päivä vahvistuksesta?
case class Vahvistus(päivä: LocalDate)

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[SuoritetutTutkinnotKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[SuoritetutTutkinnotKoodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[SuoritetutTutkinnotKoodistokoodiviite]
)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[SuoritetutTutkinnotKoodistokoodiviite] = None
)

case class SuoritetutTutkinnotLaajuus(arvo: Double, yksikkö: SuoritetutTutkinnotKoodistokoodiviite)
