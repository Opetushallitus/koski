package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.scalaschema.annotation.{Description, Title}

case class KorkeakoulunOpiskeluoikeus(
  id: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OidOrganisaatio] = None,
  alkamispäivä: Option[LocalDate] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  @Description("Jos tämä on opiskelijan ensisijainen opiskeluoikeus tässä oppilaitoksessa, ilmoitetaan tässä ensisijaisuuden tiedot.")
  ensisijaisuus: Option[Ensisijaisuus] = None,
  tila: KorkeakoulunOpiskeluoikeudenTila,
  läsnäolotiedot: Option[KorkeakoulunLäsnäolotiedot] = None,
  suoritukset: List[KorkeakouluSuoritus],
  @KoodistoKoodiarvo("korkeakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulutus", Some("Korkeakoulutus"), "opiskeluoikeudentyyppi", None)
) extends Opiskeluoikeus {
  override def withKoulutustoimija(koulutustoimija: OidOrganisaatio) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def versionumero = None
}

@Description("Ensisijaisuustiedot sisältävät alku- ja loppupäivämäärän.")
case class Ensisijaisuus(
  alkamispäivä: LocalDate,
  päättymispäivä: Option[LocalDate]
)

trait KorkeakouluSuoritus extends PäätasonSuoritus {
  def toimipiste: Oppilaitos
}

  case class KorkeakoulututkinnonSuoritus(
    @Title("Tutkinto")
    koulutusmoduuli: Korkeakoulututkinto,
    toimipiste: Oppilaitos,
    tila: Koodistokoodiviite,
    arviointi: Option[List[KorkeakoulunArviointi]],
    vahvistus: Option[Henkilövahvistus],
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
    tila: Koodistokoodiviite,
    arviointi: Option[List[KorkeakoulunArviointi]],
    vahvistus: Option[Henkilövahvistus],
    suorituskieli: Option[Koodistokoodiviite],
    @Description("Opintojaksoon sisältyvien opintojaksojen suoritukset")
    @Title("Sisältyvät opintojaksot")
    override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]] = None,
    @KoodistoKoodiarvo("korkeakoulunopintojakso")
    tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulunopintojakso", koodistoUri = "suorituksentyyppi")
  ) extends KorkeakouluSuoritus {
    override def tarvitseeVahvistuksen = false
  }

@Description("Korkeakoulututkinnon tunnistetiedot")
case class Korkeakoulututkinto(
  tunniste: Koodistokoodiviite
) extends Koulutus  {
  override def laajuus = None
  override def isTutkinto = true
}

@Description("Korkeakoulun opintojakson tunnistetiedot")
case class KorkeakoulunOpintojakso(
  tunniste: PaikallinenKoodi,
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

case class LaajuusOpintopisteissä(
  arvo: Float,
  @KoodistoKoodiarvo("2")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("2", Some(finnish("opintopistettä")), "opintojenlaajuusyksikko")
) extends Laajuus

case class KorkeakoulunLäsnäolotiedot(
  läsnäolojaksot: List[KorkeakoulunLäsnäolojakso]
) extends Läsnäolotiedot

case class KorkeakoulunLäsnäolojakso(
  alku: LocalDate,
  @KoodistoUri("virtalukukausiilmtila")
  tila: Koodistokoodiviite
) extends Läsnäolojakso