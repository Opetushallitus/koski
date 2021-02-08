package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.raportointikanta.RHenkilöRow
import fi.oph.koski.valpas.repository.{ValpasDatabaseService, ValpasOppija}
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasOppijaService(application: KoskiApplication) {
  private lazy val dbService = new ValpasDatabaseService(application)

  def getOppijat(implicit session: ValpasSession): Option[Seq[RHenkilöRow]] = {
    None
  }

  def getOppija(oid: String)(implicit session: ValpasSession): Option[ValpasOppija] =
    dbService.getOppija(oid, ValpasAccessResolver.valpasOrganisaatioOids)
}
