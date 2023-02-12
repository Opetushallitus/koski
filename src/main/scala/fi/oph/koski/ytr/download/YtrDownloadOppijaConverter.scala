package fi.oph.koski.ytr.download

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{YlioppilasTutkinnonKoe, YlioppilastutkinnonKokeenSuoritus, YlioppilastutkinnonOpiskeluoikeudenTila, YlioppilastutkinnonOpiskeluoikeus, YlioppilastutkinnonSuoritus}
import fi.oph.koski.ytr.YtrConversionUtils

class YtrDownloadOppijaConverter(
  koodistoViitePalvelu: KoodistoViitePalvelu,
  organisaatioRepository: OrganisaatioRepository,
  localizations: LocalizationRepository
) extends Logging {
  private val conversionUtils = new YtrConversionUtils(localizations, koodistoViitePalvelu, organisaatioRepository)

  private val ytl = conversionUtils.ytl

  def convertOppijastaOpiskeluoikeus(ytrLaajaOppija: YtrLaajaOppija): Option[YlioppilastutkinnonOpiskeluoikeus] = {
    Some(YlioppilastutkinnonOpiskeluoikeus(
      lähdejärjestelmänId = None,
      oppilaitos = None,
      koulutustoimija = Some(ytl),
      tila = YlioppilastutkinnonOpiskeluoikeudenTila(opiskeluoikeusjaksot = List()),
      suoritukset = List(
        YlioppilastutkinnonSuoritus(
          toimipiste = ytl,
          vahvistus = ytrLaajaOppija.graduationDate.map(
            graduationDate => conversionUtils.convertVahvistus(graduationDate)
          ),
          pakollisetKokeetSuoritettu = ytrLaajaOppija.hasCompletedMandatoryExams.getOrElse(false),
          osasuoritukset = Some(ytrLaajaOppija.examinations
            .flatMap(examination => examination.examinationPeriods
              .flatMap(period => period.exams
                .map(exam => YlioppilastutkinnonKokeenSuoritus(
                  koulutusmoduuli = YlioppilasTutkinnonKoe(
                    tunniste = conversionUtils.requiredKoodi("koskiyokokeet", exam.examId)
                  ),
                  tutkintokerta = conversionUtils.convertTutkintokerta(period.examinationPeriod),
                  arviointi = exam.grade.map(grade =>
                    List(conversionUtils.convertArviointi(grade, exam.gradePoints))
                  ),
                ))
              )
            )
          )
        )
      )
    ))
  }
}
