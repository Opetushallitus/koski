package fi.oph.koski.tiedonsiirto

import java.net.InetAddress
import java.sql.SQLException

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.OppilaitosIPOsoite
import fi.oph.koski.db.{KoskiDatabaseMethods, OppilaitosIPOsoiteRow}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import org.log4s.{Logger, getLogger}

class IPService(val db: DB) extends KoskiDatabaseMethods with Logging {
  def setIP(username: String, ip: String): Unit = try {
    runDbSync(OppilaitosIPOsoite.insertOrUpdate(OppilaitosIPOsoiteRow(username, ip)))
  } catch {
    case e:SQLException if e.getSQLState == "23505" =>
      logger.info("Inserting new ip address caused duplicate key exception, ignoring.")
  }

  def getIP(username: String): Option[InetAddress] =
    runDbSync(OppilaitosIPOsoite.filter(_.username === username).map(_.ip).result.headOption).flatMap(toInetAddress)

  def trackIPAddress(koskiSession: KoskiSession) {
    val ip = getIP(koskiSession.username)

    if (ip != toInetAddress(koskiSession.lastClientIp)) {
      ip.foreach(IPTracking(koskiSession).logIPChange)
      setIP(koskiSession.username, koskiSession.lastClientIp)
    }
  }

  private def toInetAddress(host: String): Option[InetAddress] = try {
    Some(InetAddress.getByName(host))
  } catch {
    case e: Exception =>
      logger.error(e)(s"Error converting host $host to inet address")
      None
  }
}

private case class IPTracking(koskiSession: KoskiSession) {
  def logIPChange(oldIP: InetAddress) {
    val user = koskiSession.user
    IPTracking.logger.info(s"${user.username}(${user.oid}), vanha: ${oldIP.getHostAddress}, uusi: ${koskiSession.lastClientIp}")
  }
}

private object IPTracking {
  val logger: Logger = getLogger(classOf[IPTracking])
}
