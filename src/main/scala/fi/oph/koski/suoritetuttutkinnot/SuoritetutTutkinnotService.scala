package fi.oph.koski.suoritetuttutkinnot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.suoritusjako.common.{OpiskeluoikeusFacade, RawOppija}

import java.time.LocalDate

class SuoritetutTutkinnotService(application: KoskiApplication) extends GlobalExecutionContext with Logging {

  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[SuoritetutTutkinnotOpiskeluoikeus](
    application,
    Some(SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema),
    Some(SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findSuoritetutTutkinnotOppija(oppijaOid: String, merkitseSuoritusjakoTehdyksi: Boolean = true)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SuoritetutTutkinnotOppija] = {

    val oppija: Either[HttpStatus, RawOppija[SuoritetutTutkinnotOpiskeluoikeus]] = findRawSuoritetutTutkinnotOppija(oppijaOid)

    val suoritetutTutkinnotOppija = oppija.map(teePalautettavaSuoritetutTutkinnotOppijaLaajatHenkilötiedot)

    if (merkitseSuoritusjakoTehdyksi) {
      suoritetutTutkinnotOppija.foreach(oppija =>
        oppija.opiskeluoikeudet
          .collect({ case oo: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus => oo })
          .foreach(
            _.oid.map(application.oppijaFacade.merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta)
          )
      )
    }

    suoritetutTutkinnotOppija
  }

  private def findRawSuoritetutTutkinnotOppija(oppijaOid: String)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, RawOppija[SuoritetutTutkinnotOpiskeluoikeus]] = {
    val oppija: Either[HttpStatus, RawOppija[SuoritetutTutkinnotOpiskeluoikeus]] =
      opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, SuoritetutTutkinnotSchema.schemassaTuetutOpiskeluoikeustyypit)
    oppija
  }

  private def teePalautettavaSuoritetutTutkinnotOppijaLaajatHenkilötiedot(
    rawOppija: RawOppija[SuoritetutTutkinnotOpiskeluoikeus]
  ): SuoritetutTutkinnotOppija = {
    SuoritetutTutkinnotOppija(
      henkilö = rawOppija.henkilö,
      opiskeluoikeudet = suodataPalautettavat(rawOppija.opiskeluoikeudet).toList
    )
  }

  private def suodataPalautettavat(opiskeluoikeudet: Seq[SuoritetutTutkinnotOpiskeluoikeus]): Seq[SuoritetutTutkinnotOpiskeluoikeus] = {
    val kuoriOpiskeluoikeusOidit = opiskeluoikeudet.collect({ case koo: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus => koo }).map(_.sisältyyOpiskeluoikeuteen).map(_.map(_.oid)).flatten.toSet

    opiskeluoikeudet
      .filterNot(onKuoriOpiskeluoikeus(kuoriOpiskeluoikeusOidit))
      .map(_.withoutSisältyyOpiskeluoikeuteen)
      .map { opiskeluoikeus =>
        opiskeluoikeus.withSuoritukset(
          opiskeluoikeus.suoritukset
            .map(poistaTutkintonimikeJaOsaamisalaTarvittaessa)
            .map(poistaAikaisemmatOsaamisalat)
            .filter(vahvistettuNykyhetkeenMennessä)
            .filter(josMuuAmmatillinenNiinTehtäväänValmistava)
            .filterNot(onKorotusSuoritus)
            .map(lisääViitekehystiedot)
        )
      }.filter(_.suoritukset.nonEmpty)
  }

  private def onKuoriOpiskeluoikeus(kuoriOpiskeluoikeusOidit: Set[String])(o: SuoritetutTutkinnotOpiskeluoikeus): Boolean = {
    o match {
      case ko: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus => ko.oid.map(kuoriOpiskeluoikeusOidit.contains).getOrElse(false)
      case _ => false
    }
  }

  private def poistaTutkintonimikeJaOsaamisalaTarvittaessa(s: Suoritus): Suoritus = {
    s match {
      case s: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus =>
        val tutkintonimike = if (s.toinenTutkintonimike.getOrElse(false)) {
          s.tutkintonimike
        } else {
          None
        }
        val osaamisala = if (s.toinenOsaamisala.getOrElse(false)) {
          s.osaamisala
        } else {
          None
        }
        s.copy(tutkintonimike = tutkintonimike, osaamisala = osaamisala)
      case _ => s
    }
  }

  def poistaAikaisemmatOsaamisalat(s: Suoritus): Suoritus = {
    s match {
      case s: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus =>
        s.withVainUusinOsaamisala
      case _ => s
    }
  }

  private def vahvistettuNykyhetkeenMennessä(s: Suoritus): Boolean = {
    s.vahvistus.exists(!_.päivä.isAfter(LocalDate.now))
  }

  private def josMuuAmmatillinenNiinTehtäväänValmistava(s: Suoritus): Boolean = {
    s match {
      case ms: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus
      => ms.koulutusmoduuli.tunniste.koodistoUri.contains("ammatilliseentehtavaanvalmistavakoulutus")
      case _ => true
    }
  }

  private def onKorotusSuoritus(s: Suoritus): Boolean = {
    s match {
      case ms: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus
        if ms.korotettuOpiskeluoikeusOid.isDefined => true
      case _ => false
    }
  }

  private def lisääViitekehystiedot(s: Suoritus): Suoritus = {
    s.withKoulutusmoduuli(s.koulutusmoduuli.withViitekehykset(application.koodistoViitePalvelu))
  }
}
