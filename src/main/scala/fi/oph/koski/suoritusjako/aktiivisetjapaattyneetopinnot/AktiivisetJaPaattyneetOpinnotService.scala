package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.config.{KoskiApplication}
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.http.{HttpStatus}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.schema
import fi.oph.koski.suoritusjako.common.{OpiskeluoikeusFacade, RawOppija}

class AktiivisetJaPäättyneetOpinnotService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus](
    application,
    None,
    Some(AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, AktiivisetJaPäättyneetOpinnotOppija] = {

    val aktiivisetOpinnotOppija = opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, AktiivisetJaPäättyneetOpinnotSchema.schemassaTuetutOpiskeluoikeustyypit)
      .map(teePalautettavaAktiivisetJaPäättyneetOpinnotOppija)

    aktiivisetOpinnotOppija.foreach(
      _.opiskeluoikeudet.collect {
        case oo: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus => oo
      }
      .foreach(_.oid.foreach(application.oppijaFacade.merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta))
    )

    aktiivisetOpinnotOppija
  }

  private def teePalautettavaAktiivisetJaPäättyneetOpinnotOppija(
    rawOppija: RawOppija[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]
  ): AktiivisetJaPäättyneetOpinnotOppija = {
    AktiivisetJaPäättyneetOpinnotOppija(
      henkilö = Henkilo.fromOppijaHenkilö(rawOppija.henkilö),
      opiskeluoikeudet = suodataPalautettavat(rawOppija.opiskeluoikeudet).toList
    )
  }

  private def suodataPalautettavat(opiskeluoikeudet: Seq[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]): Seq[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus] = {

    val kuoriOpiskeluoikeusOidit = opiskeluoikeudet.map {
      case oo: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus => oo.sisältyyOpiskeluoikeuteen.map(_.oid)
      case _ => None
    }.flatten.toSet

    opiskeluoikeudet
      .filterNot(onKuoriOpiskeluoikeus(kuoriOpiskeluoikeusOidit))
      .map(_.withoutSisältyyOpiskeluoikeuteen)
      .map { opiskeluoikeus =>
        opiskeluoikeus.withSuoritukset(
          opiskeluoikeus.suoritukset
            .filter(josInternationalSchoolNiinLukiotaVastaava)
        )
      }.filter(_.suoritukset.nonEmpty)
  }

  private def josInternationalSchoolNiinLukiotaVastaava(s: Suoritus): Boolean = {
    s match {
      case s: AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus
        if !schema.InternationalSchoolOpiskeluoikeus.onLukiotaVastaavaInternationalSchoolinSuoritus(
          s.tyyppi.koodiarvo,
          s.koulutusmoduuli.tunniste.koodiarvo
        )
        => false
      case _
        => true
    }
  }

  private def onKuoriOpiskeluoikeus(kuoriOpiskeluoikeusOidit: Set[String])(o: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus): Boolean = {
    o match {
      case ko: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus => ko.oid.map(kuoriOpiskeluoikeusOidit.contains).getOrElse(false)
      case _ => false
    }
  }
}
