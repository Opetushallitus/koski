package fi.oph.koski.sdg

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.suoritusjako.common.{OpiskeluoikeusFacade}

import scala.util.control.NonFatal

class SdgService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[SdgOpiskeluoikeus](
    application,
    Some(SdgYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema(application.organisaatioRepository)),
    Some(SdgKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findOppijaByHetu(hetu: String, queryParams: SdgQueryParams)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SdgOppija] = {

    application.opintopolkuHenkilöFacade.findOppijaByHetu(hetu) match {
      case Some(o) =>
        for {
          oppija <- findPalautettavaOppija(o.oid, queryParams)
            .left.flatMap(status => oppijaIlmanOpiskeluoikeuksia(o.oid, queryParams, status))
          valintatiedot <- haeValintatiedot(oppija.henkilö.oid, queryParams)
        } yield oppija.copy(valintatiedot = valintatiedot)
      case None => Left(KoskiErrorCategory.notFound.oppijaaEiLöydyHetulla())
    }
  }

  private def oppijaIlmanOpiskeluoikeuksia(
    oppijaOid: String,
    queryParams: SdgQueryParams,
    status: HttpStatus
  ): Either[HttpStatus, SdgOppija] =
    if (queryParams.withValintatiedot && status.statusCode == 404) {
      application.opintopolkuHenkilöFacade.findMasterOppija(oppijaOid)
        .map(henkilö => SdgOppija(henkilö = SdgHenkilo.fromOppijaHenkilö(henkilö), opiskeluoikeudet = Nil))
        .toRight(status)
    } else {
      Left(status)
    }

  private def haeValintatiedot(oppijaOid: String, queryParams: SdgQueryParams): Either[HttpStatus, Option[SdgValintatieto]] =
    if (!queryParams.withValintatiedot) {
      Right(None)
    } else {
      try {
        application.ovaraClient.fetchOpiskelijavalintatiedot(oppijaOid)
          .map(raw => Some(SdgValintatieto.from(raw.map(application.opiskelijavalintatietoConverter.convert))))
      } catch {
        case NonFatal(e) =>
          logger.error(e)("Valintatietojen käsittelyssä tapahtui odottamaton virhe")
          Left(KoskiErrorCategory.internalError("Valintatietojen käsittelyssä tapahtui odottamaton virhe."))
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
    opiskeluoikeudet: Seq[SdgOpiskeluoikeus],
    queryParams: SdgQueryParams
  ): Seq[SdgOpiskeluoikeus] = {
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

  private def josYOTutkintoNiinVainTodistuksellaOlevatKoesuoritukset(s: SdgSuoritus): SdgSuoritus = {
    s match {
      case s: SdgYlioppilastutkinnonSuoritus =>
        val filteredOsasuoritukset = s.osasuoritukset.map(_.filter { x =>
          x.suoritusMukanaTodistuksella.forall(_ == true)
        })
        s.copy(osasuoritukset = filteredOsasuoritukset)
      case _ => s
    }
  }

  private def josYOTutkintoNiinVahvistettu(s: SdgSuoritus): Boolean = {
    s match {
      case s: SdgYlioppilastutkinnonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }
}
