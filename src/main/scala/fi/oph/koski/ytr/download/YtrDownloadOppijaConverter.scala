package fi.oph.koski.ytr.download

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{YlioppilasTutkinnonKoe, YlioppilastutkinnonKokeenSuoritus, YlioppilastutkinnonOpiskeluoikeudenLisätiedot, YlioppilastutkinnonOpiskeluoikeudenTila, YlioppilastutkinnonOpiskeluoikeus, YlioppilastutkinnonSuoritus, YlioppilastutkinnonTutkintokerranLisätiedot, YlioppilastutkinnonTutkintokerta, YlioppilastutkinnonTutkintokokonaisuudenLisätiedot}
import fi.oph.koski.ytr.YtrConversionUtils

class YtrDownloadOppijaConverter(
  koodistoViitePalvelu: KoodistoViitePalvelu,
  organisaatioRepository: OrganisaatioRepository,
  localizations: LocalizationRepository
) extends Logging {
  private val conversionUtils = new YtrConversionUtils(localizations, koodistoViitePalvelu, organisaatioRepository)

  private val ytl = conversionUtils.ytl

  private def getTutkintokokonaisuudenTunniste(indexOfExamination: Int) = indexOfExamination

  def convertOppijastaOpiskeluoikeus(ytrLaajaOppija: YtrLaajaOppija): Option[YlioppilastutkinnonOpiskeluoikeus] = {
    val certificateSchoolAndDate = (ytrLaajaOppija.certificateSchoolOphOid zip ytrLaajaOppija.certificateDate).headOption
    Some(YlioppilastutkinnonOpiskeluoikeus(
      lähdejärjestelmänId = None,
      oppilaitos = conversionUtils.ytlOppilaitos,
      koulutustoimija = Some(ytl),
      tila = YlioppilastutkinnonOpiskeluoikeudenTila(opiskeluoikeusjaksot = List()),
      oppilaitosSuorituspäivänä = certificateSchoolAndDate.flatMap {
        case (oid, date) => organisaatioRepository.getOppilaitosHetkellä(oid, date)
      },
      lisätiedot = Some(YlioppilastutkinnonOpiskeluoikeudenLisätiedot(
        tutkintokokonaisuudet = Some(ytrLaajaOppija.examinations.zipWithIndex
          .map { case (examination, index) => YlioppilastutkinnonTutkintokokonaisuudenLisätiedot(
            tunniste = getTutkintokokonaisuudenTunniste(index),
            tyyppi = Some(conversionUtils.requiredKoodi("ytrtutkintokokonaisuudentyyppi", examination.examinationType)),
            tila = examination.examinationState.map(x => conversionUtils.requiredKoodi("ytrtutkintokokonaisuudentila", x)),
            suorituskieli = Some(conversionUtils.requiredKoodi("kieli", examination.language.toUpperCase)),
            tutkintokerrat = examination.examinationPeriods.map(period => YlioppilastutkinnonTutkintokerranLisätiedot(
              tutkintokerta = conversionUtils.convertTutkintokerta(period.examinationPeriod),
              koulutustausta = period.education.map(x => conversionUtils.requiredKoodi("ytrkoulutustausta", x.toString)),
              oppilaitos = period.schoolOid.flatMap(oid =>
                organisaatioRepository.getOppilaitosHetkellä(
                  oid,
                  conversionUtils.convertTutkintokertaToDate(period.examinationPeriod)
                )
              )
            ))
          )}
        )
      )),
      suoritukset = List(
        YlioppilastutkinnonSuoritus(
          toimipiste = ytl,
          vahvistus = ytrLaajaOppija.graduationDate.map(
            graduationDate => conversionUtils.convertVahvistus(graduationDate)
          ),
          pakollisetKokeetSuoritettu = ytrLaajaOppija.hasCompletedMandatoryExams.getOrElse(false),
          osasuoritukset = Some(ytrLaajaOppija.examinations.zipWithIndex
            .flatMap { case (examination, index) => examination.examinationPeriods
              .flatMap(period => period.exams
                .map(exam => YlioppilastutkinnonKokeenSuoritus(
                  koulutusmoduuli = YlioppilasTutkinnonKoe(
                    tunniste = conversionUtils.requiredKoodi("koskiyokokeet", exam.examId)
                  ),
                  tutkintokerta = conversionUtils.convertTutkintokerta(period.examinationPeriod),
                  tutkintokokonaisuudenTunniste = Some(getTutkintokokonaisuudenTunniste(index)),
                  arviointi = exam.grade.map(grade =>
                    List(conversionUtils.convertArviointi(grade, exam.gradePoints))
                  ),
                  keskeytynyt = exam.aborted,
                  maksuton = exam.freeOfCharge
                ))
              )
            }
          )
        )
      )
    ))
  }
}
