package fi.oph.koski.kela

import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri, UnitOfMeasure}
import fi.oph.scalaschema.annotation.{Discriminator, ReadFlattened, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.{LocalDate, LocalDateTime}

object KelaSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[KelaOppija]).asInstanceOf[ClassSchema])
}

case class KelaOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[KelaOpiskeluoikeus]
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
  def fromOppijaHenkilö(oppijaHenkilö: OppijaHenkilö) = Henkilo(
    oid = oppijaHenkilö.oid,
    hetu = oppijaHenkilö.hetu,
    syntymäaika = oppijaHenkilö.syntymäaika,
    etunimet = oppijaHenkilö.etunimet,
    sukunimi = oppijaHenkilö.sukunimi,
    kutsumanimi = oppijaHenkilö.kutsumanimi
  )
}

trait KelaOpiskeluoikeus {
  def oid: Option[String]
  def versionumero: Option[Int]
  def aikaleima: Option[LocalDateTime]
  def oppilaitos: Option[Oppilaitos]
  def koulutustoimija: Option[Koulutustoimija]
  def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus]
  def arvioituPäättymispäivä: Option[LocalDate]
  def tila: OpiskeluoikeudenTila
  def suoritukset: List[Suoritus]
  def lisätiedot: Option[OpiskeluoikeudenLisätiedot]
  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
  @SyntheticProperty
  def alkamispäivä: Option[LocalDate] = this.tila.opiskeluoikeusjaksot.headOption.map(_.alku)
  @SyntheticProperty
  def päättymispäivä: Option[LocalDate] = this.tila.opiskeluoikeusjaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(_.alku)
  def organisaatioHistoria: Option[List[OrganisaatioHistoria]]
  @Deprecated("Ei palauteta Kela-API:ssa. Kenttä on näkyvissä skeemassa vain teknisistä syistä.")
  def organisaatiohistoria: Option[List[OrganisaatioHistoria]]

  def withCleanedData: KelaOpiskeluoikeus = this.withOrganisaatiohistoria.withEmptyArvosana

  protected def withOrganisaatiohistoria: KelaOpiskeluoikeus
  protected def withEmptyArvosana: KelaOpiskeluoikeus
}

case class SisältäväOpiskeluoikeus(
  oid: String,
  oppilaitos: Oppilaitos
)

case class KelaOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KelaOpiskeluoikeudenTilaRahoitustiedoilla(
  opiskeluoikeusjaksot: List[KelaOpiskeluoikeusjaksoRahoituksella]
) extends OpiskeluoikeudenTila

trait OpiskeluoikeudenTila {
  def opiskeluoikeusjaksot: List[Opiskeluoikeusjakso]
}

case class KelaOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: KelaKoodistokoodiviite,
) extends Opiskeluoikeusjakso

case class KelaOpiskeluoikeusjaksoRahoituksella(
  alku: LocalDate,
  tila: KelaKoodistokoodiviite,
  opintojenRahoitus: Option[KelaKoodistokoodiviite]
) extends Opiskeluoikeusjakso

trait Opiskeluoikeusjakso {
  def alku: LocalDate
  def tila: KelaKoodistokoodiviite
  def opiskeluoikeusPäättynyt = schema.Opiskeluoikeus.OpiskeluoikeudenPäättymistila.koski(tila.koodiarvo)
}

case class OrganisaatioHistoria(
  muutospäivä: LocalDate,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija]
)

trait OpiskeluoikeudenLisätiedot

trait Suoritus{
  def osasuoritukset: Option[List[Osasuoritus]]
  def koulutusmoduuli: SuorituksenKoulutusmoduuli
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
  def withEmptyArvosana: Suoritus
}

trait Osasuoritus{
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
  def withEmptyArvosana: Osasuoritus
}

trait YksilöllistettyOppimäärä {
  def yksilöllistettyOppimäärä: Option[Boolean]
}

trait SuorituksenKoulutusmoduuli

trait OsasuorituksenKoulutusmoduuli

case class KelaKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
)

object KelaKoodistokoodiviite{
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = KelaKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

case class KelaPaikallinenKoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
)

case class Ulkomaanjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  maa: Option[KelaKoodistokoodiviite],
  kuvaus: Option[schema.LocalizedString]
)

case class OsaamisenTunnustaminen(selite: schema.LocalizedString, rahoituksenPiirissä: Boolean)

case class Vahvistus(päivä: LocalDate)

@Title("Osasuorituksen arviointi")
trait OsasuorituksenArviointi{
  @Deprecated("Ei palauteta Kela-API:ssa. Kenttä on näkyvissä skeemassa vain teknisistä syistä.")
  def arvosana: Option[schema.Koodistokoodiviite]
  def hyväksytty: Option[Boolean]
  def päivä: Option[LocalDate]
  def withEmptyArvosana: OsasuorituksenArviointi
}

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[KelaKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[KelaKoodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[KelaKoodistokoodiviite]
)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[KelaKoodistokoodiviite] = None
)

case class KelaLaajuus(arvo: Double, yksikkö: KelaKoodistokoodiviite)

case class KelaAikajakso (
  alku: LocalDate,
  loppu: Option[LocalDate]
) {
  override def toString: String = s"$alku – ${loppu.getOrElse("")}"
}

case class KelaOsaAikaisuusJakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @UnitOfMeasure("%")
  osaAikaisuus: Int
) extends KelaJakso

case class KelaMaksuttomuus(
  alku: LocalDate,
  loppu: Option[LocalDate],
  maksuton: Boolean
) extends KelaJakso

case class KelaTehostetunTuenPäätös(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tukimuodot: Option[List[KelaKoodistokoodiviite]] = None
) extends KelaJakso

trait KelaJakso {
  def alku: LocalDate
  def loppu: Option[LocalDate]
  override def toString: String = s"$alku – ${loppu.getOrElse("")}"
}

case class KelaOikeuttaMaksuttomuuteenPidennetty(
  alku: LocalDate,
  loppu: LocalDate
) {
  override def toString: String = s"$alku – $loppu"
}

@ReadFlattened
case class KelaOsaamisalajakso(
  osaamisala: KelaKoodistokoodiviite,
  alku: Option[LocalDate] = None,
  loppu: Option[LocalDate] = None
)

case class KelaOpiskeluvalmiuksiaTukevienOpintojenJakso(
  alku: LocalDate,
  loppu: LocalDate,
  kuvaus: schema.LocalizedString
)
