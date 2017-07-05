package fi.oph.koski.tutkinto

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi._

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository, koodistoViitePalvelu: KoodistoViitePalvelu) {
  def validateTutkintoRakenne(suoritus: PäätasonSuoritus) = suoritus match {
    case tutkintoSuoritus: AmmatillisenTutkinnonSuoritus =>
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
    case suoritus: AikuistenPerusopetuksenOppimääränSuoritus =>
      HttpStatus.justStatus(getRakenne(suoritus.koulutusmoduuli, Some(List(aikuistenPerusopetus)))).then { validateKurssikoodisto(suoritus) }
    case _ =>
      suoritus.koulutusmoduuli match {
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

  private def validateKurssikoodisto(suoritus: AikuistenPerusopetuksenOppimääränSuoritus) = {
    def verifyKurssikoodisto(vuosiluku: String, koodistoUri: String) = {
      val kurssit = suoritus.osasuoritukset.toList.flatten.flatMap(_.osasuoritukset.toList.flatten).map(_.koulutusmoduuli)

      HttpStatus.fold(kurssit
        .map(_.tunniste)
        .collect { case k: Koulutustyyppi if k.koodistoUri != koodistoUri => k.koodistoUri }
        .distinct
        .map(k => KoskiErrorCategory.badRequest.validation.rakenne.vääräKurssikoodisto(s"Aikuisten perusopetuksessa ${vuosiluku} käytetty väärää kurssikoodistoa ${k} (käytettävä koodistoa ${koodistoUri})"))
      )
    }

    suoritus.koulutusmoduuli.perusteenDiaarinumero match {
      case Some("OPH-1280-2017") =>
        verifyKurssikoodisto("2017", "aikuistenperusopetuksenpaattovaiheenkurssit2017")
      case Some("19/011/2015") => HttpStatus.ok
        verifyKurssikoodisto("2015", "aikuistenperusopetuksenkurssit2015")
      case _ => HttpStatus.ok
    }
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
            HttpStatus.justStatus(getRakenne(tutkinto, Some(ammatillisetKoulutustyypit)))
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
