package fi.oph.koski.valpas.oppija

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Yhteystiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.util.Monoids.rightSeqMonoid
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.db.ValpasSchema.OpiskeluoikeusLisätiedotKey
import fi.oph.koski.valpas.kuntailmoitus.ValpasKunnat
import fi.oph.koski.valpas.opiskeluoikeusrepository._
import fi.oph.koski.valpas.valpasrepository._
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.koski.valpas.yhteystiedot.ValpasYhteystiedot

class ValpasOppijaLaajatTiedotService(
  application: KoskiApplication
) extends Logging with Timing {
  private val hakukoosteService = application.valpasHakukoosteService
  private val opiskeluoikeusDbService = application.valpasOpiskeluoikeusDatabaseService
  private val ovKeskeytysRepositoryService = application.valpasOppivelvollisuudenKeskeytysRepositoryService
  private val oppijanumerorekisteri = application.opintopolkuHenkilöFacade
  private val localizationRepository = application.valpasLocalizationRepository
  private val koodistoviitepalvelu = application.koodistoViitePalvelu
  private val lisätiedotRepository = application.valpasOpiskeluoikeusLisätiedotRepository
  private val rajapäivätService = application.valpasRajapäivätService
  private val oppijanumerorekisteriService = application.valpasOppijanumerorekisteriService
  private val organisaatioService = application.organisaatioService

  private val accessResolver = new ValpasAccessResolver

  private val validatingAndResolvingExtractor = application.validatingAndResolvingExtractor

  private val roolitJoilleHaetaanKaikistaOVLPiirinOppijoista: Seq[ValpasRooli.Role] = Seq(
    ValpasRooli.OPPILAITOS_MAKSUTTOMUUS,
    ValpasRooli.KUNTA,
  )

  def getOppijaLaajatTiedot
    (
      rooli: ValpasRooli.Role,
      oppijaOid: ValpasHenkilö.Oid,
      haeMyösVainOppijanumerorekisterissäOleva: Boolean,
      palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja: Boolean
    )
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    val rajaaOVKelpoisiinOpiskeluoikeuksiin = !roolitJoilleHaetaanKaikistaOVLPiirinOppijoista.contains(rooli)

    opiskeluoikeusDbService.getOppija(oppijaOid, rajaaOVKelpoisiinOpiskeluoikeuksiin, haeMyösOppivelvollisuudestaVapautettu = true) match {
      case Some(oppijaRow) =>
        asValpasOppijaLaajatTiedot(palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja)(oppijaRow)
          .flatMap(accessResolver.withOppijaAccessAsRole(rooli))
          .map(withTurvakieltosiivottuVapautus)
          .map(withTurvakieltosiivottuKotikunta)
      case None if haeMyösVainOppijanumerorekisterissäOleva =>
        oppijanumerorekisteriService.getOppijaLaajatTiedotOppijanumerorekisteristä(oppijaOid)
          .map(withTurvakieltosiivottuKotikunta)
      case _ =>
        Left(ValpasErrorCategory.forbidden.oppija())
    }
  }

  def getOppijaLaajatTiedotIlmanOikeustarkastusta(oppijaOid: ValpasHenkilö.Oid) : Either[HttpStatus, Option[ValpasOppijaLaajatTiedot]] = {
    val rajaaOVKelpoisiinOpiskeluoikeuksiin = false
    opiskeluoikeusDbService.getOppija(oppijaOid, rajaaOVKelpoisiinOpiskeluoikeuksiin, haeMyösOppivelvollisuudestaVapautettu = true) match {
      case Some(dbRow) => asValpasOppijaLaajatTiedot()(dbRow).map(Some(_))
      case _ => Right(None)
    }
  }

  def getOppijaLaajatTiedot
    (roolit: Seq[ValpasRooli.Role], oppijaOid: ValpasHenkilö.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    // Käyttäjän antaessa useamman roolin yritetään hakea niistä sillä, jolla on laajimmat valtuudet nähdä oppijoita.
    // Tällä vältetään turhien tietokantakyselyiden tekeminen, sillä granulariteettitaso on nyt ValpasOpiskeluoikeusDatabaseServicen
    // rajaaOVKelposillaOppivelvollisuuksilla-lippu. Naiivimpi vaihtoehto olisi hakea kaikilla rooleilla ja tarkastaa mitkä kyselyt onnistuivat.
    // Jos granulariteettia jatkossa kasvatetaan, tämä funktio pitää toteuttaa myös eri tavalla.

    val laajimmatRoolit = roolit.intersect(roolitJoilleHaetaanKaikistaOVLPiirinOppijoista)
    if (roolit.isEmpty) {
      Left(ValpasErrorCategory.forbidden.oppija())
    } else if (laajimmatRoolit.nonEmpty) {
      getOppijaLaajatTiedot(
        laajimmatRoolit.head,
        oppijaOid,
        haeMyösVainOppijanumerorekisterissäOleva = false,
        palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja = false
      )
    } else {
      getOppijaLaajatTiedot(
        roolit.head,
        oppijaOid,
        haeMyösVainOppijanumerorekisterissäOleva = false,
        palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja = false
      )
    }
  }

  def getOppijaLaajatTiedot
    (
      oppijaOid: ValpasHenkilö.Oid,
      haeMyösVainOppijanumerorekisterissäOleva: Boolean,
      palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja: Boolean
    )
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    opiskeluoikeusDbService.getOppija(oppijaOid, rajaaOVKelpoisiinOpiskeluoikeuksiin = true, haeMyösOppivelvollisuudestaVapautettu = true) match {
      case Some(oppijaRow) =>
        asValpasOppijaLaajatTiedot(palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja)(oppijaRow)
          .flatMap(accessResolver.withOppijaAccess(_))
      case None if haeMyösVainOppijanumerorekisterissäOleva =>
        oppijanumerorekisteriService.getOppijaLaajatTiedotOppijanumerorekisteristä(oppijaOid)
      case _ =>
        Left(ValpasErrorCategory.forbidden.oppija())
    }
  }

  def getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla
    (oppijaOid: ValpasHenkilö.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] = {
    val haeMyösVainOppijanumerorekisterissäOleva =
      accessResolver.accessToAnyOrg(ValpasRooli.KUNTA) ||
      accessResolver.accessToAnyOrg(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS)
    val palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja = true

    val rooli = roolitJoilleHaetaanKaikistaOVLPiirinOppijoista.find(accessResolver.accessToAnyOrg)

    getOppijaLaajatTiedotHakuJaYhteystiedoilla(
      oppijaOid,
      rooli,
      haeMyösVainOppijanumerorekisterissäOleva,
      palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja
    )
      .flatMap(withKuntailmoitukset)
      .map(withOikeusTehdäKuntailmoitus)
      .map(withOikeusMitätöidäOppivelvollisuudenVapautus)
  }

  def getOppijaLaajatTiedotHakuJaYhteystiedoilla
    (
      oppijaOid: ValpasHenkilö.Oid,
      rooli: Option[ValpasRooli.Role] = None,
      haeMyösVainOppijanumerorekisterissäOleva: Boolean = false,
      palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja: Boolean = false
    )
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] = {
    if (rooli.isEmpty && haeMyösVainOppijanumerorekisterissäOleva) {
      throw new InternalError("Ei voi tapahtua: vain onr:ssä olevat haetaan vain kunta- ja maksuttomuuskäyttäjille, jolloin roolinkin pitää olla määritelty")
    }
    (rooli match {
      case None => getOppijaLaajatTiedot(
        oppijaOid,
        haeMyösVainOppijanumerorekisterissäOleva,
        palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja
      )
      case Some(r) => getOppijaLaajatTiedot(
        r,
        oppijaOid,
        haeMyösVainOppijanumerorekisterissäOleva,
        palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja
      )
    })
      .map(withTurvakieltosiivottuKotikunta)
      .map(fetchHakuYhteystiedoilla)
      .flatMap(withVirallisetYhteystiedot)
      .map(_.validate(koodistoviitepalvelu))
      .map(fetchOppivelvollisuudenKeskeytykset)
  }

  def asValpasOppijaLaajatTiedot(
    palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja: Boolean = false
  )(dbRow: ValpasOppijaRow): Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    validatingAndResolvingExtractor
      .extract[List[ValpasOpiskeluoikeusLaajatTiedot]](strictDeserialization)(dbRow.opiskeluoikeudet)
      .left.map(e => {
        logger.error(e.toString)
        ValpasErrorCategory.internalError("Oppijan tietojen haku epäonnistui")
      })
      .map(opiskeluoikeudet => {
        val onAmmatillisiaOpintoja = opiskeluoikeudet.exists(_.tyyppi.koodiarvo == "ammatillinenkoulutus")
        if (onAmmatillisiaOpintoja && palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja) {
          opiskeluoikeudet
        } else {
          opiskeluoikeudet.filter(o => o.päätasonSuoritukset.exists(s =>
            !Set("lukionoppiaineenoppimaara", "lukionaineopinnot").contains(s.suorituksenTyyppi.koodiarvo)
          ))
        }
      }
      )
      .map(opiskeluoikeudet =>
        ValpasOppivelvollinenOppijaLaajatTiedot(
          henkilö = ValpasHenkilöLaajatTiedot(
            oid = dbRow.oppijaOid,
            kaikkiOidit = dbRow.kaikkiOppijaOidit.toSet,
            hetu = dbRow.hetu,
            syntymäaika = dbRow.syntymäaika,
            etunimet = dbRow.etunimet,
            sukunimi = dbRow.sukunimi,
            kotikunta = dbRow.kotikunta.flatMap(k => application.koodistoViitePalvelu.validate("kunta", k)),
            turvakielto = dbRow.turvakielto,
            äidinkieli = dbRow.äidinkieli
          ),
          hakeutumisvalvovatOppilaitokset = dbRow.hakeutumisvalvovatOppilaitokset,
          suorittamisvalvovatOppilaitokset = dbRow.suorittamisvalvovatOppilaitokset,
          opiskeluoikeudet = opiskeluoikeudet.filter(_.oppivelvollisuudenSuorittamiseenKelpaava),
          oppivelvollisuusVoimassaAsti = dbRow.oppivelvollisuusVoimassaAsti,
          oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = dbRow.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti,
          onOikeusValvoaKunnalla = dbRow.onOikeusValvoaKunnalla,
          onOikeusValvoaMaksuttomuutta = dbRow.onOikeusValvoaMaksuttomuutta,
          oppivelvollisuudestaVapautus = dbRow.oppivelvollisuudestaVapautus,
        )
      )
      .map(oppija => oppija.oppivelvollisuudestaVapautus match {
        case Some(vapautus) if !vapautus.tulevaisuudessa => ValpasOppivelvollisuudestaVapautettuLaajatTiedot.apply(oppija, vapautus)
        case _ => oppija
      })
  }

  def withTurvakieltosiivottuVapautus(oppija: ValpasOppijaLaajatTiedot)(implicit session: ValpasSession): ValpasOppijaLaajatTiedot = {
    def onTurvakielto: Boolean = oppija.henkilö.turvakielto
    def käyttäjänKuntaOidit: Seq[Oid] = ValpasKunnat.getUserKunnat(organisaatioService).map(_.oid)
    def onKäyttäjänKunnanTekemäVapautus: Boolean = (
        for {
          vapautus    <- oppija.oppivelvollisuudestaVapautus
          kunta       <- vapautus.kunta
        } yield käyttäjänKuntaOidit.contains(kunta.oid)
      ).getOrElse(false)

    if (onTurvakielto && !onKäyttäjänKunnanTekemäVapautus) {
      val vapautus = oppija.oppivelvollisuudestaVapautus.map(_.poistaTurvakiellonAlaisetTiedot)
      oppija match {
        case o: ValpasOppivelvollinenOppijaLaajatTiedot => o.copy(oppivelvollisuudestaVapautus = vapautus)
        case o: ValpasOppivelvollisuudestaVapautettuLaajatTiedot => o.copy(oppivelvollisuudestaVapautus = vapautus)
      }
    } else {
      oppija
    }
  }

  def withTurvakieltosiivottuKotikunta(oppija: ValpasOppijaLaajatTiedot)(implicit session: ValpasSession): ValpasOppijaLaajatTiedot = {
    def onTurvakielto: Boolean = oppija.henkilö.turvakielto

    if(onTurvakielto){
      oppija match {
        case o: ValpasOppivelvollinenOppijaLaajatTiedot => o.copy(henkilö = o.henkilö.copy(kotikunta = None))
        case o: ValpasOppivelvollisuudestaVapautettuLaajatTiedot => o.copy(henkilö = o.henkilö.copy(kotikunta = None))
      }
    } else {
      oppija
    }
  }

  def withVirallisetYhteystiedot(
    o: OppijaHakutilanteillaLaajatTiedot
  ): Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] =
    fetchVirallisetYhteystiedot(o.oppija)
      .map(yhteystiedot => o.copy(
        yhteystiedot = o.yhteystiedot ++ yhteystiedot.map(yt => ValpasYhteystiedot.virallinenYhteystieto(yt, localizationRepository.get("oppija__viralliset_yhteystiedot")))
      ))

  def fetchHakuYhteystiedoilla(oppija: ValpasOppijaLaajatTiedot): OppijaHakutilanteillaLaajatTiedot = {
    val hakukoosteet = if (oppija.oppivelvollisuudestaVapautettu) {
      Right(Nil)
    } else {
      hakukoosteService.getYhteishakujenHakukoosteet(oppijaOids = Set(oppija.henkilö.oid), ainoastaanAktiivisetHaut = false, errorClue = s"oppija:${oppija.henkilö.oid}")
    }

    OppijaHakutilanteillaLaajatTiedot.apply(oppija = oppija, yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"), haut = hakukoosteet)
  }

  private def fetchVirallisetYhteystiedot(oppija: ValpasOppijaLaajatTiedot): Either[HttpStatus, Seq[Yhteystiedot]] = {
    if (oppija.henkilö.turvakielto || oppija.oppivelvollisuudestaVapautettu) {
      Right(Seq.empty)
    } else {
      timed("fetchVirallisetYhteystiedot", 10) {
        oppijanumerorekisteri.findOppijaJaYhteystiedotByOid(oppija.henkilö.oid)
          .toRight(ValpasErrorCategory.internalError("Virallisten yhteystietojen haku epäonnistui"))
          .map(_.yhteystiedot.flatMap(yt => {
            val alkuperä = koodistoviitepalvelu.validate(yt.alkuperä)
              .filter(_.koodiarvo == "alkupera1") // Filtteröi pois muut kuin VTJ:ltä peräisin olevat yhteystiedot
            val tyyppi = koodistoviitepalvelu.validate(yt.tyyppi)
            (alkuperä, tyyppi) match {
              case (Some(alkuperä), Some(tyyppi)) => Some(yt.copy(alkuperä = alkuperä, tyyppi = tyyppi))
              case _ => None
            }
          }))
      }
    }
  }

  private def withKuntailmoitukset(
    o: OppijaHakutilanteillaLaajatTiedot
  )(implicit session: ValpasSession): Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] =
    fetchKuntailmoitukset(o.oppija)
      .map(kuntailmoitukset => o.copy(kuntailmoitukset = kuntailmoitukset))

  private def fetchKuntailmoitukset(
    oppija: ValpasOppijaLaajatTiedot
  )(implicit session: ValpasSession): Either[HttpStatus, Seq[ValpasKuntailmoitusLaajatTiedot]] = {
    oppija.mapOppivelvollinen { o =>
      timed("fetchKuntailmoitukset", 10) {
        application.valpasKuntailmoitusService.getKuntailmoitukset(oppija)
          .map(lisääAktiivisuustiedot(o))
      }
    }(rightSeqMonoid)
  }

  def lisääAktiivisuustiedot(
    oppija: ValpasOppivelvollinenOppijaLaajatTiedot
  )(
    kuntailmoitukset: Seq[ValpasKuntailmoitusLaajatTiedot]
  ): Seq[ValpasKuntailmoitusLaajatTiedot] = {
    kuntailmoitukset.zipWithIndex.map { case (kuntailmoitus, index) =>
      val ilmoituksentekopäivä = kuntailmoitus.aikaleima.get.toLocalDate
      val aktiivinen = {
        // 1. Ei uusin kuntailmoitus oppijasta --> passiivinen
        if (index > 0) {
          false
        }
        // 2. Voimassaoleva ovl-kelpoinen opiskeluoikeus alkanut (ja mahdollisesti päättynyt) kuntailmoituksen tekemisen jälkeen --> passiivinen
        else if (oppija.opiskeluoikeudet.exists(oo =>
          (oo.isOpiskelu || oo.isKuntailmoituksenPassivoivassaTerminaalitilassa ) &&
            oo.oppivelvollisuudenSuorittamiseenKelpaava &&
            oo.alkamispäivä.exists(_.isAfter(ilmoituksentekopäivä)))) {
          false
        }
        // 3. Ilmoituksen teosta alle 2kk (vaikka olisikin voimassaoleva opiskeluoikeus) --> aktiivinen
        // Tällä säännöllä napataan kiinni tilanteet, joissa Valppaan ja Kosken tiedot opiskeluoikeuden voimassaolosta eivät ole
        // synkassa tai oppijasta on tehty ilmoitus, mutta oppijan eroamisen merkitseminen on jäänyt tekemättä
        // ja sen takia olisi vaara että kuntailmoitus hautautuisi käyttöliittymässä.
        else if (!rajapäivätService.tarkastelupäivä.isAfter(ilmoituksentekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina))) {
          true
        }
        // 4. Aktiivinen jos oppijalla ei ole oppivelvollisuuden suorittamiseen kelpaavaa opiskeluoikeutta
        else {
          oppija.opiskeluoikeudet.forall(oo => !oo.isOpiskelu)
        }
      }

      kuntailmoitus.copy(aktiivinen = Some(aktiivinen))
    }
  }

  def fetchOppivelvollisuudenKeskeytykset(
    oppija: OppijaHakutilanteillaLaajatTiedot
  ): OppijaHakutilanteillaLaajatTiedot = {
    oppija.copy(
      oppivelvollisuudenKeskeytykset = ovKeskeytysRepositoryService.getKeskeytykset(oppija.oppija.henkilö.kaikkiOidit.toSeq)
    )
  }

  private def withOikeusTehdäKuntailmoitus(
    oppija: OppijaHakutilanteillaLaajatTiedot
  )(implicit session: ValpasSession): OppijaHakutilanteillaLaajatTiedot = {
    val onOikeus = if (oppija.oppija.henkilö.onTallennettuKoskeen && !oppija.oppija.oppivelvollisuudestaVapautettu) {
      application.valpasKuntailmoitusService.withOikeusTehdäKuntailmoitusOppijalle(oppija.oppija).isRight
    } else {
      false
    }
    oppija.copy(onOikeusTehdäKuntailmoitus = Some(onOikeus))
  }

  private def withOikeusMitätöidäOppivelvollisuudenVapautus (
    oppija: OppijaHakutilanteillaLaajatTiedot
  )(implicit session: ValpasSession): OppijaHakutilanteillaLaajatTiedot =
    oppija.oppija.oppivelvollisuudestaVapautus
      .flatMap(_.kunta)
      .fold(oppija) { kunta =>
        val onOikeus = ValpasKunnat.getUserKunnat(organisaatioService).exists(_.oid == kunta.oid)
        oppija.copy(onOikeusMitätöidäOppivelvollisuudestaVapautus = Some(onOikeus))
      }

  def setMuuHaku(key: OpiskeluoikeusLisätiedotKey, value: Boolean)(implicit session: ValpasSession): HttpStatus = {
    HttpStatus.justStatus(
      accessResolver.assertAccessToOrg(ValpasRooli.OPPILAITOS_HAKEUTUMINEN, key.oppilaitosOid)
        .flatMap(_ => getOppijaLaajatTiedot(
          ValpasRooli.OPPILAITOS_HAKEUTUMINEN,
          key.oppijaOid,
          haeMyösVainOppijanumerorekisterissäOleva = false,
          palautaLukionAineopinnotJaYOTutkinnotJosMyösAmmatillisiaOpintoja = false
        ))
        .flatMap(accessResolver.withOppijaAccessAsOrganisaatio(ValpasRooli.OPPILAITOS_HAKEUTUMINEN, key.oppilaitosOid))
        .flatMap(accessResolver.withOpiskeluoikeusAccess(ValpasRooli.OPPILAITOS_HAKEUTUMINEN)(key.opiskeluoikeusOid))
        .flatMap(_ => lisätiedotRepository.setMuuHaku(key, value).toEither)
    )
  }
}
