package fi.oph.koski.sdg

import fi.oph.koski.schema
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
  )
}

case class SdgOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[SdgOpiskeluoikeus]
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

  def tila: SdgOpiskeluoikeudenTila

  def lisätiedot: Option[SdgOpiskeluoikeudenLisätiedot]

  def withSuoritukset(suoritukset: List[Suoritus]): SdgOpiskeluoikeus
}

trait SdgKoskeenTallennettavaOpiskeluoikeus extends SdgOpiskeluoikeus {
  def oid: Option[String]

  def versionumero: Option[Int]
}

case class SisältäväOpiskeluoikeus(
  oid: String,
  oppilaitos: Oppilaitos
)

trait Suoritus {
  def koulutusmoduuli: SuorituksenKoulutusmoduuli

  @Discriminator
  def tyyppi: schema.Koodistokoodiviite

  def vahvistus: Option[Vahvistus]

  def toimipiste: Option[Toimipiste]
}

trait SdgKoodiViite {
  def koodiarvo: String
}

@Title("Koodistokoodiviite")
case class SdgKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
) extends SdgKoodiViite

object SdgKoodistokoodiviite {
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = SdgKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

@Title("Paikallinen koodi")
case class SdgPaikallinenKoodi(
  koodiarvo: String,
  nimi: schema.LocalizedString,
  koodistoUri: Option[String]
) extends SdgKoodiViite

object SdgPaikallinenKoodi {
  def fromKoskiSchema(kv: schema.PaikallinenKoodi) = SdgPaikallinenKoodi(
    kv.koodiarvo,
    kv.nimi,
    kv.koodistoUri
  )
}

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[SdgKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[SdgKoodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[SdgKoodistokoodiviite]
)

@Title("Opiskeluoikeuden tila")
case class SdgOpiskeluoikeudenTila(
  @Representative
  opiskeluoikeusjaksot: List[SdgOpiskeluoikeusjakso]
)

@Title("Opiskeluoikeusjakso")
case class SdgOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: SdgKoodistokoodiviite,
  opintojenRahoitus: Option[SdgKoodistokoodiviite]
)

@Title("Opiskeluoikeuden lisätiedot")
trait SdgOpiskeluoikeudenLisätiedot


trait SuorituksenKoulutusmoduuli {
  def tunniste: SdgKoodiViite
}

trait SuorituksenKooditettuKoulutusmoduuli extends SuorituksenKoulutusmoduuli {
  def tunniste: SdgKoodistokoodiviite
}

@Title("Päätason suoritus")
case class SdgPäätasonSuoritus(
  koulutusmoduuli: SdgPäätasonKoulutusmoduuli,
  suorituskieli: SdgKoodistokoodiviite,
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste]
) extends Suoritus

@Title("Päätason koulutusmoduuli")
case class SdgPäätasonKoulutusmoduuli(
  tunniste: SdgKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[SdgKoodistokoodiviite],
) extends SuorituksenKooditettuKoulutusmoduuli

case class Vahvistus(päivä: LocalDate)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[SdgKoodistokoodiviite] = None
)
