package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.validation.MaksuttomuusValidation
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö.Oid
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOppijaLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasOppijanumerorekisteriService(application: KoskiApplication) {
  private val henkilöRepository = application.henkilöRepository
  private val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  private val rajapäivätService = application.valpasRajapäivätService
  private val accessResolver = new ValpasAccessResolver

  def getOppijaLaajatTiedotOppijanumerorekisteristä
    (oppijaOid: Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
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
  ): Either[HttpStatus, ValpasOppijaLaajatTiedot] =
    getOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
      onMaksuttomuuskäyttäjälleNäkyväVainOnrssäOlevaOppija,
      oppijaOid
    )

  private def getKuntakäyttäjälleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
    oppijaOid: ValpasHenkilö.Oid
  ): Either[HttpStatus, ValpasOppijaLaajatTiedot] =
    getOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
      onKunnalleNäkyväVainOnrssäOlevaOppija,
      oppijaOid
    )

  def getKansalaiselleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
    oppijaOid: ValpasHenkilö.Oid
  ): Either[HttpStatus, ValpasOppijaLaajatTiedot] =
    getOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
      onKansalaiselleNäkyväVainOnrssäOlevaOppija,
      oppijaOid
    )

  private def getOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
    onPalautettavaOppija: OppijaHenkilö => Boolean,
    oppijaOid: ValpasHenkilö.Oid
  ): Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    henkilöRepository.findByOid(oppijaOid, findMasterIfSlaveOid = true) match {
      case Some(henkilö) if onPalautettavaOppija(henkilö) =>
        Right(ValpasOppijaLaajatTiedot(henkilö, rajapäivätService, onTallennettuKoskeen = false))
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
    val onMahdollisestiLainPiirissä =
      MaksuttomuusValidation.eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
        henkilö.syntymäaika,
        opiskeluoikeusRepository.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(henkilö.oid),
        rajapäivätService
      ).isEmpty
    lazy val näytäKotikunnanPerusteella = onKotikunnanPerusteellaLaajennetunOppivelvollisuudenPiirissä(henkilö)
    // Toistaiseksi vain hetullisilla voi olla kotikunta, mutta tämä saattaa tulevaisuudessa muuttua, joten varmistetaan,
    // että henkilöllä on myös hetu
    lazy val näytäHetunOlemassaolonPerusteella = henkilö.hetu.isDefined
    lazy val maksuttomuudenPäättymispäiväTulevaisuudessa = !rajapäivätService.tarkastelupäivä.isAfter(rajapäivätService.maksuttomuusVoimassaAstiIänPerusteella(henkilö.syntymäaika.get))

    onMahdollisestiLainPiirissä && näytäKotikunnanPerusteella && näytäHetunOlemassaolonPerusteella && maksuttomuudenPäättymispäiväTulevaisuudessa
  }

  private def onKansalaiselleNäkyväVainOnrssäOlevaOppija(henkilö: OppijaHenkilö): Boolean = {
    MaksuttomuusValidation.eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
      henkilö.syntymäaika,
      opiskeluoikeusRepository.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(henkilö.oid),
      rajapäivätService
    ).isEmpty
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
