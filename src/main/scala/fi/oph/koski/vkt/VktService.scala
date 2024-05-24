package fi.oph.koski.vkt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.suoritusjako.common.{OpiskeluoikeusFacade, RawOppija}

class VktService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[VktOpiskeluoikeus](
    application,
    Some(VktYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema),
    Some(VktKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, VktOppija] = {

    val vktOppija = opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, VktSchema.schemassaTuetutOpiskeluoikeustyypit)
      .map(teePalautettavaVktOppija)

    vktOppija
  }

  def findOppijaByHetu(hetu: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, VktOppija] = {

    val oppijaOid = application.opintopolkuHenkilöFacade.findOppijaByHetu(hetu).map(_.oid).get // TODO: virheenkäsittely

    val vktOppija = opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, VktSchema.schemassaTuetutOpiskeluoikeustyypit)
      .map(teePalautettavaVktOppija)

    vktOppija
  }

  private def teePalautettavaVktOppija(
    rawOppija: RawOppija[VktOpiskeluoikeus]
  ): VktOppija = {
    VktOppija(
      henkilö = Henkilo.fromOppijaHenkilö(rawOppija.henkilö),
      opiskeluoikeudet = suodataPalautettavat(rawOppija.opiskeluoikeudet).toList
    )
  }

  private def suodataPalautettavat(opiskeluoikeudet: Seq[VktOpiskeluoikeus]): Seq[VktOpiskeluoikeus] = {
    val kuoriOpiskeluoikeusOidit = opiskeluoikeudet.flatMap {
      case oo: VktKoskeenTallennettavaOpiskeluoikeus => oo.sisältyyOpiskeluoikeuteen.map(_.oid)
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
      case s: VktYlioppilastutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josEBTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: VktEBTutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josDIATutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: VktDIATutkinnonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def onKuoriOpiskeluoikeus(kuoriOpiskeluoikeusOidit: Set[String])(o: VktOpiskeluoikeus): Boolean = {
    o match {
      case ko: VktKoskeenTallennettavaOpiskeluoikeus => ko.oid.map(kuoriOpiskeluoikeusOidit.contains).getOrElse(false)
      case _ => false
    }
  }
}
