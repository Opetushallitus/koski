package fi.oph.koski.sdg

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.suoritusjako.common.{OpiskeluoikeusFacade}

class SdgService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[Opiskeluoikeus](
    application,
    Some(SdgYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema),
    Some(SdgKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findOppijaByHetu(hetu: String, queryParams: SdgQueryParams)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SdgOppija] = {

    val oppijaResult = application.opintopolkuHenkilöFacade.findOppijaByHetu(hetu)

    oppijaResult match {
      case Some(o) => findPalautettavaOppija(o.oid, queryParams)
      case None => Left(KoskiErrorCategory.notFound.oppijaaEiLöydyHetulla())
    }
  }

  private def findPalautettavaOppija(
    oppijaOid: String,
    queryParams: SdgQueryParams
  )
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SdgOppija] = {

    val sdgOppija = opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, SdgSchema.schemassaTuetutOpiskeluoikeustyypit, useDownloadedYtr = false)
      .map(rawOppija => SdgOppija(
        henkilö = SdgHenkilo.fromOppijaHenkilö(rawOppija.henkilö),
        opiskeluoikeudet = suodataPalautettavatSuoritukset(rawOppija.opiskeluoikeudet, queryParams)
          .toList
      ))

    sdgOppija
  }

  private def suodataPalautettavatSuoritukset(
    opiskeluoikeudet: Seq[Opiskeluoikeus],
    queryParams: SdgQueryParams
  ): Seq[Opiskeluoikeus] = {
    opiskeluoikeudet
      .map { opiskeluoikeus =>
        val suoritukset = opiskeluoikeus.suoritukset
          .filter(josYOTutkintoNiinVahvistettu)
          .filter(suoritus => !queryParams.onlyVahvistetut || suoritus.vahvistus.isDefined)
          .map(josYOTutkintoNiinVainTodistuksellaOlevatKoesuoritukset)
          .map { suoritus =>
            if (queryParams.withOsasuoritukset) {
              suoritus
            }
            else {
              suoritus.withOsasuoritukset(None)
            }
          }

        opiskeluoikeus.withSuoritukset(suoritukset)
      }
      .filter(_.suoritukset.nonEmpty)
  }

  private def josYOTutkintoNiinVainTodistuksellaOlevatKoesuoritukset(s: Suoritus): Suoritus = {
    s match {
      case s: SdgYlioppilastutkinnonSuoritus =>
        val filteredOsasuoritukset = s.osasuoritukset.map(_.filter { x =>
          x.suoritusMukanaTodistuksella.forall(_ == true)
        })
        s.copy(osasuoritukset = filteredOsasuoritukset)
      case _ => s
    }
  }

  private def josYOTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: SdgYlioppilastutkinnonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }
}
