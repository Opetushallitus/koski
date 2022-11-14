package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.documentation.VapaaSivistystyöExample.{opiskeluoikeusHyväksytystiSuoritettu, opiskeluoikeusKeskeytynyt}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat

import java.time.LocalDate

object VapaaSivistystyöValidation {
  def validateVapaanSivistystyönPäätasonSuoritus(
    suoritus: Suoritus,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    vstJotpaAikaisinSallittuAlkamispäivä: LocalDate,
  ): HttpStatus = {
    suoritus match {
      case suoritus:VapaanSivistystyönPäätasonSuoritus => {
        HttpStatus.fold(List(
          suoritus match {
            case kops: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus if suoritus.vahvistettu =>
              HttpStatus.fold(List(
                validateVapaanSivistystyönPäätasonKOPSSuorituksenLaajuus(kops),
                validateVapaanSivistystyönPäätasonKOPSSuorituksenOsaamiskokonaisuuksienLaajuudet(kops)
              ))
            case vapaa: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus if vapaatavoitteinenKoulutusTuleeValidoida(opiskeluoikeus) =>
              HttpStatus.fold(List(
                validateVapaanSivistystyönVahvistus(vapaa, opiskeluoikeus),
                validateVapaanSivistystyönVapaatavoitteisenPäätasonOsaSuoritukset(vapaa)
              ))
            case jotpa: VapaanSivistystyönJotpaKoulutuksenSuoritus =>
              HttpStatus.fold(List(
                validateVapaanSivistystyönJotpaKoulutuksenVahvistus(jotpa, opiskeluoikeus),
                validateVapaanSivistystyönJotpaKoulutuksenAlkamispäivä(opiskeluoikeus, vstJotpaAikaisinSallittuAlkamispäivä)
              ))
            case _ =>
              HttpStatus.ok
          }
        ))
      }
      case _ => HttpStatus.ok
    }
  }

  def validateVapaanSivistystyönPäätasonOpintokokonaisuus(opiskeluoikeus: Opiskeluoikeus)(implicit user: KoskiSpecificSession): HttpStatus = {
    (opiskeluoikeus.lähdejärjestelmänId, opiskeluoikeus) match {
      // Frontissa estä muokkaus, jos opintokokonaisuus puuttuu vapaatavoitteiselta koulutukselta
      case (None, oo: VapaanSivistystyönOpiskeluoikeus) => oo.suoritukset.headOption match {
        case Some(vs: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus) if vs.koulutusmoduuli.opintokokonaisuus.isEmpty => KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.puuttuvaOpintokokonaisuus()
        case Some(_) => HttpStatus.ok
      }
      // Muissa järjestelmissä sallitaan opintokokonaisuuden puuttuminen, kunhan siirtymäajan deadlinea ei ole saavutettu
      case (Some(_), oo: VapaanSivistystyönOpiskeluoikeus) => oo.suoritukset.headOption match {
        case Some(vs: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus) if vs.koulutusmoduuli.opintokokonaisuus.isEmpty =>
          // 1.9.2022 alkaen validoidaan, että VST-opiskeluoikeudelta löytyy opintokokonaisuus
          if (LocalDate.now().isAfter(LocalDate.of(2022, 9, 1))) {
            KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.puuttuvaOpintokokonaisuusDeadline()
          } else {
            HttpStatus.ok
          }
        case Some(_) => HttpStatus.ok
      }
      case _ => HttpStatus.ok
    }
  }

  def vstJotpaAikaisinSallittuAlkamispäivä(config: Config): LocalDate =
    LocalDate.parse(config.getString("validaatiot.vstJotpaAikaisinSallittuAlkamispäivä"))

  private def validateVapaanSivistystyönPäätasonKOPSSuorituksenLaajuus(suoritus: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus): HttpStatus = {
    if (suoritus.osasuoritusLista.map(_.osasuoritusLista).flatten.map(_.koulutusmoduuli.laajuusArvo(0)).sum < 53.0) {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVahvistetunPäätasonSuorituksenLaajuus("Päätason suoritus " + suorituksenTunniste(suoritus) + " on vahvistettu, mutta sillä ei ole 53 opintopisteen edestä hyväksytyksi arvioituja suorituksia")
    }
    else {
      HttpStatus.ok
    }
  }

  private def validateVapaanSivistystyönPäätasonKOPSSuorituksenOsaamiskokonaisuuksienLaajuudet(suoritus: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus): HttpStatus = {
    if (suoritus.osasuoritusLista.filter(_ match {
      case _:OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus => true
      case _ => false
    })
      .exists(s => s.osasuoritusLista.map(_.koulutusmoduuli.laajuusArvo(0)).sum < 4.0)) {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVahvistetunPäätasonSuorituksenLaajuus("Päätason suoritus " + suorituksenTunniste(suoritus) + " on vahvistettu, mutta sillä on hyväksytyksi arvioituja osaamiskokonaisuuksia, joiden laajuus on alle 4 opintopistettä")
    }
    else {
      HttpStatus.ok
    }
  }

  private def validateVapaanSivistystyönVapaatavoitteisenPäätasonOsaSuoritukset(suoritus: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus): HttpStatus = {
    if (suoritus.osasuoritusLista.exists(_.arviointi.nonEmpty)) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteisenKoulutuksenPäätasonOsasuoritukset()
    }
  }

  private def validateVapaanSivistystyönVahvistus(suoritus: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    if (suoritus.vahvistettu && opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(_.tila.koodiarvo == opiskeluoikeusKeskeytynyt.koodiarvo)) {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistetulla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Keskeytynyt'")
    } else if (!suoritus.vahvistettu && opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(_.tila.koodiarvo == opiskeluoikeusHyväksytystiSuoritettu.koodiarvo)) {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistamattomalla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Hyväksytysti suoritettu'")
    } else {
      HttpStatus.ok
    }
  }

  private def validateVapaanSivistystyönJotpaKoulutuksenVahvistus(suoritus: VapaanSivistystyönJotpaKoulutuksenSuoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    if (suoritus.vahvistettu && opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(_.tila.koodiarvo == opiskeluoikeusKeskeytynyt.koodiarvo)) {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistetulla jatkuvaan oppimiseen suunnatulla vapaan sivistystyön koulutuksella ei voi olla päättävänä tilana 'Keskeytynyt'")
    } else if (!suoritus.vahvistettu && opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(_.tila.koodiarvo == opiskeluoikeusHyväksytystiSuoritettu.koodiarvo)) {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistamattomalla jatkuvaan oppimiseen suunnatulla vapaan sivistystyön koulutuksella ei voi olla päättävänä tilana 'Hyväksytysti suoritettu'")
    } else {
      HttpStatus.ok
    }
  }

  private def validateVapaanSivistystyönJotpaKoulutuksenAlkamispäivä(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    vstJotpaAikaisinSallittuAlkamispäivä: LocalDate,
  ): HttpStatus = {
    if (opiskeluoikeus.alkamispäivä.exists(_.isBefore(vstJotpaAikaisinSallittuAlkamispäivä))) {      
      KoskiErrorCategory.badRequest.validation.date.alkamispäivä(s"Jatkuvaan oppimiseen suunnattu vapaan sivistystyön koulutuksen opiskeluoikeus ei voi alkaa ennen ${finnishDateFormat.format(vstJotpaAikaisinSallittuAlkamispäivä)}")
    } else {
      HttpStatus.ok
    }
  }

  private def vapaatavoitteinenKoulutusTuleeValidoida(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) =
    opiskeluoikeus.versionumero.getOrElse(0) > 0 || opiskeluoikeus.lähdejärjestelmänId.nonEmpty

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }
}
