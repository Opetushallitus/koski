package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.annotation.{FlattenInUI, KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.{Description, Title}

case class KorkeakoulunOpiskeluoikeus(
  oid: Option[String] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  @Description("Jos tämä on opiskelijan ensisijainen opiskeluoikeus tässä oppilaitoksessa, ilmoitetaan tässä ensisijaisuuden tiedot")
  tila: KorkeakoulunOpiskeluoikeudenTila,
  lisätiedot: Option[KorkeakoulunOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[KorkeakouluSuoritus],
  @KoodistoKoodiarvo("korkeakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulutus", Some("Korkeakoulutus"), "opiskeluoikeudentyyppi", None),
  synteettinen: Boolean = false
) extends Opiskeluoikeus {
  override def versionumero = None
  override def sisältyyOpiskeluoikeuteen = None
}

@Description("Korkeakoulun opiskeluoikeuden lisätiedot")
case class KorkeakoulunOpiskeluoikeudenLisätiedot(
  ensisijaisuus: Option[List[Aikajakso]] = None,
  @Title("Korkeakoulun opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  virtaOpiskeluoikeudenTyyppi: Option[Koodistokoodiviite]
) extends OpiskeluoikeudenLisätiedot {
  def ensisijaisuusVoimassa(d: LocalDate): Boolean = ensisijaisuus.exists(_.exists((j: Aikajakso) => j.contains(d)))
}

trait KorkeakouluSuoritus extends PäätasonSuoritus with MahdollisestiSuorituskielellinen with Toimipisteellinen {
  def toimipiste: Oppilaitos
}

case class KorkeakoulututkinnonSuoritus(
  @Title("Tutkinto")
  koulutusmoduuli: Korkeakoulututkinto,
  toimipiste: Oppilaitos,
  arviointi: Option[List[KorkeakoulunArviointi]],
  vahvistus: Option[Päivämäärävahvistus],
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Tutkintoon kuuluvien opintojaksojen suoritukset")
  @Title("Opintojaksot")
  override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]],
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulututkinto", koodistoUri = "suorituksentyyppi")
) extends KorkeakouluSuoritus {
  override def tarvitseeVahvistuksen = false
}

case class KorkeakoulunOpintojaksonSuoritus(
  @Title("Opintojakso")
  koulutusmoduuli: KorkeakoulunOpintojakso,
  toimipiste: Oppilaitos,
  arviointi: Option[List[KorkeakoulunArviointi]],
  vahvistus: Option[Päivämäärävahvistus],
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Opintojaksoon sisältyvien opintojaksojen suoritukset")
  @Title("Sisältyvät opintojaksot")
  override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]] = None,
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulunopintojakso", koodistoUri = "suorituksentyyppi")
) extends KorkeakouluSuoritus {
  override def tarvitseeVahvistuksen = false
}

@Description("Muut kuin tutkintoon johtavat opiskeluoikeudet, joilla ei ole koulutuskoodia")
case class MuuKorkeakoulunSuoritus (
   @Title("Opiskeluoikeus")
   @FlattenInUI
   koulutusmoduuli: MuuKorkeakoulunOpinto,
   toimipiste: Oppilaitos,
   vahvistus: Option[Päivämäärävahvistus],
   suorituskieli: Option[Koodistokoodiviite],
   override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]],
   @KoodistoKoodiarvo("muukorkeakoulunsuoritus")
   tyyppi: Koodistokoodiviite = Koodistokoodiviite("muukorkeakoulunsuoritus", koodistoUri = "suorituksentyyppi")
 ) extends KorkeakouluSuoritus with Arvioinniton {
}

@Description("Korkeakoulututkinnon tunnistetiedot")
case class Korkeakoulututkinto(
  tunniste: Koodistokoodiviite,
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Koulutus with Tutkinto with Laajuudeton

@Description("Korkeakoulun opintojakson tunnistetiedot")
case class KorkeakoulunOpintojakso(
  tunniste: PaikallinenKoodi,
  nimi: LocalizedString,
  laajuus: Option[LaajuusOpintopisteissä]
) extends Koulutusmoduuli

@Description("Muun korkeakoulun opinnon tunnistetiedot")
case class MuuKorkeakoulunOpinto(
  @Title("Opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  tunniste: Koodistokoodiviite,
  nimi: LocalizedString,
  laajuus: Option[LaajuusOpintopisteissä]
) extends Koulutusmoduuli

case class KorkeakoulunOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KorkeakoulunOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KorkeakoulunOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoUri("virtaopiskeluoikeudentila")
  tila: Koodistokoodiviite
) extends Opiskeluoikeusjakso {
  def opiskeluoikeusPäättynyt = List("3", "4", "5").contains(tila.koodiarvo)
}

trait KorkeakoulunArviointi extends ArviointiPäivämäärällä {
  def hyväksytty = true
}

case class KorkeakoulunKoodistostaLöytyväArviointi(
  @KoodistoUri("virtaarvosana")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate
) extends KoodistostaLöytyväArviointi with KorkeakoulunArviointi {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
}

case class KorkeakoulunPaikallinenArviointi(
  arvosana: PaikallinenKoodi,
  päivä: LocalDate
) extends PaikallinenArviointi with KorkeakoulunArviointi {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
}
