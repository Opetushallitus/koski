package fi.oph.koski.validation

import fi.oph.koski.documentation.ExampleData.muutaKauttaRahoitettu
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.{EuropeanSchoolOfHelsinkiOpiskeluoikeus, EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso, EuropeanSchoolOfHelsinkiVuosiluokanSuoritus, Koodistokoodiviite, KoskeenTallennettavaOpiskeluoikeus, NurseryVuosiluokanSuoritus, PrimaryVuosiluokanSuoritus, SecondaryLowerVuosiluokanSuoritus, SecondaryUpperVuosiluokanSuoritus}

object EuropeanSchoolOfHelsinkiValidation {
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

  private def fillSuorituksenKoulutustyyppi(koodistoPalvelu: KoodistoViitePalvelu)(suoritus: EuropeanSchoolOfHelsinkiVuosiluokanSuoritus): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus = {
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
    }
  }

  private def eshKoulutustyyppi(koodistoPalvelu: KoodistoViitePalvelu): Option[Koodistokoodiviite] = {
    koodistoPalvelu.validate(Koodistokoodiviite(koodiarvo = "21", koodistoUri = "koulutustyyppi"))
  }
}
