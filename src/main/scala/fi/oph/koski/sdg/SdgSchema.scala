package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoUri}
import fi.oph.scalaschema.annotation.{Discriminator, SkipSerialization, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object SdgSchema {
  lazy val schemaJson: JValue = {
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[Oppija]).asInstanceOf[ClassSchema])
  }

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo,
  )
}

@Title("Oppija")
case class Oppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[Opiskeluoikeus]
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

trait Opiskeluoikeus {
  def oppilaitos: Option[schema.Oppilaitos]
  def koulutustoimija: Option[schema.Koulutustoimija]
  def suoritukset: List[Suoritus]

  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite

  def tila: GenericOpiskeluoikeudenTila

  @SyntheticProperty
  def alkamispäivä: Option[LocalDate] = schema.Opiskeluoikeus.alkamispäivä(this.tyyppi.koodiarvo, this.tila.opiskeluoikeusjaksot.map(_.alku))

  @SyntheticProperty
  def päättymispäivä: Option[LocalDate] = schema.Opiskeluoikeus.päättymispäivä(this.tyyppi.koodiarvo, this.tila.opiskeluoikeusjaksot.map(j => (j.alku, j.tila.koodiarvo)))

  def withSuoritukset(suoritukset: List[Suoritus]): Opiskeluoikeus
}

trait GenericOpiskeluoikeudenTila {
  def opiskeluoikeusjaksot: List[GenericOpiskeluoikeusjakso]
}

trait GenericOpiskeluoikeusjakso {
  def alku: LocalDate
  def tila: schema.Koodistokoodiviite
}

case class Opiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoUri("koskiopiskeluoikeudentila")
  tila: schema.Koodistokoodiviite
) extends GenericOpiskeluoikeusjakso

@Title("Opiskeluoikeuden tila")
case class OpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[Opiskeluoikeusjakso]
) extends GenericOpiskeluoikeudenTila

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
  def tunniste: schema.Koodistokoodiviite
}

case class Vahvistus(päivä: LocalDate)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[schema.Koodistokoodiviite] = None
)

trait WithTunnustettuBoolean {
  @SkipSerialization
  def tunnustettu: Option[schema.OsaamisenTunnustaminen]

  @SyntheticProperty
  def tunnustettuBoolean: Boolean = tunnustettu.nonEmpty
}
