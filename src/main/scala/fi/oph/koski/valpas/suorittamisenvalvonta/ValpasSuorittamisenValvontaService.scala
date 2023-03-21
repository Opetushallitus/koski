package fi.oph.koski.valpas.suorittamisenvalvonta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusrepository.{HakeutumisvalvontaTieto, ValpasOpiskeluoikeusLaajatTiedot, ValpasOppilaitos}
import fi.oph.koski.valpas.oppija.{OppijaHakutilanteillaLaajatTiedot, OppijaHakutilanteillaSuppeatTiedot}
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaTiming
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

import java.time.LocalDate


class ValpasSuorittamisenValvontaService(
  application: KoskiApplication
) extends Logging with ValpasRouhintaTiming {
  private val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  private val oppijalistatService = application.valpasOppijalistatService
  private val kuntailmoitusService = application.valpasKuntailmoitusService

  def getOppijatSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    getOppijatLaajatTiedot(oppilaitosOid)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))

  private def getOppijatLaajatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] =
    oppijalistatService.getOppijatLaajatTiedot(ValpasRooli.OPPILAITOS_SUORITTAMINEN, oppilaitosOid, HakeutumisvalvontaTieto.Kaikki)
      .map(oppijat => oppijat.map(OppijaHakutilanteillaLaajatTiedot.apply))
      .map(_.map(poistaEronneetOpiskeluoikeudetJoillaUusiKelpaavaOpiskelupaikka))
      .map(kuntailmoitusService.poistaKuntailmoitetutOpiskeluoikeudet(säästäJosOpiskeluoikeusVoimassa = true))
      // poista oppijat, joille ei eronneiden poiston jälkeen jäänyt jäljelle yhtään suorittamisvalvottavia opiskeluoikeuksia
      .map(_.filter(onSuorittamisvalvottaviaOpiskeluoikeuksia))
      .map(_.map(oppijaLaajatTiedotService.fetchOppivelvollisuudenKeskeytykset))
      .map(_.map(poistaMuutKuinVoimassaolevatKeskeytykset))

  private def poistaEronneetOpiskeluoikeudetJoillaUusiKelpaavaOpiskelupaikka(
    oppija: OppijaHakutilanteillaLaajatTiedot
  ): OppijaHakutilanteillaLaajatTiedot = {
    oppija.oppija.ifOppivelvollinenOtherwise(oppija) { o =>
      val uudetOpiskeluoikeudet =
        oppija.oppija.opiskeluoikeudet.filterNot(
          opiskeluoikeus => onEronnutTaiValmistunutJaUusiOpiskelupaikkaVoimassaTaiEronnutMyöhemmin(
            opiskeluoikeus = opiskeluoikeus,
            muutOppijanOpiskeluoikeudet =
              oppija.oppija.opiskeluoikeudet.filterNot(opiskeluoikeus2 => opiskeluoikeus2.equals(opiskeluoikeus))
          )
        )

      oppija.copy(
        oppija = o.copy(
          opiskeluoikeudet = uudetOpiskeluoikeudet
        )
      )
    }
  }

  private def onEronnutTaiValmistunutJaUusiOpiskelupaikkaVoimassaTaiEronnutMyöhemmin(
    opiskeluoikeus: ValpasOpiskeluoikeusLaajatTiedot,
    muutOppijanOpiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot]
  ): Boolean = {
    val onLasnaUudessaOpiskeluoikeudessa =
      sisältääVoimassaolevanToisenAsteenOpiskeluoikeuden(muutOppijanOpiskeluoikeudet) ||
        sisältääVoimassaolevanNivelvaiheenOpiskeluoikeuden(muutOppijanOpiskeluoikeudet)

    val onEronnutUudestaOpiskeluoikeudestaMyöhemmin =
      sisältääMyöhemminEronneenToisenAsteenOpiskeluoikeuden(opiskeluoikeus, muutOppijanOpiskeluoikeudet) ||
        sisältääMyöhemminEronneenNivelvaiheenOpiskeluoikeuden(opiskeluoikeus, muutOppijanOpiskeluoikeudet)

    // Tarkastellaan valmistumista toistaiseksi vain ammatillisen ostatutkintotavoitteisen osalta
    (onEronnut(opiskeluoikeus) || onValmistunutAmmatillinenOsatutkinto(opiskeluoikeus)) &&
      (onLasnaUudessaOpiskeluoikeudessa || onEronnutUudestaOpiskeluoikeudestaMyöhemmin)
  }

  private def sisältääVoimassaolevanToisenAsteenOpiskeluoikeuden(
    opiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot]
  ): Boolean =
    opiskeluoikeudet.exists(oo => onToisenAsteenOpiskeluoikeus(oo) && oo.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänTila.koodiarvo).contains("voimassa"))

  private def sisältääMyöhemminEronneenToisenAsteenOpiskeluoikeuden(
    opiskeluoikeus: ValpasOpiskeluoikeusLaajatTiedot,
    muutOppijanOpiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot]
  ): Boolean = muutOppijanOpiskeluoikeudet.exists(muuOpiskeluoikeus =>
    onToisenAsteenOpiskeluoikeus(muuOpiskeluoikeus) && onEronnutJaMyöhemminPäättynytOpiskeluoikeus(opiskeluoikeus, muuOpiskeluoikeus)
  )

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
      // TODO: TOR-1685 Eurooppalainen koulu
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

  private def sisältääMyöhemminEronneenNivelvaiheenOpiskeluoikeuden(
    opiskeluoikeus: ValpasOpiskeluoikeusLaajatTiedot,
    muutOppijanOpiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot]
  ): Boolean = muutOppijanOpiskeluoikeudet.exists(muuOpiskeluoikeus =>
    onNivelvaiheenOpiskeluoikeus(muuOpiskeluoikeus) && onEronnutJaMyöhemminPäättynytOpiskeluoikeus(opiskeluoikeus, muuOpiskeluoikeus)
  )

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
      // TUVA: aina nivelvaihetta
      case "tuva" => true
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

  def getKunnalleTehdytIlmoituksetSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] = {
    kuntailmoitusService.getOppilaitoksenKunnalleTekemätIlmoituksetLaajatTiedot(ValpasRooli.OPPILAITOS_SUORITTAMINEN, oppilaitosOid)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))
  }

  def onEronnut(opiskeluoikeus: ValpasOpiskeluoikeusLaajatTiedot): Boolean =
    opiskeluoikeus.onSuorittamisValvottava && onEronnutTila(opiskeluoikeus)

  def onValmistunutAmmatillinenOsatutkinto(opiskeluoikeus: ValpasOpiskeluoikeusLaajatTiedot): Boolean = {
    opiskeluoikeus.päätasonSuoritukset.exists(pts => pts.suorituksenTyyppi.koodiarvo == "ammatillinentutkintoosittainen") &&
      opiskeluoikeus.onSuorittamisValvottava &&
      opiskeluoikeus.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänTila.koodiarvo).contains("valmistunut")
  }

  def onEronnutTila(opiskeluoikeus: ValpasOpiskeluoikeusLaajatTiedot): Boolean =
    opiskeluoikeus.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänTila.koodiarvo)
      .exists(Seq("eronnut", "katsotaaneronneeksi", "peruutettu", "keskeytynyt").contains)

  def onEronnutJaMyöhemminPäättynytOpiskeluoikeus(
    opiskeluoikeus: ValpasOpiskeluoikeusLaajatTiedot,
    muuOpiskeluoikeus: ValpasOpiskeluoikeusLaajatTiedot
  ): Boolean = {
    def päättymispäivä(oo: ValpasOpiskeluoikeusLaajatTiedot): Option[LocalDate] = oo.perusopetuksenJälkeinenTiedot
      .flatMap(_.päättymispäivä)
      .map(LocalDate.parse)

    onEronnutTila(muuOpiskeluoikeus) &&
      päättymispäivä(muuOpiskeluoikeus).exists(muunOpiskeluoikeudenPäättymispäivä =>
        muunOpiskeluoikeudenPäättymispäivä.isAfter(
          päättymispäivä(opiskeluoikeus).getOrElse(LocalDate.MIN)
        )
      )
  }
}
