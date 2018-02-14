package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi._
import fi.oph.koski.tutkinto._

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository, koodistoViitePalvelu: KoodistoViitePalvelu) {
  def validateTutkintoRakenne(suoritus: PäätasonSuoritus) = suoritus match {
    case tutkintoSuoritus: AmmatillisenTutkinnonSuoritus =>
      getRakenne(tutkintoSuoritus.koulutusmoduuli, Some(ammatillisetKoulutustyypit)) match {
        case Left(status) => status
        case Right(rakenne) =>
          validateOsaamisalat(tutkintoSuoritus.osaamisala.toList.flatten.map(_.osaamisala), rakenne).onSuccess(HttpStatus.fold(suoritus.osasuoritusLista.map {
            case osaSuoritus: AmmatillisenTutkinnonOsanSuoritus =>
              HttpStatus.fold(osaSuoritus.koulutusmoduuli match {
                case osa: ValtakunnallinenTutkinnonOsa =>
                  validateTutkinnonOsa(osaSuoritus, osa, rakenne, tutkintoSuoritus.suoritustapa)
                case osa: PaikallinenTutkinnonOsa =>
                  HttpStatus.ok // vain OpsTutkinnonosatoteutukset validoidaan, muut sellaisenaan läpi, koska niiden rakennetta ei tunneta
              }, validateTutkintoField(tutkintoSuoritus, osaSuoritus))
          }))
      }
    case suoritus: AikuistenPerusopetuksenOppimääränSuoritus =>
      HttpStatus.justStatus(getRakenne(suoritus.koulutusmoduuli, Some(List(aikuistenPerusopetus))))
    case suoritus: AmmatillisenTutkinnonOsittainenSuoritus =>
      HttpStatus.justStatus(getRakenne(suoritus.koulutusmoduuli, Some(ammatillisetKoulutustyypit)))
        .onSuccess(HttpStatus.fold(suoritus.osasuoritukset.toList.flatten.map(validateTutkinnonOsanTutkinto)))
    case _ =>
      suoritus.koulutusmoduuli match {
        case d: AikuistenPerusopetus =>
          HttpStatus.justStatus(getRakenne(d, Some(List(aikuistenPerusopetus))))
        case d: PerusopetuksenDiaarinumerollinenKoulutus =>
          HttpStatus.justStatus(getRakenne(d, Some(List(perusopetus))))
        case d: PerusopetuksenOppiaine =>
          HttpStatus.justStatus(getRakenne(d, Some(List(aikuistenPerusopetus))))
        case d: LukionOppimäärä =>
          HttpStatus.justStatus(getRakenne(d, Some(lukionKoulutustyypit)))
        case d: LukionOppiaine =>
          HttpStatus.justStatus(getRakenne(d, Some(lukionKoulutustyypit)))
        case d: Diaarinumerollinen =>
          HttpStatus.justStatus(getRakenne(d, None))
        case _ =>
          HttpStatus.ok
      }
  }

  private def validateTutkintoField(tutkintoSuoritus: AmmatillisenTutkinnonSuoritus, osaSuoritus: AmmatillisenTutkinnonOsanSuoritus) = (tutkintoSuoritus.koulutusmoduuli.tunniste, osaSuoritus.tutkinto.map(_.tunniste)) match {
    case (tutkintoKoodi, Some(tutkinnonOsanTutkintoKoodi)) if (tutkintoKoodi.koodiarvo == tutkinnonOsanTutkintoKoodi.koodiarvo) =>
      KoskiErrorCategory.badRequest.validation.rakenne.samaTutkintokoodi(s"Tutkinnon osalle ${osaSuoritus.koulutusmoduuli.tunniste} on merkitty tutkinto, jossa on sama tutkintokoodi ${tutkintoKoodi} kuin tutkinnon suorituksessa")
    case _ =>
      HttpStatus.ok
  }

  private def getRakenne(tutkinto: Diaarinumerollinen, koulutustyypit: Option[List[Koulutustyyppi.Koulutustyyppi]]): Either[HttpStatus, TutkintoRakenne] = {
      tutkinto.perusteenDiaarinumero.map { diaarinumero =>
        tutkintoRepository.findPerusteRakenne(diaarinumero) match {
          case None =>
            if (koodistoViitePalvelu.getKoodistoKoodiViite("koskikoulutustendiaarinumerot", diaarinumero).isEmpty) {
              Left(KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla " + diaarinumero))
            } else {
              Left(KoskiErrorCategory.ok())
            }
          case Some(rakenne) =>
            koulutustyypit match {
              case Some(koulutustyypit) if !koulutustyypit.contains(rakenne.koulutustyyppi) =>
                Left(KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi("Perusteella " + rakenne.diaarinumero + s" on väärä koulutustyyppi ${Koulutustyyppi.describe(rakenne.koulutustyyppi)}. Hyväksytyt koulutustyypit tälle suoritukselle ovat ${koulutustyypit.map(Koulutustyyppi.describe).mkString(", ")}"))
              case _ =>
                Right(rakenne)
            }
        }
      }.getOrElse(Left(KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu()))
  }


  private def validateOsaamisalat(osaamisalat: List[Koodistokoodiviite], rakenne: TutkintoRakenne): HttpStatus = {
    val tuntemattomatOsaamisalat: List[Koodistokoodiviite] = osaamisalat.filter(osaamisala => !findOsaamisala(rakenne, osaamisala.koodiarvo).isDefined)

    HttpStatus.fold(tuntemattomatOsaamisalat.map {
      osaamisala: Koodistokoodiviite => KoskiErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala " + osaamisala.koodiarvo + " ei löydy tutkintorakenteesta perusteelle " + rakenne.diaarinumero)
    })
  }

  private def validateTutkinnonOsanTutkinto(suoritus: AmmatillisenTutkinnonOsanSuoritus) = {
    suoritus.tutkinto match {
      case Some(tutkinto) => HttpStatus.justStatus(getRakenne(tutkinto, Some(ammatillisetKoulutustyypit)))
      case None => HttpStatus.ok
    }
  }

  private def validateTutkinnonOsa(suoritus: AmmatillisenTutkinnonOsanSuoritus, osa: ValtakunnallinenTutkinnonOsa, rakenne: TutkintoRakenne, suoritustapa: Koodistokoodiviite): HttpStatus = {
    val suoritustapaJaRakenne = rakenne.findSuoritustapaJaRakenne(suoritustapa)
    suoritustapaJaRakenne match {
      case Some(suoritustapaJaRakenne)  =>
        (suoritus.tutkinto, suoritus.tutkinnonOsanRyhmä) match {
          case (Some(tutkinto), _) =>
            // Tutkinnon osa toisesta tutkinnosta.
            // Ei validoida rakenteeseen kuuluvuutta, vain se, että rakenne löytyy diaarinumerolla
            validateTutkinnonOsanTutkinto(suoritus)
          case (_, Some(Koodistokoodiviite(koodiarvo, _, _, _, _))) if List("3", "4").contains(koodiarvo) =>
            // Vapaavalintainen tai yksilöllisesti tutkintoa laajentava osa
            // Ei validoida rakenteeseen kuuluvuutta
            HttpStatus.ok
          case (_, _) =>
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
