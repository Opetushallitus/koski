package fi.oph.koski.valpas.kuntavalvonta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.valpas.oppija.{OppijaHakutilanteillaLaajatTiedot, OppijaKuntailmoituksillaSuppeatTiedot, ValpasAccessResolver}
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaTiming
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.koski.util.Monoids._
import fi.oph.koski.valpas.valpasrepository.ValpasKuntailmoitusLaajatTiedot

class ValpasKuntavalvontaService(
  application: KoskiApplication
) extends Logging with ValpasRouhintaTiming {
  private val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  private val opiskeluoikeusDbService = application.valpasOpiskeluoikeusDatabaseService

  private val accessResolver = new ValpasAccessResolver

  def getOppijatSuppeatTiedot
    (kuntaOid: Organisaatio.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaKuntailmoituksillaSuppeatTiedot]] =
    getOppijatLaajatTiedot(kuntaOid)
      .map(_.map(OppijaKuntailmoituksillaSuppeatTiedot.apply))

  private def getOppijatLaajatTiedot
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
        val kuntailmoitukset = oppija.mapOppivelvollinen(oppijaLaajatTiedotService.lisääAktiivisuustiedot(_)(kuntailmoituksetLaajatTiedot))(seqMonoid)

        OppijaHakutilanteillaLaajatTiedot(
          oppija = oppija,
          kuntailmoitukset = kuntailmoitukset
        )
      }))

      // Siivotaan hakutuloksista pois tapaukset, joilla ei ole kuntailmoituksia
      .map(_.filter(_.kuntailmoitukset.nonEmpty))
  }

}
