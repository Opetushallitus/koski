package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.schema.Henkilö.{Hetu, Oid}

class HuollettavatService(application: KoskiApplication) {
  private val huollettavatRepository = HuollettavatRepository()

  def getHuollettavatWithHetu(hetu: Hetu): List[OppijaHenkilö] = for {
    huollettava <- huollettavatRepository.getHuollettavatFromVTJ(hetu)
    hetu <- huollettava.hetu
    oppija <- application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu, Some(huollettava.nimitiedot))
  } yield oppija

  def getHuollettavatWithOid(oid: Oid): List[OppijaHenkilö] =
    application.henkilöRepository.findByOid(oid).flatMap(_.hetu)
      .toList
      .flatMap(getHuollettavatWithHetu)
}
