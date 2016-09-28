package fi.oph.koski.tutkinto

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi._

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository) {
  def validateTutkintoRakenne(suoritus: Suoritus) = suoritus match {
    case (todistus: PerusopetuksenOppimääränSuoritus) =>
      HttpStatus.justStatus(getRakenne(todistus.koulutusmoduuli, perusopetuksenKoulutustyypit))
    case (todistus: LukionOppimääränSuoritus) =>
      HttpStatus.justStatus(getRakenne(todistus.koulutusmoduuli, lukionKoulutustyypit))
    case (tutkintoSuoritus: AmmatillisenTutkinnonSuoritus) =>
      getRakenne(tutkintoSuoritus.koulutusmoduuli, ammatillisetKoulutustyypit) match {
        case Left(status) => status
        case Right(rakenne) =>
          validateOsaamisala(tutkintoSuoritus.osaamisala.toList.flatten, rakenne).then(HttpStatus.fold(suoritus.osasuoritusLista.map {
            case osaSuoritus: AmmatillisenTutkinnonOsanSuoritus if !tutkintoSuoritus.suoritustapa.isDefined =>
              KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu()
            case osaSuoritus: AmmatillisenTutkinnonOsanSuoritus => osaSuoritus.koulutusmoduuli match {
              case osa: ValtakunnallinenTutkinnonOsa =>
                validateTutkinnonOsa(osaSuoritus, osa, rakenne, tutkintoSuoritus.suoritustapa)
              case osa: PaikallinenTutkinnonOsa =>
                HttpStatus.ok // vain OpsTutkinnonosatoteutukset validoidaan, muut sellaisenaan läpi, koska niiden rakennetta ei tunneta
            }
          }))
      }
    case _ =>
      // Jos juurisuorituksena on jokin muu kuin tutkintosuoritus, ei rakennetta voida validoida
      HttpStatus.ok
  }

  private def getRakenne(tutkinto: DiaarinumerollinenKoulutus, koulutustyypit: List[Koulutustyyppi.Koulutustyyppi]): Either[HttpStatus, TutkintoRakenne] = {
    tutkinto.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
      case None =>
        tutkinto.perusteenDiaarinumero match {
          case Some(d) => Left(KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla " + d))
          case None => Left(KoskiErrorCategory.ok()) // Ei diaarinumeroa -> ei validointia
        }
      case Some(rakenne) =>
        if (koulutustyypit.contains(rakenne.koulutustyyppi)) {
          Right(rakenne)
        } else {
          Left(KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi("Perusteella " + rakenne.diaarinumero + " on väärä koulutustyyppi"))
        }
    }
  }


  private def validateOsaamisala(osaamisala: List[Koodistokoodiviite], rakenne: TutkintoRakenne): HttpStatus = {
    val tuntemattomatOsaamisalat: List[Koodistokoodiviite] = osaamisala.filter(osaamisala => !findOsaamisala(rakenne, osaamisala.koodiarvo).isDefined)

    HttpStatus.fold(tuntemattomatOsaamisalat.map {
      osaamisala: Koodistokoodiviite => KoskiErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala " + osaamisala.koodiarvo + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero)
    })
  }

  private def validateTutkinnonOsa(suoritus: AmmatillisenTutkinnonOsanSuoritus, osa: ValtakunnallinenTutkinnonOsa, rakenne: TutkintoRakenne, suoritustapa: Option[Koodistokoodiviite]): HttpStatus = {
    val suoritustapaJaRakenne = suoritustapa.flatMap(rakenne.findSuoritustapaJaRakenne(_))
    suoritustapaJaRakenne match {
      case Some(suoritustapaJaRakenne)  =>
        suoritus.tutkinto match {
          case Some(tutkinto) =>
            // Tutkinnon osa toisesta tutkinnosta.
            // Ei validoida rakenteeseen kuuluvuutta, vain se, että rakenne löytyy diaarinumerolla
            HttpStatus.justStatus(getRakenne(tutkinto, ammatillisetKoulutustyypit))
          case None =>
            // Validoidaan tutkintorakenteen mukaisesti
            findTutkinnonOsa(suoritustapaJaRakenne, osa.tunniste) match {
              case None =>
                KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa(
                  "Tutkinnon osa " + osa.tunniste + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero + " - suoritustapa " + suoritustapaJaRakenne.suoritustapa.koodiarvo)
              case Some(tutkinnonOsa) =>
                HttpStatus.ok
            }
        }
      case None =>
        KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaaEiLöydyRakenteesta()
    }
  }

  private def findTutkinnonOsa(rakenne: SuoritustapaJaRakenne, koulutusModuuliTunniste: Koodistokoodiviite): Option[TutkinnonOsa] = {
    rakenne.rakenne.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste))
  }

  private def findTutkinnonOsa(rakenne: RakenneOsa, koulutusModuuliTunniste: Koodistokoodiviite): Option[TutkinnonOsa] = rakenne match {
    case t:TutkinnonOsa if t.tunniste == koulutusModuuliTunniste => Some(t)
    case t:RakenneModuuli => t.osat.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste)).headOption
    case _ => None
  }

  private def findOsaamisala(rakenne: TutkintoRakenne, osaamisAlaKoodi: String) = rakenne.osaamisalat.find(_.koodiarvo == osaamisAlaKoodi)
}
