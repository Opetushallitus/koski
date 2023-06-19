package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.http.KoskiErrorCategory.badRequest.validation.laajuudet._
import fi.oph.koski.http.KoskiErrorCategory.badRequest.validation.tila.tuvaSuorituksenOpiskeluoikeidenTilaVääräKoodiarvo
import fi.oph.koski.schema._

import java.time.LocalDate

object TutkintokoulutukseenValmentavaKoulutusValidation {

  private def getTuvaLaajuusValidaationRajapäivä(config: Config): LocalDate = {
    LocalDate.parse(config.getString("validaatiot.tuvaLaajuusValidaatioMuutoksetAstuvatVoimaan"))
  }

  /**
   * Suorittaa annetuista funktioista toisen riippuen siitä onko Tuva laajuuksien validaation rajapäivä mennyt.
   * Rajapäivän jälkeen tämän rakenteen voi purkaa ja siirtyä käyttämään vain rajapäivän jälkeisiä toimintoja
   * kaikkialla missä tätä funktiota on kutsuttu.
   */
  def validateLaajuusRajapäivääEnnenTaiJälkeen[A](config: Config, rajapäivääEnnen: () => A, rajapäivänJälkeen: () => A): A = {
    if(getTuvaLaajuusValidaationRajapäivä(config).isBefore(LocalDate.now())){
      rajapäivänJälkeen()
    } else {
      rajapäivääEnnen()
    }
  }
  def validateOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    HttpStatus.fold(oo.tila.opiskeluoikeusjaksot.map(validateOpiskeluoikeusjaksonRahoitusmuoto))

  def validateTuvaSuoritus(config: Config, suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    suoritus match {
      case suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus =>
        HttpStatus.fold(
          validateLaajuusRajapäivääEnnenTaiJälkeen(
            config,
            () => validateTuvaSuorituksenLaajuusJaRakenne(suoritus, opiskeluoikeus),
            () => validateTuvaSuorituksenLaajuusJaRakenneRajapäivänJälkeen(suoritus, opiskeluoikeus)
          ),
          validateTuvaSallitutOpiskeluoikeudenTilat(opiskeluoikeus),
          validateTuvaSuorituksenOpiskeluoikeudenTila(opiskeluoikeus)
        )
      case _ =>
        HttpStatus.ok
    }
  }

  /*
    Valmistunut-tilan saa merkitä, kun opiskelija on suorittanut vähintään neljä (4) viikkoa suorituksia,
      joista kaksi (2) viikkoa oltava suoritettu yhteistä koulutuksen osaa Opiskelu- ja urasuunnittelutaidot
      ja lisäksi kaksi vähintään á yhden viikon (1) laajuista muuta koulutuksen osaa.
    Suorituksia tulee siis olla kolmesta eri koulutuksen osasta (2 + 1 + 1 viikkoa) yhteensä neljän (4) viikon edestä,
      joista kaksi (2) viikkoa tule olla pakollisesta yhteisestä koulutuksen osasta.
   */
  private def validateTuvaSuorituksenLaajuusJaRakenne(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus
  ): HttpStatus = {
    val opiskeluOikeudenTilaValmistunut =
      opiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo).contains("valmistunut")
    val päätasonSuorituksenLaajuusViikkoina = suoritus.koulutusmoduuli.laajuusArvo(default = 0.0)

