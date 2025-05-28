package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.scalaschema.annotation.{Description, Discriminator}

import java.time.LocalDate

trait SupaOpiskeluoikeus {
  @Description("Opiskeluoikeuden tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) liittyvät opiskeluoikeudet")
  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: Koodistokoodiviite

  @Description("Opiskeluoikeuden yksilöivä tunniste.")
  def oid: String
  def koulutustoimija: Option[Koulutustoimija]
  def oppilaitos: Option[Oppilaitos]
  @Description("Opiskeluoikeuden tila, joka muodostuu opiskeluoikeusjaksoista")
  def tila: OpiskeluoikeudenTila
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
