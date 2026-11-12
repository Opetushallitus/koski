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
    case oo: MuunKuinSäännellynKoulutuksenOpiskeluoikeus =>
      HttpStatus.fold(
        validatePäättävänTilanVahvistus(oo),
        validateArvioinnit(oo),
      )
    case _ => HttpStatus.ok
  }

  private def validatePäättävänTilanVahvistus(oo: MuunKuinSäännellynKoulutuksenOpiskeluoikeus): HttpStatus =
    viimeisinTila(oo) match {
      case Some("hyvaksytystisuoritettu") if !oo.suoritukset.forall(_.vahvistettu) =>
        KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu(
          "Suoritukselta puuttuu vahvistus, vaikka opiskeluoikeus on tilassa hyväksytysti suoritettu"
        )
      case Some("keskeytynyt") if oo.suoritukset.exists(_.vahvistettu) =>
        KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus(
          "Suorituksella on vahvistus, vaikka opiskeluoikeus on tilassa keskeytynyt"
        )
      case _ => HttpStatus.ok
    }

  private def validateArvioinnit(oo: MuunKuinSäännellynKoulutuksenOpiskeluoikeus): HttpStatus =
    if (oo.alkamispäivä.exists(alkamispäivä => !alkamispäivä.isBefore(arviointiVaaditaanAlkaen))) {
      HttpStatus.fold(
        validateOsasuoritustenArvioinnit(oo),
        validateOsasuoritustenArviointipäivät(oo),
      )
    } else {
      HttpStatus.ok
    }

  private def validateOsasuoritustenArvioinnit(oo: MuunKuinSäännellynKoulutuksenOpiskeluoikeus): HttpStatus = {
    val arvioimattomienOsasuoritustenTunnisteet = oo.suoritukset
      .flatMap(arviointiaVaativatOsasuoritukset(_, viimeisinTila(oo)))
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
    case _ if suoritus.vahvistettu => suoritus.rekursiivisetOsasuoritukset
    case _ => Nil
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

  private def viimeisinTila(oo: MuunKuinSäännellynKoulutuksenOpiskeluoikeus): Option[String] =
    oo.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo)

  private def tunnisteTekstinä(tunniste: KoodiViite, kieli: String): String = {
    tunniste.getNimi.flatMap(_.getOptional(kieli)) match {
      case Some(nimi) if nimi != tunniste.koodiarvo => s"$nimi (${tunniste.koodiarvo})"
      case _ => tunniste.koodiarvo
    }
  }
}