    if (opiskeluOikeudenTilaValmistunut && 4.0 <= päätasonSuorituksenLaajuusViikkoina && päätasonSuorituksenLaajuusViikkoina <= 200.0) {
      HttpStatus.fold(
        validateOsasuoritustenLaajuus(suoritus),
        validateTuvaOpiskeluJaUrasuunnittelutaidotOsasuoritusOlemassa(suoritus),
        validateTuvaMuutOsasuorituksetOlemassa(suoritus)
      )
    } else if (!opiskeluOikeudenTilaValmistunut) {
      HttpStatus.ok
    } else {
      tuvaPäätasonSuoritusVääräLaajuus()
    }
  }

  /*
      1.8.2023 alkaen: valmistunut-tilan saa merkitä, kun oppija on suorittanut vähintään kaksi (2) viikkoa
        yhteistä koulutuksen osaa Opiskelu- ja urasuunnittelutaidot.
   */
  private def validateTuvaSuorituksenLaajuusJaRakenneRajapäivänJälkeen(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus
  ): HttpStatus = {
    val opiskeluOikeudenTilaValmistunut =
      opiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo).contains("valmistunut")
    val päätasonSuorituksenLaajuusViikkoina = suoritus.koulutusmoduuli.laajuusArvo(default = 0.0)

    if (opiskeluOikeudenTilaValmistunut && 2.0 <= päätasonSuorituksenLaajuusViikkoina) {
      HttpStatus.fold(
        validateOsasuoritustenLaajuusRajapäivänJälkeen(suoritus),
        validateTuvaOpiskeluJaUrasuunnittelutaidotOsasuoritusOlemassaRajapäivänJälkeen(suoritus)
      )
    } else if (!opiskeluOikeudenTilaValmistunut) {
      HttpStatus.ok
    } else {
      tuvaPäätasonSuoritusVääräLaajuus()
    }
  }

  def validateOpiskeluoikeusjaksonRahoitusmuoto(jakso: Opiskeluoikeusjakso): HttpStatus = {
    val tuvanRahoitustiedonVaativatTilat = List("lasna", "valmistunut", "loma")

    jakso match {
      case j: TutkintokoulutukseenValmentavanOpiskeluoikeusjakso if j.opintojenRahoitus.isEmpty && tuvanRahoitustiedonVaativatTilat.contains(j.tila.koodiarvo) =>
        KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto()
      case _ => HttpStatus.ok
    }
  }

  private def validateTuvaSallitutOpiskeluoikeudenTilat(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = opiskeluoikeus match {
    // Tämä tilan tarkistus on tehty validaationa eikä tietomalliin siksi, että tuotantoon ehti livahtaa väärää dataa.
    case a: TutkintokoulutukseenValmentavanOpiskeluoikeus =>
      HttpStatus.validate(!a.tila.opiskeluoikeusjaksot.exists(j => j.tila.koodiarvo == "eronnut"))(
        KoskiErrorCategory.badRequest.validation.tila.tuvaSuorituksenOpiskeluoikeidenTilaVääräKoodiarvo(
          s"""Opiskeluoikeuden tila "Eronnut" ei ole sallittu tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeudessa. Käytä tilaa "Katsotaan eronneeksi"."""
        )
      )
    case _ => HttpStatus.ok
  }

  private def validateTuvaSuorituksenOpiskeluoikeudenTila(
                                                       opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus
                                                     ): HttpStatus = opiskeluoikeus match {
    case a: TutkintokoulutukseenValmentavanOpiskeluoikeus if a.järjestämislupa.koodiarvo != "ammatillinen" && a.tila.opiskeluoikeusjaksot.exists(_.tila.koodiarvo == "loma") => tuvaSuorituksenOpiskeluoikeidenTilaVääräKoodiarvo()
    case _ => HttpStatus.ok
  }

  private def validateOsasuoritustenLaajuus(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): HttpStatus = {
    val validointiTulokset = suoritus.osasuoritukset.getOrElse(List.empty).map(_.koulutusmoduuli).map {
      case t: TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot =>
        laajuusVälillä(min = 2, max = 10, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen opiskelu- ja urasuunnittelutaitojen osasuorituksen laajuus on oltava vähintään 2 ja enintään 10 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen =>
        laajuusVälillä(min = 1, max = 30, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen perustaitojen vahvistamisen osasuorituksen laajuus on oltava vähintään 1 ja enintään 30 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot =>
        laajuusVälillä(min = 1, max = 30, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen lukion opintojen osasuorituksen laajuus on oltava vähintään 1 ja enintään 30 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot =>
        laajuusVälillä(min = 1, max = 30, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen ammatillisen koulutuksen opintojen osasuorituksen laajuus on oltava vähintään 1 ja enintään 30 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =>
        laajuusVälillä(min = 1, max = 20, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen työelämätaitojen ja työpaikalla tapahtuvan oppimisen osasuorituksen laajuus on oltava vähintään 1 ja enintään 20 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =>
        laajuusVälillä(min = 1, max = 20, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen arjen ja yhteiskunnallisen osallisuuden taitojen osasuorituksen laajuus on oltava vähintään 1 ja enintään 20 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa =>
        laajuusVälillä(min = 1, max = 10, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen valinnaisen osasuorituksen laajuus on oltava vähintään 1 ja enintään 10 viikkoa."
        ))
      case _ => HttpStatus.ok
    }

    HttpStatus.fold(validointiTulokset)
  }

  private def validateOsasuoritustenLaajuusRajapäivänJälkeen(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): HttpStatus = {
    val validointiTulokset = suoritus.osasuoritukset.getOrElse(List.empty).map(_.koulutusmoduuli).map {
      case t: TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot =>
        laajuusVälillä(min = 2, max = 10, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen opiskelu- ja urasuunnittelutaitojen osasuorituksen laajuus on oltava vähintään 2 ja enintään 10 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen =>
        laajuusEnintään(max = 30, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen perustaitojen vahvistamisen osasuorituksen laajuus on oltava enintään 30 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot =>
        laajuusEnintään(max = 30, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen lukion opintojen osasuorituksen laajuus on oltava enintään 30 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot =>
        laajuusEnintään(max = 30, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen ammatillisen koulutuksen opintojen osasuorituksen laajuus on oltava enintään 30 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =>
        laajuusEnintään(max = 20, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen työelämätaitojen ja työpaikalla tapahtuvan oppimisen osasuorituksen laajuus on oltava enintään 20 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =>
        laajuusEnintään(max = 20, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen arjen ja yhteiskunnallisen osallisuuden taitojen osasuorituksen laajuus on oltava enintään 20 viikkoa."
        ))
      case t: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa =>
        laajuusEnintään(max = 10, t, tuvaOsaSuoritusVääräLaajuus(
          "Tutkintokoulutukseen valmentavan koulutuksen valinnaisen osasuorituksen laajuus on oltava enintään 10 viikkoa."
        ))
      case _ => HttpStatus.ok
    }

    HttpStatus.fold(validointiTulokset)
  }

  private def laajuusVälillä(min: Double, max: Double, k: Koulutusmoduuli, virheIlmoitus: HttpStatus): HttpStatus =
    HttpStatus.validate(min <= k.laajuusArvo(default = 0.0) && k.laajuusArvo(default = 0.0) <= max) {
      virheIlmoitus
    }

  private def laajuusEnintään(max: Double, k: Koulutusmoduuli, virheIlmoitus: HttpStatus): HttpStatus =
    HttpStatus.validate(k.laajuusArvo(default = 0.0) <= max) {
      virheIlmoitus
    }


  private def validateTuvaOpiskeluJaUrasuunnittelutaidotOsasuoritusOlemassa(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): HttpStatus = {
    HttpStatus.validate(
      suoritus.osasuoritukset.getOrElse(List.empty).map(_.koulutusmoduuli).exists {
        case _: TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot => true
        case _ => false
      }
    ) {
      KoskiErrorCategory.badRequest.validation.rakenne.tuvaOpiskeluJaUrasuunnittelutaitojenOsasuoritusPuuttuu()
    }
  }

  private def validateTuvaOpiskeluJaUrasuunnittelutaidotOsasuoritusOlemassaRajapäivänJälkeen(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): HttpStatus = {
    val opiskeluJaUrasuunnittelu = suoritus.osasuoritusLista.find(os => os.koulutusmoduuli.tunniste.koodiarvo == "101")

    HttpStatus.validate(
      opiskeluJaUrasuunnittelu.nonEmpty && opiskeluJaUrasuunnittelu.flatMap(_.viimeisinArviointi).exists(_.hyväksytty)
    ) {
      KoskiErrorCategory.badRequest.validation.rakenne.tuvaOpiskeluJaUrasuunnittelutaitojenOsasuoritusPuuttuu()
    }
  }
  private def validateTuvaMuutOsasuorituksetOlemassa(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): HttpStatus = {
    val muutOsasuoritukset =
      suoritus.osasuoritukset.getOrElse(List.empty).groupBy(_.koulutusmoduuli).filter {
        case (_: TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot, _) => false
        case (_, _) => true
      }

    HttpStatus.validate(muutOsasuoritukset.size >= 2) {
      KoskiErrorCategory.badRequest.validation.rakenne.tuvaOsasuorituksiaLiianVähän()
    }
  }

  def validateJärjestämislupaEiMuuttunut(
    oldState: KoskeenTallennettavaOpiskeluoikeus,
    newState: KoskeenTallennettavaOpiskeluoikeus
  ): HttpStatus = {
    (oldState, newState) match {
      case (oldOo: TutkintokoulutukseenValmentavanOpiskeluoikeus, newOo: TutkintokoulutukseenValmentavanOpiskeluoikeus) =>
        HttpStatus.validate(oldOo.järjestämislupa.koodiarvo == newOo.järjestämislupa.koodiarvo) {
          KoskiErrorCategory
            .badRequest(
              "Olemassaolevan tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeuden järjestämislupaa ei saa muuttaa."
            )
        }
      case _ => HttpStatus.ok
    }
  }
}
