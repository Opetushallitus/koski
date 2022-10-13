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
  // TODO: TOR-1685 Eurooppalainen koulu
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
  turvakielto: Option[Boolean],
  pääoppijaOid: Option[String]
)

object YtlHenkilö {
  def apply(hlö: OppijaHenkilö, pääoppijaOid: Option[String], äidinkieli: Option[schema.Koodistokoodiviite]): YtlHenkilö = {
    YtlHenkilö(
      oid = hlö.oid,
      hetu = hlö.hetu,
      syntymäaika = hlö.syntymäaika,
      etunimet = hlö.etunimet,
      sukunimi = hlö.sukunimi,
      kutsumanimi = hlö.kutsumanimi,
      äidinkieli = äidinkieli,
      turvakielto = Some(hlö.turvakielto),
      pääoppijaOid = pääoppijaOid
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
  def suoritukset: List[Suoritus]
  def lisätiedot: Option[OpiskeluoikeudenLisätiedot]
  def organisaatiohistoria: Option[List[OrganisaatioHistoria]]
  def alkamispäivä: Option[LocalDate]
  def päättymispäivä: Option[LocalDate]

  def siivoaTiedot(poistaOrganisaatiotiedot: Boolean = false): Option[YtlOpiskeluoikeus]

  def kaikkiMahdollisetOppilaitosOiditRakenteessa: Seq[String] =
    oppilaitosOidit ++ organisaatiohistorianOppilaitosOidit

  protected def oppilaitosOidit: Seq[String] =
    oppilaitos.map(_.oid).toSeq

  protected def organisaatiohistorianOppilaitosOidit: Seq[String] =
    this.organisaatiohistoria.map(_.map(_.oppilaitos.map(_.oid))).toSeq.flatten.flatten
}

trait LaajatSuoritustiedotSisältäväYtlOpiskeluoikeus extends YtlOpiskeluoikeus {
  def suoritukset: List[SuoritusLaajatTiedot]

  override def kaikkiMahdollisetOppilaitosOiditRakenteessa: Seq[String] =
    super.kaikkiMahdollisetOppilaitosOiditRakenteessa ++ suoritustenToimipisteet ++ suoritustenVahvistustenOrganisaatiot

  private def suoritustenToimipisteet =
    this.suoritukset.flatMap(_.toimipiste.map(_.oid))

  private def suoritustenVahvistustenOrganisaatiot =
    this.suoritukset.flatMap(_.vahvistus.flatMap(_.myöntäjäOrganisaatio.flatMap {
    case o: OrganisaatioWithOid => Some(o.oid)
    case _ => None
  }))
}

trait Suoritus {
  @KoodistoUri("suorituksentyyppi")
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
}

trait SuoritusLaajatTiedot extends Suoritus {
  def koulutusmoduuli: SuorituksenKoulutusmoduuli
  def toimipiste: Option[OrganisaatioWithOid]
  def vahvistus: Option[Vahvistus]
  @KoodistoUri("kieli")
  def suorituskieli: schema.Koodistokoodiviite
}

trait SuorituksenKoulutusmoduuli {
  @Discriminator
  def tunniste: schema.Koodistokoodiviite
}

@Title("Lukion opiskeluoikeus")
case class YTLLukionOpiskeluoikeus(
  oid: Option[String],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[LukionSuoritus],
  lisätiedot: Option[OpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("lukiokoulutus")
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppimääräSuoritettu: Option[Boolean]
) extends YtlOpiskeluoikeus {
  def siivoaTiedot(poistaOrganisaatiotiedot: Boolean = false): Option[YtlOpiskeluoikeus] = {
    if (poistaOrganisaatiotiedot) {
      Some(this.copy(
        oppilaitos = None,
        koulutustoimija = None,
        organisaatiohistoria = None
      ))
    } else {
      Some(this)
    }
  }
}

@Title("Lukion suoritus")
case class LukionSuoritus(
  @KoodistoKoodiarvo("lukionoppiaineenoppimaara")
  @KoodistoKoodiarvo("lukionaineopinnot")
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: schema.Koodistokoodiviite
) extends Suoritus

@Title("Ammatillinen opiskeluoikeus")
case class YtlAmmatillinenOpiskeluoikeus(
  oid: Option[String],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[AmmatillinenSuoritus],
  lisätiedot: Option[OpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
) extends LaajatSuoritustiedotSisältäväYtlOpiskeluoikeus {
  def siivoaTiedot(poistaOrganisaatiotiedot: Boolean = false): Option[YtlOpiskeluoikeus] = {
    (suoritukset.isEmpty, poistaOrganisaatiotiedot) match {
      case (true, _) => None
      case (_, true) => Some(this.copy(
        oppilaitos = None,
        koulutustoimija = None,
        organisaatiohistoria = None,
        suoritukset = this.suoritukset.map(s => s.copy(
          toimipiste = None,
          vahvistus = s.vahvistus.map(v => v.copy(myöntäjäOrganisaatio = None)))
        )
      ))
      case _ => Some(this)
    }
  }
}

@Title("Ammatillinen suoritus")
case class AmmatillinenSuoritus(
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: schema.Koodistokoodiviite,
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  toimipiste: Option[OrganisaatioWithOid],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite
) extends SuoritusLaajatTiedot

case class AmmatillinenTutkintoKoulutus(
  @KoodistoUri("koulutus")
  tunniste: schema.Koodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  @KoodistoUri("koulutustyyppi")
  koulutustyyppi: Option[schema.Koodistokoodiviite]
) extends SuorituksenKoulutusmoduuli

@Title("IB-tutkinnon opiskeluoikeus")
case class YtlIBOpiskeluoikeus(
  oid: Option[String],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[IBSuoritus],
  lisätiedot: Option[OpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
) extends LaajatSuoritustiedotSisältäväYtlOpiskeluoikeus {
  def siivoaTiedot(poistaOrganisaatiotiedot: Boolean = false): Option[YtlOpiskeluoikeus] = {
    (suoritukset.isEmpty, poistaOrganisaatiotiedot) match {
      case (true, _) => None
      case (_, true) => Some(this.copy(
        oppilaitos = None,
        koulutustoimija = None,
        organisaatiohistoria = None,
        suoritukset = this.suoritukset.map(s => s.copy(
          toimipiste = None,
          vahvistus = s.vahvistus.map(v => v.copy(myöntäjäOrganisaatio = None)))
        )
      ))
      case _ => Some(this)
    }
  }
}

@Title("IB-suoritus")
case class IBSuoritus(
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: schema.Koodistokoodiviite,
  koulutusmoduuli: IBTutkinto,
  toimipiste: Option[OrganisaatioWithOid],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite
) extends SuoritusLaajatTiedot

@Title("IB-tutkinto")
case class IBTutkinto(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("301102")
  tunniste: schema.Koodistokoodiviite,
  koulutustyyppi: Option[schema.Koodistokoodiviite]
) extends SuorituksenKoulutusmoduuli

@Title("International Schoolin opiskeluoikeus")
case class YTLInternationalSchoolOpiskeluoikeus(
  oid: Option[String],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[InternationalSchoolSuoritus],
  lisätiedot: Option[OpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("internationalschool")
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
) extends LaajatSuoritustiedotSisältäväYtlOpiskeluoikeus {
  def siivoaTiedot(poistaOrganisaatiotiedot: Boolean = false): Option[YtlOpiskeluoikeus] = {
    (suoritukset.isEmpty, poistaOrganisaatiotiedot) match {
      case (true, _) => None
      case (_, true) => Some(this.copy(
        oppilaitos = None,
        koulutustoimija = None,
        organisaatiohistoria = None,
        suoritukset = this.suoritukset.map(s => s.copy(
          toimipiste = None,
          vahvistus = s.vahvistus.map(v => v.copy(myöntäjäOrganisaatio = None)))
        )
      ))
      case _ => Some(this)
    }
  }
}

@Title("International Schoolin suoritus")
case class InternationalSchoolSuoritus(
  @KoodistoKoodiarvo("internationalschooldiplomavuosiluokka")
  tyyppi: schema.Koodistokoodiviite,
  koulutusmoduuli: InternationalSchoolLuokkaAste,
  toimipiste: Option[OrganisaatioWithOid],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite
) extends SuoritusLaajatTiedot

@Title("International Schoolin luokka-aste")
case class InternationalSchoolLuokkaAste(
  @KoodistoUri("internationalschoolluokkaaste")
  @KoodistoKoodiarvo("12")
  tunniste: schema.Koodistokoodiviite,
  @KoodistoUri("internationalschooldiplomatype")
  @KoodistoKoodiarvo("ib")
  @KoodistoKoodiarvo("ish")
  diplomaType: schema.Koodistokoodiviite
) extends SuorituksenKoulutusmoduuli

trait Organisaatio

trait OrganisaatioWithOid extends Organisaatio {
  @Discriminator
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
  myöntäjäOrganisaatio: Option[Organisaatio]
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
