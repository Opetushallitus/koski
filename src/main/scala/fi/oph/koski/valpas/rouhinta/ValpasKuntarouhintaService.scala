package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusrepository.HetuMasterOid
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasKuntarouhintaService(application: KoskiApplication) extends DatabaseConverters with Logging with ValpasRouhintaTiming {
  private val oppijaService = application.valpasOppijaService

  def haeKunnanPerusteella
    (kunta: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, KuntarouhinnanTulos] = {
    val oppivelvollisetKoskessa = getOppivelvollisetKotikunnalla(kunta)

    rouhintaTimed("haeKunnanPerusteella", oppivelvollisetKoskessa.size) {
      oppijaService
        .getOppijalista(oppivelvollisetKoskessa.map(_.masterOid))
        .map(oppivelvollisetKoskessa => {
          rouhintaTimed("haeKunnanPerusteella:KuntarouhinnanTulos", oppivelvollisetKoskessa.size) {
            val eiSuorittavat =
              oppivelvollisetKoskessa
                .map(ValpasRouhintaOppivelvollinen.apply)
                .filterNot(_.suorittaaOppivelvollisuutta)
            KuntarouhinnanTulos(
              eiOppivelvollisuuttaSuorittavat = eiSuorittavat,
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
)
