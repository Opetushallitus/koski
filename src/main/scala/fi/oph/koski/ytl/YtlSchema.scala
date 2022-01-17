package fi.oph.koski.ytl

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.{Discriminator, IgnoreInAnyOfDeserialization, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

object YtlSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[YtlOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] =
    List("lukiokoulutus", "ammatillinenkoulutus", "ibtutkinto", "internationalschool")
}

@Title("Oppija")
case class YtlOppija(
  henkilö: YtlHenkilö,
  opiskeluoikeudet: List[YtlOpiskeluoikeus]
)

@Title("Henkilö")
case class YtlHenkilö(
  oid: String,
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  kutsumanimi: String,
  @KoodistoUri("kieli")
  äidinkieli: Option[schema.Koodistokoodiviite],
  turvakielto: Option[Boolean]
)

object YtlHenkilö {
  def apply(hlö: OppijaHenkilö, äidinkieli: Option[schema.Koodistokoodiviite]): YtlHenkilö = {
    YtlHenkilö(
      oid = hlö.oid,
      hetu = hlö.hetu,
      syntymäaika = hlö.syntymäaika,
      etunimet = hlö.etunimet,
      sukunimi = hlö.sukunimi,
      kutsumanimi = hlö.kutsumanimi,
      äidinkieli = äidinkieli,
      turvakielto = Some(hlö.turvakielto)
    )
  }
}

trait YtlOpiskeluoikeus {
  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
  def oid: Option[String]
  def aikaleima: Option[LocalDateTime]
  def oppilaitos: Option[Oppilaitos]
  def koulutustoimija: Option[Koulutustoimija]
  def tila: OpiskeluoikeudenTila
  def lisätiedot: Option[OpiskeluoikeudenLisätiedot]
  def organisaatiohistoria: Option[List[OrganisaatioHistoria]]
  def alkamispäivä: Option[LocalDate]
  def päättymispäivä: Option[LocalDate]

  // Poistaa deserialisoinnissa poistettaviksi merkityt suoritukset ja koulutusmoduulit jos sellaisia datassa on.
  // TODO: Poistaa myös erityisoppilaitosten tiedot, koska ne ovat sensitiivistä dataa.
  def poistaTiedotJoihinEiKäyttöoikeutta: Option[YtlOpiskeluoikeus]
}

trait RaakaSuoritus {
  @KoodistoUri("suorituksentyyppi")
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
}

trait Suoritus extends RaakaSuoritus {
  def koulutusmoduuli: RaakaSuorituksenKoulutusmoduuli
  def toimipiste: OrganisaatioWithOid
  def vahvistus: Option[Vahvistus]
  @KoodistoUri("kieli")
  def suorituskieli: schema.Koodistokoodiviite
}

trait PoistettavaSuoritus extends RaakaSuoritus

trait RaakaSuorituksenKoulutusmoduuli {
  @Discriminator
  def tunniste: schema.Koodistokoodiviite
}

trait SuorituksenKoulutusmoduuli extends RaakaSuorituksenKoulutusmoduuli
trait PoistettavaSuorituksenKoulutusmoduuli extends RaakaSuorituksenKoulutusmoduuli

