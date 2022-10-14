package fi.oph.koski.eperusteetvalidation

import fi.oph.koski.eperusteet.{EPerusteRakenne, EPerusteetRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi.{Koulutustyyppi, ammatillisenPerustutkinnonTyypit}
import fi.oph.koski.tutkinto.TutkintoRepository

import java.time.LocalDate

class EPerusteisiinPerustuvaValidator(
  ePerusteet: EPerusteetRepository,
  tutkintoRepository: TutkintoRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu
) extends EPerusteetValidationUtils(tutkintoRepository, koodistoViitePalvelu) with Logging {
  private val tutkintorakenneValidator: TutkintoRakenneValidator = TutkintoRakenneValidator(tutkintoRepository, koodistoViitePalvelu)

  def validateTutkintorakenne(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = HttpStatus.fold(
    opiskeluoikeus.suoritukset.map(
      validateTutkintorakenne(
        _,
        opiskeluoikeus.tila.opiskeluoikeusjaksot.find(_.tila.koodiarvo == "lasna").map(_.alku),
        EPerusteetValidationUtils
          .getVaadittuPerusteenVoimassaolopäivä(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)
      )
    )
  )

  private def validateTutkintorakenne(
    suoritus: PäätasonSuoritus,
    alkamispäiväLäsnä: Option[LocalDate],
    vaadittuPerusteenVoimassaolopäivä: LocalDate
  ): HttpStatus = tutkintorakenneValidator.validate(suoritus, alkamispäiväLäsnä, vaadittuPerusteenVoimassaolopäivä)

  def validateKoulutustyypinLöytyminenAmmatillisissa(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    tutkintorakenneValidator.validateKoulutustyypinLöytyminenAmmatillisissa(oo)
  }

  def validateTutkinnonosanRyhmä(suoritus: Suoritus, vaadittuPerusteenVoimassaolopäivä: LocalDate): HttpStatus = {
    def validateTutkinnonosaSuoritus(tutkinnonSuoritus: AmmatillisenTutkinnonSuoritus, suoritus: TutkinnonOsanSuoritus, koulutustyyppi: Koulutustyyppi): HttpStatus = {
      if (ammatillisenPerustutkinnonTyypit.contains(koulutustyyppi)) {
        if (tutkinnonSuoritus.suoritustapa.koodiarvo == "ops" || tutkinnonSuoritus.suoritustapa.koodiarvo == "reformi") {
          // OPS- tai reformi -suoritustapa => vaaditaan ryhmittely
          //suoritus.tutkinnonOsanRyhmä
          //  .map(_ => HttpStatus.ok)
          //  .getOrElse(KoskiErrorCategory.badRequest.validation.rakenne.tutkinnonOsanRyhmäPuuttuu("Tutkinnonosalta " + suoritus.koulutusmoduuli.tunniste + " puuttuu tutkinnonosan ryhmä, joka on pakollinen ammatillisen perustutkinnon tutkinnonosille." ))
          // !Väliaikainen! Solenovo ei osannut ajoissa korjata datojaan. Poistetaan mahd pian. Muistutus kalenterissa 28.5.
          HttpStatus.ok
        } else {
          // Näyttö-suoritustapa => ei vaadita ryhmittelyä
          HttpStatus.ok
        }
      } else {
        // Ei ammatillinen perustutkinto => ryhmittely ei sallittu
        suoritus.tutkinnonOsanRyhmä
          .map(_ => KoskiErrorCategory.badRequest.validation.rakenne.koulutustyyppiEiSalliTutkinnonOsienRyhmittelyä("Tutkinnonosalle " + suoritus.koulutusmoduuli.tunniste + " on määritetty tutkinnonosan ryhmä, vaikka kyseessä ei ole ammatillinen perustutkinto."))
          .getOrElse(HttpStatus.ok)
      }
    }

    def validateTutkinnonosaSuoritukset(tutkinnonSuoritus: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus, suoritukset: Option[List[TutkinnonOsanSuoritus]]) = {
      haeKoulutustyyppi(tutkinnonSuoritus.koulutusmoduuli.perusteenDiaarinumero.get)
        .map(tyyppi => tutkinnonSuoritus match {
          case tutkinnonSuoritus: AmmatillisenTutkinnonSuoritus => HttpStatus.fold(suoritukset.toList.flatten.map(s => validateTutkinnonosaSuoritus(tutkinnonSuoritus, s, tyyppi)))
          case _ => HttpStatus.ok
        })
        .getOrElse {
          logger.warn("Ammatilliselle tutkintokoulutukselle " + tutkinnonSuoritus.koulutusmoduuli.perusteenDiaarinumero.get + " ei löydy koulutustyyppiä e-perusteista.")
          HttpStatus.ok
        }
    }

    suoritus match {
      case s: AmmatillisenTutkinnonSuoritus => validateTutkinnonosaSuoritukset(s, s.osasuoritukset)
      case s: AmmatillisenTutkinnonOsittainenSuoritus => validateTutkinnonosaSuoritukset(s, s.osasuoritukset)
      case _ => HttpStatus.ok
    }
  }

  def validateAmmatillinenOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus match {
      case ammatillinen: AmmatillinenOpiskeluoikeus =>
        HttpStatus.fold(
          validateViestintäJaVuorovaikutusÄidinkielellä2022(ammatillinen)
        )
      case _ => HttpStatus.ok
    }
  }

  def validatePerusteVoimassa(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    validatePerusteVoimassa(opiskeluoikeus.suoritukset,
      EPerusteetValidationUtils
        .getVaadittuPerusteenVoimassaolopäivä(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)
    )
  }

  private def validatePerusteVoimassa(
    suoritukset: List[KoskeenTallennettavaPäätasonSuoritus],
    vaadittuPerusteenVoimassaolopäivä: LocalDate
  ): HttpStatus = {
    HttpStatus.fold(
      suoritukset.map(s =>
        (s, s.koulutusmoduuli) match {
          // Valmassa erikoistapauksena hyväksytään valmistuminen pidempään TUVA-siirtymän vuoksi
          // Katso myös TutkintoRakenneValidator.validateTutkintoRakenne
          case (_: ValmaKoulutuksenSuoritus, diaarillinen: Diaarinumerollinen) if vaadittuPerusteenVoimassaolopäivä.isBefore(LocalDate.of(2022, 7, 31)) =>
            validatePerusteVoimassa(diaarillinen.perusteenDiaarinumero.get, vaadittuPerusteenVoimassaolopäivä)
          case (_: ValmaKoulutuksenSuoritus, diaarillinen: Diaarinumerollinen) if vaadittuPerusteenVoimassaolopäivä.isBefore(LocalDate.of(2022, 10, 2)) =>
            validatePerusteVoimassa(diaarillinen.perusteenDiaarinumero.get, LocalDate.of(2022, 7, 31))
          case (s: NäyttötutkintoonValmistavanKoulutuksenSuoritus, _) if s.tutkinto.perusteenDiaarinumero.isDefined =>
            validatePerusteVoimassa(s.tutkinto.perusteenDiaarinumero.get, vaadittuPerusteenVoimassaolopäivä)
          case (_, diaarillinen: Diaarinumerollinen) if diaarillinen.perusteenDiaarinumero.isDefined =>
            validatePerusteVoimassa(diaarillinen.perusteenDiaarinumero.get, vaadittuPerusteenVoimassaolopäivä)
          case _ => HttpStatus.ok
        }
      )
    )
  }

  private def validatePerusteVoimassa(diaarinumero: String, vaadittuPerusteenVoimassaolopäivä: LocalDate): HttpStatus = {
    lazy val voimassaolleetPerusteet = ePerusteet.findRakenteet(diaarinumero, Some(vaadittuPerusteenVoimassaolopäivä))

    if (onKoodistossa(diaarinumero) || voimassaolleetPerusteet.nonEmpty) {
      HttpStatus.ok
    } else {
      val kaikkiPerusteet = ePerusteet.findKaikkiRakenteet(diaarinumero)
      if (kaikkiPerusteet.isEmpty) {
        KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla " + diaarinumero)
      } else {
        KoskiErrorCategory.badRequest.validation.rakenne.perusteEiVoimassa()
      }
    }
  }

  private def validateViestintäJaVuorovaikutusÄidinkielellä2022(
    oo: AmmatillinenOpiskeluoikeus,
  ): HttpStatus = {
    val rajapäivä = LocalDate.of(2022, 8, 1)

    def löytyyVVAI22(suoritus: AmmatillisenTutkinnonSuoritus): Boolean = suoritus
      .osasuoritukset
      .getOrElse(List.empty)
      .flatMap(_.osasuoritusLista)
      .map(_.koulutusmoduuli)
      .exists(k => k.tunniste.koodiarvo == "VVAI22")

    def haePerusteet(perusteenDiaarinumero: String): List[EPerusteRakenne] = ePerusteet.findRakenteet(
      perusteenDiaarinumero,
      Some(EPerusteetValidationUtils.getVaadittuPerusteenVoimassaolopäivä(oo.alkamispäivä, oo.päättymispäivä))
    )

    HttpStatus.fold(
      oo.suoritukset.map {
        case suoritus: AmmatillisenTutkinnonSuoritus if löytyyVVAI22(suoritus) =>
          suoritus.koulutusmoduuli.perusteenDiaarinumero match {
            case Some(diaarinumero) if !onKoodistossa(diaarinumero) =>
              HttpStatus.validate(
                haePerusteet(diaarinumero).exists(peruste => peruste.voimassaoloAlkaaLocalDate.exists(d => !d.isBefore(rajapäivä)))
              ) {
                KoskiErrorCategory.badRequest.validation.ammatillinen.yhteinenTutkinnonOsaVVAI22()
              }
            case _ => HttpStatus.ok
          }
        case _ => HttpStatus.ok
      }
    )
  }
}
