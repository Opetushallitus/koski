package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö}
import fi.oph.koski.huoltaja.VtjHuollettavaHenkilö
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat.{oppivelvollinenYsiluokkaKeskenKeväällä2021, turvakieltoOppija, turvakieltoOppijanVanhempi}

object ValpasMockHuollettavatRepository {
  def getHuollettavat(oppija: OppijaHenkilö): Option[List[VtjHuollettavaHenkilö]] = {
    if (turvakieltoOppijanVanhempi.hetu.contains(oppija.hetu.get)) {
      Some(List(
        asHuollettava(turvakieltoOppija),
        asHuollettava(oppivelvollinenYsiluokkaKeskenKeväällä2021),
        VtjHuollettavaHenkilö("Olli", "Oiditon", "060488-681S")
      ))
    } else {
      None
    }
  }

  private def asHuollettava(oppija: LaajatOppijaHenkilöTiedot): VtjHuollettavaHenkilö = VtjHuollettavaHenkilö(
    etunimet = oppija.etunimet,
    sukunimi = oppija.sukunimi,
    hetu = oppija.hetu.get,
  )
}

