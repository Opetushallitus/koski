package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{
  KoodiViite,
  MuunKuinSäännellynKoulutuksenOpiskeluoikeus,
  MuunKuinSäännellynKoulutuksenOsasuoritus,
  MuunKuinSäännellynKoulutuksenPäätasonSuoritus,
  Opiskeluoikeus
}

import java.time.LocalDate

object MuunKuinSäännellynKoulutuksenValidation {
  private val arviointiVaaditaanAlkaen = LocalDate.of(2027, 1, 1)

  def validateOpiskeluoikeus(opiskeluoikeus: Opiskeluoikeus): HttpStatus = opiskeluoikeus match {
    case oo: MuunKuinSäännellynKoulutuksenOpiskeluoikeus
      if oo.alkamispäivä.exists(alkamispäivä => !alkamispäivä.isBefore(arviointiVaaditaanAlkaen)) =>
      HttpStatus.fold(
        validateOsasuoritustenArvioinnit(oo),
        validateOsasuoritustenArviointipäivät(oo),
      )
    case _ => HttpStatus.ok
  }

  private def validateOsasuoritustenArvioinnit(oo: MuunKuinSäännellynKoulutuksenOpiskeluoikeus): HttpStatus = {
    val viimeisinTila = oo.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo)
    val arvioimattomienOsasuoritustenTunnisteet = oo.suoritukset
      .filter(_.vahvistettu)
      .flatMap(arviointiaVaativatOsasuoritukset(_, viimeisinTila))
      .filterNot(_.arvioitu)
      .map(_.koulutusmoduuli.tunniste)

    HttpStatus.fold(arvioimattomienOsasuoritustenTunnisteet.map(tunniste =>
      KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(
        s"Muun kuin säännellyn koulutuksen osasuoritukselta ${tunnisteTekstinä(tunniste, kieli = "fi")} puuttuu arviointi"
      )
    ))
  }

  private def arviointiaVaativatOsasuoritukset(
    suoritus: MuunKuinSäännellynKoulutuksenPäätasonSuoritus,
    tila: Option[String],
  ): List[MuunKuinSäännellynKoulutuksenOsasuoritus] = tila match {
    case Some("keskeytynyt") => suoritus.osasuoritusLista.flatMap(osasuoritus =>
      if (osasuoritus.osasuoritusLista.isEmpty) List(osasuoritus) else osasuoritus.rekursiivisetOsasuoritukset
    )
    case _ => suoritus.rekursiivisetOsasuoritukset
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
