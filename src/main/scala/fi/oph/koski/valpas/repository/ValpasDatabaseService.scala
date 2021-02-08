package fi.oph.koski.valpas.repository

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportointikanta.{Public, RHenkilöRow, ROpiskeluoikeusRow, Temp}
import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema.{RHenkilöTable, RHenkilöTableTemp, ROpiskeluoikeusTable, ROpiskeluoikeusTableTemp}

class ValpasDatabaseService(application: KoskiApplication) extends Logging {
  val schema = application.raportointiConfig.raportointiSchema.get
  val db = application.raportointiDatabase

  def getOppija(oppijaOid: String, organisaatioOids: Set[String]): Option[ValpasOppija] = {
    val opiskeluoikeudet = getOpiskeluoikeudet(oppijaOid, organisaatioOids)
    if (opiskeluoikeudet.nonEmpty) {
      getHenkilö(oppijaOid).map(henkilö => ValpasOppija(
        henkilö = ValpasHenkilö(henkilö),
        opiskeluoikeudet = opiskeluoikeudet.map(ValpasOpiskeluoikeus(_))
      ))
    } else {
      None
    }
  }

  def getHenkilö(oppijaOid: String): Option[RHenkilöRow] =
    db.runDbSync(RHenkilöt
      .filter(_.oppijaOid === oppijaOid)
      .result).headOption

  def getOpiskeluoikeudet(oppijaOid: String, organisaatioOids: Set[String]): Seq[ROpiskeluoikeusRow] =
    db.runDbSync(ROpiskeluoikeudet
      .filter(_.oppijaOid === oppijaOid)
      .filter(_.oppilaitosOid inSet organisaatioOids)
      .result)

  lazy val RHenkilöt = schema match {
    case Public => TableQuery[RHenkilöTable]
    case Temp => TableQuery[RHenkilöTableTemp]
  }

  lazy val ROpiskeluoikeudet = schema match {
    case Public => TableQuery[ROpiskeluoikeusTable]
    case Temp => TableQuery[ROpiskeluoikeusTableTemp]
  }
}

