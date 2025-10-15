package fi.oph.koski.sdg

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.suoritusjako.common.{OpiskeluoikeusFacade, RawOppija}

class SdgService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[SdgOpiskeluoikeus](
    application,
    Some(SdgYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema),
    Some(SdgKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findOppijaByHetu(hetu: String, includeOsasuoritukset: Boolean)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SdgOppija] = {

    val oppijaResult = application.opintopolkuHenkilöFacade.findOppijaByHetu(hetu)

    oppijaResult match {
      case Some(o) => findPalautettavaOppija(o.oid, includeOsasuoritukset)
      case None => Left(KoskiErrorCategory.notFound.oppijaaEiLöydyHetulla())
    }
  }

  def findPalautettavaOppija(
    oppijaOid: String,
    includeOsasuoritukset: Boolean
  )
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SdgOppija] = {

    val sdgOppija = opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, SdgSchema.schemassaTuetutOpiskeluoikeustyypit, useDownloadedYtr = true)
      .map(rawOppija => SdgOppija(
        henkilö = Henkilo.fromOppijaHenkilö(rawOppija.henkilö),
        opiskeluoikeudet = suodataPalautettavatSuoritukset(rawOppija.opiskeluoikeudet, includeOsasuoritukset)
          .toList
      ))

    sdgOppija
  }


  private def suodataPalautettavatSuoritukset(
    opiskeluoikeudet: Seq[SdgOpiskeluoikeus],
    includeOsasuoritukset: Boolean
  ): Seq[SdgOpiskeluoikeus] = {
    opiskeluoikeudet
      .map { opiskeluoikeus =>
        opiskeluoikeus.withSuoritukset(
          opiskeluoikeus.suoritukset
            .filter(josYOTutkintoNiinVahvistettu)
            .filter(josEBTutkintoNiinVahvistettu)
            .filter(josDIATutkintoNiinVahvistettu)
            .map { suoritus =>
              if (includeOsasuoritukset) suoritus
              else suoritus.withOsasuoritukset(None)
            }

        )
      }.filter(_.suoritukset.nonEmpty)
  }

  // SÄILYTETÄÄN: vain vahvistetut YO:t mukaan
  private def josYOTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: SdgYlioppilastutkinnonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josEBTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: SdgEBTutkinnonOpiskeluoikeus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josDIATutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: SdgDIAOpiskeluoikeus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }
}
