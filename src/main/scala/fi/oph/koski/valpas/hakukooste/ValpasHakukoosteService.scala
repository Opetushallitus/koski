package fi.oph.koski.valpas.hakukooste

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOppijaLaajatTiedot}
import fi.oph.koski.valpas.oppija.OppijaHakutilanteillaLaajatTiedot

trait ValpasHakukoosteService {
  protected def localizationRepository: LocalizationRepository

  def fetchHautIlmanYhteystietoja(errorClue: String, oppijatJoilleHaetaanHakutiedot: Seq[Henkilö.Oid])(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] =
    fetchHautYhteystiedoilla(errorClue, oppijatJoilleHaetaanHakutiedot)(oppijat)
      .map(oppija => oppija.copy(yhteystiedot = Seq.empty))

  def fetchHautYhteystiedoilla(errorClue: String, oppijatJoilleHaetaanHakutiedot: Seq[Henkilö.Oid])(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] = {
    val oppijaOids = oppijat
      .map(_.henkilö.oid)
      .filter(oppijatJoilleHaetaanHakutiedot.contains)
      .toSet

    val hakukoosteet = getYhteishakujenHakukoosteet(oppijaOids = oppijaOids, ainoastaanAktiivisetHaut = true, errorClue = errorClue)

    hakukoosteet.map(_.groupBy(_.oppijaOid))
      .fold(
        error => oppijat.map(oppija => OppijaHakutilanteillaLaajatTiedot.apply(oppija = oppija, yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"), haut = Left(error))),
        groups => oppijat.map(oppija =>
          OppijaHakutilanteillaLaajatTiedot.apply(oppija = oppija, yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"), haut = Right(groups.getOrElse(oppija.henkilö.oid, Seq()))))
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
        application.validatingAndResolvingExtractor,
        config
      )
    }
  }
}
