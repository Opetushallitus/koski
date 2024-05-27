package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri, Representative}
import fi.oph.koski.suoritusjako.common.Jakolinkki
import fi.oph.scalaschema.annotation.{Discriminator, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object VktSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[VktOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo, // Vain vahvistetut
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo // YO-tutkinnoista otetaan vain vahvistetut
  )
}

case class VktOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[VktOpiskeluoikeus]
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

trait VktOpiskeluoikeus {
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

  def tila: VktOpiskeluoikeudenTila

  def lisätiedot: Option[VktOpiskeluoikeudenLisätiedot]

  def withSuoritukset(suoritukset: List[Suoritus]): VktOpiskeluoikeus
}

trait VktKoskeenTallennettavaOpiskeluoikeus extends VktOpiskeluoikeus {
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

trait VktKoodiViite {
  def koodiarvo: String
}

@Title("Koodistokoodiviite")
case class VktKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
) extends VktKoodiViite

object VktKoodistokoodiviite {
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = VktKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

@Title("Paikallinen koodi")
case class VktPaikallinenKoodi(
  koodiarvo: String,
  nimi: schema.LocalizedString,
  koodistoUri: Option[String]
) extends VktKoodiViite

object VktPaikallinenKoodi {
  def fromKoskiSchema(kv: schema.PaikallinenKoodi) = VktPaikallinenKoodi(
    kv.koodiarvo,
    kv.nimi,
    kv.koodistoUri
  )
}

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[VktKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[VktKoodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[VktKoodistokoodiviite]
)

@Title("Opiskeluoikeuden tila")
case class VktOpiskeluoikeudenTila(
  @Representative
  opiskeluoikeusjaksot: List[VktOpiskeluoikeusjakso]
)

@Title("Opiskeluoikeusjakso")
case class VktOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: VktKoodistokoodiviite,
  opintojenRahoitus: Option[VktKoodistokoodiviite]
)

@Title("Opiskeluoikeuden lisätiedot")
trait VktOpiskeluoikeudenLisätiedot


trait SuorituksenKoulutusmoduuli {
  def tunniste: VktKoodiViite
}

trait SuorituksenKooditettuKoulutusmoduuli extends SuorituksenKoulutusmoduuli {
  def tunniste: VktKoodistokoodiviite
}

@Title("Päätason suoritus")
case class VktPäätasonSuoritus(
  koulutusmoduuli: VktPäätasonKoulutusmoduuli,
  suorituskieli: VktKoodistokoodiviite,
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste]
) extends Suoritus

@Title("Päätason koulutusmoduuli")
case class VktPäätasonKoulutusmoduuli(
  tunniste: VktKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[VktKoodistokoodiviite],
) extends SuorituksenKooditettuKoulutusmoduuli

case class Vahvistus(päivä: LocalDate)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[VktKoodistokoodiviite] = None
)
