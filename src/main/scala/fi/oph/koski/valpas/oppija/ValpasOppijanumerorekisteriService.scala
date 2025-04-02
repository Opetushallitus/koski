package fi.oph.koski.valpas.oppija

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.validation.MaksuttomuusValidation
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö.Oid
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOppivelvollinenOppijaLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasOppijanumerorekisteriService(application: KoskiApplication) {
  private val henkilöRepository = application.henkilöRepository
  private val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  private val rajapäivätService = application.valpasRajapäivätService
  private val koodistoViitePalvelu = application.koodistoViitePalvelu
  private val accessResolver = new ValpasAccessResolver

  def getOppijaLaajatTiedotOppijanumerorekisteristä
    (oppijaOid: Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppivelvollinenOppijaLaajatTiedot] = {
    if (accessResolver.accessToAnyOrg(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS)) {
      // Maksuttomuusoikeudet ovat laajemmat ja sisältävät kuntaoikeudet, joten haetaan ensisijaisesti niillä, jos
      // käyttäjällä on molemmat oikeudet
      getMaksuttomuuskäyttäjälleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(oppijaOid)
    } else if (accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)) {
      getKuntakäyttäjälleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(oppijaOid)
    } else {
      Left(ValpasErrorCategory.internalError("Yritettiin tutkia oppijanumerorekisteriä ilman oikeuksia"))
    }
  }

  private def getMaksuttomuuskäyttäjälleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
    oppijaOid: ValpasHenkilö.Oid
  ): Either[HttpStatus, ValpasOppivelvollinenOppijaLaajatTiedot] =
    getOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
      onMaksuttomuuskäyttäjälleNäkyväVainOnrssäOlevaOppija,
      oppijaOid
    )

  private def getKuntakäyttäjälleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
    oppijaOid: ValpasHenkilö.Oid
  ): Either[HttpStatus, ValpasOppivelvollinenOppijaLaajatTiedot] =
    getOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
      onKunnalleNäkyväVainOnrssäOlevaOppija,
      oppijaOid
    )

  def getKansalaiselleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
    oppijaOid: ValpasHenkilö.Oid
  ): Either[HttpStatus, ValpasOppivelvollinenOppijaLaajatTiedot] =
    getOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
      onKansalaiselleNäkyväVainOnrssäOlevaOppija,
      oppijaOid
    )

  private def getOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
    onPalautettavaOppija: OppijaHenkilö => Boolean,
    oppijaOid: ValpasHenkilö.Oid
  ): Either[HttpStatus, ValpasOppivelvollinenOppijaLaajatTiedot] = {
    henkilöRepository.findByOid(oppijaOid, findMasterIfSlaveOid = true) match {
      case Some(henkilö) if onPalautettavaOppija(henkilö) =>
        Right(ValpasOppivelvollinenOppijaLaajatTiedot(
          henkilö,
          rajapäivätService,
          koodistoViitePalvelu,
          onTallennettuKoskeen = false
        ))
      case _ => Left(ValpasErrorCategory.forbidden.oppija())
    }
  }

  def onKunnalleNäkyväVainOnrssäOlevaOppija(henkilö: OppijaHenkilö): Boolean = {
    lazy val onAlle18VuotiasTarkastelupäivänä = rajapäivätService.onAlle18VuotiasTarkastelupäivänä(henkilö.syntymäaika)

    onKelalleNäkyväVainOnrssäOlevaOppija(henkilö) && onAlle18VuotiasTarkastelupäivänä
  }

  def onKelalleNäkyväVainOnrssäOlevaOppija(henkilö: OppijaHenkilö): Boolean =
    onMaksuttomuuskäyttäjälleNäkyväVainOnrssäOlevaOppija(henkilö)

  def onMaksuttomuuskäyttäjälleNäkyväVainOnrssäOlevaOppija(henkilö: OppijaHenkilö): Boolean = {
    lazy val näytäKotikunnanPerusteella = onKotikunnanPerusteellaLaajennetunOppivelvollisuudenPiirissä(henkilö)

    onMaksuttomuuskäyttäjänHenkilöhaussaNäkyväVainOnrssäOlevaOppija(henkilö) &&
      näytäKotikunnanPerusteella
  }

  def onMaksuttomuuskäyttäjänHenkilöhaussaNäkyväVainOnrssäOlevaOppija(henkilö: OppijaHenkilö): Boolean = {
    val onMahdollisestiLainPiirissä =
      MaksuttomuusValidation.eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
        henkilö.syntymäaika,
        opiskeluoikeusRepository.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(None, henkilö.oid),
        rajapäivätService
      ).isEmpty
    lazy val onTarpeeksiVanhaKeskeytysmerkintöjäVarten = onTarpeeksiVanhaOllakseenLainPiirissä(henkilö)
    lazy val näytäHetunOlemassaolonPerusteella = henkilö.hetu.isDefined
    lazy val maksuttomuudenPäättymispäiväTulevaisuudessa = maksuttomuudenPäättymispäiväOnTulevaisuudessa(henkilö)

    onMahdollisestiLainPiirissä &&
      onTarpeeksiVanhaKeskeytysmerkintöjäVarten &&
      näytäHetunOlemassaolonPerusteella &&
      maksuttomuudenPäättymispäiväTulevaisuudessa
  }

  private def onTarpeeksiVanhaOllakseenLainPiirissä(henkilö: OppijaHenkilö): Boolean = {
    rajapäivätService.oppijaOnTarpeeksiVanhaKeskeytysmerkintöjäVarten(henkilö.syntymäaika)
  }

  private def maksuttomuudenPäättymispäiväOnTulevaisuudessa(henkilö: OppijaHenkilö) = {
    !rajapäivätService.tarkastelupäivä.isAfter(rajapäivätService.maksuttomuusVoimassaAstiIänPerusteella(henkilö.syntymäaika.get))
  }

  private def onKansalaiselleNäkyväVainOnrssäOlevaOppija(henkilö: OppijaHenkilö): Boolean = {
    val onMahdollisestiLainPiirissä = MaksuttomuusValidation.eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
      henkilö.syntymäaika,
      opiskeluoikeusRepository.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(None, henkilö.oid),
      rajapäivätService
    ).isEmpty
    lazy val onTarpeeksiVanhaKeskeytysmerkintöjäVarten = rajapäivätService.oppijaOnTarpeeksiVanhaKeskeytysmerkintöjäVarten(henkilö.syntymäaika)

    onMahdollisestiLainPiirissä && onTarpeeksiVanhaKeskeytysmerkintöjäVarten
  }

  private def onKotikunnanPerusteellaLaajennetunOppivelvollisuudenPiirissä(henkilö: OppijaHenkilö): Boolean = {
    asLaajatOppijaHenkilöTiedot(henkilö) match {
      case Some(o) if o.turvakielto || !o.laajennetunOppivelvollisuudenUlkopuolinenKunnanPerusteella =>
        true
      case _ =>
        false
    }
  }

  def asLaajatOppijaHenkilöTiedot(henkilö: OppijaHenkilö): Option[LaajatOppijaHenkilöTiedot] = {
    henkilö match {
      case h: LaajatOppijaHenkilöTiedot => Some(h)
      case _ => henkilöRepository.findByOid(henkilö.oid, findMasterIfSlaveOid = true)
    }
  }
}
