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
    Some(AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema),
    Some(AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, AktiivisetJaPäättyneetOpinnotOppija] = {
    findOppijaLaajatHenkilötiedot(
      oppijaOid,
      merkitseSuoritusjakoTehdyksi = true
    ).map(oppijaLaajatTiedot => {
      AktiivisetJaPäättyneetOpinnotOppija(
        henkilö = Henkilo.fromOppijaHenkilö(oppijaLaajatTiedot.henkilö),
        opiskeluoikeudet = oppijaLaajatTiedot.opiskeluoikeudet
      )
    })
  }

  def findOppijaLaajatHenkilötiedot(oppijaOid: String, merkitseSuoritusjakoTehdyksi: Boolean = true)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, AktiivisetJaPäättyneetOpinnotOppijaLaajatHenkilötiedot] = {

    val oppija: Either[HttpStatus, RawOppija[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]] = findRawAktiivisetJaPäättyneetOpinnotOppija(oppijaOid)

    val aktiivisetJaPäättyneetOpinnotTutkinnotOppija = oppija.map(teePalautettavaAktiivisetJaPäättyneetOpinnotOppijaLaajatHenkilötiedot)

    if (merkitseSuoritusjakoTehdyksi) {
      aktiivisetJaPäättyneetOpinnotTutkinnotOppija.foreach(oppija =>
        oppija.opiskeluoikeudet.collect {
          case oo: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus => oo
        }
          .foreach(_.oid.foreach(application.oppijaFacade.merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta))
      )
    }

    aktiivisetJaPäättyneetOpinnotTutkinnotOppija
  }

  private def findRawAktiivisetJaPäättyneetOpinnotOppija(oppijaOid: String)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, RawOppija[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]] = {
    val oppija: Either[HttpStatus, RawOppija[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]] =
      opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, AktiivisetJaPäättyneetOpinnotSchema.schemassaTuetutOpiskeluoikeustyypit)
    oppija
  }

  private def teePalautettavaAktiivisetJaPäättyneetOpinnotOppijaLaajatHenkilötiedot(
    rawOppija: RawOppija[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]
  ): AktiivisetJaPäättyneetOpinnotOppijaLaajatHenkilötiedot = {
    AktiivisetJaPäättyneetOpinnotOppijaLaajatHenkilötiedot(
      henkilö = rawOppija.henkilö,
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
            .filter(josYOTutkintoNiinVahvistettu)
            .filter(josEBTutkintoNiinVahvistettu)
            .filter(vähintäänS5josESHSecondaryLower)
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

  private def josYOTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus
        => s.vahvistus.isDefined
      case _
        => true
    }
  }

  private def josEBTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
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

  private def vähintäänS5josESHSecondaryLower(s: Suoritus): Boolean = {
    s match {
      case s: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus
        if s.tyyppi.koodiarvo == "europeanschoolofhelsinkivuosiluokkasecondarylower" => s.koulutusmoduuli.tunniste.koodiarvo == "S5"
      case _ => true
    }
  }
}
