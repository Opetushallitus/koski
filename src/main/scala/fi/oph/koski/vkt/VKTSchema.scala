package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri, Representative}
import fi.oph.koski.suoritusjako.common.Jakolinkki
import fi.oph.scalaschema.annotation.{Discriminator, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object VKTSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[VKTOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo, // Vain vahvistetut
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo // YO-tutkinnoista otetaan vain vahvistetut
  )
}

case class VKTOppija(
  jakolinkki: Option[Jakolinkki] = None,
  henkilö: Henkilo,
  opiskeluoikeudet: List[VKTOpiskeluoikeus]
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

trait VKTOpiskeluoikeus {
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

  def tila: VKTOpiskeluoikeudenTila

  def lisätiedot: Option[VKTOpiskeluoikeudenLisätiedot]

  def withSuoritukset(suoritukset: List[Suoritus]): VKTOpiskeluoikeus

  def withoutSisältyyOpiskeluoikeuteen: VKTOpiskeluoikeus
}

trait VKTKoskeenTallennettavaOpiskeluoikeus extends VKTOpiskeluoikeus {
  def oid: Option[String]

  def versionumero: Option[Int]

  @Deprecated("Ei palauteta. Kenttä on näkyvissä skeemassa vain teknisistä syistä.")
  def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus]
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

trait VKTKoodiViite {
  def koodiarvo: String
}

@Title("Koodistokoodiviite")
case class VKTKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
) extends VKTKoodiViite

object VKTKoodistokoodiviite {
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = VKTKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

@Title("Paikallinen koodi")
case class VKTPaikallinenKoodi(
  koodiarvo: String,
  nimi: schema.LocalizedString,
  koodistoUri: Option[String]
) extends VKTKoodiViite

object VKTPaikallinenKoodi {
  def fromKoskiSchema(kv: schema.PaikallinenKoodi) = VKTPaikallinenKoodi(
    kv.koodiarvo,
    kv.nimi,
    kv.koodistoUri
  )
}

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[VKTKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[VKTKoodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[VKTKoodistokoodiviite]
)

@Title("Opiskeluoikeuden tila")
case class VKTOpiskeluoikeudenTila(
  @Representative
  opiskeluoikeusjaksot: List[VKTOpiskeluoikeusjakso]
)

@Title("Opiskeluoikeusjakso")
case class VKTOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: VKTKoodistokoodiviite,
  opintojenRahoitus: Option[VKTKoodistokoodiviite]
)

@Title("Opiskeluoikeuden lisätiedot")
trait VKTOpiskeluoikeudenLisätiedot


trait SuorituksenKoulutusmoduuli {
  def tunniste: VKTKoodiViite
}

trait SuorituksenKooditettuKoulutusmoduuli extends SuorituksenKoulutusmoduuli {
  def tunniste: VKTKoodistokoodiviite
}

@Title("Päätason suoritus")
case class VKTPäätasonSuoritus(
  koulutusmoduuli: VKTPäätasonKoulutusmoduuli,
  suorituskieli: VKTKoodistokoodiviite,
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste]
) extends Suoritus

@Title("Päätason koulutusmoduuli")
case class VKTPäätasonKoulutusmoduuli(
  tunniste: VKTKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[VKTKoodistokoodiviite],
) extends SuorituksenKooditettuKoulutusmoduuli

case class Vahvistus(päivä: LocalDate)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[VKTKoodistokoodiviite] = None
)
