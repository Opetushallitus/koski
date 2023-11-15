package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("EB-tutkinnon opiskeluoikeus")
case class KelaEBOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaEBTutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
) extends KelaOpiskeluoikeus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBOpiskeluoikeus = copy(suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana))

  override def withOrganisaatiohistoria: KelaEBOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )

  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
  override def lisätiedot: Option[OpiskeluoikeudenLisätiedot] = None
}

@Title("EB-tutkinnon suoritus")
case class KelaEBTutkinnonSuoritus(
  koulutusmoduuli: KelaEBTutkinto,
  toimipiste: Toimipiste,
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite,
  override val osasuoritukset: Option[List[KelaEBTutkinnonOsasuoritus]]
) extends Suoritus {
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBTutkinnonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("EB-tutkinnon osasuoritus")
case class KelaEBTutkinnonOsasuoritus(
  koulutusmoduuli: KelaESHSecondaryGradeOppiaine,
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[KelaEBOppiaineenAlaosasuoritus]]
) extends Osasuoritus {
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBTutkinnonOsasuoritus = copy(osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)))
}

@Title("EB-oppiaineen alaosasuoritus")
case class KelaEBOppiaineenAlaosasuoritus(
  @Title("Arviointikomponentti")
  koulutusmoduuli: KelaEBOppiaineKomponentti,
  arviointi: Option[List[KelaEBArviointi]],
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus {
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBOppiaineenAlaosasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("EB-oppiainekomponentti")
case class KelaEBOppiaineKomponentti(
  tunniste: KelaKoodistokoodiviite
)

@Title("EB-tutkinnon arviointi")
case class KelaEBArviointi(
  arvosana: Option[schema.Koodistokoodiviite],
  päivä: Option[LocalDate],
  hyväksytty: Option[Boolean]
) extends OsasuorituksenArviointi {
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.EuropeanSchoolOfHelsinkiArviointi.hyväksytty)
  )
}

@Title("EB-tutkinto")
case class KelaEBTutkinto(
  tunniste: KelaKoodistokoodiviite,
  koulutustyyppi: Option[KelaKoodistokoodiviite],
  curriculum: KelaKoodistokoodiviite
) extends SuorituksenKoulutusmoduuli
