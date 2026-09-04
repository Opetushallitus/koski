package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{
  KoodiViite,
  MuunKuinSäännellynKoulutuksenOpiskeluoikeus,
  Opiskeluoikeus
}

import java.time.LocalDate

object MuunKuinSäännellynKoulutuksenValidation {
  private val arviointipäiväVaaditaanAlkaen = LocalDate.of(2027, 1, 1)

  def validateOpiskeluoikeus(opiskeluoikeus: Opiskeluoikeus): HttpStatus = opiskeluoikeus match {
    case oo: MuunKuinSäännellynKoulutuksenOpiskeluoikeus
      if oo.alkamispäivä.exists(alkamispäivä => !alkamispäivä.isBefore(arviointipäiväVaaditaanAlkaen)) =>
      validateOsasuoritustenArviointipäivät(oo)
    case _ => HttpStatus.ok
  }

  private def validateOsasuoritustenArviointipäivät(oo: MuunKuinSäännellynKoulutuksenOpiskeluoikeus): HttpStatus = {
    val arviointipäivättömienOsasuoritustenTunnisteet = oo.suoritukset
      .flatMap(_.rekursiivisetOsasuoritukset)
      .filter(_.arviointi.exists(_.exists(_.arviointipäivä.isEmpty)))
      .map(_.koulutusmoduuli.tunniste)

    HttpStatus.fold(arviointipäivättömienOsasuoritustenTunnisteet.map(tunniste =>
      KoskiErrorCategory.badRequest.validation.arviointi.arviointipäiväPuuttuu(
        s"Muun kuin säännellyn koulutuksen osasuoritukselta ${tunnisteTekstinä(tunniste, kieli = "fi")} puuttuu arviointipäivä"
      )
    ))
  }

  private def tunnisteTekstinä(tunniste: KoodiViite, kieli: String): String = {
    tunniste.getNimi.flatMap(_.getOptional(kieli)) match {
      case Some(nimi) if nimi != tunniste.koodiarvo => s"$nimi (${tunniste.koodiarvo})"
      case _ => tunniste.koodiarvo
    }
  }
}
