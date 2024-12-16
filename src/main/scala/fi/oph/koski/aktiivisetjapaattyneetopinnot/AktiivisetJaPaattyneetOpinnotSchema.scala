package fi.oph.koski.aktiivisetjapaattyneetopinnot

import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri, Representative}
import fi.oph.scalaschema.annotation.{Description, Discriminator, ReadFlattened, SyntheticProperty, Title}

import java.time.LocalDate

object AktiivisetJaPäättyneetOpinnotOppijaLaajatHenkilötiedot {
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

case class AktiivisetJaPäättyneetOpinnotOppijaLaajatHenkilötiedot(
  henkilö: LaajatOppijaHenkilöTiedot,
  opiskeluoikeudet: List[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]
)

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

  def withKoulutusmoduuli(km: SuorituksenKoulutusmoduuli): Suoritus = {
    import mojave._
    shapeless.lens[Suoritus].field[SuorituksenKoulutusmoduuli]("koulutusmoduuli").set(this)(km)
  }
}

trait AktiivisetJaPäättyneetOpinnotKoodiViite {
  def koodiarvo: String

  def toKoskiSchema: Option[schema.Koodistokoodiviite]
}

@Title("Koodistokoodiviite")
case class AktiivisetJaPäättyneetOpinnotKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
) extends AktiivisetJaPäättyneetOpinnotKoodiViite {
  def toKoskiSchema: Option[schema.Koodistokoodiviite] = {
    koodistoUri match {
      case Some(uri) =>
        Some(schema.Koodistokoodiviite(
          koodiarvo,
          nimi,
          lyhytNimi,
          uri,
          koodistoVersio
        ))
      case _ => None
    }
  }
}

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
) extends AktiivisetJaPäättyneetOpinnotKoodiViite {
  def toKoskiSchema: Option[schema.Koodistokoodiviite] = None
}

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

  def withViitekehykset(koodistoViitePalvelu: KoodistoViitePalvelu): SuorituksenKoulutusmoduuli = this
}

trait SuorituksenKooditettuKoulutusmoduuli extends SuorituksenKoulutusmoduuli {
  def tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

trait ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli extends SuorituksenKooditettuKoulutusmoduuli {
  @KoodistoUri("eqf")
  @Description("Koulutusta vastaava eurooppalainen tutkinnon viitekehystieto, jos tieto on saatavilla")
  def eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite]
  @KoodistoUri("nqf")
  @Description("Koulutusta vastaava kansallinen tutkinnon viitekehystieto, jos tieto on saatavilla")
  def kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]

  override def withViitekehykset(koodistoViitePalvelu: KoodistoViitePalvelu): ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli = {
    import mojave._

    val eqf: Option[schema.Koodistokoodiviite] = haeSisältyväKoodi(koodistoViitePalvelu, "eqf")
    val nqf: Option[schema.Koodistokoodiviite] = haeSisältyväKoodi(koodistoViitePalvelu, "nqf")

    val withEqf = shapeless.lens[ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli].field[Option[schema.Koodistokoodiviite]]("eurooppalainenTutkintojenViitekehysEQF").set(this)(eqf)
    shapeless.lens[ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli].field[Option[schema.Koodistokoodiviite]]("kansallinenTutkintojenViitekehysNQF").set(withEqf)(nqf)
  }

  private def haeSisältyväKoodi(koodistoViitePalvelu: KoodistoViitePalvelu, koodistoUri: String): Option[schema.Koodistokoodiviite] = {
    val koodisto = koodistoViitePalvelu.koodistoPalvelu.getLatestVersionRequired(koodistoUri)
    tunniste.toKoskiSchema match {
      case Some(koodiViite) =>
        val sisältyvä: Option[schema.Koodistokoodiviite] = koodistoViitePalvelu.getSisältyvätKoodiViitteet(koodisto, koodiViite) match {
          case Some(List(viite)) =>
            Some(viite)
          case _ =>
            None
        }
        sisältyvä
      case None => None
    }
  }
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
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli

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
