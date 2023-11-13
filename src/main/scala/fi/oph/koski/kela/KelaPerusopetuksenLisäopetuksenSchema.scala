package fi.oph.koski.kela

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, SensitiveData}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Perusopetuksen lisäopetuksen opiskeluoikeus")
@Description("Perusopetuksen lisäopetuksen opiskeluoikeus")
case class KelaPerusopetuksenLisäopetuksenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaPerusopetuksenLisäopetuksenPäätasonSuoritus],
  lisätiedot: Option[KelaPerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def arvioituPäättymispäivä = None
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaPerusopetuksenLisäopetuksenOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaPerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
  sisäoppilaitosmainenMajoitus: Option[List[KelaAikajakso]],
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  koulukoti: Option[List[KelaAikajakso]],
  majoitusetu: Option[KelaAikajakso],
  ulkomailla: Option[KelaAikajakso],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  tehostetunTuenPäätös: Option[KelaTehostetunTuenPäätös],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  tehostetunTuenPäätökset: Option[List[KelaTehostetunTuenPäätös]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  joustavaPerusopetus: Option[KelaAikajakso],
  maksuttomuus: Option[List[KelaMaksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[KelaOikeuttaMaksuttomuuteenPidennetty]]
) extends OpiskeluoikeudenLisätiedot

@Title("Perusopetuksen lisäopetuksen suoritus")
case class KelaPerusopetuksenLisäopetuksenPäätasonSuoritus(
  koulutusmoduuli: KelaPerusopetuksenLisäopetuksenSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaPerusopetuksenLisäopetuksenOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends Suoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaPerusopetuksenLisäopetuksenPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("Perusopetuksen lisäopetuksen osasuoritus")
case class KelaPerusopetuksenLisäopetuksenOsasuoritus(
  koulutusmoduuli: KelaPerusopetuksenLisäopetuksenOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaPerusopetuksenOsasuorituksenArviointi]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  yksilöllistettyOppimäärä: Option[Boolean]
) extends Osasuoritus with YksilöllistettyOppimäärä {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaPerusopetuksenLisäopetuksenOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class KelaPerusopetuksenLisäopetuksenSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaPerusopetuksenLisäopetuksenOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite],
) extends OsasuorituksenKoulutusmoduuli
