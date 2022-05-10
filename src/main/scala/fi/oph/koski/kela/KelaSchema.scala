package fi.oph.koski.kela

import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.scalaschema.annotation.{Discriminator, SyntheticProperty}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.{LocalDate, LocalDateTime}

object KelaSchema {
  lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[KelaOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
  "aikuistenperusopetus",
  "ammatillinenkoulutus",
  "ibtutkinto",
  "diatutkinto",
  "internationalschool",
  "lukiokoulutus",
  "luva",
  "perusopetukseenvalmistavaopetus",
  "perusopetuksenlisaopetus",
  "perusopetus",
  "ylioppilastutkinto",
  "vapaansivistystyonkoulutus",
//  "tuva"
  )

  val schemassaEiTuetutSuoritustyypit: List[String] = List("vstmaahanmuuttajienkotoutumiskoulutus")
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
  def sisältyyOpiskeluoikeuteen: Option[Sisältäväopiskeluoikeus]
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

  def withEmptyArvosana: KelaOpiskeluoikeus
}

case class Sisältäväopiskeluoikeus(
  oid: String,
  oppilaitos: Oppilaitos
)

case class KelaOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

trait OpiskeluoikeudenTila {
  def opiskeluoikeusjaksot: List[Opiskeluoikeusjakso]
}

case class KelaOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: KelaKoodistokoodiviite,
) extends Opiskeluoikeusjakso

trait Opiskeluoikeusjakso {
  def alku: LocalDate
  def tila: KelaKoodistokoodiviite
  def opiskeluoikeusPäättynyt = schema.KoskiOpiskeluoikeusjakso.päätöstilat.contains(tila.koodiarvo) || tila.koodiarvo == "mitatoity"
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
}

trait Osasuoritus{
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
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

case class Ulkomaanjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  maa: Option[KelaKoodistokoodiviite],
  kuvaus: Option[schema.LocalizedString]
)

case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[schema.LocalizedString],
  paikkakunta: KelaKoodistokoodiviite,
  maa: KelaKoodistokoodiviite
)

case class Työssäoppimisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[schema.LocalizedString],
  paikkakunta: KelaKoodistokoodiviite,
  maa: KelaKoodistokoodiviite,
  laajuus: KelaLaajuus
)

case class Järjestämismuoto (
  tunniste: KelaKoodistokoodiviite
)

case class Järjestämismuotojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  järjestämismuoto: Järjestämismuoto
)

case class Oppisopimus(
  työnantaja: Yritys
)

case class Yritys(
  nimi: schema.LocalizedString,
  yTunnus: String
)

trait OsaamisenHankkimistapa {
  def tunniste: KelaKoodistokoodiviite
}

case class OsaamisenHankkimistapaIlmanLisätietoja (
  tunniste: KelaKoodistokoodiviite
) extends OsaamisenHankkimistapa

case class OppisopimuksellinenOsaamisenHankkimistapa (
  tunniste: KelaKoodistokoodiviite,
  oppisopimus: Oppisopimus
) extends OsaamisenHankkimistapa

case class OsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaamisenHankkimistapa: OsaamisenHankkimistapa
)

trait OsaamisenTunnustaminen{
  def osaaminen: Option[Osasuoritus]
  def selite: schema.LocalizedString
  def rahoituksenPiirissä: Boolean
}

case class Vahvistus(
  päivä: LocalDate
)

trait OsasuorituksenArvionti{
  def arvosana: Option[schema.Koodistokoodiviite]
  def hyväksytty: Option[Boolean]
  def päivä: Option[LocalDate]
}

case class KelaOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArvionti

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

case class KelaLaajuus(
  arvo: Double,
  yksikkö: KelaKoodistokoodiviite
)
