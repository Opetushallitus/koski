package fi.oph.koski.kios

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoUri, Representative}
import fi.oph.scalaschema.annotation.{Discriminator, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object KiosSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[KiosOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo, // Vain vahvistetut
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo // YO-tutkinnoista otetaan vain vahvistetut
  )
}

case class KiosOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[KiosOpiskeluoikeus]
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

trait KiosOpiskeluoikeus {
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

  def tila: KiosOpiskeluoikeudenTila

  def lisätiedot: Option[KiosOpiskeluoikeudenLisätiedot]

  def withSuoritukset(suoritukset: List[Suoritus]): KiosOpiskeluoikeus
}

trait KiosKoskeenTallennettavaOpiskeluoikeus extends KiosOpiskeluoikeus {
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

trait KiosKoodiViite {
  def koodiarvo: String
}

@Title("Koodistokoodiviite")
case class KiosKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
) extends KiosKoodiViite

object KiosKoodistokoodiviite {
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = KiosKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

@Title("Paikallinen koodi")
case class KiosPaikallinenKoodi(
  koodiarvo: String,
  nimi: schema.LocalizedString,
  koodistoUri: Option[String]
) extends KiosKoodiViite

object KiosPaikallinenKoodi {
  def fromKoskiSchema(kv: schema.PaikallinenKoodi) = KiosPaikallinenKoodi(
    kv.koodiarvo,
    kv.nimi,
    kv.koodistoUri
  )
}

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[KiosKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[KiosKoodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[KiosKoodistokoodiviite]
)

@Title("Opiskeluoikeuden tila")
case class KiosOpiskeluoikeudenTila(
  @Representative
  opiskeluoikeusjaksot: List[KiosOpiskeluoikeusjakso]
)

@Title("Opiskeluoikeusjakso")
case class KiosOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: KiosKoodistokoodiviite,
  opintojenRahoitus: Option[KiosKoodistokoodiviite]
)

@Title("Opiskeluoikeuden lisätiedot")
trait KiosOpiskeluoikeudenLisätiedot


trait SuorituksenKoulutusmoduuli {
  def tunniste: KiosKoodiViite
}

trait SuorituksenKooditettuKoulutusmoduuli extends SuorituksenKoulutusmoduuli {
  def tunniste: KiosKoodistokoodiviite
}

@Title("Päätason suoritus")
case class KiosPäätasonSuoritus(
  koulutusmoduuli: KiosPäätasonKoulutusmoduuli,
  suorituskieli: KiosKoodistokoodiviite,
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste]
) extends Suoritus

@Title("Päätason koulutusmoduuli")
case class KiosPäätasonKoulutusmoduuli(
  tunniste: KiosKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[KiosKoodistokoodiviite],
) extends SuorituksenKooditettuKoulutusmoduuli

case class Vahvistus(päivä: LocalDate)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[KiosKoodistokoodiviite] = None
)
