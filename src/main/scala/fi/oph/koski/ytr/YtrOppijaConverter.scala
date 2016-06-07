package fi.oph.koski.ytr

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.Finnish
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._

case class YtrOppijaConverter(oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository) extends Logging {
  def convert(ytrOppija: YtrOppija): Option[YlioppilastutkinnonOpiskeluoikeus] = {
    ytrOppija.graduationSchoolOphOid match {
      case None =>
        logger.warn("YTR-tiedosta puuttuu oppilaitoksen tunniste")
        None
      case Some(oid) =>
        oppilaitosRepository.findByOid(oid) match  {
          case None =>
            logger.error("Oppilaitosta " + oid + " ei löydy")
            None
          case Some(oppilaitos) =>
            val (vahvistus, tila) = ytrOppija.graduationDate match {
              case Some(graduationDate) =>
                val helsinki: Koodistokoodiviite = koodistoViitePalvelu.getKoodistoKoodiViite("kunta", "091").getOrElse(throw new IllegalStateException("Helsingin kaupunkia ei löytynyt koodistopalvelusta"))
                val ytl = organisaatioRepository.getOrganisaatio("1.2.246.562.10.43628088406").getOrElse(throw new IllegalStateException(("Ylioppilastutkintolautakuntaorganisaatiota ei löytynyt organisaatiopalvelusta")))
                val puheenjohtajanNimi: String = "N N" // TODO: oikea nimi puuttuu
                val puheenjohtaja = OrganisaatioHenkilö(puheenjohtajanNimi, Finnish("Ylioppilastutkintolautakunnan puheenjohtaja"), ytl)
                (Some(Vahvistus(graduationDate, helsinki, oppilaitos, myöntäjäHenkilöt = List(puheenjohtaja))), tilaValmis)
              case None =>
                (None, tilaKesken)
            }
            Some(YlioppilastutkinnonOpiskeluoikeus(
              oppilaitos = oppilaitos, koulutustoimija = None, tila = None, suoritukset = List(YlioppilastutkinnonSuoritus(
                tila = tila,
                vahvistus = vahvistus,
                toimipiste = oppilaitos,
                koulutusmoduuli = Ylioppilastutkinto(requiredKoodi("koulutus", "301000"), None),
                osasuoritukset = Some(ytrOppija.exams.map(convertExam)))
              ))
            )
        }
    }
  }
  private def convertExam(exam: YtrExam) = YlioppilastutkinnonKokeenSuoritus(
    tila = tilaValmis,
    arviointi = Some(List(YlioppilaskokeenArviointi(exam.grade))),
    koulutusmoduuli = YlioppilasTutkinnonKoe(PaikallinenKoodi(exam.examId, Finnish(exam.examNameFi, exam.examNameSv, exam.examNameEn), "ytr/koetunnukset"))
  )

  private def requiredKoodi(uri: String, koodi: String) = {
    koodistoViitePalvelu.validate(Koodistokoodiviite(koodi, uri)).getOrElse(throw new IllegalArgumentException("Puuttuva koodi: " + Koodistokoodiviite(koodi, uri)))
  }

  private def tilaValmis = requiredKoodi("suorituksentila", "VALMIS")
  private def tilaKesken = requiredKoodi("suorituksentila", "KESKEN")
}
