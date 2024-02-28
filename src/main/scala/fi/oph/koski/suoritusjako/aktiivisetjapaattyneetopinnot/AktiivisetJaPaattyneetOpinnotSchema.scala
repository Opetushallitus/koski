package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri, Representative}
import fi.oph.koski.suoritusjako.common.Jakolinkki
import fi.oph.scalaschema.annotation.{Discriminator, ReadFlattened, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object AktiivisetJaPäättyneetOpinnotSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[AktiivisetJaPäättyneetOpinnotOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.tuva.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo, // Vain vahvistetut
    schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo, // Vain ne, missä on lukiota vastaavia luokkia
    schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo, // Vain ne, missä on lukiota vastaavia luokkia
    schema.OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo, // Ei vapaatavoitteisia
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo // YO-tutkinnoista otetaan vain vahvistetut
  )
}

case class AktiivisetJaPäättyneetOpinnotOppija(
  jakolinkki: Option[Jakolinkki] = None,
  henkilö: Henkilo,
  opiskeluoikeudet: List[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]
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

trait AktiivisetJaPäättyneetOpinnotOpiskeluoikeus {
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
  def tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  def lisätiedot: Option[AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot]

  def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotOpiskeluoikeus
  def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus
}

trait AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus extends AktiivisetJaPäättyneetOpinnotOpiskeluoikeus {
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

trait AktiivisetJaPäättyneetOpinnotKoodiViite {
  def koodiarvo: String
}

@Title("Koodistokoodiviite")
case class AktiivisetJaPäättyneetOpinnotKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
) extends AktiivisetJaPäättyneetOpinnotKoodiViite

object AktiivisetJaPäättyneetOpinnotKoodistokoodiviite{
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

@Title("Paikallinen koodi")
case class AktiivisetJaPäättyneetOpinnotPaikallinenKoodi(
  koodiarvo: String,
  nimi: schema.LocalizedString,
  koodistoUri: Option[String]
) extends AktiivisetJaPäättyneetOpinnotKoodiViite

object AktiivisetJaPäättyneetOpinnotPaikallinenKoodi {
  def fromKoskiSchema(kv: schema.PaikallinenKoodi) = AktiivisetJaPäättyneetOpinnotPaikallinenKoodi(
    kv.koodiarvo,
    kv.nimi,
    kv.koodistoUri
  )
}

// case class Vahvistus(päivä: LocalDate)

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]
)

@Title("Opiskeluoikeuden tila")
case class AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila(
  @Representative
  opiskeluoikeusjaksot: List[AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso]
)

@Title("Opiskeluoikeusjakso")
case class AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  opintojenRahoitus: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]
)

@Title("Opiskeluoikeuden lisätiedot")
trait AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot


trait SuorituksenKoulutusmoduuli {
  def tunniste: AktiivisetJaPäättyneetOpinnotKoodiViite
}

trait SuorituksenKooditettuKoulutusmoduuli extends SuorituksenKoulutusmoduuli {
  def tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

@Title("Päätason suoritus")
case class AktiivisetJaPäättyneetOpinnotPäätasonSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli,
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste]
) extends Suoritus

@Title("Päätason koulutusmoduuli")
case class AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
) extends SuorituksenKooditettuKoulutusmoduuli

@ReadFlattened
@Title("Osaamisalajakso")
case class AktiivisetJaPäättyneetOpinnotOsaamisalajakso(
  osaamisala: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  alku: Option[LocalDate] = None,
  loppu: Option[LocalDate] = None
)

case class Vahvistus(päivä: LocalDate)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite] = None
)
