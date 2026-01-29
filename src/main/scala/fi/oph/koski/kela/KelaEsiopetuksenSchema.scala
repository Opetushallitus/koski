package fi.oph.koski.kela

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, SensitiveData}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Esiopetuksen opiskeluoikeus")
@Description("Esiopetuksen opiskeluoikeus")
case class KelaEsiopetuksenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaEsiopetuksenSuoritus],
  lisätiedot: Option[KelaEsiopetuksenOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.esiopetus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaEsiopetuksenOpiskeluoikeus = this
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaEsiopetuksenOpiskeluoikeudenLisätiedot(
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  varhennetunOppivelvollisuudenJaksot: Option[List[KelaAikajakso]]
) extends OpiskeluoikeudenLisätiedot

@Title("Esiopetuksen suoritus")
case class KelaEsiopetuksenSuoritus(
  koulutusmoduuli: KelaEsiopetus,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite
) extends Suoritus {
  override def osasuoritukset: Option[List[Osasuoritus]] = None
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaEsiopetuksenSuoritus = this
}

case class KelaEsiopetus(
  tunniste: KelaKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[KelaKoodistokoodiviite]
) extends SuorituksenKoulutusmoduuli
