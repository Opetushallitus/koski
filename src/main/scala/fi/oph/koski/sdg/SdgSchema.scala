package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.{Koodistokoodiviite, Koulutustoimija, Opiskeluoikeusjakso, OpiskeluoikeudenTila, Oppilaitos}
import fi.oph.koski.schema.annotation.{KoodistoUri, Representative}
import fi.oph.scalaschema.annotation.{Discriminator, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object SdgSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[SdgOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo,
  )
}

@Title("Oppija")
case class SdgOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[SdgOpiskeluoikeus]
)

case class Henkilo(
  oid: String,
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  kutsumanimi: String
)

object Henkilo {
  def fromOppijaHenkilö(oppijaHenkilö: fi.oph.koski.henkilo.OppijaHenkilö) = Henkilo(
    oid = oppijaHenkilö.oid,
    hetu = oppijaHenkilö.hetu,
    syntymäaika = oppijaHenkilö.syntymäaika,
    etunimet = oppijaHenkilö.etunimet,
    sukunimi = oppijaHenkilö.sukunimi,
    kutsumanimi = oppijaHenkilö.kutsumanimi
  )
}

trait SdgOpiskeluoikeus {
  def oppilaitos: Option[Oppilaitos]
  def koulutustoimija: Option[Koulutustoimija]
  def suoritukset: List[Suoritus]

  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite

  @SyntheticProperty
  def alkamispäivä: Option[LocalDate] = schema.Opiskeluoikeus.alkamispäivä(this.tyyppi.koodiarvo, this.tila.opiskeluoikeusjaksot.map(_.alku))

  @SyntheticProperty
  def päättymispäivä: Option[LocalDate] = schema.Opiskeluoikeus.päättymispäivä(this.tyyppi.koodiarvo, this.tila.opiskeluoikeusjaksot.map(j => (j.alku, j.tila.koodiarvo)))
  def tila: OpiskeluoikeudenTila
  def withSuoritukset(suoritukset: List[Suoritus]): SdgOpiskeluoikeus
}

trait SdgKoskeenTallennettavaOpiskeluoikeus extends SdgOpiskeluoikeus {
  def oid: Option[String]

  def versionumero: Option[Int]
}

trait Suoritus {
  def koulutusmoduuli: schema.Koulutusmoduuli

  @Discriminator
  def tyyppi: schema.Koodistokoodiviite

  def vahvistus: Option[Vahvistus]

  def toimipiste: Option[Toimipiste]

  def osasuoritukset: Option[List[Osasuoritus]]

  def withOsasuoritukset(os: Option[List[Osasuoritus]]): Suoritus
}

trait Osasuoritus

@Title("Opiskeluoikeuden lisätiedot")
trait SdgOpiskeluoikeudenLisätiedot


trait SuorituksenKoulutusmoduuli {
  def tunniste: Koodistokoodiviite
}

case class Vahvistus(päivä: LocalDate)

object Toimipiste {
  def fromOrganisaatioWithOid(o: schema.OrganisaatioWithOid) = Toimipiste(
    oid = o.oid,
    nimi = o.nimi,
    kotipaikka = o.kotipaikka
  )
}
case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[Koodistokoodiviite] = None
)
