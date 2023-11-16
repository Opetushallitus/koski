package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("International school opiskeluoikeus")
@Description("International school opiskeluoikeus")
case class KelaInternationalSchoolOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaInternationalSchoolPäätasonSuoritus],
  lisätiedot: Option[KelaInternationalSchoolOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaInternationalSchoolOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaInternationalSchoolOpiskeluoikeudenLisätiedot(
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  maksuttomuus: Option[List[KelaMaksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[KelaOikeuttaMaksuttomuuteenPidennetty]]
) extends OpiskeluoikeudenLisätiedot

@Title("International school suoritus")
case class KelaInternationalSchoolPäätasonSuoritus(
  koulutusmoduuli: KelaInternationalSchoolSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaInternationalSchoolOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  alkamispäivä: Option[LocalDate]
) extends Suoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaInternationalSchoolPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("International school osasuoritus")
case class KelaInternationalSchoolOsasuoritus(
  koulutusmoduuli: KelaInternationalSchoolOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaInternationalSchoolOsasuorituksenArvionti]],
  tyyppi: schema.Koodistokoodiviite,
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaInternationalSchoolOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class KelaInternationalSchoolOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArviointi {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaInternationalSchoolOsasuorituksenArvionti = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.InternationalSchoolArviointi.hyväksytty)
  )
}

case class KelaInternationalSchoolSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  diplomaType: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaInternationalSchoolOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  kieli: Option[KelaKoodistokoodiviite],
  taso: Option[KelaKoodistokoodiviite],
) extends OsasuorituksenKoulutusmoduuli
