package fi.oph.koski.ytr

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.schema._

case class YtrOppijaConverter(oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu) extends Logging {
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
            val (valmistumisPäivä, tila) = ytrOppija.graduationDate match {
              case Some(graduationDate) => (Some(graduationDate), tilaValmis)
              case None => (None, tilaKesken)
            }
            Some(YlioppilastutkinnonOpiskeluoikeus(
              oppilaitos = oppilaitos, koulutustoimija = None, tila = None, suoritukset = List(YlioppilastutkinnonSuoritus(
                tila = tila,
                valmistumisPäivä = valmistumisPäivä, // TODO: ei oikein sovi yleiseen malliin, kuuliisi vahvistukseen tai arviointiin, mutta kumpaakaan ei tässä ole
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
    koulutusmoduuli = YlioppilasTutkinnonKoe(PaikallinenKoodi(exam.examId, LocalizedString.unlocalized(exam.examId), "ytr/koetunnukset")) // TODO: oikea nimi
  )

  private def requiredKoodi(uri: String, koodi: String) = {
    koodistoViitePalvelu.validate(Koodistokoodiviite(koodi, uri)).getOrElse(throw new IllegalArgumentException("Puuttuva koodi: " + Koodistokoodiviite(koodi, uri)))
  }

  private def tilaValmis = requiredKoodi("suorituksentila", "VALMIS")
  private def tilaKesken = requiredKoodi("suorituksentila", "KESKEN")
}
