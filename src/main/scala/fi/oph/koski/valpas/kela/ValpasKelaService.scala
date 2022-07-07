package fi.oph.koski.valpas.kela

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log._
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.db.ValpasSchema.OppivelvollisuudenKeskeytysRow
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppivelvollisuustiedotRow

class ValpasKelaService(application: KoskiApplication) extends Logging with Timing {
  val henkilöRepository = application.henkilöRepository
  val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  val rajapäivätService = application.valpasRajapäivätService
  val oppijanumerorekisteriService = application.valpasOppijanumerorekisteriService

  def findValpasKelaOppijatByHetut(hetut: Seq[String]): Either[HttpStatus, Seq[ValpasKelaOppija]] = {

    val timedBlockName = hetut.length match {
      case 1 => "findValpasKelaOppijatByHetut1"
      case n if n <= 100 => "findValpasKelaOppijatByHetut2To100"
      case n if n <= 500 => "findValpasKelaOppijatByHetut101To500"
      case _ => "findValpasKelaOppijatByHetut501To1000"
    }

    timed(timedBlockName, 10) {
      val oppivelvollisuusTiedot = application.valpasOpiskeluoikeusDatabaseService.getOppivelvollisuusTiedot(hetut)
      val koskeenTallennetutPalautettavatOppijat = asValpasKelaOppijatWithOppivelvollisuudenKeskeytykset(oppivelvollisuusTiedot)

      val hetutJoitaEiLöytynytKoskesta = hetut.diff(oppivelvollisuusTiedot.map(_.hetu.getOrElse("")))

      // Väliaikainen logitus mahdollisen ONR-kuormituksen tarkkailemiseksi seuraavassa Kelan massahaussa. Voi poistaa sen jälkeen.
      logger.info(s"Kela haki ${hetut.length} oppijaa, joista ${hetutJoitaEiLöytynytKoskesta.length} ei löytynyt Koskesta vaan haetaan ONR:stä")

      val oppijatJotkaLöytyvätOnrstä =
        hetutJoitaEiLöytynytKoskesta.flatMap(hetu => henkilöRepository.opintopolku.findByHetu(hetu))
          .filter(o => oppijanumerorekisteriService.onKelalleNäkyväVainOnrssäOlevaOppija(o))

      val onrPalautettavatOppijat = tiedotAsValpasKelaOppijatWithOppivelvollisuudenKeskeytykset(oppijatJotkaLöytyvätOnrstä)

      val oppijatJoidenMaksuttomuusoikeusOnVieläVoimassa = (koskeenTallennetutPalautettavatOppijat ++ onrPalautettavatOppijat)
        .filter(_.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti match {
          case Some(d) if !rajapäivätService.tarkastelupäivä.isAfter(d) => true
          case None => true
          case _ => false
        })

      Right(oppijatJoidenMaksuttomuusoikeusOnVieläVoimassa)
    }
  }

  private def asValpasKelaOppijatWithOppivelvollisuudenKeskeytykset(oppijat: Seq[ValpasOppivelvollisuustiedotRow]): Seq[ValpasKelaOppija] = {
    val keskeytykset: Map[String, Seq[OppivelvollisuudenKeskeytysRow]] =
      application.valpasOppivelvollisuudenKeskeytysRepository
        .getKeskeytykset(oppijat.flatMap(_.kaikkiOppijaOidit))
        .groupBy(_.oppijaOid)
        .withDefaultValue(Seq.empty)

    oppijat.map(oppija => {
      val oppijanKeskeytykset =
        oppija.kaikkiOppijaOidit.flatMap(keskeytykset)

      asValpasKelaOppija(oppija, oppijanKeskeytykset)
    })
  }

  private def asValpasKelaOppija(
    dbRow: ValpasOppivelvollisuustiedotRow,
    keskeytykset: Seq[OppivelvollisuudenKeskeytysRow]
  ): ValpasKelaOppija =
  {
    ValpasKelaOppija(
      henkilö = ValpasKelaHenkilö(
        oid = dbRow.oppijaOid,
        hetu = dbRow.hetu,
        oppivelvollisuusVoimassaAsti = dbRow.oppivelvollisuusVoimassaAsti,
        oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = Some(dbRow.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti)
      ),
      oppivelvollisuudenKeskeytykset = keskeytykset.map(asValpasKelaOppivelvollisuudenKeskeytys)
    )
  }

  private def tiedotAsValpasKelaOppijatWithOppivelvollisuudenKeskeytykset(oppijat: Seq[LaajatOppijaHenkilöTiedot]): Seq[ValpasKelaOppija] = {
    val keskeytykset: Map[String, Seq[OppivelvollisuudenKeskeytysRow]] =
      application.valpasOppivelvollisuudenKeskeytysRepository
        .getKeskeytykset(oppijat.flatMap(o => o.kaikkiOidit))
        .groupBy(_.oppijaOid)
        .withDefaultValue(Seq.empty)

    oppijat.map(oppija => {
      val oppijanKeskeytykset =
        oppija.kaikkiOidit.flatMap(keskeytykset)

      asValpasKelaOppija(oppija, oppijanKeskeytykset)
    })
  }

  private def asValpasKelaOppija(
    oppija: LaajatOppijaHenkilöTiedot,
    keskeytykset: Seq[OppivelvollisuudenKeskeytysRow]
  ): ValpasKelaOppija =
  {
    ValpasKelaOppija(
      henkilö = ValpasKelaHenkilö(
        oid = oppija.oid,
        hetu = oppija.hetu,
        // Tänne ei pitäisi koskaan päätyä syntymäajattomalla oppijalla, koska heitä ei katsota oppivelvollisiksi
        oppivelvollisuusVoimassaAsti =
          rajapäivätService.oppivelvollisuusVoimassaAstiIänPerusteella(oppija.syntymäaika.get),
        oikeusKoulutuksenMaksuttomuuteenVoimassaAsti =
          Some(rajapäivätService.maksuttomuusVoimassaAstiIänPerusteella(oppija.syntymäaika.get))
      ),
      oppivelvollisuudenKeskeytykset = keskeytykset.map(asValpasKelaOppivelvollisuudenKeskeytys)
    )
  }

  private def asValpasKelaOppivelvollisuudenKeskeytys(
    keskeytys: OppivelvollisuudenKeskeytysRow
  ): ValpasKelaOppivelvollisuudenKeskeytys = ValpasKelaOppivelvollisuudenKeskeytys(
    uuid = keskeytys.uuid.toString,
    alku = keskeytys.alku,
    loppu = keskeytys.loppu,
    luotu = keskeytys.luotu,
    peruttu = keskeytys.peruttu
  )
}
