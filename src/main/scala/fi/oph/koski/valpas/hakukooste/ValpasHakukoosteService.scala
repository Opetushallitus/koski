package fi.oph.koski.valpas.hakukooste

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.henkilo.OpintopolkuHenkilöFacade
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOppijaLaajatTiedot, ValpasOppivelvollinenOppijaLaajatTiedot}
import fi.oph.koski.valpas.oppija.OppijaHakutilanteillaLaajatTiedot

import scala.util.Try

trait ValpasHakukoosteService extends Logging {
  protected def localizationRepository: LocalizationRepository
  protected def opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade

  def fetchHautIlmanYhteystietoja(errorClue: String, oppijatJoilleHaetaanHakutiedot: Seq[Henkilö.Oid])(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] =
    fetchHautYhteystiedoilla(errorClue, oppijatJoilleHaetaanHakutiedot)(oppijat)
      .map(oppija => oppija.copy(yhteystiedot = Seq.empty))

  def fetchHautYhteystiedoilla(errorClue: String, oppijatJoilleHaetaanHakutiedot: Seq[Henkilö.Oid])(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] = {
    val oppijaOids = oppijat
      .collect { case o: ValpasOppivelvollinenOppijaLaajatTiedot => o }
      .map(_.henkilö.oid)
      .filter(oppijatJoilleHaetaanHakutiedot.contains)
      .toSet

    val hakukoosteet = getYhteishakujenHakukoosteet(oppijaOids = oppijaOids, ainoastaanAktiivisetHaut = true, errorClue = errorClue)
    val hakukoosteetOppijaOidit = hakukoosteet.map(_.map(_.oppijaOid).toSet)
    val tuntemattomatOppijaOiditHakukoosteissa = hakukoosteetOppijaOidit.map(_.diff(oppijaOids))
    val masterOppijaOiditTuntemattomille: Either[HttpStatus, Map[String, String]] = tuntemattomatOppijaOiditHakukoosteissa
      .map(tuntemattomatOppijaOids =>
        Try(
          if (tuntemattomatOppijaOids.nonEmpty) {
            opintopolkuHenkilöFacade.findMasterOppijat(tuntemattomatOppijaOids.toList).map {
              case (tuntematonOid, masterHenkilö) => tuntematonOid -> masterHenkilö.oid
            }
          } else {
            Map.empty[String, String]
          }
        ).recover {
          case e =>
            logger.warn(e)("ValpasHakukoosteService: master-oppijoiden haku hakemusten tuntemattomille oppijoille epäonnistui")
            Map.empty[String, String]
        }.get
      )

    val hakukoosteetGroupedByOppijaOid = for {
      koosteet <- hakukoosteet
      tuntematonToMasterOppijaOid <- masterOppijaOiditTuntemattomille
    } yield koosteet
      .groupBy(_.oppijaOid)
      .toSeq
      .map { case (oid, hakukoosteet) => tuntematonToMasterOppijaOid.getOrElse(oid, oid) -> hakukoosteet }
      .groupBy(_._1)
      .map { case (oid, hakukoosteet) => oid -> hakukoosteet.flatMap(_._2) }

    hakukoosteetGroupedByOppijaOid
      .fold(
        error => oppijat.map(oppija =>
          OppijaHakutilanteillaLaajatTiedot.apply(
            oppija = oppija,
            yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"),
            haut = Left(error))
        ),
        groups => {
          oppijat.map(oppija =>
            OppijaHakutilanteillaLaajatTiedot.apply(
              oppija = oppija,
              yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"),
              haut = Right(groups.getOrElse(oppija.henkilö.oid, Seq())))
          )
        }
      )
  }

  def getHakukoosteet(
    oppijaOids: Set[ValpasHenkilö.Oid],
    ainoastaanAktiivisetHaut: Boolean,
    errorClue: String
  ): Either[HttpStatus, Seq[Hakukooste]]

  def getYhteishakujenHakukoosteet(
    oppijaOids: Set[ValpasHenkilö.Oid],
    ainoastaanAktiivisetHaut: Boolean,
    errorClue: String
  ): Either[HttpStatus, Seq[Hakukooste]] = {
    getHakukoosteet(oppijaOids, ainoastaanAktiivisetHaut, errorClue).map(_.filter(hk => hk.hakutapa.koodiarvo == "01"))
  }
}

object ValpasHakukoosteService {
  def apply(application: KoskiApplication, overrideConfig: Option[Config] = None): ValpasHakukoosteService = {
    val config = overrideConfig match {
      case None => application.config
      case _ => overrideConfig.get
    }

    if (!config.getBoolean("valpas.hakukoosteEnabled")) {
      new DisabledHakukoosteService(application)
    } else if (Environment.isMockEnvironment(config)) {
      new MockHakukoosteService(application)
    } else {
      new SureHakukoosteService(
        application.valpasLocalizationRepository,
        application.opintopolkuHenkilöFacade,
        application.validatingAndResolvingExtractor,
        config,
        application.healthMonitoring,
      )
    }
  }
}
