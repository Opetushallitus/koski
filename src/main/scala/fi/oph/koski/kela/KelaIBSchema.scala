package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("IB-tutkinnon opiskeluoikeus")
@Description("IB-tutkinnon opiskeluoikeus")
case class KelaIBOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaIBPäätasonSuoritus],
  lisätiedot: Option[KelaLukionOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

@Title("IB-tutkinnon suoritus")
case class KelaIBPäätasonSuoritus(
  koulutusmoduuli: KelaIBSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaIBOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  theoryOfKnowledge: Option[IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[IBExtendedEssaySuoritus],
  creativityActionService: Option[IBCASSuoritus]
) extends Suoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    theoryOfKnowledge = theoryOfKnowledge.map(_.withHyväksyntämerkinnälläKorvattuArvosana),
    extendedEssay = extendedEssay.map(_.withHyväksyntämerkinnälläKorvattuArvosana),
    creativityActionService = creativityActionService.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
}

@Title("IB-tutkinnon osasuoritus")
case class KelaIBOsasuoritus(
  koulutusmoduuli: KelaIBOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArvionti]],
  predictedArviointi: Option[List[KelaIBOsasuorituksenArvionti]],
  osasuoritukset: Option[List[KelaIBOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  tunnustettu: Option[OsaamisenTunnustaminen],
  suoritettuLukiodiplomina: Option[Boolean],
  suoritettuSuullisenaKielikokeena: Option[Boolean]
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class KelaIBOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArviointi {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBOsasuorituksenArvionti = copy(
    arvosana = None,
    hyväksytty = arvosana.map(a => schema.IBArviointi.hyväksytty(a) && schema.CoreRequirementsArvionti.hyväksytty(a))
  )
}

case class KelaIBSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  koulutustyyppi: Option[KelaKoodistokoodiviite],
  laajuus: Option[KelaLaajuus],
  pakollinen: Option[Boolean]
) extends SuorituksenKoulutusmoduuli

case class KelaIBOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite],
  taso: Option[KelaKoodistokoodiviite],
  ryhmä: Option[KelaKoodistokoodiviite],
  kurssinTyyppi: Option[KelaKoodistokoodiviite],
  oppimäärä: Option[KelaKoodistokoodiviite]
) extends OsasuorituksenKoulutusmoduuli

case class IBTheoryOfKnowledgeSuoritus(
  koulutusmoduuli: IBTheoryOfKnowledgeSuoritusKoulutusmoduuli,
  tila: Option[KelaKoodistokoodiviite],
  arviointi: Option[List[KelaIBOsasuorituksenArvionti]] = None,
  osasuoritukset: Option[List[KelaIBOsasuoritus]],
  tyyppi: KelaKoodistokoodiviite
) {
  def withHyväksyntämerkinnälläKorvattuArvosana: IBTheoryOfKnowledgeSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class IBTheoryOfKnowledgeSuoritusKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  pakollinen: Boolean
)

case class IBExtendedEssaySuoritus(
  koulutusmoduuli: IBExtendedEssaySuoritusKoulutusmoduuli,
  tila: Option[KelaKoodistokoodiviite],
  arviointi: Option[List[KelaIBOsasuorituksenArvionti]] = None,
  tyyppi: KelaKoodistokoodiviite
) {
  def withHyväksyntämerkinnälläKorvattuArvosana: IBExtendedEssaySuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class IBExtendedEssaySuoritusKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  pakollinen: Boolean
)

case class IBCASSuoritus(
  koulutusmoduuli: KelaIBSuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArvionti]] = None,
  tyyppi: KelaKoodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite]
) {
  def withHyväksyntämerkinnälläKorvattuArvosana: IBCASSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}
