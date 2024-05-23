package fi.oph.koski.vkt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.suoritusjako.common.{OpiskeluoikeusFacade, RawOppija}

class VKTService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[VKTOpiskeluoikeus](
    application,
    Some(VKTYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema),
    Some(VKTKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, VKTOppija] = {

    val vktOppija = opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, VKTSchema.schemassaTuetutOpiskeluoikeustyypit)
      .map(teePalautettavaVKTOppija)

    vktOppija
  }

  def findOppijaByHetu(hetu: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, VKTOppija] = {

    val oppijaOid = application.opintopolkuHenkilöFacade.findOppijaByHetu(hetu).map(_.oid).get // TODO: virheenkäsittely

    val vktOppija = opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, VKTSchema.schemassaTuetutOpiskeluoikeustyypit)
      .map(teePalautettavaVKTOppija)

    vktOppija
  }

  private def teePalautettavaVKTOppija(
    rawOppija: RawOppija[VKTOpiskeluoikeus]
  ): VKTOppija = {
    VKTOppija(
      henkilö = Henkilo.fromOppijaHenkilö(rawOppija.henkilö),
      opiskeluoikeudet = suodataPalautettavat(rawOppija.opiskeluoikeudet).toList
    )
  }

  private def suodataPalautettavat(opiskeluoikeudet: Seq[VKTOpiskeluoikeus]): Seq[VKTOpiskeluoikeus] = {
    val kuoriOpiskeluoikeusOidit = opiskeluoikeudet.flatMap {
      case oo: VKTKoskeenTallennettavaOpiskeluoikeus => oo.sisältyyOpiskeluoikeuteen.map(_.oid)
      case _ => None
    }.toSet

    opiskeluoikeudet
      .filterNot(onKuoriOpiskeluoikeus(kuoriOpiskeluoikeusOidit))
      .map(_.withoutSisältyyOpiskeluoikeuteen)
      .map { opiskeluoikeus =>
        opiskeluoikeus.withSuoritukset(
          opiskeluoikeus.suoritukset
            .filter(josYOTutkintoNiinVahvistettu)
            .filter(josEBTutkintoNiinVahvistettu)
            .filter(josDIATutkintoNiinVahvistettu)
        )
      }.filter(_.suoritukset.nonEmpty)
  }

  private def josYOTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: VKTYlioppilastutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josEBTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: VKTEBTutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josDIATutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: VKTDIATutkinnonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def onKuoriOpiskeluoikeus(kuoriOpiskeluoikeusOidit: Set[String])(o: VKTOpiskeluoikeus): Boolean = {
    o match {
      case ko: VKTKoskeenTallennettavaOpiskeluoikeus => ko.oid.map(kuoriOpiskeluoikeusOidit.contains).getOrElse(false)
      case _ => false
    }
  }
}
