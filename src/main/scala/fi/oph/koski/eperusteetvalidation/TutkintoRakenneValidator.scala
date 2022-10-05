package fi.oph.koski.eperusteetvalidation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi._
import fi.oph.koski.tutkinto.{Koulutustyyppi, _}

import java.time.LocalDate

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository, koodistoViitePalvelu: KoodistoViitePalvelu) extends Logging {
  def validate(suoritus: PäätasonSuoritus, alkamispäiväLäsnä: Option[LocalDate], opiskeluoikeudenPäättymispäivä: Option[LocalDate]): HttpStatus = {
    validateTutkintoRakenne(suoritus, alkamispäiväLäsnä, opiskeluoikeudenPäättymispäivä)
      .onSuccess(validateDiaarinumerollinenAmmatillinen(suoritus, opiskeluoikeudenPäättymispäivä))
  }

  private def validateTutkintoRakenne(
    suoritus: PäätasonSuoritus,
    alkamispäiväLäsnä: Option[LocalDate],
    opiskeluoikeudenPäättymispäivä: Option[LocalDate]
  ): HttpStatus = suoritus match {
    case tutkintoSuoritus: AmmatillisenTutkinnonSuoritus =>
      validateKoulutustyypitJaHaeRakenteet(tutkintoSuoritus.koulutusmoduuli, Some(ammatillisetKoulutustyypit), opiskeluoikeudenPäättymispäivä, Some(tutkintoSuoritus)) match {
        case Left(status) => status
        case Right(rakenteet) =>
          HttpStatus.fold {
            val tulokset = rakenteet.map(rakenne =>
              validateOsaamisalat(tutkintoSuoritus.osaamisala.toList.flatten.map(_.osaamisala), rakenne)
            )
            if(tulokset.exists(_.isOk)) {
              List(HttpStatus.ok)
            } else {
              tulokset
            }
          }.onSuccess(HttpStatus.fold(suoritus.osasuoritusLista.map {
            case osaSuoritus: AmmatillisenTutkinnonOsanSuoritus =>
              HttpStatus.fold(osaSuoritus.koulutusmoduuli match {
                case osa: ValtakunnallinenTutkinnonOsa =>
                  HttpStatus.fold {
                    val tulokset = rakenteet.map(rakenne =>
                      validateTutkinnonOsa(
                        osaSuoritus,
                        osa,
                        rakenne,
                        tutkintoSuoritus.suoritustapa,
                        alkamispäiväLäsnä,
                        opiskeluoikeudenPäättymispäivä
                      )
                    )
                    if(tulokset.exists(_.isOk)) {
                      List(HttpStatus.ok)
                    } else {
                      tulokset
                    }
                  }
                case osa: PaikallinenTutkinnonOsa =>
                  HttpStatus.ok // vain OpsTutkinnonosatoteutukset validoidaan, muut sellaisenaan läpi, koska niiden rakennetta ei tunneta
                case osa: KorkeakouluopinnotTutkinnonOsa =>
                  HttpStatus.ok
                case osa: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa =>
                  HttpStatus.ok
              }, validateTutkintoField(tutkintoSuoritus, osaSuoritus))
          }))
      }
    case n: NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
      HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(n.tutkinto, Some(ammatillisetKoulutustyypit), opiskeluoikeudenPäättymispäivä))
    case suoritus: AikuistenPerusopetuksenOppimääränSuoritus =>
      HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(suoritus.koulutusmoduuli, Some(List(aikuistenPerusopetus)), opiskeluoikeudenPäättymispäivä, Some(suoritus)))
    case suoritus: AmmatillisenTutkinnonOsittainenSuoritus =>
      HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(suoritus.koulutusmoduuli, Some(ammatillisetKoulutustyypit), opiskeluoikeudenPäättymispäivä, Some(suoritus)))
        .onSuccess(HttpStatus.fold(suoritus.osasuoritukset.toList.flatten.map(suoritus => validateTutkinnonOsanTutkinto(suoritus, opiskeluoikeudenPäättymispäivä))))
    case s: LukionPäätasonSuoritus2019 =>
      HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(s.koulutusmoduuli, Some(lukionKoulutustyypit), opiskeluoikeudenPäättymispäivä)).onSuccess(validateLukio2019Diaarinumero(s))
    case _ =>
      suoritus.koulutusmoduuli match {
        case d: Esiopetus =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(esiopetus)), opiskeluoikeudenPäättymispäivä))
        case d: AikuistenPerusopetus =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(aikuistenPerusopetus)), opiskeluoikeudenPäättymispäivä))
        case d: AikuistenPerusopetuksenAlkuvaihe =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(aikuistenPerusopetus)), opiskeluoikeudenPäättymispäivä))
        case d: PerusopetuksenDiaarinumerollinenKoulutus =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(perusopetus)), opiskeluoikeudenPäättymispäivä))
        case d: PerusopetukseenValmistavaOpetus =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(perusopetukseenValmistava)), opiskeluoikeudenPäättymispäivä))
        case d: PerusopetuksenLisäopetus =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(perusopetuksenLisäopetus)), opiskeluoikeudenPäättymispäivä))
        case d: AikuistenPerusopetuksenOppiaine =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(aikuistenPerusopetus)), opiskeluoikeudenPäättymispäivä))
        case d: NuortenPerusopetuksenOppiaine =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(perusopetus)), opiskeluoikeudenPäättymispäivä))
        case d: LukionOppimäärä =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(lukionKoulutustyypit), opiskeluoikeudenPäättymispäivä)).onSuccess(validateLukio2015Diaarinumero(d))
        case d: LukioonValmistavaKoulutus =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(luvaKoulutustyypit), opiskeluoikeudenPäättymispäivä))
        // Valmassa erikoistapauksena hyväksytään valmistuminen pidempään TUVA-siirtymän vuoksi
        // Katso myös EPerusteisiinPerustuvaValidation.validatePerusteVoimassa
        case d: ValmaKoulutus if opiskeluoikeudenPäättymispäivä.exists(päivä => päivä.isBefore(LocalDate.of(2022, 10, 2))) =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(valmaKoulutustyypit), None))
        case d: ValmaKoulutus =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(valmaKoulutustyypit), opiskeluoikeudenPäättymispäivä))
        case d: TelmaKoulutus =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(telma)), opiskeluoikeudenPäättymispäivä))
        case d: LukionOppiaine =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(lukionKoulutustyypit), opiskeluoikeudenPäättymispäivä)).onSuccess(validateLukio2015Diaarinumero(d))
        case d: Diaarinumerollinen =>
          HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, None, opiskeluoikeudenPäättymispäivä))
        case _ => HttpStatus.ok
      }
  }

  private def validateTutkintoField(tutkintoSuoritus: AmmatillisenTutkinnonSuoritus, osaSuoritus: AmmatillisenTutkinnonOsanSuoritus) = (tutkintoSuoritus.koulutusmoduuli.perusteenDiaarinumero, osaSuoritus.tutkinto.flatMap(_.perusteenDiaarinumero)) match {
    case (Some(tutkinnonDiaari), Some(osanDiaari)) if tutkinnonDiaari == osanDiaari =>
      KoskiErrorCategory.badRequest.validation.rakenne.samaTutkintokoodi(s"Tutkinnon osalle ${osaSuoritus.koulutusmoduuli.tunniste} on merkitty tutkinto, jossa on sama diaarinumero $tutkinnonDiaari kuin tutkinnon suorituksessa")
    case _ =>
      HttpStatus.ok
  }

  private def validateKoulutustyypitJaHaeRakenteet(
    tutkinto: Diaarinumerollinen,
    koulutustyypit: Option[List[Koulutustyyppi.Koulutustyyppi]],
    päivä: Option[LocalDate],
    suoritusVirheilmoitukseen: Option[PäätasonSuoritus] = None
  ): Either[HttpStatus, List[TutkintoRakenne]] = {
    validateDiaarinumero(tutkinto.perusteenDiaarinumero)
      .flatMap { diaarinumero =>
        if (onKoodistossa(diaarinumero)) {
          Left(KoskiErrorCategory.ok())
        } else {
          tutkintoRepository.findPerusteRakenteet(diaarinumero, päivä) match {
            case Nil => {
              val päiväInfo = päivä.map(p => s", joka on voimassa tai siirtymäajalla ${p.toString}, ").getOrElse("")

              logger.warn(s"Tutkinnon perustetta ${päiväInfo}ei löydy diaarinumerolla " + diaarinumero + " eperusteista eikä koskikoulutustendiaarinumerot-koodistosta")
              Left(KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari(s"Tutkinnon perustetta ${päiväInfo}ei löydy diaarinumerolla " + diaarinumero))
            }
            case rakenteet =>
              koulutustyypit match {
                case Some(koulutustyypit) if !rakenteet.exists(rakenne => koulutustyypit.contains(rakenne.koulutustyyppi)) =>
                  val tyyppiStr = suoritusVirheilmoitukseen.getOrElse(tutkinto) match {
                    case p: Product => p.productPrefix
                    case x: AnyRef => x.getClass.getSimpleName
                  }
                  Left(KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi(
                    rakenteet.map(rakenne =>
                      s"Suoritukselle $tyyppiStr ei voi käyttää perustetta ${rakenne.diaarinumero} (${rakenne.id}), jonka koulutustyyppi on ${Koulutustyyppi.describe(rakenne.koulutustyyppi)}. "
                    ).mkString +
                      s"Tälle suoritukselle hyväksytyt perusteen koulutustyypit ovat ${koulutustyypit.map(Koulutustyyppi.describe).mkString(", ")}."
                  ))
                case _ =>
                  Right(rakenteet)
              }
          }
        }
      }
  }

  private def validateDiaarinumerollinenAmmatillinen(suoritus: PäätasonSuoritus, opiskeluoikeudenPäättymispäivä: Option[LocalDate]) = suoritus.koulutusmoduuli match {
    case koulutusmoduuli: Diaarinumerollinen if suoritus.isInstanceOf[AmmatillinenPäätasonSuoritus] =>
      validateKoulutusmoduulinTunniste(koulutusmoduuli.tunniste, koulutusmoduuli.perusteenDiaarinumero, opiskeluoikeudenPäättymispäivä)
    case _ => HttpStatus.ok
  }

  private def validateKoulutusmoduulinTunniste(tunniste: KoodiViite, diaariNumero: Option[String], opiskeluoikeudenPäättymispäivä: Option[LocalDate]) = diaariNumero match {
    case None => KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu()
    case Some(diaari) if onKoodistossa(diaari) =>
      HttpStatus.ok
    case Some(diaari) =>
      val koulutukset = tutkintoRepository.findPerusteRakenteet(diaari, opiskeluoikeudenPäättymispäivä).flatMap(_.koulutukset.map(_.koodiarvo))
      HttpStatus.validate(koulutukset.isEmpty || koulutukset.contains(tunniste.koodiarvo))(
        KoskiErrorCategory.badRequest.validation.rakenne.tunnisteenKoodiarvoaEiLöydyRakenteesta(
          s"Tunnisteen koodiarvoa ${tunniste.koodiarvo} ei löytynyt rakenteen ${diaariNumero.get} mahdollisista koulutuksista. Tarkista tutkintokoodit ePerusteista."
        ))
  }

  private def validateDiaarinumero(diaarinumero: Option[String]): Either[HttpStatus, String] = {
    // Avoid sending totally bogus diaarinumeros to ePerusteet (e.g. 3000 characters long), as that leads
    // to "414 Request-URI Too Large" and eventually "Internal server error". Other than that, don't validate
    // the format (at least not yet), since in theory diaarinumero could contain spaces etc.
    diaarinumero match {
      case None => Left(KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu())
      case Some(d) if (d.length < 1) || (d.length > 30) => Left(KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Diaarinumeron muoto on virheellinen: " + diaarinumero.get.take(30)))
      case Some(d) => Right(d)
    }
  }

  private def validateOsaamisalat(osaamisalat: List[Koodistokoodiviite], rakenne: TutkintoRakenne): HttpStatus = {
    val tuntemattomatOsaamisalat: List[Koodistokoodiviite] = osaamisalat.filter(osaamisala => findOsaamisala(rakenne, osaamisala.koodiarvo).isEmpty)

    HttpStatus.fold(tuntemattomatOsaamisalat.map {
      osaamisala: Koodistokoodiviite => KoskiErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala(s"Osaamisala ${osaamisala.koodiarvo} ei löydy tutkintorakenteesta perusteelle ${rakenne.diaarinumero} (${rakenne.id})")
    })
  }

  private def validateTutkinnonOsanTutkinto(suoritus: TutkinnonOsanSuoritus, opiskeluoikeudenPäättymispäivä: Option[LocalDate]) = {
    suoritus.tutkinto match {
      case Some(tutkinto) => HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(tutkinto, Some(ammatillisetKoulutustyypit), opiskeluoikeudenPäättymispäivä))
      case None => HttpStatus.ok
    }
  }

  private def validateTutkinnonOsa(
    suoritus: AmmatillisenTutkinnonOsanSuoritus,
    osa: ValtakunnallinenTutkinnonOsa,
    rakenne: TutkintoRakenne,
    suoritustapa: Koodistokoodiviite,
    alkamispäiväLäsnä: Option[LocalDate],
    opiskeluoikeudenPäättymispäivä: Option[LocalDate]
  ): HttpStatus = {
    val suoritustapaJaRakenne = rakenne.findSuoritustapaJaRakenne(suoritustapa)
      .orElse {
        // TOR-384 Siirtymäaikana (vuonna 2018 aloittaneet) käytetään suoritustapaa "reformi", vaikka
        // opiskelisi vanhojen perusteiden mukaisesti. Mikäli ePerusteista ei löydy tutkinnon rakennetta
        // suoritustavalla "reformi", tarkistetaan löytyykö rakenne suoritustavalla "naytto" tai "ops" ja
        // validoidaan sen mukaan.
        if (suoritustapa == Suoritustapa.reformi && alkamispäiväLäsnä.exists(_.getYear == 2018)) rakenne.koulutustyyppi match {
          case Koulutustyyppi.ammattitutkinto | Koulutustyyppi.erikoisammattitutkinto => rakenne.findSuoritustapaJaRakenne(Suoritustapa.naytto)
          case k if Koulutustyyppi.ammatillisenPerustutkinnonTyypit.contains(k) => rakenne.findSuoritustapaJaRakenne(Suoritustapa.ops)
          case _ => None
        } else None
      }
    suoritustapaJaRakenne match {
      case Some(suoritustapaJaRakenne) =>
        (suoritus.tutkinto, suoritus.tutkinnonOsanRyhmä) match {
          case (Some(tutkinto), _) =>
            // Tutkinnon osa toisesta tutkinnosta.
            // Ei validoida rakenteeseen kuuluvuutta, vain se, että rakenne löytyy diaarinumerolla
            validateTutkinnonOsanTutkinto(suoritus, opiskeluoikeudenPäättymispäivä)
          case (_, Some(Koodistokoodiviite(koodiarvo, _, _, _, _))) if List("3", "4").contains(koodiarvo) =>
            // Vapaavalintainen tai yksilöllisesti tutkintoa laajentava osa
            // Ei validoida rakenteeseen kuuluvuutta
            HttpStatus.ok
          case (_, _) =>
            // Validoidaan tutkintorakenteen mukaisesti
            findTutkinnonOsa(suoritustapaJaRakenne, osa.tunniste) match {
              case None =>
                KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa(
                  s"Tutkinnon osa ${osa.tunniste} ei löydy tutkintorakenteesta perusteelle ${rakenne.diaarinumero} (${rakenne.id}) - suoritustapa ${suoritustapaJaRakenne.suoritustapa.koodiarvo}")
              case Some(tutkinnonOsa) =>
                HttpStatus.ok
            }
        }
      case None =>
        KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaaEiLöydyRakenteesta(s"Suoritustapaa ei löydy tutkinnon rakenteesta perusteelle ${rakenne.diaarinumero} (${rakenne.id})")
    }
  }

  private def findTutkinnonOsa(rakenne: SuoritustapaJaRakenne, koulutusModuuliTunniste: Koodistokoodiviite): Option[TutkinnonOsa] = {
    rakenne.rakenne.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste))
  }

  private def findTutkinnonOsa(rakenne: RakenneOsa, koulutusModuuliTunniste: Koodistokoodiviite): Option[TutkinnonOsa] = rakenne match {
    case t: TutkinnonOsa if t.tunniste == koulutusModuuliTunniste => Some(t)
    case t: RakenneModuuli => t.osat.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste)).headOption
    case _ => None
  }

  private def findOsaamisala(rakenne: TutkintoRakenne, osaamisAlaKoodi: String) = rakenne.osaamisalat.find(_.koodiarvo == osaamisAlaKoodi)

  private def validateLukio2019Diaarinumero(s: LukionPäätasonSuoritus2019) = {
    val diaarinumerorajaus = s.oppimäärä.koodiarvo match {
      case "aikuistenops" =>
        Perusteet.AikuistenLukiokoulutuksenOpetussuunnitelmanPerusteet2019
      case "nuortenops" =>
        Perusteet.LukionOpetussuunnitelmanPerusteet2019
      case _ =>
        Diaarinumerot(List())
    }
    val diaarinumero = s.koulutusmoduuli.perusteenDiaarinumero.getOrElse("")

    if (diaarinumerorajaus.matches(diaarinumero)) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari(s"""Väärä diaarinumero "$diaarinumero" suorituksella ${s.tyyppi.koodiarvo}, sallitut arvot: $diaarinumerorajaus""")
    }
  }

  private def validateLukio2015Diaarinumero(d: Diaarinumerollinen) = {
    val diaarinumero = d.perusteenDiaarinumero.getOrElse("")

    val lops2021Diaarinumerot = List(
      Perusteet.LukionOpetussuunnitelmanPerusteet2019.diaari,
      Perusteet.AikuistenLukiokoulutuksenOpetussuunnitelmanPerusteet2019.diaari
    )

    if (lops2021Diaarinumerot.contains(diaarinumero)) {
      KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari(s"Lukion aiemman opetusohjelman mukaisessa suorituksessa ei voi käyttää lukion 2019 opetussuunnitelman diaarinumeroa")
    } else {
      HttpStatus.ok
    }
  }

  private def onKoodistossa(diaarinumero: String): Boolean =
    koodistoViitePalvelu.onKoodistossa("koskikoulutustendiaarinumerot", diaarinumero)
}
