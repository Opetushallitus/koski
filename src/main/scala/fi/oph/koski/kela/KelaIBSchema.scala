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
  tila: KelaOpiskeluoikeudenTilaRahoitustiedoilla,
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

@Title("IB-lukion päätason suoritus")
case class KelaIBPäätasonSuoritus(
  koulutusmoduuli: KelaIBSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaIBTutkinnonOppiaineenSuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  theoryOfKnowledge: Option[IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[IBExtendedEssaySuoritus],
  creativityActionService: Option[IBCASSuoritus],
  omanÄidinkielenOpinnot: Option[KelaLukionOmanÄidinkielenOpinnot],
  puhviKoe: Option[KelaPuhviKoe2019],
  suullisenKielitaidonKokeet: Option[List[KelaSuullisenKielitaidonKoe2019]],
) extends Suoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    theoryOfKnowledge = theoryOfKnowledge.map(_.withHyväksyntämerkinnälläKorvattuArvosana),
    extendedEssay = extendedEssay.map(_.withHyväksyntämerkinnälläKorvattuArvosana),
    creativityActionService = creativityActionService.map(_.withHyväksyntämerkinnälläKorvattuArvosana),
    omanÄidinkielenOpinnot = omanÄidinkielenOpinnot.map(_.withHyväksyntämerkinnälläKorvattuArvosana),
    puhviKoe = puhviKoe.map(_.withHyväksyntämerkinnälläKorvattuArvosana),
    suullisenKielitaidonKokeet = suullisenKielitaidonKokeet.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

trait KelaIBTutkinnonOppiaineenSuoritus extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBTutkinnonOppiaineenSuoritus
}

@Title("IB-oppiaineen osasuoritus")
case class KelaIBOppiaineenSuoritus(
  koulutusmoduuli: KelaIBOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArviointi]],
  predictedArviointi: Option[List[KelaIBOsasuorituksenArviointi]],
  osasuoritukset: Option[List[KelaIBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: schema.Koodistokoodiviite,
  tunnustettu: Option[OsaamisenTunnustaminen],
  suoritettuLukiodiplomina: Option[Boolean],
  suoritettuSuullisenaKielikokeena: Option[Boolean]
) extends KelaIBTutkinnonOppiaineenSuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBOppiaineenSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    predictedArviointi = predictedArviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
  )
}

@Title("IB-lukion DP Core -suoritus")
case class KelaIBDPCoreSuoritus(
  koulutusmoduuli: KelaIBOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArviointi]],
  osasuoritukset: Option[List[KelaIBKurssinSuoritus]],
  @KoodistoKoodiarvo("ibcore")
  tyyppi: schema.Koodistokoodiviite,
) extends KelaIBTutkinnonOppiaineenSuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBDPCoreSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
  )
}

@Title("IB-kurssin suoritus")
case class KelaIBKurssinSuoritus(
  koulutusmoduuli: KelaIBOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArviointi]] = None,
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBKurssinSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class KelaIBOsasuorituksenArviointi(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArviointi {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaIBOsasuorituksenArviointi = copy(
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
  oppimäärä: Option[KelaKoodistokoodiviite],
  @Description("Vain extended essay")
  aine: Option[KelaIBAineRyhmäOppiaine],
) extends OsasuorituksenKoulutusmoduuli

case class IBTheoryOfKnowledgeSuoritus(
  koulutusmoduuli: IBTheoryOfKnowledgeSuoritusKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArviointi]] = None,
  osasuoritukset: Option[List[KelaIBOppiaineenSuoritus]],
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
  arviointi: Option[List[KelaIBOsasuorituksenArviointi]] = None,
  tyyppi: KelaKoodistokoodiviite
) {
  def withHyväksyntämerkinnälläKorvattuArvosana: IBExtendedEssaySuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class IBExtendedEssaySuoritusKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  aine: KelaIBAineRyhmäOppiaine,
  pakollinen: Boolean
)

case class KelaIBAineRyhmäOppiaine(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  pakollinen: Boolean,
  kieli: Option[KelaKoodistokoodiviite],
  taso: Option[KelaKoodistokoodiviite],
  ryhmä: KelaKoodistokoodiviite,
) extends OsasuorituksenKoulutusmoduuli

case class IBCASSuoritus(
  koulutusmoduuli: KelaIBSuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArviointi]] = None,
  tyyppi: KelaKoodistokoodiviite,
) {
  def withHyväksyntämerkinnälläKorvattuArvosana: IBCASSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}
