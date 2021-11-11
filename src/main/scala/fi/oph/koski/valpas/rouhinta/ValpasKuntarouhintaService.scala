package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusrepository.HetuMasterOid
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö.Oid

class ValpasKuntarouhintaService(application: KoskiApplication)
  extends ValpasRouhintaTiming
    with DatabaseConverters
    with Logging
{
  private val oppijaService = application.valpasOppijaService
  private val rouhintaOvKeskeytyksetService = application.valpasRouhintaOppivelvollisuudenKeskeytysService

  def haeKunnanPerusteellaIlmanOikeustarkastusta
    (kunta: String)
  : Either[HttpStatus, KuntarouhinnanTulos] = {
    val oppivelvollisetKoskessa = getOppivelvollisetKotikunnalla(kunta)

    rouhintaTimed("haeKunnanPerusteella", oppivelvollisetKoskessa.size) {
      oppijaService
        // Kunnan käyttäjällä on aina oikeudet kaikkiin oppijoihin, joilla on oppivelvollisuus voimassa, joten
        // käyttöoikeustarkistusta ei tarvitse tehdä
        .getOppijalistaIlmanOikeustarkastusta(oppivelvollisetKoskessa.map(_.masterOid))
        .map(oppivelvollisetKoskessa => {
          rouhintaTimed("haeKunnanPerusteella:KuntarouhinnanTulos", oppivelvollisetKoskessa.size) {
            val eiSuorittavat =
              oppivelvollisetKoskessa
                .map(ValpasRouhintaOppivelvollinen.apply)
                .filterNot(_.suorittaaOppivelvollisuutta)

            val eiSuorittavatKeskeytyksillä =
              rouhintaOvKeskeytyksetService.fetchOppivelvollisuudenKeskeytykset(eiSuorittavat)

            KuntarouhinnanTulos(
              eiOppivelvollisuuttaSuorittavat = eiSuorittavatKeskeytyksillä,
            )
          }
        })
    }
  }

  private def getOppivelvollisetKotikunnalla(kunta: String): Seq[HetuMasterOid] = {
    timed("getOppivelvollisetKotikunnalla") {
      oppijaService.getOppivelvollisetKotikunnallaIlmanOikeustarkastusta(kunta)
    }
  }
}

case class KuntarouhinnanTulos(
  eiOppivelvollisuuttaSuorittavat: Seq[ValpasRouhintaOppivelvollinen],
) {
  def palautetutOppijaOidit: Seq[Oid] = eiOppivelvollisuuttaSuorittavat.map(_.oppijanumero)
}
