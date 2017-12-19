package fi.oph.koski.ytr

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._

case class YtrOppijaConverter(oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository) extends Logging {
  def convert(ytrOppija: YtrOppija): Option[YlioppilastutkinnonOpiskeluoikeus] = {
    val ytl = organisaatioRepository.getOrganisaatio("1.2.246.562.10.43628088406")
      .map(_.toKoulutustoimija)
      .getOrElse(throw new IllegalStateException(("Ylioppilastutkintolautakuntaorganisaatiota ei löytynyt organisaatiopalvelusta")))

    val oppilaitos = ytrOppija.graduationSchoolOphOid.flatMap(oid => oppilaitosRepository.findByOid(oid) match  {
      case None =>
        logger.error(s"Oppilaitosta $oid ei löydy")
        None
      case Some(oppilaitos) =>
        Some(oppilaitos)
    })

    val vahvistus = oppilaitos.flatMap(oppilaitos => ytrOppija.graduationDate.map { graduationDate =>
      val helsinki: Koodistokoodiviite = koodistoViitePalvelu.getKoodistoKoodiViite("kunta", "091").getOrElse(throw new IllegalStateException("Helsingin kaupunkia ei löytynyt koodistopalvelusta"))
      Organisaatiovahvistus(graduationDate, helsinki, oppilaitos)
    })

    Some(YlioppilastutkinnonOpiskeluoikeus(
      lähdejärjestelmänId = Some(LähdejärjestelmäId(None, requiredKoodi("lahdejarjestelma", "ytr"))),
      oppilaitos = oppilaitos,
      koulutustoimija = Some(ytl),
      tila = YlioppilastutkinnonOpiskeluoikeudenTila(Nil),
      tyyppi = requiredKoodi("opiskeluoikeudentyyppi", "ylioppilastutkinto"),
      suoritukset = List(YlioppilastutkinnonSuoritus(
        tyyppi = requiredKoodi("suorituksentyyppi", "ylioppilastutkinto"),
        vahvistus = vahvistus,
        toimipiste = oppilaitos,
        koulutusmoduuli = Ylioppilastutkinto(requiredKoodi("koulutus", "301000"), None),
        osasuoritukset = Some(ytrOppija.exams.flatMap(convertExam)))
      )
    ))
  }
  private def convertExam(exam: YtrExam) = koodistoViitePalvelu.getKoodistoKoodiViite("koskiyokokeet", exam.examId).map(tunniste =>
    YlioppilastutkinnonKokeenSuoritus(
      tyyppi = requiredKoodi("suorituksentyyppi", "ylioppilastutkinnonkoe"),
      arviointi = Some(List(YlioppilaskokeenArviointi(requiredKoodi("koskiyoarvosanat", exam.grade)))),
      koulutusmoduuli = YlioppilasTutkinnonKoe(tunniste)
    )
  ).orElse {
    logger.warn(s"Tuntematon yo-kokeen koetunnus: ${exam.examId}")
    None
  }

  private def requiredKoodi(uri: String, koodi: String) = {
    koodistoViitePalvelu.validateRequired(uri, koodi)
  }
}
