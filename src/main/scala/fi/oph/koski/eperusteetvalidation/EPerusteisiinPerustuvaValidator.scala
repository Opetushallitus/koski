package fi.oph.koski.eperusteetvalidation

import fi.oph.koski.eperusteet.{EPerusteRakenne, EPerusteetRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{DiaarinumerollinenKoulutus, _}
import fi.oph.koski.tutkinto.Koulutustyyppi.{Koulutustyyppi, ammatillinenPerustutkintoErityisopetuksena, ammatillisenPerustutkinnonTyypit, valmaErityisopetuksena}
import fi.oph.koski.tutkinto.TutkintoRepository
import mojave.{lens, traversal}
import mojave._

import java.time.LocalDate

class EPerusteisiinPerustuvaValidator(
  ePerusteet: EPerusteetRepository,
  tutkintoRepository: TutkintoRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu
) extends Logging {
  private val tutkintorakenneValidator: TutkintoRakenneValidator = TutkintoRakenneValidator(tutkintoRepository, koodistoViitePalvelu)

  def addKoulutustyyppi(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    koulutustyyppiTraversal.modify(oo) { koulutus =>
      val koulutustyyppi = koulutus match {
        case np: NuortenPerusopetus =>
          np.perusteenDiaarinumero.flatMap(haeKoulutustyyppi)
        case _ =>
          // 30.9.2022: voisiko koulutustyypit hakea EPerusteista myös lopuille tapauksille?
          val koulutustyyppiKoodisto = koodistoViitePalvelu.koodistoPalvelu.getLatestVersionRequired("koulutustyyppi")
          val koulutusTyypit = koodistoViitePalvelu.getSisältyvätKoodiViitteet(koulutustyyppiKoodisto, koulutus.tunniste).toList.flatten
          koulutusTyypit.filterNot(koodi => List(ammatillinenPerustutkintoErityisopetuksena.koodiarvo, valmaErityisopetuksena.koodiarvo).contains(koodi.koodiarvo)).headOption
      }
      lens[Koulutus].field[Option[Koodistokoodiviite]]("koulutustyyppi").set(koulutus)(koulutustyyppi)
    }
  }

  private def koulutustyyppiTraversal =
    traversal[KoskeenTallennettavaOpiskeluoikeus]
      .field[List[PäätasonSuoritus]]("suoritukset")
      .items
      .field[Koulutusmoduuli]("koulutusmoduuli")
      .ifInstanceOf[Koulutus]

  def validateTutkintorakenne(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = HttpStatus.fold(
    opiskeluoikeus.suoritukset.map(
      validateTutkintorakenne(
        _,
        opiskeluoikeus.tila.opiskeluoikeusjaksot.find(_.tila.koodiarvo == "lasna").map(_.alku),
        opiskeluoikeus.päättymispäivä
      )
    )
  )

  private def validateTutkintorakenne(
    suoritus: PäätasonSuoritus,
    alkamispäiväLäsnä: Option[LocalDate],
    opiskeluoikeudenPäättymispäivä: Option[LocalDate]
  ): HttpStatus = tutkintorakenneValidator.validate(suoritus, alkamispäiväLäsnä, opiskeluoikeudenPäättymispäivä)

  def fillPerusteenNimi(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = oo match {
    case a: AmmatillinenOpiskeluoikeus => a.withSuoritukset(
      a.suoritukset.map {
        case s: AmmatillisenTutkinnonSuoritus =>
          s.copy(koulutusmoduuli = s.koulutusmoduuli.copy(perusteenNimi = s.koulutusmoduuli.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, oo.päättymispäivä))))
        case s: NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
          s.copy(tutkinto = s.tutkinto.copy(perusteenNimi = s.tutkinto.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, oo.päättymispäivä))))
        case s: AmmatillisenTutkinnonOsittainenSuoritus =>
          s.copy(koulutusmoduuli = s.koulutusmoduuli.copy(perusteenNimi = s.koulutusmoduuli.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, oo.päättymispäivä))))
        case o => o
      })
    case x => x
  }

  def validateTutkinnonosanRyhmä(suoritus: Suoritus, opiskeluoikeudenPäättymispäivä: Option[LocalDate]): HttpStatus = {
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

  private def perusteenNimi(diaariNumero: String, päivä: Option[LocalDate]): Option[LocalizedString] = {
    ePerusteet.findPerusteenYksilöintitiedot(diaariNumero, päivä)
      .headOption
      .map(_.nimi)
      .flatMap(LocalizedString.sanitize)
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
    if(opiskeluoikeus.päättymispäivä.isDefined) {
      validatePerusteVoimassa(opiskeluoikeus.suoritukset, opiskeluoikeus.päättymispäivä.get)
    } else {
      HttpStatus.ok
    }
  }

  private def validatePerusteVoimassa(
    suoritukset: List[KoskeenTallennettavaPäätasonSuoritus],
    tarkastelupäivä: LocalDate
  ): HttpStatus = {
    HttpStatus.fold(
      suoritukset.map(s =>
        (s, s.koulutusmoduuli) match {
          case (_: ValmaKoulutuksenSuoritus, _) if tarkastelupäivä.isBefore(LocalDate.of(2022, 10, 2)) =>
            // Valmassa erikoistapauksena hyväksytään valmistuminen pidempään TUVA-siirtymän vuoksi
            // Katso myös TutkintoRakenneValidator.validateTutkintoRakenne
            HttpStatus.ok
          case (s: NäyttötutkintoonValmistavanKoulutuksenSuoritus, _) if s.tutkinto.perusteenDiaarinumero.isDefined =>
            validatePerusteVoimassa(s.tutkinto.perusteenDiaarinumero.get, tarkastelupäivä)
          case (_, diaarillinen: Diaarinumerollinen) if diaarillinen.perusteenDiaarinumero.isDefined =>
            validatePerusteVoimassa(diaarillinen.perusteenDiaarinumero.get, tarkastelupäivä)
          case _ => HttpStatus.ok
        }
      )
    )
  }

  private def validatePerusteVoimassa(diaarinumero: String, tarkastelupäivä: LocalDate): HttpStatus = {
    lazy val voimassaolleetPerusteet = ePerusteet.findRakenteet(diaarinumero, Some(tarkastelupäivä))

    if (onKoodistossa(diaarinumero) || voimassaolleetPerusteet.nonEmpty) {
      HttpStatus.ok
    } else {
      val kaikkiPerusteet = ePerusteet.findKaikkiRakenteet(diaarinumero)
      if (kaikkiPerusteet.isEmpty) {
        KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla " + diaarinumero)
      } else {
        KoskiErrorCategory.badRequest.validation.rakenne.perusteenVoimassaoloPäättynyt()
      }
    }
  }

  def validateVanhanOpiskeluoikeudenTapaukset(
    vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus
  ): HttpStatus = {
    (vanhaOpiskeluoikeus, uusiOpiskeluoikeus) match {
      case (vanha: AmmatillinenOpiskeluoikeus, uusi: AmmatillinenOpiskeluoikeus) =>
        validateTutkintokoodinTaiSuoritustavanMuutos(vanha, uusi)
      case _ => HttpStatus.ok
    }
  }

  private def validateTutkintokoodinTaiSuoritustavanMuutos(
    vanhaOpiskeluoikeus: AmmatillinenOpiskeluoikeus,
    uusiOpiskeluoikeus: AmmatillinenOpiskeluoikeus
  ): HttpStatus = {
    val vanhanSuoritustavat = suoritustavat(vanhaOpiskeluoikeus)
    val uudenSuoritustavat = suoritustavat(uusiOpiskeluoikeus)

    // Päätason suorituksia on enemmän kuin 1 vain näyttötutkinto + näyttöön valmistavan tapauksessa,
    // joka on hiljalleen poistumassa käytöstä. Jotta voidaan tukea päätason suorituksen poistoa,
    // kun halutaan poistaa virheellisesti lisätty näyttöön valmistavan suoritus, riittää, että
    // 1 tutkintokoodi mätsää.
    val suoritusTavatLöytyvät = vanhanSuoritustavat.isEmpty || vanhanSuoritustavat.exists(koodi => uudenSuoritustavat.contains(koodi))
    val tutkintokooditLöytyvät = checkTutkintokooditLöytyvät(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)

    if (suoritusTavatLöytyvät && tutkintokooditLöytyvät) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.ammatillinen.muutettuSuoritustapaaTaiTutkintokoodia()
    }
  }

  private def checkTutkintokooditLöytyvät(
    vanhaOpiskeluoikeus: AmmatillinenOpiskeluoikeus,
    uusiOpiskeluoikeus: AmmatillinenOpiskeluoikeus
  ): Boolean = {
    // Optimointi: Tarkistetaan eperusteista löytyvyys vain jos tarpeen eli jos tutkintokoodit eivät alustavasti mätsää
    val vanhanTutkintokoodit = tutkintokoodit(vanhaOpiskeluoikeus)
    val uudenTutkintokoodit = tutkintokoodit(uusiOpiskeluoikeus)

    // Päätason suorituksia on enemmän kuin 1 vain näyttötutkinto + näyttöön valmistavan tapauksessa,
    // joka on hiljalleen poistumassa käytöstä. Jotta voidaan tukea päätason suorituksen poistoa,
    // kun halutaan poistaa virheellisesti lisätty näyttöön valmistavan suoritus, riittää, että
    // 1 tutkintokoodi mätsää.
    if (vanhanTutkintokoodit.isEmpty || vanhanTutkintokoodit.exists(koodi => uudenTutkintokoodit.contains(koodi))) {
      true
    } else {
      val vanhanTutkintokooditEperusteettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(vanhaOpiskeluoikeus, vanhaOpiskeluoikeus.päättymispäivä)
      val uudenTutkintokooditEperusteettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(uusiOpiskeluoikeus, uusiOpiskeluoikeus.päättymispäivä)

      vanhanTutkintokooditEperusteettomat.count(koodi => uudenTutkintokooditEperusteettomat.contains(koodi)) == vanhanTutkintokooditEperusteettomat.length
    }
  }

  private def tutkintokooditPoislukienPerusteestaLöytymättömät(oo: AmmatillinenOpiskeluoikeus, päivä: Option[LocalDate]): List[String] = {
    oo.suoritukset.filter(suoritus =>
      suoritus.koulutusmoduuli match {
        case diaarillinen: DiaarinumerollinenKoulutus =>
          diaarillinen.perusteenDiaarinumero match {
            case Some(diaarinumero) if !onKoodistossa(diaarinumero) =>
              ePerusteet
                .findTarkatRakenteet(diaarinumero, päivä)
                .exists(_.koulutukset.exists(_.koulutuskoodiArvo == diaarillinen.tunniste.koodiarvo))
            case _ => true
          }
        case _ => true
      }
    ).map(_.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def tutkintokoodit(oo: AmmatillinenOpiskeluoikeus): List[String] = {
    oo.suoritukset.map(
      _.koulutusmoduuli.tunniste.koodiarvo
    )
  }

  private def suoritustavat(oo: AmmatillinenOpiskeluoikeus): List[String] = {
    oo.suoritukset.collect {
      case osittainenTaiKokonainen: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus => osittainenTaiKokonainen.suoritustapa.koodiarvo
      case _: NäyttötutkintoonValmistavanKoulutuksenSuoritus => "valmentava"
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

    def haePerusteet(perusteenDiaarinumero: String) =
      ePerusteet.findRakenteet(perusteenDiaarinumero, oo.päättymispäivä)

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

  private def haeKoulutustyyppi(diaarinumero: String): Option[Koulutustyyppi] =
    // Lue koulutustyyppi aina uusimmasta perusteesta. Käytännössä samalla diaarinumerolla
    // julkaistuissa perusteessa koulutustyyppi ei voi vaihtua.
    tutkintoRepository.findUusinPerusteRakenne(diaarinumero).map(r => r.koulutustyyppi)

  private def onKoodistossa(diaarinumero: String): Boolean =
    koodistoViitePalvelu.onKoodistossa("koskikoulutustendiaarinumerot", diaarinumero)
}
