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
  organisaatioHistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withEmptyArvosana: KelaIBOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
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
  def withEmptyArvosana: KelaIBPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana)),
    theoryOfKnowledge = theoryOfKnowledge.map(_.withEmptyArvosana),
    extendedEssay = extendedEssay.map(_.withEmptyArvosana),
    creativityActionService = creativityActionService.map(_.withEmptyArvosana)
  )
}

trait KelaIBOsasuoritus extends Osasuoritus {
  def withEmptyArvosana: KelaIBOsasuoritus
}

@Title("IB-tutkinnon osasuoritus")
case class KelaIBMuuOsasuoritus(
  koulutusmoduuli: KelaIBOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArvionti]],
  osasuoritukset: Option[List[KelaIBMuuOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  tunnustettu: Option[OsaamisenTunnustaminen],
  suoritettuLukiodiplomina: Option[Boolean],
  suoritettuSuullisenaKielikokeena: Option[Boolean]
) extends KelaIBOsasuoritus {
  def withEmptyArvosana: KelaIBMuuOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("IB-tutkinnon lukion oppiaineen tai muiden opintojen osasuoritus")
case class KelaIBLukionOppiaineenOsasuoritus(
  koulutusmoduuli: KelaIBOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArvionti]],
  osasuoritukset: Option[List[KelaPreIBLukioOsasuoritus]],
  @KoodistoKoodiarvo("lukionoppiaine")
  @KoodistoKoodiarvo("lukionmuuopinto")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  tunnustettu: Option[OsaamisenTunnustaminen],
  suoritettuLukiodiplomina: Option[Boolean],
  suoritettuSuullisenaKielikokeena: Option[Boolean]
) extends KelaIBOsasuoritus {
  def withEmptyArvosana: KelaIBLukionOppiaineenOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Lukion moduulin suoritus")
case class KelaIBOsasuorituksenLukionModuulinSuoritus(
  koulutusmoduuli: KelaIBOsasuorituksenOsasuorituksenKieletönKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArvionti]],
  @KoodistoKoodiarvo("lukionvaltakunnallinenmoduuli")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  tunnustettu: Option[OsaamisenTunnustaminen],
) extends Osasuoritus with KelaPreIBLukioOsasuoritus {
  def withEmptyArvosana: KelaIBOsasuorituksenLukionModuulinSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Lukion muun opintojakson suoritus")
case class KelaIBOsasuorituksenLukionMuunOpintojaksonSuoritus(
  koulutusmoduuli: KelaIBOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaIBOsasuorituksenArvionti]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  tunnustettu: Option[OsaamisenTunnustaminen],
) extends Osasuoritus with KelaPreIBLukioOsasuoritus {
  def withEmptyArvosana: KelaIBOsasuorituksenLukionMuunOpintojaksonSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana))
  )
}

trait KelaPreIBLukioOsasuoritus {
  def withEmptyArvosana: KelaPreIBLukioOsasuoritus
}

case class KelaIBOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArvionti {
  def withEmptyArvosana: KelaIBOsasuorituksenArvionti = copy(
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

case class KelaIBOsasuorituksenOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite]
) extends OsasuorituksenKoulutusmoduuli

case class KelaIBOsasuorituksenOsasuorituksenKieletönKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  pakollinen: Option[Boolean],
) extends OsasuorituksenKoulutusmoduuli

case class IBTheoryOfKnowledgeSuoritus(
  koulutusmoduuli: IBTheoryOfKnowledgeSuoritusKoulutusmoduuli,
  tila: Option[KelaKoodistokoodiviite],
  arviointi: Option[List[KelaIBOsasuorituksenArvionti]] = None,
  osasuoritukset: Option[List[KelaIBMuuOsasuoritus]],
  tyyppi: KelaKoodistokoodiviite
) {
  def withEmptyArvosana: IBTheoryOfKnowledgeSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
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
  def withEmptyArvosana: IBExtendedEssaySuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana))
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
  def withEmptyArvosana: IBCASSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana))
  )
}
