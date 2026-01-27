package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.scalaschema.annotation.{DefaultValue, Description, Discriminator, Title}

import java.time.{LocalDate, LocalDateTime}

trait SupaPoistettuTaiOlemassaolevaOpiskeluoikeus {
  @Description("Oppijan yksilöivä tunniste, jolla kyseinen opiskeluoikeus on tallennettu Koski-tietovarantoon.")
  def oppijaOid: String
  @Description("Opiskeluoikeuden yksilöivä tunniste.")
  def oid: String
  @Description("Versionumero, joka generoidaan Koski-järjestelmässä. Ensimmäinen tallennettu versio saa versionumeron 1, jonka jälkeen jokainen päivitys aiheuttaa versionumeron noston yhdellä.")
  def versionumero: Option[Int]
  @Description("Koski-palvelimella muodostettu aikaleima opiskeluoikeutta tallennettaessa.")
  def aikaleima: Option[LocalDateTime]
}

@Title("Poistettu opiskeluoikeus")
@Description(
  """Koski-tietovarannosta poistettu opiskeluoikeus.
    |Opiskeluoikeuden tarkempia tietoja tai historiallisia tietoja ei ole saatavilla Koski-tietovarannosta poistamisen jälkeen.
    |Tiedot kaikista oppijan poistetuista opiskeluoikeuksista palautetaan SUPA-rajapinnassa,
    |vaikka mahdollisesti kaikkien poistettujen opiskeluoikeuksien tietoja ei ole palautunut rajapinnasta niiden olemassa olon aikana.
    |""".stripMargin)
case class SupaPoistettuOpiskeluoikeus(
  oppijaOid: String,
  oid: String,
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  @DefaultValue(true)
  @Description("Tässä kentässä palautetaan aina arvo 'true'.")
  poistettu: Boolean = true
) extends SupaPoistettuTaiOlemassaolevaOpiskeluoikeus

case class SupaVirheellinenOpiskeluoikeus(
  oppijaOid: String,
  oid: String,
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  virheet: List[String]
)

trait SupaOpiskeluoikeus extends SupaPoistettuTaiOlemassaolevaOpiskeluoikeus {
  @Description("Opiskeluoikeuden tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) liittyvät opiskeluoikeudet")
  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: Koodistokoodiviite
  def koulutustoimija: Option[Koulutustoimija]
  def oppilaitos: Option[Oppilaitos]
  @Description("Opiskeluoikeuden tila, joka muodostuu opiskeluoikeusjaksoista")
  def tila: OpiskeluoikeudenTila
  @Description("Opiskeluoikeuden alkamispäivä. Tieto poimitaan tila-kentän ensimmäisestä opiskeluoikeusjaksosta.")
  def alkamispäivä: Option[LocalDate]
  @Description("Opiskeluoikeuden päättymispäivä. Tieto poimitaan tila-kentän viimeisestä opiskeluoikeusjaksosta, jos se on opiskeluoikeuden päättävä tila.")
  def päättymispäivä: Option[LocalDate]
  @Description("Opiskeluoikeuteen liittyvien tutkinto- ja muiden suoritusten tiedot")
  def suoritukset: List[SupaSuoritus]
}

trait SupaSuoritus {
  @Description("Suorituksen tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) ja eri tasoihin (tutkinto, tutkinnon osa, kurssi, oppiaine...) liittyvät suoritukset")
  @KoodistoUri("suorituksentyyppi")
  @Discriminator
  def tyyppi: Koodistokoodiviite
  def koulutusmoduuli: Koulutusmoduuli
}

trait SupaVahvistuksellinen {
  def vahvistus: Option[SupaVahvistus]
}

case class SupaVahvistus (
  @Description("Tutkinnon tai tutkinnon osan vahvistettu suorituspäivämäärä, eli päivämäärä jolloin suoritus on hyväksyttyä todennettua osaamista. Muoto YYYY-MM-DD")
  päivä: LocalDate
)
