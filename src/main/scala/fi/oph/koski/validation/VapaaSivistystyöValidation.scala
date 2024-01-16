package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.documentation.VapaaSivistystyöExample.{opiskeluoikeusHyväksytystiSuoritettu, opiskeluoikeusKeskeytynyt}
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.HttpStatus.validate
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat

import java.time.LocalDate

object VapaaSivistystyöValidation {
  def validateVapaanSivistystyönPäätasonSuoritus(
    config: Config,
    henkilö: Option[Henkilö],
    suoritus: Suoritus,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    vstJotpaAikaisinSallittuAlkamispäivä: LocalDate,
    henkilöRepository: HenkilöRepository,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository
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
            case osaamismerkki: VapaanSivistystyönOsaamismerkinSuoritus =>
              HttpStatus.fold(
                validateTallennuspäivä(
                  LocalDate.parse(config.getString("validaatiot.vstOsaamismerkkiAikaisinSallittuTallennuspaiva"))
                ),
                validateEnsitallennusKäyttöliittymästäTaiKaikkiPäivätMääritelty(opiskeluoikeus, osaamismerkki),
                validateSamaArviointiVahvistusJaPäättymispäivä(
                  opiskeluoikeus,
                  osaamismerkki
                ),
                validatePäättymispäivä(
                  opiskeluoikeus,
                  LocalDate.parse(config.getString("validaatiot.vstOsaamismerkkiAikaisinSallittuPaattymispaiva")),
                ),
                validateEiPäällekkäinen(henkilö, opiskeluoikeus, henkilöRepository, koskiOpiskeluoikeudet)
              )
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

  private def validateTallennuspäivä(aikaisinTallennuspäivä: LocalDate): HttpStatus = {
    if (LocalDate.now.isBefore(aikaisinTallennuspäivä)) {
      KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.tallennuspäivä(s"Osaamismerkkejä voi alkaa tallentaa vasta ${finnishDateFormat.format(aikaisinTallennuspäivä)} alkaen")
    } else {
      HttpStatus.ok
    }
  }

  private def validateEnsitallennusKäyttöliittymästäTaiKaikkiPäivätMääritelty(oo: Opiskeluoikeus, osaamismerkki: VapaanSivistystyönOsaamismerkinSuoritus): HttpStatus = {
    validate(
      (oo.oid.isEmpty && oo.lähdejärjestelmänId.isEmpty) ||
      osaamismerkki.arviointi.exists(_.length > 0) &&
        osaamismerkki.vahvistus.isDefined &&
        oo.päättymispäivä.isDefined
    ) {
      KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.osaamismerkkiPäivät(s"Osaamismerkin pitää olla arvioitu, vahvistettu ja päättynyt")
    }
  }

  private def validateSamaArviointiVahvistusJaPäättymispäivä(oo: Opiskeluoikeus, osaamismerkki: VapaanSivistystyönOsaamismerkinSuoritus): HttpStatus = {
    val päivät = osaamismerkki.arviointi.toList.flatMap(_.map(_.päivä)) ++
      osaamismerkki.vahvistus.map(_.päivä).toList ++
      oo.päättymispäivä.toList

    if (!päivät.forall(_ == päivät.head)) {
      KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.osaamismerkkiPäivät(s"Osaamismerkin pitää olla arvioitu, vahvistettu ja päättynyt samana päivänä")
    } else {
      HttpStatus.ok
    }
  }

  private def validatePäättymispäivä(oo: Opiskeluoikeus, aikaisinPäättymispäivä: LocalDate): HttpStatus = {
    val opiskeluoikeusPäättynytLiianAikaisin = oo.päättymispäivä.exists(_.isBefore(aikaisinPäättymispäivä))

    if (opiskeluoikeusPäättynytLiianAikaisin) {
      KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.päättymispäivä(s"Osaamismerkin sisältävä opiskeluoikeus ei voi olla päättynyt ennen lain voimaantuloa ${finnishDateFormat.format(aikaisinPäättymispäivä)}")
    } else {
      HttpStatus.ok
    }
  }

  private def validateEiPäällekkäinen(
    henkilö: Option[Henkilö],
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    henkilöRepository: HenkilöRepository,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository
  ): HttpStatus = {
    def samaOo(vanhaOsaamismerkkiOo: VapaanSivistystyönOpiskeluoikeus) = {
      val samaOid = vanhaOsaamismerkkiOo.oid.isDefined && vanhaOsaamismerkkiOo.oid == opiskeluoikeus.oid
      val samaLähdejärjestelmänId = vanhaOsaamismerkkiOo.lähdejärjestelmänId.isDefined && vanhaOsaamismerkkiOo.lähdejärjestelmänId == opiskeluoikeus.lähdejärjestelmänId

      samaOid || samaLähdejärjestelmänId
    }

    def oppijallaOnPäällekkäinenOsaamismerkki(oppijaOidit: List[Henkilö.Oid]) = {
      val oot = koskiOpiskeluoikeudet.findByOppijaOids(oppijaOidit)(KoskiSpecificSession.systemUser)

      oot.exists {
        case vanhaOsaamismerkkiOo: VapaanSivistystyönOpiskeluoikeus if
          !samaOo(vanhaOsaamismerkkiOo) &&
          vanhaOsaamismerkkiOo.oppilaitos.map(_.oid) == opiskeluoikeus.oppilaitos.map(_.oid) &&
          vanhaOsaamismerkkiOo.suoritukset.head.koulutusmoduuli.tunniste == opiskeluoikeus.suoritukset.head.koulutusmoduuli.tunniste &&
          vanhaOsaamismerkkiOo.päättymispäivä == opiskeluoikeus.päättymispäivä
          => true
        case _ => false
      }
    }

    val henkilöOid = henkilö match {
      case Some(h: HenkilöWithOid) => Some(h.oid)
      case Some(h: UusiHenkilö) => henkilöRepository.opintopolku.findByHetu(h.hetu) match {
        case Some(henkilö) => Some(henkilö.oid)
        case _ => None
      }
      case _ => None
    }

    henkilöOid
      .flatMap(henkilöOid => henkilöRepository.findByOid(henkilöOid, findMasterIfSlaveOid = true))
      .map(hlö => oppijallaOnPäällekkäinenOsaamismerkki(hlö.kaikkiOidit))
    match {
      case Some(true) => KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.duplikaattiOsaamismerkki()
      case _ => HttpStatus.ok
    }
  }
}
