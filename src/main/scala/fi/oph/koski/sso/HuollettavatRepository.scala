package fi.oph.koski.sso

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema.Henkilö.Hetu
import fi.oph.koski.schema.Henkilötiedot

object HuollettavatRepository {
  def apply(): HuollettavatRepository = {
    new MockHuollettavatRepository()
  }
}

trait HuollettavatRepository {
  def getHuollettavatFromVTJ(hetu: Hetu): List[Henkilötiedot]
}

class MockHuollettavatRepository extends HuollettavatRepository {
  override def getHuollettavatFromVTJ(hetu: Hetu): List[Henkilötiedot] =
    if (MockOppijat.aikuisOpiskelija.hetu.contains(hetu)) {
      List(MockOppijat.ylioppilasLukiolainen.toHenkilötiedotJaOid)
    } else {
      Nil
    }
}
