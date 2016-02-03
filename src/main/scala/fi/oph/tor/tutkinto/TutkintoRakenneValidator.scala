package fi.oph.tor.tutkinto

import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.schema._

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository) {
  def validateTutkintoRakenne(opiskeluOikeus: OpiskeluOikeus) = {
    validateSuoritus(opiskeluOikeus.suoritus, None, None)
  }
  private def validateSuoritus(suoritus: Suoritus, rakenne: Option[TutkintoRakenne], suoritustapa: Option[Suoritustapa]): HttpStatus = (suoritus.koulutusmoduulitoteutus, rakenne, suoritustapa) match {
    case (t: TutkintoKoulutustoteutus, _, _) =>
      t.koulutusmoduuli.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
        case None =>
          t.koulutusmoduuli.perusteenDiaarinumero match {
            case Some(d) => TorErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla " + d)
            case None => TorErrorCategory.ok()
          }
        case Some(rakenne) =>
          val tuntemattomatOsaamisalat: List[KoodistoKoodiViite] = t.osaamisala.toList.flatten.filter(osaamisala => !TutkintoRakenne.findOsaamisala(rakenne, osaamisala.koodiarvo).isDefined)
          HttpStatus.fold(tuntemattomatOsaamisalat.map { osaamisala: KoodistoKoodiViite => TorErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala " + osaamisala.koodiarvo + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero) })
            .then(HttpStatus.fold(suoritus.osasuoritukset.toList.flatten.map{validateSuoritus(_, Some(rakenne), t.suoritustapa)}))
      }
    case (t: OpsTutkinnonosatoteutus, Some(rakenne), None)  =>
      TorErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu()
    case (t: OpsTutkinnonosatoteutus, Some(rakenne), Some(suoritustapa))  =>
      TutkintoRakenne.findTutkinnonOsa(rakenne, suoritustapa.tunniste, t.koulutusmoduuli.tunniste) match {
        case None =>
          TorErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa " + t.koulutusmoduuli.tunniste + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero + " - suoritustapa " + suoritustapa.tunniste.koodiarvo)
        case Some(tutkinnonOsa) =>
          HttpStatus.ok
      }
    case _ =>
      HttpStatus.ok
  }
}
