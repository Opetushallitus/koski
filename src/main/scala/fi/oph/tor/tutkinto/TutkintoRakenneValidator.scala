package fi.oph.tor.tutkinto

import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.schema._

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository) {
  def validateTutkintoRakenne(suoritus: Suoritus) = suoritus.koulutusmoduulitoteutus match {
    case (tutkintoToteutus: TutkintoKoulutustoteutus) =>
      getRakenne(tutkintoToteutus.koulutusmoduuli) match {
        case Left(status) => status
        case Right(rakenne) =>
          validateOsaamisala(tutkintoToteutus.osaamisala.toList.flatten, rakenne).then(HttpStatus.fold(suoritus.osasuoritukset.toList.flatten.map(_.koulutusmoduulitoteutus).map {
            case osa: AmmatillinenTutkinnonosaToteutus if !tutkintoToteutus.suoritustapa.isDefined =>
              TorErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu()
            case osa: OpsTutkinnonosatoteutus =>
              validateTutkinnonOsa(osa, Some(rakenne), tutkintoToteutus.suoritustapa)
            case osa =>
              HttpStatus.ok // vain OpsTutkinnonosatoteutukset validoidaan, muut sellaisenaan läpi, koska niiden rakennetta ei tunneta
          }))
      }
    case _ =>
      // Jos juurisuorituksena on jokin muu kuin tutkintosuoritus, ei rakennetta voida validoida
      HttpStatus.ok
  }

  private def getRakenne(tutkinto: TutkintoKoulutus): Either[HttpStatus, TutkintoRakenne] = {
    tutkinto.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
      case None =>
        tutkinto.perusteenDiaarinumero match {
          case Some(d) => Left(TorErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla " + d))
          case None => Left(TorErrorCategory.ok()) // Ei diaarinumeroa -> ei validointia
        }
      case Some(rakenne) =>
        Right(rakenne)
    }
  }


  private def validateOsaamisala(osaamisala: List[KoodistoKoodiViite], rakenne: TutkintoRakenne): HttpStatus = {
    val tuntemattomatOsaamisalat: List[KoodistoKoodiViite] = osaamisala.filter(osaamisala => !findOsaamisala(rakenne, osaamisala.koodiarvo).isDefined)

    HttpStatus.fold(tuntemattomatOsaamisalat.map {
      osaamisala: KoodistoKoodiViite => TorErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala " + osaamisala.koodiarvo + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero)
    })
  }

  private def validateTutkinnonOsa(tutkinnonOsaToteutus: OpsTutkinnonosatoteutus, rakenne: Option[TutkintoRakenne], suoritustapa: Option[Suoritustapa]): HttpStatus = (tutkinnonOsaToteutus, rakenne, suoritustapa) match {
    case (t: OpsTutkinnonosatoteutus, Some(rakenne), Some(suoritustapa))  =>
      t.tutkinto match {
        case Some(tutkinto) =>
          // Tutkinnon osa toisesta tutkinnosta.
          getRakenne(tutkinto) match {
            case Right(rakenne) =>
              // Ei validoida rakenteeseen kuuluvuutta.
              HttpStatus.ok
            case Left(status) =>
              status
          }
        case None =>
          // Validoidaan tutkintorakenteen mukaisesti
          validoiTutkinnonOsaRakenteessa(t.koulutusmoduuli, rakenne, suoritustapa)
      }
  }

  private def validoiTutkinnonOsaRakenteessa(tutkinnonOsa: OpsTutkinnonosa, rakenne: TutkintoRakenne, suoritustapa: Suoritustapa): HttpStatus = {
    findTutkinnonOsa(rakenne, suoritustapa.tunniste, tutkinnonOsa.tunniste) match {
      case None =>
        TorErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa " + tutkinnonOsa.tunniste + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero + " - suoritustapa " + suoritustapa.tunniste.koodiarvo)
      case Some(tutkinnonOsa) =>
        HttpStatus.ok
    }
  }

  private def findTutkinnonOsa(rakenne: TutkintoRakenne, suoritustapa: KoodistoKoodiViite, koulutusModuuliTunniste: KoodistoKoodiViite): Option[TutkinnonOsa] = {
    rakenne.suoritustavat.find(_.suoritustapa == suoritustapa).flatMap(suoritustapa => findTutkinnonOsa(suoritustapa.rakenne, koulutusModuuliTunniste)).headOption
  }

  private def findTutkinnonOsa(rakenne: RakenneOsa, koulutusModuuliTunniste: KoodistoKoodiViite): Option[TutkinnonOsa] = rakenne match {
    case t:TutkinnonOsa if t.tunniste == koulutusModuuliTunniste => Some(t)
    case t:RakenneModuuli => t.osat.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste)).headOption
    case _ => None
  }

  private def findOsaamisala(rakenne: TutkintoRakenne, osaamisAlaKoodi: String) = rakenne.osaamisalat.find(_.koodiarvo == osaamisAlaKoodi)
}
