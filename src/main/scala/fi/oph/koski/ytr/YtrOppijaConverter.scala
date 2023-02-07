package fi.oph.koski.ytr

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._

case class YtrOppijaConverter(oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository, localizations: LocalizationRepository) extends Logging {
  private val conversionUtils = new YtrConversionUtils(localizations, koodistoViitePalvelu, organisaatioRepository)

  def convert(ytrOppija: YtrOppija): Option[YlioppilastutkinnonOpiskeluoikeus] = {
    val ytl = conversionUtils.ytl

    val vahvistus =
      ytrOppija.graduationDate.map(gradudationDate => conversionUtils.convertVahvistus(gradudationDate))

    val oppilaitos = ytrOppija.certificateSchoolOphOid.flatMap(oid => oppilaitosRepository.findByOid(oid) match  {
      case None =>
        // certificateSchoolOphOid on ollut jo muutaman vuoden aina tarkoituksella YO-tutkintolautakunnan organisaation oid.
        // Tämä on ollut quick fix siihen, että omaopintopolussa ei hämäävästi väitettäisi YO-tutkinnon hyväksyjäksi oppilaitosta, kun
        // oikeasti se on Ylioppilastutkintolautakunnan myöntämä.
        if (oid != "1.2.246.562.10.43628088406") {
          logger.warn(s"Oppilaitosta $oid ei löydy")
        }
        None
      case Some(oppilaitos) =>
        vahvistus
          .flatMap(v => organisaatioRepository.getOrganisaationNimiHetkellä(oppilaitos.oid, v.päivä))
          .map(n => oppilaitos.copy(nimi = Some(n)))
          .orElse(Some(oppilaitos))
    })

    Some(YlioppilastutkinnonOpiskeluoikeus(
      lähdejärjestelmänId = Some(LähdejärjestelmäId(None, conversionUtils.requiredKoodi("lahdejarjestelma", "ytr"))),
      oppilaitos = oppilaitos,
      koulutustoimija = Some(ytl),
      tila = YlioppilastutkinnonOpiskeluoikeudenTila(Nil),
      tyyppi = conversionUtils.requiredKoodi("opiskeluoikeudentyyppi", "ylioppilastutkinto"),
      suoritukset = List(YlioppilastutkinnonSuoritus(
        tyyppi = conversionUtils.requiredKoodi("suorituksentyyppi", "ylioppilastutkinto"),
        vahvistus = vahvistus,
        toimipiste = oppilaitos,
        koulutusmoduuli = Ylioppilastutkinto(conversionUtils.requiredKoodi("koulutus", "301000"), None),
        pakollisetKokeetSuoritettu = ytrOppija.hasCompletedMandatoryExams,
        osasuoritukset = Some(ytrOppija.exams.flatMap(convertExam).sorted(examOrdering))
      ))
    ))
  }
  private def convertExam(exam: YtrExam) = koodistoViitePalvelu.validate("koskiyokokeet", exam.examId).map { tunniste =>
    val tutkintokerta: YlioppilastutkinnonTutkintokerta = conversionUtils.convertTutkintokerta(exam.period)

    YlioppilastutkinnonKokeenSuoritus(
      koulutusmoduuli = YlioppilasTutkinnonKoe(tunniste),
      tutkintokerta = tutkintokerta,
      arviointi = Some(List(conversionUtils.convertArviointi(exam.grade, exam.points))),
      tyyppi = conversionUtils.requiredKoodi("suorituksentyyppi", "ylioppilastutkinnonkoe")
    )
  }.orElse {
    logger.warn(s"Tuntematon yo-kokeen koetunnus: ${exam.examId}")
    None
  }

  private lazy val examOrdering = Ordering.by {
    s: YlioppilastutkinnonKokeenSuoritus => (s.tutkintokerta.koodiarvo, s.koulutusmoduuli.tunniste.koodiarvo)
  } (Ordering.Tuple2(Ordering.String.reverse, Ordering.String))
}
