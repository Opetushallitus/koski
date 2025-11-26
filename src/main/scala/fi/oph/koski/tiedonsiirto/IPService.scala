package fi.oph.koski.tiedonsiirto

import java.net.InetAddress
import java.sql.SQLException

import fi.oph.koski.db.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiTables.OppilaitosIPOsoite
import fi.oph.koski.db.{QueryMethods, OppilaitosIPOsoiteRow}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import org.log4s.{Logger, getLogger}

class IPService(val db: DB) extends QueryMethods with Logging {
  def setIP(username: String, ip: String): Unit = try {
    runDbSync(OppilaitosIPOsoite.insertOrUpdate(OppilaitosIPOsoiteRow(username, ip)))
  } catch {
    case e:SQLException if e.getSQLState == "23505" =>
      logger.info("Inserting new ip address caused duplicate key exception, ignoring.")
  }

  def getIP(username: String): Option[InetAddress] =
    runDbSync(OppilaitosIPOsoite.filter(_.username === username).map(_.ip).result.headOption).map(InetAddress.getByName)

  def trackIPAddress(koskiSession: KoskiSpecificSession): Unit = {
    val ip = getIP(koskiSession.username)

    if (!ip.contains(koskiSession.clientIp)) {
      ip.foreach(IPTracking(koskiSession).logIPChange)
      setIP(koskiSession.username, koskiSession.clientIp.getHostAddress)
    }
  }
}

private case class IPTracking(koskiSession: KoskiSpecificSession) {
  def logIPChange(oldIP: InetAddress): Unit = {
    val user = koskiSession.user
    IPTracking.logger.info(s"${user.username}(${user.oid}), vanha: ${oldIP.getHostAddress}, uusi: ${koskiSession.clientIp.getHostAddress}")
  }
}

private object IPTracking {
  val logger: Logger = getLogger(classOf[IPTracking])
}
