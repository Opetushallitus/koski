package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.documentation.ExampleData.muutaKauttaRahoitettu
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.{EBTutkinnonOsasuoritus, EBTutkinnonSuoritus, EuropeanSchoolOfHelsinkiOpiskeluoikeus, EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso, EuropeanSchoolOfHelsinkiPäätasonSuoritus, EuropeanSchoolOfHelsinkiVuosiluokanSuoritus, Koodistokoodiviite, KoskeenTallennettavaOpiskeluoikeus, NurseryVuosiluokanSuoritus, PrimaryVuosiluokanSuoritus, SecondaryLowerVuosiluokanSuoritus, SecondaryUpperOppiaineenSuoritus, SecondaryUpperVuosiluokanSuoritus}
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat

import java.time.LocalDate

object EuropeanSchoolOfHelsinkiValidation {

  def validateOpiskeluoikeus(config: Config)(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    oo match {
      case eshOo: EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
        HttpStatus.fold(
          validateTallennuspäivä(
            LocalDate.parse(config.getString("validaatiot.europeanSchoolOfHelsinkiAikaisinSallittuTallennuspaiva"))
          ),
          validatePäättymispäivä(
            eshOo,
            LocalDate.parse(config.getString("validaatiot.europeanSchoolOfHelsinkiAikaisinSallittuPaattymispaiva")),
          )
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateTallennuspäivä(aikaisinTallennuspäivä: LocalDate): HttpStatus = {
    if (LocalDate.now.isBefore(aikaisinTallennuspäivä)) {
      KoskiErrorCategory.badRequest.validation.esh.tallennuspäivä(s"Helsingin eurooppalaisen koulun opiskeluoikeuksia voi alkaa tallentaa vasta ${finnishDateFormat.format(aikaisinTallennuspäivä)} alkaen")
    } else {
      HttpStatus.ok
    }
  }

  private def validatePäättymispäivä(oo: EuropeanSchoolOfHelsinkiOpiskeluoikeus, aikaisinPäättymispäivä: LocalDate): HttpStatus = {
    oo.päättymispäivä.filter(_.isBefore(aikaisinPäättymispäivä)) match {
      case Some(_) =>
        KoskiErrorCategory.badRequest.validation.esh.päättymispäivä(s"Helsingin eurooppalaisen koulun tallennettavat opiskeluoikeudet eivät voi olla päättyneet ennen lain voimaantuloa ${finnishDateFormat.format(aikaisinPäättymispäivä)}")
      case _ => HttpStatus.ok
    }
  }

  def validateEBTutkinnonArvioinnit(suoritus: EBTutkinnonSuoritus): HttpStatus = {
    if (suoritus.vahvistettu && suoritus.yleisarvosana.isEmpty) {
      KoskiErrorCategory.badRequest.validation.esh.yleisarvosana()
    } else {
      HttpStatus.ok
    }
  }

  def fillRahoitusmuodot(koodistoPalvelu: KoodistoViitePalvelu)(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case e: EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
        e.copy(tila = e.tila.copy(
          opiskeluoikeusjaksot =
            fillRahoitusmuodot(e.tila.opiskeluoikeusjaksot, koodistoPalvelu)
        ))
      case _ => oo
    }
  }

  private def fillRahoitusmuodot(opiskeluoikeusjaksot: List[EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso], koodistoPalvelu: KoodistoViitePalvelu): List[EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso] = {
    opiskeluoikeusjaksot.map {
      case j if rahoitusmuotoTäydennetään(j) =>
        j.copy(opintojenRahoitus = koodistoPalvelu.validate(muutaKauttaRahoitettu))
      case j => j
    }
  }

  private def rahoitusmuotoTäydennetään(opiskeluoikeusjakso: EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso): Boolean =
    opiskeluoikeusjakso.opintojenRahoitus.isEmpty && tilatJoilleRahoitusmuotoTäydennetään.contains(opiskeluoikeusjakso.tila.koodiarvo)

  private lazy val tilatJoilleRahoitusmuotoTäydennetään = List("lasna", "valmistunut")

  def fillKoulutustyyppi(koodistoPalvelu: KoodistoViitePalvelu)(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    // Huomaa, että koulutustyypin täydentäminen tehdään diaarinumerollisille ja koulutus-koodistoa käyttäville koulutuksille EPerusteetFiller.addKoulutustyyppi -metodissa
    // ESH:ssa ei ole vuosiluokilla kumpaakaan.
    val result = oo match {
      case e: EuropeanSchoolOfHelsinkiOpiskeluoikeus => {
        val result = e.copy(suoritukset = e.suoritukset.map(fillSuorituksenKoulutustyyppi(koodistoPalvelu)))
        result
      }
      case _ => oo
    }

    result
  }

  private def fillSuorituksenKoulutustyyppi(koodistoPalvelu: KoodistoViitePalvelu)(suoritus: EuropeanSchoolOfHelsinkiPäätasonSuoritus): EuropeanSchoolOfHelsinkiPäätasonSuoritus = {
    suoritus match {
      case s: NurseryVuosiluokanSuoritus => s
      case s: PrimaryVuosiluokanSuoritus => s.copy(
        koulutusmoduuli = s.koulutusmoduuli.copy(
          koulutustyyppi = eshKoulutustyyppi(koodistoPalvelu)
        )
      )
      case s: SecondaryLowerVuosiluokanSuoritus => s.copy(
        koulutusmoduuli = s.koulutusmoduuli.copy(
          koulutustyyppi = eshKoulutustyyppi(koodistoPalvelu)
        )
      )
      case s: SecondaryUpperVuosiluokanSuoritus => s.copy(
        koulutusmoduuli = s.koulutusmoduuli.copy(
          koulutustyyppi = eshKoulutustyyppi(koodistoPalvelu)
        )
      )
      case s: EBTutkinnonSuoritus => s.copy(
        koulutusmoduuli = s.koulutusmoduuli.copy(
          koulutustyyppi = eshKoulutustyyppi(koodistoPalvelu)
        )
      )
    }
  }

  private def eshKoulutustyyppi(koodistoPalvelu: KoodistoViitePalvelu): Option[Koodistokoodiviite] = {
    koodistoPalvelu.validate(Koodistokoodiviite(koodiarvo = "21", koodistoUri = "koulutustyyppi"))
  }

  def osasuorituksetKunnossa(s: SecondaryUpperVuosiluokanSuoritus): Boolean = {
    s.osasuoritukset.exists(os => !os.isEmpty && os.forall(osasuorituksetKunnossa))
  }

  private def osasuorituksetKunnossa(s: SecondaryUpperOppiaineenSuoritus): Boolean = {
    def sisältää(koodiarvo: String) =
      s.osasuoritukset.exists(_.exists(_.koulutusmoduuli.tunniste.koodiarvo == koodiarvo))

    (sisältää("A") && sisältää("B")) || sisältää("yearmark")
  }

  def osasuorituksetKunnossa(s: EBTutkinnonSuoritus): Boolean = {
    s.osasuoritukset.exists(os => !os.isEmpty && os.forall(osasuorituksetKunnossa))
  }

  private def osasuorituksetKunnossa(s: EBTutkinnonOsasuoritus): Boolean = {
    def sisältää(koodiarvo: String) =
      s.osasuoritukset.exists(_.exists(_.koulutusmoduuli.tunniste.koodiarvo == koodiarvo))

    sisältää("Final")
  }
}
