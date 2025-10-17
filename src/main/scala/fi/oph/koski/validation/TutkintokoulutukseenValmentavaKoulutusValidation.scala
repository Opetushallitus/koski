package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.http.KoskiErrorCategory.badRequest.validation.laajuudet._
import fi.oph.koski.http.KoskiErrorCategory.badRequest.validation.tila.tuvaSuorituksenOpiskeluoikeidenTilaVääräKoodiarvo
import fi.oph.koski.schema._

object TutkintokoulutukseenValmentavaKoulutusValidation {

  def validateOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    HttpStatus.fold(oo.tila.opiskeluoikeusjaksot.map(validateOpiskeluoikeusjaksonRahoitusmuoto))

  def validateTuvaSuoritus(config: Config, suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    suoritus match {
      case suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus =>
        HttpStatus.fold(
          validateTuvaSuorituksenLaajuusJaRakenne(suoritus, opiskeluoikeus),
          validateTuvaSallitutOpiskeluoikeudenTilat(opiskeluoikeus),
          validateTuvaSuorituksenOpiskeluoikeudenTila(opiskeluoikeus)
        )
      case _ =>
        HttpStatus.ok
    }
  }

  /*
      1.8.2023 alkaen: valmistunut-tilan saa merkitä, kun oppija on suorittanut vähintään kaksi (2) viikkoa
        yhteistä koulutuksen osaa Opiskelu- ja urasuunnittelutaidot.
   */
  private def validateTuvaSuorituksenLaajuusJaRakenne(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus
  ): HttpStatus = {
    val opiskeluOikeudenTilaValmistunut =
      opiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo).contains("valmistunut")
    val päätasonSuorituksenLaajuusViikkoina = suoritus.koulutusmoduuli.laajuusArvo(default = 0.0)

    if (opiskeluOikeudenTilaValmistunut && 2.0 <= päätasonSuorituksenLaajuusViikkoina) {
      HttpStatus.fold(
        validateOsasuoritustenLaajuus(suoritus),
        validateTuvaOpiskeluJaUrasuunnittelutaidotOsasuoritusOlemassa(suoritus)
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
    val opiskeluJaUrasuunnittelu = suoritus.osasuoritusLista.find(os => os.koulutusmoduuli.tunniste.koodiarvo == "101")

    HttpStatus.validate(
      opiskeluJaUrasuunnittelu.nonEmpty && opiskeluJaUrasuunnittelu.flatMap(_.viimeisinArviointi).exists(_.hyväksytty)
    ) {
      KoskiErrorCategory.badRequest.validation.rakenne.tuvaOpiskeluJaUrasuunnittelutaitojenOsasuoritusPuuttuu()
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
