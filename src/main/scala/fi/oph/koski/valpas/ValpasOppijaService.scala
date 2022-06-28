package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö, Yhteystiedot}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.huoltaja.{HuollettavienHakuEpäonnistui, HuollettavienHakuOnnistui}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.{Henkilö, LocalizedString, Organisaatio}
import fi.oph.koski.util.ChainingSyntax.chainingOps
import fi.oph.koski.util.DateOrdering.localDateTimeOrdering
import fi.oph.koski.util.UuidUtils
import fi.oph.koski.validation.MaksuttomuusValidation
import fi.oph.koski.valpas.db.ValpasSchema
import fi.oph.koski.valpas.db.ValpasSchema.{OpiskeluoikeusLisätiedotKey, OpiskeluoikeusLisätiedotRow, OppivelvollisuudenKeskeytyshistoriaRow}
import fi.oph.koski.valpas.hakukooste.{Hakukooste, ValpasHakukoosteService}
import fi.oph.koski.valpas.kansalainen.{KansalainenOppijaIlmanTietoja, KansalainenOppijatiedot, KansalaisnäkymänTiedot}
import fi.oph.koski.valpas.opiskeluoikeusrepository._
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaTiming
import fi.oph.koski.valpas.valpasrepository._
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.koski.valpas.yhteystiedot.ValpasYhteystiedot

import java.util.UUID

case class OppijaHakutilanteillaLaajatTiedot(
  oppija: ValpasOppijaLaajatTiedot,
  hakutilanteet: Seq[ValpasHakutilanneLaajatTiedot],
  hakutilanneError: Option[String],
  yhteystiedot: Seq[ValpasYhteystiedot],
  kuntailmoitukset: Seq[ValpasKuntailmoitusLaajatTiedot],
  oppivelvollisuudenKeskeytykset: Seq[ValpasOppivelvollisuudenKeskeytys],
  onOikeusTehdäKuntailmoitus: Option[Boolean],
  lisätiedot: Seq[OpiskeluoikeusLisätiedot]
) {
  def validate(koodistoviitepalvelu: KoodistoViitePalvelu): OppijaHakutilanteillaLaajatTiedot =
    this.copy(hakutilanteet = hakutilanteet.map(_.validate(koodistoviitepalvelu)))

  def withLisätiedot(lisätiedot: Seq[OpiskeluoikeusLisätiedotRow]): OppijaHakutilanteillaLaajatTiedot = {
    this.copy(
      lisätiedot = lisätiedot.map(l => OpiskeluoikeusLisätiedot(
        oppijaOid = l.oppijaOid,
        opiskeluoikeusOid = l.opiskeluoikeusOid,
        oppilaitosOid = l.oppilaitosOid,
        muuHaku = l.muuHaku
      ))
    )
  }
}

object OppijaHakutilanteillaLaajatTiedot {
  def apply(oppija: ValpasOppijaLaajatTiedot, yhteystietoryhmänNimi: LocalizedString, haut: Either[HttpStatus, Seq[Hakukooste]]): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot(
      oppija = oppija,
      hakutilanteet = haut.map(_.map(ValpasHakutilanneLaajatTiedot.apply)).getOrElse(Seq()),
      // TODO: Pitäisikö virheet mankeloida jotenkin eikä palauttaa sellaisenaan fronttiin?
      hakutilanneError = haut.left.toOption.flatMap(_.errorString),
      yhteystiedot = haut.map(uusimmatIlmoitetutYhteystiedot(yhteystietoryhmänNimi)).getOrElse(Seq.empty),
      kuntailmoitukset = Seq.empty,
      oppivelvollisuudenKeskeytykset = Seq.empty,
      onOikeusTehdäKuntailmoitus = None,
      lisätiedot = Seq.empty
    )
  }

  def apply(oppija: ValpasOppijaLaajatTiedot, kuntailmoitukset: Seq[ValpasKuntailmoitusLaajatTiedot]): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot(
      oppija = oppija,
      hakutilanteet = Seq.empty,
      hakutilanneError = None,
      yhteystiedot = Seq.empty,
      kuntailmoitukset = kuntailmoitukset,
      oppivelvollisuudenKeskeytykset = Seq.empty,
      onOikeusTehdäKuntailmoitus = None,
      lisätiedot = Seq.empty
    )
  }

  def apply(oppija: ValpasOppijaLaajatTiedot): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot(
      oppija = oppija,
      hakutilanteet = Seq.empty,
      hakutilanneError = None,
      yhteystiedot = Seq.empty,
      kuntailmoitukset = Seq.empty,
      oppivelvollisuudenKeskeytykset = Seq.empty,
      onOikeusTehdäKuntailmoitus = None,
      lisätiedot = Seq.empty
    )
  }

  private def uusimmatIlmoitetutYhteystiedot(yhteystietoryhmänNimi: LocalizedString)(hakukoosteet: Seq[Hakukooste]): Seq[ValpasYhteystiedot] =
    hakukoosteet
      .sortBy(hk => hk.hakemuksenMuokkauksenAikaleima.getOrElse(hk.haunAlkamispaivamaara))
      .lastOption
      .map(haku => List(
        ValpasYhteystiedot.oppijanIlmoittamatYhteystiedot(haku, yhteystietoryhmänNimi),
      ))
      .getOrElse(List.empty)
}

