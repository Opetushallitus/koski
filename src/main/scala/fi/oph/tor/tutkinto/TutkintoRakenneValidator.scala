package fi.oph.tor.tutkinto

import fi.oph.tor.http.{ErrorDetail, TorErrorCode, HttpStatus}
import fi.oph.tor.schema._

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository) {
  def validateTutkintoRakenne(opiskeluOikeus: OpiskeluOikeus) = {
    validateSuoritus(opiskeluOikeus.suoritus, None, None)
  }
  private def validateSuoritus(suoritus: Suoritus, rakenne: Option[TutkintoRakenne], suoritustapa: Option[Suoritustapa]): HttpStatus = (suoritus.koulutusmoduulitoteutus, rakenne, suoritustapa) match {
    case (t: TutkintoKoulutustoteutus, _, _) =>
      t.koulutusmoduuli.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
        case None =>
          HttpStatus.badRequest(t.koulutusmoduuli.perusteenDiaarinumero match {
            case Some(d) => ErrorDetail(TorErrorCode.Validation.Rakenne.tuntematonDiaari,"Tutkinnon peruste on virheellinen: " + d)
            case None => ErrorDetail(TorErrorCode.Validation.Rakenne.diaariPuuttuu, "Tutkinnon peruste puuttuu")
          })
        case Some(rakenne) =>
          val tuntemattomatOsaamisalat: List[KoodistoKoodiViite] = t.osaamisala.toList.flatten.filter(osaamisala => !TutkintoRakenne.findOsaamisala(rakenne, osaamisala.koodiarvo).isDefined)
          HttpStatus.fold(tuntemattomatOsaamisalat.map { osaamisala: KoodistoKoodiViite => HttpStatus.badRequest(TorErrorCode.Validation.Rakenne.tuntematonOsaamisala, "Osaamisala " + osaamisala.koodiarvo + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero) })
            .then(HttpStatus.fold(suoritus.osasuoritukset.toList.flatten.map{validateSuoritus(_, Some(rakenne), t.suoritustapa)}))
      }
    case (t: OpsTutkinnonosatoteutus, Some(rakenne), None)  =>
      HttpStatus.badRequest(TorErrorCode.Validation.Rakenne.suoritustapaPuuttuu, "Tutkinnolta puuttuu suoritustapa. Tutkinnon osasuorituksia ei hyväksytä.")
    case (t: OpsTutkinnonosatoteutus, Some(rakenne), Some(suoritustapa))  =>
      TutkintoRakenne.findTutkinnonOsa(rakenne, suoritustapa.tunniste, t.koulutusmoduuli.tunniste) match {
        case None =>
          HttpStatus.badRequest(TorErrorCode.Validation.Rakenne.tuntematonOsa, "Tutkinnon osa " + t.koulutusmoduuli.tunniste + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero + " - suoritustapa " + suoritustapa.tunniste.koodiarvo)
        case Some(tutkinnonOsa) =>
          HttpStatus.ok
      }
    case _ =>
      HttpStatus.ok
  }
}
