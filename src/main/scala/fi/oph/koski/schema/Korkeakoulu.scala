package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.scalaschema.annotation.Description

case class KorkeakoulunOpiskeluoikeus(
  id: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  suoritukset: List[KorkeakouluSuoritus],
  tila: KorkeakoulunOpiskeluoikeudenTila,
  läsnäolotiedot: Option[KorkeakoulunLäsnäolotiedot],
  @KoodistoKoodiarvo("korkeakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulutus", Some("Korkeakoulutus"), "opiskeluoikeudentyyppi", None),
  @Description("Jos tämä on opiskelijan ensisijainen opiskeluoikeus tässä oppilaitoksessa, ilmoitetaan tässä ensisijaisuuden tiedot.")
  ensisijaisuus: Option[Ensisijaisuus] = None
) extends Opiskeluoikeus {
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def versionumero = None
}

@Description("Ensisijaisuustiedot sisältävät alku- ja loppupäivämäärän.")
case class Ensisijaisuus(
  alkamispäivä: LocalDate,
  päättymispäivä: Option[LocalDate]
)

trait KorkeakouluSuoritus extends Suoritus {
  def toimipiste: Oppilaitos
}

  case class KorkeakoulututkinnonSuoritus(
    koulutusmoduuli: Korkeakoulututkinto,
    @KoodistoKoodiarvo("korkeakoulututkinto")
    tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulututkinto", koodistoUri = "suorituksentyyppi"),
    arviointi: Option[List[KorkeakoulunArviointi]],
    tila: Koodistokoodiviite,
    vahvistus: Option[Henkilövahvistus],
    suorituskieli: Option[Koodistokoodiviite],
    toimipiste: Oppilaitos,
    @Description("Tutkintoon kuuluvien opintojaksojen suoritukset")
    @Title("Opintojaksot")
    override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]]
  ) extends KorkeakouluSuoritus {
    override def tarvitseeVahvistuksen = false
  }

  case class KorkeakoulunOpintojaksonSuoritus(
    koulutusmoduuli: KorkeakoulunOpintojakso,
    @KoodistoKoodiarvo("korkeakoulunopintojakso")
    tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulunopintojakso", koodistoUri = "suorituksentyyppi"),
    arviointi: Option[List[KorkeakoulunArviointi]],
    tila: Koodistokoodiviite,
    vahvistus: Option[Henkilövahvistus],
    suorituskieli: Option[Koodistokoodiviite],
    toimipiste: Oppilaitos,
    @Description("Opintojaksoon sisältyvien opintojaksojen suoritukset")
    @Title("Sisältyvät opintojaksot")
    override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]] = None
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