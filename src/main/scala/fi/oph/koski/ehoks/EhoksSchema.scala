package fi.oph.koski.ehoks

import fi.oph.koski.schema
import fi.oph.koski.schema.LocalizedString
import fi.oph.scalaschema.annotation.{Discriminator, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object EhoksSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[EhoksOppija]).asInstanceOf[ClassSchema])

  val palautettavatOpiskeluoikeudenTyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.tuva.koodiarvo
  )
}

case class EhoksOppija(
  henkilö: EhoksHenkilö,
  opiskeluoikeudet: List[EhoksOpiskeluoikeus]
)

@Title("Henkilö")
case class EhoksHenkilö(oid: String)

trait EhoksOpiskeluoikeus {
  def oid: String
  @Discriminator
  def tyyppi: EhoksKoodi
  def oppilaitos: Option[EhoksOppilaitos]
  def koulutustoimija: Option[EhoksKoulutustoimija]
  def arvioituPäättymispäivä: Option[LocalDate]
  def tila: EhoksOpiskeluoikeudenTila
  def suoritukset: List[EhoksPäätasonSuoritus]

  @SyntheticProperty
  def alkamispäivä: Option[LocalDate] =
    schema.Opiskeluoikeus.alkamispäivä(tyyppi.koodiarvo, tila.opiskeluoikeusjaksot.map(_.alku))

  @SyntheticProperty
  def päättymispäivä: Option[LocalDate] =
    schema.Opiskeluoikeus.päättymispäivä(tyyppi.koodiarvo, tila.opiskeluoikeusjaksot.map(j => (j.alku, j.tila.koodiarvo)))
}

@Title("Ammatillinen opiskeluoikeus")
case class EhoksAmmatillinenOpiskeluoikeus(
  oid: String,
  @EhoksKoodiarvo("ammatillinenkoulutus")
  tyyppi: EhoksKoodi,
  oppilaitos: Option[EhoksOppilaitos],
  koulutustoimija: Option[EhoksKoulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[EhoksSisältäväOpiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: EhoksOpiskeluoikeudenTila,
  suoritukset: List[EhoksAmmatillinenPäätasonSuoritus]
) extends EhoksOpiskeluoikeus

@Title("Tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeus")
case class EhoksTuvaOpiskeluoikeus(
  oid: String,
  @EhoksKoodiarvo("tuva")
  tyyppi: EhoksKoodi,
  oppilaitos: Option[EhoksOppilaitos],
  koulutustoimija: Option[EhoksKoulutustoimija],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: EhoksOpiskeluoikeudenTila,
  suoritukset: List[EhoksTuvaSuoritus]
) extends EhoksOpiskeluoikeus

@Title("Koodi")
case class EhoksKoodi(@Discriminator koodiarvo: String)

@Title("Koodi nimellä")
case class EhoksNimettyKoodi(koodiarvo: String, nimi: Option[LocalizedString])

@Title("Organisaatio")
case class EhoksOrganisaatio(oid: String)

@Title("Oppilaitos")
case class EhoksOppilaitos(oid: String, nimi: Option[LocalizedString])

@Title("Koulutustoimija")
case class EhoksKoulutustoimija(oid: String, nimi: Option[LocalizedString])

@Title("Sisältävä opiskeluoikeus")
case class EhoksSisältäväOpiskeluoikeus(oid: String, oppilaitos: EhoksOrganisaatio)

@Title("Opiskeluoikeuden tila")
case class EhoksOpiskeluoikeudenTila(opiskeluoikeusjaksot: List[EhoksOpiskeluoikeusjakso])

@Title("Opiskeluoikeusjakso")
case class EhoksOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: EhoksKoodi,
  opintojenRahoitus: Option[EhoksKoodi]
)

trait EhoksPäätasonSuoritus {
  @Discriminator
  def tyyppi: EhoksKoodi
  def koulutusmoduuli: EhoksKoulutusmoduuli
  def suorituskieli: Option[EhoksKoodi]
  def toimipiste: Option[EhoksOrganisaatio]
  def vahvistus: Option[EhoksVahvistus]
}

trait EhoksAmmatillinenPäätasonSuoritus extends EhoksPäätasonSuoritus

@Title("Ammatillisen tutkinnon tai näyttötutkintoon valmistavan koulutuksen suoritus")
case class EhoksAmmatillisenTutkinnonSuoritus(
  @EhoksKoodiarvo("ammatillinentutkinto")
  @EhoksKoodiarvo("ammatillinentutkintoosittainen")
  @EhoksKoodiarvo("nayttotutkintoonvalmistavakoulutus")
  tyyppi: EhoksKoodi,
  koulutusmoduuli: EhoksKoulutusmoduuli,
  osaamisala: Option[List[EhoksOsaamisalajakso]],
  tutkintonimike: Option[List[EhoksNimettyKoodi]],
  suorituskieli: Option[EhoksKoodi],
  toimipiste: Option[EhoksOrganisaatio],
  vahvistus: Option[EhoksVahvistus]
) extends EhoksAmmatillinenPäätasonSuoritus

@Title("Muun ammatillisen koulutuksen suoritus")
case class EhoksMuunAmmatillisenKoulutuksenSuoritus(
  @EhoksKoodiarvo("telma")
  @EhoksKoodiarvo("valma")
  @EhoksKoodiarvo("muuammatillinenkoulutus")
  @EhoksKoodiarvo("tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus")
  tyyppi: EhoksKoodi,
  koulutusmoduuli: EhoksKoulutusmoduuli,
  suorituskieli: Option[EhoksKoodi],
  toimipiste: Option[EhoksOrganisaatio],
  vahvistus: Option[EhoksVahvistus]
) extends EhoksAmmatillinenPäätasonSuoritus

@Title("Tutkintokoulutukseen valmentavan koulutuksen suoritus")
case class EhoksTuvaSuoritus(
  @EhoksKoodiarvo("tuvakoulutuksensuoritus")
  tyyppi: EhoksKoodi,
  koulutusmoduuli: EhoksKoulutusmoduuli,
  suorituskieli: Option[EhoksKoodi],
  toimipiste: Option[EhoksOrganisaatio],
  vahvistus: Option[EhoksVahvistus]
) extends EhoksPäätasonSuoritus

@Title("Koulutusmoduuli")
case class EhoksKoulutusmoduuli(tunniste: EhoksNimettyKoodi, perusteenDiaarinumero: Option[String])

@Title("Osaamisalajakso")
case class EhoksOsaamisalajakso(osaamisala: EhoksNimettyKoodi, alku: Option[LocalDate], loppu: Option[LocalDate])

@Title("Vahvistus")
case class EhoksVahvistus(päivä: LocalDate)
