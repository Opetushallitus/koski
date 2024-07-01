package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoUri, Representative}
import fi.oph.scalaschema.annotation.{Discriminator, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object HakemuspalveluSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[HakemuspalveluOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo
  )
}

case class HakemuspalveluOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[HakemuspalveluOpiskeluoikeus]
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

trait HakemuspalveluOpiskeluoikeus {
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

  def tila: HakemuspalveluOpiskeluoikeudenTila

  def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot]

  def withSuoritukset(suoritukset: List[Suoritus]): HakemuspalveluOpiskeluoikeus
}

trait HakemuspalveluKoskeenTallennettavaOpiskeluoikeus extends HakemuspalveluOpiskeluoikeus {
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

trait HakemuspalveluKoodiViite {
  def koodiarvo: String
}

@Title("Koodistokoodiviite")
case class HakemuspalveluKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
) extends HakemuspalveluKoodiViite

object HakemuspalveluKoodistokoodiviite {
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = HakemuspalveluKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

@Title("Paikallinen koodi")
case class HakemuspalveluPaikallinenKoodi(
  koodiarvo: String,
  nimi: schema.LocalizedString,
  koodistoUri: Option[String]
) extends HakemuspalveluKoodiViite

object HakemuspalveluPaikallinenKoodi {
  def fromKoskiSchema(kv: schema.PaikallinenKoodi) = HakemuspalveluPaikallinenKoodi(
    kv.koodiarvo,
    kv.nimi,
    kv.koodistoUri
  )
}

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[HakemuspalveluKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[HakemuspalveluKoodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[HakemuspalveluKoodistokoodiviite]
)

@Title("Opiskeluoikeuden tila")
case class HakemuspalveluOpiskeluoikeudenTila(
  @Representative
  opiskeluoikeusjaksot: List[HakemuspalveluOpiskeluoikeusjakso]
)

@Title("Opiskeluoikeusjakso")
case class HakemuspalveluOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: HakemuspalveluKoodistokoodiviite,
  opintojenRahoitus: Option[HakemuspalveluKoodistokoodiviite]
)

@Title("Opiskeluoikeuden lisätiedot")
trait HakemuspalveluOpiskeluoikeudenLisätiedot


trait SuorituksenKoulutusmoduuli {
  def tunniste: HakemuspalveluKoodiViite
}

trait SuorituksenKooditettuKoulutusmoduuli extends SuorituksenKoulutusmoduuli {
  def tunniste: HakemuspalveluKoodistokoodiviite
}

@Title("Päätason suoritus")
case class HakemuspalveluPäätasonSuoritus(
  koulutusmoduuli: HakemuspalveluPäätasonKoulutusmoduuli,
  suorituskieli: HakemuspalveluKoodistokoodiviite,
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste]
) extends Suoritus

@Title("Päätason koulutusmoduuli")
case class HakemuspalveluPäätasonKoulutusmoduuli(
  tunniste: HakemuspalveluKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[HakemuspalveluKoodistokoodiviite],
) extends SuorituksenKooditettuKoulutusmoduuli

case class Vahvistus(päivä: LocalDate)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[HakemuspalveluKoodistokoodiviite] = None
)
