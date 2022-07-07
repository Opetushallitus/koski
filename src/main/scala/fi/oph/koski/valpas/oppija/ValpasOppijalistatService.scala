package fi.oph.koski.valpas.oppija

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Henkilö, Organisaatio}
import fi.oph.koski.valpas.opiskeluoikeusrepository._
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaTiming
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasOppijalistatService(
  application: KoskiApplication
) extends Logging with ValpasRouhintaTiming {
  private val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  private val hakukoosteService = application.valpasHakukoosteService
  private val opiskeluoikeusDbService = application.valpasOpiskeluoikeusDatabaseService
  private val localizationRepository = application.valpasLocalizationRepository
  private val koodistoviitepalvelu = application.koodistoViitePalvelu
  private val lisätiedotRepository = application.valpasOpiskeluoikeusLisätiedotRepository

  private val accessResolver = new ValpasAccessResolver

  def getHakeutumisvalvottavatOppijatSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value, haeHakutilanteet: Seq[Henkilö.Oid] = Seq.empty)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    getHakeutumisvalvottavatOppijatLaajatTiedot(oppilaitosOid, hakeutumisvalvontaTieto, haeHakutilanteet)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))

  private def getHakeutumisvalvottavatOppijatLaajatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value, haeHakutilanteet: Seq[Henkilö.Oid])
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    (if (haeHakutilanteet.nonEmpty) {
      getHakeutumisvalvottavatOppijatLaajatTiedotHakutilanteilla(
        ValpasRooli.OPPILAITOS_HAKEUTUMINEN,
        oppilaitosOid,
        hakeutumisvalvontaTieto,
        haeHakutilanteet,
      ).map(_.filter(o => haeHakutilanteet.contains(o.oppija.henkilö.oid)))
    } else {
      getHakeutumisvalvottavatOppijatLaajatTiedotIlmanHakutilanteita(
        ValpasRooli.OPPILAITOS_HAKEUTUMINEN,
        oppilaitosOid,
        hakeutumisvalvontaTieto
      )
    })
      .map(poistaKuntailmoitetutOpiskeluoikeudet(säästäJosOpiskeluoikeusVoimassa = false))
      .map(lisätiedotRepository.readForOppijat)
      .map(_.map(oppijaLisätiedotTuple => oppijaLisätiedotTuple._1.withLisätiedot(oppijaLisätiedotTuple._2)))
  }

  private def getHakeutumisvalvottavatOppijatLaajatTiedotHakutilanteilla
    (rooli: ValpasRooli.Role, oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value, oppijatJoilleHaetaanHakutiedot: Seq[Henkilö.Oid])
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    val errorClue = oppilaitosOidErrorClue(oppilaitosOid)

    getOppijatLaajatTiedot(rooli, oppilaitosOid, hakeutumisvalvontaTieto)
      .map(fetchHautIlmanYhteystietoja(errorClue, oppijatJoilleHaetaanHakutiedot))
  }

  private def oppilaitosOidErrorClue(oppilaitosOid: ValpasOppilaitos.Oid): String =
    s"oppilaitos: ${oppilaitosOid}"

  private def getHakeutumisvalvottavatOppijatLaajatTiedotIlmanHakutilanteita
    (rooli: ValpasRooli.Role, oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    getOppijatLaajatTiedot(rooli, oppilaitosOid, hakeutumisvalvontaTieto)
      .map(oppijat => oppijat.map(OppijaHakutilanteillaLaajatTiedot.apply))
  }

  private def poistaKuntailmoitetutOpiskeluoikeudet
    (säästäJosOpiskeluoikeusVoimassa: Boolean)
      (oppijat: Seq[OppijaHakutilanteillaLaajatTiedot])
      (implicit session: ValpasSession)
  : Seq[OppijaHakutilanteillaLaajatTiedot] = {
    application.valpasKuntailmoitusService
      .addOpiskeluoikeusOnTehtyIlmoitusProperties(oppijat)
      .flatMap(oppija => {
        val opiskeluoikeudet = oppija.oppija.opiskeluoikeudet
          .filter(oo => !oo.onTehtyIlmoitus.contains(true) || (säästäJosOpiskeluoikeusVoimassa && oo.isOpiskelu))
        if (opiskeluoikeudet.nonEmpty) {
          Some(oppija.copy(oppija = oppija.oppija.copy(opiskeluoikeudet = opiskeluoikeudet)))
        } else {
          None
        }
      })
  }

  def getSuorittamisvalvottavatOppijatSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    getSuorittamisvalvottavatOppijatLaajatTiedot(oppilaitosOid)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))

  private def getSuorittamisvalvottavatOppijatLaajatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] =
    getOppijatLaajatTiedotIlmanHakutilanteita(ValpasRooli.OPPILAITOS_SUORITTAMINEN, oppilaitosOid)
      .map(_.map(poistaEronneetOpiskeluoikeudetJoillaUusiKelpaavaOpiskelupaikka))
      .map(poistaKuntailmoitetutOpiskeluoikeudet(säästäJosOpiskeluoikeusVoimassa = true))
      // poista oppijat, joille ei eronneiden poiston jälkeen jäänyt jäljelle yhtään suorittamisvalvottavia opiskeluoikeuksia
      .map(_.filter(onSuorittamisvalvottaviaOpiskeluoikeuksia))
      .map(_.map(oppijaLaajatTiedotService.fetchOppivelvollisuudenKeskeytykset))
      .map(_.map(poistaMuutKuinVoimassaolevatKeskeytykset))

  private def poistaEronneetOpiskeluoikeudetJoillaUusiKelpaavaOpiskelupaikka(
    oppija: OppijaHakutilanteillaLaajatTiedot
  ): OppijaHakutilanteillaLaajatTiedot = {
    val uudetOpiskeluoikeudet =
      oppija.oppija.opiskeluoikeudet.filterNot(
        opiskeluoikeus => onEronnutJaUusiOpiskelupaikkaVoimassa(
          opiskeluoikeus = opiskeluoikeus,
          muutOppijanOpiskeluoikeudet =
            oppija.oppija.opiskeluoikeudet.filterNot(opiskeluoikeus2 => opiskeluoikeus2.equals(opiskeluoikeus))
        )
      )

    oppija.copy(
      oppija = oppija.oppija.copy(
        opiskeluoikeudet = uudetOpiskeluoikeudet
      )
    )
  }

  private def onEronnutJaUusiOpiskelupaikkaVoimassa(
    opiskeluoikeus: ValpasOpiskeluoikeusLaajatTiedot,
    muutOppijanOpiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot]
  ): Boolean = {
    val onEronnut =
      opiskeluoikeus.onSuorittamisValvottava &&
        opiskeluoikeus.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänTila.koodiarvo)
          .exists(Seq("eronnut", "katsotaaneronneeksi", "peruutettu", "keskeytynyt").contains)

    val onValmistunutNivelvaiheesta =
      muutOppijanOpiskeluoikeudet.exists(oo => onNivelvaiheenOpiskeluoikeus(oo) &&
        oo.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänTila.koodiarvo)
          .exists(Seq("valmistunut", "hyvaksytystisuoritettu").contains)
      )

    val onLasnaUudessaOpiskeluoikeudessa =
      sisältääVoimassaolevanToisenAsteenOpiskeluoikeuden(muutOppijanOpiskeluoikeudet) ||
        (!onValmistunutNivelvaiheesta && sisältääVoimassaolevanNivelvaiheenOpiskeluoikeuden(muutOppijanOpiskeluoikeudet))

    onEronnut && onLasnaUudessaOpiskeluoikeudessa
  }

  private def sisältääVoimassaolevanToisenAsteenOpiskeluoikeuden(
    opiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot]
  ): Boolean =
    opiskeluoikeudet.exists(oo => onToisenAsteenOpiskeluoikeus(oo) && oo.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänTila.koodiarvo).contains("voimassa"))

  private def onToisenAsteenOpiskeluoikeus(oo: ValpasOpiskeluoikeusLaajatTiedot): Boolean = {
    oo.tyyppi.koodiarvo match {
      // Ammatillinen opiskeluoikeus: On toista astetta, jos ei ole nivelvaihetta
      case "ammatillinenkoulutus"
        if !onNivelvaiheenOpiskeluoikeus(oo) => true
      case "diatutkinto" => true
      case "ibtutkinto"  => true
      // International school on toista astetta, jos siinä on luokka-asteen 10+ suoritus. Tämä on tarkistettu jo SQL:ssä,
      // joten tässä riittää tutkia, onko perusopetuksen jälkeisiä tietoja määritelty.
      case "internationalschool" if oo.perusopetuksenJälkeinenTiedot.isDefined => true
      // Lukiokoulutus on toista astetta, jos siinä ei ole pelkkiä aineopintoja:
      case "lukiokoulutus"
        if oo.päätasonSuoritukset.exists(pts => pts.suorituksenTyyppi.koodiarvo == "lukionoppimaara") => true
      case _ => false
    }
  }

  private def sisältääVoimassaolevanNivelvaiheenOpiskeluoikeuden(
    opiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot]
  ): Boolean =
    opiskeluoikeudet.exists(oo => onNivelvaiheenOpiskeluoikeus(oo) && oo.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänTila.koodiarvo).contains("voimassa"))

  private def onNivelvaiheenOpiskeluoikeus(oo: ValpasOpiskeluoikeusLaajatTiedot): Boolean = {
    oo.tyyppi.koodiarvo match {
      // Ammatillinen opiskeluoikeus: Jos opiskeluoikeudessa on yksikin VALMA tai TELMA-päätason suoritus, se on nivelvaihetta.
      case "ammatillinenkoulutus"
        if oo.päätasonSuoritukset.exists(pts => List("valma", "telma").contains(pts.suorituksenTyyppi.koodiarvo)) => true
      // Aikuisten perusopetuksen opiskeluoikeus: On nivelvaihetta, jos siinä on alku- tai loppuvaiheen suoritus
      case "aikuistenperusopetus"
        if oo.päätasonSuoritukset.exists(
          pts => List(
            "aikuistenperusopetuksenoppimaaranalkuvaihe",
            "aikuistenperusopetuksenoppimaara"
          ).contains(pts.suorituksenTyyppi.koodiarvo)) => true
      // VST: on nivelvaihetta, jos ei ole vapaatavoitteista
      case "vapaansivistystyonkoulutus"
        if oo.päätasonSuoritukset.exists(pts => pts.suorituksenTyyppi.koodiarvo != "vstvapaatavoitteinenkoulutus") => true
      // Luva: aina nivelvaihetta
      case "luva" => true
      // Perusopetuksen lisäopetus: aina nivelvaihetta
      case "perusopetuksenlisaopetus" => true
      // Esim. lukio, DIA, IB tai international school ei ole ikinä nivelvaihetta:
      case _ => false
    }
  }

  private def onSuorittamisvalvottaviaOpiskeluoikeuksia(oppija: OppijaHakutilanteillaLaajatTiedot): Boolean =
    oppija.oppija.opiskeluoikeudet.exists(_.onSuorittamisValvottava)

  private def poistaMuutKuinVoimassaolevatKeskeytykset(
    oppija: OppijaHakutilanteillaLaajatTiedot
  ): OppijaHakutilanteillaLaajatTiedot =
    oppija.copy(oppivelvollisuudenKeskeytykset = oppija.oppivelvollisuudenKeskeytykset.filter(_.voimassa))

  def getKunnanOppijatSuppeatTiedot
    (kuntaOid: Organisaatio.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaKuntailmoituksillaSuppeatTiedot]] =
    getKunnanOppijatLaajatTiedot(kuntaOid)
      .map(_.map(OppijaKuntailmoituksillaSuppeatTiedot.apply))

  def getHakeutumisenvalvonnanKunnalleTehdytIlmoituksetSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    getOppilaitoksenKunnalleTekemätIlmoituksetLaajatTiedot(ValpasRooli.OPPILAITOS_HAKEUTUMINEN, oppilaitosOid)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))

  def getSuorittamisvalvonnanKunnalleTehdytIlmoituksetSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] = {
    getOppilaitoksenKunnalleTekemätIlmoituksetLaajatTiedot(ValpasRooli.OPPILAITOS_SUORITTAMINEN, oppilaitosOid)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))
  }

  private def getKunnanOppijatLaajatTiedot
    (kuntaOid: Organisaatio.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    accessResolver.assertAccessToOrg(ValpasRooli.KUNTA, kuntaOid)
      // Haetaan kuntailmoitukset Seq[ValpasKuntailmoitusLaajatTiedot]
      .flatMap(_ => application.valpasKuntailmoitusService.getKuntailmoituksetKunnalleIlmanKäyttöoikeustarkistusta(kuntaOid))

      // Haetaan kaikki oppijat, (Seq[ValpasKuntailmoitusLaajatTiedot], Seq[ValpasOppijaLaajatTiedot])
      .map(kuntailmoitukset => (
        kuntailmoitukset,
        accessResolver.filterByOppijaAccess(ValpasRooli.KUNTA)(
          opiskeluoikeusDbService
            // Tietokannassa ei voi olla kuntailmoituksia ilman oppijaOid:ia, joten oppijaOid:n olemassaoloa ei tässä
            // erikseen tarkisteta, vaan keskeytys ja sen seurauksena tuleva 500-virhe on ok, jos oppijaOid on None.
            .getOppijat(kuntailmoitukset.map(_.oppijaOid.get).distinct)
            .flatMap(oppijaLaajatTiedotService.asValpasOppijaLaajatTiedot(_).toOption)
        )
      ))

      // Yhdistetään kuntailmoitukset ja oppijat Seq[(ValpasOppijaLaajatTiedot, ValpasKuntailmoitusLaajatTiedot)]
      .map(kuntailmoituksetOppijat => kuntailmoituksetOppijat._1.flatMap(ilmoitus =>
        kuntailmoituksetOppijat._2
          // Tietokannassa ei voi olla kuntailmoituksia ilman oppijaOid:ia, joten oppijaOid:n olemassaoloa ei tässä
          // erikseen tarkisteta, vaan keskeytys ja sen seurauksena tuleva 500-virhe on ok, jos oppijaOid on None.
          .find(oppija => oppija.henkilö.kaikkiOidit.contains(ilmoitus.oppijaOid.get))
          .map(oppija => (oppija, ilmoitus)
          )))

      // Ryhmitellään henkilöiden master-oidien perusteella Seq[Seq[(ValpasOppijaLaajatTiedot, ValpasKuntailmoitusLaajatTiedot)]]
      .map(_.groupBy(_._1.henkilö.oid).values.toSeq)

      // Kääräistään tuplelistat muotoon Seq[OppijaHakutilanteillaLaajatTiedot]
      .map(_.map(oppijaJaKuntailmoitusTuples => {
        val oppija = oppijaJaKuntailmoitusTuples.head._1
        val kuntailmoituksetLaajatTiedot = oppijaJaKuntailmoitusTuples.map(_._2)
        val kuntailmoitukset = oppijaLaajatTiedotService.lisääAktiivisuustiedot(oppija)(kuntailmoituksetLaajatTiedot)

        OppijaHakutilanteillaLaajatTiedot(
          oppija = oppija,
          kuntailmoitukset = kuntailmoitukset
        )
      }))

      // Siivotaan hakutuloksista pois tapaukset, joilla ei ole kuntailmoituksia
      .map(_.filter(_.kuntailmoitukset.nonEmpty))
  }

  private def getOppijatLaajatTiedotIlmanHakutilanteita
    (rooli: ValpasRooli.Role, oppilaitosOid: ValpasOppilaitos.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    getOppijatLaajatTiedot(rooli, oppilaitosOid, HakeutumisvalvontaTieto.Kaikki)
      .map(oppijat => oppijat.map(OppijaHakutilanteillaLaajatTiedot.apply))
  }

  private def getOppijatLaajatTiedot
    (
      rooli: ValpasRooli.Role,
      oppilaitosOid: ValpasOppilaitos.Oid,
      hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value
    )
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[ValpasOppijaLaajatTiedot]] = {
    accessResolver.assertAccessToOrg(rooli, oppilaitosOid)
      .map(_ => opiskeluoikeusDbService.getOppijatByOppilaitos(oppilaitosOid, hakeutumisvalvontaTieto))
      .flatMap(results => HttpStatus.foldEithers(results.map(oppijaLaajatTiedotService.asValpasOppijaLaajatTiedot)))
      .map(accessResolver.filterByOppijaAccess(rooli))
  }

  def getOppijatLaajatTiedotYhteystiedoilla(
    oppilaitosOid: ValpasOppilaitos.Oid,
    oppijaOids: Seq[ValpasHenkilö.Oid]
  )(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    val errorClue = oppilaitosOidErrorClue(oppilaitosOid)

    accessResolver.assertAccessToOrg(ValpasRooli.OPPILAITOS_HAKEUTUMINEN, oppilaitosOid)
      .map(_ => opiskeluoikeusDbService.getOppijatByOppilaitos(oppilaitosOid, HakeutumisvalvontaTieto.Kaikki))
      .map(_.filter(oppijaRow => oppijaOids.contains(oppijaRow.oppijaOid)))
      .flatMap(results => HttpStatus.foldEithers(results.map(oppijaLaajatTiedotService.asValpasOppijaLaajatTiedot)))
      .map(accessResolver.filterByOppijaAccess(ValpasRooli.OPPILAITOS_HAKEUTUMINEN))
      .map(fetchHautYhteystiedoilla(errorClue, oppijaOids))
      .flatMap(oppijat => HttpStatus.foldEithers(oppijat.map(oppijaLaajatTiedotService.withVirallisetYhteystiedot)))
      .map(oppijat => oppijat.map(_.validate(koodistoviitepalvelu)))
  }

  def getOppijalistaIlmanOikeustarkastusta
    (oppijaOids: Seq[ValpasHenkilö.Oid])
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    rouhintaTimed("getOppijalista", oppijaOids.size) {
      HttpStatus.foldEithers({
        val oppijat = opiskeluoikeusDbService.getOppijat(oppijaOids, rajaaOVKelpoisiinOpiskeluoikeuksiin = false)

        rouhintaTimed("getOppijalista:asValpasOppijaLaajatTiedot", oppijat.size) {
          oppijat.map(oppijaLaajatTiedotService.asValpasOppijaLaajatTiedot)
        }
      })
        .map(asEmptyOppijaHakutilanteillaLaajatTiedot) // Huom! Ei haeta hakutietoja, halutaan vain vaihtaa tyyppi fetchOppivelvollisuudenKeskeytykset-kutsua varten
    }
  }

  def getOppijaOiditHetuillaIlmanOikeustarkastusta(hetut: Seq[String]): Seq[HetuMasterOid] = {
    opiskeluoikeusDbService.haeOppijatHetuilla(hetut)
  }

  def getOppivelvollisetKotikunnallaIlmanOikeustarkastusta(kunta: String): Seq[HetuMasterOid] = {
    opiskeluoikeusDbService.haeOppivelvollisetKotikunnalla(kunta)
  }

  private def asEmptyOppijaHakutilanteillaLaajatTiedot(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] = {
    rouhintaTimed("asEmptyOppijaHakutilanteillaLaajatTiedot", oppijat.size) {
      oppijat.map(asEmptyOppijaHakutilanteillaLaajatTiedot)
    }
  }
  private def asEmptyOppijaHakutilanteillaLaajatTiedot(oppija: ValpasOppijaLaajatTiedot): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot.apply(oppija = oppija, yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"), haut = Right(Seq.empty))
  }

  private def fetchHautIlmanYhteystietoja(errorClue: String, oppijatJoilleHaetaanHakutiedot: Seq[Henkilö.Oid])(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] =
    fetchHautYhteystiedoilla(errorClue, oppijatJoilleHaetaanHakutiedot)(oppijat)
      .map(oppija => oppija.copy(yhteystiedot = Seq.empty))

  private def fetchHautYhteystiedoilla(errorClue: String, oppijatJoilleHaetaanHakutiedot: Seq[Henkilö.Oid])(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] = {
    val oppijaOids = oppijat
      .map(_.henkilö.oid)
      .filter(oppijatJoilleHaetaanHakutiedot.contains)
      .toSet

    val hakukoosteet = hakukoosteService.getYhteishakujenHakukoosteet(oppijaOids = oppijaOids, ainoastaanAktiivisetHaut = true, errorClue = errorClue)

    hakukoosteet.map(_.groupBy(_.oppijaOid))
      .fold(
        error => oppijat.map(oppija => OppijaHakutilanteillaLaajatTiedot.apply(oppija = oppija, yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"), haut = Left(error))),
        groups => oppijat.map(oppija =>
          OppijaHakutilanteillaLaajatTiedot.apply(oppija = oppija, yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"), haut = Right(groups.getOrElse(oppija.henkilö.oid, Seq()))))
      )
  }

  def getOppilaitoksenKunnalleTekemätIlmoituksetLaajatTiedot(
    rooli: ValpasRooli.Role,
    oppilaitosOid: ValpasOppilaitos.Oid
  )(
    implicit session: ValpasSession
  ) : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    application.valpasKuntailmoitusService.getOppilaitoksenTekemätIlmoituksetIlmanKäyttöoikeustarkistusta(oppilaitosOid)
      .map(ilmoitukset => {
        // Tietokannassa ei voi olla kuntailmoituksia ilman oppijaOid:ia, joten oppijaOid:n olemassaoloa ei tässä
        // erikseen tarkisteta, vaan keskeytys ja sen seurauksena tuleva 500-virhe on ok, jos oppijaOid on None.
        val oppijaOids = ilmoitukset.map(_.oppijaOid.get)
        val oppijat = opiskeluoikeusDbService
          .getOppijat(oppijaOids)
          .flatMap(oppijaLaajatTiedotService.asValpasOppijaLaajatTiedot(_).toOption)
        val oppijatJoihinKatseluoikeus = accessResolver
          .filterByOppijaAccess(rooli)(oppijat)
          .map(OppijaHakutilanteillaLaajatTiedot.apply)
        val oppijatLisätiedoilla = lisätiedotRepository.readForOppijat(oppijatJoihinKatseluoikeus)
        val oppijatLaajatTiedot = oppijatLisätiedoilla.map(oppijaLisätiedotTuple =>
          oppijaLisätiedotTuple._1.withLisätiedot(oppijaLisätiedotTuple._2)
        )

        // Lisää kuntailmoitukset oppijan tietoihin
        oppijatLaajatTiedot.map(oppija => oppija.copy(
          kuntailmoitukset = ilmoitukset
            // Tietokannassa ei voi olla kuntailmoituksia ilman oppijaOid:ia, joten oppijaOid:n olemassaoloa ei tässä
            // erikseen tarkisteta, vaan keskeytys ja sen seurauksena tuleva 500-virhe on ok, jos oppijaOid on None.
            .filter(ilmoitus => oppija.oppija.henkilö.kaikkiOidit.contains(ilmoitus.oppijaOid.get))
        ))
      })
  }

  def withKuntailmoituksetIlmanKäyttöoikeustarkistusta(
    oppijaTiedot: Seq[OppijaHakutilanteillaLaajatTiedot]
  ): Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    application.valpasKuntailmoitusService.getKuntailmoituksetIlmanKäyttöoikeustarkistusta(oppijaTiedot.map(_.oppija))
      .map(kuntailmoitukset =>
        oppijaTiedot.map(oppijaTieto => {
          val oppijanIlmoitukset = kuntailmoitukset.filter(
            // Tietokannassa ei voi olla kuntailmoituksia ilman oppijaOid:ia, joten oppijaOid:n olemassaoloa ei tässä
            // erikseen tarkisteta, vaan keskeytys ja sen seurauksena tuleva 500-virhe on ok, jos oppijaOid on None.
            ilmoitus => oppijaTieto.oppija.henkilö.kaikkiOidit.contains(ilmoitus.oppijaOid.get)
          )
          val oppijanIlmoituksetAktiivisuustiedoilla =
            oppijaLaajatTiedotService.lisääAktiivisuustiedot(oppijaTieto.oppija)(oppijanIlmoitukset)

          oppijaTieto.copy(
            kuntailmoitukset = oppijanIlmoituksetAktiivisuustiedoilla
          )
        })
      )
  }
}