case class OpiskeluoikeusLisätiedot(
  oppijaOid: ValpasHenkilö.Oid,
  opiskeluoikeusOid: ValpasOpiskeluoikeus.Oid,
  oppilaitosOid: ValpasOppilaitos.Oid,
  muuHaku: Boolean
)

class ValpasOppijaService(
  application: KoskiApplication
) extends Logging with ValpasRouhintaTiming {
  private val henkilöRepository = application.henkilöRepository
  private val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  private val hakukoosteService = ValpasHakukoosteService(application.config, application.validatingAndResolvingExtractor)
  private val opiskeluoikeusDbService = application.valpasOpiskeluoikeusDatabaseService
  private val ovKeskeytysService = new OppivelvollisuudenKeskeytysService(application)
  private val oppijanumerorekisteri = application.opintopolkuHenkilöFacade
  private val localizationRepository = application.valpasLocalizationRepository
  private val koodistoviitepalvelu = application.koodistoViitePalvelu
  private val lisätiedotRepository = application.valpasOpiskeluoikeusLisätiedotRepository
  private val rajapäivätService = application.valpasRajapäivätService

  private val accessResolver = new ValpasAccessResolver

  private val validatingAndResolvingExtractor = application.validatingAndResolvingExtractor

  private val roolitJoilleHaetaanKaikistaOVLPiirinOppijoista: Seq[ValpasRooli.Role] = Seq(
    ValpasRooli.OPPILAITOS_MAKSUTTOMUUS,
    ValpasRooli.KUNTA,
  )

  def getHakeutumisvalvottavatOppijatLaajatTiedot
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

  def getSuorittamisvalvottavatOppijatLaajatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] =
    getOppijatLaajatTiedotIlmanHakutilanteita(ValpasRooli.OPPILAITOS_SUORITTAMINEN, oppilaitosOid)
      .map(_.map(poistaEronneetOpiskeluoikeudetJoillaUusiKelpaavaOpiskelupaikka))
      .map(poistaKuntailmoitetutOpiskeluoikeudet(säästäJosOpiskeluoikeusVoimassa = true))
      // poista oppijat, joille ei eronneiden poiston jälkeen jäänyt jäljelle yhtään suorittamisvalvottavia opiskeluoikeuksia
      .map(_.filter(onSuorittamisvalvottaviaOpiskeluoikeuksia))
      .map(_.map(fetchOppivelvollisuudenKeskeytykset))
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

  def getKunnanOppijatLaajatTiedot
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
            .flatMap(asValpasOppijaLaajatTiedot(_).toOption)
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
        val kuntailmoitukset = lisääAktiivisuustiedot(oppija)(kuntailmoituksetLaajatTiedot)

        OppijaHakutilanteillaLaajatTiedot(
          oppija = oppija,
          kuntailmoitukset = kuntailmoitukset
        )
      }))

      // Siivotaan hakutuloksista pois tapaukset, joilla ei ole kuntailmoituksia
      .map(_.filter(_.kuntailmoitukset.nonEmpty))
  }

  private def oppilaitosOidErrorClue(oppilaitosOid: ValpasOppilaitos.Oid): String =
    s"oppilaitos: ${oppilaitosOid}"

  private def getHakeutumisvalvottavatOppijatLaajatTiedotHakutilanteilla
    (rooli: ValpasRooli.Role, oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value, oppijatJoilleHaetaanHakutiedot: Seq[Henkilö.Oid])
    (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    val errorClue = oppilaitosOidErrorClue(oppilaitosOid)

    getOppijatLaajatTiedot(rooli, oppilaitosOid, hakeutumisvalvontaTieto)
      .map(fetchHautIlmanYhteystietoja(errorClue, oppijatJoilleHaetaanHakutiedot))
  }

  private def getHakeutumisvalvottavatOppijatLaajatTiedotIlmanHakutilanteita
    (rooli: ValpasRooli.Role, oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value)
    (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    getOppijatLaajatTiedot(rooli, oppilaitosOid, hakeutumisvalvontaTieto)
      .map(oppijat => oppijat.map(OppijaHakutilanteillaLaajatTiedot.apply))
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
      .flatMap(results => HttpStatus.foldEithers(results.map(asValpasOppijaLaajatTiedot)))
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
      .flatMap(results => HttpStatus.foldEithers(results.map(asValpasOppijaLaajatTiedot)))
      .map(accessResolver.filterByOppijaAccess(ValpasRooli.OPPILAITOS_HAKEUTUMINEN))
      .map(fetchHautYhteystiedoilla(errorClue, oppijaOids))
      .flatMap(oppijat => HttpStatus.foldEithers(oppijat.map(withVirallisetYhteystiedot)))
      .map(oppijat => oppijat.map(_.validate(koodistoviitepalvelu)))
  }

  def getOppijaLaajatTiedot
    (rooli: ValpasRooli.Role, oppijaOid: ValpasHenkilö.Oid, haeMyösVainOppijanumerorekisterissäOleva: Boolean)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    val rajaaOVKelpoisiinOpiskeluoikeuksiin = !roolitJoilleHaetaanKaikistaOVLPiirinOppijoista.contains(rooli)

    opiskeluoikeusDbService.getOppija(oppijaOid, rajaaOVKelpoisiinOpiskeluoikeuksiin) match {
      case Some(oppijaRow) =>
        asValpasOppijaLaajatTiedot(oppijaRow)
          .flatMap(accessResolver.withOppijaAccessAsRole(rooli))
      case None if haeMyösVainOppijanumerorekisterissäOleva =>
        getKuntakäyttäjälleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(oppijaOid)
      case _ =>
        Left(ValpasErrorCategory.forbidden.oppija())
    }
  }

  private def getKuntakäyttäjälleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
    oppijaOid: ValpasHenkilö.Oid
  ): Either[HttpStatus, ValpasOppijaLaajatTiedot] =
    getOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(
      onKunnalleNäkyväVainOnrssäOlevaOppija,
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
    val onMahdollisestiLainPiirissä =
      MaksuttomuusValidation.eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
        henkilö.syntymäaika,
        opiskeluoikeusRepository.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(henkilö.oid),
        rajapäivätService
      ).isEmpty
    lazy val onAlle18VuotiasTarkastelupäivänä = rajapäivätService.onAlle18VuotiasTarkastelupäivänä(henkilö.syntymäaika)
    lazy val näytäKotikunnanPerusteella = onKotikunnanPerusteellaLaajennetunOppivelvollisuudenPiirissä(henkilö)

    onMahdollisestiLainPiirissä && onAlle18VuotiasTarkastelupäivänä && näytäKotikunnanPerusteella
  }

  private def onKotikunnanPerusteellaLaajennetunOppivelvollisuudenPiirissä(henkilö: OppijaHenkilö): Boolean = {
    asLaajatOppijaHenkilöTiedot(henkilö) match {
      case Some(o) if o.turvakielto || !o.laajennetunOppivelvollisuudenUlkopuolinenKunnanPerusteella =>
        true
      case _ =>
        // TODO: mitä jos laajojen tietojen onr-haku epäonnistuu esim. sen ollessa väliaikaisesti alhaalla?
        // Pitäisikö palauttaa joku virheilmoitus käyttöliittymään asti?
        false
    }
  }

  def asLaajatOppijaHenkilöTiedot(henkilö: OppijaHenkilö): Option[LaajatOppijaHenkilöTiedot] = {
    henkilö match {
      case h: LaajatOppijaHenkilöTiedot => Some(h)
      case _ => henkilöRepository.findByOid(henkilö.oid, findMasterIfSlaveOid = true)
    }
  }

  def getOppijaLaajatTiedotIlmanOikeustarkastusta(oppijaOid: ValpasHenkilö.Oid) : Either[HttpStatus, Option[ValpasOppijaLaajatTiedot]] = {
    val rajaaOVKelpoisiinOpiskeluoikeuksiin = false
    opiskeluoikeusDbService.getOppija(oppijaOid, rajaaOVKelpoisiinOpiskeluoikeuksiin) match {
      case Some(dbRow) => asValpasOppijaLaajatTiedot(dbRow).map(Some(_))
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
      getOppijaLaajatTiedot(laajimmatRoolit.head, oppijaOid, haeMyösVainOppijanumerorekisterissäOleva = false)
    } else {
      getOppijaLaajatTiedot(roolit.head, oppijaOid, haeMyösVainOppijanumerorekisterissäOleva = false)
    }
  }

  def getOppijaLaajatTiedot
    (oppijaOid: ValpasHenkilö.Oid, haeMyösVainOppijanumerorekisterissäOleva: Boolean = false)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    opiskeluoikeusDbService.getOppija(oppijaOid) match {
      case Some(oppijaRow) =>
        asValpasOppijaLaajatTiedot(oppijaRow)
          .flatMap(accessResolver.withOppijaAccess(_))
      case None if haeMyösVainOppijanumerorekisterissäOleva =>
        getKuntakäyttäjälleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(oppijaOid)
      case _ =>
        Left(ValpasErrorCategory.forbidden.oppija())
    }
  }

  def getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla
    (oppijaOid: ValpasHenkilö.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] = {
    val haeMyösVainOppijanumerorekisterissäOleva = accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)

    val rooli = roolitJoilleHaetaanKaikistaOVLPiirinOppijoista.find(accessResolver.accessToAnyOrg)

    getOppijaLaajatTiedotHakuJaYhteystiedoilla(oppijaOid, rooli, haeMyösVainOppijanumerorekisterissäOleva)
      .flatMap(withKuntailmoitukset)
      .map(withOikeusTehdäKuntailmoitus)
  }

  def getOppijalistaIlmanOikeustarkastusta
    (oppijaOids: Seq[ValpasHenkilö.Oid])
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    rouhintaTimed("getOppijalista", oppijaOids.size) {
      HttpStatus.foldEithers({
        val oppijat = opiskeluoikeusDbService.getOppijat(oppijaOids, rajaaOVKelpoisiinOpiskeluoikeuksiin = false)

        rouhintaTimed("getOppijalista:asValpasOppijaLaajatTiedot", oppijat.size) {
          oppijat.map(asValpasOppijaLaajatTiedot)
        }
      })
        .map(asEmptyOppijaHakutilanteillaLaajatTiedot) // Huom! Ei haeta hakutietoja, halutaan vain vaihtaa tyyppi fetchOppivelvollisuudenKeskeytykset-kutsua varten
    }
  }

  def getOppijaLaajatTiedotHakuJaYhteystiedoilla
    (oppijaOid: ValpasHenkilö.Oid, rooli: Option[ValpasRooli.Role] = None, haeMyösVainOppijanumerorekisterissäOleva: Boolean = false)
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] = {
    if (rooli.isEmpty && haeMyösVainOppijanumerorekisterissäOleva) {
      throw new InternalError("Ei voi tapahtua: vain onr:ssä olevat haetaan vain kuntakäyttäjälle, jolloin roolinkin pitää olla määritelty")
    }
    (rooli match {
      case None => getOppijaLaajatTiedot(oppijaOid)
      case Some(r) => getOppijaLaajatTiedot(r, oppijaOid, haeMyösVainOppijanumerorekisterissäOleva)
    })
      .map(fetchHakuYhteystiedoilla)
      .flatMap(withVirallisetYhteystiedot)
      .map(_.validate(koodistoviitepalvelu))
      .map(fetchOppivelvollisuudenKeskeytykset)
  }

  def getOppijaOiditHetuillaIlmanOikeustarkastusta(hetut: Seq[String]) = {
    opiskeluoikeusDbService.haeOppijatHetuilla(hetut)
  }

  def getOppivelvollisetKotikunnallaIlmanOikeustarkastusta(kunta: String): Seq[HetuMasterOid] = {
    opiskeluoikeusDbService.haeOppivelvollisetKotikunnalla(kunta)
  }

  def addOppivelvollisuudenKeskeytys
    (keskeytys: UusiOppivelvollisuudenKeskeytys)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppivelvollisuudenKeskeytys] = {
    val haeMyösVainOppijanumerorekisterissäOleva = accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)

    for {
      saaTehdäIlmoituksen <- accessResolver.assertAccessToOrg(ValpasRooli.KUNTA, keskeytys.tekijäOrganisaatioOid)
      oppija              <- getOppijaLaajatTiedot(keskeytys.oppijaOid, haeMyösVainOppijanumerorekisterissäOleva)
      ovKeskeytys         <- ovKeskeytysService.create(keskeytys)
    } yield ovKeskeytys
  }

  def updateOppivelvollisuudenKeskeytys
    (muutos: OppivelvollisuudenKeskeytyksenMuutos)
    (implicit session: ValpasSession)
  : Either[HttpStatus, (ValpasSchema.OppivelvollisuudenKeskeytysRow, ValpasOppivelvollisuudenKeskeytys)] = {
    val haeMyösVainOppijanumerorekisterissäOleva = accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)

    UuidUtils.optionFromString(muutos.id)
      .toRight(ValpasErrorCategory.badRequest.validation.epävalidiUuid())
      .flatMap(uuid => ovKeskeytysService.getLaajatTiedot(uuid).toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia()))
      .flatMap(keskeytys => {
        accessResolver
          .assertAccessToOrg(ValpasRooli.KUNTA, keskeytys.tekijäOrganisaatioOid)
          .flatMap(_ => getOppijaLaajatTiedot(keskeytys.oppijaOid, haeMyösVainOppijanumerorekisterissäOleva))
          .flatMap(accessResolver.withOppijaAccess(_))
          .flatMap(_ => ovKeskeytysService.update(muutos))
          .map(_ => (keskeytys, ovKeskeytysService.getSuppeatTiedot(UUID.fromString(muutos.id)).get))
      })
  }

  def deleteOppivelvollisuudenKeskeytys
    (uuid: UUID)
    (implicit session: ValpasSession)
  : Either[HttpStatus, (ValpasSchema.OppivelvollisuudenKeskeytysRow, ValpasOppivelvollisuudenKeskeytys)] = {
    val haeMyösVainOppijanumerorekisterissäOleva = accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)

    ovKeskeytysService.getLaajatTiedot(uuid)
      .toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
      .flatMap(keskeytys => {
        accessResolver
          .assertAccessToOrg(ValpasRooli.KUNTA, keskeytys.tekijäOrganisaatioOid)
          .flatMap(_ => getOppijaLaajatTiedot(keskeytys.oppijaOid, haeMyösVainOppijanumerorekisterissäOleva))
          .flatMap(accessResolver.withOppijaAccess(_))
          .flatMap(_ => ovKeskeytysService.delete(uuid))
          .map(k => (keskeytys, ovKeskeytysService.getSuppeatTiedot(UUID.fromString(k.id)).get))
      })
  }

  def getOppivelvollisuudenKeskeytyksenMuutoshistoria
    (uuid: UUID)
    (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppivelvollisuudenKeskeytyshistoriaRow]] = {
    val haeMyösVainOppijanumerorekisterissäOleva = accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)

    val ovKeskeytyshistoria = ovKeskeytysService.getMuutoshistoria(uuid)
    ovKeskeytyshistoria
      .headOption
      .toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
      .map(_.oppijaOid)
      .flatMap(o => getOppijaLaajatTiedot(o, haeMyösVainOppijanumerorekisterissäOleva)) // Tarkasta oikeus katsoa oppijan tietoja getOppijaLaajatTiedot avulla
      .map(_ => ovKeskeytyshistoria)
  }

  private def asValpasOppijaLaajatTiedot(dbRow: ValpasOppijaRow): Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    validatingAndResolvingExtractor
      .extract[List[ValpasOpiskeluoikeusLaajatTiedot]](strictDeserialization)(dbRow.opiskeluoikeudet)
      .left.map(e => {
        logger.error(e.toString)
        ValpasErrorCategory.internalError("Oppijan tietojen haku epäonnistui")
      })
      .map(opiskeluoikeudet =>
        ValpasOppijaLaajatTiedot(
          henkilö = ValpasHenkilöLaajatTiedot(
            oid = dbRow.oppijaOid,
            kaikkiOidit = dbRow.kaikkiOppijaOidit.toSet,
            hetu = dbRow.hetu,
            syntymäaika = dbRow.syntymäaika,
            etunimet = dbRow.etunimet,
            sukunimi = dbRow.sukunimi,
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
        )
      )
  }

  private def withVirallisetYhteystiedot(
    o: OppijaHakutilanteillaLaajatTiedot
  ): Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] =
    fetchVirallisetYhteystiedot(o.oppija)
      .map(yhteystiedot => o.copy(
        yhteystiedot = o.yhteystiedot ++ yhteystiedot.map(yt => ValpasYhteystiedot.virallinenYhteystieto(yt, localizationRepository.get("oppija__viralliset_yhteystiedot")))
      ))

  private def fetchHakuYhteystiedoilla(oppija: ValpasOppijaLaajatTiedot): OppijaHakutilanteillaLaajatTiedot = {
    val hakukoosteet = hakukoosteService.getYhteishakujenHakukoosteet(oppijaOids = Set(oppija.henkilö.oid), ainoastaanAktiivisetHaut = false, errorClue = s"oppija:${oppija.henkilö.oid}")
    OppijaHakutilanteillaLaajatTiedot.apply(oppija = oppija, yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"), haut = hakukoosteet)
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

  private def fetchVirallisetYhteystiedot(oppija: ValpasOppijaLaajatTiedot): Either[HttpStatus, Seq[Yhteystiedot]] = {
    if (oppija.henkilö.turvakielto) {
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
            lisääAktiivisuustiedot(oppijaTieto.oppija)(oppijanIlmoitukset)

          oppijaTieto.copy(
            kuntailmoitukset = oppijanIlmoituksetAktiivisuustiedoilla
          )
        })
      )
  }

  private def withKuntailmoitukset(
    o: OppijaHakutilanteillaLaajatTiedot
  )(implicit session: ValpasSession): Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] =
    fetchKuntailmoitukset(o.oppija)
      .map(kuntailmoitukset => o.copy(kuntailmoitukset = kuntailmoitukset))

  private def withKuntailmoituksetIlmanKäyttöoikeustarkastusta(
    o: OppijaHakutilanteillaLaajatTiedot
  ): Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] = {
    timed("fetchKuntailmoitukset", 10) {
      application.valpasKuntailmoitusService.getKuntailmoituksetIlmanKäyttöoikeustarkistusta(o.oppija)
        .map(lisääAktiivisuustiedot(o.oppija))
        .map(kuntailmoitukset => o.copy(kuntailmoitukset = kuntailmoitukset))
    }
  }

  private def fetchKuntailmoitukset(
    oppija: ValpasOppijaLaajatTiedot
  )(implicit session: ValpasSession): Either[HttpStatus, Seq[ValpasKuntailmoitusLaajatTiedot]] = {
    timed("fetchKuntailmoitukset", 10) {
      application.valpasKuntailmoitusService.getKuntailmoitukset(oppija)
        .map(lisääAktiivisuustiedot(oppija))
    }
  }

  private def lisääAktiivisuustiedot(
    oppija: ValpasOppijaLaajatTiedot
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
        // 2. Voimassaoleva ovl-kelpoinen opiskeluoikeus alkanut kuntailmoituksen tekemisen jälkeen --> passiivinen
        else if (oppija.opiskeluoikeudet.exists(oo => oo.isOpiskelu && oo.oppivelvollisuudenSuorittamiseenKelpaava && oo.alkamispäivä.exists(_.isAfter(ilmoituksentekopäivä)))) {
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

  private def fetchOppivelvollisuudenKeskeytykset(
    oppija: OppijaHakutilanteillaLaajatTiedot
  ): OppijaHakutilanteillaLaajatTiedot = {
    oppija.copy(
      oppivelvollisuudenKeskeytykset = ovKeskeytysService.getKeskeytykset(oppija.oppija.henkilö.kaikkiOidit.toSeq)
    )
  }

  private def withOikeusTehdäKuntailmoitus(
    oppija: OppijaHakutilanteillaLaajatTiedot
  )(implicit session: ValpasSession): OppijaHakutilanteillaLaajatTiedot = {
    val onOikeus = if (oppija.oppija.henkilö.onTallennettuKoskeen) {
      application.valpasKuntailmoitusService.withOikeusTehdäKuntailmoitusOppijalle(oppija.oppija)
        .fold(_ => false, _ => true)
    } else {
      false
    }
    oppija.copy(onOikeusTehdäKuntailmoitus = Some(onOikeus))
  }

  def setMuuHaku(key: OpiskeluoikeusLisätiedotKey, value: Boolean)(implicit session: ValpasSession): HttpStatus = {
    HttpStatus.justStatus(
      accessResolver.assertAccessToOrg(ValpasRooli.OPPILAITOS_HAKEUTUMINEN, key.oppilaitosOid)
        .flatMap(_ => getOppijaLaajatTiedot(ValpasRooli.OPPILAITOS_HAKEUTUMINEN, key.oppijaOid, haeMyösVainOppijanumerorekisterissäOleva = false))
        .flatMap(accessResolver.withOppijaAccessAsOrganisaatio(ValpasRooli.OPPILAITOS_HAKEUTUMINEN, key.oppilaitosOid))
        .flatMap(accessResolver.withOpiskeluoikeusAccess(ValpasRooli.OPPILAITOS_HAKEUTUMINEN)(key.opiskeluoikeusOid))
        .flatMap(_ => lisätiedotRepository.setMuuHaku(key, value).toEither)
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
          .flatMap(asValpasOppijaLaajatTiedot(_).toOption)
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

  def getKansalaisnäkymänTiedot()(implicit session: ValpasSession): KansalaisnäkymänTiedot = {
    val omatTiedot = getKansalaisenTiedotIlmanKäyttöoikeustarkastusta(session.user.oid)

    val huollettavat = session.user.huollettavat.toList.flatMap {
      case r: HuollettavienHakuOnnistui => r.huollettavat
        .map(o => {
          def fallback = KansalainenOppijaIlmanTietoja(nimi = s"${o.sukunimi} ${o.etunimet}", hetu = o.hetu)
          o.oid
            .toRight(fallback)
            .map(oid => getKansalaisenTiedotIlmanKäyttöoikeustarkastusta(oppijaOid = oid, piilotaTurvakieltoaineisto = true))
            .flatMap(_.left.map(_ => fallback))
        })
      case _ => Seq.empty
    }

    KansalaisnäkymänTiedot(
      omatTiedot = omatTiedot.toOption,
      huollettavat = huollettavat.collect { case Right(r) => r },
      huollettavatIlmanTietoja = huollettavat.collect { case Left(l) => l },
    )
  }

  private def getKansalaisenTiedotIlmanKäyttöoikeustarkastusta(
    oppijaOid: ValpasHenkilö.Oid,
    piilotaTurvakieltoaineisto: Boolean = false
  ): Either[HttpStatus, KansalainenOppijatiedot] = {
    def yhteystiedotHaettava(o: OppijaHakutilanteillaLaajatTiedot): Boolean = !piilotaTurvakieltoaineisto || !o.oppija.henkilö.turvakielto
    def turvakiellonAlainenTietoPoistettava(o: KansalainenOppijatiedot): Boolean = piilotaTurvakieltoaineisto && o.oppija.henkilö.turvakielto

    opiskeluoikeusDbService
      .getOppija(oppijaOid, rajaaOVKelpoisiinOpiskeluoikeuksiin = false)
      .toRight(ValpasErrorCategory.notFound.oppijaEiOppivelvollisuuslainPiirissä())
      .flatMap(asValpasOppijaLaajatTiedot)
      .map(fetchHakuYhteystiedoilla)
      .flatMap(o => if (yhteystiedotHaettava(o)) withVirallisetYhteystiedot(o) else Right(o) )
      .map(_.validate(koodistoviitepalvelu))
      .map(fetchOppivelvollisuudenKeskeytykset)
      .flatMap(withKuntailmoituksetIlmanKäyttöoikeustarkastusta)
      .map(KansalainenOppijatiedot.apply)
      .map(o => if (turvakiellonAlainenTietoPoistettava(o)) o.poistaTurvakiellonAlaisetTiedot else o)
  }
}

