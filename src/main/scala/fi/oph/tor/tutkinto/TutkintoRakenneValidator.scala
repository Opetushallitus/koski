package fi.oph.tor.tutkinto

import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.schema._

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository) {
  def validateTutkintoRakenne(suoritus: Suoritus) = suoritus match {
    case (tutkintoSuoritus: AmmatillisenTutkinnonSuoritus) =>
      getRakenne(tutkintoSuoritus.koulutusmoduuli) match {
        case Left(status) => status
        case Right(rakenne) =>
          validateOsaamisala(tutkintoSuoritus.osaamisala.toList.flatten, rakenne).then(HttpStatus.fold(suoritus.osasuoritusLista.map {
            case osaSuoritus: AmmatillisenTutkinnonosanSuoritus if !tutkintoSuoritus.suoritustapa.isDefined =>
              TorErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu()
            case osaSuoritus: AmmatillisenTutkinnonosanSuoritus => osaSuoritus.koulutusmoduuli match {
              case osa: OpsTutkinnonosa =>
                validateTutkinnonOsa(osaSuoritus, osa, rakenne, tutkintoSuoritus.suoritustapa)
              case osa: PaikallinenTutkinnonosa =>
                HttpStatus.ok // vain OpsTutkinnonosatoteutukset validoidaan, muut sellaisenaan läpi, koska niiden rakennetta ei tunneta
            }
          }))
      }
    case _ =>
      // Jos juurisuorituksena on jokin muu kuin tutkintosuoritus, ei rakennetta voida validoida
      HttpStatus.ok
  }

  private def getRakenne(tutkinto: AmmatillinenTutkintoKoulutus): Either[HttpStatus, TutkintoRakenne] = {
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


  private def validateOsaamisala(osaamisala: List[Koodistokoodiviite], rakenne: TutkintoRakenne): HttpStatus = {
    val tuntemattomatOsaamisalat: List[Koodistokoodiviite] = osaamisala.filter(osaamisala => !findOsaamisala(rakenne, osaamisala.koodiarvo).isDefined)

    HttpStatus.fold(tuntemattomatOsaamisalat.map {
      osaamisala: Koodistokoodiviite => TorErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala " + osaamisala.koodiarvo + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero)
    })
  }

  private def validateTutkinnonOsa(suoritus: AmmatillisenTutkinnonosanSuoritus, osa: OpsTutkinnonosa, rakenne: TutkintoRakenne, suoritustapa: Option[Suoritustapa]): HttpStatus = {
    val suoritustapaJaRakenne = suoritustapa.flatMap(rakenne.findSuoritustapaJaRakenne(_))
    suoritustapaJaRakenne match {
      case Some(suoritustapaJaRakenne)  =>
        suoritus.tutkinto match {
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
            findTutkinnonOsa(suoritustapaJaRakenne, osa.tunniste) match {
              case None =>
                TorErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa(
                  "Tutkinnon osa " + osa.tunniste + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero + " - suoritustapa " + suoritustapaJaRakenne.suoritustapa.koodiarvo)
              case Some(tutkinnonOsa) =>
                HttpStatus.ok
            }
        }
      case None =>
        TorErrorCategory.badRequest.validation.rakenne.suoritustapaaEiLöydyRakenteesta()
    }
  }

  private def findTutkinnonOsa(rakenne: SuoritustapaJaRakenne, koulutusModuuliTunniste: Koodistokoodiviite): Option[TutkinnonOsa] = {
    findTutkinnonOsa(rakenne.rakenne, koulutusModuuliTunniste)
  }

  private def findTutkinnonOsa(rakenne: RakenneOsa, koulutusModuuliTunniste: Koodistokoodiviite): Option[TutkinnonOsa] = rakenne match {
    case t:TutkinnonOsa if t.tunniste == koulutusModuuliTunniste => Some(t)
    case t:RakenneModuuli => t.osat.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste)).headOption
    case _ => None
  }

  private def findOsaamisala(rakenne: TutkintoRakenne, osaamisAlaKoodi: String) = rakenne.osaamisalat.find(_.koodiarvo == osaamisAlaKoodi)
}
