package fi.oph.koski.ytr.download

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{Koodistokoodiviite, KoskiSchema, YlioppilasTutkinnonKoe, YlioppilastutkinnonKokeenSuoritus, YlioppilastutkinnonOpiskeluoikeudenLisätiedot, YlioppilastutkinnonOpiskeluoikeudenTila, YlioppilastutkinnonOpiskeluoikeus, YlioppilastutkinnonOpiskeluoikeusjakso, YlioppilastutkinnonSisältyväKoe, YlioppilastutkinnonSuoritus, YlioppilastutkinnonTutkintokerranLisätiedot, YlioppilastutkinnonTutkintokerta, YlioppilastutkinnonTutkintokokonaisuudenLisätiedot, Ylioppilastutkinto}
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import fi.oph.koski.ytr.YtrConversionUtils
import fi.oph.scalaschema.{SerializationContext, Serializer}

import java.time.LocalDate

class YtrDownloadOppijaConverter(
  koodistoViitePalvelu: KoodistoViitePalvelu,
  organisaatioRepository: OrganisaatioRepository,
  localizations: LocalizationRepository,
  validatingAndResolvingExtractor: ValidatingAndResolvingExtractor
) extends Logging {
  private val conversionUtils = new YtrConversionUtils(localizations, koodistoViitePalvelu, organisaatioRepository)

  private val ytl = conversionUtils.ytl

  private val serializationContext = SerializationContext(KoskiSchema.schemaFactory)

  private val deserializationContext = KoskiSchema.strictDeserialization

  private def getTutkintokokonaisuudenTunniste(indexOfExamination: Int) = indexOfExamination

  def convertOppijastaOpiskeluoikeus(ytrLaajaOppija: YtrLaajaOppija): Option[YlioppilastutkinnonOpiskeluoikeus] = {
    val certificateSchoolAndDate = (ytrLaajaOppija.certificateSchoolOphOid zip ytrLaajaOppija.certificateDate).headOption

    val raakaOpiskeluoikeus =
      YlioppilastutkinnonOpiskeluoikeus(
        lähdejärjestelmänId = None,
        oppilaitos = None,
        koulutustoimija = Some(ytl),
        tila = YlioppilastutkinnonOpiskeluoikeudenTila(opiskeluoikeusjaksot = ytrLaajaOppija.graduationDate match {
          case gd: Some[LocalDate] => List(YlioppilastutkinnonOpiskeluoikeusjakso(
            alku = gd.get,
            tila = Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila")
          ))
          case _ => List()
        }),
        oppilaitosSuorituspäivänä = certificateSchoolAndDate.flatMap {
          case (oid, date) => organisaatioRepository.getOppilaitosHetkellä(oid, date)
        },
        lisätiedot = Some(YlioppilastutkinnonOpiskeluoikeudenLisätiedot(
          tutkintokokonaisuudet = Some(ytrLaajaOppija.examinations.zipWithIndex
            .map { case (examination, index) => YlioppilastutkinnonTutkintokokonaisuudenLisätiedot(
              tunniste = getTutkintokokonaisuudenTunniste(index),
              tyyppi = Some(conversionUtils.requiredKoodi("ytrtutkintokokonaisuudentyyppi", examination.examinationType)),
              tila = examination.examinationState.map(x => conversionUtils.requiredKoodi("ytrtutkintokokonaisuudentila", x))
                // Muut kuin graduated-tila eivät ole luotettavia, koska YTR ei pysty vielä toimittamaan ajantasaista tietoa
                // esim. failed-tilasta, mitä eivät tallenna tietokantaan.
                .filter(_.koodiarvo == "graduated"),
              suorituskieli = Some(conversionUtils.requiredKoodi("kieli", examination.language.toUpperCase)),
              tutkintokerrat = examination.examinationPeriods.map(period => YlioppilastutkinnonTutkintokerranLisätiedot(
                tutkintokerta = conversionUtils.convertTutkintokerta(period.examinationPeriod),
                koulutustausta = period.education.map(x => conversionUtils.requiredKoodi("ytrkoulutustausta", x.toString)),
                oppilaitos = period.schoolOid.flatMap(oid =>
                  organisaatioRepository.getOppilaitosHetkellä(
                    oid,
                    YtrConversionUtils.convertTutkintokertaToDate(period.examinationPeriod)
                  )
                )
              )),
              aiemminSuoritetutKokeet = examination.includedExams.map(_.map(includedExam =>
                YlioppilastutkinnonSisältyväKoe(
                  koulutusmoduuli = YlioppilasTutkinnonKoe(
                    tunniste = conversionUtils.requiredKoodi("koskiyokokeet", includedExam.examId),
                  ),
                  tutkintokerta = conversionUtils.convertTutkintokerta(includedExam.examinationPeriod)
                )
              )).filter(_.nonEmpty)
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
      )

    val opiskeluoikeus = täydennäKoodistoYmsMerkkijonot(raakaOpiskeluoikeus)
      // Lisää oppilaitokseksi ylioppilastutkintolautakunta, joka ei ole oikeasti oppilaitos vaan koulutustoimija
      .copy(
        oppilaitos = conversionUtils.ytlOppilaitos
      )

    Some(opiskeluoikeus)
  }

  private def täydennäKoodistoYmsMerkkijonot(opiskeluoikeus: YlioppilastutkinnonOpiskeluoikeus): YlioppilastutkinnonOpiskeluoikeus = {
    // Serialisoi ja deserialisoi opiskeluoikeus, jotta se tulee käsiteltyä samalla tavalla kuin muutkin Koskeen
    // tallennettavat opiskeluoikeudet. Tämä varmistaa, että koodistoarvoilla ja organisaatioille on haettu nimet.
    val jsonOo = Serializer.serialize(opiskeluoikeus, serializationContext)
    val resultOo =
      validatingAndResolvingExtractor.extract[YlioppilastutkinnonOpiskeluoikeus](deserializationContext)(jsonOo)
    resultOo match {
      case Right(oo) => oo
      case _ =>
        throw new RuntimeException("Opiskeluoikeuden konversio epäonnistui täydentämään koodistoarvoja ja organisaatioita")
    }
  }
}
