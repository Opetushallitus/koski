package fi.oph.tor.tutkinto

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.schema._

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository) {
  def validateTutkintoRakenne(opiskeluOikeus: OpiskeluOikeus) = {
    validateSuoritus(opiskeluOikeus.suoritus, None, None)
  }
  private def validateSuoritus(suoritus: Suoritus, rakenne: Option[TutkintoRakenne], suoritustapa: Option[Suoritustapa]): HttpStatus = (suoritus.koulutusmoduulitoteutus, rakenne, suoritustapa) match {
    case (t: TutkintoKoulutustoteutus, _, _) =>
      t.koulutusmoduuli.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
        case None =>
          HttpStatus.badRequest(t.koulutusmoduuli.perusteenDiaarinumero.map(d => "Tutkinnon peruste on virheellinen: " + d).getOrElse("Tutkinnon peruste puuttuu"))
        case Some(rakenne) =>
          HttpStatus.each(t.osaamisala.toList.flatten.filter(osaamisala => !TutkintoRakenne.findOsaamisala(rakenne, osaamisala.koodiarvo).isDefined))
              { osaamisala: KoodistoKoodiViite => HttpStatus.badRequest("Osaamisala " + osaamisala.koodiarvo + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero) }
            .then(HttpStatus.each(suoritus.osasuoritukset.toList.flatten)(validateSuoritus(_, Some(rakenne), t.suoritustapa)))
      }
    case (t: OpsTutkinnonosatoteutus, Some(rakenne), None)  =>
      HttpStatus.badRequest("Tutkinnolta puuttuu suoritustapa. Tutkinnon osasuorituksia ei hyväksytä.")
    case (t: OpsTutkinnonosatoteutus, Some(rakenne), Some(suoritustapa))  =>
      TutkintoRakenne.findTutkinnonOsa(rakenne, suoritustapa.tunniste, t.koulutusmoduuli.tunniste) match {
        case None =>
          HttpStatus.badRequest("Tutkinnon osa " + t.koulutusmoduuli.tunniste + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero + " - suoritustapa " + suoritustapa.tunniste.koodiarvo)
        case Some(tutkinnonOsa) =>
          HttpStatus.ok
      }
    case _ =>
      HttpStatus.ok
  }
}
