package fi.oph.koski.eperusteetvalidation

import com.typesafe.config.Config
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi.{Koulutustyyppi, ammatillinenPerustutkintoErityisopetuksena, ammatillisenPerustutkinnonTyypit, valmaErityisopetuksena}
import fi.oph.koski.tutkinto.TutkintoRepository
import mojave.{lens, traversal}
import mojave._

import java.time.LocalDate

class EPerusteisiinPerustuvaValidation(
  ePerusteet: EPerusteetRepository,
  config: Config,
  tutkintoRepository: TutkintoRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu
) extends Logging {
  private val tutkintorakenneValidator: TutkintoRakenneValidator = TutkintoRakenneValidator(tutkintoRepository, koodistoViitePalvelu)

  def addKoulutustyyppi(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    koulutustyyppiTraversal.modify(oo) { koulutus =>
      val koulutustyyppi = koulutus match {
        case np: NuortenPerusopetus =>
          // Lue koulutustyyppi aina uusimmasta perusteesta, jos samalla diaarinumerolla sattuu olemaan monta. Käytännössä samalla diaarinumerolla
          // julkaistussa perusteessa koulutustyyppi ei voi vaihtua.
          np.perusteenDiaarinumero.flatMap(diaarinumero => tutkintoRepository.findUusinPerusteRakenne(diaarinumero).map(_.koulutustyyppi))
        case _ =>
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

  def validateTutkintorakenne(suoritus: PäätasonSuoritus, alkamispäivä: Option[LocalDate], opiskeluoikeudenPäättymispäivä: Option[LocalDate]): HttpStatus =
    tutkintorakenneValidator.validate(suoritus, alkamispäivä, opiskeluoikeudenPäättymispäivä)

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
      koulutustyyppi(tutkinnonSuoritus.koulutusmoduuli.perusteenDiaarinumero.get)
        .map(tyyppi => tutkinnonSuoritus match {
          case tutkinnonSuoritus: AmmatillisenTutkinnonSuoritus => HttpStatus.fold(suoritukset.toList.flatten.map(s => validateTutkinnonosaSuoritus(tutkinnonSuoritus, s, tyyppi)))
          case _ => HttpStatus.ok
        })
        .getOrElse {
          logger.warn("Ammatilliselle tutkintokoulutukselle " + tutkinnonSuoritus.koulutusmoduuli.perusteenDiaarinumero.get + " ei löydy koulutustyyppiä e-perusteista.")
          HttpStatus.ok
        }
    }

    // TODO: tarkista, voiko koulutustyyppi muuttua. Jos voi, niin tässä pitää käsitellä ja palauttaa monta.
    def koulutustyyppi(diaarinumero: String): Option[Koulutustyyppi] = tutkintoRepository.findPerusteRakenteet(diaarinumero, opiskeluoikeudenPäättymispäivä).headOption.map(r => r.koulutustyyppi)

    suoritus match {
      case s: AmmatillisenTutkinnonSuoritus => validateTutkinnonosaSuoritukset(s, s.osasuoritukset)
      case s: AmmatillisenTutkinnonOsittainenSuoritus => validateTutkinnonosaSuoritukset(s, s.osasuoritukset)
      case _ => HttpStatus.ok
    }
  }
  private def perusteenNimi(diaariNumero: String, päivä: Option[LocalDate]): Option[LocalizedString] = {
    ePerusteet.findPerusteenYksilöintitiedot(diaariNumero, päivä)
      .sortBy(_.luotu)(Ordering[Option[Long]]).reverse
      .headOption
      .map(_.nimi)
      .flatMap(LocalizedString.sanitize)
  }
  def validateAmmatillinenOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus match {
      case ammatillinen: AmmatillinenOpiskeluoikeus =>
        HttpStatus.fold(
          validatePerusteVoimassa(ammatillinen),
          validateViestintäJaVuorovaikutusÄidinkielellä2022(ammatillinen)
        )
      case _ => HttpStatus.ok
    }
  }

  private def validatePerusteVoimassa(opiskeluoikeus: AmmatillinenOpiskeluoikeus): HttpStatus = {
    val validaatioViimeinenPäiväEnnenVoimassaoloa = LocalDate.parse(config.getString("validaatiot.ammatillisenPerusteidenVoimassaoloTarkastusAstuuVoimaan")).minusDays(1)
    val voimassaolotarkastusAstunutVoimaan = LocalDate.now().isAfter(validaatioViimeinenPäiväEnnenVoimassaoloa)

    if (voimassaolotarkastusAstunutVoimaan && opiskeluoikeus.päättymispäivä.isDefined) {
      validatePerusteVoimassa(opiskeluoikeus.suoritukset, opiskeluoikeus.päättymispäivä.get)
    } else {
      HttpStatus.ok
    }
  }

  private def validatePerusteVoimassa(suoritukset: List[AmmatillinenPäätasonSuoritus], tarkastelupäivä: LocalDate): HttpStatus = {
    HttpStatus.fold(
      suoritukset.map(s =>
        (s, s.koulutusmoduuli) match {
          case (_: ValmaKoulutuksenSuoritus, _) if tarkastelupäivä.isBefore(LocalDate.of(2022, 10, 2)) =>
            // Valmassa erikoistapauksena hyväksytään valmistuminen pidempään TUVA-siirtymän vuoksi
            HttpStatus.ok
          case (_, diaarillinen: DiaarinumerollinenKoulutus) if diaarillinen.perusteenDiaarinumero.isDefined =>
            validatePerusteVoimassa(diaarillinen.perusteenDiaarinumero.get, tarkastelupäivä)
          case _ => HttpStatus.ok
        }
      )
    )
  }

  private def validatePerusteVoimassa(diaarinumero: String, tarkastelupäivä: LocalDate): HttpStatus = {
    val kaikkiPerusteet = ePerusteet.findKaikkiRakenteet(diaarinumero)
    lazy val voimassaolleetPerusteet = ePerusteet.findRakenteet(diaarinumero, Some(tarkastelupäivä))

    if (kaikkiPerusteet.isEmpty || voimassaolleetPerusteet.nonEmpty) {
      // TODO: Miksi tässä on aiemmin hyväksytty tilanne, jossa perustetta ei löydy lainkaan? Ilmeisesti siksi, että diaari voi silti olla
      // hyväksytty, koska on koodistossa, vaikkei perustetta löydy lainkaan? Ehkä validaatiot voisi yhdistää jotenkin, tai jotenkin skipata
      // automaattisesti kaikki perustevalidaatiot, jos perusteen nimi on koodistossa?
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.rakenne.perusteenVoimassaoloPäättynyt()
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
  ) = {
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
      // TODO: Välitä päivä oikein opiskeluoikeudesta
      val vanhanTutkintokooditEperusteettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(vanhaOpiskeluoikeus, None)
      val uudenTutkintokooditEperusteeettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(uusiOpiskeluoikeus, None)

      vanhanTutkintokooditEperusteettomat.count(koodi => uudenTutkintokooditEperusteeettomat.contains(koodi)) == vanhanTutkintokooditEperusteettomat.length
    }
  }

  private def tutkintokooditPoislukienPerusteestaLöytymättömät(oo: AmmatillinenOpiskeluoikeus, päivä: Option[LocalDate]): List[String] = {
    oo.suoritukset.filter(suoritus =>
      suoritus.koulutusmoduuli match {
        case diaarillinen: DiaarinumerollinenKoulutus =>
          // TODO: Käsittele monta perustetta oikein, palauta kaikki kelvolliste tutkintokoodit
          diaarillinen.perusteenDiaarinumero.flatMap(diaari => ePerusteet.findTarkatRakenteet(diaari, päivä).headOption) match {
            case Some(rakenne) =>
              rakenne.koulutukset.exists(_.koulutuskoodiArvo == diaarillinen.tunniste.koodiarvo)
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

    def haePeruste(suoritus: AmmatillinenPäätasonSuoritus) =
      suoritus.koulutusmoduuli match {
        case diaarillinen: DiaarinumerollinenKoulutus =>
          // TODO: Käsittele monta paluuarvoa oikein
          diaarillinen.perusteenDiaarinumero.flatMap(dNro => ePerusteet.findRakenteet(dNro, oo.päättymispäivä).headOption)
        case _ => None
      }

    HttpStatus.fold(
      oo.suoritukset.flatMap {
        case suoritus: AmmatillisenTutkinnonSuoritus if löytyyVVAI22(suoritus) =>
          haePeruste(suoritus).map { peruste =>
            HttpStatus.validate(peruste.voimassaoloAlkaaLocalDate.exists(d => !d.isBefore(rajapäivä))) {
              KoskiErrorCategory.badRequest.validation.ammatillinen.yhteinenTutkinnonOsaVVAI22()
            }
          }
        case _ => Some(HttpStatus.ok)
      }
    )
  }
}
