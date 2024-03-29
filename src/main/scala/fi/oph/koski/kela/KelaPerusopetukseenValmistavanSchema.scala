package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Perusopetukseen valmistavan koulutuksen opiskeluoikeus")
@Description("Perusopetukseen valmistavan opetuksen opiskeluoikeuden tiedot")
case class KelaPerusopetukseenValmistavanOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaPerusopetukseenValmistavanPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def arvioituPäättymispäivä = None
  override def lisätiedot: Option[OpiskeluoikeudenLisätiedot] = None
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaPerusopetukseenValmistavanOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

@Title("Perusopetukseen valmistavan koulutuksen suoritus")
case class KelaPerusopetukseenValmistavanPäätasonSuoritus(
  koulutusmoduuli: KelaPerusopetukseenValmistavanSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaPerusopetukseenValmistavanOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  kokonaislaajuus: Option[KelaLaajuus]
) extends Suoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaPerusopetukseenValmistavanPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("Perusopetukseen valmistavan koulutuksen osasuoritus")
case class KelaPerusopetukseenValmistavanOsasuoritus(
  koulutusmoduuli: KelaPerusopetukseenValmistavanOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaPerusopetuksenOsasuorituksenArviointi]],
  tyyppi: schema.Koodistokoodiviite,
  luokkaAste: Option[KelaKoodistokoodiviite],
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaPerusopetukseenValmistavanOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class KelaPerusopetukseenValmistavanSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaPerusopetukseenValmistavanOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite],
) extends OsasuorituksenKoulutusmoduuli