@Title("Lukion opiskeluoikeus")
case class YTLLukionOpiskeluoikeus(
  oid: Option[String],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  lisätiedot: Option[OpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("lukiokoulutus")
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppimääräSuoritettu: Option[Boolean]
) extends YtlOpiskeluoikeus {
  def poistaTiedotJoihinEiKäyttöoikeutta: Option[YtlOpiskeluoikeus] = Some(this)
}

@Title("Ammatillinen opiskeluoikeus")
case class YtlAmmatillinenOpiskeluoikeus(
  oid: Option[String],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[RaakaAmmatillinenSuoritus],
  lisätiedot: Option[OpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
) extends YtlOpiskeluoikeus {
  def poistaTiedotJoihinEiKäyttöoikeutta: Option[YtlOpiskeluoikeus] = {
    val uudetSuoritukset = suoritukset.filter {
      case s: PoistettavaAmmatillinenSuoritus => false
      case _ => true
    }

    if (uudetSuoritukset.length > 0) {
      Some(this.copy(suoritukset = uudetSuoritukset))
    } else {
      None
    }
  }
}

trait RaakaAmmatillinenSuoritus extends RaakaSuoritus

@Title("IGNORE")
case class PoistettavaAmmatillinenSuoritus(
  tyyppi: schema.Koodistokoodiviite
) extends RaakaAmmatillinenSuoritus with PoistettavaSuoritus

@Title("Ammatillinen suoritus")
case class AmmatillinenSuoritus(
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: schema.Koodistokoodiviite,
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite
) extends RaakaAmmatillinenSuoritus with Suoritus

case class AmmatillinenTutkintoKoulutus(
  @KoodistoUri("koulutus")
  tunniste: schema.Koodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  @KoodistoUri("koulutustyyppi")
  koulutustyyppi: Option[schema.Koodistokoodiviite]
) extends RaakaSuorituksenKoulutusmoduuli

@Title("IB-tutkinnon opiskeluoikeus")
case class YtlIBOpiskeluoikeus(
  oid: Option[String],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[RaakaIBSuoritus],
  lisätiedot: Option[OpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
) extends YtlOpiskeluoikeus {
  def poistaTiedotJoihinEiKäyttöoikeutta: Option[YtlOpiskeluoikeus] = {
    val uudetSuoritukset = suoritukset.filter {
      case s: PoistettavaIBSuoritus => false
      case _ => true
    }

    if (uudetSuoritukset.length > 0) {
      Some(this.copy(suoritukset = uudetSuoritukset))
    } else {
      None
    }
  }
}

trait RaakaIBSuoritus extends RaakaSuoritus

@Title("IGNORE")
case class PoistettavaIBSuoritus(
  tyyppi: schema.Koodistokoodiviite,
) extends RaakaIBSuoritus with PoistettavaSuoritus

@Title("IB-suoritus")
case class IBSuoritus(
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: schema.Koodistokoodiviite,
  koulutusmoduuli: IBTutkinto,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite
) extends RaakaIBSuoritus with Suoritus

@Title("IB-tutkinto")
case class IBTutkinto(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("301102")
  tunniste: schema.Koodistokoodiviite,
  koulutustyyppi: Option[schema.Koodistokoodiviite]
) extends RaakaSuorituksenKoulutusmoduuli

@Title("International Schoolin opiskeluoikeus")
case class YTLInternationalSchoolOpiskeluoikeus(
  oid: Option[String],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[RaakaInternationalSchoolSuoritus],
  lisätiedot: Option[OpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("internationalschool")
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
) extends YtlOpiskeluoikeus {
  def poistaTiedotJoihinEiKäyttöoikeutta: Option[YtlOpiskeluoikeus] = {
    val uudetSuoritukset = suoritukset.filter {
      case s: PoistettavaInternationalSchoolSuoritus => false
      case s: InternationalSchoolSuoritus if s.koulutusmoduuli.isInstanceOf[PoistettavaInternationalSchoolLuokkaAste] => false
      case _ => true
    }

    if (uudetSuoritukset.length > 0) {
      Some(this.copy(suoritukset = uudetSuoritukset))
    } else {
      None
    }
  }
}

trait RaakaInternationalSchoolSuoritus extends RaakaSuoritus

@Title("IGNORE")
case class PoistettavaInternationalSchoolSuoritus(
  tyyppi: schema.Koodistokoodiviite
) extends RaakaInternationalSchoolSuoritus with PoistettavaSuoritus

@Title("International Schoolin suoritus")
case class InternationalSchoolSuoritus(
  @KoodistoKoodiarvo("internationalschooldiplomavuosiluokka")
  tyyppi: schema.Koodistokoodiviite,
  koulutusmoduuli: RaakaInternationalSchoolLuokkaAste,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite
) extends RaakaInternationalSchoolSuoritus with Suoritus

trait RaakaInternationalSchoolLuokkaAste extends RaakaSuorituksenKoulutusmoduuli

@Title("IGNORE")
case class PoistettavaInternationalSchoolLuokkaAste(
  tunniste: schema.Koodistokoodiviite,
) extends RaakaInternationalSchoolLuokkaAste with PoistettavaSuorituksenKoulutusmoduuli

@Title("International Schoolin luokka-aste")
case class InternationalSchoolLuokkaAste(
  @KoodistoUri("internationalschoolluokkaaste")
  @KoodistoKoodiarvo("12")
  tunniste: schema.Koodistokoodiviite,
  @KoodistoUri("internationalschooldiplomatype")
  @KoodistoKoodiarvo("ib")
  @KoodistoKoodiarvo("ish")
  diplomaType: schema.Koodistokoodiviite
) extends RaakaInternationalSchoolLuokkaAste with SuorituksenKoulutusmoduuli

trait Organisaatio

trait OrganisaatioWithOid extends Organisaatio {
  def oid: String
  def nimi: Option[schema.LocalizedString]
  @KoodistoUri("kunta")
  def kotipaikka: Option[schema.Koodistokoodiviite]
}

@Title("Organisaatio-OID")
case class OidOrganisaatio(
  oid: String,
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[schema.Koodistokoodiviite]
) extends OrganisaatioWithOid

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  @Discriminator
  @Title("Y-tunnus")
  yTunnus: Option[String],
  kotipaikka: Option[schema.Koodistokoodiviite]
) extends OrganisaatioWithOid

case class Oppilaitos(
  oid: String,
  @KoodistoUri("oppilaitosnumero")
  @Discriminator
  oppilaitosnumero: Option[schema.Koodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[schema.Koodistokoodiviite]
) extends OrganisaatioWithOid

@IgnoreInAnyOfDeserialization
case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[schema.Koodistokoodiviite]
) extends OrganisaatioWithOid

case class Yritys(
  nimi: schema.LocalizedString,
  @Discriminator
  @Title("Y-tunnus")
  yTunnus: String
) extends Organisaatio

case class Tutkintotoimikunta(
  nimi: schema.LocalizedString,
  @Discriminator
  tutkintotoimikunnanNumero: String
) extends Organisaatio

case class OpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[Opiskeluoikeusjakso]
)

case class Opiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoUri("koskiopiskeluoikeudentila")
  tila: schema.Koodistokoodiviite
)

case class Vahvistus(
  päivä: LocalDate,
  myöntäjäOrganisaatio: Organisaatio
)

case class OpiskeluoikeudenLisätiedot(
  maksuttomuus: Option[List[schema.Maksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[schema.OikeuttaMaksuttomuuteenPidennetty]]
)

@Title("Organisaatiohistoria")
case class OrganisaatioHistoria(
  muutospäivä: LocalDate,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija]
)
