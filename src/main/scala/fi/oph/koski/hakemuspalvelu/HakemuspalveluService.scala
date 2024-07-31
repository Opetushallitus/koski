package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.suoritusjako.common.{OpiskeluoikeusFacade, RawOppija}

class HakemuspalveluService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[HakemuspalveluOpiskeluoikeus](
    application,
    Some(HakemuspalveluYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema),
    Some(HakemuspalveluKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, HakemuspalveluOppija] = {
    opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, HakemuspalveluSchema.schemassaTuetutOpiskeluoikeustyypit, useDownloadedYtr = true)
      .map(teePalautettavaHakemuspalveluOppija)
  }

  private def teePalautettavaHakemuspalveluOppija(
    rawOppija: RawOppija[HakemuspalveluOpiskeluoikeus]
  ): HakemuspalveluOppija = {
    HakemuspalveluOppija(
      henkilö = HakemuspalveluHenkilo.fromOppijaHenkilö(rawOppija.henkilö),
      opiskeluoikeudet = suodataPalautettavat(rawOppija.opiskeluoikeudet).toList
    )
  }

  private def suodataPalautettavat(opiskeluoikeudet: Seq[HakemuspalveluOpiskeluoikeus]): Seq[HakemuspalveluOpiskeluoikeus] = {
    opiskeluoikeudet
      .map { opiskeluoikeus =>
        opiskeluoikeus.withSuoritukset(
          opiskeluoikeus.suoritukset
            .filter(josKKTutkintoNiinVahvistettu)
            .filter(josYOTutkintoNiinVahvistettu)
            .filter(josEBTutkintoNiinVahvistettu)
            .filter(josDIATutkintoNiinVahvistettu)
        )
      }.filter(_.suoritukset.nonEmpty)
  }

  private def josKKTutkintoNiinVahvistettu(s: HakemuspalveluSuoritus): Boolean = {
    s match {
      case s: HakemuspalveluKorkeakoulututkinnonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josYOTutkintoNiinVahvistettu(s: HakemuspalveluSuoritus): Boolean = {
    s match {
      case s: HakemuspalveluYlioppilastutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josEBTutkintoNiinVahvistettu(s: HakemuspalveluSuoritus): Boolean = {
    s match {
      case s: HakemuspalveluEBTutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josDIATutkintoNiinVahvistettu(s: HakemuspalveluSuoritus): Boolean = {
    s match {
      case s: HakemuspalveluDIATutkinnonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }
}
