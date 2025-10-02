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

  def findOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SdgOppija] = {

    val sdgOppija = opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, SdgSchema.schemassaTuetutOpiskeluoikeustyypit, useDownloadedYtr = true)
      .map(teePalautettavaSdgOppija)

    sdgOppija
  }

  def findOppijaByHetu(hetu: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SdgOppija] = {

    val oppijaResult = application.opintopolkuHenkilöFacade.findOppijaByHetu(hetu)

    oppijaResult match {
      case Some(o) => findOppija(o.oid)
      case None => Left(KoskiErrorCategory.notFound.oppijaaEiLöydyHetulla())
    }
  }

  private def teePalautettavaSdgOppija(
    rawOppija: RawOppija[SdgOpiskeluoikeus]
  ): SdgOppija = {
    SdgOppija(
      henkilö = Henkilo.fromOppijaHenkilö(rawOppija.henkilö),
      opiskeluoikeudet = suodataPalautettavat(rawOppija.opiskeluoikeudet).toList
    )
  }

  private def suodataPalautettavat(opiskeluoikeudet: Seq[SdgOpiskeluoikeus]): Seq[SdgOpiskeluoikeus] = {
    opiskeluoikeudet
      .map { opiskeluoikeus =>
        opiskeluoikeus.withSuoritukset(
          opiskeluoikeus.suoritukset
            .filter(josYOTutkintoNiinVahvistettu)
        )
      }.filter(_.suoritukset.nonEmpty)
  }

  private def josYOTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: SdgYlioppilastutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }
}
