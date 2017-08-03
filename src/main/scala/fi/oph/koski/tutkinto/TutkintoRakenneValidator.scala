package fi.oph.koski.tutkinto

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi._

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository, koodistoViitePalvelu: KoodistoViitePalvelu) {
  def validateTutkintoRakenne(suoritus: Suoritus) = suoritus match {
    case (tutkintoSuoritus: AmmatillisenTutkinnonSuoritus) =>
      getRakenne(tutkintoSuoritus.koulutusmoduuli, Some(ammatillisetKoulutustyypit)) match {
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
      suoritus.koulutusmoduuli match {
        case d: PerusopetuksenDiaarinumerollinenKoulutus =>
          HttpStatus.justStatus(getRakenne(d, Some(perusopetuksenKoulutustyypit)))
        case d: PerusopetuksenOppiaine =>
          HttpStatus.justStatus(getRakenne(d, Some(perusopetuksenKoulutustyypit)))
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

  private def getRakenne(tutkinto: Diaarinumerollinen, koulutustyypit: Option[List[Koulutustyyppi.Koulutustyyppi]]): Either[HttpStatus, TutkintoRakenne] = {
    tutkinto.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne) match {
      case None =>
        tutkinto.perusteenDiaarinumero match {
          case Some(d) if koodistoViitePalvelu.getKoodistoKoodiViite("koskikoulutustendiaarinumerot", d).isEmpty =>
            Left(KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla " + d))
          case _ =>
            Left(KoskiErrorCategory.ok()) // Ei diaarinumeroa -> ei validointia
        }
      case Some(rakenne) =>
        koulutustyypit match {
          case Some(koulutustyypit) if !koulutustyypit.contains(rakenne.koulutustyyppi) =>
            Left(KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi("Perusteella " + rakenne.diaarinumero + s" on väärä koulutustyyppi ${Koulutustyyppi.describe(rakenne.koulutustyyppi)}. Hyväksytyt koulutustyypit tälle suoritukselle ovat ${koulutustyypit.map(Koulutustyyppi.describe).mkString(", ")}"))
          case _ =>
            Right(rakenne)
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
        (suoritus.tutkinto, suoritus.tutkinnonOsanRyhmä) match {
          case (Some(tutkinto), _) =>
            // Tutkinnon osa toisesta tutkinnosta.
            // Ei validoida rakenteeseen kuuluvuutta, vain se, että rakenne löytyy diaarinumerolla
            HttpStatus.justStatus(getRakenne(tutkinto, Some(ammatillisetKoulutustyypit)))
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
